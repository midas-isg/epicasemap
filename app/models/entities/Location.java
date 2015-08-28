package models.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Location implements models.entities.Entity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String label;
	@Column(name = "als_id", unique = true)
	private Long alsId;
	@Column(name = "lat")
	private Double latitude;
	@Column(name = "long")
	private Double longitude;
	private String geojson;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Long getAlsId() {
		return alsId;
	}

	public void setAlsId(Long alsId) {
		this.alsId = alsId;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getGeojson() {
		return geojson;
	}

	public void setGeojson(String geojson) {
		this.geojson = geojson;
	}
}
