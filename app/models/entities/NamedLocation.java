package models.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lsparser.xmlparser.ALSIDQueryInput;

public class NamedLocation extends Location {
	private String locationTypeName;
/*
	private Long id;
	private String label;
	private Long alsId;
	private Double latitude;
	private Double longitude;
	private String geojson;
*/
	
	private ALSIDQueryInput alsIDQueryInput;
	
	public ALSIDQueryInput getALSIDQueryInput() {
		return alsIDQueryInput;
	}

	public void setALSIDQueryInput(ALSIDQueryInput alsIDQueryInput) {
		this.alsIDQueryInput = alsIDQueryInput;
		
		return;
	}
	
	public String getLocationTypeName() {
		return locationTypeName;
	}
	
	public void setLocationTypeName(String locationTypeName) {
		this.locationTypeName = locationTypeName;
		
		return;
	}
}
