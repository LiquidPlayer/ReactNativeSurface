package org.liquidplayer.surface.reactnative;

import android.app.Activity;
import android.os.Bundle;

import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactNativeHost;

import javax.annotation.Nullable;

class LiquidCoreReactActivityDelegate extends ReactActivityDelegate {
    private final ReactNativeSurface reactNativeSurface;
    private ReactNativeHost host;

    void setReactNativeHost(ReactNativeHost host) {
        this.host = host;
    }

    LiquidCoreReactActivityDelegate(ReactNativeSurface reactNativeSurface,
                                    @Nullable String mainComponentName) {
        super((Activity) reactNativeSurface.getContext(), mainComponentName);
        this.reactNativeSurface = reactNativeSurface;
    }

    @Override
    protected ReactNativeHost getReactNativeHost() {
        return host;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        if (getReactNativeHost() != null && getReactNativeHost().hasInstance()) {
            getReactNativeHost().getReactInstanceManager().onHostResume(
                    (Activity) reactNativeSurface.getContext(),
                    reactNativeSurface);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void loadApp(String appKey) {
        // No op
    }
}