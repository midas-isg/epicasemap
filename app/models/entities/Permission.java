package models.entities;

import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@MappedSuperclass
public class Permission extends Mode implements Entity {
	private Long id;
	private Account account;

	@Transient
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	@Column(name="read_data")
	public Boolean getRead_data() {
		return super.getRead_data();
	}

	@Override
	public Boolean getUse() {
		return super.getUse();
	}
	
	@Override
	public Boolean getChange() {
		return super.getChange();
	}
	
	@Override
	public Boolean getPermit() {
		return super.getPermit();
	}
	
	@ManyToOne
	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public void copy(Mode mode) {
		setUse(mode.getUse());
		setRead_data(mode.getRead_data());
		setChange(mode.getChange());
		setPermit(mode.getPermit());
	}
}