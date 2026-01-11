package com.fulfai.sellingpartner.category;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class CategoryService {

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    CategoryMapper categoryMapper;

    public CategoryResponseDTO createCategory(@Valid CategoryRequestDTO categoryDTO) {

        if (categoryDTO == null) {
            throw new BadRequestException("Category request body is required");
        }

        if (categoryDTO.getName() == null || categoryDTO.getName().isBlank()) {
            throw new BadRequestException("Category name is required");
        }

        Category existing = categoryRepository.getByName(categoryDTO.getName());
        if (existing != null) {
            throw new BadRequestException(
                    "Category with name '" + categoryDTO.getName() + "' already exists"
            );
        }

        Category category = categoryMapper.toEntity(categoryDTO);

        Instant now = Instant.now();
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }
        if (category.getDisplayOrder() == null) {
            category.setDisplayOrder(0);
        }

        categoryRepository.save(category);
        Log.debugf("Created category: %s", category.getName());

        return categoryMapper.toResponseDTO(category);
    }

    public CategoryResponseDTO getCategoryByName(String name) {
        Log.debugf("Getting category by name: %s", name);

        Category category = categoryRepository.getByName(name);
        if (category == null) {
            throw new NotFoundException("Category not found with name: " + name);
        }

        return categoryMapper.toResponseDTO(category);
    }

    public List<CategoryResponseDTO> getAllCategories() {
        Log.debugf("Getting all categories");

        return categoryRepository.getAll()
                .stream()
                .map(categoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public CategoryResponseDTO updateCategory(String name, @Valid CategoryRequestDTO categoryDTO) {

        if (categoryDTO == null) {
            throw new BadRequestException("Category request body is required");
        }

        if (categoryDTO.getName() == null || categoryDTO.getName().isBlank()) {
            throw new BadRequestException("Category name is required");
        }

        Category originalCategory = categoryRepository.getByName(name);
        if (originalCategory == null) {
            throw new NotFoundException("Category not found with name: " + name);
        }

        if (!name.equals(categoryDTO.getName())) {
            Category existingWithNewName =
                    categoryRepository.getByName(categoryDTO.getName());

            if (existingWithNewName != null) {
                throw new BadRequestException(
                        "Category with name '" + categoryDTO.getName() + "' already exists"
                );
            }

            categoryRepository.delete(name);
        }

        Category category = categoryMapper.toEntity(categoryDTO);
        category.setCreatedAt(originalCategory.getCreatedAt());
        category.setUpdatedAt(Instant.now());

        categoryRepository.save(category);
        Log.debugf("Updated category: %s", category.getName());

        return categoryMapper.toResponseDTO(category);
    }

    public void deleteCategory(String name) {
        Category category = categoryRepository.getByName(name);
        if (category == null) {
            throw new NotFoundException("Category not found with name: " + name);
        }

        categoryRepository.delete(name);
        Log.debugf("Deleted category: %s", name);
    }
}
