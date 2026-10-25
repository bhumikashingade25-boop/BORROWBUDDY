package com.borrowbuddy.model;

import java.sql.Timestamp;

/** Represents a registered user (student, faculty, or administrator). */
public class User {
    private int userId;
    private String name;
    private String email;
    private String passwordHash;
    private String role;          // STUDENT | FACULTY | ADMIN
    private String department;
    private String phone;
    private Timestamp registrationDate;

    public User() {}

    public User(int userId, String name, String email, String passwordHash,
                String role, String department, String phone, Timestamp registrationDate) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.department = department;
        this.phone = phone;
        this.registrationDate = registrationDate;
    }

    // Getters
    public int getUserId()              { return userId; }
    public String getName()             { return name; }
    public String getEmail()            { return email; }
    public String getPasswordHash()     { return passwordHash; }
    public String getRole()             { return role; }
    public String getDepartment()       { return department; }
    public String getPhone()            { return phone; }
    public Timestamp getRegistrationDate() { return registrationDate; }

    // Setters
    public void setUserId(int userId)           { this.userId = userId; }
    public void setName(String name)            { this.name = name; }
    public void setEmail(String email)          { this.email = email; }
    public void setPasswordHash(String hash)    { this.passwordHash = hash; }
    public void setRole(String role)            { this.role = role; }
    public void setDepartment(String dept)      { this.department = dept; }
    public void setPhone(String phone)          { this.phone = phone; }
    public void setRegistrationDate(Timestamp t){ this.registrationDate = t; }

    public boolean isAdmin()   { return "ADMIN".equals(role); }
    public boolean isStudent() { return "STUDENT".equals(role); }
    public boolean isFaculty() { return "FACULTY".equals(role); }

    @Override
    public String toString() {
        return name + " (" + email + ") [" + role + "]";
    }
}
