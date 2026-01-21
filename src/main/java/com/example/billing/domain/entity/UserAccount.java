package com.example.billing.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "user_account")
public class UserAccount {
    @Id
    @Column(length = 50)
    private String userName;

    @NotBlank
    private String password; // TODO: replace with hashed storage in production

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
