package com.hsstoyanova.barkwise;

import java.io.ByteArrayOutputStream;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class UploadImageActivity extends AppCompatActivity{

	public static final String UPLOAD_KEY = "image";
	 
    private int PICK_IMAGE_REQUEST = 1;
    private ImageView ivUpload;
    private ImageView imageView;
    private Bitmap bitmap;
    private Uri filePath;
    ProgressDialog pDialog;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);
 
        ivUpload = (ImageView) findViewById(R.id.ivUpload);
        
        imageView = (ImageView) findViewById(R.id.imageView);
 
        ivUpload.setOnClickListener(onClickUpload);
        
        showFileChooser();
        
    }
    
    protected void onDestroy() 
    {
    	bitmap = null;
    	filePath =null;
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
    
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
 
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
 
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        return encodedImage;
    }
    
    
    private class UploadImage extends AsyncTask<Bitmap, String, String>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(UploadImageActivity.this);
			pDialog.setMessage("Uploading image..");
		
			pDialog.show();
		}
		
		protected String doInBackground(Bitmap... prms) 
		{
			Bitmap bitmap = prms[0];
			
			String imageStr = getStringImage(bitmap);
			imageStr=imageStr.trim();
			
		    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
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
					else {
						debugMsg(Utils.ErrorMessage.somethingWentWrong);
					}
				} 
				catch (JSONException e) 
				{
					debugMsg(Utils.ErrorMessage.somethingWentWrong);
				}
			}
			else
			{
				debugMsg(Utils.ErrorMessage.somethingWentWrong);;
			}
			
			return null;
		}
		protected void onPostExecute(String file_url) {
			// dismiss the dialog once done
			pDialog.dismiss();
		}
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
