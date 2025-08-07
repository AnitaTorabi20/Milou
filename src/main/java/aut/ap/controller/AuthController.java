package aut.ap.controller;

import aut.ap.entity.User;
import aut.ap.service.UserService;

import java.util.Scanner;

import static aut.ap.controller.EmailController.showUnreadEmails;

public class AuthController {
    private final UserService userService;
    private final Scanner scanner;

    public AuthController(UserService userService, Scanner scanner) {
        this.userService = userService;
        this.scanner = scanner;
    }

    public User handleSignup() {
        while (true) {
            System.out.print("Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Email: ");
            String emailInput = scanner.nextLine().trim();
            String email = fixEmail(emailInput);

            System.out.print("Password: ");
            String password = scanner.nextLine();

            if (password.length() < 8) {
                System.out.println("Password must be at least 8 characters. Please try again.");
                continue;
            }

            try {
                userService.signup(name, email, password);
                System.out.println("Your new account is created.\nGo ahead and login!");
                return null;
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Please try again.");
            }
        }
    }

    public User handleLogin() {
        System.out.print("Email: ");
        String emailInput = scanner.nextLine().trim();
        String email = fixEmail(emailInput);

        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            User user = userService.login(email, password);
            System.out.println("Welcome back, " + user.getName() + "!");
            showUnreadEmails(user);
            return user;
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            return null;
        }
    }

    private static String fixEmail(String emailInput) {
        if (!emailInput.contains("@")) {
            return emailInput + "@milou.com";
        }
        return emailInput;
    }
}
