package gift.oauth2.errorhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.global.exception.KakaoKAuthException;
import gift.oauth2.dto.KAuthExceptionResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

public class KakaoKAuthResponseHandler implements ResponseErrorHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEFAULT_MESSAGE = "카카오[kauth] API 예외 발생";


    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {

        HttpStatus httpStatus = HttpStatus.valueOf(response.getStatusCode().value());

        KAuthExceptionResponse kAuthExceptionResponse;
        try {
            kAuthExceptionResponse = objectMapper.readValue(response.getBody(), KAuthExceptionResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(DEFAULT_MESSAGE + "[응답 파싱 실패]");
        }


        throw new KakaoKAuthException(DEFAULT_MESSAGE, httpStatus, kAuthExceptionResponse);
    }
}
