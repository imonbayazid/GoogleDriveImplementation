package com.example.googledriveimplementation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.Metadata;

import com.google.android.gms.drive.OpenFileActivityBuilder;

/**
 * upload a text file from device to Drive
 */
public class GetFileFromGoogleDrive extends Activity implements
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {
	
	
	private static final String TAG = "drive";
    private static final int REQUEST_CODE_SELECT = 102;
    private static final int REQUEST_CODE_RESOLUTION = 103;
    private GoogleApiClient googleApiClient;
    
    // for downloading process
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    public ProgressDialog mProgressDialog;
  
    public ProgressDialog progressDialog;
    
 
  

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_file);
//      you can check if play services installed and up to date - returns integer value
      Log.i(TAG, "Is Google Play Services available and up to date? "
              + GooglePlayServicesUtil.isGooglePlayServicesAvailable(this));
    
   // first show a dialog to wait
	  progressDialog=ProgressDialog.show(GetFileFromGoogleDrive.this, "Loading","Your Google drive is opening please wait", true);   	
      
      buildGoogleApiClient();


    }

   
    /*connect client to Google Play Services*/
    @Override
    protected void onStart() {   	
        super.onStart();
        Log.i(TAG, "In onStart() - connecting...");
        googleApiClient.connect();
    }

    /*close connection to Google Play Services*/
    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            Log.i(TAG, "In onStop() - disConnecting...");
            googleApiClient.disconnect();
        }
    }

    /*Connection callback - on successful connection*/
    @Override
    public void onConnected(Bundle bundle) {
    	// first close the waiting dialog
        progressDialog.cancel();
    	
        Log.i(TAG, "in onConnected() - we're connected, let's do the work in the background...");
//        build an intent that we'll use to start the open file activity
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
//                these mimetypes enable these folders/files types to be selected
                .setMimeType(new String[] { DriveFolder.MIME_TYPE, "text/plain", "image/png","application/pdf","image/jpeg","audio/mp3"})//"text/plain", "image/png","application/pdf","image/jpeg","audio/*"
                .build(googleApiClient);
        try {
            startIntentSenderForResult(
                    intentSender, REQUEST_CODE_SELECT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.i(TAG, "Unable to send intent", e);
        }
    }

    /*Connection callback - Called when the client is temporarily in a disconnected state*/
    @Override
    public void onConnectionSuspended(int i) {
        switch (i) {
            case 1:
                Log.i(TAG, "Connection suspended - Cause: " + "Service disconnected");
                break;
            case 2:
                Log.i(TAG, "Connection suspended - Cause: " + "Connection lost");
                break;
            default:
                Log.i(TAG, "Connection suspended - Cause: " + "Unknown");
                break;
        }
    }

    /*connection failed callback - Called when there was an error connecting the client to the service*/
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed - result: " + result.toString());
        if (!result.hasResolution()) {
//            display error dialog
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }

        try {
            Log.i(TAG, "trying to resolve the Connection failed error...");
//            tries to resolve the connection failure by trying to restart this activity
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.i(TAG, "Exception while starting resolution activity", e);
        }
    }

    /*build the google api client*/
    private void buildGoogleApiClient() {
        Log.i(TAG, "Building the client");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

   /* receives returned result - called by the open file activity when it's exited
   by user pressing Select. This passes the request code, result code and data back
   which is received here*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "in onActivityResult() - triggered on pressing Select");
        switch (requestCode) {
            case REQUEST_CODE_SELECT:
                if (resultCode == RESULT_OK) {
                    /*get the selected item's ID*/
                    DriveId driveId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);//this extra contains the drive id of the selected file
                    Log.i(TAG, "Selected folder's ID: " + driveId.encodeToString());
                    Log.i(TAG, "Selected folder's Resource ID: " + driveId.getResourceId());

//                    selected file (can also be a folder)
                    DriveFile selectedFile = Drive.DriveApi.getFile(googleApiClient, driveId);
                    PendingResult<MetadataResult> selectedFileMetadata = selectedFile.getMetadata(googleApiClient);

//                    fetch the selected item's metadata asynchronously using a pending result
                   
                    
                    selectedFileMetadata.setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
						
						@SuppressLint("NewApi") @Override
						public void onResult(MetadataResult result) {
							// TODO Auto-generated method stub
							 Metadata fileMetadata = result.getMetadata();
							 
							 
							 Log.d(TAG, "File title: " + fileMetadata.getTitle());
	                         Log.d(TAG, "File size: " + fileMetadata.getFileSize());
	                         Log.d(TAG, "File mime type: " + fileMetadata.getMimeType());
	                         Log.d(TAG, "TAG: " + fileMetadata.getTitle());
	                         
	                         System.out.print("l:"+fileMetadata.getWebContentLink());
	                         
	                      
	                        // Toast.makeText(getApplicationContext(),"Link: "+fileMetadata.getWebContentLink() , Toast.LENGTH_LONG).show();
	                         Log.d("link:"," link:  "+fileMetadata.getWebContentLink());
	                         Log.d("link:","type: "+fileMetadata.getMimeType());
	                         Log.d("link:","name:  "+fileMetadata.getOriginalFilename());
	                      
	                         String mimTypeOfFile=fileMetadata.getMimeType();
	                         int startingIndexForGettingFileType=mimTypeOfFile.indexOf("/");
	                         String extentionOfTheSelectedFile=mimTypeOfFile.substring(startingIndexForGettingFileType+1, mimTypeOfFile.length());
	                         
	                         Log.d("link:","extention:  "+extentionOfTheSelectedFile);
	                         
	                         String totalLink=fileMetadata.getWebContentLink();
	                         
	                         int totalLinkSize=fileMetadata.getWebContentLink().length();	                         
	                         //Toast.makeText(getApplicationContext(),"total link size : "+totalLinkSize , Toast.LENGTH_LONG).show();
	                      int startingIndex=totalLink.indexOf("id=")+3;
	                      
	                         
	                         String mySelectedFiledriveId=totalLink.substring(startingIndex, totalLinkSize-16);

	                         Toast.makeText(getApplicationContext(),"id: "+mySelectedFiledriveId , Toast.LENGTH_LONG).show();
	                        
	                         Log.d("link:","id:  "+mySelectedFiledriveId);
	                         
	                        Intent i=new Intent(GetFileFromGoogleDrive.this,DownloadFileFromGoogleDrive.class);
	                        i.putExtra("fileId", mySelectedFiledriveId);
	                        i.putExtra("fileName", fileMetadata.getOriginalFilename());
	                       // i.putExtra("fileExtention", extentionOfTheSelectedFile);
	                        startActivity(i);
	                        finish();
	                       
	                     
						}
					});
                    
                }
             
                
                break;
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "in onActivityResult() - resolving connection, connecting...");
                    googleApiClient.connect();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

	
	
	
	
	
	
	
	

	 
	 
	    
	    
}