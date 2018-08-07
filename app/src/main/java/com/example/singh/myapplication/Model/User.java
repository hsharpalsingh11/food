package com.example.singh.myapplication.Model;

public class User
{
    private String Name;
    private String Password;
    private String Phone;
    private String IsStaff;
    private String secureCode;
    private double balance;

    public User() {
    }

    public String getSecureCode() {
        return secureCode;
    }

    public void setSecureCode(String secureCode) {
        this.secureCode = secureCode;
    }

    public User(String name, String password, String phone, String secureCode, double balance)
    {
        Name = name;
        Password = password;
        Phone = phone;
        IsStaff = "false";
        this.secureCode = secureCode;
        this.balance = balance;

    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getIsStaff() {
        return IsStaff;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }

    public String getName()
    {
        return Name;
    }

    public String getPhone() {
        return Phone;
    }

    public String setPhone(String phone) {
        return phone;
    }

    public void setName(String name)
    {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
