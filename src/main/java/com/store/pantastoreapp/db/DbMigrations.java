package com.store.pantastoreapp.db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

public class DbMigrations {
    public static void ensureTables(DataSource ds) {
        String usersSql = """
          CREATE TABLE IF NOT EXISTS users (
          id INT AUTO_INCREMENT PRIMARY KEY,
          username VARCHAR(150) NOT NULL UNIQUE,
          password_hash VARCHAR(255) NOT NULL,
          first_name VARCHAR(100) NOT NULL,
          last_name VARCHAR(100) NOT NULL,
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
        """;

        String productsSql = """
            CREATE TABLE IF NOT EXISTS products (
            id INT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            exp_date DATE NULL DEFAULT NULL,
            price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
            cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
            quantity INT NOT NULL DEFAULT 0,
            cat_name VARCHAR(100) NULL,
            cat_id INT NULL DEFAULT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_category
                FOREIGN KEY (cat_name)
                    REFERENCES categories(name)
                    ON DELETE SET NULL
          );
        """;

        try (Connection c = ds.getConnection();
             Statement s = c.createStatement()) {
            s.execute(usersSql);
            s.execute(productsSql);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to run DB migrations", ex);
        }
    }
}
//package com.store.pantastoreapp.db;
//
//import javax.sql.DataSource;
//import java.sql.Connection;
//import java.sql.Statement;
//
//public class DbMigrations {
//    public static void ensureTables(DataSource ds) {
//        String usersSql = """
//        CREATE TABLE IF NOT EXISTS users (
//          id INT AUTO_INCREMENT PRIMARY KEY,
//          username VARCHAR(150) NOT NULL UNIQUE,
//          password_hash VARCHAR(255) NOT NULL,
//          first_name VARCHAR(100),
//          last_name VARCHAR(100),
//          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
//        );
//        """;
//
//        String productsSql = """
//        CREATE TABLE IF NOT EXISTS products (
//          id INT AUTO_INCREMENT PRIMARY KEY,
//          name VARCHAR(255) NOT NULL,
//          cat_name VARCHAR(255) NULL,
//          exp_date DATE NULL Default CURRENT_DATE + INTERVAL 1 YEAR,
//          price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
//          cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
//          quantity INT NOT NULL DEFAULT 0,
//          reorder_level INT NOT NULL DEFAULT 0,
//          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
//        );
//        """;
//
//        try (Connection c = ds.getConnection();
//             Statement s = c.createStatement()) {
//            s.execute(usersSql);
//            s.execute(productsSql);
//        } catch (Exception ex) {
//            throw new RuntimeException("Failed to run DB migrations", ex);
//        }
//    }
//}
