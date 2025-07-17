package ru.practicum.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.request.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
@Validated
public class RequestController {

    private final RequestService requestService;

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto createParticipationRequest(@PathVariable @Positive Long userId,
                                                 @RequestParam @Positive Long eventId) {
        return requestService.createParticipationRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public RequestDto cancelParticipationRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        return requestService.cancelParticipationRequest(userId, requestId);
    }

    @GetMapping("/requests")
    public List<RequestDto> getParticipationRequests(@PathVariable @Positive Long userId) {
        return requestService.getParticipationRequests(userId);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<RequestDto> getParticipationRequestsForUserEvent(@PathVariable @Positive Long userId,
                                                                 @PathVariable @Positive Long eventId) {
        return requestService.getParticipationRequestsForUserEvent(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResultDto changeParticipationRequestsStatus(@PathVariable @Positive Long userId,
                                                                               @PathVariable @Positive Long eventId,
                                                                               @RequestBody EventRequestStatusUpdateRequestDto requestDto) {
        return requestService.changeParticipationRequestsStatus(userId, eventId, requestDto);
    }
}