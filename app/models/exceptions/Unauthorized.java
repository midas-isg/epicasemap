package models.exceptions;

public class Unauthorized extends RuntimeException {
	private static final long serialVersionUID = 8383571417032313871L;

	public Unauthorized() {
        super();
    }

    public Unauthorized(String message) {
        super(message);
    }

    public Unauthorized(String message, Throwable cause) {
        super(message, cause);
    }

    public Unauthorized(Throwable cause) {
        super(cause);
    }
}