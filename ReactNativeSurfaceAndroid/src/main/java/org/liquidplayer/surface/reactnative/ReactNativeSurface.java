package org.liquidplayer.surface.reactnative;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.react.BuildConfig;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactInstanceManagerBuilder;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.CatalystInstanceImpl;
import com.facebook.react.bridge.JSBundleLoader;
import com.facebook.react.bridge.JavaScriptExecutorFactory;
import com.facebook.react.bridge.LiquidCoreJavaScriptExecutorFactory;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;

import org.liquidplayer.javascript.JSContext;
import org.liquidplayer.javascript.JSFunction;
import org.liquidplayer.javascript.JSObject;
import org.liquidplayer.jscshim.JSCShim;
import org.liquidplayer.service.MicroService;
import org.liquidplayer.service.Surface;
import org.liquidplayer.service.Synchronizer;

import java.util.Collections;
import java.util.List;

import static com.facebook.react.modules.systeminfo.AndroidInfoHelpers.getFriendlyDeviceName;

public class ReactNativeSurface extends ReactRootView
        implements Surface, DefaultHardwareBackBtnHandler {
    @SuppressWarnings("unused")
    public static String SURFACE_VERSION = BuildConfig.VERSION_NAME;

    public ReactNativeSurface(Context context) {
        this(context, null);
    }

    public ReactNativeSurface(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReactNativeSurface(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDelegate = createReactActivityDelegate();
    }

    @Override
    public void bind(final MicroService service, final JSContext context,
                     final Synchronizer synchronizer) {

        /*
         * ReactNative tries to overwrite global.console, but Node creates it with the read only
         * attribute set.  So we have to delete it first to remove the attribute.
         */
        context.evaluateScript(
                "var _c = global.console; " +
                        "delete global.console; " +
                        "global.console = _c;");

        final Application application = ((Activity)getContext()).getApplication();

        SoLoader.init(application,false);
        SoLoader.loadLibrary("liquidcore");
        SoLoader.loadLibrary("reactnativesurface");
        JSCShim.staticInit();

        final ReactNativeHost reactNativeHost = new ReactNativeHost(application) {

            /* This must be false for use with LiquidCore because we override the
             * the serving code.
             */
            @Override
            public boolean getUseDeveloperSupport() {
                return false;
            }

            @Override
            protected List<ReactPackage> getPackages() {
                return Collections.singletonList(new MainReactPackage());
            }

            @Override
            protected String getJSMainModuleName() {
                String path = service.getServiceURI().getPath();
                return path.substring(path.lastIndexOf('/') + 1);
            }

            @Override
            protected JSContext getJSContext() {
                return context;
            }

            @Override
            protected JavaScriptExecutorFactory getJavaScriptExecutorFactory() {
                String appName = getContext().getPackageName();
                String deviceName = getFriendlyDeviceName();

                return new LiquidCoreJavaScriptExecutorFactory(appName, deviceName, getJSContext());
            }

            private String getJSBundleUri() {
                return "assets://dummy_load.js";
            }

            JSBundleLoader createAssetLoader(
                    final Context context,
                    final String assetUrl) {
                return new JSBundleLoader() {
                    @Override
                    public String loadScript(CatalystInstanceImpl instance) {
                        instance.loadScriptFromAssets(context.getAssets(), assetUrl, false);
                        instance.setSourceURLs(service.getServiceURI().toString(), null);
                        /* Ok, React Native binding is complete, let the startup code continue */
                        synchronizer.exit();
                        //return assetUrl;
                        return service.getServiceURI().toString();
                    }
                };
            }

            protected ReactInstanceManager createReactInstanceManager() {
                ReactInstanceManagerBuilder builder = ReactInstanceManager.builder()
                        .setApplication(application)
                        .setJSMainModulePath(getJSMainModuleName())
                        .setUseDeveloperSupport(getUseDeveloperSupport())
                        .setRedBoxHandler(getRedBoxHandler())
                        .setJavaScriptExecutorFactory(getJavaScriptExecutorFactory())
                        .setUIImplementationProvider(getUIImplementationProvider())
                        .setInitialLifecycleState(LifecycleState.BEFORE_CREATE)
                        .setJSContext(getJSContext());

                for (ReactPackage reactPackage : getPackages()) {
                    builder.addPackage(reactPackage);
                }

                JSBundleLoader bundleLoader = createAssetLoader(getContext(),
                        getJSBundleUri());
                builder.setJSBundleLoader(bundleLoader);
                return builder.build();
            }

        };
        mDelegate.setReactNativeHost(reactNativeHost);

        getDelegate().onCreate(null);

        final JSFunction startReactApplication =
                new JSFunction(context, "startReactApplication") {
                    @SuppressWarnings("unused")
                    public void startReactApplication(String moduleName) {
                        if (moduleName != null) {
                            startApplicationFromJavascript(moduleName);
                        }
                    }
                };
        final JSObject reactnative = new JSObject(context);
        reactnative.property("startReactApplication", startReactApplication);
        context.property("LiquidCore").toObject().property("reactnative", reactnative);


        // Module name will be set later by startApplicationFromJavascript
        startReactApplication(reactNativeHost.getReactInstanceManager(),
                "", null);

        /* Don't let the startup code complete until we have finished setting up ReactNative
         * asynchronously.
         */
        synchronizer.enter();
    }

    @Override
    public void reset() {
        detach();
    }

    @Override
    public View attach(MicroService service, Runnable onAttached) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(params);
        mDelegate.onResume();

        onAttached.run();
        return this;
    }

    @Override
    public void detach() {
    }

    private final LiquidCoreReactActivityDelegate mDelegate;

    LiquidCoreReactActivityDelegate getDelegate() { return mDelegate; }


    protected @javax.annotation.Nullable
    String getMainComponentName() {
        return null;
    }

    protected LiquidCoreReactActivityDelegate createReactActivityDelegate() {
        return new LiquidCoreReactActivityDelegate(this, getMainComponentName());
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDelegate.onResume();
        setFocusableInTouchMode(true);
        requestFocus();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mDelegate.onPause();
        setFocusableInTouchMode(false);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if (mDelegate != null && keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mDelegate.onBackPressed()) {
                invokeDefaultOnBackPressed();
                return true;
            }
        }
        if (mDelegate != null) {
            return mDelegate.onKeyUp(keyCode, event);
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mDelegate != null && keyCode != KeyEvent.KEYCODE_BACK) {
            return mDelegate.onKeyDown(keyCode, event);
        } else if (mDelegate != null) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (mDelegate != null) {
            return mDelegate.onKeyLongPress(keyCode, event);
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        ((Activity)getContext()).onBackPressed();
    }

}
