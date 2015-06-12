package com.example.googledriveimplementation;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends ActionBarActivity {

	Button upload, download;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		upload = (Button) findViewById(R.id.upload_button);
		download = (Button) findViewById(R.id.download_button);

		download.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,
						GetFileFromGoogleDrive.class);
				startActivity(intent);
			}
		});

		upload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,
						UploadFilesGoogleDrive.class);
				startActivity(intent);
			}
		});

	}

}
