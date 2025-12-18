package com.matchylabs.matchy;

@FunctionalInterface
public interface ReloadCallback {
    void onReload(ReloadEvent event);
}
