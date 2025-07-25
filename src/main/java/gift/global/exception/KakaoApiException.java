package gift.global.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class KakaoApiException extends RuntimeException {

  private final HttpStatus status;
  private final Map<String, Object> details;

  public KakaoApiException(String message, HttpStatus status, Map<String, Object> details) {
      super(message);
      this.status = status;
      this.details = details;
  }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
