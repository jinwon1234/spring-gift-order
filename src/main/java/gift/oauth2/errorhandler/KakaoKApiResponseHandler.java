package gift.oauth2.errorhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.global.exception.KakaoKApiException;
import gift.global.exception.KakaoTokenExpiredException;
import gift.oauth2.dto.KApiExceptionResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

public class KakaoKApiResponseHandler implements ResponseErrorHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEFAULT_MESSAGE = "카카오[kapi] API 예외 발생";


    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        ResponseErrorHandler.super.handleError(url, method, response);
        HttpStatus httpStatus = HttpStatus.valueOf(response.getStatusCode().value());

        KApiExceptionResponse kApiExceptionResponse;
        try {
            kApiExceptionResponse = objectMapper.readValue(response.getBody(), KApiExceptionResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(DEFAULT_MESSAGE + "[응답 파싱 실패]");
        }

        if (kApiExceptionResponse.code() == -401) {
            throw new KakaoTokenExpiredException(DEFAULT_MESSAGE + "[액세스 토큰 만료]");
        }

        throw new KakaoKApiException(DEFAULT_MESSAGE, httpStatus, kApiExceptionResponse);
    }
}
