package com.xhg.route_result;

public class Route_Result_ListCell {
	public String lineTitle = "";
	public String route_Num = "";
	public String route_Distance = "";
	public String Route_detailContents = "";
	public int type = 1;
	
	public Route_Result_ListCell(String lineTitle,String route_Num,String route_Distance,String Route_detailContents,int type) {
		this.lineTitle = lineTitle;
		this.route_Num = route_Num;
		this.route_Distance = route_Distance;
		this.Route_detailContents = Route_detailContents;
		this.type = type;
	}

	public String getLineTitle() {
		return lineTitle;
	}

	public void setLineTitle(String lineTitle) {
		this.lineTitle = lineTitle;
	}

	public String getRoute_Num() {
		return route_Num;
	}

	public void setRoute_Num(String route_Num) {
		this.route_Num = route_Num;
	}

	public String getRoute_Distance() {
		return route_Distance;
	}

	public void setRoute_Distance(String route_Distance) {
		this.route_Distance = route_Distance;
	}

	public String getRoute_detailContents() {
		return Route_detailContents;
	}

	public void setRoute_detailContents(String route_detailContents) {
		Route_detailContents = route_detailContents;
	}
	
	
}
