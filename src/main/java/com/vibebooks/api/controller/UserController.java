package com.vibebooks.api.controller;

import com.vibebooks.api.dto.UserPasswordUpdateDTO;
import com.vibebooks.api.dto.UserResponseDTO;
import com.vibebooks.api.dto.UserUpdateDTO;
import com.vibebooks.api.model.User;
import com.vibebooks.api.service.FollowService;
import com.vibebooks.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.vibebooks.api.util.ApiConstants.API_PREFIX;

@RestController
@RequestMapping(API_PREFIX + "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FollowService followService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable UUID id,
            @RequestBody @Valid UserUpdateDTO dto,
            @AuthenticationPrincipal User loggedInUser) {
        User updatedUser = userService.updateUser(id, dto, loggedInUser);
        return ResponseEntity.ok(new UserResponseDTO(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail(), updatedUser.getBio()));
    }

    @PutMapping("{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID id,
            @RequestBody @Valid UserPasswordUpdateDTO dto,
            @AuthenticationPrincipal User loggedInUser) {
        userService.changePassword(id, dto, loggedInUser);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, @AuthenticationPrincipal User loggedInUser) {
        userService.deleteUser(id, loggedInUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("{id}/follow")
    public ResponseEntity<Void> followUser(@PathVariable UUID id, @AuthenticationPrincipal User loggedInUser) {
        followService.followUser(loggedInUser, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/follow")
    public ResponseEntity<Void> unfollowUser(@PathVariable UUID id, @AuthenticationPrincipal User loggedInUser) {
        followService.unfollowUser(loggedInUser, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<List<UserResponseDTO>> listFollowing(@PathVariable UUID id) {
        return ResponseEntity.ok(followService.getFollowing(id));
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<List<UserResponseDTO>> listFollowers(@PathVariable UUID id) {
        return ResponseEntity.ok(followService.getFollowers(id));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile(@AuthenticationPrincipal User loggedInUser) {
        return ResponseEntity.ok(new UserResponseDTO(loggedInUser));
    }
}
