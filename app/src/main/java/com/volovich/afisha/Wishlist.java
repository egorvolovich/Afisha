package com.volovich.afisha;

public class Wishlist {
    public Wishlist() {
    }

    private String uid;
    private String eventId;

    //not firestore, but local field
    private String documentId;


    public Wishlist(String uid, String eventId, String documentId) {
        this.uid = uid;
        this.eventId = eventId;
        this.documentId = documentId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
