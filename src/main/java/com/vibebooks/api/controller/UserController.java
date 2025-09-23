package com.vibebooks.api.controller;

import com.vibebooks.api.dto.UserCreateDTO;
import com.vibebooks.api.dto.UserResponseDTO;
import com.vibebooks.api.model.User;
import com.vibebooks.api.service.FollowService;
import com.vibebooks.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FollowService followService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO createUser(@RequestBody UserCreateDTO dto){
        User newUser = userService.createUser(dto);
        return new UserResponseDTO(
                newUser.getId(),
                newUser.getUsername(),
                newUser.getEmail()
        );
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
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
}
