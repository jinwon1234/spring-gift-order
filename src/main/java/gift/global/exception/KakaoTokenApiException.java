package gift.global.exception;

import gift.oauth2.dto.KAuthExceptionResponse;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class KakaoTokenApiException extends RuntimeException {

  private final HttpStatus status;
  private final KAuthExceptionResponse kAuthExceptionResponse;

  public KakaoTokenApiException(String message, HttpStatus status, KAuthExceptionResponse kAuthExceptionResponse) {
      super(message);
      this.status = status;
      this.kAuthExceptionResponse = kAuthExceptionResponse;
  }

    public HttpStatus getStatus() {
        return status;
    }

    public KAuthExceptionResponse getkAuthExceptionResponse() {
        return kAuthExceptionResponse;
    }
}
