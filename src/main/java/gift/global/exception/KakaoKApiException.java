package gift.global.exception;

import gift.oauth2.dto.KApiExceptionResponse;
import org.springframework.http.HttpStatus;

public class KakaoKApiException extends RuntimeException {

  private final HttpStatus status;
  private final KApiExceptionResponse kApiExceptionResponse;

  public KakaoKApiException(String message, HttpStatus status, KApiExceptionResponse kApiExceptionResponse) {
    super(message);
      this.status = status;
      this.kApiExceptionResponse = kApiExceptionResponse;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public KApiExceptionResponse getkApiExceptionResponse() {
    return kApiExceptionResponse;
  }
}
