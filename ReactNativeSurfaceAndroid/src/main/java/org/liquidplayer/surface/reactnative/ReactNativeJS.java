package org.liquidplayer.surface.reactnative;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.facebook.react.BuildConfig;

import org.liquidplayer.caraml.CaramlJS;
import org.liquidplayer.caraml.CaramlSurface;
import org.liquidplayer.javascript.JSContext;
import org.liquidplayer.javascript.JSFunction;
import org.liquidplayer.javascript.JSObject;
import org.liquidplayer.javascript.JSPromise;
import org.liquidplayer.javascript.JSValue;
import org.liquidplayer.service.MicroService;

import java.util.HashMap;
import java.util.UUID;

public class ReactNativeJS extends JSObject implements CaramlSurface {
    public ReactNativeJS(final Context androidContext,
                         final MicroService service,
                         final JSContext context,
                         final JSObject binding,
                         final JSValue opts) {
        super(context);
        currentState = State.Init;
        JSFunction createEmitter = new JSFunction(context, "createEmitter",
                "let e=new events(); Object.assign(thiz,e); " +
                        "thiz.__proto__=e.__proto__", "thiz");
        createEmitter.call(null, this);
        emit = property("emit").toFunction();
        attachPromise = null;
        detachPromise = null;

        uuid = UUID.randomUUID().toString();
        sessionMap.put(uuid, this);
        final JSPromise promise = new JSPromise(context);

        new Handler(Looper.getMainLooper()).post(() -> {
            currentView = new ReactNativeSurface(androidContext);
            currentView.setSession(ReactNativeJS.this);
            currentView.bind(service, context, binding, opts, () -> {
                currentState = State.Detached;
                context.getGroup().schedule(() -> {
                    emit.call(ReactNativeJS.this, "ready");
                    promise.resolve();
                });
            });
        });
    }

    /*--
    /* JavaScript API
    /*--*/

    @jsexport @SuppressWarnings("unused")
    JSObject attach(JSValue value) {
        attachPromise = new JSPromise(getContext());
        try {
            if (value == null)
                throw new RuntimeException("attach: first argument must be a caraml object");
            if (currentState != State.Detached)
                throw new RuntimeException("attach: must be in detached state");
            caramlJS = CaramlJS.from(value);
            currentState = State.Attaching;
            caramlJS.attach(this);
        } catch (RuntimeException e) {
            onError(e);
        }

        return attachPromise;
    }

    @jsexport @SuppressWarnings("unused")
    String state() {
        switch (currentState) {
            case Init:      return "init";
            case Attaching: return "attaching";
            case Attached:  return "attached";
            case Detaching: return "detaching";
            case Detached:  return "detached";
        }
        if (BuildConfig.DEBUG) throw new AssertionError();
        return "error";
    }

    @jsexport @SuppressWarnings("unused")
    JSObject detach() {
        detachPromise = new JSPromise(getContext());
        JSPromise promise = detachPromise;
        if (currentState == State.Detached) {
            emit.call(this, "detached");
            detachPromise.resolve();
            detachPromise = null;
        } else if (currentState == State.Attached) {
            if (BuildConfig.DEBUG && caramlJS == null) throw new AssertionError();
            currentState = State.Detaching;
            caramlJS.detach();
        } else {
            detachPromise.reject("Attach/detach pending");
            detachPromise = null;
        }
        return promise;
    }

    /*--
    /* CaramlSurface implementation
    /*--*/

    @Override
    public View getView() {
        return currentView;
    }

    @Override
    public void onAttached(boolean fromRestore) {
        currentState = State.Attached;
        new Handler(Looper.getMainLooper()).post(() -> currentView.attach() );

        if (!fromRestore) {
            if (attachPromise != null) {
                attachPromise.resolve();
            }
            emit.call(this, "attached");
            attachPromise = null;
        }
    }

    @Override
    public void onDetached() {
        currentState = State.Detached;

        if (uuid != null) {
            sessionMap.remove(uuid);
        }

        caramlJS = null;

        emit.call(this, "detached");
        if (detachPromise != null) {
            detachPromise.resolve();
        }
        detachPromise = null;
    }

    @Override
    public void onError(Exception e) {
        if (currentState != State.Init) {
            currentState = State.Detached;
        }
        JSPromise promise;
        if (attachPromise != null) promise = attachPromise;
        else promise = detachPromise;

        if (promise != null) {
            promise.reject(e.getMessage());
        }
        emit.call(this, "error", e.getMessage());
        attachPromise = detachPromise = null;
    }

    /*--
    /* React Native session
    /*--*/

    static ReactNativeJS getSessionFromUUID(String id) {
        return sessionMap.get(id);
    }

    String getSessionUUID() { return uuid; }

    void setCurrentView(ReactNativeSurface view) {
        currentView = view;
    }

    void removeCurrentView(ReactNativeSurface view) {
        if (currentView == view) currentView = null;
    }

    /*--
    /* private statics
    /*--*/

    private static HashMap<String,ReactNativeJS> sessionMap = new HashMap<>();

    /*--
    /* session privates
    /*--*/

    private final JSFunction emit;
    private final String uuid;
    private JSPromise attachPromise;
    private JSPromise detachPromise;
    private ReactNativeSurface currentView;
    private CaramlJS caramlJS;

    private enum State {
        Init,
        Attaching,
        Attached,
        Detaching,
        Detached,
    }
    private State currentState;
}
