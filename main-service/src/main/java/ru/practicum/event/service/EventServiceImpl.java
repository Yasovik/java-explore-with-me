package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitDto;
import ru.practicum.StatClient;
import ru.practicum.StatDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;

import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.StateAdminAction;
import ru.practicum.event.model.StateUserAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exceptions.*;
import ru.practicum.location.model.Location;
import ru.practicum.location.repository.LocationRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.event.mapper.EventMapper.toEvent;
import static ru.practicum.event.mapper.EventMapper.toEventFullDto;
import static ru.practicum.location.mapper.LocationMapper.toLocation;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final StatClient statClient;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException(newEventDto.getCategory()));

        if (newEventDto.getParticipantLimit() < 0) {
            throw new ValidationRequestException("Лимит участников не может быть отрицательным");
        }

        Event event = toEvent(newEventDto);
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationRequestException("Событие должно начинаться минимум через 2 часа");
        }
        event.setCreatedOn(LocalDateTime.now());
        event.setInitiator(user);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        event.setLocation(locationRepository.save(toLocation(newEventDto.getLocation())));
        event.setViews(0L);

        return toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, int from, int size) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        List<Event> events = eventRepository.findByInitiatorId(userId, PageRequest.of(from / size, size));
        return events.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        return toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequestDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ForbiddenException("Только инициатор может изменить событие");
        }

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ForbiddenException("Изменять можно только события в состоянии PENDING или CANCELED");
        }

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) {
            if (dto.getParticipantLimit() < 0) {
                throw new ValidationRequestException("Лимит участников не может быть отрицательным");
            }
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());

        if (dto.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(dto.getEventDate(), formatter);
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationRequestException("Дата события должна быть минимум через 2 часа");
            }
            event.setEventDate(eventDate);
        }

        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException(dto.getCategory()));
            event.setCategory(category);
        }

        if (dto.getLocation() != null) {
            Location location = event.getLocation();
            location.setLat(dto.getLocation().getLat());
            location.setLon(dto.getLocation().getLon());
            locationRepository.save(location);
        }

        if (dto.getStateAction() == StateUserAction.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        } else if (dto.getStateAction() == StateUserAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }

        return toEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequestDto dto) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        if (dto.getStateAction() != null) {
            if (dto.getStateAction() == StateAdminAction.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ForbiddenException("Можно опубликовать только событие в состоянии PENDING");
                }
                event.setPublishedOn(LocalDateTime.now());
                event.setState(EventState.PUBLISHED);
            } else if (dto.getStateAction() == StateAdminAction.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ForbiddenException("Нельзя отклонить опубликованное событие");
                }
                event.setState(EventState.CANCELED);
            }
        }

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());

        if (dto.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(dto.getEventDate(), formatter);
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationRequestException("Дата события должна быть минимум через 2 часа");
            }
            event.setEventDate(eventDate);
        }

        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException(dto.getCategory()));
            event.setCategory(category);
        }

        if (dto.getLocation() != null) {
            Location location = event.getLocation();
            location.setLat(dto.getLocation().getLat());
            location.setLon(dto.getLocation().getLon());
            locationRepository.save(location);
        }

        return toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states,
                                               List<Long> categories, String rangeStart,
                                               String rangeEnd, int from, int size) {
        LocalDateTime start = parseDateTime(rangeStart);
        LocalDateTime end = parseDateTime(rangeEnd);

        List<String> validStates = states != null ?
                states.stream()
                        .map(String::toUpperCase)
                        .collect(Collectors.toList()) :
                null;

        List<Event> events = eventRepository.findEvents(
                users,
                validStates,
                categories,
                start,
                end,
                PageRequest.of(from / size, size)
        );

        return events.stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (DateTimeParseException e) {
            throw new ValidationRequestException("Неверный формат даты. Ожидается: yyyy-MM-dd HH:mm:ss");
        }
    }

    @Override
    public List<EventShortDto> getPublishedEvents(String text, List<Long> categories, Boolean paid,
                                                  String rangeStart, String rangeEnd, boolean onlyAvailable,
                                                  String sort, int from, int size, HttpServletRequest request) {
        sendHit(request);

        LocalDateTime start = null;
        LocalDateTime end = null;

        try {
            if (rangeStart != null) {
                start = LocalDateTime.parse(rangeStart, formatter);
            }
            if (rangeEnd != null) {
                end = LocalDateTime.parse(rangeEnd, formatter);
            }
        } catch (Exception e) {
            throw new ValidationRequestException("Неверный формат даты. Ожидается: yyyy-MM-dd HH:mm:ss");
        }

        if (start != null && end != null && start.isAfter(end)) {
            throw new ValidationRequestException("Дата начала должна быть раньше даты окончания.");
        }

        List<Event> events = eventRepository.findPublishedEvents(
                text,
                categories,
                paid,
                start != null ? start : LocalDateTime.now(),
                end,
                PageRequest.of(from / size, size)
        );

        if (onlyAvailable) {
            events = events.stream()
                    .filter(event -> EventMapper.countConfirmedRequests(event.getRequests()) < event.getParticipantLimit())
                    .toList();
        }

        List<EventShortDto> dtos = events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        if (sort != null) {
            if (sort.equals("EVENT_DATE")) {
                dtos.sort(Comparator.comparing(EventShortDto::getEventDate));
            } else if (sort.equals("VIEWS")) {
                dtos.sort(Comparator.comparing(EventShortDto::getViews));
            } else {
                throw new ValidationRequestException("Неверный параметр сортировки.");
            }
        }

        return dtos;
    }

    @Override
    public EventFullDto getPublishedEventById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        int previousHits = getHits(request);
        sendHit(request);
        int newHits = getHits(request);

        if (newHits > previousHits) {
            event.setViews(event.getViews() + 1);
            eventRepository.save(event);
        }

        return toEventFullDto(event);
    }

    private void sendHit(HttpServletRequest request) {
        statClient.addHit(HitDto.builder()
                .app("ewm-main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(formatter))
                .build());
    }

    private int getHits(HttpServletRequest request) {
        ResponseEntity<StatDto[]> response = statClient.getStats(
                LocalDateTime.now().minusYears(100).format(formatter),
                LocalDateTime.now().format(formatter),
                new String[]{request.getRequestURI()},
                true
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().length > 0) {
            return Math.toIntExact(response.getBody()[0].getHits());
        }
        return 0;
    }
}