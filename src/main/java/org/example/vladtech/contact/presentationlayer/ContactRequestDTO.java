package org.example.vladtech.contact.presentationlayer;

ublic class ContactRequestDto {

    private String email;       // client's email address
    private String name;        // client's name
    private String subject;     // title
    private String message;     // main message body

    public ContactRequestDto() {
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
}