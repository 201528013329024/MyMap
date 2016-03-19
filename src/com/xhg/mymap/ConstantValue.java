/**
 * @author Haoguang Xu
 * Copyright (c) 2016, UCAS
 * All rights reserved. 
 * 
 * ConstantValue interface {@link ConstantValue}  
 */

package com.xhg.mymap;

import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.R.string;

public interface ConstantValue {

	// Ҫʹ�ðٶȵ�ͼ�����ݣ�������Ҫ���ٶȿ�������ҳ������key
	String KEY = "1A4A4ABEFBEECD8C17DEE880C4EA69B9607020B5";

	int latitude = (int) (40.414256 * 1E6); // ���ƴ�ά������
	int longitude = (int) (116.688663 * 1E6); // ���ƴ󾭶�����
	GeoPoint geoUCAS = new GeoPoint(latitude, longitude); // �������ƴ��ڵ�ͼ�ϵĵ��������
}
