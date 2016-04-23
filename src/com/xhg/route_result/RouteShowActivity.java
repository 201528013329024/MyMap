package com.xhg.route_result;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPlanNode;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKRoutePlan;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRoutePlan;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.xhg.mymap.ConstantValue;
import com.xhg.mymap.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class RouteShowActivity extends Activity {

	private MKSearch search_Transit = null;
	private MKSearch search_Walk = null;
	private MKSearch search_Drive = null;
	private MKSearchListener listener_Transit = null;
	private MKSearchListener listener_Walk = null;
	private MKSearchListener listener_Drive = null;
	private BMapManager routeManager; // 地图引擎的管理工具

	private Button btn_GongJiao = null;
	private Button btn_JiaChe = null;
	private Button btn_Walking = null;
	private ListView lv = null;

	private int mode = 0;
	private GeoPoint start_Point = null;
	private int startLantitude = 0;
	private int startLongitude = 0;
	private String start_Place = "";
	private String end_Place = "";
	private String myCity = "北京";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		checkKey();

		setContentView(R.layout.route_view);

		btn_GongJiao = (Button) findViewById(R.id.btn_GongJiao);
		btn_JiaChe = (Button) findViewById(R.id.btn_JiaChe);
		btn_Walking = (Button) findViewById(R.id.btn_Walking);
		lv = (ListView) findViewById(R.id.tv_routes_list);

		Intent intent = getIntent();
		if (intent != null) {

			mode = intent.getIntExtra("mode", 0);
			myCity = intent.getStringExtra("myCity").toString();
			
			if (mode == 0) {
				start_Place = intent.getStringExtra("start_place");
				end_Place = intent.getStringExtra("end_place");
				
			}else if(mode == 1){
				startLantitude = intent.getIntExtra("start_place_latitude", 0);
				startLongitude = intent.getIntExtra("start_place_longitude", 0);
				start_Point = new GeoPoint((int) (startLantitude), (int) (startLongitude));
				end_Place = intent.getStringExtra("end_place");
			}
			
			search_Transit_Route(mode);		

		}
		btn_GongJiao.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				search_Transit_Route(mode);
			}
		});

		btn_JiaChe.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				search_Drive_Route(mode);
			}
		});

		btn_Walking.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				search_Walk_Route(mode);
			}
		});

		
	}

	private void search_Transit_Route(int mode) {

		search_Transit = new MKSearch();
		listener_Transit = new MyMKSearchListener() {
			@Override
			public void onGetTransitRouteResult(MKTransitRouteResult result, int error) {
				if (error == 0) {
					MyRouteAdapter adapter = new MyRouteAdapter(RouteShowActivity.this);

					int numPlan = result.getNumPlan(); // 路线条数
					adapter.InitData(numPlan);

					for (int i = 0; i < result.getNumPlan(); i++) {
						MKTransitRoutePlan route = result.getPlan(i);
						int lineNum = route.getNumLines();
						String lineTitle = "";
						String route_DetailContents = "";
						for (int j = 0; j < lineNum; j++) {
							if (j == lineNum - 1) {
								lineTitle = lineTitle + route.getLine(j).getTitle();
								route_DetailContents = route_DetailContents + route.getLine(j).getTip() + "。";
							} else {
								lineTitle = lineTitle + route.getLine(j).getTitle() + " -> ";

								route_DetailContents = route_DetailContents + route.getLine(j).getTip() + "；\n";
							}

						}
						int routeNum = route.getNumRoute();
						int route_Distance = route.getDistance();
						adapter.data[i] = new Route_Result_ListCell(lineTitle, routeNum + "站", route_Distance + "m",
								route_DetailContents,1);
					}

					lv.setAdapter(adapter);

					// lv.getChildAt(0).findViewById(R.id.tv_Route_Detail)

				} else {
					Toast.makeText(RouteShowActivity.this, "没有查询结果", Toast.LENGTH_SHORT).show();
				}

			}
		};

		search_Transit.init(routeManager, listener_Transit);
		String city = myCity;

		MKPlanNode start_place = new MKPlanNode();
		MKPlanNode end_place = new MKPlanNode();
		
		if(mode == 1){
			start_place.pt = start_Point; // 也可以用地理坐标表示
		}else if(mode == 0){
			start_place.name = start_Place; // 也可以用地理坐标表示
		}
		end_place.name = end_Place;// 也可以用地理坐标表示
		search_Transit.transitSearch(city, start_place, end_place);
	}

	private void search_Walk_Route(int mode) {
		search_Walk = new MKSearch();
		listener_Walk = new MyMKSearchListener() {
			@Override
			public void onGetWalkingRouteResult(MKWalkingRouteResult result, int error) {
				if (error == 0) {
					MyRouteAdapter adapter = new MyRouteAdapter(RouteShowActivity.this);

					int numPlan = result.getNumPlan(); // 路线条数
					adapter.InitData(numPlan);

					for (int i = 0; i < result.getNumPlan(); i++) {
						MKRoutePlan route = result.getPlan(i);
						int routeNum = route.getNumRoutes();
						int distance = route.getDistance();
						String string = "";
						for (int j = 0; j < 1; j++) {
							// String string = string +
							// route.getRoute(j).getTip() + "\n";
							int numSteps = route.getRoute(j).getNumSteps();
							for (int k = 0; k < numSteps; k++) {
								string = string + route.getRoute(j).getStep(k).getContent() + "\n";
							}

						}

						adapter.data[i] = new Route_Result_ListCell("", routeNum + "", distance + "m", string,0);
					}

					lv.setAdapter(adapter);
				} else {
					Toast.makeText(RouteShowActivity.this, "没有查询结果", Toast.LENGTH_SHORT).show();
				}

			}
		};

		search_Walk.init(routeManager, listener_Walk);

		String start_city = myCity;
		String end_city = myCity;
		
		MKPlanNode start_place = new MKPlanNode();
		MKPlanNode end_place = new MKPlanNode();
		
		if(mode == 1){
			start_place.pt = start_Point; // 也可以用地理坐标表示
		}else if(mode == 0){
			start_place.name = start_Place; // 也可以用地理坐标表示
		}
		
		end_place.name = end_Place;// 也可以用地理坐标表示

		search_Walk.walkingSearch(start_city, start_place, end_city, end_place);
	}

	private void search_Drive_Route(int mode) {
		search_Drive = new MKSearch();
		listener_Drive = new MyMKSearchListener() {
			@Override
			public void onGetDrivingRouteResult(MKDrivingRouteResult result, int error) {
				if (error == 0) {
					MyRouteAdapter adapter = new MyRouteAdapter(RouteShowActivity.this);

					int numPlan = result.getNumPlan(); // 路线条数
					adapter.InitData(numPlan);
					for (int i = 0; i < result.getNumPlan(); i++) {
						MKRoutePlan route = result.getPlan(i);
						int routeNum = route.getNumRoutes();
						int distance = route.getDistance();
						String string = "";
						for (int j = 0; j < 1; j++) {
							int numSteps = route.getRoute(j).getNumSteps();
							for (int k = 0; k < numSteps; k++) {
								string = string + route.getRoute(j).getStep(k).getContent() + "\n";
							}
						}

						adapter.data[i] = new Route_Result_ListCell("", routeNum + "", distance + "m", string,0);
					}

					lv.setAdapter(adapter);

				} else {
					Toast.makeText(RouteShowActivity.this, "没有查询结果", Toast.LENGTH_SHORT).show();
				}
			}

		};

		search_Drive.init(routeManager, listener_Drive);

		String start_city = myCity;
		String end_city = myCity;

		
		MKPlanNode start_place = new MKPlanNode();
		MKPlanNode end_place = new MKPlanNode();
		
		if(mode == 1){
			start_place.pt = start_Point; // 也可以用地理坐标表示
		}else if(mode == 0){
			start_place.name = start_Place; // 也可以用地理坐标表示
		}
		
		end_place.name = end_Place;// 也可以用地理坐标表示
		search_Drive.drivingSearch(start_city, start_place, end_city, end_place);

//		ArrayList<MKWpNode> nodes = new ArrayList<MKWpNode>();
//		MKWpNode node = new MKWpNode();
//		node.city = "北京";
//		node.name = "中国美术学院";
//		nodes.add(node);

		// search_Drive.drivingSearch(start_city, start_place,
		// end_city,end_place, nodes);
	}

	/** 向百度服务器请求验证KEY **/
	private void checkKey() {
		routeManager = new BMapManager(getApplicationContext());
		routeManager.init(ConstantValue.KEY, new MKGeneralListener() {

			@Override
			public void onGetPermissionState(int error) {
				// TODO 授权验证
				if (error == MKEvent.ERROR_PERMISSION_DENIED) {
					Toast.makeText(RouteShowActivity.this, "授权验证失败。", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onGetNetworkState(int error) {
				// TODO 网络验证
				if (error == MKEvent.ERROR_NETWORK_CONNECT) {
					Toast.makeText(RouteShowActivity.this, "网络连接失败，请检查网络状态。", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	class MyMKSearchListener implements MKSearchListener {

		@Override
		public void onGetAddrResult(MKAddrInfo result, int error) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetBusDetailResult(MKBusLineResult result, int error) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult result, int error) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetPoiDetailSearchResult(int type, int error) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetPoiResult(MKPoiResult result, int type, int error) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetSuggestionResult(MKSuggestionResult result, int error) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult result, int error) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult result, int error) {
			// TODO Auto-generated method stub

		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		// super.onBackPressed();
		RouteShowActivity.this.finish();
	}
}
