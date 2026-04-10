package com.community.center;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashTest {
    
    @Test
    public void testPasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String hashedPassword = "$2a$10$7JB720yubVSOfvVWbfXCOOxjTOQcQjmrJF1ZM4nAVccp/.rkMlDWy";
        
        System.out.println("Password: " + password);
        System.out.println("Hashed Password: " + hashedPassword);
        System.out.println("Matches: " + encoder.matches(password, hashedPassword));
        
        // 生成新的哈希值
        String newHash = encoder.encode(password);
        System.out.println("New Hash: " + newHash);
        System.out.println("New Hash Matches: " + encoder.matches(password, newHash));
    }
}