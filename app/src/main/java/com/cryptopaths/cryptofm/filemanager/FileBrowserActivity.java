package com.cryptopaths.cryptofm.filemanager;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cryptopaths.cryptofm.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class FileBrowserActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_activity);
		setResult(RESULT_OK);
		//fill list view
		fillList();
	}
	private void fillList(){
		ListView listView=(ListView)findViewById(R.id.fileListView);
		MyAdapter listAdapter=new MyAdapter(this);
		listAdapter.fillAdapter(Environment.getExternalStorageDirectory().getPath());
		listView.setAdapter(listAdapter);

	}

	private class MyAdapter extends BaseAdapter {
		private List<String>	 	mAdapter	=new ArrayList<>();
		private LayoutInflater 		mInflator;
		private	ViewHolder 			mViewHodler;

		public MyAdapter(Context context){
			mInflator=(LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
			mViewHodler=new ViewHolder();
		}
		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int i) {
			return true;
		}

		@Override
		public void registerDataSetObserver(DataSetObserver dataSetObserver) {

		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

		}

		@Override
		public int getCount() {
			return mAdapter.size();
		}

		@Override
		public Object getItem(int i) {
			return null;
		}

		@Override
		public long getItemId(int i) {
			return 0;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}
		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			if(view==null){
				view=mInflator.inflate(R.layout.filebrowse_lisrview,viewGroup,false);
			}
			mViewHodler.mTextView=(TextView)view.findViewById(R.id.list_textview);
			mViewHodler.mImageView=(ImageView)view.findViewById(R.id.list_imageview);
			mViewHodler.mTextView.setText(mAdapter.get(i));
			mViewHodler.mImageView.setImageDrawable(getDrawable(R.drawable.ic_folder_white_48dp));
			return view;
		}

		@Override
		public int getItemViewType(int i) {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}
		public void fillAdapter(String dirPath){
			//clear the adapter
			mAdapter.clear();
			File file=new File(dirPath);
			File[] files=file.listFiles();
			for (File f:
					files) {
				mAdapter.add(f.getName());
			}
		}
		class ViewHolder{
			public ImageView mImageView;
			public TextView mTextView;
		}

	}

}