package com.fulfai.sellingpartner.branchreview;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.branch.Branch;
import com.fulfai.sellingpartner.branch.BranchRepository;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class BranchReviewService {

    private static final int DEFAULT_LIMIT = 20;

    @Inject
    BranchReviewRepository reviewRepository;

    @Inject
    BranchReviewMapper reviewMapper;

    @Inject
    BranchRepository branchRepository;

    public BranchReviewResponseDTO createReview(
            String companyId,
            String branchId,
            String userId,
            @Valid BranchReviewRequestDTO request
    ) {
        return upsertReview(companyId, branchId, userId, request);
    }

    public BranchReviewResponseDTO upsertReview(
            String companyId,
            String branchId,
            String userId,
            @Valid BranchReviewRequestDTO request
    ) {
        Branch branch = requireBranch(companyId, branchId);

        String branchKey = branchKey(companyId, branchId);
        Instant now = Instant.now();

        BranchReview review = reviewRepository.findByUserAndBranch(userId, branchKey)
                .orElseGet(BranchReview::new);

        Integer oldRating = review.getRating();

        if (review.getReviewId() == null) {
            review.setBranchKey(branchKey);
            review.setReviewId("rev-" + UUID.randomUUID());
            review.setBranchId(branchId);
            review.setUserId(userId);
            review.setCreatedAt(now);
            review.setIsDeleted(false);
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setUserName(
                request.getUserName() == null || request.getUserName().isBlank()
                        ? userId
                        : request.getUserName().trim()
        );
        review.setUpdatedAt(now);

        reviewRepository.save(review);

        applyAggregateOnUpsert(branch, oldRating, request.getRating(), now);
        branchRepository.save(branch);

        Log.debugf("BRANCH_REVIEW upserted reviewId=%s branch=%s by user=%s",
                review.getReviewId(), branchId, userId);

        return reviewMapper.toResponseDTO(review);
    }

    public void deleteOwnReview(String companyId, String branchId, String userId) {
        Branch branch = requireBranch(companyId, branchId);
        String branchKey = branchKey(companyId, branchId);

        BranchReview review = reviewRepository.findByUserAndBranch(userId, branchKey)
                .orElseThrow(() -> new NotFoundException("Review not found for this user and branch"));

        if (Boolean.TRUE.equals(review.getIsDeleted())) {
            return;
        }

        review.setIsDeleted(true);
        review.setUpdatedAt(Instant.now());
        reviewRepository.save(review);

        applyAggregateOnDelete(branch, review.getRating(), Instant.now());
        branchRepository.save(branch);
    }

    public void deleteByReviewId(String companyId, String branchId, String reviewId) {
        Branch branch = requireBranch(companyId, branchId);
        String branchKey = branchKey(companyId, branchId);

        BranchReview review = reviewRepository.getById(branchKey, reviewId);
        if (review == null) {
            throw new NotFoundException("Review not found with id: " + reviewId);
        }

        if (!Boolean.TRUE.equals(review.getIsDeleted())) {
            review.setIsDeleted(true);
            review.setUpdatedAt(Instant.now());
            reviewRepository.save(review);

            applyAggregateOnDelete(branch, review.getRating(), Instant.now());
            branchRepository.save(branch);
        }
    }

    public BranchReviewResponseDTO getReviewById(String companyId, String branchId, String reviewId) {
        requireBranch(companyId, branchId);
        String branchKey = branchKey(companyId, branchId);

        BranchReview review = reviewRepository.getById(branchKey, reviewId);
        if (review == null || Boolean.TRUE.equals(review.getIsDeleted())) {
            throw new NotFoundException("Review not found with id: " + reviewId);
        }

        return reviewMapper.toResponseDTO(review);
    }

    public BranchReviewResponseDTO updateReviewById(
            String companyId,
            String branchId,
            String reviewId,
            @Valid BranchReviewRequestDTO request
    ) {
        Branch branch = requireBranch(companyId, branchId);
        String branchKey = branchKey(companyId, branchId);

        BranchReview review = reviewRepository.getById(branchKey, reviewId);
        if (review == null || Boolean.TRUE.equals(review.getIsDeleted())) {
            throw new NotFoundException("Review not found with id: " + reviewId);
        }

        Integer oldRating = review.getRating();
        Instant now = Instant.now();

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        if (request.getUserName() != null && !request.getUserName().isBlank()) {
            review.setUserName(request.getUserName().trim());
        }
        review.setUpdatedAt(now);

        reviewRepository.save(review);

        applyAggregateOnUpsert(branch, oldRating, request.getRating(), now);
        branchRepository.save(branch);

        return reviewMapper.toResponseDTO(review);
    }

    public PaginatedResponse<BranchReviewResponseDTO> getReviews(
            String companyId,
            String branchId,
            String nextToken,
            Integer limit
    ) {
        requireBranch(companyId, branchId);

        PaginatedResponse<BranchReview> page = reviewRepository.listByBranch(
                branchId,
                nextToken,
                limit == null ? DEFAULT_LIMIT : limit
        );

        List<BranchReviewResponseDTO> mapped = page.getItems().stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                .map(reviewMapper::toResponseDTO)
                .toList();

        return PaginatedResponse.<BranchReviewResponseDTO>builder()
                .items(mapped)
                .nextToken(page.getNextToken())
                .hasMore(page.isHasMore())
                .build();
    }

    private Branch requireBranch(String companyId, String branchId) {
        Branch branch = branchRepository.getById(companyId, branchId);
        if (branch == null) {
            throw new NotFoundException("Branch not found with id: " + branchId);
        }
        return branch;
    }

    private void applyAggregateOnUpsert(Branch branch, Integer oldRating, Integer newRating, Instant now) {
        long ratingSum = branch.getRatingSum() == null ? 0L : branch.getRatingSum();
        int ratingCount = branch.getRatingCount() == null ? 0 : branch.getRatingCount();

        if (oldRating == null) {
            ratingSum += newRating;
            ratingCount += 1;
        } else {
            ratingSum = ratingSum - oldRating + newRating;
        }

        branch.setRatingSum(ratingSum);
        branch.setRatingCount(ratingCount);
        branch.setRatingAverage(computeAverage(ratingSum, ratingCount));
        branch.setUpdatedAt(now);
    }

    private void applyAggregateOnDelete(Branch branch, Integer oldRating, Instant now) {
        if (oldRating == null) {
            return;
        }

        long ratingSum = branch.getRatingSum() == null ? 0L : branch.getRatingSum();
        int ratingCount = branch.getRatingCount() == null ? 0 : branch.getRatingCount();

        ratingSum = Math.max(0L, ratingSum - oldRating);
        ratingCount = Math.max(0, ratingCount - 1);

        branch.setRatingSum(ratingSum);
        branch.setRatingCount(ratingCount);
        branch.setRatingAverage(computeAverage(ratingSum, ratingCount));
        branch.setUpdatedAt(now);
    }

    private Double computeAverage(long ratingSum, int ratingCount) {
        if (ratingCount <= 0) {
            return 0.0;
        }

        return BigDecimal.valueOf(ratingSum)
                .divide(BigDecimal.valueOf(ratingCount), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static String branchKey(String companyId, String branchId) {
        return companyId + "#" + branchId;
    }
}
