package com.cryptopaths.cryptofm.filemanager;

import java.io.File;
import java.math.BigDecimal;

/**
 * Created by tripleheader on 12/17/16.
 */

public class FileUtils {
    private final static float BYTE_MB=1048576f;

    public static float getFileSize(String filename){
        return round(new File(filename).length()/BYTE_MB,2);
    }
    public static float getFolderSize(String foldername) {
        File dir=new File(foldername);
        float size = 0f;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                System.out.println(file.getName() + " " + file.length());
                size += file.length();
            }
            else
                size += getFolderSize(file.getAbsolutePath());
        }
        return round(size/BYTE_MB,2);
    }
    public static String isEncryptedFolder(String filename){
        File dir=new File(filename);
        //if file is not a directory but just a file
        if(dir.isFile()){
            if(dir.getName().contains("pgp")){
                return "Encrypted";
            }else{
                return "Not encrypted";
            }
        }
        //if all the files in folder are encrypted than this variable will be zero
        if(dir.listFiles().length<1){
            return "Cannot see";
        }
        int temp=dir.listFiles().length;
        for (File f:
                dir.listFiles()) {
            if(f.getName().contains("pgp")){
                temp--;
            }else{
                temp++;
            }
        }
        if(temp==0){
            return "Encrypted";
        }else{
            return "Not Encrypted";
        }
    }
    public static String isEncryptedFile(String filename){
        if(filename.contains(".pgp")){
            return "Encrypted";
        }else{
            return "Not encrypted";
        }
    }
    public static int getNumberOfFiles(String  foldername){
        return new File(foldername).listFiles().length;
    }
    public static String getExtension(String fileName){
        final String emptyExtension = "file";
        if(fileName == null){
            return emptyExtension;
        }
        int index = fileName.lastIndexOf(".");
        if(index == -1){
            return emptyExtension;
        }
        return fileName.substring(index + 1);
    }

    /**
     * Round to certain number of decimals
     *
     * @param d
     * @param decimalPlace the numbers of decimals
     * @return
     */
    private static float round(float d, int decimalPlace) {
        return BigDecimal.valueOf(d).setScale(decimalPlace, BigDecimal.ROUND_HALF_UP).floatValue();
    }

}
