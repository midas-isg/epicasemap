package models.exceptions;

public class NotFound extends RuntimeException {
	private static final long serialVersionUID = -8042355405456530378L;
	
    public NotFound() {
        super();
    }

    public NotFound(String message) {
        super(message);
    }

    public NotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFound(Throwable cause) {
        super(cause);
    }
}
