package aut.ap;

import org.hibernate.SessionFactory;

import java.util.Scanner;

import aut.ap.controller.AuthController;
import aut.ap.controller.EmailController;
import aut.ap.entity.*;
import aut.ap.service.*;
import aut.ap.util.HibernateUtil;
import aut.ap.dao.*;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        UserDao userDao = new UserDao(sessionFactory);
        EmailDao emailDao = new EmailDao(sessionFactory);

        UserService userService = new UserService(userDao);
        EmailService emailService = new EmailService(emailDao);

        AuthController authController = new AuthController(userService, scanner);
        EmailController emailController = new EmailController(emailService, scanner);


        while (true) {
            System.out.println("[L]ogin, [S]ign up:");
            String choice = scanner.nextLine().trim();

            if (choice.equalsIgnoreCase("S") || choice.equalsIgnoreCase("Sign up")) {
                authController.handleSignup();

            } else if (choice.equalsIgnoreCase("L") || choice.equalsIgnoreCase("Login")) {
                User loggedInUser = null;

                while (loggedInUser == null) {
                    loggedInUser = authController.handleLogin();
                    if (loggedInUser == null) {
                        System.out.println("Try again.\n");
                    }
                }

                boolean loggedIn = true;
                while (loggedIn) {
                    System.out.println("\nAvailable Commands:");
                    System.out.println("[S]end, [V]iew, [R]eply, [F]orward, [Se]arch,[Ch]ange read status, [D]elete, [Q]uit:");

                    String cmd = scanner.nextLine().trim();

                    switch (cmd.toUpperCase()) {
                        case "S":
                            EmailController.sendEmail(scanner, emailService, loggedInUser.getEmail());
                            break;
                        case "V":
                            EmailController.readEmails(scanner, emailService, loggedInUser.getEmail());
                            break;
                        case "R":
                            EmailController.replyToEmail(scanner, emailService, loggedInUser.getEmail());
                            break;
                        case "F":
                            EmailController.forwardEmail(scanner, emailService, loggedInUser.getEmail());
                            break;
                        case "SE":
                            emailController.searchEmails(loggedInUser.getEmail());
                            break;
                        case "D":
                            emailController.deleteEmail(loggedInUser.getEmail());
                            break;
                        case "CH":
                            emailController.changeReadStatus(loggedInUser.getEmail());
                            break;
                        case "Q":
                            System.out.println("Logging out...");
                            loggedIn = false;
                            break;
                        default:
                            System.out.println("Invalid command.");
                    }
                }
            }
        }
    }
}
