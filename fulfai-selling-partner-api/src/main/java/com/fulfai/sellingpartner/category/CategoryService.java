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
        // Check if category with same name already exists
        Category existing = categoryRepository.getByName(categoryDTO.getName());
        if (existing != null) {
            throw new BadRequestException("Category with name '" + categoryDTO.getName() + "' already exists");
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
        if (category != null) {
            return categoryMapper.toResponseDTO(category);
        } else {
            throw new NotFoundException("Category not found with name: " + name);
        }
    }

    public List<CategoryResponseDTO> getAllCategories() {
        Log.debugf("Getting all categories");
        List<Category> categories = categoryRepository.getAll();

        return categories.stream()
                .map(categoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public CategoryResponseDTO updateCategory(String name, @Valid CategoryRequestDTO categoryDTO) {
        Category originalCategory = categoryRepository.getByName(name);
        if (originalCategory != null) {
            // If name is being changed, we need to delete old and create new
            if (!name.equals(categoryDTO.getName())) {
                // Check if new name already exists
                Category existingWithNewName = categoryRepository.getByName(categoryDTO.getName());
                if (existingWithNewName != null) {
                    throw new BadRequestException("Category with name '" + categoryDTO.getName() + "' already exists");
                }
                // Delete old category
                categoryRepository.delete(name);
            }

            Category category = categoryMapper.toEntity(categoryDTO);
            category.setCreatedAt(originalCategory.getCreatedAt());
            category.setUpdatedAt(Instant.now());

            categoryRepository.save(category);
            Log.debugf("Updated category: %s", category.getName());

            return categoryMapper.toResponseDTO(category);
        } else {
            throw new NotFoundException("Category not found with name: " + name);
        }
    }

    public void deleteCategory(String name) {
        Category category = categoryRepository.getByName(name);
        if (category != null) {
            categoryRepository.delete(name);
            Log.debugf("Deleted category: %s", name);
        } else {
            throw new NotFoundException("Category not found with name: " + name);
        }
    }
}
