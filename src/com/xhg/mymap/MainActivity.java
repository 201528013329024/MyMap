/**
 * @author Haoguang Xu
 * Copyright (c) 2016, UCAS
 * All rights reserved. 
 * 
 * MainActivity Class {@link MainActivity}  
 */

package com.xhg.mymap;

import java.util.ArrayList;

//
/*定位需要引用的包*/
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.Geometry;
import com.baidu.mapapi.map.Graphic;
import com.baidu.mapapi.map.GraphicsOverlay;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PoiOverlay;
import com.baidu.mapapi.map.RouteOverlay;
import com.baidu.mapapi.map.Symbol;
import com.baidu.mapapi.map.TextItem;
import com.baidu.mapapi.map.TextOverlay;
import com.baidu.mapapi.map.TransitOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPlanNode;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKRoutePlan;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRoutePlan;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.mapapi.search.MKWpNode;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.xhg.search_result.MyAdapter;
import com.xhg.search_result.SearchLableActivity;
import com.xhg.search_result.Search_Place_Result_ListCell;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private BMapManager manager; // 地图引擎的管理工具
	private MapView mapView; // 地图显示容器
	private MapController mapController; // mapController用于控制地图的平移，缩放和旋转

	private View satellite_traffic_map, location_icon, search_icon;
	private ImageView img_Flag, img_Traffic, img_Satellite, img_Loc;
	private EditText editText;

	// 下面两个变量用于场所搜索，如美食、加油站等
	private MKSearch search_Place, search_Drive, search_Walk, search_Transit;
	private MKSearchListener listener_Place, listener_Drive, listener_Walk, listener_Transit;

	public LocationClient mLocationClient = null;
	public BDLocationListener loctionListener = new MyLocationListener();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 必须在设定Layout前验证百度key
		checkKey();

		setContentView(R.layout.activity_main);

		initView();

		addFlagTrafficSatellite(); // 加载可显示平面图、路况图和卫星图按钮

		location();
		addLocation();
		addSearchIcon();

		// search_Drive_Route();// 搜索驾车路线

		// search_Walk_Route();// 搜索步行路线
		// search_Transit_Route();// 搜索公交路线

	}

	private void addSearchIcon() {
		// RelativeLayout.LayoutParams lp = new
		// RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT);
		// lp.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
		// lp.leftMargin = 100;
		// LayoutInflater inflater = LayoutInflater.from(this);
		// search_icon = inflater.inflate(R.layout.search, null);
		// search_icon.setLayoutParams(lp);

		search_icon = View.inflate(this, R.layout.search_icon, null);

		LinearLayout layout = (LinearLayout) search_icon.findViewById(R.id.search_icon);

		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels; // 屏幕宽度（像素）

		search_icon.setVisibility(View.VISIBLE);

		Toast.makeText(MainActivity.this, layout.getMeasuredWidth() + "", Toast.LENGTH_SHORT).show();
		search_icon.setX((width - dip2px(MainActivity.this, 320)) / 2);
		search_icon.setY(15);

		mapView.addView(search_icon);

		editText = (EditText) search_icon.findViewById(R.id.editText1);
		editText.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// search_place();
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, SearchLableActivity.class);
				MainActivity.this.startActivityForResult(intent, 0);

			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (resultCode) {
		case RESULT_OK:
			// System.out.println(data.getExtras().getString( "result" ));
			String searchContent = data.getExtras().getString("result");
			// Toast.makeText(MainActivity.this, searchContent,
			// Toast.LENGTH_SHORT).show();
			search_place(searchContent);
			break;
		default:
			break;
		}
	}

	public int dip2px(Context context, float dipValue) {
		float m = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * m + 0.5f);
	}

	private void addLocation() {
		location_icon = View.inflate(this, R.layout.location, null);

		// 实例化img_Flag
		img_Loc = (ImageView) location_icon.findViewById(R.id.img_Loc);
		img_Loc.setImageResource(R.drawable.img_loc);

		location_icon.setVisibility(View.VISIBLE);

		// 获取手机屏幕的宽度

		location_icon.setX(screenWidth - 160);
//		location_icon.setY(height - dip2px(MainActivity.this, 70));
		location_icon.setY(800);

		// 将satellite_traffic_map显示在mapView上
		mapView.addView(location_icon);

		location_icon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { // 显示平面图

				// 调用两次location()是为了加快定位速度
				location();
				location();
			}
		});

	}

	public GeoPoint MyPoint = null;

	private void location() {
		// mapView.setTraffic(false);
		// mapView.setSatellite(false);
		mLocationClient = new LocationClient(getApplicationContext());
		mLocationClient.start();
		// mLocationClient.setForBaiduMap(true);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		// option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
		option.disableCache(true);// 禁止启用缓存定位
		option.setPoiNumber(5); // 最多返回POI个数
		option.setPoiDistance(1000); // poi查询距离
		option.setPoiExtraInfo(true); // 是否需要POI的电话和地址等详细信息
		mLocationClient.setLocOption(option);

		mLocationClient.registerLocationListener(loctionListener);
	}

	private class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}

			MyLocationOverlay overlay = new MyLocationOverlay(mapView) {

				@Override
				protected boolean dispatchTap() {
					// TODO Auto-generated method stub
					// GeoPoint point =
					Toast.makeText(MainActivity.this, "再按一次后退键退出地图！", Toast.LENGTH_SHORT).show();
					// mapView.setSatellite(true);
					return true;
				}
			};
			LocationData data = new LocationData();
			data.latitude = location.getLatitude();
			data.longitude = location.getLongitude();

			Drawable drawable = MainActivity.this.getResources().getDrawable(R.drawable.location);
			// overlay.setMarker(drawable);

			overlay.enableCompass();
			overlay.isCompassEnable();

			// overlay.d
			overlay.setData(data);
			// overlay.enableCompass();
			// mapView.getOverlay().clear();
			mapView.getOverlays().add(overlay);

			// mapView.refresh();
			MyPoint = new GeoPoint((int) (data.latitude * 1E6), (int) (data.longitude * 1E6));
			mapController.setZoom(15);
			// mapController.setCenter(point);
			mapController.animateTo(MyPoint);
			mapView.refresh();
			// mapView.invalidate();

		}

		@Override
		public void onReceivePoi(BDLocation arg0) {
			// TODO Auto-generated method stub

		}
		// TODO Auto-generated method stub

	}

	private void search_Transit_Route() {
		mapView.setTraffic(true); // 要在地图上显示推荐驾驶路线，必须将地图设置为路况模式
		search_Transit = new MKSearch();
		listener_Transit = new MyMKSearchListener() {
			@Override
			public void onGetTransitRouteResult(MKTransitRouteResult result, int error) {
				if (error == 0) {
					if (result != null) {
						TransitOverlay overlay = new TransitOverlay(MainActivity.this, mapView);
						setData(overlay, result);
						mapView.getOverlays().add(overlay);
						mapView.refresh();
						Toast.makeText(MainActivity.this, "result not null", Toast.LENGTH_SHORT).show();
						super.onGetTransitRouteResult(result, error);
					}
				} else {
					Toast.makeText(MainActivity.this, "没有查询结果", Toast.LENGTH_SHORT).show();
				}

			}

			private void setData(TransitOverlay overlay, MKTransitRouteResult result) {
				if (result.getNumPlan() > 0) { // 显示所有公交路线
					for (int i = 0; i < result.getNumPlan(); i++) {
						MKTransitRoutePlan route = result.getPlan(i);
						overlay.setData(route);
					}

				} else {
					Toast.makeText(MainActivity.this, "XXXX", Toast.LENGTH_SHORT).show();
				}
			}
		};

		search_Transit.init(manager, listener_Transit);

		String city = "北京";

		MKPlanNode start_place = new MKPlanNode();
		start_place.name = "天安门"; // 也可以用地理坐标表示
		// start_place.pt = ConstantValue.geoUCAS;

		MKPlanNode end_place = new MKPlanNode();
		end_place.name = "北京大学";// 也可以用地理坐标表示

		search_Transit.transitSearch(city, start_place, end_place);
	}

	private void search_Walk_Route() {
		mapView.setTraffic(true); // 要在地图上显示推荐驾驶路线，必须将地图设置为路况模式
		search_Walk = new MKSearch();
		listener_Walk = new MyMKSearchListener() {
			@Override
			public void onGetWalkingRouteResult(MKWalkingRouteResult result, int error) {
				if (error == 0) {
					if (result != null) {
						RouteOverlay overlay = new RouteOverlay(MainActivity.this, mapView);
						setData(overlay, result);
						mapView.getOverlays().add(overlay);
						mapView.refresh();
						Toast.makeText(MainActivity.this, "result not null", Toast.LENGTH_SHORT).show();
						super.onGetWalkingRouteResult(result, error);
					}
				} else {
					Toast.makeText(MainActivity.this, "没有查询结果", Toast.LENGTH_SHORT).show();
				}
				super.onGetWalkingRouteResult(result, error);
			}

			private void setData(RouteOverlay overlay, MKWalkingRouteResult result) {
				if (result.getNumPlan() > 0) {
					MKRoutePlan plan = result.getPlan(0);
					MKRoute route = plan.getRoute(0); // 此处默认参数为0
					overlay.setData(route);
				} else {
					Toast.makeText(MainActivity.this, "XXXX", Toast.LENGTH_SHORT).show();
				}
			}
		};

		search_Walk.init(manager, listener_Walk);

		String start_city = "北京";
		String end_city = "北京";

		MKPlanNode start_place = new MKPlanNode();
		start_place.name = "天安门"; // 也可以用地理坐标表示
		// start_place.pt = ConstantValue.geoUCAS;

		MKPlanNode end_place = new MKPlanNode();
		end_place.name = "北京大学";// 也可以用地理坐标表示

		search_Walk.walkingSearch(start_city, start_place, end_city, end_place);
	}

	private void search_Drive_Route() {
		mapView.setTraffic(true); // 要在地图上显示推荐驾驶路线，必须将地图设置为路况模式
		search_Drive = new MKSearch();
		listener_Drive = new MyMKSearchListener() {
			@Override
			public void onGetDrivingRouteResult(MKDrivingRouteResult result, int error) {
				if (error == 0) {
					if (result != null) {
						RouteOverlay overlay = new RouteOverlay(MainActivity.this, mapView);
						setData(overlay, result);
						mapView.getOverlays().add(overlay);
						mapView.refresh();
						Toast.makeText(MainActivity.this, "result not null", Toast.LENGTH_SHORT).show();
						super.onGetDrivingRouteResult(result, error);
					}
				} else {
					Toast.makeText(MainActivity.this, "没有查询结果", Toast.LENGTH_SHORT).show();
				}
			}

			private void setData(RouteOverlay overlay, MKDrivingRouteResult result) {
				if (result.getNumPlan() > 0) {
					MKRoutePlan plan = result.getPlan(0);
					MKRoute route = plan.getRoute(0); // 此处默认参数为0
					overlay.setData(route);
				} else {
					Toast.makeText(MainActivity.this, "XXXX", Toast.LENGTH_SHORT).show();
				}
			}
		};

		search_Drive.init(manager, listener_Drive);

		String start_city = "北京";
		String end_city = "北京";

		MKPlanNode start_place = new MKPlanNode();
		start_place.name = "天安门"; // 也可以用地理坐标表示
		// start_place.pt = ConstantValue.geoUCAS;

		MKPlanNode end_place = new MKPlanNode();
		end_place.name = "北京大学";// 也可以用地理坐标表示

		// search_Drive.setDrivingPolicy(MKSearch.EBUS_TIME_FIRST); //驾车筛选条件

		search_Drive.drivingSearch(start_city, start_place, end_city, end_place);

		ArrayList<MKWpNode> nodes = new ArrayList<MKWpNode>();
		MKWpNode node = new MKWpNode();
		node.city = "北京";
		node.name = "中国美术学院";
		nodes.add(node);

		// search_Drive.drivingSearch(start_city, start_place,
		// end_city,end_place, nodes);
	}

	public View view = null;
	public ListView lv = null;
	public Button btn_UpPage = null;
	public Button btn_NextPage = null;
	public Button  btn_PageInfo = null;

	public int currentPage = 0;
	public int numPage = 0;
	public ViewGroup.LayoutParams params = null;
	public int screenWidth = 0;;
	public int screenHeight = 0;

	/** 场所搜索 **/
	private void search_place(String serchContent) {
		// Toast.makeText(MainActivity.this, "进入search_place",
		// Toast.LENGTH_SHORT).show();
		search_Place = new MKSearch();
		listener_Place = new MyMKSearchListener() {
			@SuppressLint("NewApi")
			public void onGetPoiResult(MKPoiResult result, int type, int error) {
				if (error == 0) {
					if (result != null) {
						PoiOverlay overlay = new PoiOverlay(MainActivity.this, mapView);
						setData(overlay, result);
						mapView.getOverlays().clear();
						mapView.getOverlays().add(overlay);
						mapView.refresh();
					} else {
						Toast.makeText(MainActivity.this, "result == null", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(MainActivity.this, "没有查询到相关信息", Toast.LENGTH_SHORT).show();
				}
			}

			@SuppressLint("NewApi")
			private void setData(PoiOverlay overlay, MKPoiResult result) {
				ArrayList<MKPoiInfo> mkPoiInfos = result.getAllPoi();

				MyAdapter adapter = new MyAdapter(MainActivity.this);

				for (int i = 0; i < 10; i++) {
					MKPoiInfo info = result.getPoi(i);
					String placeName = info.name;
					String placeAdress = info.address;
					String placePhone = info.phoneNum != "" ? info.phoneNum : "暂无电话信息";
					int placeDistance = (int) DistanceUtil.getDistance(MyPoint, info.pt);
					adapter.data[i] = new Search_Place_Result_ListCell(placeName, placeAdress, placePhone,
							placeDistance);
				}

				if(view != null) mapView.removeViewInLayout(view);
				view = View.inflate(MainActivity.this, R.layout.serch_result, null);
				lv = (ListView) view.findViewById(R.id.tv_search_result);
				btn_UpPage = (Button) view.findViewById(R.id.btn_UpPage);
				btn_NextPage = (Button) view.findViewById(R.id.btn_NextPage);
			    btn_PageInfo = (Button) view.findViewById(R.id.btn_PageInfo);

				currentPage = result.getPageIndex();
				numPage = result.getNumPages();

				btn_UpPage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (currentPage == 0) {
							Toast.makeText(MainActivity.this, "当前已经是第一页", Toast.LENGTH_SHORT).show();
						} else {
							UpPageForSearch();
						}
					}
				});

				btn_NextPage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (currentPage == numPage) {
							Toast.makeText(MainActivity.this, "当前已经是最后一页", Toast.LENGTH_SHORT).show();
						} else {
							nextPageForSearch();
						}
					}
				});

				btn_PageInfo.setText("共找到" + result.getNumPois() + "搜索结果");

				params = lv.getLayoutParams();

				btn_PageInfo.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						view.setVisibility(View.INVISIBLE);
						show_Close_Search_Result();
					}
				});

				lv.setAdapter(adapter);

				params.width = screenWidth;
				params.height = screenHeight / 2;

				// params.height=setListViewHeightBasedOnChild(lv);

				lv.setLayoutParams(params);

				view.setVisibility(View.VISIBLE);

				view.setY(params.height - dip2px(MainActivity.this, 65));

				view.invalidate();

				mapView.addView(view);

				overlay.setData(mkPoiInfos);

				mapController.animateTo(result.getPoi(0).pt);
				mapController.setZoom(13);
				String string = "当前页" + result.getPageIndex() + "/总页数" + result.getNumPages() + "，   当前条目"
						+ result.getCurrentNumPois() + "/总条目" + result.getNumPois();

			};
		};

		search_Place.init(manager, listener_Place);
		// search.poiSearchNearBy("医院", ConstantValue.geoUCAS, 10000);
		search_Place.poiSearchInCity("北京", serchContent);
	}

	public View view_close_search_result = null;

	private void show_Close_Search_Result() {
		view_close_search_result = View.inflate(MainActivity.this, R.layout.close_search_result, null);
		ListView lv = (ListView) view_close_search_result.findViewById(R.id.lv);
		ViewGroup.LayoutParams params = lv.getLayoutParams();
		params.width = screenWidth;
		lv.setLayoutParams(params);
		view_close_search_result.setY(screenHeight - dip2px(MainActivity.this, 65));

		Button btn_Show = (Button) view_close_search_result.findViewById(R.id.btn_Show);
		Button btn_Close = (Button) view_close_search_result.findViewById(R.id.btn_Close);
		Button btn_Info = (Button) view_close_search_result.findViewById(R.id.btn);
		btn_Info.setText(btn_PageInfo.getText());
		btn_Show.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				view.setVisibility(View.VISIBLE);
				view_close_search_result.setVisibility(View.INVISIBLE);
			}
		});
		
		btn_Info.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				view.setVisibility(View.VISIBLE);
				view_close_search_result.setVisibility(View.INVISIBLE);
			}
		});

		btn_Close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				view_close_search_result.setVisibility(View.INVISIBLE);
				mapView.removeViewInLayout(view_close_search_result);
				mapView.removeViewInLayout(view);
			}
		});
		
		mapView.addView(view_close_search_result);
	}

	/** 跳转到搜索页面的下一页 **/

	private void UpPageForSearch() {
		currentPage--;
		search_Place.goToPoiPage(currentPage); // 跳转到指定页面
	}

	private void nextPageForSearch() {
		currentPage++;
		search_Place.goToPoiPage(currentPage); // 跳转到指定页面
	}

	private void initView() {
		mapView = (MapView) findViewById(R.id.mapView);

		mapView.setBuiltInZoomControls(true); // 启用百度地图的缩放功能
		// mapView.displ
		mapController = mapView.getController();

		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		screenWidth = metric.widthPixels; // 屏幕宽度（像素）
		screenHeight = metric.heightPixels; // 屏幕高度（像素）

		// View view = View.inflate(MainActivity.this, R.layout.serch_result,
		// null);
		// ListView lv = (ListView) view.findViewById(R.id.tv_search_result);
		// MyAdapter adapter = new MyAdapter(MainActivity.this);
		//
		// lv.setAdapter(adapter);
		//
		// view.setVisibility(View.VISIBLE);
		// view.setX(0);
		// view.setY(0);
		// mapView.addView(view);

		// mapController.setZoom(15); // 设置地图初始显示为缩放15倍

		// mapController.setCompassMargin(100, 100);

		// mapController.setCenter(ConstantValue.geoUCAS); // 初始显示地图时设置中心点为国科大
		// search();

		// drawUCAS_with_Text(); // 在地图上用文字标记国科大
		// drawUCAS_with_Circle(); // 在地图上用圆形字标记国科大
		// drawUCAS_with_Image(); // 在地图上用圆形字标记国科大
		// mapView.setTraffic(true); //显示交通图
		// mapView.setSatellite(true); //显示卫星图

		// 初始化Location模块
		// /通过enableProvider和disableProvider方法，选择定位的Provider
		// mLocationManager.enableProvider(MKLocationManager.MK_NETWORK_PROVIDER);
		// mLocationManager.disableProvider(MKLocationManager.MK_GPS_PROVIDER);
		// // 返回手机位置
		// mLocationManager.requestLocationUpdates(locationListener);
		// mLocationManager.setNotifyInternal(5, 2);
		// 添加定位图层
		// MyLocationOverlay myLocationOverlay = new MyLocationOverlay(mapView);
		// ((MyLocationOverlay) myLocationOverlay).getMyLocation(); // 启用定位
		// myLocationOverlay.enableCompass(); // 启用指南针
		//
		// mapView.getOverlays().add(myLocationOverla/y);
		// mapView.setTraffic(true);// 交通地图
		// mapView.setSatellite(true);// 卫星地图
		// mapController.setZoom(15);// 设置缩放级别
		// mapView.invalidate();// 刷新地图

	}

	/** 向百度服务器请求验证KEY **/
	private void checkKey() {
		manager = new BMapManager(getApplicationContext());
		manager.init(ConstantValue.KEY, new MKGeneralListener() {

			@Override
			public void onGetPermissionState(int error) {
				// TODO 授权验证
				if (error == MKEvent.ERROR_PERMISSION_DENIED) {
					Toast.makeText(MainActivity.this, "授权验证失败。", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onGetNetworkState(int error) {
				// TODO 网络验证
				if (error == MKEvent.ERROR_NETWORK_CONNECT) {
					Toast.makeText(MainActivity.this, "网络连接失败，请检查网络状态。", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	/** 在百度地图上加载可显示平面图、路况图和卫星图按钮 **/
	private void addFlagTrafficSatellite() {
		satellite_traffic_map = View.inflate(this, R.layout.satellite_traffic_map, null);

		// 实例化img_Flag
		img_Flag = (ImageView) satellite_traffic_map.findViewById(R.id.img_Flag);
		img_Flag.setImageResource(R.drawable.flag_map);

		// 实例化img_Traffic
		img_Traffic = (ImageView) satellite_traffic_map.findViewById(R.id.img_Traffic);
		img_Traffic.setImageResource(R.drawable.traffic_map);

		// 实例化img_Traffic
		img_Satellite = (ImageView) satellite_traffic_map.findViewById(R.id.img_Satellite);
		img_Satellite.setImageResource(R.drawable.satellite_map);

		// 获取手机屏幕的宽度
		satellite_traffic_map.setVisibility(View.VISIBLE);
		satellite_traffic_map.setX(screenWidth - 130);
		satellite_traffic_map.setY(35);

		// 将satellite_traffic_map显示在mapView上
		mapView.addView(satellite_traffic_map);

		img_Flag.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { // 显示平面图
				mapView.setSatellite(false);
				mapView.setTraffic(false);
			}
		});

		img_Traffic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { // 显示交通路况图
				mapView.setTraffic(true);
				mapView.setSatellite(false);
			}
		});

		img_Satellite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { // 显示卫星图
				mapView.setSatellite(true);
				mapView.setTraffic(false);
			}
		});
	}

	/** 将地图中的国科大用实心圆标记出来 **/
	private void drawUCAS_with_Circle() {
		GraphicsOverlay overlay = new GraphicsOverlay(mapView); // 创建GraphicsOverlay对象
		setCircleData(overlay); // 给overlay对象设置数据格式
		mapView.getOverlays().add(overlay); // 将overlay对象加入到mapView的GraphicsOverlay集合中
		mapView.refresh(); // 刷新mapView，防止卡顿
	}

	private void setCircleData(GraphicsOverlay overlay) {
		Geometry geometry = new Geometry(); // 构造集合图形元素
		geometry.setCircle(ConstantValue.geoUCAS, 125); // 国科大为圆心，半径为125米

		Symbol symbol = new Symbol(); // 构造样式图像
		Symbol.Color color = setColor(symbol, 200, 50, 100, 100);
		symbol.setSurface(color, 1, 0);
		Graphic graphic = new Graphic(geometry, symbol);

		overlay.setData(graphic);
	}

	private Symbol.Color setColor(Symbol symbol, int red, int green, int blue, int alpha) {
		Symbol.Color color = symbol.new Color();
		color.red = red;
		color.green = green;
		color.blue = blue;
		color.alpha = alpha;
		return color;
	}

	private void drawUCAS_with_Text() {
		TextOverlay overlay = new TextOverlay(mapView); // 创建GraphicsOverlay对象
		setTextData(overlay); // 给overlay对象设置数据格式
		mapView.getOverlays().add(overlay); // 将overlay对象加入到mapView的GraphicsOverlay集合中
		mapView.refresh(); // 刷新mapView，防止卡顿
	}

	private void setTextData(TextOverlay overlay) {
		TextItem textItem = new TextItem();
		textItem.align = TextItem.ALIGN_CENTER;
		textItem.fontSize = 40;
		textItem.text = "UCAS";
		textItem.pt = ConstantValue.geoUCAS;
		textItem.typeface = Typeface.DEFAULT_BOLD;
		Symbol symbol = new Symbol();
		Symbol.Color color = setColor(symbol, 100, 20, 200, 100);
		textItem.fontColor = color;

		overlay.addText(textItem);
	}

	private void drawUCAS_with_Image() {
		// Toast.makeText(MainActivity.this, "中国科学院大学",
		// Toast.LENGTH_SHORT).show();
		ItemizedOverlay<OverlayItem> overlay = new ItemizedOverlay<OverlayItem>(
				this.getResources().getDrawable(R.drawable.location), mapView) {

			// @Override
			// /** 地图上的点击事件 **/
			// public boolean onTap(GeoPoint p, MapView m) {
			// // TODO Auto-generated method stub
			// for (int i = 0; i < this.size(); i++) {
			// OverlayItem item = this.getItem(i);
			// if (Math.abs(p.getLatitudeE6() - item.getPoint().getLatitudeE6())
			// < 4000
			// && Math.abs(p.getLongitudeE6() -
			// item.getPoint().getLongitudeE6()) < 4000) {
			// Toast.makeText(MainActivity.this, item.getTitle(),
			// Toast.LENGTH_SHORT).show();
			// }
			// }
			//
			// // Toast.makeText(MainActivity.this, "经度：" + p.getLongitudeE6()
			// // + "，维度：" + p.getLatitudeE6(),
			// // Toast.LENGTH_SHORT).show();
			// // OverlayItem item = this.getItem(1);
			// // Toast.makeText(MainActivity.this,
			// // item.getTitle(),Toast.LENGTH_SHORT).show();
			// return super.onTap(p, m);
			// }

			// @Override
			// public boolean onTap(int index) {
			//// OverlayItem item = this.getItem(index);
			// GeoPoint point=getItem(index).getPoint();
			// // Log.println(1, "MainActivity", "中国科学院大学");
			//// Toast.makeText(MainActivity.this,
			// "中国科学院大学",Toast.LENGTH_SHORT).show();
			// Toast.makeText(MainActivity.this,
			// point.getLatitudeE6()+"",Toast.LENGTH_SHORT).show();
			// // Toast.LENGTH_SHORT).show();
			//// System.out.println("中国科学院大学");
			//// mapView.setSatellite(true);
			// return onTap(index);
			// }
		};

		setImgData(overlay);
		mapView.getOverlays().add(overlay);
		mapView.refresh();
	}

	private void setImgData(ItemizedOverlay<OverlayItem> overlay) {
		OverlayItem item = new OverlayItem(ConstantValue.geoUCAS, "UCAS", "ssss");

		overlay.addItem(item);
		overlay.updateItem(item);
	}

	@Override
	protected void onResume() {
		mapView.onResume(); // 防止再次进入MainActivity地图显示出现问题
		// search();
		// search_Drive_Route();
		// location();
		// mLocationClient.start();
		// search_place();
		super.onResume();
	}

	@Override
	protected void onPause() {
		mapView.onPause(); // 防止再次进入MainActivity地图显示出现问题
		// search();
		// search_Drive_Route();
		// location();
		mLocationClient.stop();// 及时关闭，为省电
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mapView.destroy(); // 防止再次进入MainActivity地图显示出现问题
		super.onDestroy();
	}

	/** 实现连续按两次后退键退出游戏程序 **/
	private int clickCount = 0;
	private long lastClickTime = 0;

	@Override
	public void onBackPressed() {

		if (lastClickTime <= 0) {
			Toast.makeText(this, "Press again to exit!", Toast.LENGTH_SHORT).show();
			lastClickTime = System.currentTimeMillis();
		} else {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - lastClickTime) < 1000) {
				finish();
			} else {
				lastClickTime = currentTime;
			}
		}
	}
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