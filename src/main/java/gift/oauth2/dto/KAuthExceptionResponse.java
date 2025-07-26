package gift.oauth2.dto;

public record KAuthExceptionResponse(
        String error,
        String error_description
) {
}
