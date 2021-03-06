package models.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Table(
	uniqueConstraints=@UniqueConstraint(columnNames={"account_id", "viz_id"})
)
@Entity(name="viz_permission")
public class VizPermission extends Permission implements models.entities.Entity {
	private Visualization viz;
	
	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return super.getId();
	}

	@ManyToOne
	public Visualization getViz() {
		return viz;
	}

	public void setViz(Visualization viz) {
		this.viz = viz;
	}
}