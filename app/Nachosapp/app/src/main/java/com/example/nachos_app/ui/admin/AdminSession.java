package com.example.nachos_app.ui.admin;



import android.content.Context;

/**
 * Helper class for in app admin session.
 * <p>
 * This does nothing on the firebase side of the project, instead
 * it uses a local SharedPreferences file to adjust UI behaviour
 * </p>
 *
 * @author Darius
 */
public class AdminSession {
    private static final String PREFS = "admin_prefs";
    private static final String KEY = "is_admin";

    /**
     * Returns whether the app is currently in admin mode
     *
     * @param ctx context for SharedPreferences
     * @return  true if admin mode is enabled, false otherwise
     */

    public static boolean isAdmin(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, false);
    }


    /**
     * Enables admin mode
     *
     * @param ctx context for SharedPreferences
     */
    public static void enable(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY, true).apply();
    }


    /**
     * Disables admin mode
     *
     * @param ctx context for SharedPreferences
     */
    public static void disable(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY, false).apply();
    }
}
