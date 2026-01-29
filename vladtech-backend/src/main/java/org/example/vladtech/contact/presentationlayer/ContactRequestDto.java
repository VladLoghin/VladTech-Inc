package org.example.vladtech.contact.presentationlayer;

public class ContactRequestDto {

    private String email;       // client's email address
    private String name;        // client's name
    private String subject;     // title
    private String message;     // main message body

    private String userName;

    public ContactRequestDto() {
    }

    private String safe(String userName) {
        return userName == null ? "Unknown" : userName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserName() {
        return safe(userName);
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
