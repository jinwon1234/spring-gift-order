package gift.oauth2.errorhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.global.exception.KakaoTokenApiException;
import gift.global.exception.KakaoUserApiException;
import gift.oauth2.dto.KApiExceptionResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class KakaoUserResponseHandler implements ResponseErrorHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEFAULT_MESSAGE = "카카오 USER API 예외 발생";

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


        throw new KakaoUserApiException(DEFAULT_MESSAGE, httpStatus, kApiExceptionResponse);
    }
}
