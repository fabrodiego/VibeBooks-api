package com.vibebooks.api.service;

import com.vibebooks.api.dto.UserResponseDTO;
import com.vibebooks.api.model.Follow;
import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.FollowRepository;
import com.vibebooks.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void followUser(User follower, UUID userIdToFollow) {
        if (follower.getId().equals(userIdToFollow)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User cannot follow themselves.");
        }

        var userToFollow = userRepository.findById(userIdToFollow)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User to follow not found."));

        boolean alreadyFollowing = followRepository.existsByFollowerIdAndFollowingId(follower.getId(), userToFollow.getId());
        if (alreadyFollowing) {
            return;
        }

        var follow = new Follow(follower, userToFollow);
        followRepository.save(follow);
    }

    @Transactional
    public void unfollowUser(User follower, UUID userIdToUnfollow) {
        var userToUnfollow = userRepository.findById(userIdToUnfollow)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User to unfollow not found."));

        followRepository.findById(new com.vibebooks.api.model.FollowId(follower.getId(), userToUnfollow.getId()))
                .ifPresent(followRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getFollowing(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }

        return followRepository.findAllByFollowerId(userId)
                .stream()
                .map(Follow::getFollowing)
                .map(user -> new UserResponseDTO(user.getId(), user.getUsername(), user.getEmail(), user.getBio()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getFollowers(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }

        return followRepository.findAllByFollowingId(userId)
                .stream()
                .map(Follow::getFollower)
                .map(user -> new UserResponseDTO(user.getId(), user.getUsername(), user.getEmail(), user.getBio()))
                .collect(Collectors.toList());
    }
}