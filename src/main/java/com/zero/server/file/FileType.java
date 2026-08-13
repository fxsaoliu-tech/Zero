package com.zero.server.file;

import com.zero.server.type.AttachmentType;
import com.zero.server.type.BulletType;
import com.zero.server.type.GunType;
import com.zero.server.type.InfoType;

public enum FileType {
    ATTACHMENT("attachments"),
    BULLET("bullets"),
    GUN("guns");

    private String name;

    FileType(String name) {
        this.name = name;
    }

    public static FileType getFileType(String name) {
        for (FileType fileType : FileType.values()) {
            if (name.equalsIgnoreCase(fileType.name)) {
                return fileType;
            }
        }
        return null;
    }

    public static FileType getIndex(InfoType type){
        if (type instanceof GunType) {
            return GUN;
        }else if (type instanceof BulletType) {
            return BULLET;
        } else if (type instanceof AttachmentType) {
            return ATTACHMENT;
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
