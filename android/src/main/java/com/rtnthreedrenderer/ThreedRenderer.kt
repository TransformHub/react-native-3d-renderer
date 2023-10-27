package com.rtnthreedrenderer

import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import android.net.Uri
import android.util.Log
import android.view.Choreographer
import android.view.GestureDetector
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.google.android.filament.Fence
import com.google.android.filament.IndirectLight
import com.google.android.filament.Skybox
import com.google.android.filament.View
import com.google.android.filament.android.UiHelper
import com.google.android.filament.utils.AutomationEngine
import com.google.android.filament.utils.HDRLoader
import com.google.android.filament.utils.IBLPrefilterContext
import com.google.android.filament.utils.KTX1Loader
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.RemoteServer
import com.google.android.filament.utils.Utils
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.URI
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

class ThreedRenderer(context: ReactApplicationContext, private val activity: Activity): LinearLayout(context), LifecycleEventListener{

    companion object {
        init {
            Utils.init()
        }
    }

    private var url = ""
    private var fileName = ""
    private var animationIndex = 0

    private var choreographer: Choreographer
    private var uiHelper: UiHelper

    private var modelViewer: ModelViewer

    private val viewerContent = AutomationEngine.ViewerContent()
    private val frameScheduler = FrameCallback()
    private val automation = AutomationEngine()

    private var path: File? = null
    private var file: File? = null

    private val lifecycle: Lifecycle by lazy {
        ((context as ReactContext).currentActivity as AppCompatActivity).lifecycle
    }

    init {
        context.addLifecycleEventListener(this)

        val layoutParams: ViewGroup.LayoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setLayoutParams(layoutParams)
        val surfaceView = SurfaceView(context)
        surfaceView.layoutParams = layoutParams
        addView(surfaceView)



        choreographer = Choreographer.getInstance()
        uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK).apply {
            // This is needed to make the background transparent
            isOpaque = false
        }

        modelViewer = ModelViewer(surfaceView = surfaceView, uiHelper = uiHelper)

