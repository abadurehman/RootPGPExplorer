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

package com.osama.cryptofm.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.osama.cryptofm.CryptoFM;
import com.osama.cryptofm.encryption.DatabaseHandler;
import com.osama.cryptofm.encryption.DocumentFileEncryption;
import com.osama.cryptofm.encryption.EncryptionWrapper;
import com.osama.cryptofm.filemanager.listview.FileListAdapter;
import com.osama.cryptofm.filemanager.utils.SharedData;
import com.osama.cryptofm.filemanager.utils.UiUtils;
import com.osama.cryptofm.utils.FileDocumentUtils;
import com.osama.cryptofm.utils.FileUtils;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by tripleheader on 1/2/17.
 * file decryption task
 */

public class DecryptTask extends AsyncTask<Void,String,String> {
    private ArrayList<String>   mFilePaths;
    private FileListAdapter     mAdapter;
    private MyProgressDialog    mProgressDialog;
    private Context             mContext;
    private InputStream         mSecKey;
    private String              mDbPassword;
    private String              mUsername;
    private String              mFileName=null;
    private File                mPubKey;
    private char[]              mKeyPass;
    private String              rootPath;
    private ProgressDialog      singleModeDialog;
    private String              destFilename;

    private ArrayList<File>     mCreatedFiles=new ArrayList<>();
    private boolean             singleFileMode=false;

    private static final String TAG                        = DecryptTask.class.getName();
    private static final String DECRYPTION_SUCCESS_MESSAGE = "Decryption successful";

    public DecryptTask(Context context,FileListAdapter adapter,
                       ArrayList<String> filePaths,
                       String DbPass,String mUsername,String keypass){
        this.mContext           = context;
        this.mAdapter           = adapter;
        this.mFilePaths         = filePaths;
        this.mUsername          = mUsername;
        this.mKeyPass           = keypass.toCharArray();
        this.mDbPassword        = DbPass;
        this.mSecKey            = getSecretKey();
        this.mProgressDialog    = new MyProgressDialog(mContext,"Decrypting",this);
        this.mPubKey            = new File(mContext.getFilesDir(),"pub.asc");


    }
    public DecryptTask(Context context, FileListAdapter adapter,String DbPass,String mUsername,String filename,String keypass){
        this.mContext           = context;
        this.mAdapter           = adapter;
        this.mFileName          = filename;
        this.mDbPassword        = DbPass;
        this.mKeyPass           = keypass.toCharArray();
        this.mUsername          = mUsername;
        this.mSecKey            = getSecretKey();
        this.singleModeDialog   = new ProgressDialog(mContext);
        this.singleFileMode     = true;

    }
    @Override
    protected String doInBackground(Void... voids) {

        try{
            File root= new File(Environment.getExternalStorageDirectory(),"decrypted");
            if(!root.exists()){
                if(!root.mkdir()){
                    return "cannot decrypt file";
                }
            }
            rootPath=root.getPath();

            if(mFileName==null){
                //check if files are from external storage
                if(FileUtils.isDocumentFile(mFilePaths.get(0))){
                    //do the document files decryption
                    performDocumentFileDecryption();
                }else{
                    //do the normal files decryption
                    performNormalFormDecryption();
                }

            }else{
                File in= TasksFileUtils.getFile(mFileName);
                File out= TasksFileUtils.getFile(root.getPath() + "/" + in.getName().substring(0, in.getName().lastIndexOf('.')));
                destFilename=out.getAbsolutePath();
                mSecKey=getSecretKey();
                EncryptionWrapper.decryptFile(in,out,mPubKey,getSecretKey(),mKeyPass);
            }

        }catch (Exception ex){
            //let the activity know that password is incorrect and don't save it
            SharedData.KEY_PASSWORD=null;
            ex.printStackTrace();
            return ex.getMessage();
        }

        return DECRYPTION_SUCCESS_MESSAGE;
    }
    private ArrayList<String> tmp=new ArrayList<>();

