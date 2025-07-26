package gift.global;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

public class GlobalAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        System.out.println("비동기 예외 발생, " + "method = "
                + method.getName() + ", message = " + ex.getMessage()
                + ", ex = " + ex.getClass().getName());
    }
}
