package org.liquidplayer.surface.reactnative;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactRootView;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

public class ReactNativeView extends ReactRootView
        implements DefaultHardwareBackBtnHandler {

    public ReactNativeView(Context context) {
        this(context, null);
    }

    public ReactNativeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReactNativeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDelegate = createReactActivityDelegate();
    }

    private final LiquidCoreReactActivityDelegate mDelegate;

    LiquidCoreReactActivityDelegate getDelegate() { return mDelegate; }

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     * e.g. "MoviesApp"
     */
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
