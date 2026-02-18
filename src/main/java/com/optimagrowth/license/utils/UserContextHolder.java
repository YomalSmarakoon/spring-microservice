package com.optimagrowth.license.utils;

public class UserContextHolder {

    private static final ThreadLocal<UserContext> userContext =
            ThreadLocal.withInitial(UserContext::new);

    public static UserContext getContext() {
        return userContext.get();
    }

    public static void setContext(UserContext context) {
        userContext.set(context);
    }

    public static void clear() {
        userContext.remove();
    }
}
