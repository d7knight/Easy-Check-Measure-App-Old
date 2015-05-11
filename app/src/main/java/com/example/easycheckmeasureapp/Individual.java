package com.example.easycheckmeasureapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class Individual extends Activity {

	static Context appContext;
	String filename;


    public void create(View v){
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
        finish();
    }
 
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		appContext = this;
		setContentView(R.layout.viewerhtml);

		filename=MainActivity.filename;
		String file = "Error loading checkmeasure.";
		try {
			file = readFile(MainActivity.storageDir
					+ filename);
		} catch (IOException e1) {
		}
		WebView wv = (WebView) findViewById(R.id.viewer_html);
		wv.loadData(file, "text/html", "utf-8");
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

	public void send(View v) {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"Checkmeasure #" + filename);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				"Hello,\nPlease find checkmeasure #" + filename
						+ " attached to this email.\n\n"
						);
		emailIntent.putExtra(
				android.content.Intent.EXTRA_STREAM,
				Uri.fromFile(new File(MainActivity.storageDir
						+ filename)));
		startActivity(emailIntent);
	}

}
