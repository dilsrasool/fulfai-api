package com.fulfai.sellingpartner.branchreview;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "cdi")
public interface BranchReviewMapper {

    BranchReviewMapper INSTANCE = Mappers.getMapper(BranchReviewMapper.class);

    @Mapping(target = "branchKey", ignore = true)
    @Mapping(target = "reviewId", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BranchReview toEntity(BranchReviewRequestDTO dto);

    BranchReviewResponseDTO toResponseDTO(BranchReview entity);
}
