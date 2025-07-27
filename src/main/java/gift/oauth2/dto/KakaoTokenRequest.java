package gift.oauth2.dto;

public record KakaoTokenRequest(
        String code,
        String key,
        String redirectUri
){
}
