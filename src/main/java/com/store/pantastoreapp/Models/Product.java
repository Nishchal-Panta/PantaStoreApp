package com.store.pantastoreapp.Models;

import java.sql.Date;
import java.sql.Timestamp;

public class Product {
    private int id;
    private Date exp_date;
    private String name;
    private String cat_name;
    private double price;
    private double cost;
    private int quantity;
    private Timestamp created_at = new Timestamp(System.currentTimeMillis());

    public Timestamp getTimestamp() {
        return created_at;
    }
    public void setTimestamp(Timestamp activity){
        this.created_at = created_at;
    }

    public void setExp(Date exp_date) {
        this.exp_date = exp_date;
    }
    public Date getExp() {
        return exp_date;
    }

    public String getCat_name() {
        return cat_name;
    }
    public void setCat_name(String cat_name) {
        this.cat_name = cat_name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    public double getPrice() {
        return price;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
    public double getCost() { return cost;}

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public int getQuantity() {
        return quantity;
    }
}