        // This is needed so we can move the camera in the rendering
        surfaceView.setOnTouchListener { _, event ->
            modelViewer.onTouchEvent(event)
            true
        }

    }

    fun setup(propUrl: String, propFileName: String, propAnimationIndex: Int){
        url = propUrl
        fileName = propFileName
        animationIndex = propAnimationIndex
        if(url.isNotEmpty() && fileName.isNotEmpty()){
            lifecycle.coroutineScope.launch {
                downloadGltf()
                createIndirectLight()
                configureViewer()
            }
        }

    }

    private fun createPath(): File? {
        path = File(context.filesDir, "gltf")
        if (path?.exists() == false) {
            path?.mkdirs()
        }
        return path
    }

    private fun splitUrl(url: String): UrlParts {
        val uri = Uri.parse(url)
        val scheme = uri.scheme
        val host = uri.host
        val port = uri.port
        val path = uri.path
        val query = uri.query

        // Reconstruct the base URL
        val baseUrl = StringBuilder()

        if (scheme != null) {
            baseUrl.append(scheme)
            baseUrl.append("://")
        }

        if (host != null) {
            baseUrl.append(host)

            if (port != -1) {
                baseUrl.append(":")
                baseUrl.append(port)
            }

            // Append a trailing slash to the base URL
            baseUrl.append("/")
        }

        // Reconstruct the rest of the URL
        val restOfUrl = StringBuilder()

        if (path != null) {
            restOfUrl.append(path.trimStart('/'))
        }

        if (query != null) {
            // Append the query parameters
            restOfUrl.append("?")
            restOfUrl.append(query)
        }



        return UrlParts(baseUrl.toString(), restOfUrl.toString())
    }

    private suspend fun downloadModel(): String? {
        file = File(createPath(), fileName)
        if(file?.exists() == true){
            return "gltf/${fileName}"
        } else {
            val urlParts = splitUrl(url)
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.MINUTES)
                .readTimeout(3, TimeUnit.MINUTES)
                .build()
            val retrofit = Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(urlParts.baseUrl)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .build().create(GltfApi::class.java)

            val response =
                retrofit.downloadFile(urlParts.restOfUrl)
            return if (response.isSuccessful) {
                file?.absolutePath?.let { saveFile(response.body(), it) }
                return "gltf/${fileName}"
            } else {
                null
            }
        }
    }

    private fun saveFile(body: ResponseBody?, pathWhereYouWantToSaveFile: String): String {
        if (body == null)
            return ""
        var input: InputStream? = null
        try {
            input = body.byteStream()

            //val file = File(getCacheDir(), "cacheFileAppeal.srl")
            val fos = FileOutputStream(pathWhereYouWantToSaveFile)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            return pathWhereYouWantToSaveFile
        } catch (e: Exception) {
        } finally {
            input?.close()
        }
        return ""
    }

    private suspend fun downloadGltf(){
        withContext(Dispatchers.IO){
            val data = downloadModel()
            if(data != null) {
                val file = File(context.filesDir, "gltf/${fileName}")
                val size: Int = file.length().toInt()
                val bytes = ByteArray(size)
                try {
                    val buf = BufferedInputStream(FileInputStream(file))
                    buf.read(bytes, 0, bytes.size)
                    buf.close()
                    withContext(Dispatchers.Main) {
                        createDefaultRenderables(bytes)
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }  else {
                activity.runOnUiThread{
                    Toast.makeText(context,"Data is null",Toast.LENGTH_LONG).show()
                }

            }
        }

    }

    private fun createDefaultRenderables(bytes:  ByteArray) {

        modelViewer.loadModelGltfAsync(ByteBuffer.wrap(bytes)) { uri ->
            readCompressedAsset(
                "models/$uri"
            )
        }

        updateRootTransform()

    }

    private fun updateRootTransform() {
        if (automation.viewerOptions.autoScaleEnabled) {
            modelViewer.transformToUnitCube()
        } else {
            modelViewer.clearRootTransform()
        }
        
    }

    private fun configureViewer(){
        modelViewer.view.blendMode = View.BlendMode.TRANSLUCENT
        modelViewer.renderer.clearOptions = modelViewer.renderer.clearOptions.apply {
            clear = true
        }

        modelViewer.view.apply {
            renderQuality = renderQuality.apply {
                hdrColorBuffer = View.QualityLevel.MEDIUM
            }
            dynamicResolutionOptions = dynamicResolutionOptions.apply {
                enabled = true
                quality = View.QualityLevel.MEDIUM
            }
            multiSampleAntiAliasingOptions = multiSampleAntiAliasingOptions.apply {
                enabled = true
            }
            antiAliasing = View.AntiAliasing.FXAA
            ambientOcclusionOptions = ambientOcclusionOptions.apply {
                enabled = true
            }
            bloomOptions = bloomOptions.apply {
                enabled = true
            }
        }
    }

    private fun createIndirectLight() {
        val engine = modelViewer.engine
        val scene = modelViewer.scene
        val ibl = "venetian_crossroads_2k"
        readCompressedAsset("${ibl}_ibl.ktx").let {
            scene.indirectLight = KTX1Loader.createIndirectLight(engine, it)
            scene.indirectLight!!.intensity = 30_000.0f
            viewerContent.indirectLight = modelViewer.scene.indirectLight
        }
        readCompressedAsset("${ibl}_skybox.ktx").let {
            scene.skybox = KTX1Loader.createSkybox(engine, it)
        }
    }

    private fun readCompressedAsset(assetName: String): ByteBuffer {
        val input = context.assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }

    inner class FrameCallback : Choreographer.FrameCallback {

        private val startTime = System.nanoTime()

        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)
            modelViewer.animator?.apply {
                if (animationCount > 0 && animationIndex <= animationCount - 1) {
                    val elapsedTimeSeconds = (frameTimeNanos - startTime).toDouble() / 1_000_000_000
                    applyAnimation(animationIndex, elapsedTimeSeconds.toFloat())
                }
                updateBoneMatrices()
            }

            modelViewer.render(frameTimeNanos)
        }
    }


    override fun onHostResume() {
        choreographer.postFrameCallback(frameScheduler)
    }

    override fun onHostPause() {
        choreographer.removeFrameCallback(frameScheduler)
    }

    override fun onHostDestroy() {
        choreographer.removeFrameCallback(frameScheduler)
    }


}