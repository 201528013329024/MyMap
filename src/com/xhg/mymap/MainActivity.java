/**
 * @author Haoguang Xu
 * Copyright (c) 2016, UCAS
 * All rights reserved. 
 * 
 * MainActivity Class {@link MainActivity}  
 */

package com.xhg.mymap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/*定位需要引用的包*/
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.PoiOverlay;
import com.baidu.mapapi.map.Symbol;
import com.baidu.mapapi.map.TextItem;
import com.baidu.mapapi.map.TextOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.xhg.route_result.RouteShowActivity;
import com.xhg.search_result.MyAdapter;
import com.xhg.search_result.SearchLableActivity;
import com.xhg.search_result.Search_Place_Result_ListCell;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private BMapManager manager; // 地图引擎的管理工具，主要用来验证百度key
	private MapView mapView; // 用于显示地图的View
	private MapController mapController; // mapController用于控制地图的平移，缩放和旋转

	public int screenWidth = 0; // 屏幕宽度
	public int screenHeight = 0;// 屏幕高度

	private View ui_right_icon;
	private ImageView img_Flag, img_Traffic, img_Satellite, img_Loc, img_route;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE); // 去除屏幕的Title栏
		checkKey();// 必须在设定Layout前验证百度key
		setContentView(R.layout.activity_main);
		initView(); // 初始化界面
		location(); // 进入应用后就开始启动定位
		addUIRightIcon(); // 显示UI右侧的图标
		addSearchIcon(); // 显示搜索图标
	}

	/** 在主UI界面上添加搜索图标 **/
	private View search_icon = null; // 主界面上的搜索图标
	private EditText editText = null;

	private void addSearchIcon() {
		search_icon = View.inflate(this, R.layout.search_icon, null);

		search_icon.setVisibility(View.VISIBLE);

		search_icon.setX((screenWidth - dip2px(MainActivity.this, 327)) / 2); // 横向居中显示
		search_icon.setY(15);

		mapView.addView(search_icon);

		editText = (EditText) search_icon.findViewById(R.id.editText1);
		ImageView imgShare = (ImageView) search_icon.findViewById(R.id.imageShare); // 分享位置图标

		editText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 进入SearchLableActivity
				Intent intent = new Intent();
				intent.putExtra("myCity", myCity);
				intent.setClass(MainActivity.this, SearchLableActivity.class);
				setResult(RESULT_OK, intent);
				MainActivity.this.startActivityForResult(intent, 0);
			}
		});

		imgShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				shareMyAdress(); // 分享我的位置
			}
		});
	}

	/** Intent返回后执行的操作 **/
	public int Label = R.id.btn0;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			String searchContent = data.getExtras().getString("result"); // 获取搜索内容

			Label = data.getIntExtra("search_label", R.drawable.search_lbel_default); // 获取搜索内容的label编号，主要用于根据搜索内容的不同显示不同图片

			String range = data.getExtras().getString("range");
			String area_or_boundary = null;
			
			if (range.equals("searchByArea")) {
				area_or_boundary = data.getExtras().getString("search_area");
			} else if (range.equals("searchByBoundary")) {
				area_or_boundary = data.getExtras().getString("search_boundary");
			}

			search_place(searchContent, range, area_or_boundary); // 进入地点搜索函数

			break;
		default:
			break;
		}
	}

	/** dip(dp)与px之间单位转换 **/
	public int dip2px(Context context, float dipValue) {
		float m = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * m + 0.5f);
	}

	/** 弹出路线搜索窗口，用于输入起点和终点 **/
	public void prompt() {
		View routeSearchDialog = View.inflate(this, R.layout.route_search_dialog, null);
		final EditText inputStart = (EditText) routeSearchDialog.findViewById(R.id.editStart);
		final EditText inputEnd = (EditText) routeSearchDialog.findViewById(R.id.editEnd);
		routeSearchDialog.setVisibility(View.VISIBLE);

		View dailogTitle = View.inflate(this, R.layout.route_search_dialog_title, null);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_menu_search);
		builder.setTitle("路线查询");
		builder.setCustomTitle(dailogTitle);
		builder.setView(routeSearchDialog);
		builder.setNegativeButton("取消", null);

		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String start = inputStart.getText().toString();
				String end = inputEnd.getText().toString();
				if (start.equals("") || end.equals("")) {
					Toast.makeText(MainActivity.this, "请输入起点和终点", Toast.LENGTH_SHORT).show();
				} else {

					// 跳转到RouteShowActivity
					Intent intent = new Intent();

					intent.putExtra("mode", 0); // 0表示通过Dialog进入RouteShowActivity，
												// 1表示通过serach_icon进入RouteShowActivity
					intent.putExtra("myCity", myCity);
					intent.putExtra("start_place", start);
					intent.putExtra("end_place", end);

					intent.setClass(MainActivity.this, RouteShowActivity.class);
					MainActivity.this.startActivityForResult(intent, 1);
				}
			}
		});
		builder.show();
	}

	/** 定位 **/
	// LocationClient: 定位服务
	public LocationClient mLocationClient = null;
	public BDLocationListener loctionListener = new MyLocationListener();
	public GeoPoint MyPoint = null; // 我的地点
	public MyLocationOverlay locationOverlay = null;

	private void location() {
		mLocationClient = new LocationClient(getApplicationContext());
		mLocationClient.start();
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

	/** 自定义的LocationListener **/
	public String myAddr = null;
	public String myCity = "北京";

	private class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}

			locationOverlay = new MyLocationOverlay(mapView);
			LocationData data = new LocationData();
			if (location.getAddrStr() == null) {
				Toast.makeText(MainActivity.this, "定位失败,请检查网络状态", Toast.LENGTH_SHORT).show();
			} else {
				// location.get
				myAddr = location.getAddrStr();
				myCity = location.getCity();
				Toast.makeText(MainActivity.this, "您的位置：" + myAddr, Toast.LENGTH_SHORT).show();

				// MKPoiInfo info = new MKPoiInfo();
				// info.city = location.getCity();
				// info.

				// MKCityListInfo m = new MKCityListInfo();
				// m.
			}

			data.latitude = location.getLatitude();
			data.longitude = location.getLongitude();

			locationOverlay.enableCompass(); // 启用指南针
			locationOverlay.isCompassEnable();

			locationOverlay.setData(data);
			mapView.getOverlays().add(locationOverlay);

			MyPoint = new GeoPoint((int) (data.latitude * 1E6), (int) (data.longitude * 1E6));

			mapController.setZoom(15);
			mapController.animateTo(MyPoint);
			mapView.refresh();
		}

		@Override
		public void onReceivePoi(BDLocation arg0) {
			// TODO Auto-generated method stub

		}
	}

	// MKSearch：搜索服务，用于位置检索、周边检索、范围检索、公交检索、驾乘检索、步行检索
	private MKSearch search_Place;

	// MKSearchListener：搜索监视器，返回poi搜索,公交搜索,驾乘路线,步行路线结果
	private MKSearchListener listener_Place;
	public View search_place_layout = null;
	public ListView search_place_lv = null;
	public Button btn_UpPage = null;
	public Button btn_NextPage = null;
	public Button btn_PageInfo = null;
	public PoiOverlay search_Place_Overlay = null;

	public int currentPage = 0;
	public int numPage = 0;
	public ViewGroup.LayoutParams params = null;

	public MyAdapter adapter = null; // 自定义的用于显示搜索场所结果的适配器

	/**
	 * 场所搜索
	 * 
	 * @param area_or_boundary
	 * @param range
	 **/
	private void search_place(String serchContent, String range, String area_or_boundary) {
		search_Place = new MKSearch();
		listener_Place = new MyMKSearchListener() {
			public void onGetPoiResult(MKPoiResult result, int type, int error) {

				if (error == 0) {
					if (result != null) {
						if (result.getNumPois() >= 10) {
							search_Place_Overlay = new PoiOverlay(MainActivity.this, mapView);
							setData(search_Place_Overlay, result);
							mapView.getOverlays().clear();
							mapView.getOverlays().add(search_Place_Overlay);
							mapView.refresh();
						} else {
							Toast.makeText(MainActivity.this, "没有查询到相关信息", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(MainActivity.this, "没有查询到相关信息", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(MainActivity.this, "没有查询到相关信息", Toast.LENGTH_SHORT).show();
				}
			}

			@SuppressLint("NewApi")
			private void setData(PoiOverlay overlay, MKPoiResult result) {

				ArrayList<MKPoiInfo> mkPoiInfos = result.getAllPoi();

				adapter = new MyAdapter(MainActivity.this);
				adapter.search_label = Label;

				for (int i = 0; i < 10; i++) {
					MKPoiInfo info = result.getPoi(i);
					String placeName = info.name;
					String placeAdress = info.address;
					String placePhone = info.phoneNum != "" ? info.phoneNum : "暂无电话信息";
					int placeDistance = (int) DistanceUtil.getDistance(MyPoint, info.pt);
					adapter.data[i] = new Search_Place_Result_ListCell(placeName, placeAdress, placePhone,
							placeDistance);
				}

				if (search_place_layout != null)
					mapView.removeViewInLayout(search_place_layout);

				search_place_layout = View.inflate(MainActivity.this, R.layout.serch_result, null);
				search_place_lv = (ListView) search_place_layout.findViewById(R.id.tv_search_result);
				btn_UpPage = (Button) search_place_layout.findViewById(R.id.btn_UpPage);
				btn_NextPage = (Button) search_place_layout.findViewById(R.id.btn_NextPage);
				btn_PageInfo = (Button) search_place_layout.findViewById(R.id.btn_PageInfo);

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

				params = search_place_lv.getLayoutParams();

				btn_PageInfo.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						search_place_layout.setVisibility(View.INVISIBLE);
						show_Close_Search_Result();
					}
				});

				search_place_lv.setAdapter(adapter);

				params.width = screenWidth;
				params.height = screenHeight / 2;

				search_place_lv.setLayoutParams(params);

				search_place_layout.setVisibility(View.VISIBLE);

				search_place_layout.setY(params.height - dip2px(MainActivity.this, 62));

				mapView.addView(search_place_layout);

				overlay.setData(mkPoiInfos);

				mapController.animateTo(result.getPoi(0).pt);
				mapController.setZoom(10);

				//// ((search_place_lv.getChildAt(0).findViewById(R.id.img_go_to_place))).setOnClickListener(new
				//// View.OnClickListener() {
				//
				// @Override
				// public void onClick(View v) {
				// // TODO Auto-generated method stub
				// Toast.makeText(MainActivity.this, "当前已经是最后一页",
				//// Toast.LENGTH_SHORT).show();
				// }
				//
				//
				// });

				search_place_lv.setOnItemClickListener(new ListView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						Intent intent = new Intent();
						int latitude = MyPoint.getLatitudeE6();
						int longitude = MyPoint.getLongitudeE6();

						String endPlaceName = adapter.data[position].getPlace_name();
						intent.putExtra("mode", 1); // 通过listVIew进入搜索
						intent.putExtra("start_place_latitude", latitude);
						intent.putExtra("start_place_longitude", longitude);
						intent.putExtra("end_place", endPlaceName);
						intent.putExtra("myCity", myCity);
						intent.setClass(MainActivity.this, RouteShowActivity.class);
						MainActivity.this.startActivity(intent);
					}
				});

			}

		};

		search_Place.init(manager, listener_Place);

		if (range.equals("searchByArea")) {
			search_Place.poiSearchInCity(area_or_boundary, serchContent);
		} else if (range.equals("searchByBoundary")) {
			if (area_or_boundary.equals(""))
				area_or_boundary = "50000";
			search_Place.poiSearchNearBy(serchContent, MyPoint, Integer.parseInt(area_or_boundary));
		}
	}

	/** 跳转到搜索页面的上一页 **/
	private void UpPageForSearch() {
		currentPage--;
		search_Place.init(manager, listener_Place);
		search_Place.goToPoiPage(currentPage); // 跳转到指定页面
	}

	/** 跳转到搜索页面的上一页 **/
	private void nextPageForSearch() {
		currentPage++;
		search_Place.init(manager, listener_Place);
		search_Place.goToPoiPage(currentPage); // 跳转到指定页面
	}

	/** 搜索结果的layout显示在底部 **/
	public View view_close_search_result = null;

	private void show_Close_Search_Result() {
		view_close_search_result = View.inflate(MainActivity.this, R.layout.close_search_result, null);
		ListView lv = (ListView) view_close_search_result.findViewById(R.id.lv);
		ViewGroup.LayoutParams params = lv.getLayoutParams();
		params.width = screenWidth;
		lv.setLayoutParams(params);
		view_close_search_result.setY(screenHeight - dip2px(MainActivity.this, 62));

		Button btn_Show = (Button) view_close_search_result.findViewById(R.id.btn_Show);
		Button btn_Close = (Button) view_close_search_result.findViewById(R.id.btn_Close);
		Button btn_Info = (Button) view_close_search_result.findViewById(R.id.btn);
		btn_Info.setText(btn_PageInfo.getText());

		btn_Show.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				search_place_layout.setVisibility(View.VISIBLE);
				view_close_search_result.setVisibility(View.INVISIBLE);
			}
		});

		btn_Info.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				search_place_layout.setVisibility(View.VISIBLE);
				view_close_search_result.setVisibility(View.INVISIBLE);
			}
		});

		btn_Close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mapView.getOverlays().clear();
				view_close_search_result.setVisibility(View.INVISIBLE);
				mapView.removeViewInLayout(view_close_search_result);
				mapView.removeViewInLayout(search_place_layout);
			}
		});

		mapView.addView(view_close_search_result);
	}

	/** 主UI初始化 **/
	private void initView() {
		mapView = (MapView) findViewById(R.id.mapView);

		mapView.setBuiltInZoomControls(true); // 启用百度地图的缩放功能
		mapController = mapView.getController();

		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		screenWidth = metric.widthPixels; // 屏幕宽度（像素）
		screenHeight = metric.heightPixels; // 屏幕高度（像素）
		mapView.regMapViewListener(manager, mapViewListener);

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

	/** 在主UI显示右侧图标 **/
	private void addUIRightIcon() {
		ui_right_icon = View.inflate(this, R.layout.satellite_traffic_map, null);

		img_Flag = (ImageView) ui_right_icon.findViewById(R.id.img_Flag);// 实例化img_Flag
		img_Traffic = (ImageView) ui_right_icon.findViewById(R.id.img_Traffic);// 实例化img_Traffic
		img_Satellite = (ImageView) ui_right_icon.findViewById(R.id.img_Satellite);// 实例化img_Traffic
		img_Loc = (ImageView) ui_right_icon.findViewById(R.id.img_Loc);// 定位图标
		img_route = (ImageView) ui_right_icon.findViewById(R.id.img_route);// 路线查找图标

		ui_right_icon.setVisibility(View.VISIBLE);
		ui_right_icon.setX(screenWidth - dip2px(MainActivity.this, 40) - 20);
		ui_right_icon.setY(50);

		mapView.addView(ui_right_icon);

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

		img_Loc.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 调用两次location()是为了加快定位速度
				location();
				location();

			}
		});

		img_route.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				prompt(); // 弹出输入路线查找的起点和终点对话框
			}
		});
	}

	@Override
	protected void onResume() {

		mapView.onResume(); // 防止再次进入MainActivity地图显示出现问题
		super.onResume();
	}

	@Override
	protected void onPause() {
		mapView.onPause(); // 防止再次进入MainActivity地图显示出现问题
		mLocationClient.stop();// 及时关闭，为省电
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mapView.destroy(); // 防止再次进入MainActivity地图显示出现问题
		super.onDestroy();
	}

	/** 实现连续按两次后退键退出游戏程序 **/
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

	/** 分享我的位置 **/

	public void shareMyAdress() {
		TextOverlay textOverlay = new TextOverlay(mapView);
		setData(textOverlay);
		mapView.getOverlays().add(textOverlay);

		Toast.makeText(MainActivity.this, "正在生成分享内容...", Toast.LENGTH_SHORT).show();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mapView.getCurrentMap();
	}

	private void setData(TextOverlay textOverlay) {
		TextItem text = new TextItem();
		text.align = TextItem.ALIGN_BOTTOM;
		text.fontSize = 50;
		text.pt = MyPoint;
		Symbol symbol = new Symbol();
		Symbol.Color color = symbol.new Color();
		color.red = 255;
		color.blue = 0;
		color.green = 0;
		color.alpha = 200;
		text.fontColor = color;
		text.typeface = Typeface.DEFAULT;
		text.text = "来自IMap的位置分享\\我在  " + myAddr + "  附近";
		textOverlay.addText(text);
	}

	public MKMapViewListener mapViewListener = new MKMapViewListener() {

		@Override
		public void onClickMapPoi(MapPoi arg0) {
			// TODO Auto-generated method stub
		}

		// 当调用过 mMapView.getCurrentMap()后，此回调会被触发 可在此保存截图至存储设备
		@Override
		public void onGetCurrentMap(Bitmap b) {

			if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				Toast.makeText(MainActivity.this, "请插入sd卡", Toast.LENGTH_SHORT).show();
				return;
			}

			String pathx = Environment.getExternalStorageDirectory().toString() + "/XMap/shareAdress/";
			File filex = new File(pathx);
			if (!(filex.exists())) {
				filex.mkdirs();// 创建文件夹
			}

			File file = new File(pathx, System.currentTimeMillis() + ".png");

			FileOutputStream out;

			try {
				out = new FileOutputStream(file);
				if (b.compress(Bitmap.CompressFormat.PNG, 0, out)) {
					// out.flush();
					out.close();
				}

				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.setType("image/*");
				Uri uri = Uri.fromFile(new File(file.toString()));
				shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
				startActivity(Intent.createChooser(shareIntent, "分享我的位置"));

				mapView.getOverlays().clear();
				mapView.getOverlays().add(locationOverlay);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onMapAnimationFinish() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMapMoveFinish() {
			// TODO Auto-generated method stub

		}
	};

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