package com.matchylabs.matchy;

import com.matchylabs.matchy.jna.MatchyLibrary;
import com.matchylabs.matchy.jna.NativeStructs;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import java.util.ArrayList;
import java.util.List;

public final class Entry {
    
    private final NativeStructs.MatchyEntry nativeEntry;
    
    Entry(NativeStructs.MatchyEntry nativeEntry) {
        this.nativeEntry = nativeEntry;
    }
    
    public EntryData getValue(String... path) throws MatchyException {
        if (path == null || path.length == 0) {
            throw new IllegalArgumentException("path cannot be null or empty");
        }
        
        int ptrSize = Native.POINTER_SIZE;
        Memory pathArrayMem = new Memory((path.length + 1) * ptrSize);
        for (int i = 0; i < path.length; i++) {
            Memory strMem = new Memory(path[i].length() + 1);
            strMem.setString(0, path[i]);
            pathArrayMem.setPointer(i * ptrSize, strMem);
        }
        pathArrayMem.setPointer(path.length * ptrSize, Pointer.NULL);
        
        NativeStructs.MatchyEntryData entryData = new NativeStructs.MatchyEntryData();
        int result = MatchyLibrary.INSTANCE.matchy_aget_value(nativeEntry, entryData, pathArrayMem);
        
        if (result != MatchyLibrary.MATCHY_SUCCESS) {
            return new EntryData(entryData);
        }
        
        return new EntryData(entryData);
    }
    
    public List<EntryData> getDataList() throws MatchyException {
        PointerByReference listPtr = new PointerByReference();
        int result = MatchyLibrary.INSTANCE.matchy_get_entry_data_list(nativeEntry, listPtr);
        
        if (result != MatchyLibrary.MATCHY_SUCCESS) {
            throw new MatchyException("Failed to get entry data list");
        }
        
        List<EntryData> dataList = new ArrayList<>();
        Pointer ptr = listPtr.getValue();
        
        if (ptr != null && ptr != Pointer.NULL) {
            NativeStructs.MatchyEntryDataList current = new NativeStructs.MatchyEntryDataList(ptr);
            while (current != null) {
                dataList.add(new EntryData(current.entry_data));
                current = current.getNext();
            }
            MatchyLibrary.INSTANCE.matchy_free_entry_data_list(ptr);
        }
        
        return dataList;
    }
}
