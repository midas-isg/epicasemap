package interactors.security;

public class Credential {
	private Long id;
	private String name;

	public Credential(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
