package com.store.pantastoreapp.Utils;

import com.store.pantastoreapp.Models.User;

public class CurrentUser {
    private static User user;
    public static void set(User u) { user = u; }
    public static User get() { return user; }
    public static boolean isLoggedIn() { return user != null; }
}
