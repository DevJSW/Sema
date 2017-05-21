package com.sema.sema;

/**
 * Created by John on 31-Oct-16.
 */
public class Chat {

    private String name, image, photo, date, message, hashtag;

    public Chat() {

    }

    public Chat(String name, String photo, String image, String date, String message, String hashtag) {
        this.name = name;
        this.image = image;

        this.photo = photo;
        this.date = date;
        this.message = message;
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

    public String getPhoto() {
        return photo;
    }
    public void setPhoto(String photo) {
        this.photo = photo;
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

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }
}
