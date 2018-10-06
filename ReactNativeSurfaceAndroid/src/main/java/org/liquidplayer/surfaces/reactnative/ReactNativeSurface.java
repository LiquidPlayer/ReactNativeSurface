package org.liquidplayer.surfaces.reactnative;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.facebook.react.BuildConfig;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactInstanceManagerBuilder;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.JavaScriptExecutorFactory;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;

import org.liquidplayer.javascript.JSContext;
import org.liquidplayer.javascript.JSFunction;
import org.liquidplayer.javascript.JSObject;
import org.liquidplayer.service.MicroService;
import org.liquidplayer.service.Surface;
import org.liquidplayer.service.Synchronizer;

import java.util.Collections;
import java.util.List;

import static com.facebook.react.modules.systeminfo.AndroidInfoHelpers.getFriendlyDeviceName;

public class ReactNativeSurface extends ReactRootView implements Surface {
    public static String SURFACE_VERSION = BuildConfig.VERSION_NAME;

    public ReactNativeSurface(Context context) {
        this(context, null);
    }

    public ReactNativeSurface(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReactNativeSurface(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
                return Collections.<ReactPackage>singletonList(new MainReactPackage());
            }

            @Override
            protected String getJSMainModuleName() {
                return "index";
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

            private final Runnable mStartFunction = new Runnable() {
                @Override
                public void run() {
                    /* Ok, React Native binding is complete, let the startup code continue */
                    synchronizer.exit();
                }
            };

            private String getJSBundleUri() {
                // FIXME: This doesn't really matter, but would be nice to use a meaningful name
                return "index.bundle.js";
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

                LiquidCoreBundleLoader bundleLoader = LiquidCoreBundleLoader.createLiquidCoreLoader(
                        getJSBundleUri(), getJSBundleUri(), mStartFunction
                );
                builder.setJSBundleLoader(bundleLoader);
                return builder.build();
            }

        };

        /* Don't let the startup code complete until we have finished setting up ReactNative
         * asynchronously.
         */
        synchronizer.enter();
        ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();

        final JSFunction startReactApplication =
                new JSFunction(context, "startReactApplication") {
                    public void startReactApplication(String moduleName) {
                        if (moduleName != null) {
                            startApplicationFromJavascript(moduleName);
                        }
                    }
                };
        final JSObject reactnative = new JSObject(context);
        reactnative.property("startReactApplication", startReactApplication);
        context.property("LiquidCore").toObject().property("reactnative", reactnative);

        // Module name will be set later by startReactApplication
        startReactApplication(reactInstanceManager, "", null);
    }

    @Override
    public void reset() {
        detach();
    }

    @Override
    public View attach(MicroService service, Runnable onAttached) {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        setLayoutParams(params);
        onAttached.run();
        return this;
    }

    @Override
    public void detach() {
    }

}
