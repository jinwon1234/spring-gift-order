package gift.oauth2.dto;

import gift.domain.Social;

public record SocialLoginRequest(
        String email,
        Social social
) {

}
