package com.vibebooks.api.service;

import com.vibebooks.api.dto.CommentCreationDTO;
import com.vibebooks.api.dto.CommentDetailsDTO;
import com.vibebooks.api.model.Comment;
import com.vibebooks.api.model.Like;
import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.BookRepository;
import com.vibebooks.api.repository.CommentRepository;
import com.vibebooks.api.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BookRepository bookRepository;
    private final LikeRepository likeRepository;

    @Transactional
    public Comment addCommentToBook(CommentCreationDTO dto, User loggedInUser) {
        var book = bookRepository.findById(dto.bookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        var comment = new Comment(dto.text(), loggedInUser, book);
        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDetailsDTO> findCommentsByBookId(UUID bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
        return commentRepository.findAllByBookId(bookId).stream()
                .map(CommentDetailsDTO::new)
                .toList();
    }

    @Transactional
    public void deleteComment(UUID commentId, User loggedInUser) {
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (!comment.getUser().getId().equals(loggedInUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public void likeOrUnlikeComment(UUID commentId, User loggedInUser) {
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        var likeOptional = likeRepository.findByUserIdAndCommentId(loggedInUser.getId(), comment.getId());

        if (likeOptional.isPresent()) {
            likeRepository.delete(likeOptional.get());
        } else {
            likeRepository.save(new Like(loggedInUser, comment));
        }
    }
}