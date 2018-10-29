package com.facebook.react.bridge.queue;

import android.os.Looper;

import com.facebook.react.common.MapBuilder;

import org.liquidplayer.javascript.JSContext;

import java.util.Map;

public class LiquidCoreReactQueueConfigurationImpl implements ReactQueueConfiguration {

    private final MessageQueueThreadImpl mUIQueueThread;
    private final MessageQueueThreadImpl mNativeModulesQueueThread;
    private final LiquidCoreMessageQueueThreadImpl mJSQueueThread;

    private LiquidCoreReactQueueConfigurationImpl(
            MessageQueueThreadImpl uiQueueThread,
            MessageQueueThreadImpl nativeModulesQueueThread,
            LiquidCoreMessageQueueThreadImpl jsQueueThread) {
        mUIQueueThread = uiQueueThread;
        mNativeModulesQueueThread = nativeModulesQueueThread;
        mJSQueueThread = jsQueueThread;
    }

    @Override
    public MessageQueueThread getUIQueueThread() {
        return mUIQueueThread;
    }

    @Override
    public MessageQueueThread getNativeModulesQueueThread() {
        return mNativeModulesQueueThread;
    }

    @Override
    public MessageQueueThread getJSQueueThread() {
        return mJSQueueThread;
    }

    /**
     * Should be called when the corresponding {@link com.facebook.react.bridge.CatalystInstance}
     * is destroyed so that we shut down the proper queue threads.
     */
    public void destroy() {
        if (mNativeModulesQueueThread.getLooper() != Looper.getMainLooper()) {
            mNativeModulesQueueThread.quitSynchronous();
        }
        /* FIXME
        if (mJSQueueThread.getLooper() != Looper.getMainLooper()) {
            mJSQueueThread.quitSynchronous();
        }
        */
    }

    public static LiquidCoreReactQueueConfigurationImpl create(
            JSContext jsContext,
            ReactQueueConfigurationSpec spec,
            QueueThreadExceptionHandler exceptionHandler) {
        Map<MessageQueueThreadSpec, MessageQueueThreadImpl> specsToThreads = MapBuilder.newHashMap();

        MessageQueueThreadSpec uiThreadSpec = MessageQueueThreadSpec.mainThreadSpec();
        MessageQueueThreadImpl uiThread =
                MessageQueueThreadImpl.create(uiThreadSpec, exceptionHandler);
        specsToThreads.put(uiThreadSpec, uiThread);

        /*
        MessageQueueThreadImpl jsThread = specsToThreads.get(spec.getJSQueueThreadSpec());
        if (jsThread == null) {
            jsThread = MessageQueueThreadImpl.create(spec.getJSQueueThreadSpec(), exceptionHandler);
        }
        */
        LiquidCoreMessageQueueThreadImpl jsThread = LiquidCoreMessageQueueThreadImpl.create(
                jsContext, spec.getJSQueueThreadSpec(), exceptionHandler);

        MessageQueueThreadImpl nativeModulesThread =
                specsToThreads.get(spec.getNativeModulesQueueThreadSpec());
        if (nativeModulesThread == null) {
            nativeModulesThread =
                    MessageQueueThreadImpl.create(spec.getNativeModulesQueueThreadSpec(), exceptionHandler);
        }

        return new LiquidCoreReactQueueConfigurationImpl(
                uiThread,
                nativeModulesThread,
                jsThread);
    }
}
