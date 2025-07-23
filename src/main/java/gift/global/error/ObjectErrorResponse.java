package gift.global.error;


public class ObjectErrorResponse {

    String message;

    public ObjectErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
