package com.example.wifiapp.model;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by fg607 on 15-12-22.
 */
public class FileInfo {

    private File file;
    private String filePath;
    private String fileName;
    private long fileSize;

    public FileInfo(String filePath){

        this.filePath = filePath;
        file = new File(filePath);
    }


    public File getFile() throws FileNotFoundException{
        if(file.exists()){

            return file;

        }else{

            throw new FileNotFoundException();
        }

    }

    public boolean exists(){

        return file.exists();
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {

        if(fileName != null){
            return fileName;
        }

        if(file.exists()){
            return file.getName();
        }else {

            return null;
        }

    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {

        if(file.exists()){
            return file.length();
        }else {

            return -1;
        }

    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "file=" + file +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                '}';
    }
}
