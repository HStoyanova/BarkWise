package com.hsstoyanova.barkwise.common;

import java.util.ArrayList;

import com.hsstoyanova.barkwise.R;
import com.hsstoyanova.barkwise.data.Suggestion;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SuggestionsListAdapter extends ArrayAdapter{

	private Context context;
	private int layoutResourceId;    
	private ArrayList<Suggestion> data;
	
	public SuggestionsListAdapter(Context context, int layoutResourceId, ArrayList<Suggestion> data) {
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
		
		TextView user = (TextView) row.findViewById(R.id.tvUser);
		user.setText(data.get(position).user.toString());

		TextView descr = (TextView) row.findViewById(R.id.tvDescr);
		descr.setText(data.get(position).content.toString());

		TextView date = (TextView) row.findViewById(R.id.tvDate);
		date.setText(data.get(position).date.toString());

		return row;
	}
	
}
