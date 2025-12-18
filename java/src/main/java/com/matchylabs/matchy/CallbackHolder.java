package com.matchylabs.matchy;

import com.matchylabs.matchy.jna.MatchyLibrary;
import com.sun.jna.CallbackReference;
import com.sun.jna.Pointer;

class CallbackHolder {
    
    private ReloadCallback javaCallback;
    private MatchyLibrary.ReloadCallbackNative nativeCallback;
    
    void setCallback(ReloadCallback callback) {
        this.javaCallback = callback;
        if (callback != null) {
            this.nativeCallback = (event, userData) -> {
                ReloadEvent javaEvent = new ReloadEvent(
                    event.path,
                    event.isSuccess(),
                    event.error,
                    event.generation
                );
                javaCallback.onReload(javaEvent);
            };
        } else {
            this.nativeCallback = null;
        }
    }
    
    Pointer getNativeCallback() {
        if (nativeCallback == null) {
            return Pointer.NULL;
        }
        return CallbackReference.getFunctionPointer(nativeCallback);
    }
    
    ReloadCallback getJavaCallback() {
        return javaCallback;
    }
}
