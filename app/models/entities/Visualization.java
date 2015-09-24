package models.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name="viz")
public class Visualization extends MetaData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "viz_series", 
		joinColumns = { @JoinColumn(name = "viz_id", nullable = false) }, 
		inverseJoinColumns = { @JoinColumn(name = "series_id", nullable = false) }
	)
	private List<Series> allSeries;

	@Column(columnDefinition = "TEXT")
	private String uiSetting;

	@ManyToOne
	@JsonIgnore
	private Account owner;

	public Visualization() {
		allSeries = new ArrayList<>();
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public List<Series> getAllSeries() {
		return allSeries;
	}

	public void setAllSeries(List<Series> allSeries) {
		this.allSeries = allSeries;
	}

	public String getUiSetting() {
		return uiSetting;
	}

	public void setUiSetting(String uiSetting) {
		this.uiSetting = uiSetting;
	}

	
	public Account getOwner() {
		return owner;
	}

	public void setOwner(Account owner) {
		this.owner = owner;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((allSeries == null) ? 0 : allSeries.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((this.getTitle() == null) ? 0 : this.getTitle().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Visualization other = (Visualization) obj;
		if (allSeries == null) {
			if (other.allSeries != null)
				return false;
		} else if (!equals_toHandleHibenateBug(allSeries, other.allSeries))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (this.getTitle() == null) {
			if (other.getTitle() != null)
				return false;
		} else if (!this.getTitle().equals(other.getTitle()))
			return false;
		return true;
	}

	private <T> boolean equals_toHandleHibenateBug(List<T> a, List<T> b) {
		if (a == null)
			return b == null;
		assert (a != null);
		if (b == null)
			return false;
		assert (a != null && b != null);

		if (a.size() != b.size())
			return false;
		return a.containsAll(b);
	}

	@Override
	public String toString() {
		return "Viz [id=" + id + ", allSeries=" + allSeries +"]";
	}
}