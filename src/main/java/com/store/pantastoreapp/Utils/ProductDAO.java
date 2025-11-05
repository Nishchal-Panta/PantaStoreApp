package com.store.pantastoreapp.Utils;
import com.store.pantastoreapp.db.DBUtil;
import com.store.pantastoreapp.Models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public Product create(Product p) throws SQLException {
        String sql = "INSERT INTO products (name,exp_date,price,cost,quantity,cat_name,created_at) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getExp());
            ps.setDouble(4, p.getPrice());
            ps.setDouble(5, p.getCost());
            ps.setInt(6, p.getQuantity());
            ps.setString(7, p.getCat_name());
            ps.setTimestamp(8, p.getTimestamp());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
            return p;
        }
    }

    public Product findById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Product> findAll() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY name";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public boolean update(Product p) throws SQLException {
        String sql = "UPDATE products SET name=?,exp_date=?,price=?,cost=?,quantity=?,cat_name=?, created_at=? WHERE id=?";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getExp());
            ps.setDouble(4, p.getPrice());
            ps.setDouble(5, p.getCost());
            ps.setInt(6, p.getQuantity());
            ps.setInt(7, p.getId());
            ps.setString(8, p.getCat_name());
            ps.setTimestamp(9, p.getTimestamp());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setExp(rs.getString("exp_date").replace("-", "/").replace(" ", "T00:00:00.000+00:00"));
        p.setName(rs.getString("name"));
        p.setPrice(rs.getDouble("price"));
        p.setCost(rs.getDouble("cost"));
        p.setQuantity(rs.getInt("quantity"));
        p.setCat_name(rs.getString("cat_name"));
        p.setTimestamp(rs.getTimestamp("Activity"));
        return p;
    }
}
