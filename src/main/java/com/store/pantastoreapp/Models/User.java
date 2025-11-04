package com.store.pantastoreapp.Models;

public abstract class User {
    protected String Fname;
    protected String Lname;
    protected String password; // Stored securely (e.g., hashed)

    public User() {
        // Default constructor for frameworks if needed
    }

    public User(String Fname, String Lname, String password) {
        this.Fname = Fname;
        this.Lname = Lname;
        this.password = password;
    }
    public void setFname(String Fname) {
        this.Fname = Fname;
    }

    public void setLname(String name) { this.Lname = Lname;}

    public void setPassword(String password) {
        this.password = password;
    }
}