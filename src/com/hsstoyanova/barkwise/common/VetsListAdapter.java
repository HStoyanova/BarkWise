package com.hsstoyanova.barkwise.common;

import java.util.List;

import com.bumptech.glide.Glide;
import com.hsstoyanova.barkwise.R;
import com.hsstoyanova.barkwise.data.VetClinic;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class VetsListAdapter extends ArrayAdapter {

	private Context context;
	private int layoutResourceId;
	private List<VetClinic> data;
	
	public VetsListAdapter(Context context, int layoutResourceId, List<VetClinic> data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		if (row == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			row = inflater.inflate(layoutResourceId, parent, false);

		}
		TextView name = (TextView) row.findViewById(R.id.txtName);
		name.setText(data.get(position).name.toString());

		TextView isOpenNow = (TextView) row.findViewById(R.id.txIsOpenNow);
		isOpenNow.setText(data.get(position).openNow.toString());

		 Glide
        .with(context)
        .load(data.get(position).icon)
        .asBitmap()
        .atMost()
        .into((ImageView) row.findViewById(R.id.imageVet))
        ;

		return row;
	}
}
