package net.superliujian.mockgps.model;

import com.baidu.mapapi.GeoPoint;



public class DataModel {
	public String id = "";
	public String name = "";
	public String lat = "";
	public String lng = "";
	public String type = "";
	
	public DataModel(){
	}
	
	public DataModel(String name, GeoPoint gp){
		this.name = name;
		this.lat = Double.toString(gp.getLatitudeE6() / 1E6);
		this.lng = Double.toString(gp.getLongitudeE6() / 1E6);
	}
	
	public String getLatLng(){
		return this.lat + ", " + this.lng;
	}
	

	public String toString(){
		return this.name+","+ this.lat + ", " + this.lng;
	}
	public String getKey(){
		return this.lat + ", " + this.lng + "-" + this.type;
	}
	
	public double getDoubleLat(){
		if (!"".equals(lat)){
			return Double.parseDouble(lat);
		}
		return 0;
	}
	
	public double getDoubleLng(){
		if (!"".equals(lng)){
			return Double.parseDouble(lng);
		}
		return 0;
	}
	
}
