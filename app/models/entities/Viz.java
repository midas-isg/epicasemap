package models.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
public class Viz implements models.entities.Entity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "viz_series", 
		joinColumns = { @JoinColumn(name = "viz_id", nullable = false) }, 
		inverseJoinColumns = { @JoinColumn(name = "series_id", nullable = false) }
	)
	private List<Series> allSeries;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "viz_series2", 
		joinColumns = { @JoinColumn(name = "viz_id", nullable = false) }, 
		inverseJoinColumns = { @JoinColumn(name = "series_id", nullable = false) }
	)
	private List<Series> allSeries2;
	private String uiSetting;

	public Viz() {
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Series> getAllSeries() {
		return allSeries;
	}

	public void setAllSeries(List<Series> allSeries) {
		this.allSeries = allSeries;
	}

	public List<Series> getAllSeries2() {
		return allSeries2;
	}

	public void setAllSeries2(List<Series> allSeries2) {
		this.allSeries2 = allSeries2;
	}

	public String getUiSetting() {
		return uiSetting;
	}

	public void setUiSetting(String uiSetting) {
		this.uiSetting = uiSetting;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((allSeries == null) ? 0 : allSeries.hashCode());
		result = prime * result
				+ ((allSeries2 == null) ? 0 : allSeries2.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		Viz other = (Viz) obj;
		if (allSeries == null) {
			if (other.allSeries != null)
				return false;
		} else if (!equals_toHandleHibenateBug(allSeries, other.allSeries))
			return false;
		if (allSeries2 == null) {
			if (other.allSeries2 != null)
				return false;
		} else if (!equals_toHandleHibenateBug(allSeries2, other.allSeries2))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
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
		return "Viz [id=" + id + ", name=" + title + ", allSeries=" + allSeries
				+ ", allSeries2=" + allSeries2 + "]";
	}
}