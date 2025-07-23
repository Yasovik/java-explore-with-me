package ru.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comments.dto.CommentResponseDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exceptions.AccessDeniedException;
import ru.practicum.exceptions.CommentNotFoundException;
import ru.practicum.exceptions.EventNotFoundException;
import ru.practicum.exceptions.UserNotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.comments.mapper.CommentMapper.toComment;
import static ru.practicum.comments.mapper.CommentMapper.toCommentResponseDto;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CommentResponseDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        Comment comment = toComment(dto);
        comment.setCreatedOn(LocalDateTime.now());
        comment.setAuthorId(user);
        comment.setEventId(event);

        return toCommentResponseDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentResponseDto updateComment(Long userId, Long commentId, NewCommentDto dto) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.getAuthorId().getId().equals(userId)) {
            throw new AccessDeniedException(userId);
        }

        comment.setText(dto.getText());
        comment.setUpdatedOn(LocalDateTime.now());

        return toCommentResponseDto(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByEventId(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        return commentRepository.findByEventId(event)
                .stream()
                .map(CommentMapper::toCommentResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponseDto getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        return toCommentResponseDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.getAuthorId().getId().equals(userId)) {
            throw new AccessDeniedException(userId);
        }

        commentRepository.delete(comment);
    }

}
