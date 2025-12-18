package com.matchylabs.matchy;

public final class ReloadEvent {
    
    private final String path;
    private final boolean success;
    private final String error;
    private final long generation;
    
    ReloadEvent(String path, boolean success, String error, long generation) {
        this.path = path;
        this.success = success;
        this.error = error;
        this.generation = generation;
    }
    
    public String getPath() {
        return path;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getError() {
        return error;
    }
    
    public long getGeneration() {
        return generation;
    }
    
    @Override
    public String toString() {
        if (success) {
            return String.format("ReloadEvent{path='%s', success=true, generation=%d}", path, generation);
        } else {
            return String.format("ReloadEvent{path='%s', success=false, error='%s'}", path, error);
        }
    }
}
