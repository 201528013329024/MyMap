package com.xhg.route_result;

import com.xhg.mymap.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyRouteAdapter extends BaseAdapter {

	private Context context;
	public Route_Result_ListCell[] data;

	public void InitData(int length) {

		data = new Route_Result_ListCell[length];
	}

	public MyRouteAdapter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		return data.length;
	}

	@Override
	public Route_Result_ListCell getItem(int position) {
		return data[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Context getContext() {
		return context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LinearLayout l1 = null;
		if (convertView != null) {
			l1 = (LinearLayout) convertView;
		} else {
			l1 = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.route_cell, null);

		}

		
		LinearLayout layout1 = (LinearLayout) l1.findViewById((com.xhg.mymap.R.id.layout1));
		LinearLayout layout2 = (LinearLayout) l1.findViewById((com.xhg.mymap.R.id.layout2));
		
		TextView tv_lineTitle = (TextView) l1.findViewById(com.xhg.mymap.R.id.tv_LineTitle);
		TextView tv_route_Num = (TextView) l1.findViewById(com.xhg.mymap.R.id.tv_Route_Num);
		TextView tv_route_Distance = (TextView) l1.findViewById(com.xhg.mymap.R.id.tv_Route_Distance);
		TextView tv_route_Detail_Contents = (TextView) l1.findViewById(com.xhg.mymap.R.id.tv_Route_Detail_Contents);

		Route_Result_ListCell data = getItem(position);
		if (data.type == 1) {
			tv_lineTitle.setText(data.getLineTitle());
			tv_route_Num.setText(data.getRoute_Num() + "");
			tv_route_Distance.setText(data.getRoute_Distance() + "");
		}else {
			tv_lineTitle.setText("æ‡¿Î£∫"+data.getRoute_Distance() + "");

			layout2.setVisibility(View.INVISIBLE);
		}
		tv_route_Detail_Contents.setText(data.getRoute_detailContents());

		return l1;
	}

}
