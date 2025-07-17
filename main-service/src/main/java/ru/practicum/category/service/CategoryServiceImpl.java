package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exceptions.CategoryNotFoundException;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.ForbiddenException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.category.mapper.CategoryMapper.toCategory;
import static ru.practicum.category.mapper.CategoryMapper.toCategoryDto;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(int from, int size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size))
                .stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(long catId) {
        return toCategoryDto(categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException(catId)));
    }

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Optional<Category> existingCategory = categoryRepository.findByName(newCategoryDto.getName());
        if (existingCategory.isPresent()) {
            throw new ConflictException("Категория с таким именем уже существует: " + newCategoryDto.getName());
        }

        Category category = toCategory(newCategoryDto);
        return toCategoryDto(categoryRepository.save(category));
    }


    @Override
    @Transactional
    public CategoryDto updateCategory(long catId, NewCategoryDto newCategoryDto) {
        Category existCategory = categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException(catId));
        Category updatedCategory = toCategory(newCategoryDto);
        updatedCategory.setId(existCategory.getId());
        return toCategoryDto(categoryRepository.save(updatedCategory));
    }

    @Override
    @Transactional
    public void deleteCategory(long catId) {
        categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException(catId));
        Event event = eventRepository.findFirstByCategoryId(catId);
        if (event != null) {
            throw new ForbiddenException("Категория не пустая");
        }
        categoryRepository.deleteById(catId);
    }
}