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
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {

	private BMapManager manager; //地图引擎的管理工具
	private MapView mapView; //地图显示容器
	private MapController mapController; //mapController用于控制地图的平移，缩放和旋转
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//必须在设定Layout前验证百度key
		checkKey();
		
		setContentView(R.layout.activity_main);
		
		initView();
	}
	
	private void initView() { 
		mapView = (MapView) findViewById(R.id.mapView);	
		mapView.setBuiltInZoomControls(true); //启用百度地图的缩放功能
		mapController = mapView.getController();
		
		mapController.setZoom(16); //设置地图初始显示为缩放15倍
		
		int latitude = (int)(40.415293*1E6); //国科大维度坐标
		int longitude = (int)(116.68627*1E6); //国科大经度坐标
		GeoPoint geoUCAS = new GeoPoint(latitude,longitude); //创建国科大在地图上的地理坐标点
		mapController.setCenter(geoUCAS); //初始显示地图时设置中心点为国科大
		
//		mapView.setTraffic(true); //显示交通图
//		mapView.setSatellite(true); //显示卫星图
	}

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
	
	@Override
	protected void onResume() {
		mapView.onResume(); //防止再次进入MainActivity地图显示出现问题
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		mapView.onPause(); //防止再次进入MainActivity地图显示出现问题
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		mapView.destroy(); //防止再次进入MainActivity地图显示出现问题
		super.onDestroy();
	}
}
