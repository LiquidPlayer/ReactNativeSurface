package com.facebook.react.bridge;

import org.liquidplayer.javascript.JSContext;

public class LiquidCoreJavaScriptExecutorFactory implements JavaScriptExecutorFactory {
    private final String mAppName;
    private final String mDeviceName;
    private final JSContext mNodeContext;

    public LiquidCoreJavaScriptExecutorFactory(String appName, String deviceName,
                                        JSContext nodeContext) {
        this.mAppName = appName;
        this.mDeviceName = deviceName;
        this.mNodeContext = nodeContext;
    }

    @Override
    public JavaScriptExecutor create() {
        WritableNativeMap jscConfig = new WritableNativeMap();
        jscConfig.putString("OwnerIdentity", "ReactNative");
        jscConfig.putString("AppIdentity", mAppName);
        jscConfig.putString("DeviceIdentity", mDeviceName);

        jscConfig.putString("UseContext", Long.toString(mNodeContext.getJSCContext()));

        return new JSCJavaScriptExecutor(jscConfig);
    }

}
