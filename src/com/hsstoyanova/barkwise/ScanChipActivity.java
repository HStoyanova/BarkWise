package com.hsstoyanova.barkwise;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.hsstoyanova.barkwise.common.Utils;
import com.hsstoyanova.barkwise.databaseservice.PhpFiles;
import com.hsstoyanova.barkwise.databaseservice.Request;
import com.loopj.android.http.RequestParams;

import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ScanChipActivity extends AppCompatActivity{

	private int PICK_IMAGE_REQUEST = 1;
	private ImageView ivGallery, ivCamera, ivCheck;
	private Button btnScan;
	private EditText etChipNum;
	private Uri imageUri;
	private Bitmap bitmap;
	private ImageView imgView;
	private static final int ACTION_TAKE_PHOTO_B = 1;
	public String mCurrentPhotoPath;
	
	ProgressDialog pDialog;
    String chipNum = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_chip);

		ivGallery = (ImageView) findViewById(R.id.ivGallery);
		ivCamera = (ImageView) findViewById(R.id.ivCamera);
		btnScan = (Button)findViewById(R.id.btnScanChip);
		imgView = (ImageView) findViewById(R.id.ImageView);
		ivCheck = (ImageView) findViewById(R.id.ivCheckInDB);
		etChipNum = (EditText) findViewById(R.id.editTxtChipNumber);
		
		ivGallery.setOnClickListener(onClickUpload);
		ivCamera.setOnClickListener(onClickTakePictureAndScan);
		btnScan.setOnClickListener(onClickScan);
		ivCheck.setOnClickListener(onClickCheck);

	}
	
	OnClickListener onClickUpload = new OnClickListener(){
		// TODO Auto-generated method stub
		
		public void onClick(View v) 
		{
			showFileChooser();
		}
	};
	
	OnClickListener onClickTakePictureAndScan = new OnClickListener(){
		// TODO Auto-generated method stub
		
		public void onClick(View v) 
		{
			dispatchTakePictureIntent();
		}
	};
	
	OnClickListener onClickScan = new OnClickListener(){
		// TODO Auto-generated method stub
		String result = "";
		public void onClick(View v) 
		{
			if(isNetworkAvailable())
			{
				try {
					result = new ScanImage().execute().get();
				} catch (InterruptedException e) {
					debugMsg(Utils.ErrorMessage.couldntScanImage);
				} catch (ExecutionException e) {
					debugMsg(Utils.ErrorMessage.couldntScanImage);
				}
				if(result != "")
				{
					etChipNum.setText(result);
				}
			}
			else
			{
				debugMsg(Utils.ErrorMessage.internetConnection);
			}
		}
	};
	
	OnClickListener onClickCheck = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(isNetworkAvailable())
			{
				new CheckChipNumber().execute();
			}
			else
			{
				debugMsg(Utils.ErrorMessage.internetConnection);
			}
		}
	};
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
 
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
        	imageUri = data.getData();
            mCurrentPhotoPath = getRealPathFromURI(getApplicationContext() , imageUri); 
			Bitmap bitmapFromMediaStore = getBitmap(imageUri);
			if(bitmapFromMediaStore != null)
			{
				bitmap = bitmapFromMediaStore;
			}
			imgView.setImageBitmap(bitmap);
        }
        else if(requestCode == ACTION_TAKE_PHOTO_B)
		{
			if (resultCode == RESULT_OK) 
			{
				handleBigCameraPhoto();
			}
		}	
    }
	
	@Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	btnScan = null;
    	etChipNum = null;
    	imageUri= null;
    	bitmap= null;
    	imgView= null;
    	mCurrentPhotoPath= null;
    	System.gc();
    }
	
	public  Bitmap getBitmap(Uri uri)
	{
		Bitmap result = null;
		
		try {
			result = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	private void handleBigCameraPhoto() 
	{
		if (mCurrentPhotoPath != null) {
			setBitmap();
			galleryAddPic();
		}
	}
	
	public String getRealPathFromURI(Context context, Uri contentUri) {
	  Cursor cursor = null;
	  try { 
	    String[] proj = { MediaStore.Images.Media.DATA };
	    cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	  } finally {
	    if (cursor != null) {
	      cursor.close();
	    }
	  }
	}
	
	private void setBitmap()
	{
		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;

		/* Decode the JPEG file into a Bitmap */
		Bitmap bmp = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		
		if(bmp != null)
		{
			//debugMsg("BMP ne e null");
			imgView.setImageBitmap(bmp);
		}
		
		bitmap = bmp;
	}
		
	private void galleryAddPic() {
	    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    this.sendBroadcast(mediaScanIntent);
	}
	
	private void showFileChooser() {
	        Intent intent = new Intent();
	        intent.setType("image/*");
	        intent.setAction(Intent.ACTION_GET_CONTENT);
	        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
	    }

	private void dispatchTakePictureIntent() {

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

			return f;
		}
	 
	private File createImageFile() throws IOException
	{
		    
		    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		    String imageFileName = "JPEG_" + timeStamp + "_";
		    File storageDir = Environment.getExternalStoragePublicDirectory(
		            Environment.DIRECTORY_PICTURES);
		    
		    File image = File.createTempFile(
		        imageFileName,  // prefix 
		        ".jpg",         //suffix 
		        storageDir      // directory 
		    );

		    // Save a file: path for use with ACTION_VIEW intents
		    mCurrentPhotoPath = "file:" + image.getAbsolutePath();
		    Log.d("+++++++ create image file ", mCurrentPhotoPath);
		    return image;
		}
	 
	private String scanChipNumber()
	{
		if(bitmap != null)
		{	
			
				ExifInterface exif = null;
		    	
				try
				{
					exif = new ExifInterface(mCurrentPhotoPath);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					debugMsg(Utils.ErrorMessage.somethingWentWrong);
					return "";
				}
		    	
				int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);
				bitmap = rotateBitmap(bitmap, exifOrientation);
				String DATA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
				checkTrainedFile(DATA_PATH);
				TessBaseAPI baseApi = new TessBaseAPI();
				baseApi.setVariable("tessedit_char_whitelist", "0123456789,/ABCDEFGHJKLMNPQRSTUVWXY");
				boolean initResult = baseApi.init(DATA_PATH + "/tesseract", "eng");
				
				if(initResult)
				{
					baseApi.setImage(bitmap);
					try
					{
						String recognizedText = baseApi.getUTF8Text();
						baseApi.end();
						return recognizedText.trim();	
					}
					catch(Exception e)
					{
						baseApi.end();
						return "";
					}
				}
				else
				{
					debugMsg(Utils.ErrorMessage.problemWithTrainedDataOCR);
					return "";
				}
		}
		else
		{
			debugMsg(Utils.ErrorMessage.noImageToScan);
			return "";
		}
	}

	private class ScanImage extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ScanChipActivity.this);
			pDialog.setMessage("Scanning image..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
		    pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{
			String result = scanChipNumber();
			chipNum = result;
			return result;
		}
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
		}
	}
	
	private boolean checkTrainedFile(String DATA_PATH)
	{
		
        boolean result = false;
		
		File tesseractDir = new File(DATA_PATH + "/tesseract/tessdata");
		
		
	    if(!tesseractDir.exists() || !tesseractDir.isDirectory())
	    {
			tesseractDir.mkdirs();
	    }
	    
		File trainedData = new File(tesseractDir.getAbsolutePath() + "/eng.traineddata");
		if(!trainedData.exists())
		{
			try
			{
				 trainedData.createNewFile();					
			}
			catch(IOException e)
			{
				return false;
			}
		}
	 
		
		InputStream in = null; 
	    OutputStream out = null;
		try
		{
			in = getAssets().open("eng.traineddata");
			if(in == null)
			{
				return false;
			}
			
		    out = new FileOutputStream(trainedData);
		    
		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = in.read(buf)) > 0) 
			{
		        out.write(buf, 0, len);
		    }
			
		    in.close();
		    out.close();
		    
		    result = true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
		}	
	    
		return result;
		
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
	
	private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

	    Matrix matrix = new Matrix();
	    switch (orientation) {
	        case ExifInterface.ORIENTATION_NORMAL:
	            return bitmap;
	        case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
	            matrix.setScale(-1, 1);
	            break;
	        case ExifInterface.ORIENTATION_ROTATE_180:
	            matrix.setRotate(180);
	            break;
	        case ExifInterface.ORIENTATION_FLIP_VERTICAL:
	            matrix.setRotate(180);
	            matrix.postScale(-1, 1);
	            break;
	        case ExifInterface.ORIENTATION_TRANSPOSE:
	            matrix.setRotate(90);
	            matrix.postScale(-1, 1);
	            break;
	       case ExifInterface.ORIENTATION_ROTATE_90:
	           matrix.setRotate(90);
	           break;
	       case ExifInterface.ORIENTATION_TRANSVERSE:
	           matrix.setRotate(-90);
	           matrix.postScale(-1, 1);
	           break;
	       case ExifInterface.ORIENTATION_ROTATE_270:
	           matrix.setRotate(-90);
	           break;
	       default:
	           return bitmap;
	    }
	    try {
	        Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	        bitmap.recycle();
	        return bmRotated;
	    }
	    catch (OutOfMemoryError e) {
	        e.printStackTrace();
	        return null;
	    }
	}

	private class CheckChipNumber extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ScanChipActivity.this);
			pDialog.setMessage("Logging in..");
		
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected String doInBackground(String... args) 
		{
			JSONArray jsonArray;
			if(chipNum != null && chipNum != "")
			{
				RequestParams params = new RequestParams();
				params.put("chipNum", chipNum);
				
				Request request = new Request(PhpFiles.scanChip, Utils.RequestActions.GET.toString(), params);
				JSONObject json = request.response;
				if(json != null)
				{
					try {
						int success = json.getInt(Utils.REQUEST_RESULT);
						
						if (success == 1) 
						{
							Log.d("-------------", "success e 1");
							jsonArray = json.getJSONArray("userPetResult");
							
							if(jsonArray != null && jsonArray.length() > 0)
							{

								Log.d("-------------", "v if-a na json");
								JSONObject firstResult = jsonArray.getJSONObject(0);
								String pet =  firstResult.getString("pet");
								String dob =  firstResult.getString("dob");
								String user = firstResult.getString("user");
								String email = firstResult.getString("email");
								
								debugMsg("pet:" + pet + ", dob:"+ dob + ", user:" + user + ", email:" + email);
								
							}
							else
							{
								debugMsg("Empty result set.");
							}
							// closing this screen
							finish();
						} 
						else 
						{
							debugMsg("No pet with this chip number is found!");
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				else
				{
					Log.d("json", "json is null");
				}
				}
			else
			{
				debugMsg("No value to check for");
			}
			
			return null;
		}
		protected void onPostExecute(String file_url) {
			// dismiss the dialog once done
			pDialog.dismiss();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(isNetworkAvailable())
		{
			switch(item.getItemId())	
			{
				case R.id.news_feed:
				{
					Intent i = new Intent(getApplicationContext(),NewsFeedActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.pets:
				{
					// pets activity
					Intent i = new Intent(getApplicationContext(),PetsActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				
				case R.id.gallery:
				{
					Intent i = new Intent(getApplicationContext(),GalleryActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.reminders:
				{
					// reminders activity
					Intent i = new Intent(getApplicationContext(),RemindersActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.vet_clinics:
				{
					// vet clinics activity
					Intent i = new Intent(getApplicationContext(),VetsActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.suggestions:
				{
					Intent i = new Intent(getApplicationContext(),SuggestionsActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.edit_profile:
				{
					Intent i = new Intent(getApplicationContext(),EditProfileActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.my_places:
				{
					Intent i = new Intent(getApplicationContext(),UserPlacesActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.change_pass:
				{
					Intent i = new Intent(getApplicationContext(),ChangePasswordActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				case R.id.logout:
				{
					Intent i = new Intent(getApplicationContext(),UserPlacesActivity.class);
					startActivity(i);
					finish();
					break;
				}
				
				default:
				{}	
			
			}
		}
		else
		{
			debugMsg(Utils.ErrorMessage.internetConnection);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
	    Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
	    startActivity(i);
	    finish();
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
}
