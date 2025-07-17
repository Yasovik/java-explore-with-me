package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Hit;
import ru.practicum.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepository extends JpaRepository<Hit, Long> {

    @Query("SELECT new ru.practicum.model.Stat(h.app, h.uri, COUNT(h.uri)) " +
            "FROM Hit h " +
            "WHERE h.uri IN :uris AND h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.uri) DESC")
    List<Stat> findStatsByUris(List<String> uris, LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.model.Stat(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hit h " +
            "WHERE h.uri IN :uris AND h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<Stat> findStatsByUrisUnique(List<String> uris, LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.model.Stat(h.app, h.uri, COUNT(h.uri)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.uri) DESC")
    List<Stat> findAllStats(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.model.Stat(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<Stat> findAllStatsUnique(LocalDateTime start, LocalDateTime end);
}