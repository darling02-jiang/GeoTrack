package com.geotrack.mall.util;

import java.util.UUID;

public final class OrderNoGenerator {

    private OrderNoGenerator() {
    }

    public static String next() {
        return "GT" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
