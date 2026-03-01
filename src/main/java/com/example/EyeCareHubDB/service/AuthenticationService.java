package com.example.EyeCareHubDB.service;

import com.example.EyeCareHubDB.dto.AccountResponse;
import com.example.EyeCareHubDB.dto.LoginRequest;
import com.example.EyeCareHubDB.dto.RegisterRequest;
import com.example.EyeCareHubDB.entity.Account;
import com.example.EyeCareHubDB.entity.Customer;
import com.example.EyeCareHubDB.repository.AccountRepository;
import com.example.EyeCareHubDB.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {
    
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    
    public Account register(RegisterRequest request) {
        // Check if email already exists
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }
        
        // Create new account
        Account account = Account.builder()
                .email(request.getEmail())
                .passwordHash(request.getPassword()) // TODO: Hash password properly with BCrypt
                .phoneNumber(request.getPhoneNumber())
                .role(Account.AccountRole.CUSTOMER)
                .status(Account.AccountStatus.ACTIVE)
                .build();
        
        Account savedAccount = accountRepository.save(account);
        
        // Create customer profile
        if (request.getFirstName() != null || request.getLastName() != null) {
            Customer customer = Customer.builder()
                    .account(savedAccount)
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .build();
            customerRepository.save(customer);
        }
        
        return savedAccount;
    }
    
    public AccountResponse login(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found: " + request.getEmail()));
        
        // TODO: Verify password with BCrypt
        if (!account.getPasswordHash().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        if (account.getStatus() == Account.AccountStatus.DELETED || 
            account.getStatus() == Account.AccountStatus.SUSPENDED) {
            throw new RuntimeException("Account is not active");
        }
        
        // Update last login time
        account.setLastLoginAt(LocalDateTime.now());
        accountRepository.save(account);
        
        // TODO: Generate JWT token
        String token = generateToken(account);
        
        // Get customer info if exists
        Customer customer = customerRepository.findByAccountId(account.getId()).orElse(null);
        
        return AccountResponse.builder()
                .id(account.getId())
                .email(account.getEmail())
                .role(account.getRole().name())
                .phoneNumber(account.getPhoneNumber())
                .firstName(customer != null ? customer.getFirstName() : null)
                .lastName(customer != null ? customer.getLastName() : null)
                .token(token)
                .message("Login successfully")
                .build();
    }
    
    public void forgotPassword(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found: " + email));
        
        // TODO: Generate password reset token and send email
        // For now, just a placeholder
        String resetToken = UUID.randomUUID().toString();
        // Store token in cache or database with expiration
    }
    
    public void resetPassword(String password) {
        // TODO: Verify token and update password
        // This should be called after validating the reset token from SecurityContext
    }
    
    public Account createAccountByAdmin(RegisterRequest request) {
        return register(request);
    }
    
    private String generateToken(Account account) {
        // TODO: Implement JWT token generation
        return UUID.randomUUID().toString();
    }
}
