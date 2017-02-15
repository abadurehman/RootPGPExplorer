/*
 * Copyright (c) 2017. Osama Bin Omar
 *    This file is part of Crypto File Manager also known as Crypto FM
 *
 *     Crypto File Manager is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Crypto File Manager is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Crypto File Manager.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.osama.cryptofm.filemanager.listview;


import android.content.Context;
import android.graphics.drawable.Drawable;

import com.osama.cryptofm.R;
import com.osama.cryptofm.filemanager.utils.MimeType;
import com.osama.cryptofm.utils.FileUtils;

/**
 * Created by tripleheader on 12/17/16.
 * Data model for the recyclerview
 */

public class DataModelFiles  {
    private String      fileName;
    private String      fileExtensionOrItems;
    private String      fileSize;
    private Drawable    fileIcon;

    private Boolean     isSelected   = false;
    private Boolean     isFile       = false;
    private boolean     isEncrypted  = false;
    private Context     mContext;

    private static MimeType mIconManager;
    public DataModelFiles(String filename, Context context) {
        if(mIconManager==null){
            mIconManager=new MimeType(context);
        }
        this.fileName = filename;
        this.mContext=context;
        //check if i can read file
        if(FileUtils.isFile(filename)){
            this.fileIcon=context.getDrawable(R.drawable.ic_insert_drive_file_white_48dp);
            this.fileExtensionOrItems=FileUtils.getExtension(filename);
            this.fileIcon=MimeType.getIcon(fileExtensionOrItems);
            long size=FileUtils.getFileSize(filename);
            this.fileSize=FileUtils.getReadableSize(size);
            this.isEncrypted=FileUtils.isEncryptedFile(filename);
            this.isFile=true;
        }else{
            this.fileIcon=context.getDrawable(R.drawable.ic_default_folder);
            //in case of folder file extension will be number of items in folder
            this.fileExtensionOrItems=FileUtils.getNumberOfFiles(filename)+" items";
            this.isEncrypted=FileUtils.isEncryptedFolder(filename);
            this.fileSize=FileUtils.getLastModifiedDate(filename);
        }
    }


    public String getFileName() {
        return fileName;
    }

    public Drawable getFileEncryptionStatus() {
        if(isEncrypted){
            return mContext.getDrawable(R.drawable.ic_encrypt);
        }else{
            return mContext.getDrawable(R.drawable.ic_decrypt);
        }
    }

    public String getFileExtension() {
        return fileExtensionOrItems;
    }

    public String getFileSize() {
        return fileSize;
    }
    public Drawable getFileIcon(){
        return this.fileIcon;
    }
    public void setFileIcon(Drawable drawable){
        this.fileIcon=drawable;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }
    public String getFilePath(){
        return FileUtils.CURRENT_PATH+this.fileName;
    }

    public Boolean getFile() {
        return isFile;
    }
}