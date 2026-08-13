package com.zero.server.file;

import com.zero.server.type.InfoType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileData {
    public FileList fileList;
    public InfoType infoType;

    public static final Map<String, List<FileData>> JsonData = new HashMap<>();


    public FileData(FileList fileList, InfoType infoType) {
        if (JsonData.containsKey(fileList.getNameContentPack())) {
            JsonData.get(fileList.getNameContentPack()).add(this);
        } else {
            List<FileData> list = new ArrayList<>();
            list.add(this);
            JsonData.put(fileList.getNameContentPack(), list);
        }
        this.fileList = fileList;
        this.infoType = infoType;
    }
}
