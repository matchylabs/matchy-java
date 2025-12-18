package com.matchylabs.matchy;

import com.matchylabs.matchy.jna.NativeStructs;

public final class EntryData {
    
    private final boolean hasData;
    private final DataType type;
    private final NativeStructs.MatchyEntryData nativeData;
    
    EntryData(NativeStructs.MatchyEntryData nativeData) {
        this.nativeData = nativeData;
        this.hasData = nativeData.hasData();
        this.type = DataType.fromNative(nativeData.type_);
        if (hasData && type != null) {
            nativeData.setActiveUnionField();
        }
    }
    
    public boolean hasData() {
        return hasData;
    }
    
    public DataType getType() {
        return type;
    }
    
    public int getDataSize() {
        return nativeData.data_size;
    }
    
    public String getString() {
        if (type != DataType.UTF8_STRING) {
            throw new IllegalStateException("Data type is " + type + ", not UTF8_STRING");
        }
        return nativeData.value.getUtf8String();
    }
    
    public int getInt32() {
        if (type != DataType.INT32) {
            throw new IllegalStateException("Data type is " + type + ", not INT32");
        }
        return nativeData.value.int32;
    }
    
    public long getUint32() {
        if (type != DataType.UINT32 && type != DataType.POINTER) {
            throw new IllegalStateException("Data type is " + type + ", not UINT32");
        }
        return Integer.toUnsignedLong(nativeData.value.uint32);
    }
    
    public long getUint64() {
        if (type != DataType.UINT64) {
            throw new IllegalStateException("Data type is " + type + ", not UINT64");
        }
        return nativeData.value.uint64;
    }
    
    public short getUint16() {
        if (type != DataType.UINT16) {
            throw new IllegalStateException("Data type is " + type + ", not UINT16");
        }
        return nativeData.value.uint16;
    }
    
    public double getDouble() {
        if (type != DataType.DOUBLE) {
            throw new IllegalStateException("Data type is " + type + ", not DOUBLE");
        }
        return nativeData.value.double_value;
    }
    
    public float getFloat() {
        if (type != DataType.FLOAT) {
            throw new IllegalStateException("Data type is " + type + ", not FLOAT");
        }
        return nativeData.value.float_value;
    }
    
    public boolean getBoolean() {
        if (type != DataType.BOOLEAN) {
            throw new IllegalStateException("Data type is " + type + ", not BOOLEAN");
        }
        return nativeData.value.getBoolean();
    }
    
    public byte[] getBytes() {
        if (type != DataType.BYTES) {
            throw new IllegalStateException("Data type is " + type + ", not BYTES");
        }
        return nativeData.value.getBytes(nativeData.data_size);
    }
    
    public byte[] getUint128() {
        if (type != DataType.UINT128) {
            throw new IllegalStateException("Data type is " + type + ", not UINT128");
        }
        return nativeData.value.uint128;
    }
    
    @Override
    public String toString() {
        if (!hasData) {
            return "EntryData{hasData=false}";
        }
        return String.format("EntryData{type=%s, size=%d}", type, nativeData.data_size);
    }
}
