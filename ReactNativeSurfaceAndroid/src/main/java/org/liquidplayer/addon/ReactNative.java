package org.liquidplayer.addon;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.facebook.react.BuildConfig;
import com.facebook.soloader.SoLoader;

import org.liquidplayer.javascript.JSFunction;
import org.liquidplayer.javascript.JSObject;
import org.liquidplayer.javascript.JSValue;
import org.liquidplayer.service.AddOn;
import org.liquidplayer.service.MicroService;
import org.liquidplayer.surface.reactnative.ReactNativeJS;

public class ReactNative implements AddOn {
    public ReactNative(Context androidContext) {
        this.androidContext = androidContext;
    }

    @Override
    public void register(String module) {
        if (BuildConfig.DEBUG && !module.equals("react-native")) { throw new AssertionError(); }
        final Application application = ((Activity)androidContext).getApplication();
        System.loadLibrary("react-native.node");

        register();
    }

    @Override
    public void require(final JSValue binding, MicroService service) {
        if (BuildConfig.DEBUG && (binding == null || !binding.isObject())) {
            throw new AssertionError();
        }

        JSObject bindingObject = binding.toObject();

        bindingObject.property("React",
            new JSFunction(binding.getContext(), "React") {
                @SuppressWarnings("unused") public JSObject React(JSValue opts) {
                    if (singleton == null) {
                        singleton = new ReactNativeJS(androidContext, service, binding.getContext(),
                                bindingObject, opts);
                    }

                    return singleton;
                }
            });
    }

    private final Context androidContext;
    private ReactNativeJS singleton;

    native static void register();
}
