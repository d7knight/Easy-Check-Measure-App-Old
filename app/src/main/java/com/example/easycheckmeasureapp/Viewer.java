package com.example.easycheckmeasureapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class Viewer extends ListActivity {

	private CMAdapter adapter;
	private static CM[] items;
	private static File[] files;
	static Context appContext;
	


    public void create(View v){
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
        finish();
    }
 


	private static String readFile(String path) throws IOException {
		  FileInputStream stream = new FileInputStream(new File(path));
		  try {
		    FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    return Charset.defaultCharset().decode(bb).toString();
		  }
		  finally {
		    stream.close();
		  }
		}
	@Override
	public void onPause() {
		super.onPause();
	}

	
	public static void send(File f) {

		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

		emailIntent.setType("plain/text");

		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "CheckMeasure");
		emailIntent
				.putExtra(
						Intent.EXTRA_TEXT,
						"Hello,\n"
								+ "Your checkmeasure from Draper Knight is attached to this email.\n\nThanks,\nAllan");
		emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));

		final PackageManager pm = appContext.getPackageManager();
		final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent,
				0);
		ResolveInfo best = null;
		for (final ResolveInfo info : matches) {
			if (info.activityInfo.packageName.endsWith(".gm")
					|| info.activityInfo.name.toLowerCase().contains("gmail")) {
				best = info;
			}
		}
		if (best != null) {
			emailIntent.setClassName(best.activityInfo.packageName,
					best.activityInfo.name);
		}
		appContext.startActivity(emailIntent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appContext = this;
		items = loadcms();
		
		this.adapter = new CMAdapter(this, android.R.layout.simple_list_item_1, items);
		setListAdapter(this.adapter);
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0,
					final View arg1, final int arg2, long arg3) {
			
			final Dialog dialog = new Dialog(appContext);
			dialog.setContentView(R.layout.dialogue);
			dialog.setTitle("What would you like to do?");
			
 
			// set the custom dialog components - text, image and button
	
			Button sendbtn = (Button) dialog.findViewById(R.id.send);
			sendbtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					send(files[files.length-arg2-1]);
				}
			});
			
			Button edit = (Button) dialog.findViewById(R.id.edit);
			edit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i=new Intent(appContext, MainActivity.class);
                	i.putExtra("editcm", files[files.length-arg2-1].getAbsolutePath());
                	appContext.startActivity(i);
					dialog.dismiss();
				}
			});
			Button delete = (Button) dialog.findViewById(R.id.delete);
			delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int i=items.length-arg2-1;
					File temp=files[i];
					
					
					remove(items,arg2);
			
				    CMAdapter nadapter = new CMAdapter(appContext, android.R.layout.simple_list_item_1, items);
			        setListAdapter(nadapter);
					getListView().refreshDrawableState();
				    getListView().invalidate();
				    remove(files, i);
					temp.delete();
					dialog.dismiss();
					
					
				}
			});
			Button back = (Button) dialog.findViewById(R.id.back);
			back.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			dialog.show();
			return false;
		  }
		});
		
	}
private void remove(File[] f,int i){
	File[] newf= new File[f.length-1];
	int k=0;
    for(int j=0; j<newf.length; j++){
    	
        if (k==i)k++;
    	newf[j]=f[k];
    	
    	
      k++;
    }
     files=newf;
}
private void remove(CM[] f,int i){
	CM[] newf= new CM[f.length-1];
	int k=0;
    for(int j=0; j<newf.length; j++){
    	
        if (k==i)k++;
    	newf[j]=f[k];
    	
    	
      k++;
    }
	
     items= newf;
}

	public CM[] loadcms() {
		File folder=new File(MainActivity.storageDir);
		files=folder.listFiles();
		if (files != null) {
		   List<File> directoryListing = new ArrayList<File>();
		   directoryListing.addAll(Arrays.asList(files));
		   Collections.sort(directoryListing, new sortfilename());
		   directoryListing.toArray(files);
		}
		
		CM[] r = new CM[files.length];
		for (int nC = 0; nC < files.length; nC++) {
			r[nC] = new CM(files[files.length-nC-1]);
		}
		return r;
	}

	private class CM {
		View v;

		CM(File f) {
			WebView wv = new WebView(appContext);
			
			String file = "Error loading checkmeasure.";
			try {
				file = readFile(f.getAbsolutePath());
				
			} catch (IOException e1) {
			}
			wv.loadData(file, "text/html", "utf-8");
			v=wv;
		}

	}

	private class CMAdapter extends ArrayAdapter<CM> {

		private CM[] items;

		public CMAdapter(Context context, int textViewResourceId,
				CM[] items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return items[position].v;
		}

	}
}


