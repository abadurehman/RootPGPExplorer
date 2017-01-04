package com.cryptopaths.cryptofm.filemanager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cryptopaths.cryptofm.R;
import com.cryptopaths.cryptofm.utils.FileUtils;

import java.util.ArrayList;


/**
 * Created by tripleheader on 12/14/16.
 *
 *
 */

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder>{

    private Context             mContext;
    private FileFillerWrapper   mFile;
    private Drawable            mSelectedFileIcon;
    private Drawable            mFileIcon;
    private Drawable            mFolderIcon;
    private DataModelFiles      mDataModel;

    private Boolean                 mSelectionMode      = false;
    private ArrayList<Integer>      mSelectedPosition   = new ArrayList<>();
    private ArrayList<String>       mSelectedFilePaths  = new ArrayList<>();
    private  static final String    TAG                 = "filesList";
    private static final int        NO_FILES_VIEW       = 100;
    private static final int        NORMAL_VIEW         = 50;


        public FileListAdapter(Context context){
            this.mContext     = context;
            mSelectedFileIcon = mContext.getDrawable(R.drawable.ic_check_circle_white_48dp);
            mFileIcon         = mContext.getDrawable(R.drawable.ic_insert_drive_file_white_48dp);
            mFolderIcon       = mContext.getDrawable(R.drawable.ic_folder_white_48dp);

    }

    public void setmFile(FileFillerWrapper mFile) {
        this.mFile = mFile;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==NO_FILES_VIEW){
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.no_files_layout,parent,false));
        }else{
            Context context=parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View view               = inflater.inflate(R.layout.filebrowse_lisrview,parent,false);
            return new ViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if(mFile.getTotalFilesCount()<1){
            return NO_FILES_VIEW;
        }else{
            return NORMAL_VIEW;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(mFile.getTotalFilesCount()<1){
            ImageView view=holder.noFilesLayout;
            view.setImageDrawable(mContext.getDrawable(R.drawable.nofiles_image));
        }else {
            mDataModel=mFile.getFileAtPosition(position);
            TextView textView1=holder.mTextView;
            ImageView imageView=holder.mImageView;
            TextView textView2=holder.mFolderSizeTextView;
            TextView textView3=holder.mEncryptionStatusTextView;
            TextView textView4=holder.mNumberFilesTextView;

            textView1.setText(mDataModel.getFileName());
            textView2.setText(mDataModel.getFileSize());
            textView3.setText(mDataModel.getFileEncryptionStatus());
            textView4.setText(mDataModel.getFileExtension());
            imageView.setImageDrawable(mDataModel.getFileIcon());
        }

    }

    @Override
        public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemCount() {
        if(mFile.getTotalFilesCount()<1){
            return 1;
        }
        return mFile.getTotalFilesCount();
    }
    private LongClickCallBack clickCallBack;

    void selectAllFiles() {
        for (int i = 0; i < mFile.getTotalFilesCount(); i++) {
            mDataModel = mFile.getFileAtPosition(i);
            selectFile(i);
        }
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView   mImageView;
        ImageView   noFilesLayout;
        TextView    mTextView;
        TextView    mNumberFilesTextView;
        TextView    mFolderSizeTextView;
        TextView    mEncryptionStatusTextView;
        ViewHolder(View itemView){
                super(itemView);
            clickCallBack = (LongClickCallBack)mContext;

            itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mFile.getTotalFilesCount()<1){
                            return;
                        }
                        if(mSelectionMode){
                            selectionOperation(getAdapterPosition());
                            return;
                        }
                        TextView textView   = (TextView)view.findViewById(R.id.list_textview);
                        String filename     = textView.getText().toString();
                        if(FileUtils.isFile(filename)){
                            //open file TODO
                            Toast.makeText(
                                    mContext,
                                    "You click at file: "+filename,
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }else{
                            String folderPath = mFile.getCurrentPath()+filename+"/";
                            //check if folder already visited
                            if(FileBrowserActivity.mFilesData.containsKey(folderPath)){
                                mFile = FileBrowserActivity.mFilesData.get(folderPath);
                                FileUtils.CURRENT_PATH=folderPath;
                                notifyDataSetChanged();
                            }else{
                                //first visit folder
                                mFile = new FileFillerWrapper(folderPath,mContext);
                                FileBrowserActivity.mFilesData.put(folderPath,mFile);
                                notifyDataSetChanged();

                            }
                        }
                    }
                });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(mFile.getTotalFilesCount()<1){
                        return false;
                    }
                    clickCallBack.onLongClick();
                    mSelectionMode = true;
                    selectionOperation(getAdapterPosition());
                    return true;
                }
            });

            mTextView                   = (TextView)itemView.findViewById(R.id.list_textview);
            mImageView                  = (ImageView)itemView.findViewById(R.id.list_imageview);
            noFilesLayout               = (ImageView) itemView.findViewById(R.id.no_files_image);
            mNumberFilesTextView        = (TextView)itemView.findViewById(R.id.nofiles_textview);
            mFolderSizeTextView         = (TextView)itemView.findViewById(R.id.folder_size_textview);
            mEncryptionStatusTextView   = (TextView)itemView.findViewById(R.id.encryption_status_textview);

        }

    }

    private void selectionOperation(int position){
        mDataModel  = mFile.getFileAtPosition(position);

        if(mDataModel.getSelected()){
            mDataModel.setSelected(false);
            clickCallBack.decrementSelectionCount();
            if(mDataModel.getFile()){
                mDataModel.setFileIcon(mFileIcon);
            }else{
                mDataModel.setFileIcon(mFolderIcon);
            }
        }else{
            selectFile(position);
        }

        notifyItemChanged(position);

    }
    private void selectFile(int position){
        if(!mDataModel.getSelected()) {
            clickCallBack.incrementSelectionCount();
            mSelectedPosition.add(position);
            mSelectedFilePaths.add(mDataModel.getFilePath());
            mDataModel.setFileIcon(mSelectedFileIcon);
            mDataModel.setSelected(true);
        }
    }

    interface LongClickCallBack{
        void onLongClick();
        void incrementSelectionCount();
        void decrementSelectionCount();
    }

    void setmSelectionMode(Boolean value){
        //first check if there are select files
        if(mSelectedPosition.size()>0) {
            for (Integer pos : mSelectedPosition) {
                mDataModel =  mFile.getFileAtPosition(pos);
                mDataModel.setSelected(false);
            }
            mSelectedPosition.clear();
            mSelectedFilePaths.clear();
        }
        this.mSelectionMode=value;
    }

    void resetFileIcons(){
        for (Integer pos:
             mSelectedPosition) {
            mDataModel = mFile.getFileAtPosition(pos);
            if(mDataModel.getFile()){
                mDataModel.setFileIcon(mFileIcon);
            }else{
                mDataModel.setFileIcon(mFolderIcon);
            }
            notifyItemChanged(pos);

        }
    }

    ArrayList<String> getmSelectedFilePaths() {
        return mSelectedFilePaths;
    }
    public FileFillerWrapper getmFile(){
        return this.mFile;
    }
}
