package gift.global.exception;

public class KakaoApiException extends RuntimeException {

  private final int code;

  public KakaoApiException(String message, int code) {
    super(message);
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
