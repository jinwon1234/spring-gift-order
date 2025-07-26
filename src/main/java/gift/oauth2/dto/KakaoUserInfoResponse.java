package gift.oauth2.dto;

public record KakaoUserInfoResponse(
        Long id,
        KakaoAccount kakao_account
) {
    public record KakaoAccount(
            boolean email_needs_agreement,
            boolean is_email_valid,
            boolean is_email_verified,
            String email
    ) {}
}
