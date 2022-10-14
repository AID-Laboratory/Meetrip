package com.hsu.aidlab.meetrip;

public class Model {

    private String imageUrl;

    Model(){

    }

    public Model(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
