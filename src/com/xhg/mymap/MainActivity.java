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

	private BMapManager manager; // ��ͼ����Ĺ�����
	private MapView mapView; // ��ͼ��ʾ����
	private MapController mapController; // mapController���ڿ��Ƶ�ͼ��ƽ�ƣ����ź���ת

	private View satellite_traffic_map;
	private ImageView img_Flag, img_Traffic, img_Satellite;

	// ���������������ڳ�������������ʳ������վ��
	private MKSearch search;
	private MKSearchListener listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// �������趨Layoutǰ��֤�ٶ�key
		checkKey();

		setContentView(R.layout.activity_main);

		initView();

		addFlagTrafficSatellite();

		search();
	}

	/** �������� **/
	private void search() {
		// Toast.makeText(MainActivity.this, "�ѽ���Search��������",
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
					Toast.makeText(MainActivity.this, "û�в�ѯ�������Ϣ", Toast.LENGTH_SHORT).show();
					;
				}
			}

			private void setData(PoiOverlay overlay, MKPoiResult result) {
				ArrayList<MKPoiInfo> mkPoiInfos = result.getAllPoi();
				overlay.setData(mkPoiInfos);

				String string = "��ǰҳ" + result.getPageIndex() + "/��ҳ��" + result.getNumPages() + "��   ��ǰ��Ŀ"
						+ result.getCurrentNumPois() + "/����Ŀ" + result.getNumPois();
				Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
			};
		};
		search.init(manager, listener);
		// search.poiSearchNearBy("ҽԺ", ConstantValue.geoUCAS, 10000);
		search.poiSearchInCity("����", "����վ");
	}

	/** ��ת������ҳ�����һҳ  **/
	private int currentPage = 0;
	private void nextPageForSearch() {
		currentPage++; 
		search.goToPoiPage(currentPage); //��ת��ָ��ҳ��
	}

	private void initView() {
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true); // ���ðٶȵ�ͼ�����Ź���
		// mapView.displ
		mapController = mapView.getController();

		mapController.setZoom(15); // ���õ�ͼ��ʼ��ʾΪ����15��

		mapController.setCompassMargin(100, 100);

		mapController.setCenter(ConstantValue.geoUCAS); // ��ʼ��ʾ��ͼʱ�������ĵ�Ϊ���ƴ�
		// search();

		// drawUCAS_with_Text(); // �ڵ�ͼ�������ֱ�ǹ��ƴ�
		// drawUCAS_with_Circle(); // �ڵ�ͼ����Բ���ֱ�ǹ��ƴ�
		drawUCAS_with_Image(); // �ڵ�ͼ����Բ���ֱ�ǹ��ƴ�
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
		WindowManager wm = this.getWindowManager();
		int width = wm.getDefaultDisplay().getWidth();

		// ����satellite_traffic_map����ʾλ��
		satellite_traffic_map.setX(width - 130);
		satellite_traffic_map.setY(0);

		// ��satellite_traffic_map��ʾ��mapView��
		mapView.addView(satellite_traffic_map);

		img_Flag.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { // ��ʾƽ��ͼ
//				mapView.setSatellite(false);
//				mapView.setTraffic(false);
				nextPageForSearch();
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

			@Override
			/** ��ͼ�ϵĵ���¼� **/
			public boolean onTap(GeoPoint p, MapView m) {
				// TODO Auto-generated method stub
				for (int i = 0; i < this.size(); i++) {
					OverlayItem item = this.getItem(i);
					if (Math.abs(p.getLatitudeE6() - item.getPoint().getLatitudeE6()) < 4000
							&& Math.abs(p.getLongitudeE6() - item.getPoint().getLongitudeE6()) < 4000) {
						Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
					}
				}

				// Toast.makeText(MainActivity.this, "���ȣ�" + p.getLongitudeE6()
				// + "��ά�ȣ�" + p.getLatitudeE6(),
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
		super.onResume();
	}

	@Override
	protected void onPause() {
		mapView.onPause(); // ��ֹ�ٴν���MainActivity��ͼ��ʾ��������
		// search();
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
			Toast.makeText(this, "�ٰ�һ�κ��˼��˳���ͼ��", Toast.LENGTH_SHORT).show();
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