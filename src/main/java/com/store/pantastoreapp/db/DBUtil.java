package com.store.pantastoreapp.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DBUtil {
    private static HikariDataSource ds;

    static {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:mysql://localhost:3306/pantastore?useSSL=false&serverTimezone=UTC");
        cfg.setUsername("pantastoreuser");
        cfg.setPassword("pantastorepass");

        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(2);
        cfg.setConnectionTimeout(30000);
        cfg.setIdleTimeout(600000);
        cfg.setMaxLifetime(1800000);

        ds = new HikariDataSource(cfg);
    }

    private DBUtil() {}

    public static DataSource getDataSource() {
        return ds;
    }
}
