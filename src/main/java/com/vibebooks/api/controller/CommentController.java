package com.vibebooks.api.controller;

import com.vibebooks.api.model.Like;
import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.CommentRepository;
import com.vibebooks.api.repository.LikeRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public CommentController(CommentRepository commentRepository, LikeRepository likeRepository) {
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id, Authentication authentication) {
        var optionalComment = commentRepository.findById(id);
        if (optionalComment.isEmpty()) {
            return  ResponseEntity.notFound().build();
        }
        var comment = optionalComment.get();

        var loggedInUser = (User) authentication.getPrincipal();

        if (!comment.getUser().getId().equals(loggedInUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        commentRepository.delete(comment);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    @Transactional
    public ResponseEntity<Void> likeOrUnlikeComment(@PathVariable UUID id, Authentication authentication) {
        var loggedInUser = (User) authentication.getPrincipal();

        var comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        var optionalLike = likeRepository.findByUserIdAndCommentId(loggedInUser.getId(), comment.getId());

        if(optionalLike.isPresent()) {
            likeRepository.delete(optionalLike.get());
        } else {
            likeRepository.save(new Like(loggedInUser, comment));
        }
        return ResponseEntity.ok().build();
    }
}
