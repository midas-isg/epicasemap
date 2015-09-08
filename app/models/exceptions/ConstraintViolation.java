package models.exceptions;

public class ConstraintViolation extends RuntimeException {
	
	private static final long serialVersionUID = -6966236584032909661L;

	public ConstraintViolation() {
        super();
    }

    public ConstraintViolation(String message) {
        super(message);
    }

    public ConstraintViolation(String message, Throwable cause) {
        super(message, cause);
    }

    public ConstraintViolation(Throwable cause) {
        super(cause);
    }
}
