package gift.global;

import gift.member.dto.AuthMember;

public final class MySecurityContextHolder {

    private static final ThreadLocal<AuthMember> context = new ThreadLocal<>();

    public static void set(AuthMember authMember) {
        context.set(authMember);
    }

    public static AuthMember get() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
}
