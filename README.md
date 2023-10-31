# RTNThreedRenderer

RTNThreedRenderer is a React Native library for rendering glTF models on Android. This library provides a simple way to incorporate 3D models into your React Native applications, allowing you to enhance user experiences with 3D graphics.

## Features

- Render glTF 2.0 models in your React Native application.
- Support for textures, animations, and more.

## Getting Started

RTNThreedRenderer currently supports glb models for android and usdz models for iOS. It's essential to provide the filename with its extension, as the library caches the file upon the initial download. If you update the URL, make sure to also update the 'fileNameWithExtension' prop accordingly.

## IMP Note

This library supports New Architecture only. Refer [how to enable new architecture in react native over here](https://reactnative.dev/docs/next/the-new-architecture/use-app-template)

### Installation

```
npm i rtn-threed-renderer
# or
yarn add rtn-threed-renderer
```

## Android:

1. Add kotlin in your android folder

   Go to android folder => build.gradle

   ```
   ext {
       .....
       kotlinVersion = '1.8.20'
       }
   dependencies {
       ....
       classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
   }
   ```

   Go to android => app => build.gradle
   At the top of the file add

   `apply plugin: 'kotlin-android'`

   then go to the bottom and in dependencies

   ```
   dependencies {
    .....
    implementation "org.jetbrains.kotlin:kotlin-stdlib:1.8.20"
   }
   ```

2. To use, import the lib
   `import RTNThreedRenderer from 'rtn-threed-renderer/js/RTNThreedRendererNativeComponent';`

   ```
   <RTNThreedRenderer
     fileNameWithExtension="filename.glb"
     url="my_netowrk_url_where_glb_model_is_located"
     animationCount={0}
     style={{width: '100%', height: '100%'}}
   />
   ```

## iOS:

1. Add ModelIO & SceneKit to your ios project

Open your ios porject in xcode. Go to general tab and scroll down to Frameworks, Libraries and Embedded content
Add `ModelIO.framework` and `SceneKit.framework`

2. Update your pods, cd to ios directory in terminal
   `RCT_NEW_ARCH_ENABLED=1 bundle exec pod install`

3. To use, import the lib
   `import RTNThreedRenderer from 'rtn-threed-renderer/js/RTNThreedRendererNativeComponent';`

   ```
   <RTNThreedRenderer
     fileNameWithExtension="filename.usdz"
     url="my_network_url_where_usdz_model_is_located"
     style={{width: '100%', height: '100%'}}
   />
   ```
