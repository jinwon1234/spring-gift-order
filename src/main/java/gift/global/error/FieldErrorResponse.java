package gift.global.error;


public class FieldErrorResponse {

    String message;

    public FieldErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
