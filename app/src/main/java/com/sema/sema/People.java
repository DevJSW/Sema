package com.sema.sema;

/**
 * Created by John on 31-Oct-16.
 */
public class People {

    private String name, image, date, message, status, hashtag;

    public People() {

    }

    public People(String name, String image, String date, String message, String status, String hashtag) {
        this.name = name;

        this.image = image;
        this.date = date;
        this.message = message;
        this.status = status;
        this.hashtag = hashtag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }
}
