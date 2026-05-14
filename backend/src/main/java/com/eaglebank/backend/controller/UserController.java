package com.eaglebank.backend.controller;

import com.eaglebank.backend.dto.CreateUserRequest;
import com.eaglebank.backend.dto.ErrorResponse;
import com.eaglebank.backend.dto.UserResponse;
import com.eaglebank.backend.dto.UpdateUserRequest;
import com.eaglebank.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController @RequestMapping("/v1/users") @RequiredArgsConstructor @Validated
public class UserController {
    private final UserService userService;

    @PostMapping public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request)
    {
        return new ResponseEntity<>(userService.registerUser(request), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> fetchUserByID(
            @jakarta.validation.constraints.Pattern(regexp = "^usr-[A-Za-z0-9]+$", message = "userId must match ^usr-[A-Za-z0-9]+$")
            @PathVariable String userId,
            Principal principal) {

        var user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User not found"));
        }

        if (principal != null && !principal.getName().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("You are not allowed to access this resource"));
        }


        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @jakarta.validation.constraints.Pattern(regexp = "^usr-[A-Za-z0-9]+$", message = "userId must match ^usr-[A-Za-z0-9]+$")
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request,
            Principal principal) {

        var user = userService.getUserById(userId);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User not found"));
        }

        if (principal != null && !principal.getName().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("You are not allowed to access this resource"));
        }
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
            @jakarta.validation.constraints.Pattern(regexp = "^usr-[A-Za-z0-9]+$", message = "userId must match ^usr-[A-Za-z0-9]+$")
            @PathVariable String userId,
            Principal principal) {

        var user = userService.getUserById(userId);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User not found"));
        }

        if (!principal.getName().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("You are not allowed to access this resource"));
        }

        if (userService.hasBankAccounts(userId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("User cannot be deleted when associated with bank accounts"));
        }

        userService.deleteUserById(userId);

        return ResponseEntity.noContent().build();
    }
}