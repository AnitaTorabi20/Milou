package aut.ap.service;

import aut.ap.dao.UserDao;
import aut.ap.entity.User;

public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void signup(String name, String email, String password) throws Exception {
        User existingUser = userDao.findByEmail(email);
        if (existingUser != null) {
            throw new Exception("User already exists with email: " + email);
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);

        userDao.saveUser(user);
    }

    public User login(String email, String password) throws Exception {
        User user = userDao.findByEmail(email);
        if (user == null) {
            throw new Exception("No user found with email: " + email);
        }

        if (!user.getPassword().equals(password)) {
            throw new Exception("Incorrect password");
        }

        return user;
    }

    public boolean userExists(String email) {
        return userDao.existsByEmail(email);
    }
}
