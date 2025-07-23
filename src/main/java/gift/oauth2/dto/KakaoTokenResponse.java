package gift.oauth2.dto;

public record KakaoTokenResponse(
        String token_type,
        String access_token
) {
}
