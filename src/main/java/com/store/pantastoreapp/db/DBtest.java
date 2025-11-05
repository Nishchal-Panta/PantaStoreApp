package com.store.pantastoreapp.db;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBtest {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3306/pantastore?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "pantastoreuser";
        String pass = "pantastorepass";
        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected ok, DB product: " + c.getMetaData().getDatabaseProductName());
        }
    }
}
