package models.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Table(
	uniqueConstraints=@UniqueConstraint(columnNames={"account_id", "series_id"})
)
@Entity(name="series_permission")
public class SeriesPermission extends Mode implements models.entities.Entity {
	private Long id;
	private Account account;
	private Series series;
	
	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne
	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	@ManyToOne
	public Series getSeries() {
		return series;
	}

	public void setSeries(Series series) {
		this.series = series;
	}
	
	public void copy(Mode mode){
		setUse(mode.getUse());
		setRead(mode.getRead());
		setChange(mode.getChange());
		setPermit(mode.getPermit());
	}
}