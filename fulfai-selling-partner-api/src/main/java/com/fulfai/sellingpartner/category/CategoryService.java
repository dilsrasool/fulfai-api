package com.fulfai.sellingpartner.category;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import com.fulfai.sellingpartner.publicapi.dto.PublicCategoryDTO;


@ApplicationScoped
public class CategoryService {

    private static final String ROOT = "ROOT";

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    CategoryMapper categoryMapper;

    // --------------------------------------------------
    // Create
    // --------------------------------------------------
    public CategoryResponseDTO createCategory(
            String companyId,
            CategoryRequestDTO dto
    ) {
        if (companyId == null || companyId.isBlank()) {
            throw new BadRequestException("companyId is required");
        }

        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Category name is required");
        }

        // ---------- Unique name per company ----------
        Category existing =
                categoryRepository.getByCompanyAndName(companyId, dto.getName());

        if (existing != null) {
            throw new BadRequestException(
                    "Category with name '" + dto.getName() + "' already exists"
            );
        }

        Category category = categoryMapper.toEntity(dto);

        // ---------- Keys ----------
        category.setCompanyId(companyId);
        category.setCategoryId(UUID.randomUUID().toString());

        // ---------- Hierarchy ----------
        if (dto.getParentCategoryId() == null || dto.getParentCategoryId().isBlank()) {
            category.setParentCategoryId(ROOT);
            category.setParentCategories(List.of());
        } else {
            Category parent =
                    categoryRepository.getByCompanyAndId(
                            companyId,
                            dto.getParentCategoryId()
                    );

            if (parent == null) {
                throw new BadRequestException("Parent category not found");
            }

            List<String> ancestry = new ArrayList<>(parent.getParentCategories());
            ancestry.add(parent.getCategoryId());

            category.setParentCategoryId(parent.getCategoryId());
            category.setParentCategories(ancestry);
        }

        // ---------- Timestamps ----------
        Instant now = Instant.now();
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        // ---------- Defaults ----------
        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }
        if (category.getDisplayOrder() == null) {
            category.setDisplayOrder(0);
        }

        categoryRepository.save(category);

        return categoryMapper.toResponseDTO(category);
    }

    // --------------------------------------------------
    // Get all (company)
    // --------------------------------------------------
    public List<CategoryResponseDTO> getAllCategories(String companyId) {
        if (companyId == null || companyId.isBlank()) {
            throw new BadRequestException("companyId is required");
        }

        return categoryRepository
                .getAllByCompany(companyId)
                .stream()
                .map(categoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // --------------------------------------------------
    // Get by ID
    // --------------------------------------------------
    public CategoryResponseDTO getCategoryById(
            String companyId,
            String categoryId
    ) {
        Category category =
                categoryRepository.getByCompanyAndId(companyId, categoryId);

        if (category == null) {
            throw new NotFoundException("Category not found");
        }

        return categoryMapper.toResponseDTO(category);
    }

    // --------------------------------------------------
    // Update
    // --------------------------------------------------
    public CategoryResponseDTO updateCategory(
            String companyId,
            String categoryId,
            CategoryRequestDTO dto
    ) {
        if (dto == null) {
            throw new BadRequestException("Request body is required");
        }

        Category existing =
                categoryRepository.getByCompanyAndId(companyId, categoryId);

        if (existing == null) {
            throw new NotFoundException("Category not found");
        }

        Category updated = categoryMapper.toEntity(dto);

        // ---------- Preserve keys ----------
        updated.setCompanyId(companyId);
        updated.setCategoryId(categoryId);

        // ---------- Preserve hierarchy ----------
        updated.setParentCategoryId(existing.getParentCategoryId());
        updated.setParentCategories(existing.getParentCategories());

        // ---------- Preserve timestamps ----------
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(Instant.now());

        // ---------- Preserve defaults ----------
        if (updated.getIsActive() == null) {
            updated.setIsActive(existing.getIsActive());
        }
        if (updated.getDisplayOrder() == null) {
            updated.setDisplayOrder(existing.getDisplayOrder());
        }

        categoryRepository.save(updated);

        return categoryMapper.toResponseDTO(updated);
    }

    // --------------------------------------------------
    // Delete
    // --------------------------------------------------
    public void deleteCategory(String companyId, String categoryId) {
        Category existing =
                categoryRepository.getByCompanyAndId(companyId, categoryId);

        if (existing == null) {
            throw new NotFoundException("Category not found");
        }

        categoryRepository.delete(companyId, categoryId);
    }

    /* ============================
   PUBLIC BROWSING (NO AUTH)
============================ */

public List<PublicCategoryDTO> getPublicCategories(String companyId) {

    if (companyId == null || companyId.isBlank()) {
        throw new BadRequestException("companyId is required");
    }

    return categoryRepository
            .getAllByCompany(companyId)
            .stream()
            .filter(c -> c.getIsActive() == null || Boolean.TRUE.equals(c.getIsActive()))
            .sorted((a, b) -> {
                Integer ao = a.getDisplayOrder() == null ? 0 : a.getDisplayOrder();
                Integer bo = b.getDisplayOrder() == null ? 0 : b.getDisplayOrder();
                return ao.compareTo(bo);
            })
            .map(c -> {
                PublicCategoryDTO dto = new PublicCategoryDTO();
                dto.id = c.getCategoryId();
                dto.name = c.getName();
                dto.image = c.getImageUrl(); // if your entity has image field
                return dto;
            })
            .collect(Collectors.toList());
}

}
