package ru.practicum.request.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exceptions.EventNotFoundException;
import ru.practicum.exceptions.ForbiddenException;
import ru.practicum.exceptions.UserNotFoundException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.request.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.request.mapper.RequestMapper.toRequestDto;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public RequestDto createParticipationRequest(Long userId, Long eventId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (requestRepository.findByRequesterIdAndEventId(userId, eventId) != null) {
            throw new ForbiddenException("Пользователь уже подал заявку на это событие.");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Инициатор не может подавать заявку на своё событие.");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ForbiddenException("Нельзя участвовать в неопубликованном событии.");
        }
        if (event.getParticipantLimit() != 0 &&
                requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED) >= event.getParticipantLimit()) {
            throw new ForbiddenException("Достигнут лимит участников.");
        }

        RequestStatus status = (!event.isRequestModeration() || event.getParticipantLimit() == 0)
                ? RequestStatus.CONFIRMED : RequestStatus.PENDING;

        Request request = Request.builder()
                .event(event)
                .requester(user)
                .created(LocalDateTime.now())
                .status(status)
                .build();

        return toRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public RequestDto cancelParticipationRequest(Long userId, Long requestId) {
        if (userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ForbiddenException("Запрос не найден."));

        if (!request.getRequester().getId().equals(userId)) {
            throw new ForbiddenException("Можно отменить только собственный запрос.");
        }

        request.setStatus(RequestStatus.CANCELED);
        return toRequestDto(requestRepository.save(request));
    }

    @Override
    public List<RequestDto> getParticipationRequests(Long userId) {
        if (userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestDto> getParticipationRequestsForUserEvent(Long userId, Long eventId) {
        if (userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        List<Event> events = eventRepository.findByIdAndInitiatorId(eventId, userId);
        if (events.isEmpty()) {
            throw new ForbiddenException("Пользователь не инициатор события.");
        }

        return requestRepository.findByEventIn(events).stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResultDto changeParticipationRequestsStatus(Long userId, Long eventId,
                                                                               EventRequestStatusUpdateRequestDto dto) {
        if (userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        List<Request> requests = requestRepository.findAllById(dto.getRequestIds());
        EventRequestStatusUpdateResultDto result = EventRequestStatusUpdateResultDto.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(new ArrayList<>())
                .build();

        if (requests.isEmpty()) {
            return result;
        }

        RequestStatus targetStatus = RequestStatus.valueOf(dto.getStatus());

        if (targetStatus == RequestStatus.CONFIRMED) {
            int limit = event.getParticipantLimit();
            Integer confirmed = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);

            if (limit == 0 || !event.isRequestModeration()) {
                throw new ForbiddenException("Подтверждение не требуется.");
            }
            if (confirmed >= limit) {
                throw new ForbiddenException("Достигнут лимит участников.");
            }

            for (Request request : requests) {
                if (request.getStatus() != RequestStatus.PENDING) {
                    throw new ForbiddenException("Изменять можно только заявки в статусе PENDING.");
                }
                if (confirmed < limit) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    result.getConfirmedRequests().add(toRequestDto(request));
                    confirmed++;
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    result.getRejectedRequests().add(toRequestDto(request));
                }
            }

            requestRepository.saveAll(requests);

            if (confirmed == limit) {
                requestRepository.updateRequestStatusByEventIdAndStatus(event, RequestStatus.PENDING, RequestStatus.REJECTED);
            }

        } else if (targetStatus == RequestStatus.REJECTED) {
            for (Request request : requests) {
                if (request.getStatus() != RequestStatus.PENDING) {
                    throw new ForbiddenException("Изменять можно только заявки в статусе PENDING.");
                }
                request.setStatus(RequestStatus.REJECTED);
                result.getRejectedRequests().add(toRequestDto(request));
            }
            requestRepository.saveAll(requests);
        }

        return result;
    }
}