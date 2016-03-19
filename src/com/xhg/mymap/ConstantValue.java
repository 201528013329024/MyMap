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

	// 要使用百度地图的数据，首先需要到百度开发者网页下申请key
	String KEY = "1A4A4ABEFBEECD8C17DEE880C4EA69B9607020B5";

	int latitude = (int) (40.414256 * 1E6); // 国科大维度坐标
	int longitude = (int) (116.688663 * 1E6); // 国科大经度坐标
	GeoPoint geoUCAS = new GeoPoint(latitude, longitude); // 创建国科大在地图上的地理坐标点
}
