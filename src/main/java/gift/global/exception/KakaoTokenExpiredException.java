package gift.global.exception;

public class KakaoTokenExpiredException extends RuntimeException {
    public KakaoTokenExpiredException(String message) {
        super(message);
    }
}
