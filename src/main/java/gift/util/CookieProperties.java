package gift.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record CookieProperties(
        @Value("${spring.cookie.domain}")
        String domain,
        @Value("${spring.cookie.secure}")
        boolean secure,
        @Value("${spring.cookie.sameSite}")
        String sameSite
) {
}
