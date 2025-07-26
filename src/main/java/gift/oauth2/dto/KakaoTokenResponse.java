package gift.oauth2.dto;

public record KakaoTokenResponse(
        String token_type,
        String access_token,
        String refresh_token,
        Long expires_in,
        String id_token,
        Long refresh_token_expires_in,
        String scope
) {
}
