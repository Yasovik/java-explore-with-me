package ru.practicum.comments.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentResponseDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Validated
public class CommentController {
    private final CommentService service;

    @PostMapping("/user/{userId}/events/{eventId}/comment/")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto createComment(@PathVariable @Positive Long userId,
                                            @PathVariable @Positive Long eventId,
                                            @RequestBody NewCommentDto newCommentDto) {
        return service.createComment(userId, eventId, newCommentDto);
    }

    @PatchMapping("/user/{userId}/comment/{commentId}")
    public CommentResponseDto updateComment(@PathVariable @Positive Long userId,
                                            @PathVariable @Positive Long commentId,
                                            @RequestBody NewCommentDto newCommentDto) {
        return service.updateComment(userId, commentId, newCommentDto);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentResponseDto> getCommentsByEventId(@PathVariable @Positive Long eventId) {
        return service.getCommentsByEventId(eventId);
    }

    @GetMapping("/comment/{commentId}")
    public CommentResponseDto getCommentById(@PathVariable @Positive Long commentId) {
        return service.getCommentById(commentId);
    }

    @DeleteMapping("/user/{userId}/comment/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive Long userId,
                              @PathVariable @Positive Long commentId) {
        service.deleteComment(userId, commentId);
    }
}
