package com.hsstoyanova.barkwise.common;

import java.util.ArrayList;

import com.hsstoyanova.barkwise.R;
import com.hsstoyanova.barkwise.data.Tag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MyPlacesListAdapter extends ArrayAdapter{

	private Context context;
	private int layoutResourceId;
	private ArrayList<Tag> data;
	
	public MyPlacesListAdapter(Context context, int layoutResourceId, ArrayList<Tag> data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		//ViewHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			row = inflater.inflate(layoutResourceId, parent, false);

		}
		
		TextView date = (TextView) row.findViewById(R.id.txtViewDate);
		date.setText(data.get(position).date.toString());

		TextView place = (TextView) row.findViewById(R.id.txtViewPlace);
		place.setText(data.get(position).address.toString());

		TextView caption = (TextView) row.findViewById(R.id.txtViewCaption);
		caption.setText(data.get(position).caption.toString());

		return row;
	}
	
}
