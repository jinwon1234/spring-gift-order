package gift.oauth2.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KakaoProperties {

    @Value("${oauth2.key.kakao}")
    private String kakaoRestApiKey;
    @Value("${oauth2.redirect.kakao}")
    private String kakaoRedirectUri;
    @Value("${kakao.domain.kapi}")
    private String kApiUri;
    @Value("${kakao.domain.kauth}")
    private String kAuthUri;

    public String getKakaoRestApiKey() {
        return kakaoRestApiKey;
    }

    public String getKakaoRedirectUri() {
        return kakaoRedirectUri;
    }

    public String getkApiUri() {
        return kApiUri;
    }

    public String getkAuthUri() {
        return kAuthUri;
    }
}
