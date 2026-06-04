package com.spendx.util;

import com.spendx.model.User;

public class SessionManager {
    private static User currentUser;

    private SessionManager() {}

    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User user) { currentUser = user; }
    public static void clear() { currentUser = null; }
    public static boolean isLoggedIn() { return currentUser != null; }
}
