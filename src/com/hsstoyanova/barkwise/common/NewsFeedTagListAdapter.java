package com.hsstoyanova.barkwise.common;

import java.util.List;

import com.hsstoyanova.barkwise.ProfileActivity;
import com.hsstoyanova.barkwise.R;
import com.hsstoyanova.barkwise.common.Utils.RecentTagInfoPair;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NewsFeedTagListAdapter extends ArrayAdapter{
	
	
	private Context context;
	private int layoutResourceId;
	private List<RecentTagInfoPair> data;
	int userId = -1;
	
	public NewsFeedTagListAdapter(Context context, int layoutResourceId, List<RecentTagInfoPair> data) {
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
		
		TextView userName = (TextView) row.findViewById(R.id.tvUserName);
		userName.setText(data.get(position).username);

		TextView place = (TextView) row.findViewById(R.id.tvPlace);
		place.setText(data.get(position).tag.address);

		TextView date = (TextView) row.findViewById(R.id.tvDate);
		date.setText(data.get(position).tag.date);

		Log.d("NEWS FEED ADAPTER", data.get(position).tag.date);
		
		userId = data.get(position).tag.userId;
		
		userName.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				//show user profile
				Intent i = new Intent(context, ProfileActivity.class);
				i.putExtra("id", userId);
				context.startActivity(i);
				
			}
		});
		
		return row;
	}
}
