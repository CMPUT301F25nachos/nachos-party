package com.example.nachos_app.ui.admin;


import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Simple class to hold eligible admin emails (only our group)
 *
 *
 */
public final class AdminAllowList {

    private static final String[] RAW_EMAILS = {
            "dsteeves@ualberta.ca",
            "ngoctha1@ualberta.ca",
            "astromsm@ualberta.ca",
            "rayyan2@ualberta.ca",
            "sfpicket@ualberta.ca",
            "sperez2@ualberta.ca"
    };

    /** Immutable set of normalized emails. */
    private static final Set<String> ALLOWED_NORMALIZED;
    static {
        HashSet<String> s = new HashSet<>(RAW_EMAILS.length);
        for (String e : RAW_EMAILS) {
            s.add(e.trim().toLowerCase(Locale.ROOT));
        }
        ALLOWED_NORMALIZED = Collections.unmodifiableSet(s);
    }

    private AdminAllowList() { /* no instances */ }

    /**
     * Returns true if the email is in the allowed emails list
     *
     * @param email user's email
     * @return true if allowed, false otherwise
     */
    public static boolean isAllowed(String email) {
        if (email == null) return false;
        return ALLOWED_NORMALIZED.contains(email.trim().toLowerCase(Locale.ROOT));
    }
}
