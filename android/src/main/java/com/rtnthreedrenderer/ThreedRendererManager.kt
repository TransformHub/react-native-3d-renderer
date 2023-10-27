package com.rtnthreedrenderer

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.RTNThreedRendererManagerDelegate
import com.facebook.react.viewmanagers.RTNThreedRendererManagerInterface

@ReactModule(name = ThreedRendererManager.NAME)
class ThreedRendererManager(private val context: ReactApplicationContext) :
    SimpleViewManager<ThreedRenderer>(),
    RTNThreedRendererManagerInterface<ThreedRenderer> {

    private val mDelegate: ViewManagerDelegate<ThreedRenderer>

    private var propUrl = ""
    private var propFileName = ""
    private var propAnimationIndex = 0

    init {
        mDelegate = RTNThreedRendererManagerDelegate(this)
    }

    override fun getDelegate(): ViewManagerDelegate<ThreedRenderer> {
        return mDelegate
    }

    override fun getName(): String {
        return NAME
    }

    companion object {
        const val NAME = "RTNThreedRenderer"
    }

    override fun createViewInstance(p0: ThemedReactContext): ThreedRenderer {
        return ThreedRenderer(context, context.currentActivity!!)
    }

    @ReactProp(name = "url")
    override fun setUrl(view: ThreedRenderer?, value: String?) {
        if (value != null) {
            propUrl = value

        }

        if(propUrl.isNotEmpty() && propFileName.isNotEmpty()){
            view?.setup(propUrl,propFileName,propAnimationIndex)
        }

    }

    @ReactProp(name = "fileNameWithExtension")
    override fun setFileNameWithExtension(view: ThreedRenderer?, value: String?) {
        if (value != null) {
            propFileName = value

        }
        if(propUrl.isNotEmpty() && propFileName.isNotEmpty()){
            view?.setup(propUrl,propFileName,propAnimationIndex)
        }
    }

    @ReactProp(name = "animationCount")
    override fun setAnimationCount(view: ThreedRenderer?, value: Int) {
        if(value >= 0){
            propAnimationIndex = value
        }
        if(propUrl.isNotEmpty() && propFileName.isNotEmpty()){
            view?.setup(propUrl,propFileName,propAnimationIndex)
        }
    }


}