    private ArrayList<String> getOnlyEncryptedFiles(ArrayList<String> mFilePaths) throws IOException {
        int size=mFilePaths.size();
        for (int i = 0; i < size; i++) {
            Log.d(TAG, "getOnlyEncryptedFiles: file path is: "+mFilePaths.get(i));
            File f=TasksFileUtils.getFile(mFilePaths.get(i));
            if(f.isDirectory()){
                File[] fs=f.listFiles();
                ArrayList<String> tmp1=new ArrayList<>();
                for (File fin:
                    fs ) {
                    tmp1.add(fin.getAbsolutePath());
                }
                getOnlyEncryptedFiles(tmp1);
            }
                if(FileUtils.isEncryptedFile(mFilePaths.get(i))){
                tmp.add(mFilePaths.get(i));
            }
        }
        if(tmp.size()<1){
            throw new IllegalArgumentException("No encrypted files found");
        }
        return tmp;
    }

    private void decryptFile(File f) throws Exception{
        Log.d(TAG, "decryptFile: task is running");
        if(!isCancelled()) {
            if (f.isDirectory()) {
                for (File tmp : f.listFiles()) {
                    decryptFile(tmp);
                }
            } else {
                File out = new File(rootPath+"/", f.getName().substring(0, f.getName().lastIndexOf('.')));
                if (out.exists()) {
                    if(!out.delete()){
                        throw new IOException("Error in deleting already present file");
                    }
                }

                publishProgress(f.getName(), "" +
                        ((FileUtils.getReadableSize((f.length())))));
                mCreatedFiles.add(out);
                if(!EncryptionWrapper.decryptFile(f, out, mPubKey, getSecretKey(), mKeyPass)){
                    if(out.delete()){
                        throw new Exception("Error in decrypting file");
                    }
                }
            }
        }

    }

