package org.example;

import lombok.Data;
import lombok.Setter;

@Data
public class Apartment {

    private String id;
    private int amountOfRooms;
    private double sqr;
    private String flour;
    private String address;
    private long price;
    private String link;
    private String phoneNumber;
    @Setter
    private String author;
    @Setter
    private String mark;

    public Apartment(String id, int amountOfRooms, double sqr, String flour, String address, long price, String link, String phoneNumber) {
        this.id = id;
        this.amountOfRooms = amountOfRooms;
        this.sqr = sqr;
        this.flour = flour;
        this.address = address;
        this.price = price;
        this.link = link;
        this.phoneNumber = phoneNumber;
    }
}
