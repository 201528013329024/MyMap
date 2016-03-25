package com.xhg.search_result;

public class Search_Place_Result_ListCell {

	public String place_name = null;
	public String place_address = null;
	public String place_phone = null;
	public int distance = 0;

	public Search_Place_Result_ListCell(String place_name,String place_address,String place_phone,int distance) {
		this.place_name = place_name;
		this.place_address = place_address;
		this.place_phone = place_phone;
		this.distance = distance;
	}

	public String getPlace_name() {
		return place_name;
	}

	public void setPlace_name(String place_name) {
		this.place_name = place_name;
	}

	public String getPlace_address() {
		return place_address;
	}

	public void setPlace_address(String place_address) {
		this.place_address = place_address;
	}

	public String getPlace_phone() {
		return place_phone;
	}

	public void setPlace_phone(String place_phone) {
		this.place_phone = place_phone;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}
	
	
	
}