    private InputStream getSecretKey() {
        SQLiteDatabase.loadLibs(mContext);
        DatabaseHandler handler=new DatabaseHandler(mContext,mDbPassword,true);
        try {
            mSecKey= new BufferedInputStream(new ByteArrayInputStream(handler.getSecretKeyFromDb(mUsername)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert mSecKey!=null;
        return mSecKey;
    }

    @Override
    protected void onPostExecute(String s) {
        if (singleFileMode){
            if( s.equals(DECRYPTION_SUCCESS_MESSAGE)) {
                singleModeDialog.dismiss();
                Log.d(TAG, "onPostExecute: destination filename is: " + destFilename);
                //open file
                String mimeType =
                        MimeTypeMap.getSingleton().
                                getMimeTypeFromExtension(
                                        FileUtils.getExtension(destFilename
                                        )
                                );

                Intent intent = new Intent();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    Uri uri = null;
                    try {
                        uri = FileProvider.getUriForFile(
                                mContext,
                                mContext.getApplicationContext().getPackageName() + ".provider",
                                TasksFileUtils.getFile(destFilename)
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    intent.setDataAndType(uri, mimeType);
                } else {
                    try {
                        intent.setDataAndType(Uri.fromFile(TasksFileUtils.getFile(destFilename)), mimeType);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                intent.setAction(Intent.ACTION_VIEW);
                Intent x = Intent.createChooser(intent, "Open with: ");
                mContext.startActivity(x);
            }else{
                singleModeDialog.dismiss();
                Toast.makeText(mContext,
                        s,
                        Toast.LENGTH_LONG)
                        .show();
            }
        } else{
            mProgressDialog.dismiss("Decryption completed");
            SharedData.CURRENT_RUNNING_OPERATIONS.clear();
            Toast.makeText(mContext,
                    s,
                    Toast.LENGTH_LONG)
                    .show();
            UiUtils.reloadData(mContext, mAdapter);
        }

    }

    @Override
    protected void onProgressUpdate(String... values) {
        if(singleFileMode){
            return;
        }
        mProgressDialog.setmProgressTextViewText(values[0]);
    }

    @Override
    protected void onPreExecute() {
        if(singleFileMode){
            singleModeDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            singleModeDialog.setTitle("Decrypting file");
            singleModeDialog.setMessage("Please wait while I finish decrypting file");
            singleModeDialog.setIndeterminate(true);
            singleModeDialog.show();
            return;
        }
        mProgressDialog.show();
    }

    @Override
    protected void onCancelled() {

        for (File f : mCreatedFiles) {
            if(SharedData.EXTERNAL_SDCARD_ROOT_PATH !=null &&
                    f.getAbsolutePath().contains(SharedData.EXTERNAL_SDCARD_ROOT_PATH)){
                //noinspection ConstantConditions
                FileDocumentUtils.getDocumentFile(f).delete();
            }else{
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
        }

        SharedData.CURRENT_RUNNING_OPERATIONS.clear();
        UiUtils.reloadData(
                mContext,
                mAdapter
        );

        Toast.makeText(
                mContext,
                "Operation canceled",
                Toast.LENGTH_SHORT
        ).show();

        mProgressDialog.dismiss("Canceled");
        super.onCancelled();

    }

    private void performDocumentFileDecryption() throws Exception{
        tmp.clear();
        mFilePaths=getOnlyEncryptedDocumentFiles(mFilePaths);
        for (String path:mFilePaths) {
            decryptDocumentFiles(FileDocumentUtils.getDocumentFile(new File(path)));
        }
    }

    private void decryptDocumentFiles(DocumentFile f) throws Exception{
        Log.d(TAG, "decryptDocumentFiles: Running decryption on document file");
        //first always check if task is canceled
        if(!isCancelled()){
            if(f.isDirectory()){
                for (DocumentFile tmpFile:f.listFiles()) {
                    decryptDocumentFiles(tmpFile);
                }
            }else{
                File out = new File(rootPath+"/", f.getName().substring(0, f.getName().lastIndexOf('.')));
                // add the file in created files. top remove the files later of user cancels the task
                mCreatedFiles.add(out.getAbsoluteFile());
                publishProgress(f.getName(), "" +
                        ((FileUtils.getReadableSize((f.length())))));

                DocumentFileEncryption.decryptFile(
                        CryptoFM.getContext().getContentResolver().openInputStream(f.getUri()),
                        getSecretKey(),
                        mKeyPass,
                        new BufferedOutputStream(new FileOutputStream(out))

                );

            }
        }
    }

    private ArrayList<String> getOnlyEncryptedDocumentFiles(ArrayList<String> files){
        int size=files.size();
        for (int i = 0; i < size; i++) {
            DocumentFile file=FileDocumentUtils.getDocumentFile(new File(files.get(i)));
            //check if file is directory
            assert file != null;
            if(file.isDirectory()){
                //get all the lists of files in directory
                ArrayList<String> tmp=new ArrayList<>();
                for (DocumentFile f: file.listFiles()) {
                    tmp.add(FileUtils.CURRENT_PATH+f.getName());
                }
                //recursively get files
                getOnlyEncryptedDocumentFiles(tmp);
            }
            if(FileUtils.isEncryptedFile(mFilePaths.get(i))){
                tmp.add(mFilePaths.get(i));
            }
        }
        //if there are no encrypted file
        if(tmp.size()<1){
            throw new IllegalArgumentException("No encrypted files found.");
        }
        return tmp;
    }

    private void performNormalFormDecryption() throws Exception{
        tmp.clear();
        //refactor list to hold only encrypted files
        mFilePaths=getOnlyEncryptedFiles(mFilePaths);
        for (String s : mFilePaths) {
            if(!isCancelled()) {
                Log.d(TAG, "doInBackground: +" + mFilePaths.size());
                File f = TasksFileUtils.getFile(s);
                decryptFile(f);
            }
        }
    }
}