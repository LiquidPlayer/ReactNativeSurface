package org.liquidplayer.surface.reactnative;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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

import org.liquidplayer.javascript.JSContext;
import org.liquidplayer.javascript.JSFunction;
import org.liquidplayer.javascript.JSObject;
import org.liquidplayer.javascript.JSValue;
import org.liquidplayer.service.MicroService;

import java.util.Collections;
import java.util.List;

import static com.facebook.react.modules.systeminfo.AndroidInfoHelpers.getFriendlyDeviceName;

public class ReactNativeSurface extends ReactRootView
        implements DefaultHardwareBackBtnHandler {

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

    private ReactNativeJS session = null;

    void setSession(ReactNativeJS session) {
        uuid = session.getSessionUUID();
        this.session = session;
    }

    /* package */ void bind(final MicroService service,
                            final JSContext context,
                            final JSObject binding,
                            final JSValue config,
                            final Runnable onBound) {
        /*
         * ReactNative tries to overwrite global.console, but Node creates it with the read only
         * attribute set.  So we have to delete it first to remove the attribute.
         */
        context.evaluateScript(
                "var _c = global.console; " +
                        "delete global.console; " +
                        "global.console = _c;");

        final Application application = ((Activity)getContext()).getApplication();

        final ReactNativeHost reactNativeHost = new ReactNativeHost(application) {

            /* This must be false for use with LiquidCore because we override the
             * the serving code.
             */
            @Override
            public boolean getUseDeveloperSupport() {
                return config != null && config.isObject() && config.toObject().property("dev").toBoolean();
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
                        onBound.run();
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
        binding.property("startReactApplication", startReactApplication);

        // Module name will be set later by startApplicationFromJavascript
        startReactApplication(reactNativeHost.getReactInstanceManager(),
                "", null);
    }

    /* package */ void attach() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        setLayoutParams(params);
        mDelegate.onResume();
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
        session.setCurrentView(this);
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

    /* -- parcelable privates -- */
    private String uuid;

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.uuid = uuid;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        uuid = ss.uuid;
        session = ReactNativeJS.getSessionFromUUID(uuid);
    }

    static class SavedState extends BaseSavedState {
        String uuid;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            uuid = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(uuid);
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
