package models.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
//@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "id")) })
public class Series extends MetaData {
	
	private Long id;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	// @Override
	public void setId(Long id) {
		this.id = id;
	}
	
	/*@Override @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return super.getId();
	}*/


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((this.getDescription() == null) ? 0 : this.getDescription()
						.hashCode());
		result = prime * result
				+ ((this.getId() == null) ? 0 : this.getId().hashCode());
		result = prime * result
				+ ((this.getTitle() == null) ? 0 : this.getTitle().hashCode());
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

	@Override
	public String toString() {
		return "Series [id=" + this.getId() + ", name=" + this.getTitle()
				+ ", description=" + this.getDescription() + "]";
	}

}
