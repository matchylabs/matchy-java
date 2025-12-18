package com.matchylabs.matchy;

import com.matchylabs.matchy.jna.NativeStructs;

public enum ValidationLevel {
    STANDARD(NativeStructs.MATCHY_VALIDATION_STANDARD),
    STRICT(NativeStructs.MATCHY_VALIDATION_STRICT);
    
    private final int nativeValue;
    
    ValidationLevel(int nativeValue) {
        this.nativeValue = nativeValue;
    }
    
    public int getNativeValue() {
        return nativeValue;
    }
}
