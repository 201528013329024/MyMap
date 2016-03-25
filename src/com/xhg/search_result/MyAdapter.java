package com.xhg.search_result;

import com.xhg.mymap.MainActivity;
import com.xhg.mymap.R;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter {

	private Context context;

	public MyAdapter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.length;
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public Context getContext() {
		return context;
	}

	public Search_Place_Result_ListCell[] data = new Search_Place_Result_ListCell[10];
	// {
	// new Search_Place_Result_ListCell("place_name", "place_address",
	// "place_phone", 0),
	// new Search_Place_Result_ListCell("place_name", "place_address",
	// "place_phone", 0),
	// new Search_Place_Result_ListCell("place_name", "place_address",
	// "place_phone", 0)
	// };

	@SuppressLint("NewApi") 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		LinearLayout l1 = null;
		if (convertView != null) {
			 l1 = (LinearLayout) convertView;
		} else {
			 l1 = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.list_cell, null);

		}
		
		TextView tv_palceName = (TextView) l1.findViewById(com.xhg.mymap.R.id.tv_place_name);
		TextView tv_palceAdress = (TextView) l1.findViewById(com.xhg.mymap.R.id.tv_place_address);
		TextView tv_palcePhone = (TextView) l1.findViewById(com.xhg.mymap.R.id.tv_place_phone);
		//
		Search_Place_Result_ListCell data = getItem(position);
		tv_palceName.setText(data.getPlace_name());
		tv_palceAdress.setText(data.getPlace_address());
		tv_palcePhone.setText(data.getPlace_phone());

		return l1;
	}

	public Search_Place_Result_ListCell getItem(int position) {
		// TODO Auto-generated method stub
		return data[position];
	}

}
