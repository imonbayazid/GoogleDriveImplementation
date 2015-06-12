

package com.example.googledriveimplementation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*downloads a text file's contents, reads it and displays
 the contents in a new activity*/
public class DownloadFileFromGoogleDrive extends Activity implements
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {
	/* get this id from your google drive on the web */
	
	public static  String EXISTING_FILE_ID ="";
	public static  String EXISTING_FILE_NAME ="";

	private static final int REQUEST_CODE = 102;
	private GoogleApiClient googleApiClient;
	private static final String TAG = "retrieve_contents";
	
	 private ProgressDialog progressDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/* build the api client */
		
		Bundle extras = getIntent().getExtras();
		progressDialog = ProgressDialog.show(DownloadFileFromGoogleDrive.this, "Please wait ...", "Downloading file ...", true);

	    if (extras.getString("fileId") != null ) 
	    {
	    	EXISTING_FILE_ID=extras.getString("fileId");
	    	EXISTING_FILE_NAME=extras.getString("fileName");
	    }
		
	   Toast.makeText(getApplicationContext(), "ID:"+EXISTING_FILE_ID+" name:"+EXISTING_FILE_NAME, Toast.LENGTH_LONG).show();
		buildGoogleApiClient();
	}

	/* connect client to Google Play Services */
	
	
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG, "In onStart() -connecting...");
		googleApiClient.connect();
	}

	/* close connection to Google Play Services */
	@Override
	protected void onStop() {
		super.onStop();
		if (googleApiClient != null) {
			Log.i(TAG, "In onStop() -disConnecting...");
			googleApiClient.disconnect();
		}
	}

	/* handles onConnectionFailed callbacks */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			Log.i(TAG, "In onActivityResult() -connecting...");
			googleApiClient.connect();
		}
	}

	/* handles connection callbacks */
	@Override
	public void onConnected(Bundle bundle) {
		Drive.DriveApi.fetchDriveId(googleApiClient, EXISTING_FILE_ID)
				.setResultCallback(idCallback);
	}

	/* handles suspended connection callbacks */
	@Override
	public void onConnectionSuspended(int i) {
		Drive.DriveApi.fetchDriveId(googleApiClient, EXISTING_FILE_ID)
				.setResultCallback(idCallback);
	}

	/* callback on getting the drive id, contained in result */
	final private ResultCallback<DriveIdResult> idCallback = new ResultCallback<DriveIdResult>() {
		@Override
		public void onResult(DriveIdResult result) {
			DriveFile file = Drive.DriveApi.getFile(googleApiClient,
					result.getDriveId());
			/* use a pending result to get the file contents */
			PendingResult<DriveContentsResult> pendingResult = file.open(
					googleApiClient, DriveFile.MODE_READ_ONLY, null);
			/* the callback receives the contents in the result */
			pendingResult
					.setResultCallback(new ResultCallback<DriveContentsResult>() {

						@Override
						public void onResult(DriveContentsResult result) {
							
							// change the directory where u want to keep the downloaded file
							File rootDirectory=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"MYGOOGLEDRIVE");
							if(!rootDirectory.exists())
							{
								rootDirectory.mkdirs();
							}
							File file=new File(rootDirectory,EXISTING_FILE_NAME);
							try {
								file.createNewFile();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
							DriveContents fileContents = result
									.getDriveContents();
							
							InputStream inputstream=fileContents.getInputStream();
							OutputStream outputstream=null;
							try {
								outputstream=new FileOutputStream(file);
							} catch (FileNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						
						
						byte[] buffer=new byte[1024];
						int bytecount=0;
						 
						
							try {
								while((bytecount=inputstream.read(buffer))>0)
								{
									outputstream.write(buffer, 0, bytecount);
								  
								}
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						
							 try {
								outputstream.close();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							 			
							fileContents.discard(googleApiClient);
							
							progressDialog.cancel();
							finish();

						}
					});
		}
	};

	/*
	 * callback when there there's an err or connecting the client to the
	 * service.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "Connection failed");
		if (!result.hasResolution()) {
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
					0).show();
			return;
		}
		try {
			Log.i(TAG, "trying to resolve the Connection failed error...");
			result.startResolutionForResult(this, REQUEST_CODE);
		} catch (IntentSender.SendIntentException e) {
			Log.e(TAG, "Exception while starting resolution activity", e);
		}
	}

	/* build the google api client */
	private void buildGoogleApiClient() {
		if (googleApiClient == null) {
			googleApiClient = new GoogleApiClient.Builder(this)
					.addApi(Drive.API).addScope(Drive.SCOPE_FILE)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this).build();
		}
	}
}
