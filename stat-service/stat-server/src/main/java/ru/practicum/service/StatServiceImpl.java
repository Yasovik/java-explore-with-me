package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.HitDto;
import ru.practicum.StatDto;
import ru.practicum.exception.ValidationRequestException;
import ru.practicum.mapper.StatMapper;
import ru.practicum.model.Stat;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.mapper.HitMapper.toHit;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatServiceImpl implements StatService {
    private final StatRepository statRepository;

    @Override
    public void addHit(HitDto hitDto) {
        log.debug("Сохраняем hit: {}", hitDto);
        statRepository.save(toHit(hitDto));
    }

    @Override
    public List<StatDto> getStats(LocalDateTime start, LocalDateTime end, String[] uris, boolean unique) {
        List<Stat> stats;

        if (start.isAfter(end)) {
            throw new ValidationRequestException("Параметр 'start' не может быть позже параметра 'end'.");
        }

        if (uris == null || uris.length == 0) {
            stats = unique
                    ? statRepository.findAllStatsUnique(start, end)
                    : statRepository.findAllStats(start, end);
        } else {
            List<String> uriList = List.of(uris);
            stats = unique
                    ? statRepository.findStatsByUrisUnique(uriList, start, end)
                    : statRepository.findStatsByUris(uriList, start, end);
        }

        return stats.isEmpty()
                ? Collections.emptyList()
                : stats.stream().map(StatMapper::toStatDto).collect(Collectors.toList());
    }
}