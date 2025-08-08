package aut.ap.dao;

import aut.ap.entity.Email;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import aut.ap.util.HibernateUtil;

import java.util.List;

public class EmailDao {

    private final SessionFactory sessionFactory;

    public EmailDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public boolean isCodeExists(String code) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("SELECT COUNT(e) FROM Email e WHERE e.code = :code", Long.class)
                    .setParameter("code", code)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    public void saveEmail(Email email) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(email);
            tx.commit();
        }
    }

    public List<Email> getUnreadEmails(String recipient) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Email WHERE recipient = :recipient AND isRead = false ORDER BY sent_time DESC", Email.class)
                    .setParameter("recipient", recipient)
                    .list();
        }
    }

    public List<Email> getAllEmails(String userEmail) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "FROM Email WHERE recipient = :email ORDER BY sent_time DESC",
                            Email.class
                    )
                    .setParameter("email", userEmail)
                    .list();
        }
    }

    public Email getEmailByCode(String code) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Email WHERE code = :code", Email.class)
                    .setParameter("code", code)
                    .uniqueResult();
        }
    }

    public void markAsRead(Email email) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            email.setRead(true);
            session.merge(email);
            tx.commit();
        }
    }

    public List<Email> getSentEmails(String senderEmail) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Email e WHERE e.sender = :senderEmail ORDER BY e.sent_time DESC", Email.class)
                    .setParameter("senderEmail", senderEmail)
                    .list();
        }
    }

    public Email getEmailByCodeIfAuthorized(String code, String userEmail) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Email e WHERE e.code = :code AND (e.recipient = :userEmail OR e.sender = :userEmail)", Email.class)
                    .setParameter("code", code)
                    .setParameter("userEmail", userEmail)
                    .uniqueResult();
        }
    }

    public List<Email> getEmailsBySubjectAndSender(String subject, String sender) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Email e WHERE e.subject = :subject AND e.sender = :sender", Email.class)
                    .setParameter("subject", subject)
                    .setParameter("sender", sender)
                    .list();
        }
    }

    public boolean recipientExists(String recipientEmail) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                    .setParameter("email", recipientEmail)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    public List<Email> searchEmails(String keyword, String userEmail) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Email e " +
                    "WHERE (e.subject LIKE :keyword OR e.body LIKE :keyword) " +
                    "AND (e.sender = :userEmail OR e.recipient = :userEmail) " +
                    "ORDER BY e.sent_time DESC";

            return session.createQuery(hql, Email.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .setParameter("userEmail", userEmail)
                    .getResultList();
        }
    }

    public void deleteEmail(Email email) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.delete(email);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public void updateEmail(Email email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.update(email);
            tx.commit();
        }
    }


}
