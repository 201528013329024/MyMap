/**
 * @author Haoguang Xu
 * Copyright (c) 2016, UCAS
 * All rights reserved. 
 * 
 * MainActivity Class {@link MainActivity}  
 */

package com.xhg.mymap;

import java.util.ArrayList;
import java.util.Map;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.Geometry;
import com.baidu.mapapi.map.Graphic;
import com.baidu.mapapi.map.GraphicsOverlay;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
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
import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private BMapManager manager; // 地图引擎的管理工具
	private MapView mapView; // 地图显示容器
	private MapController mapController; // mapController用于控制地图的平移，缩放和旋转

	private View satellite_traffic_map;
	private ImageView img_Flag, img_Traffic, img_Satellite;

	// 下面两个变量用于场所搜索，如美食、加油站等
	private MKSearch search;
	private MKSearchListener listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 必须在设定Layout前验证百度key
		checkKey();

		setContentView(R.layout.activity_main);

		initView();

		addFlagTrafficSatellite();

		search();
	}

	/** 场所搜索 **/
	private void search() {
		// Toast.makeText(MainActivity.this, "已进入Search（）方法",
		// Toast.LENGTH_SHORT).show();
		search = new MKSearch();
		listener = new MyMKSearchListener() {
			@SuppressLint("NewApi")
			public void onGetPoiResult(MKPoiResult result, int type, int error) {
				if (error == 0) {
					if (result != null) {
						PoiOverlay overlay = new PoiOverlay(MainActivity.this, mapView);
						setData(overlay, result);
						mapView.getOverlays().clear();
						mapView.getOverlays().add(overlay);
						mapView.refresh();
					}
				} else {
					Toast.makeText(MainActivity.this, "没有查询到相关信息", Toast.LENGTH_SHORT).show();
					;
				}
			}

			private void setData(PoiOverlay overlay, MKPoiResult result) {
				ArrayList<MKPoiInfo> mkPoiInfos = result.getAllPoi();
				overlay.setData(mkPoiInfos);

				String string = "当前页" + result.getPageIndex() + "/总页数" + result.getNumPages() + "，   当前条目"
						+ result.getCurrentNumPois() + "/总条目" + result.getNumPois();
				Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
			};
		};
		search.init(manager, listener);
		// search.poiSearchNearBy("医院", ConstantValue.geoUCAS, 10000);
		search.poiSearchInCity("北京", "加油站");
	}

	/** 跳转到搜索页面的下一页  **/
	private int currentPage = 0;
	private void nextPageForSearch() {
		currentPage++; 
		search.goToPoiPage(currentPage); //跳转到指定页面
	}

	private void initView() {
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true); // 启用百度地图的缩放功能
		// mapView.displ
		mapController = mapView.getController();

		mapController.setZoom(15); // 设置地图初始显示为缩放15倍

		mapController.setCompassMargin(100, 100);

		mapController.setCenter(ConstantValue.geoUCAS); // 初始显示地图时设置中心点为国科大
		// search();

		// drawUCAS_with_Text(); // 在地图上用文字标记国科大
		// drawUCAS_with_Circle(); // 在地图上用圆形字标记国科大
		drawUCAS_with_Image(); // 在地图上用圆形字标记国科大
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
		WindowManager wm = this.getWindowManager();
		int width = wm.getDefaultDisplay().getWidth();

		// 设置satellite_traffic_map的显示位置
		satellite_traffic_map.setX(width - 130);
		satellite_traffic_map.setY(0);

		// 将satellite_traffic_map显示在mapView上
		mapView.addView(satellite_traffic_map);

		img_Flag.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { // 显示平面图
//				mapView.setSatellite(false);
//				mapView.setTraffic(false);
				nextPageForSearch();
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

			@Override
			/** 地图上的点击事件 **/
			public boolean onTap(GeoPoint p, MapView m) {
				// TODO Auto-generated method stub
				for (int i = 0; i < this.size(); i++) {
					OverlayItem item = this.getItem(i);
					if (Math.abs(p.getLatitudeE6() - item.getPoint().getLatitudeE6()) < 4000
							&& Math.abs(p.getLongitudeE6() - item.getPoint().getLongitudeE6()) < 4000) {
						Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
					}
				}

				// Toast.makeText(MainActivity.this, "经度：" + p.getLongitudeE6()
				// + "，维度：" + p.getLatitudeE6(),
				// Toast.LENGTH_SHORT).show();
				// OverlayItem item = this.getItem(1);
				// Toast.makeText(MainActivity.this,
				// item.getTitle(),Toast.LENGTH_SHORT).show();
				return super.onTap(p, m);
			}

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
		super.onResume();
	}

	@Override
	protected void onPause() {
		mapView.onPause(); // 防止再次进入MainActivity地图显示出现问题
		// search();
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
			Toast.makeText(this, "再按一次后退键退出地图！", Toast.LENGTH_SHORT).show();
			;
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