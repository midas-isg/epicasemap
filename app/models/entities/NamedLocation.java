package models.entities;

import lsparser.xmlparser.ALSIDQueryInput;

public class NamedLocation extends Location {
	private String locationTypeName;
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
