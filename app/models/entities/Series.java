package models.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.ColumnDefault;

@Entity
public class Series extends MetaData {
	private Long id;
	private Account owner;
	
	@OneToOne(mappedBy = "series"/*, cascade = CascadeType.ALL*/)
	@JoinColumn(name = "id")
	private SeriesDataUrl seriesDataUrl;
	
	@ColumnDefault("false")
	private boolean lock;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne
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
				+ ((this.getDescription() == null) ? 0 : this.getDescription().hashCode());
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
		Series other = (Series) obj;
		if (this.getDescription() == null) {
			if (other.getDescription() != null)
				return false;
		} else if (!this.getDescription().equals(other.getDescription()))
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

	@Override
	public String toString() {
		return "Series [id=" + id + ", title=" + this.getTitle() + ", description="
				+ this.getDescription() + "]";
	}

	public SeriesDataUrl getSeriesDataUrl() {
		return seriesDataUrl;
	}

	public void setSeriesDataUrl(SeriesDataUrl seriesDataUrl) {
		this.seriesDataUrl = seriesDataUrl;
	}

	public boolean getLock() {
		return lock;
	}

	public void setLock(boolean lock) {
		this.lock = lock;
	}
}
