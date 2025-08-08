package aut.ap.controller;

import aut.ap.entity.Email;
import aut.ap.entity.User;
import aut.ap.service.EmailService;
import aut.ap.util.HibernateUtil;
import org.hibernate.Session;


import java.util.*;

public class EmailController {
    private final EmailService emailService;
    private final Scanner scanner;

    public EmailController(EmailService emailService, Scanner scanner) {
        this.emailService = emailService;
        this.scanner = scanner;
    }

    public static void showUnreadEmails(User currentUser) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Email> unreadEmails = session.createQuery("""
            FROM Email e
            WHERE e.recipient = :email AND e.isRead = false
            ORDER BY e.sent_time DESC
        """, Email.class)
                    .setParameter("email", currentUser.getEmail())
                    .list();

            if (unreadEmails.isEmpty()) {
                System.out.println("You have no unread emails.\n");
            } else {
                System.out.println("Unread Emails:\n");
                System.out.println(unreadEmails.size() + " unread emails:");
                for (Email e : unreadEmails) {
                    System.out.println("+ " + e.getSender() + " - " + e.getSubject() + " (" + e.getCode() + ")");
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch unread emails: " + e.getMessage());
        }
    }

    public static void sendEmail(Scanner scanner, EmailService emailService, String senderEmail) {
        System.out.print("Recipient(s): ");
        String recipientInput = scanner.nextLine().trim();

        String[] recipientsRaw = recipientInput.split("\\s*,\\s*");

        List<String> recipients = new ArrayList<>();
        for (String r : recipientsRaw) {
            if (!r.contains("@")) {
                recipients.add(r + "@milou.com");
            } else {
                recipients.add(r);
            }
        }

        System.out.print("Subject: ");
        String subject = scanner.nextLine();

        System.out.print("Body: ");
        String body = scanner.nextLine();

        try {
            List<Email> sentEmails = emailService.sendEmail(senderEmail, recipients, subject, body);

            if (sentEmails.isEmpty()) {
                System.out.println("No valid recipients. Email not sent.");
            } else {
                System.out.println("Successfully sent your email.");
                for (Email e : sentEmails) {
                    System.out.println("Code: " + e.getCode());
                }
            }
        } catch (Exception ex) {
            System.out.println("Failed to send email: " + ex.getMessage());
        }
    }

