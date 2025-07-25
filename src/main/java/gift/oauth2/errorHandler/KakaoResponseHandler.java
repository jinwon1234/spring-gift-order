package gift.oauth2.errorHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.global.exception.KakaoApiException;
import gift.oauth2.dto.KApiExceptionResponse;
import gift.oauth2.dto.KAuthExceptionResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class KakaoResponseHandler implements ResponseErrorHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEFAULT_MESSAGE = "카카오 API 예외 발생";


    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {

        String uri = url.toString();

        HttpStatus httpStatus = HttpStatus.valueOf(response.getStatusCode().value());

        try {
            if (uri.startsWith("https://kauth.kakao.com")) {
                KAuthExceptionResponse kAuthExceptionResponse = objectMapper.readValue(response.getBody(), KAuthExceptionResponse.class);
                int code = kAuthExceptionResponse.code();
                String msg = kAuthExceptionResponse.msg();
                Map<String, Object> details = Map.of("code", code, "msg", msg);
                throw new KakaoApiException(DEFAULT_MESSAGE, httpStatus, details);
            } else if (uri.startsWith("https://kapi.kakao.com")) {
                KApiExceptionResponse kApiExceptionResponse = objectMapper.readValue(response.getBody(), KApiExceptionResponse.class);
                String error = kApiExceptionResponse.error();
                String error_description = kApiExceptionResponse.error_description();
                Map<String, Object> details = Map.of("error", error, "error_description", error_description);
                throw new KakaoApiException(DEFAULT_MESSAGE, httpStatus, details);
            } else {
                throw new KakaoApiException(DEFAULT_MESSAGE, httpStatus, Map.of());
            }
        } catch (IOException e) {
            throw new IllegalStateException("카카오 API 예외 응답 파싱 실패");
        }
    }


}
