package aut.ap.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email")
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String body;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private LocalDateTime sent_time;

    @Column(nullable = false)
    private boolean isRead;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getSent_time() { return sent_time; }
    public void setSent_time(LocalDateTime sent_time) { this.sent_time = sent_time; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean isRead) { this.isRead = isRead; }

    public void changeRead() {
        this.isRead = !this.isRead;
    }

    @Override
    public String toString() {
        return "Email Details:\n" +
                "Code: " + code + "\n" +
                "Sender: " + sender + "\n" +
                "Recipient(s): " + recipient + "\n" +
                "Subject: " + subject + "\n" +
                "Date: " + sent_time + "\n" +
                "Body:\n" + body + "\n";
    }

}
