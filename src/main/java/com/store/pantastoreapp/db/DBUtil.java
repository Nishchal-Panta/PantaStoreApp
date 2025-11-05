package com.store.pantastoreapp.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;

public final class DBUtil {
    private static HikariDataSource ds;

    static {
        String host = System.getenv().getOrDefault("DB_HOST", "127.0.0.1");
        String port = System.getenv().getOrDefault("DB_PORT", "3306");
        String db   = System.getenv().getOrDefault("DB_NAME", "pantastore");
        String user = System.getenv().getOrDefault("DB_USER", "pantastoreuser");
        String pass = System.getenv().getOrDefault("DB_PASS", "pantastorepass");

        // **allowPublicKeyRetrieval=true** added to permit caching_sha2_password exchange in dev
        String jdbcUrl = String.format(
                "jdbc:mysql://%s:%s/%s?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                host, port, db);

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setUsername(user);
        cfg.setPassword(pass);

        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(2);
        cfg.setConnectionTimeout(20000);
        cfg.setIdleTimeout(300000);
        cfg.setMaxLifetime(1800000);

        // Avoid fail-fast so your app can retry while DB starts
        cfg.setInitializationFailTimeout(-1);

        ds = new HikariDataSource(cfg);
    }

    private DBUtil() {}

    public static DataSource getDataSource() {
        return ds;
    }

    public static Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get DB connection", e);
        }
    }
}
