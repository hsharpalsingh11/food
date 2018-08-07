package com.example.singh.myapplication.Model;

import java.util.List;

public class Request
{
    private String phone;
    private String address;
    private String paymentState;
    private String total;
    private String name;
    private String paymentMethod;
    private String status;
    private String comment;
    private String latlng;
    private List<Order> foods;

    public Request() {
    }

    public Request(String phone, String address, String paymentState, String paymentMethod, String total,
                   String name, String status, String comment, String latlng, List<Order> foods) {
        this.phone = phone;
        this.address = address;
        this.paymentState = paymentState;
        this.paymentMethod = paymentMethod;
        this.total = total;
        this.name = name;
        this.status = status;
        this.comment = comment;
        this.latlng = latlng;
        this.foods = foods;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(String paymentState) {
        this.paymentState = paymentState;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getLatlng() {
        return latlng;
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }

    public List<Order> getFoods() {
        return foods;
    }

    public void setFoods(List<Order> foods) {
        this.foods = foods;
    }
}


