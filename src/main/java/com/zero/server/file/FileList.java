package com.zero.server.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileList {
    private String nameContentPack;
    private FileType fileType;
    private String nameText;
    private int readerPosition = 0;// 读取索引位置
    private final List<String> lines = new ArrayList<>();//文本内容
    public final static HashMap<FileType, List<FileList>> Files = new HashMap<>(); //文本列表

    //静态区块
    static {
        for (FileType type : FileType.values()) {
            Files.put(type, new ArrayList<>());
        }
    }

    public FileList(String nameContentPack, String nameText, FileType fileType) {
        if (fileType == null) {
            this.nameContentPack = nameContentPack;
            this.nameText = nameText;
            return;
        }
        if (Files.containsKey(fileType)) {
            this.nameContentPack = nameContentPack;
            this.nameText = nameText;
            this.fileType = fileType;
            Files.get(fileType).add(this);
        }
    }

    //写入字符串
    public void parseLine(String line) {
        if (line != null) {
            lines.add(line);
        }
    }

    //读取字符串
    public String readLine() {
        if (readerPosition == lines.size()) {
            return null;
        }
        return lines.get(readerPosition++);
    }

    //获取字符串
    public List<String> getLines() {
        if (readerPosition == lines.size()) {
            return null;
        }
        readerPosition = lines.size();
        return lines;
    }

    //获取内容包名称
    public String getNameContentPack() {
        return nameContentPack;
    }


    //获取属性
    public FileType getType() {
        return fileType;
    }

    //获取文本名字
    public String getNameText() {
        return nameText;
    }
}
