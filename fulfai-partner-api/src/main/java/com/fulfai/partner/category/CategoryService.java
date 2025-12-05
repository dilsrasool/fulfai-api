package com.fulfai.partner.category;

import java.time.Instant;
import java.util.stream.Collectors;

import com.fulfai.common.dto.PaginatedResponse;

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

    public CategoryResponseDTO createCategory(String companyId, @Valid CategoryRequestDTO categoryDTO) {
        // Check if category with same name already exists
        Category existing = categoryRepository.getById(companyId, categoryDTO.getName());
        if (existing != null) {
            throw new BadRequestException("Category with name '" + categoryDTO.getName() + "' already exists");
        }

        Category category = categoryMapper.toEntity(categoryDTO);

        Instant now = Instant.now();
        category.setCompanyId(companyId);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }
        if (category.getDisplayOrder() == null) {
            category.setDisplayOrder(0);
        }

        categoryRepository.save(category);
        Log.debugf("Created category: %s for company: %s", category.getName(), companyId);

        return categoryMapper.toResponseDTO(category);
    }

    public CategoryResponseDTO getCategoryByName(String companyId, String name) {
        Log.debugf("Getting category by companyId: %s, name: %s", companyId, name);
        Category category = categoryRepository.getById(companyId, name);
        if (category != null) {
            return categoryMapper.toResponseDTO(category);
        } else {
            throw new NotFoundException("Category not found with name: " + name);
        }
    }

    public PaginatedResponse<CategoryResponseDTO> getCategoriesByCompanyId(String companyId, String nextToken, Integer limit) {
        Log.debugf("Getting categories for company: %s", companyId);
        PaginatedResponse<Category> response = categoryRepository.getByCompanyId(companyId, nextToken, limit);

        return PaginatedResponse.<CategoryResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(categoryMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    public CategoryResponseDTO updateCategory(String companyId, String name, @Valid CategoryRequestDTO categoryDTO) {
        Category originalCategory = categoryRepository.getById(companyId, name);
        if (originalCategory != null) {
            // If name is being changed, we need to delete old and create new
            if (!name.equals(categoryDTO.getName())) {
                // Check if new name already exists
                Category existingWithNewName = categoryRepository.getById(companyId, categoryDTO.getName());
                if (existingWithNewName != null) {
                    throw new BadRequestException("Category with name '" + categoryDTO.getName() + "' already exists");
                }
                // Delete old category
                categoryRepository.delete(companyId, name);
            }

            Category category = categoryMapper.toEntity(categoryDTO);
            category.setCompanyId(companyId);
            category.setCreatedAt(originalCategory.getCreatedAt());
            category.setUpdatedAt(Instant.now());

            categoryRepository.save(category);
            Log.debugf("Updated category: %s", category.getName());

            return categoryMapper.toResponseDTO(category);
        } else {
            throw new NotFoundException("Category not found with name: " + name);
        }
    }

    public void deleteCategory(String companyId, String name) {
        Category category = categoryRepository.getById(companyId, name);
        if (category != null) {
            categoryRepository.delete(companyId, name);
            Log.debugf("Deleted category: %s", name);
        } else {
            throw new NotFoundException("Category not found with name: " + name);
        }
    }
}
