package com.hsstoyanova.barkwise;

import java.util.ArrayList;

import com.hsstoyanova.barkwise.data.Pet;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListViewPetsAdapter  extends ArrayAdapter{

	private Context context;
	private int layoutResourceId;
	private ArrayList<Pet> data;
	
	public ListViewPetsAdapter(Context context, int layoutResourceId, ArrayList<Pet> data) {
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
		
		TextView name = (TextView) row.findViewById(R.id.txtViewName);
		name.setText(data.get(position).name);

		return row;
	}
}
