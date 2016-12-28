package com.dialogGator;

import static java.lang.Integer.parseInt;

public class Product {

    public String id;
    public String title;
    public String gender;
    public String category;
    public double price;
    public String color;
    public String size;
    public String brand;



    public String imgUrl;

    public Product(){}
    public Product(String itemId, String name, String description, double price)
    {
        this.id = itemId;
        this.title = name;
        this.price = price;
    }
    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
    public String getName()
    {
        return title;
    }

    public String getProductId()
    {
        return id;
    }

    public String getDescription()
    {
        return "Our boy-cut jeans are for men and women who appreciate that skate park fashions aren’t just for skaters.  Made from the softest and most flexible organic cotton denim";
    //return "";
    }

    public double getPrice()
    {
        return price;
    }
}
