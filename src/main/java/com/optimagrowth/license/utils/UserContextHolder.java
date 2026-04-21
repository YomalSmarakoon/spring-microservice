package com.optimagrowth.license.utils;

public class UserContextHolder {

    private UserContextHolder() {}

    /*
    * What this means:
    * - Each thread gets its own separate UserContext
    * - The first time a thread calls get() → it creates a new UserContext
    * - After that → same object is reused within that thread
    * */
    private static final ThreadLocal<UserContext> userContext = ThreadLocal.withInitial(UserContext::new);

    public static UserContext getContext() {
        return userContext.get();
    }

    public static void setContext(UserContext context) {
        userContext.set(context);
    }

    // Removes the value
    // Prevents memory leaks in thread pools (VERY important in servers)
    public static void clear() {
        userContext.remove();
    }
}
