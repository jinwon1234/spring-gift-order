package gift.util;

public final class PatternUtil {

    private PatternUtil() {
        throw new AssertionError("utility class");
    }

    public static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[~`!@#$%^&*()_+=\\-\\[\\]{}|\\\\:;\"'<>,.?/]).{10,20}$";

    public static final String PRODUCT_NAME_PATTERN
            = "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\s\\(\\)\\[\\]\\+\\-\\&\\/\\_]*$";

    public static final String OPTION_NAME_PATTERN
            = "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\s\\(\\)\\[\\]\\+\\-\\&\\/\\_]*$";
}
