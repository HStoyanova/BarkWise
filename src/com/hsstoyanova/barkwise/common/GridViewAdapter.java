package com.hsstoyanova.barkwise.common;

import java.util.ArrayList;

import com.hsstoyanova.barkwise.R;
import com.bumptech.glide.Glide;
import com.hsstoyanova.barkwise.data.Image;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridViewAdapter extends ArrayAdapter {
	private Context context;
	private int layoutResourceId;
	private ArrayList<Image> data;

	public GridViewAdapter(Context context, int layoutResourceId, ArrayList<Image> data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;

		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_layout, parent, false);
		       
			holder = new ViewHolder();
			holder.imageTitle = (TextView) convertView.findViewById(R.id.text);
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		
			Glide
	        .with(context)
	        .load(data.get(position).filePath)
	        .asBitmap()
	        .atMost()
	        .into((ImageView) convertView.findViewById(R.id.image))
	        ;

		return convertView;
	}

	static class ViewHolder {
		TextView imageTitle;
		ImageView image;
	}
 
 
}
