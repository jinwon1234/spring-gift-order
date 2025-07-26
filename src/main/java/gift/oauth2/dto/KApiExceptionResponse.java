package gift.oauth2.dto;

public record KApiExceptionResponse(
        String error,
        String error_description
) {
}
