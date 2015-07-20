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
public class Viz extends MetaData{
	
	private Long id;
	
	private List<Series> allSeries;
	
	public Viz(){
		allSeries = new ArrayList<>();
	}
	

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}
	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "viz_series", 
			joinColumns = {
					@JoinColumn(name = "viz_id", nullable = false)
			},
			inverseJoinColumns ={
					@JoinColumn(name = "series_id", nullable = false)
			}
	)
	public List<Series> getAllSeries() {
		return allSeries;
	}

	public void setAllSeries(List<Series> allSeries) {
		this.allSeries = allSeries;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((allSeries == null) ? 0 : allSeries.hashCode());
		result = prime * result + ((this.getId() == null) ? 0 : this.getId().hashCode());
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
		Viz other = (Viz) obj;
		if (allSeries == null) {
			if (other.allSeries != null)
				return false;
		} else if (!equals_toHandleHibenateBug(allSeries, other.allSeries))
			return false;
		if (this.getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!this.getId().equals(other.getId()))
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
		return "Viz [id=" + this.getId() + ", name=" + this.getTitle() + ", allSeries=" + allSeries
				+ "]";
	}
}