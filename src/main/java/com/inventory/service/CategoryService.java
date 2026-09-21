package com.inventory.service;

import com.inventory.dto.CategoryDto;
import com.inventory.entity.Category;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return convertToDto(category);
    }

    public CategoryDto createCategory(CategoryDto dto) {
        if (categoryRepository.findByName(dto.getName().trim()).isPresent()) {
            throw new DuplicateResourceException("Category already exists with name: " + dto.getName());
        }

        Category category = new Category();
        category.setName(dto.getName().trim());
        category.setDescription(dto.getDescription());
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : true);

        return convertToDto(categoryRepository.save(category));
    }

    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        categoryRepository.findByName(dto.getName().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Category already exists with name: " + dto.getName());
                });

        category.setName(dto.getName().trim());
        category.setDescription(dto.getDescription());
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : category.isStatus());

        return convertToDto(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        categoryRepository.delete(category);
    }

    private CategoryDto convertToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setStatus(category.isStatus());
        return dto;
    }
}
