package gift.global.exception;

import gift.oauth2.dto.KApiExceptionResponse;
import gift.oauth2.dto.KAuthExceptionResponse;
import org.springframework.http.HttpStatus;

public class KakaoUserApiException extends RuntimeException {

  private final HttpStatus status;
  private final KApiExceptionResponse kApiExceptionResponse;

  public KakaoUserApiException(String message, HttpStatus status, KApiExceptionResponse kApiExceptionResponse) {
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
