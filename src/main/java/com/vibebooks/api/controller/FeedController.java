package com.vibebooks.api.controller;

import com.vibebooks.api.dto.BookFeedDTO;
import com.vibebooks.api.dto.PageResponseDTO;
import com.vibebooks.api.model.User;
import com.vibebooks.api.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.vibebooks.api.util.ApiConstants.API_PREFIX;

@RestController
@RequestMapping(API_PREFIX + "/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<BookFeedDTO>> getFeed(
            @PageableDefault(size = 7, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User loggedInUser
    ) {
        var feedPage = feedService.getBookFeed(pageable, loggedInUser);
        return ResponseEntity.ok(feedPage);
    }
}