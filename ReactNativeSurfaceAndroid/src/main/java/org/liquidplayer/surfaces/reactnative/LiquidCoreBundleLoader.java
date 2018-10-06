package org.liquidplayer.surfaces.reactnative;

import com.facebook.react.bridge.CatalystInstanceImpl;
import com.facebook.react.bridge.JSBundleLoader;

public class LiquidCoreBundleLoader extends JSBundleLoader {
    /**
     * Loads the script, returning the URL of the source it loaded.
     */
    public static LiquidCoreBundleLoader createLiquidCoreLoader(
            final String proxySourceURL,
            final String realSourceURL,
            Runnable startFunction) {
        return new LiquidCoreBundleLoader(proxySourceURL, realSourceURL, startFunction);
    }

    private final Runnable mStartFunction;
    private final String mProxySourceURL;
    private final String mRealSourceURL;

    private LiquidCoreBundleLoader(
            final String proxySourceURL,
            final String realSourceURL,
            Runnable startFunction) {
        this.mStartFunction = startFunction;
        this.mProxySourceURL = proxySourceURL;
        this.mRealSourceURL = realSourceURL;
    }

    @Override
    public String loadScript(CatalystInstanceImpl instance) {
        instance.setSourceURLs(mProxySourceURL, mRealSourceURL);
        if (mStartFunction != null) {
            mStartFunction.run();
        }
        return mRealSourceURL;
    }
}
