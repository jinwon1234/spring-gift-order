package gift.global.exception;

public class BadRequestEntityException extends RuntimeException {
    public BadRequestEntityException(String message) {
        super(message);
    }
}
