package co.unillanos.secct.usecases.exceptions;

/** Business rule violation. The HTTP status to answer with is carried by the exception. */
public class BusinessException extends RuntimeException {

    private final int httpStatus;

    public BusinessException(String message) {
        this(message, 400);
    }

    public BusinessException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
