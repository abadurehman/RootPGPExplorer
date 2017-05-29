/*
 * Copyright (c) 2017. slownet5
 *  This file is part of RootPGPExplorer also known as CryptoFM
 *
 *       RootPGPExplorer a is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       RootPGPExplorer is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU General Public License for more details.
 *
 *       You should have received a copy of the GNU General Public License
 *       along with RootPGPExplorer.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.slownet5.pgprootexplorer.filemanager.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.slownet5.pgprootexplorer.R;
import com.slownet5.pgprootexplorer.filemanager.listview.AdapterCallbacks;
import com.slownet5.pgprootexplorer.filemanager.listview.FileFillerWrapper;
import com.slownet5.pgprootexplorer.filemanager.listview.FileListAdapter;
import com.slownet5.pgprootexplorer.filemanager.listview.FileSelectionManagement;
import com.slownet5.pgprootexplorer.filemanager.utils.SharedData;
import com.slownet5.pgprootexplorer.filemanager.utils.UiUtils;
import com.slownet5.pgprootexplorer.services.CleanupService;
import com.slownet5.pgprootexplorer.utils.FileUtils;



public class FileBrowserActivity extends AppCompatActivity
		implements AdapterCallbacks {

	private String 						mCurrentPath;
	private String 						mRootPath;
    private FileListAdapter 			mmFileListAdapter;
	private RecyclerView 				mFileListView;
	private LinearLayoutManager			mFileViewLinearLayoutManager;
	private GridLayoutManager			mFileViewGridLayoutManager;
	private ItemTouchHelper				mHelper;
    private static final String 		TAG = "FileBrowser";
	private NoFilesFragment 			mNoFilesFragment;
	private boolean						mStartedInSelectionMode=false;
	private FileFillerWrapper 			mFileFillerWrapper;
	private FileSelectionManagement 	mFileSelectionManagement;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_activity);

		mCurrentPath 	           	= Environment.getExternalStorageDirectory().getPath()+"/";
		mRootPath	 	           	= mCurrentPath;
		mFileListView				= (RecyclerView) findViewById(R.id.fileListView);
		mmFileListAdapter 			= new FileListAdapter(this);
		mNoFilesFragment			= new NoFilesFragment();
		mFileFillerWrapper			= new FileFillerWrapper();
		mmFileListAdapter.setmFileFiller(mFileFillerWrapper);
		mFileSelectionManagement	= mmFileListAdapter.getmManager();

		//check if started in selection mode
		if(getIntent().getExtras().getBoolean("select",false)){
			SharedData.STARTED_IN_SELECTION_MODE=true;
			mStartedInSelectionMode=true;
			assert getSupportActionBar()!=null;
			getSupportActionBar().setTitle("Select Key files");
			//hide the floating button
		}else{
			setResult(RESULT_OK);
			SharedData.DB_PASSWORD = getIntent().getExtras().getString("dbpass");
			SharedData.USERNAME		    = getIntent().getExtras().getString("username","default");
		}


		//set layout manager for the recycler view according to user choice
		SharedPreferences preferences   = getPreferences(Context.MODE_PRIVATE);
		boolean linearLayout           	= preferences.getBoolean("key",false);
		if(linearLayout){
			mFileViewLinearLayoutManager=new LinearLayoutManager(this);
			mFileListView.setLayoutManager(mFileViewLinearLayoutManager);
		}else{
			mFileViewGridLayoutManager=new GridLayoutManager(this,2);
			mFileListView.setLayoutManager(mFileViewGridLayoutManager);
		}
		mFileListView.setAdapter(mmFileListAdapter);
		mFileFillerWrapper.fillData(mCurrentPath,mmFileListAdapter);

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(mStartedInSelectionMode){
			getMenuInflater().inflate(R.menu.selection_mode_menu,menu);
			return true;
		}
		getMenuInflater().inflate(R.menu.appbar_menu,menu);
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(mStartedInSelectionMode){
			if(item.getItemId()==R.id.check_menu_item){
				if(mFileSelectionManagement.getmSelectedFilePaths().size()<1){
					Toast.makeText(
							this,
							"Please choose your key file",
							Toast.LENGTH_LONG
					).show();
					return true;
				}
				Intent intent=new Intent();
				intent.putExtra("filename",mFileSelectionManagement.getmSelectedFilePaths().get(0));
				setResult(RESULT_OK,intent);
				finish();
			}
		}
	return true;
	}

	void changeDirectory(String path) {
		mCurrentPath=path;
		changeTitle(path);
		mFileFillerWrapper.fillData(path,mmFileListAdapter);
		if(mFileFillerWrapper.getTotalFilesCount()<1){
			showNoFilesFragment();
			return;
		}else if(emptyFiles){
			removeNoFilesFragment();
		}
		mmFileListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onBackPressed() {
		if(emptyFiles) {
			removeNoFilesFragment();
		}
		if(mCurrentPath.equals(mRootPath)){
			super.onBackPressed();
		}else{
			//modify the mCurrentPath
			mCurrentPath		   = mCurrentPath.substring(0,mCurrentPath.lastIndexOf('/'));
			mCurrentPath 		   = mCurrentPath.substring(0,mCurrentPath.lastIndexOf('/')+1);
			changeDirectory(mCurrentPath);

		}
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume: resuming activity");
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause: pausing activity");
		super.onPause();
	}


	@Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

	@Override
	protected void onStart() {
		Log.d(TAG,"Starting activity");
		actionMode=null;
		super.onStart();
	}

	ActionMode actionMode;
	@Override
	public void onLongClick() {
		if(SharedData.SELECTION_MODE) {
			//actionMode = startActionMode(new ActionViewHandler(this));
		}
		//MainUtils.actionMode=actionMode;
	}
	@Override
	public void incrementSelectionCount(){
		actionMode.setTitle(++SharedData.SELECT_COUNT+"");
		if(SharedData.SELECT_COUNT>1){
			actionMode.getMenu().removeItem(R.id.rename_menu_item);
		}
	}

	@Override
	public void decrementSelectionCount() {
		if(actionMode!=null){
			actionMode.setTitle(--SharedData.SELECT_COUNT+"");
			if(SharedData.SELECT_COUNT==0){
				actionMode.finish();
			}
			else if(SharedData.SELECT_COUNT<2){
				actionMode.getMenu().add(0,R.id.rename_menu_item,0,"rename");
			}
		}
	}

	@Override
	public void changeTitle(String path) {
		if(path.equals(mRootPath)){
			path="Home";
		}else{
			path=path.substring(0,path.lastIndexOf('/'));
			path=path.substring(path.lastIndexOf('/')+1);
		}
		assert getSupportActionBar()!=null;
		getSupportActionBar().setTitle(path);
	}

	boolean emptyFiles;
	public void showNoFilesFragment() {
		Log.d(TAG, "showNoFilesFragment: Adding no files layput");
		emptyFiles=true;
		mNoFilesFragment=new NoFilesFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.no_files_frame,mNoFilesFragment).commit();
	}
	public void removeNoFilesFragment(){
		Log.d(TAG, "removeNoFilesFragment: removing no files layout");
		emptyFiles=false;
		getSupportFragmentManager().beginTransaction().remove(mNoFilesFragment).commit();
	}

	@Override
	public void animateForward(String path) {
		changeDirectory(path);
	}

	@Override
	public void selectedFileType(boolean isFolder) {

	}

	/**
	 * end of task executing section
	 */

}