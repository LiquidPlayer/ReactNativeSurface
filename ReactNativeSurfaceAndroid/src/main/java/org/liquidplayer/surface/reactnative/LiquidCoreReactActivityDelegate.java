package org.liquidplayer.surface.reactnative;

import android.app.Activity;
import android.os.Bundle;

import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactNativeHost;

import javax.annotation.Nullable;

class LiquidCoreReactActivityDelegate extends ReactActivityDelegate {
    private final ReactNativeView reactNativeView;
    private ReactNativeHost host;

    void setReactNativeHost(ReactNativeHost host) {
        this.host = host;
    }

    LiquidCoreReactActivityDelegate(ReactNativeView reactNativeView,
                                    @Nullable String mainComponentName) {
        super((Activity) reactNativeView.getContext(), mainComponentName);
        this.reactNativeView = reactNativeView;
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
        if (getReactNativeHost().hasInstance()) {
            getReactNativeHost().getReactInstanceManager().onHostResume(
                    (Activity) reactNativeView.getContext(),
                    reactNativeView);
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