package org.liquidplayer.surfaces.reactnative;

import com.facebook.react.bridge.JSCJavaScriptExecutor;
import com.facebook.react.bridge.JavaScriptExecutor;
import com.facebook.react.bridge.JavaScriptExecutorFactory;
import com.facebook.react.bridge.WritableNativeMap;

import org.liquidplayer.javascript.JSContext;

public class LiquidCoreJavaScriptExecutorFactory implements JavaScriptExecutorFactory {
    private final String mAppName;
    private final String mDeviceName;
    private final JSContext mNodeContext;

    LiquidCoreJavaScriptExecutorFactory(String appName, String deviceName,
                                               JSContext nodeContext) {
        this.mAppName = appName;
        this.mDeviceName = deviceName;
        this.mNodeContext = nodeContext;
    }

    @Override
    public JavaScriptExecutor create() throws Exception {
        WritableNativeMap jscConfig = new WritableNativeMap();
        jscConfig.putString("OwnerIdentity", "ReactNative");
        jscConfig.putString("AppIdentity", mAppName);
        jscConfig.putString("DeviceIdentity", mDeviceName);

        jscConfig.putString("JSContext", Long.toString(mNodeContext.getJSCContext()));

        return new JSCJavaScriptExecutor(jscConfig);
    }
}
