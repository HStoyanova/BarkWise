package com.hsstoyanova.barkwise.common;

import java.util.ArrayList;

import com.hsstoyanova.barkwise.R;
import com.hsstoyanova.barkwise.common.Utils.ReminderPair;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RemindersListAdapter extends ArrayAdapter{

	private Context context;
	private int layoutResourceId;
	private ArrayList<ReminderPair> data;
	
	public RemindersListAdapter(Context context, int layoutResourceId, ArrayList<ReminderPair> data) {
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
		
		TextView dateTime = (TextView) row.findViewById(R.id.txtViewDateTime);
        dateTime.setText("Date & Time:" + data.get(position).time.toString() + " " + data.get(position).date.toString());

		TextView pet = (TextView) row.findViewById(R.id.txtViewPet);
		pet.setText("Pet:" + data.get(position).pet.toString());

		TextView type = (TextView) row.findViewById(R.id.txtViewType);
		type.setText("Type:" + data.get(position).type.toString());

		return row;
	}
}
