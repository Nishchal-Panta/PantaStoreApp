package com.store.pantastoreapp.Utils;

import com.store.pantastoreapp.db.DBUtil;
import com.store.pantastoreapp.Models.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.sql.DataSource;
import java.sql.*;

public class UserDAO {
    private final DataSource ds = DBUtil.getDataSource();

    public User createUser(String username, String plainPassword, String firstName, String lastName) throws SQLException {
        String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
        String sql = "INSERT INTO users (username, password_hash, first_name, last_name) VALUES (?, ?, ?, ?)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, hashed);
            ps.setString(3, firstName);
            ps.setString(4, lastName);
            ;
            ParameterMetaData pmd = ps.getParameterMetaData();
            int expectedCount = 4;
            if (pmd.getParameterCount() != expectedCount) {
                throw new IllegalStateException("PreparedStatement parameter count mismatch");
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                User u = new User();
                u.setUsername(firstName + "-" + lastName);
                u.setPasswordHash(hashed);
                u.setFirstName(firstName);
                u.setLastName(lastName);
                if (rs.next()) u.setId(rs.getInt(1));
                return u;
            }
        }
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, first_name, last_name, created_at FROM users WHERE username = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setFirstName(rs.getString("first_name"));
                    u.setLastName(rs.getString("last_name"));
                    u.setCreated_at(rs.getTimestamp("created_at"));
                    return u;
                }
            }
        }
        return null;
    }

    public boolean authenticate(String username, String plainPassword) throws SQLException {
        User u = findByUsername(username);
        if (u == null) return false;
        return BCrypt.checkpw(plainPassword, u.getPasswordHash());
    }

    public static void showAllUsers() {
        String sql = "SELECT * FROM users";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println("Username: " + rs.getString("username") +
                        ", Email: " + rs.getString("email") +
                        ", Phone: " + rs.getString("phone") +
                        ", Address: " + rs.getString("address"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

//package com.store.pantastoreapp.Utils;
//
//import com.store.pantastoreapp.Models.User;
//
//import java.io.*;
//import java.util.logging.Logger;
//
//public class UserDataManager extends User {
//    private static final Logger logger = Logger.getLogger(UserDataManager.class.getName());
//    public String getUserData() throws IOException {
//        String usrname = null;
//        String pswrd = null;
//        try {
//            BufferedReader br = new BufferedReader(new FileReader("/com/store/pantastoreapp/data/UserInfo.csv"));
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] data = line.split(",");
//                usrname = data[0];
//                pswrd = data[1];
//            }
//            br.close();
//        } catch (IOException e) {
//            logger.log(java.util.logging.Level.SEVERE, "Error reading from file", e);
//        }
//        return usrname + "," + pswrd;
//    }
//}
