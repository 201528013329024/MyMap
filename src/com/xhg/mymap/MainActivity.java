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
/*��λ��Ҫ���õİ�*/
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

	private BMapManager manager; // ��ͼ����Ĺ�����
	private MapView mapView; // ��ͼ��ʾ����
	private MapController mapController; // mapController���ڿ��Ƶ�ͼ��ƽ�ƣ����ź���ת

	private View satellite_traffic_map, location_icon, search_icon;
	private ImageView img_Flag, img_Traffic, img_Satellite, img_Loc;
	private EditText editText;

	// ���������������ڳ�������������ʳ������վ��
	private MKSearch search_Place, search_Drive, search_Walk, search_Transit;
	private MKSearchListener listener_Place, listener_Drive, listener_Walk, listener_Transit;

	public LocationClient mLocationClient = null;
	public BDLocationListener loctionListener = new MyLocationListener();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// �������趨Layoutǰ��֤�ٶ�key
		checkKey();

		setContentView(R.layout.activity_main);

		initView();

		addFlagTrafficSatellite(); // ���ؿ���ʾƽ��ͼ��·��ͼ������ͼ��ť

		location();
		addLocation();
		addSearchIcon();

		// search_Drive_Route();// �����ݳ�·��

		// search_Walk_Route();// ��������·��
		// search_Transit_Route();// ��������·��

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
		int width = metric.widthPixels; // ��Ļ��ȣ����أ�

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

		// ʵ����img_Flag
		img_Loc = (ImageView) location_icon.findViewById(R.id.img_Loc);
		img_Loc.setImageResource(R.drawable.img_loc);

		location_icon.setVisibility(View.VISIBLE);

		// ��ȡ�ֻ���Ļ�Ŀ��

		location_icon.setX(screenWidth - 160);
