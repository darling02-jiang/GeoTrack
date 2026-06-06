package com.geotrack.auth.support;

import com.geotrack.auth.dto.SessionUser;

public final class UserHolder {

    private static final ThreadLocal<SessionUser> TL = new ThreadLocal<>();

    private UserHolder() {
    }

    public static void save(SessionUser user) {
        TL.set(user);
    }

    public static SessionUser get() {
        return TL.get();
    }

    public static void clear() {
        TL.remove();
    }
}
