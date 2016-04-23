package com.xhg.search_result;


import com.baidu.platform.comapi.map.r;

import com.xhg.mymap.*;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter {

	private Context context;
	public ImageView img_go = null;

	public int search_label = R.drawable.search_lbel_default;
	
	public int[] search_label_ID = new int[] { R.drawable.search_label_0, R.drawable.search_label_1, R.drawable.search_label_2, R.drawable.search_label_3, R.drawable.search_label_4, R.drawable.search_label_5, R.drawable.search_label_6,
			R.drawable.search_label_7, R.drawable.search_label_8, R.drawable.search_label_9, R.drawable.search_label_10, R.drawable.search_label_11, R.drawable.search_label_12, R.drawable.search_label_13, R.drawable.search_label_14, R.drawable.search_label_15,
			R.drawable.search_label_16, R.drawable.search_label_17, R.drawable.search_label_18, R.drawable.search_label_19, R.drawable.search_label_20, R.drawable.search_label_21, R.drawable.search_label_22, R.drawable.search_label_23, R.drawable.search_label_24,
			R.drawable.search_label_25, R.drawable.search_label_26, R.drawable.search_label_27, R.drawable.search_label_28, R.drawable.search_label_29, R.drawable.search_label_30, R.drawable.search_label_31, R.drawable.search_label_32, R.drawable.search_label_33,
			R.drawable.search_label_34, R.drawable.search_label_35, R.drawable.search_label_36, R.drawable.search_label_37, R.drawable.search_label_38, R.drawable.search_label_39, R.drawable.search_label_40, R.drawable.search_label_41, R.drawable.search_label_42,
			R.drawable.search_label_43 };
	
	public int[] btn_ID = new int[] { R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6,
			R.id.btn7, R.id.btn8, R.id.btn9, R.id.btn10, R.id.btn11, R.id.btn12, R.id.btn13, R.id.btn14, R.id.btn15,
			R.id.btn16, R.id.btn17, R.id.btn18, R.id.btn19, R.id.btn20, R.id.btn21, R.id.btn22, R.id.btn23, R.id.btn24,
			R.id.btn25, R.id.btn26, R.id.btn27, R.id.btn28, R.id.btn29, R.id.btn30, R.id.btn31, R.id.btn32, R.id.btn33,
			R.id.btn34, R.id.btn35, R.id.btn36, R.id.btn37, R.id.btn38, R.id.btn39, R.id.btn40, R.id.btn41, R.id.btn42,
			R.id.btn43 };
	
	public MyAdapter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		return data.length;
	}

	public long getItemId(int position) {
		return position;
	}

	public Context getContext() {
		return context;
	}

	public Search_Place_Result_ListCell[] data = new Search_Place_Result_ListCell[10];

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LinearLayout l1 = null;
		if (convertView != null) {
			l1 = (LinearLayout) convertView;
		} else {
			l1 = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.list_cell, null);

		}

		TextView tv_palceName = (TextView) l1.findViewById(com.xhg.mymap.R.id.tv_place_name);
		TextView tv_palceAdress = (TextView) l1.findViewById(com.xhg.mymap.R.id.tv_place_address);
		TextView tv_palcePhone = (TextView) l1.findViewById(com.xhg.mymap.R.id.tv_place_phone);
		TextView tv_distance = (TextView) l1.findViewById(com.xhg.mymap.R.id.tv_place_distance);
		img_go = (ImageView) l1.findViewById(com.xhg.mymap.R.id.img_go_to_place);
		ImageView img_place_ico = (ImageView) l1.findViewById(com.xhg.mymap.R.id.img_place);
		
		for (int i = 0; i < 44; i++) {
			if(search_label == btn_ID[i]){
				img_place_ico.setImageResource(search_label_ID[i]);
				break;
			}
		}
		
		Search_Place_Result_ListCell data = getItem(position);
		tv_palceName.setText(data.getPlace_name());
		tv_palceAdress.setText(data.getPlace_address());
		tv_palcePhone.setText(data.getPlace_phone());
		tv_distance.setText(data.getDistance() + "m");

		return l1;
	}

	public Search_Place_Result_ListCell getItem(int position) {
		// TODO Auto-generated method stub
		return data[position];
	}

}
