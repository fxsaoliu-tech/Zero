package com.zero.server.file;

import com.zero.Zero;
import com.zero.client.sound.gun.GunSoundPlayManager;
import com.zero.client.util.ZeroResources;
import com.zero.server.ServerProxy;
import com.zero.server.item.ItemAttachment;
import com.zero.server.item.ItemBullets;
import com.zero.server.item.ItemGun;
import com.zero.server.type.AttachmentType;
import com.zero.server.type.BulletType;
import com.zero.server.type.GunType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class FileRead {

    public static void saveToItem() {
        FileType[] type = FileType.values();
        for (FileType fileType : type) {
            List<FileList> fileLists = FileList.Files.get(fileType);
            for (FileList file : fileLists) {
                switch (fileType) {
                    case BULLET:
                        new ItemBullets(new BulletType().loadContent(file));
                        break;
                    case GUN:
                        new ItemGun(new GunType().loadContent(file));
                        break;
                    case ATTACHMENT:
                        new ItemAttachment(new AttachmentType().loadContent(file));
                }
            }
        }
    }

    public static void readContentPacks(File modFile) {
        List<File> contentPacks = Zero.server.getContentList(modFile);
        for (File file : contentPacks) {
            if (ServerProxy.zipJar(file.getName())) {
                zipRead(file);
            } else {
                fileRead(file);
            }
        }
        Zero.server.forceReload();
        saveToItem();
        FileJsonWrite.createAll(modFile);
    }

    private static void fileRead(File dir) {
        File[] fileType = dir.listFiles();
        if (fileType == null) {
            return;
        }
        for (File file : fileType) {
            FileType type = FileType.getFileType(file.getName());
            if (file.isDirectory() && type != null) {
                File[] txt = file.listFiles();
                if (txt == null) {
                    continue;
                }
                for (File tx : txt) {
                    if (tx.getName().endsWith(".txt")) {
                        try (BufferedReader reader = Files.newBufferedReader(tx.toPath(), StandardCharsets.UTF_8)) {
                            writeFileList(dir.getName(), type.getName(), tx.getName(), reader);
                        } catch (IOException e) {
                            Zero.error("读取文件失败: " + tx.getName());
                        }
                    }
                }
            }
        }
    }


    private static void zipRead(File jarFile) {
        try (ZipInputStream zipStream = new ZipInputStream(Files.newInputStream(jarFile.toPath()))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipStream.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    continue;
                }
                String[] zipEntryList = zipEntry.getName().split("/");
                if (zipEntryList.length == 0) {
                    continue;
                }
                String fileName = zipEntryList[0];
                String txtName = zipEntryList[zipEntryList.length - 1];
                // 不使用 try-with-resources，避免自动关闭 zipStream
                BufferedReader reader = new BufferedReader(new InputStreamReader(zipStream, StandardCharsets.UTF_8));
                try {
                    writeFileList(jarFile.getName(), fileName, txtName, reader);
                } catch (IOException e) {
                    Zero.logger.error("读取单个ZipEntry失败: " + zipEntry.getName(), e);
                }
                // 注意：这里不能关闭 reader，因为关闭它会关闭 zipStream
                // 可以手动刷新 reader 或忽略，因为 zipStream 最终会在外层 try 中关闭
            }
        } catch (IOException e) {
            Zero.logger.error("读取压缩包失败: " + jarFile.getName(), e);
        }
    }

    private static void writeFileList(String content, String nameFile, String nameText, BufferedReader reader) throws IOException {
        if (nameText.endsWith(".txt")) {
            FileType type = FileType.getFileType(nameFile);
            if (type != null) {
                FileList list = new FileList(content, nameText, type);
                String line = reader.readLine();
                while (line != null) {
                    list.parseLine(line);
                    line = reader.readLine();
                }
            }
        }
    }
}
