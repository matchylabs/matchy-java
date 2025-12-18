package com.matchylabs.matchy;

import com.matchylabs.matchy.jna.NativeStructs;

public enum DataType {
    POINTER(NativeStructs.MATCHY_DATA_TYPE_POINTER),
    UTF8_STRING(NativeStructs.MATCHY_DATA_TYPE_UTF8_STRING),
    DOUBLE(NativeStructs.MATCHY_DATA_TYPE_DOUBLE),
    BYTES(NativeStructs.MATCHY_DATA_TYPE_BYTES),
    UINT16(NativeStructs.MATCHY_DATA_TYPE_UINT16),
    UINT32(NativeStructs.MATCHY_DATA_TYPE_UINT32),
    MAP(NativeStructs.MATCHY_DATA_TYPE_MAP),
    INT32(NativeStructs.MATCHY_DATA_TYPE_INT32),
    UINT64(NativeStructs.MATCHY_DATA_TYPE_UINT64),
    UINT128(NativeStructs.MATCHY_DATA_TYPE_UINT128),
    ARRAY(NativeStructs.MATCHY_DATA_TYPE_ARRAY),
    BOOLEAN(NativeStructs.MATCHY_DATA_TYPE_BOOLEAN),
    FLOAT(NativeStructs.MATCHY_DATA_TYPE_FLOAT);
    
    private final int nativeValue;
    
    DataType(int nativeValue) {
        this.nativeValue = nativeValue;
    }
    
    public int getNativeValue() {
        return nativeValue;
    }
    
    public static DataType fromNative(int nativeValue) {
        for (DataType type : values()) {
            if (type.nativeValue == nativeValue) {
                return type;
            }
        }
        return null;
    }
}
