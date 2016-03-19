/**
 * @author Haoguang Xu
 * Copyright (c) 2016, UCAS
 * All rights reserved. 
 * 
 * MainActivity Class {@link MainActivity}  
 */

package com.xhg.mymap;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.Geometry;
import com.baidu.mapapi.map.Graphic;
import com.baidu.mapapi.map.GraphicsOverlay;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Symbol;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {

	private BMapManager manager; // 地图引擎的管理工具
	private MapView mapView; // 地图显示容器
	private MapController mapController; // mapController用于控制地图的平移，缩放和旋转

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 必须在设定Layout前验证百度key
		checkKey();

		setContentView(R.layout.activity_main);

		initView();
	}

	private void initView() {
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true); // 启用百度地图的缩放功能
		mapController = mapView.getController();

		mapController.setZoom(16); // 设置地图初始显示为缩放15倍

		mapController.setCenter(ConstantValue.geoUCAS); // 初始显示地图时设置中心点为国科大
		drawUCAS(); // 在地图上标记国科大

		// mapView.setTraffic(true); //显示交通图
		// mapView.setSatellite(true); //显示卫星图
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

	/** 将地图中的国科大用实心圆标记出来 **/
	private void drawUCAS() {
		GraphicsOverlay overlay = new GraphicsOverlay(mapView); // 创建GraphicsOverlay对象
		setData(overlay); // 给overlay对象设置数据格式
		mapView.getOverlays().add(overlay); // 将overlay对象加入到mapView的GraphicsOverlay集合中
		mapView.refresh(); // 刷新mapView，防止卡顿
	}

	private void setData(GraphicsOverlay overlay) {
		Geometry geometry = new Geometry(); // 构造集合图形元素
		geometry.setCircle(ConstantValue.geoUCAS, 125); // 国科大为圆心，半径为125米

		Symbol symbol = new Symbol(); // 构造样式图像
		Symbol.Color color = symbol.new Color();
		color.red = 200;
		color.green = 50;
		color.blue = 100;
		color.alpha = 100;
		symbol.setSurface(color, 1, 0);
		Graphic graphic = new Graphic(geometry, symbol);
		
		overlay.setData(graphic);
	}

	@Override
	protected void onResume() {
		mapView.onResume(); // 防止再次进入MainActivity地图显示出现问题
		super.onResume();
	}

	@Override
	protected void onPause() {
		mapView.onPause(); // 防止再次进入MainActivity地图显示出现问题
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mapView.destroy(); // 防止再次进入MainActivity地图显示出现问题
		super.onDestroy();
	}
}
