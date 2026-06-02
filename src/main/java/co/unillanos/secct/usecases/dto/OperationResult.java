package co.unillanos.secct.usecases.dto;


public final class OperationResult {

    private final boolean success;
    private final String message;

    private OperationResult(boolean success, String message) {
        this.success = success;
        this.message = message != null ? message : "";
    }

    
    public static OperationResult ok(String message) {
        return new OperationResult(true, message);
    }

    
    public static OperationResult fail(String message) {
        return new OperationResult(false, message);
    }

    
    public boolean isSuccess() {
        return success;
    }

    
    public String getMessage() {
        return message;
    }
}
