package gift.global.exception;

public class JWTValidateException extends RuntimeException {
    public JWTValidateException(String message) {
        super(message);
    }
}
