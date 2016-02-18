package models.exceptions;

public class NoConnectionAvailable extends RuntimeException {

	private static final long serialVersionUID = -8146855031460215315L;

	public NoConnectionAvailable() {
		super();
	}

	public NoConnectionAvailable(String message) {
		super(message);
	}

	public NoConnectionAvailable(String message, Throwable cause) {
		super(message, cause);
	}

	public NoConnectionAvailable(Throwable cause) {
		super(cause);
	}

}
