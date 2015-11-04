package models.entities;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "series_data_url")
public class SeriesDataUrl implements models.entities.Entity {
	private Long id;
	private String url;
	private String checksum;
	private Series series;

	@Override
	@GenericGenerator(name = "generator", strategy = "foreign", parameters = @Parameter(name = "property", value = "series"))
	@Id
	@GeneratedValue(generator = "generator")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	@JsonIgnore
	@OneToOne
	@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT))
	public Series getSeries() {
		return series;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Column(columnDefinition = "TEXT")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(length = 32)
	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public void setSeries(Series series) {
		this.series = series;
	}
}
