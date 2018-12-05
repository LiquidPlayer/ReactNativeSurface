# React Native Surface

A LiquidCore surface that exposes the React Native (v. 0.56) API.  This is a work in progress.

[![](https://jitpack.io/v/LiquidPlayer/ReactNativeSurface.svg)](https://jitpack.io/#LiquidPlayer/ReactNativeSurface)


## Create a React Native project for use with LiquidCore

Refer to the React Native documentation for how to get started with React Native.  This assumes you are already familiar with it.

### Step 1: Create a React Native project

Important: you must use only version 0.56.0 of React Native.  Create a new project from the command line:

```
$ react-native init --version=0.56.0 HelloWorld
```

### Step 2: Add the LiquidCore shim

First, install the command-line utilties:

```
$ npm install -g liquidcore-cli
```

Then, initialize your project for use with LiquidCore:

```
$ liquidcore init --surface=.reactnative.ReactNative HelloWorld 
$ cd HelloWorld && npm install
```

### Step 3: Complete

You should now have the basic React Native starter project that is available for use with LiquidCore.  To run the development server, simply:

```
$ npm run server
```

Your project will remain entirely compatible with React Native.  The LiquidCore dev server does not conflict with the Metro server.  It uses port 8082 instead of 8081 by default.  You can still debug and develop your React Native project according to the documentation without conflict.  LiquidCore simply installs a little shim (namely, `liquid.js`) and enables a LiquidCore-specific server and bundler.

## Using ReactNativeSurface in your app

### Step 1: Create an app project

Create a new app in either Android Studio or XCode as normal.  Or if you already have an existing app, you can use that.  Refer to the documentation for your IDE on how to set up an app.  For the sake of this section, we will assume you have an app named _HelloWorld_.

### Step 2: Add the dependencies

#### Android

Go to your **root-level `build.grade`** file and add the `jitpack` dependency:

```
...

allprojects {
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}
...
```

Then, add the dependencies to your **app's `build.gradle`**:

```
dependencies {
    ...
    implementation 'com.github.LiquidPlayer:LiquidCore:0.5.1'
    implementation 'com.github.LiquidPlayer:ReactNativeSurface:0.56.0004'
    
    /*
     * Note: You must also include these React Native dependencies.  In future
     * releases, hopefully this won't be necessary.
     */
    implementation 'javax.inject:javax.inject:1'
    implementation 'com.facebook.fbui.textlayoutbuilder:textlayoutbuilder:1.0.0'
    implementation 'com.facebook.fresco:fresco:1.9.0'
    implementation 'com.facebook.fresco:imagepipeline-okhttp3:1.9.0'
    implementation 'com.facebook.soloader:soloader:0.3.0'
    implementation 'com.google.code.findbugs:jsr305:3.0.0'
    implementation 'com.squareup.okhttp3:okhttp:3.10.0'
    implementation 'com.squareup.okhttp3:okhttp-urlconnection:3.10.0'
    implementation 'com.squareup.okio:okio:1.14.0'
}
```

#### iOS

1. Install Carthage as described [here](https://github.com/Carthage/Carthage/blob/master/README.md#installing-carthage).
2. Create a [Cartfile](https://github.com/Carthage/Carthage/blob/master/Documentation/Artifacts.md#cartfile) that includes the following frameworks:
    ```
    git "git@github.com:LiquidPlayer/LiquidCore.git" ~> 0.5.1
    git "git@github.com:LiquidPlayer/ReactNativeSurface.git" ~> 0.56.0004
    ```
3. Run `carthage update`. This will fetch dependencies into a [Carthage/Checkouts](https://github.com/Carthage/Carthage/blob/master/Documentation/Artifacts.md#carthagecheckouts) folder, then build each one or download a pre-compiled framework.
4. On your application targets’ _General_ settings tab, in the “Linked Frameworks and Libraries” section, drag and drop `LiquidCore.framework` and `ReactNativeSurface.framework` from the [Carthage/Build](https://github.com/Carthage/Carthage/blob/master/Documentation/Artifacts.md#carthagebuild) folder on disk.
5. On your application targets’ _Build Phases_ settings tab, click the _+_ icon and choose _New Run Script Phase_. Create a Run Script in which you specify your shell (ex: `/bin/sh`), add the following contents to the script area below the shell:

    ```sh
    /usr/local/bin/carthage copy-frameworks
    ```
    Then, add the paths to the frameworks under “Input Files":

    ```
    $(SRCROOT)/Carthage/Build/iOS/LiquidCore.framework
    $(SRCROOT)/Carthage/Build/iOS/ReactNativeSurface.framework
    ```

    And finally, add the paths to the copied frameworks to the “Output Files”:

    ```
    $(BUILT_PRODUCTS_DIR)/$(FRAMEWORKS_FOLDER_PATH)/LiquidCore.framework
    $(BUILT_PRODUCTS_DIR)/$(FRAMEWORKS_FOLDER_PATH)/ReactNativeSurface.framework
    ```

### Step 3: Add the Liquid View to your app

You can either add the view in the interface builder or programmatically.  To add the view:

#### Android layout file

You can insert the view into any layout like so:

```xml
    <org.liquidplayer.service.LiquidView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/liquidview"
    />
```

#### iOS Interface Builder

Drag a `UIView` onto your storyboard in a `ViewController`.  Go to the identity inspector on the right and
set its custom class to `LCLiquidView`.

#### Android programmatic

```java
import org.liquidplayer.service.LiquidView;
...
    LiquidView liquidView = new LiquidView(androidContext);
```

#### iOS (Swift)
```swift
import LiquidCore
...
    let liquidView = LCLiquidView(frame: CGRect.zero)
```

### Step 4: Finish

This is all that is required to get up and running.  `LiquidView` defaults to using the dev server at port
8082.  See the documentation for `LiquidView` (Android) and `LCLiquidView` (iOS) in the [LiquidCore](https://github.com/LiquidPlayer/LiquidCore) project for options.

## Random Musings

This is very hastily thrown together documentation as this project is under active development.  So I am adding some
disorganized thoughts here.

* `ReactNativeSurface` is not yet suitable as a general-purpose surface.  No (or very little) consideration has been made regarding security.  Only use whitelisted domains or better yet, use local resource bundles until there has been more work in the area of security.
* On Android, the back button doesn't always work like you want it to.  This is a known issue.
* Developer mode doesn't work on either Android or iOS.  It doesn't work at all on Android and only somewhat works on iOS.  For the moment, use old fashioned React Native to do your debugging until this is resolved.


