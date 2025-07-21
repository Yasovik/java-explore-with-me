package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.Event;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Request findByRequesterIdAndEventId(Long userId, Long eventId);

    List<Request> findByRequesterId(Long userId);

    Integer countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findByEventIn(List<Event> events);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Request r SET r.status = :newStatus WHERE r.event = :event AND r.status = :searchStatus")
    void updateRequestStatusByEventIdAndStatus(@Param("event") Event event,
                                               @Param("searchStatus") RequestStatus searchStatus,
                                               @Param("newStatus") RequestStatus newStatus);
}