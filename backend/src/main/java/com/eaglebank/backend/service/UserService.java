package com.eaglebank.backend.service;

import com.eaglebank.backend.dto.AddressDto;
import com.eaglebank.backend.dto.CreateUserRequest;
import com.eaglebank.backend.dto.UserResponse;
import com.eaglebank.backend.dto.UpdateUserRequest;
import com.eaglebank.backend.exception.UserDeletionConflictException;
import com.eaglebank.backend.exception.ResourceNotFoundException;
import com.eaglebank.backend.model.Address;
import com.eaglebank.backend.model.User;
import com.eaglebank.backend.repository.BankAccountRepository;
import com.eaglebank.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor public class UserService {
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional public UserResponse registerUser(CreateUserRequest request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .address(mapToAddress(request.getAddress()))
                .build();

        User savedUser = userRepository.saveAndFlush(user);
        System.out.println(user.getCreatedTimestamp());
        return mapToUserResponse(savedUser);
    }

    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToUserResponse(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public void deleteUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean hasAccounts = bankAccountRepository.existsByUser_Id(userId);
        if (hasAccounts) {
            throw new UserDeletionConflictException("User cannot be deleted because they have associated bank accounts");
        }

        userRepository.delete(user);
    }

    public boolean hasBankAccounts(String userId) {
        return bankAccountRepository.existsByUser_Id(userId);
    }

    @Transactional
    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getAddress() != null) {
            user.setAddress(mapToAddress(request.getAddress()));
        }

        User saved = userRepository.saveAndFlush(user);
        return mapToUserResponse(saved);
    }

    private Address mapToAddress(AddressDto dto) {
        if (dto == null) return null;
        return Address.builder()
                .line1(dto.getLine1())
                .line2(dto.getLine2())
                .line3(dto.getLine3())
                .town(dto.getTown())
                .county(dto.getCounty())
                .postcode(dto.getPostcode())
                .build();
    }

    private AddressDto mapToAddressDto(Address address) {
        if (address == null) return null;
        return AddressDto.builder()
                .line1(address.getLine1())
                .line2(address.getLine2())
                .line3(address.getLine3())
                .town(address.getTown())
                .county(address.getCounty())
                .postcode(address.getPostcode())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(mapToAddressDto(user.getAddress()))
                .createdTimestamp(user.getCreatedTimestamp())
                .updatedTimestamp(user.getUpdatedTimestamp())
                .build();
    }
}