//		location_icon.setY(height - dip2px(MainActivity.this, 70));
		location_icon.setY(800);

		// ��satellite_traffic_map��ʾ��mapView��
		mapView.addView(location_icon);

		location_icon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { // ��ʾƽ��ͼ

				// ��������location()��Ϊ�˼ӿ춨λ�ٶ�
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
		option.setAddrType("all");// ���صĶ�λ���������ַ��Ϣ
		option.setCoorType("bd09ll");// ���صĶ�λ����ǰٶȾ�γ��,Ĭ��ֵgcj02
		// option.setScanSpan(5000);//���÷���λ����ļ��ʱ��Ϊ5000ms
		option.disableCache(true);// ��ֹ���û��涨λ
		option.setPoiNumber(5); // ��෵��POI����
		option.setPoiDistance(1000); // poi��ѯ����
		option.setPoiExtraInfo(true); // �Ƿ���ҪPOI�ĵ绰�͵�ַ����ϸ��Ϣ
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
					Toast.makeText(MainActivity.this, "�ٰ�һ�κ��˼��˳���ͼ��", Toast.LENGTH_SHORT).show();
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
		mapView.setTraffic(true); // Ҫ�ڵ�ͼ����ʾ�Ƽ���ʻ·�ߣ����뽫��ͼ����Ϊ·��ģʽ
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
					Toast.makeText(MainActivity.this, "û�в�ѯ���", Toast.LENGTH_SHORT).show();
				}

			}

			private void setData(TransitOverlay overlay, MKTransitRouteResult result) {
				if (result.getNumPlan() > 0) { // ��ʾ���й���·��
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

		String city = "����";

		MKPlanNode start_place = new MKPlanNode();
		start_place.name = "�찲��"; // Ҳ�����õ��������ʾ
		// start_place.pt = ConstantValue.geoUCAS;

		MKPlanNode end_place = new MKPlanNode();
		end_place.name = "������ѧ";// Ҳ�����õ��������ʾ

		search_Transit.transitSearch(city, start_place, end_place);
	}

	private void search_Walk_Route() {
		mapView.setTraffic(true); // Ҫ�ڵ�ͼ����ʾ�Ƽ���ʻ·�ߣ����뽫��ͼ����Ϊ·��ģʽ
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
					Toast.makeText(MainActivity.this, "û�в�ѯ���", Toast.LENGTH_SHORT).show();
				}
				super.onGetWalkingRouteResult(result, error);
			}

			private void setData(RouteOverlay overlay, MKWalkingRouteResult result) {
				if (result.getNumPlan() > 0) {
					MKRoutePlan plan = result.getPlan(0);
					MKRoute route = plan.getRoute(0); // �˴�Ĭ�ϲ���Ϊ0
					overlay.setData(route);
				} else {
					Toast.makeText(MainActivity.this, "XXXX", Toast.LENGTH_SHORT).show();
				}
			}
		};

		search_Walk.init(manager, listener_Walk);

		String start_city = "����";
		String end_city = "����";

		MKPlanNode start_place = new MKPlanNode();
		start_place.name = "�찲��"; // Ҳ�����õ��������ʾ
		// start_place.pt = ConstantValue.geoUCAS;

		MKPlanNode end_place = new MKPlanNode();
		end_place.name = "������ѧ";// Ҳ�����õ��������ʾ

		search_Walk.walkingSearch(start_city, start_place, end_city, end_place);
	}

	private void search_Drive_Route() {
		mapView.setTraffic(true); // Ҫ�ڵ�ͼ����ʾ�Ƽ���ʻ·�ߣ����뽫��ͼ����Ϊ·��ģʽ
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
					Toast.makeText(MainActivity.this, "û�в�ѯ���", Toast.LENGTH_SHORT).show();
				}
			}

			private void setData(RouteOverlay overlay, MKDrivingRouteResult result) {
				if (result.getNumPlan() > 0) {
					MKRoutePlan plan = result.getPlan(0);
					MKRoute route = plan.getRoute(0); // �˴�Ĭ�ϲ���Ϊ0
					overlay.setData(route);
				} else {
					Toast.makeText(MainActivity.this, "XXXX", Toast.LENGTH_SHORT).show();
				}
			}
		};

		search_Drive.init(manager, listener_Drive);

		String start_city = "����";
		String end_city = "����";

		MKPlanNode start_place = new MKPlanNode();
		start_place.name = "�찲��"; // Ҳ�����õ��������ʾ
		// start_place.pt = ConstantValue.geoUCAS;

		MKPlanNode end_place = new MKPlanNode();
		end_place.name = "������ѧ";// Ҳ�����õ��������ʾ

		// search_Drive.setDrivingPolicy(MKSearch.EBUS_TIME_FIRST); //�ݳ�ɸѡ����

		search_Drive.drivingSearch(start_city, start_place, end_city, end_place);

		ArrayList<MKWpNode> nodes = new ArrayList<MKWpNode>();
		MKWpNode node = new MKWpNode();
		node.city = "����";
		node.name = "�й�����ѧԺ";
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

	/** �������� **/
	private void search_place(String serchContent) {
		// Toast.makeText(MainActivity.this, "����search_place",
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
					Toast.makeText(MainActivity.this, "û�в�ѯ�������Ϣ", Toast.LENGTH_SHORT).show();
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
					String placePhone = info.phoneNum != "" ? info.phoneNum : "���޵绰��Ϣ";
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
							Toast.makeText(MainActivity.this, "��ǰ�Ѿ��ǵ�һҳ", Toast.LENGTH_SHORT).show();
						} else {
							UpPageForSearch();
						}
					}
				});

				btn_NextPage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (currentPage == numPage) {
							Toast.makeText(MainActivity.this, "��ǰ�Ѿ������һҳ", Toast.LENGTH_SHORT).show();
						} else {
							nextPageForSearch();
						}
					}
				});

				btn_PageInfo.setText("���ҵ�" + result.getNumPois() + "�������");

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
				String string = "��ǰҳ" + result.getPageIndex() + "/��ҳ��" + result.getNumPages() + "��   ��ǰ��Ŀ"
						+ result.getCurrentNumPois() + "/����Ŀ" + result.getNumPois();

			};
		};

		search_Place.init(manager, listener_Place);
		// search.poiSearchNearBy("ҽԺ", ConstantValue.geoUCAS, 10000);
		search_Place.poiSearchInCity("����", serchContent);
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

	/** ��ת������ҳ�����һҳ **/

	private void UpPageForSearch() {
		currentPage--;
		search_Place.goToPoiPage(currentPage); // ��ת��ָ��ҳ��
	}

	private void nextPageForSearch() {
		currentPage++;
		search_Place.goToPoiPage(currentPage); // ��ת��ָ��ҳ��
	}

	private void initView() {
		mapView = (MapView) findViewById(R.id.mapView);

		mapView.setBuiltInZoomControls(true); // ���ðٶȵ�ͼ�����Ź���
		// mapView.displ
		mapController = mapView.getController();

		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		screenWidth = metric.widthPixels; // ��Ļ��ȣ����أ�
		screenHeight = metric.heightPixels; // ��Ļ�߶ȣ����أ�

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

		// mapController.setZoom(15); // ���õ�ͼ��ʼ��ʾΪ����15��

		// mapController.setCompassMargin(100, 100);

		// mapController.setCenter(ConstantValue.geoUCAS); // ��ʼ��ʾ��ͼʱ�������ĵ�Ϊ���ƴ�
		// search();

		// drawUCAS_with_Text(); // �ڵ�ͼ�������ֱ�ǹ��ƴ�
		// drawUCAS_with_Circle(); // �ڵ�ͼ����Բ���ֱ�ǹ��ƴ�
		// drawUCAS_with_Image(); // �ڵ�ͼ����Բ���ֱ�ǹ��ƴ�
		// mapView.setTraffic(true); //��ʾ��ͨͼ
		// mapView.setSatellite(true); //��ʾ����ͼ

		// ��ʼ��Locationģ��
		// /ͨ��enableProvider��disableProvider������ѡ��λ��Provider
		// mLocationManager.enableProvider(MKLocationManager.MK_NETWORK_PROVIDER);
		// mLocationManager.disableProvider(MKLocationManager.MK_GPS_PROVIDER);
		// // �����ֻ�λ��
		// mLocationManager.requestLocationUpdates(locationListener);
		// mLocationManager.setNotifyInternal(5, 2);
		// ��Ӷ�λͼ��
		// MyLocationOverlay myLocationOverlay = new MyLocationOverlay(mapView);
		// ((MyLocationOverlay) myLocationOverlay).getMyLocation(); // ���ö�λ
		// myLocationOverlay.enableCompass(); // ����ָ����
		//
		// mapView.getOverlays().add(myLocationOverla/y);
		// mapView.setTraffic(true);// ��ͨ��ͼ
		// mapView.setSatellite(true);// ���ǵ�ͼ
		// mapController.setZoom(15);// �������ż���
		// mapView.invalidate();// ˢ�µ�ͼ

	}

	/** ��ٶȷ�����������֤KEY **/
	private void checkKey() {
		manager = new BMapManager(getApplicationContext());
		manager.init(ConstantValue.KEY, new MKGeneralListener() {

			@Override
			public void onGetPermissionState(int error) {
				// TODO ��Ȩ��֤
				if (error == MKEvent.ERROR_PERMISSION_DENIED) {
					Toast.makeText(MainActivity.this, "��Ȩ��֤ʧ�ܡ�", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onGetNetworkState(int error) {
				// TODO ������֤
				if (error == MKEvent.ERROR_NETWORK_CONNECT) {
					Toast.makeText(MainActivity.this, "��������ʧ�ܣ���������״̬��", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	/** �ڰٶȵ�ͼ�ϼ��ؿ���ʾƽ��ͼ��·��ͼ������ͼ��ť **/
	private void addFlagTrafficSatellite() {
		satellite_traffic_map = View.inflate(this, R.layout.satellite_traffic_map, null);

		// ʵ����img_Flag
		img_Flag = (ImageView) satellite_traffic_map.findViewById(R.id.img_Flag);
		img_Flag.setImageResource(R.drawable.flag_map);

		// ʵ����img_Traffic
		img_Traffic = (ImageView) satellite_traffic_map.findViewById(R.id.img_Traffic);
		img_Traffic.setImageResource(R.drawable.traffic_map);

		// ʵ����img_Traffic
		img_Satellite = (ImageView) satellite_traffic_map.findViewById(R.id.img_Satellite);
		img_Satellite.setImageResource(R.drawable.satellite_map);

		// ��ȡ�ֻ���Ļ�Ŀ��
		satellite_traffic_map.setVisibility(View.VISIBLE);
		satellite_traffic_map.setX(screenWidth - 130);
		satellite_traffic_map.setY(35);

		// ��satellite_traffic_map��ʾ��mapView��
		mapView.addView(satellite_traffic_map);

		img_Flag.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { // ��ʾƽ��ͼ
				mapView.setSatellite(false);
				mapView.setTraffic(false);
			}
		});

		img_Traffic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { // ��ʾ��ͨ·��ͼ
				mapView.setTraffic(true);
				mapView.setSatellite(false);
			}
		});

		img_Satellite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { // ��ʾ����ͼ
				mapView.setSatellite(true);
				mapView.setTraffic(false);
			}
		});
	}

	/** ����ͼ�еĹ��ƴ���ʵ��Բ��ǳ��� **/
	private void drawUCAS_with_Circle() {
		GraphicsOverlay overlay = new GraphicsOverlay(mapView); // ����GraphicsOverlay����
		setCircleData(overlay); // ��overlay�����������ݸ�ʽ
		mapView.getOverlays().add(overlay); // ��overlay������뵽mapView��GraphicsOverlay������
		mapView.refresh(); // ˢ��mapView����ֹ����
	}

	private void setCircleData(GraphicsOverlay overlay) {
		Geometry geometry = new Geometry(); // ���켯��ͼ��Ԫ��
		geometry.setCircle(ConstantValue.geoUCAS, 125); // ���ƴ�ΪԲ�ģ��뾶Ϊ125��

		Symbol symbol = new Symbol(); // ������ʽͼ��
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
		TextOverlay overlay = new TextOverlay(mapView); // ����GraphicsOverlay����
		setTextData(overlay); // ��overlay�����������ݸ�ʽ
		mapView.getOverlays().add(overlay); // ��overlay������뵽mapView��GraphicsOverlay������
		mapView.refresh(); // ˢ��mapView����ֹ����
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
		// Toast.makeText(MainActivity.this, "�й���ѧԺ��ѧ",
		// Toast.LENGTH_SHORT).show();
		ItemizedOverlay<OverlayItem> overlay = new ItemizedOverlay<OverlayItem>(
				this.getResources().getDrawable(R.drawable.location), mapView) {

			// @Override
			// /** ��ͼ�ϵĵ���¼� **/
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
			// // Toast.makeText(MainActivity.this, "���ȣ�" + p.getLongitudeE6()
			// // + "��ά�ȣ�" + p.getLatitudeE6(),
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
			// // Log.println(1, "MainActivity", "�й���ѧԺ��ѧ");
			//// Toast.makeText(MainActivity.this,
			// "�й���ѧԺ��ѧ",Toast.LENGTH_SHORT).show();
			// Toast.makeText(MainActivity.this,
			// point.getLatitudeE6()+"",Toast.LENGTH_SHORT).show();
			// // Toast.LENGTH_SHORT).show();
			//// System.out.println("�й���ѧԺ��ѧ");
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
		mapView.onResume(); // ��ֹ�ٴν���MainActivity��ͼ��ʾ��������
		// search();
		// search_Drive_Route();
		// location();
		// mLocationClient.start();
		// search_place();
		super.onResume();
	}

	@Override
	protected void onPause() {
		mapView.onPause(); // ��ֹ�ٴν���MainActivity��ͼ��ʾ��������
		// search();
		// search_Drive_Route();
		// location();
		mLocationClient.stop();// ��ʱ�رգ�Ϊʡ��
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mapView.destroy(); // ��ֹ�ٴν���MainActivity��ͼ��ʾ��������
		super.onDestroy();
	}

	/** ʵ�����������κ��˼��˳���Ϸ���� **/
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