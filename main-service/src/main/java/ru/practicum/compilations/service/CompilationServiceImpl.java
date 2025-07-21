package ru.practicum.compilations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.mapper.CompilationMapper;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequestDto;
import ru.practicum.compilations.model.Compilation;
import ru.practicum.compilations.repository.CompilationRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exceptions.CompilationNotFoundException;
import ru.practicum.exceptions.ValidationRequestException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.compilations.mapper.CompilationMapper.toCompilation;
import static ru.practicum.compilations.mapper.CompilationMapper.toCompilationDto;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findByPinned(pinned, PageRequest.of(from / size, size));
        } else {
            compilations = compilationRepository.findAll(PageRequest.of(from / size, size)).getContent();
        }
        return !compilations.isEmpty() ? compilations.stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList()) : Collections.emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        return toCompilationDto(compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException(compId)));
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = toCompilation(newCompilationDto);
        if (newCompilationDto.getEvents() != null) {
            compilation.setEvents(eventRepository.findByIdIn(newCompilationDto.getEvents()));
        }
        return toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequestDto updateCompilationRequestDto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException(compId));
        if (updateCompilationRequestDto.getTitle() != null) {
            String title = updateCompilationRequestDto.getTitle();
            if (title.isEmpty() || title.length() > 50) {
                throw new ValidationRequestException("Заголовок подборки должен содержать от 1 до 50 символов");
            }
            compilation.setTitle(updateCompilationRequestDto.getTitle());
        }
        if (updateCompilationRequestDto.getPinned() != null) {
            compilation.setPinned(updateCompilationRequestDto.getPinned());
        }
        if (updateCompilationRequestDto.getEvents() != null) {
            compilation.setEvents(eventRepository.findByIdIn(updateCompilationRequestDto.getEvents()));
        }
        return toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        compilationRepository.findById(compId).orElseThrow(() -> new CompilationNotFoundException(compId));
        compilationRepository.deleteById(compId);
    }
}