    public static String generateRandomCode(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int idx = (int) (Math.random() * chars.length());
            code.append(chars.charAt(idx));
        }
        return code.toString();
    }

    public static void readEmails(Scanner scanner, EmailService emailService, String userEmail) {
        System.out.println("[A]ll emails, [U]nread emails, [S]ent emails, Read by [C]ode: ");
        String choice = scanner.nextLine().trim().toUpperCase();

        List<Email> emails = null;
        boolean isSent = false;

        try {
            switch (choice) {
                case "A":
                    emails = emailService.getAllEmails(userEmail);
                    break;

                case "U":
                    emails = emailService.getUnreadEmails(userEmail);
                    if (emails == null || emails.isEmpty()) {
                        System.out.println("No unread emails found.");
                        return;
                    }

                    showEmailsList(emails, false);

                    System.out.print("Enter code of the email to read: ");
                    String chosenCode = scanner.nextLine().trim();

                    Email unreadEmail = emailService.getEmailByCodeIfAuthorized(chosenCode, userEmail);
                    if (unreadEmail != null) {
                        showSingleEmail(unreadEmail, emailService);
                        if (!unreadEmail.isRead()) {
                            emailService.markAsRead(unreadEmail);
                        }
                    } else {
                        System.out.println("Email not found or access denied.");
                    }
                    return;

                case "S":
                    emails = emailService.getSentEmails(userEmail);
                    isSent = true;
                    break;

                case "C":
                    System.out.print("Enter email code: ");
                    String code = scanner.nextLine().trim();

                    Email email = emailService.getEmailByCodeIfAuthorized(code, userEmail);
                    if (email != null) {
                        showSingleEmail(email, emailService);
                        if (!email.isRead() && email.getRecipient().equals(userEmail)) {
                            emailService.markAsRead(email);
                        }
                    } else {
                        System.out.println("You cannot read this email.");
                    }
                    return;

                default:
                    System.out.println("Invalid choice!");
                    return;
            }

            if (emails == null || emails.isEmpty()) {
                System.out.println("No emails found.");
            } else {
                showEmailsList(emails, isSent);
            }
        } catch (Exception e) {
            System.out.println("Failed to load emails: " + e.getMessage());
        }
    }

    public static void showEmailsList(List<Email> emails, boolean isSent) {
        System.out.println("\nEmails:");

        for (Email e : emails) {
            String who = isSent ? e.getRecipient() : e.getSender();
            System.out.println("+ " + who + " - " + e.getSubject() + " (" + e.getCode() + ")");
        }
    }

    public static void showSingleEmail(Email email, EmailService emailService) {
        if (!email.isRead()) {
            emailService.markAsRead(email);
        }

        List<Email> relatedEmails = emailService.getEmailsBySubjectAndSender(email.getSubject(), email.getSender());

        List<String> allRecipients = new ArrayList<>();
        for (Email e : relatedEmails) {
            String recipientsStr = e.getRecipient();
            String[] recipientsArray = recipientsStr.split("\\s*,\\s*");
            for (String r : recipientsArray) {
                if (!allRecipients.contains(r)) {
                    allRecipients.add(r);
                }
            }
        }

        System.out.println("\nEmail Details:");
        System.out.println("Code: " + email.getCode());
        System.out.println("Sender: " + email.getSender());
        System.out.println("Recipient(s): " + String.join(", ", allRecipients));
        System.out.println("Subject: " + email.getSubject());
        System.out.println("Date: " + email.getSent_time());
        System.out.println("Body:\n" + email.getBody());
    }

    public static void replyToEmail(Scanner scanner, EmailService emailService, String senderEmail) {
        System.out.print("Enter the code of the email you want to reply to: ");
        String originalCode = scanner.nextLine().trim();

        Email original = emailService.getEmailByCode(originalCode);
        if (original == null) {
            System.out.println("No email found with this code.");
            return;
        }

        if (!original.getRecipient().equalsIgnoreCase(senderEmail) && !original.getSender().equalsIgnoreCase(senderEmail)) {
            System.out.println("Access denied: You are not authorized to reply to this email.");
            return;
        }

        System.out.print("Body: ");
        String replyBody = scanner.nextLine().trim();

        List<Email> relatedEmails = emailService.getEmailsBySubjectAndSender(original.getSubject(), original.getSender());

        Set<String> recipients = new HashSet<>();
        for (Email e : relatedEmails) {
            String r = e.getRecipient();
            if (!r.equalsIgnoreCase(senderEmail)) {
                recipients.add(r);
            }
        }

        if (!original.getSender().equalsIgnoreCase(senderEmail)) {
            recipients.add(original.getSender());
        }

        if (recipients.isEmpty()) {
            System.out.println("No valid recipients to send reply.");
            return;
        }

        String replySubject = "[Re] " + original.getSubject();

        List<Email> sent = emailService.sendEmail(senderEmail, new ArrayList<>(recipients), replySubject, replyBody);

        for (Email reply : sent) {
            if (reply.getRecipient().equalsIgnoreCase(senderEmail)) {
                showSingleEmail(reply, emailService);
                System.out.println("Successfully sent your reply to email " + originalCode + ".");
                System.out.println("Code: " + reply.getCode());
                return;
            }
        }

        System.out.println("Successfully sent your reply to email " + originalCode + ".");
        for (Email reply : sent) {
            System.out.println("Code: " + reply.getCode());
        }

        if (replyBody.length() > 1000) {
            System.out.println("Your text is too long.");
        }
    }

    public static void forwardEmail(Scanner scanner, EmailService emailService, String senderEmail) {
        System.out.print("Enter the code of the email you want to forward: ");
        String originalCode = scanner.nextLine().trim();

        Email original = emailService.getEmailByCode(originalCode);
        if (original == null) {
            System.out.println("No email found with this code.");
            return;
        }

        System.out.print("Recipient(s): ");
        String recipientInput = scanner.nextLine().trim();

        String[] recipientsRaw = recipientInput.split("\\s*,\\s*");
        List<String> recipients = new ArrayList<>();
        for (String r : recipientsRaw) {
            if (!r.contains("@")) {
                recipients.add(r + "@milou.com");
            } else {
                recipients.add(r);
            }
        }

        String forwardSubject = "[Fw] " + original.getSubject();
        String forwardBody = original.getBody();

        try {
            List<Email> sentEmails = emailService.sendEmail(senderEmail, recipients, forwardSubject, forwardBody);

            if (sentEmails.isEmpty()) {
                System.out.println("No valid recipients. Email not forwarded.");
            } else {
                Email forwardedEmail = sentEmails.get(0);
                System.out.println("Successfully forwarded your email.");
                System.out.println("Code: " + forwardedEmail.getCode());
            }
        } catch (Exception ex) {
            System.out.println("Failed to forward email: " + ex.getMessage());
        }
    }

    public void searchEmails(String userEmail) {
        System.out.print("Enter keyword to search in subject or body: ");
        String keyword = scanner.nextLine().trim();

        List<Email> results = emailService.searchEmails(keyword, userEmail);

        if (results.isEmpty()) {
            System.out.println("No emails found with the given keyword.");
        } else {
            System.out.println("Found " + results.size() + " email(s):");
            for (Email email : results) {
                System.out.println(email);
            }
        }
    }
}
