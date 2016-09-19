package com.hsstoyanova.barkwise;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.hsstoyanova.barkwise.common.CurrentData;
import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.databaseservice.PhpFiles;
import com.hsstoyanova.barkwise.databaseservice.Request;
import com.loopj.android.http.RequestParams;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraActivity extends AppCompatActivity {

	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final int REQUEST_TAKE_PHOTO = 2;
	private ImageView imageView;
	private ImageView ivUpload;
	String mCurrentPhotoPath;
	private int ACTION_TAKE_PHOTO_B = 1;
	ProgressDialog pDialog;
	private Bitmap bitmap;

	
	@Override
	   protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.activity_camera);
	      
	      imageView = (ImageView)findViewById(R.id.imageView);
	      ivUpload = (ImageView) findViewById(R.id.ivUpload);
	      ivUpload.setOnClickListener(onClickUpload);
	      
	      dispatchTakePictureIntent();
	   }
	
	protected void onDestroy() 
	{
		bitmap = null;
		System.gc();
		super.onDestroy();
	};
	
	OnClickListener onClickUpload = new OnClickListener(){
		// TODO Auto-generated method stub
		
		public void onClick(View v) 
		{
			if(bitmap != null)
			{
				if(isNetworkAvailable())
				{
					new UploadImage().execute(bitmap);	
				}
				else
				{
					debugMsg(Utils.ErrorMessage.internetConnection);
				}
			}
			else
			{
				debugMsg(Utils.ErrorMessage.noImageWasLoaded);
			}
		}
	};

	private class UploadImage extends AsyncTask<Bitmap, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(CameraActivity.this);
			pDialog.setMessage("Uploading image..");
		
			pDialog.show();
		}
		
		protected String doInBackground(Bitmap... prms) 
		{
			String imageStr = getStringImage(bitmap);
			imageStr=imageStr.trim();
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    Date now = new Date();
		    String date = sdfDate.format(now);
			
			RequestParams params = new RequestParams();
			params.put("userId", Integer.toString(CurrentData.user.id));
			params.put("image", imageStr);
			params.put("date", date);
			
			Request request = new Request(PhpFiles.uploadImage, Utils.RequestActions.POST.toString(), params);
			
			JSONObject json = request.response;
			
			if(json != null)
			{
				try 
				{
					int success = json.getInt(Utils.REQUEST_RESULT);
					
					if (success == 1) 
					{
						debugMsg(Utils.Message.imageUploaded);
						Intent i = new Intent(getApplicationContext(), GalleryActivity.class);
						startActivity(i);
						finish();
					} 
					else 
					{
						debugMsg(Utils.ErrorMessage.somethingWentWrong);
					}
				} 
				catch (JSONException e) 
				{
					e.printStackTrace();
				}
			}
			else
			{
				debugMsg(Utils.ErrorMessage.somethingWentWrong);
			}
			
			return null;
		}
		protected void onPostExecute(String file_url) {
			// dismiss the dialog once done
			pDialog.dismiss();
		}
	}
	
	
	 public String getStringImage(Bitmap bmp){
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
	        byte[] imageBytes = baos.toByteArray();
	        String encodedImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
	        return encodedImage;
	    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == ACTION_TAKE_PHOTO_B)
		{
			if (resultCode == RESULT_OK) 
			{
				handleBigCameraPhoto();
			}
		}		
	}
	
	private void handleBigCameraPhoto() {

		if (mCurrentPhotoPath != null) {
			setPic();
			galleryAddPic();
			mCurrentPhotoPath = null;
		}

	}
		
	private void galleryAddPic() {
	    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    getApplicationContext().sendBroadcast(mediaScanIntent);
}
	
	private void setPic() {

		int targetW = imageView.getWidth();
		int targetH = imageView.getHeight();

		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
		}

		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		Bitmap bmp = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		
		bitmap = bmp;
		imageView.setImageBitmap(bmp);
		imageView.setVisibility(View.VISIBLE);
	}
	
	private void dispatchTakePictureIntent() 
	{
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File f = null;
			
			try {
				f = setUpPhotoFile();
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			} catch (IOException e) {
				e.printStackTrace();
				f = null;
				mCurrentPhotoPath = null;
			}

		startActivityForResult(takePictureIntent, 1);
	}
	
    private File setUpPhotoFile() throws IOException {
		
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		debugMsg("mCurrentPhotoPath " + mCurrentPhotoPath);
		
		return f;
	}
	
	private File createImageFile() throws IOException
	{
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = "JPEG_" + timeStamp + "_";
	    
	    File storageDir = Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES);
	    
	    if(storageDir != null)
	    {

		    File image = File.createTempFile(
		        imageFileName,  // prefix 
		        ".jpg",         //suffix 
		        storageDir      // directory 
		    );

		    mCurrentPhotoPath = "file:" + image.getAbsolutePath();
		    return image;
	    }
	    else
	    {
	    	debugMsg(Utils.ErrorMessage.couldNotCreateFileForImage);
	    	return null;
	    } 
	}
	
	@Override
	public void onBackPressed() {
		Intent i = new Intent(getApplicationContext(), GalleryActivity.class);
		startActivity(i);
		finish();
	}
	
	public void debugMsg(String msg) {
		final String str = msg;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG)
						.show();
			}
		});
	}
	
	 private boolean isNetworkAvailable() {
		    ConnectivityManager connectivityManager 
		          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
		}
}
