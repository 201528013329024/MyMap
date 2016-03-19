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

	private BMapManager manager; // ��ͼ����Ĺ�����
	private MapView mapView; // ��ͼ��ʾ����
	private MapController mapController; // mapController���ڿ��Ƶ�ͼ��ƽ�ƣ����ź���ת

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// �������趨Layoutǰ��֤�ٶ�key
		checkKey();

		setContentView(R.layout.activity_main);

		initView();
	}

	private void initView() {
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true); // ���ðٶȵ�ͼ�����Ź���
		mapController = mapView.getController();

		mapController.setZoom(16); // ���õ�ͼ��ʼ��ʾΪ����15��

		mapController.setCenter(ConstantValue.geoUCAS); // ��ʼ��ʾ��ͼʱ�������ĵ�Ϊ���ƴ�
		drawUCAS(); // �ڵ�ͼ�ϱ�ǹ��ƴ�

		// mapView.setTraffic(true); //��ʾ��ͨͼ
		// mapView.setSatellite(true); //��ʾ����ͼ
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

	/** ����ͼ�еĹ��ƴ���ʵ��Բ��ǳ��� **/
	private void drawUCAS() {
		GraphicsOverlay overlay = new GraphicsOverlay(mapView); // ����GraphicsOverlay����
		setData(overlay); // ��overlay�����������ݸ�ʽ
		mapView.getOverlays().add(overlay); // ��overlay������뵽mapView��GraphicsOverlay������
		mapView.refresh(); // ˢ��mapView����ֹ����
	}

	private void setData(GraphicsOverlay overlay) {
		Geometry geometry = new Geometry(); // ���켯��ͼ��Ԫ��
		geometry.setCircle(ConstantValue.geoUCAS, 125); // ���ƴ�ΪԲ�ģ��뾶Ϊ125��

		Symbol symbol = new Symbol(); // ������ʽͼ��
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
		mapView.onResume(); // ��ֹ�ٴν���MainActivity��ͼ��ʾ��������
		super.onResume();
	}

	@Override
	protected void onPause() {
		mapView.onPause(); // ��ֹ�ٴν���MainActivity��ͼ��ʾ��������
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mapView.destroy(); // ��ֹ�ٴν���MainActivity��ͼ��ʾ��������
		super.onDestroy();
	}
}
