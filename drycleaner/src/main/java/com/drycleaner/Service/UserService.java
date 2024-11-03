package com.drycleaner.Service;

import com.drycleaner.Model.PasswordResetToken;
import com.drycleaner.Model.UserModel;
import com.drycleaner.repository.PasswordResetTokenRepository;
import com.drycleaner.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;

    @Autowired
    public UserService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
    }

    public UserModel saveUser(UserModel user) {
        return userRepository.save(user);
    }

    public Optional<UserModel> login(String email, String password) {
        Optional<UserModel> user = userRepository.findByEmail(email);
        return user.filter(u -> u.getPassword().equals(password));
    }

    // Retrieve all users with pagination and sorting, excluding administrators
    public Page<UserModel> getAllUsers(Pageable pageable) {
        Page<UserModel> usersPage = userRepository.findAll(pageable);
        List<UserModel> filteredUsers = usersPage.getContent().stream()
                .filter(user -> !user.getRole().equalsIgnoreCase("administrator"))
                .collect(Collectors.toList());

        return new PageImpl<>(filteredUsers, pageable, usersPage.getTotalElements());
    }

    // Search users by username, excluding administrators
    public Page<UserModel> searchUsers(String search, Pageable pageable) {
        Page<UserModel> usersPage = userRepository.findByUsernameContainingIgnoreCase(search, pageable);
        List<UserModel> filteredUsers = usersPage.getContent().stream()
                .filter(user -> !user.getRole().equalsIgnoreCase("administrator"))
                .collect(Collectors.toList());

        return new PageImpl<>(filteredUsers, pageable, usersPage.getTotalElements());
    }

    // Find a user by ID
    public UserModel getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user ID:" + id));
    }

    // Delete a user by ID
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Create Password Reset Token
    public String createPasswordResetToken(String email) {
        Optional<UserModel> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user.get(), new Date(System.currentTimeMillis() + 30 * 60 * 1000)); // 30 minutes expiry
            tokenRepository.save(resetToken);
            return token;
        }
        return null;
    }

    // Send Password Reset Email
    public void sendResetEmail(String email, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("Password Reset Request");
            helper.setText("<p>You requested a password reset. Click the link below to reset your password:</p>" +
                    "<p><a href=\"" + resetLink + "\">Reset Password</a></p>", true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            // Handle exception
        }
    }

    // Validate Password Reset Token
    public boolean validateToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);
        return resetToken.isPresent() && resetToken.get().getExpiryDate().after(new Date());
    }

    // Update Password with Token Verification
    public boolean updatePassword(String token, String password) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);
        if (resetToken.isPresent() && resetToken.get().getExpiryDate().after(new Date())) {
            UserModel user = resetToken.get().getUser();
            user.setPassword(password); // Ensure to hash the password in real implementations
            userRepository.save(user);
            tokenRepository.delete(resetToken.get()); // Clear token after reset
            return true;
        }
        return false;
    }
}
