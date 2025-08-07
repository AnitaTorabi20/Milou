package aut.ap.service;

import aut.ap.dao.EmailDao;
import aut.ap.entity.Email;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EmailService {

    private final EmailDao emailDao;
    private final Random random = new Random();

    public EmailService(EmailDao emailDao) {
        this.emailDao = emailDao;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (emailDao.isCodeExists(code));
        return code;
    }

    private String generateRandomCode() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public List<Email> sendEmail(String sender, List<String> recipients, String subject, String body) {
        List<Email> emails = new ArrayList<>();

        for (String recipient : recipients) {
            if (emailDao.recipientExists(recipient)) {
                Email email = new Email();
                email.setSender(sender);
                email.setRecipient(recipient);
                email.setSubject(subject);
                email.setBody(body);
                email.setSent_time(LocalDateTime.now());
                email.setRead(false);
                email.setCode(generateUniqueCode());

                emailDao.saveEmail(email);
                emails.add(email);
            } else {
                System.out.println("Recipient not found in system: " + recipient);
            }
        }

        return emails;
    }

    public List<Email> getUnreadEmails(String recipient) {
        return emailDao.getUnreadEmails(recipient);
    }

    public List<Email> getAllEmails(String userEmail) {
        return emailDao.getAllEmails(userEmail);
    }

    public Email getEmailByCode(String code) {
        return emailDao.getEmailByCode(code);
    }

    public void markAsRead(Email email) {
        emailDao.markAsRead(email);
    }

    public List<Email> getSentEmails(String senderEmail) {
        return emailDao.getSentEmails(senderEmail);
    }

    public Email getEmailByCodeIfAuthorized(String code, String userEmail) {
        return emailDao.getEmailByCodeIfAuthorized(code, userEmail);
    }

    public List<Email> getEmailsBySubjectAndSender(String subject, String sender) {
        return emailDao.getEmailsBySubjectAndSender(subject, sender);
    }
}
