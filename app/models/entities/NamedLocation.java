package models.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class NamedLocation extends Location {
/*
	private Long id;
	private String label;
	private Long alsId;
	private Double latitude;
	private Double longitude;
	private String geojson;
*/
	
	private String inputName;

	public String getInputName() {
		return inputName;
	}

	public void setInputName(String name) {
		this.inputName = name;
	}
}
