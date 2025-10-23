package com.vibebooks.api.controller;

import com.vibebooks.api.dto.CommentCreationDTO;
import com.vibebooks.api.dto.CommentDetailsDTO;
import com.vibebooks.api.model.User;
import com.vibebooks.api.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

import static com.vibebooks.api.util.ApiConstants.API_PREFIX;

@RestController
@RequestMapping(API_PREFIX + "/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDetailsDTO> addComment(
            @RequestBody @Valid CommentCreationDTO dto,
            @AuthenticationPrincipal User loggedInUser,
            UriComponentsBuilder uriBuilder
    ) {
        var newComment = commentService.addCommentToBook(dto, loggedInUser);
        var uri = uriBuilder.path("/api/comments/{id}").buildAndExpand(newComment.getId()).toUri();
        long initialLikes = 0;
        boolean likedByCurrentUser = false;
        return ResponseEntity.created(uri).body(new CommentDetailsDTO(newComment, initialLikes, likedByCurrentUser));
    }

    @GetMapping
    public ResponseEntity<List<CommentDetailsDTO>> listCommentsByBook(
            @RequestParam(value = "bookId") UUID bookId,
            @AuthenticationPrincipal User loggedInUser) {
        var dtos = commentService.findCommentsByBookId(bookId, loggedInUser);
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID id,
            @AuthenticationPrincipal User loggedInUser) {
        commentService.deleteComment(id, loggedInUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<CommentDetailsDTO> likeOrUnlikeComment(
            @PathVariable UUID id,
            @AuthenticationPrincipal User loggedInUser
    ) {
        var updatedCommentDetails = commentService.likeOrUnlikeComment(id, loggedInUser);
        return ResponseEntity.ok(updatedCommentDetails);
    }
}