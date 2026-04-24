package com.fulfai.sellingpartner;

import java.math.BigDecimal;
import java.time.Instant;

import com.fulfai.sellingpartner.account.Account;
import com.fulfai.sellingpartner.branch.Branch;
import com.fulfai.sellingpartner.branchreview.BranchReview;
import com.fulfai.sellingpartner.category.Category;
import com.fulfai.sellingpartner.company.Company;
import com.fulfai.sellingpartner.companyJoinRequest.CompanyJoinRequest;
import com.fulfai.sellingpartner.order.Order;
import com.fulfai.sellingpartner.order.OrderItem;
import com.fulfai.sellingpartner.order.OrderTimelineEvent;
import com.fulfai.sellingpartner.product.Product;
import com.fulfai.sellingpartner.UserCompanyRole.UserCompanyRole;

import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;

public class Schemas {

        public static final TableSchema<OrderTimelineEvent> ORDER_TIMELINE_EVENT_SCHEMA =
                TableSchema.builder(OrderTimelineEvent.class)
                        .newItemSupplier(OrderTimelineEvent::new)
                        .addAttribute(String.class, a -> a.name("eventId")
                                        .getter(OrderTimelineEvent::getEventId)
                                        .setter(OrderTimelineEvent::setEventId))
                        .addAttribute(String.class, a -> a.name("action")
                                        .getter(OrderTimelineEvent::getAction)
                                        .setter(OrderTimelineEvent::setAction))
                        .addAttribute(String.class, a -> a.name("actorId")
                                        .getter(OrderTimelineEvent::getActorId)
                                        .setter(OrderTimelineEvent::setActorId))
                        .addAttribute(String.class, a -> a.name("actorRole")
                                        .getter(OrderTimelineEvent::getActorRole)
                                        .setter(OrderTimelineEvent::setActorRole))
                        .addAttribute(String.class, a -> a.name("fromStatus")
                                        .getter(OrderTimelineEvent::getFromStatus)
                                        .setter(OrderTimelineEvent::setFromStatus))
                        .addAttribute(String.class, a -> a.name("toStatus")
                                        .getter(OrderTimelineEvent::getToStatus)
                                        .setter(OrderTimelineEvent::setToStatus))
                        .addAttribute(String.class, a -> a.name("reasonCode")
                                        .getter(OrderTimelineEvent::getReasonCode)
                                        .setter(OrderTimelineEvent::setReasonCode))
                        .addAttribute(String.class, a -> a.name("note")
                                        .getter(OrderTimelineEvent::getNote)
                                        .setter(OrderTimelineEvent::setNote))
                        .addAttribute(String.class, a -> a.name("idempotencyKey")
                                        .getter(OrderTimelineEvent::getIdempotencyKey)
                                        .setter(OrderTimelineEvent::setIdempotencyKey))
                        .addAttribute(Instant.class, a -> a.name("timestamp")
                                        .getter(OrderTimelineEvent::getTimestamp)
                                        .setter(OrderTimelineEvent::setTimestamp))
                        .addAttribute(
                                        EnhancedType.mapOf(String.class, String.class),
                                        a -> a.name("metadata")
                                                        .getter(OrderTimelineEvent::getMetadata)
                                                        .setter(OrderTimelineEvent::setMetadata))
                        .build();

       public static final TableSchema<Company> COMPANY_SCHEMA =
        TableSchema.builder(Company.class)
            .newItemSupplier(Company::new)

            /* =====================
               PRIMARY KEY
            ===================== */
            .addAttribute(String.class, a -> a.name("id")
                .getter(Company::getId)
                .setter(Company::setId)
                .tags(StaticAttributeTags.primaryPartitionKey())
            )

            /* =====================
               GSIs
            ===================== */

            // 🔹 Owner lookup
            .addAttribute(String.class, a -> a.name("ownerSub")
                .getter(Company::getOwnerSub)
                .setter(Company::setOwnerSub)
                .tags(StaticAttributeTags.secondaryPartitionKey("ownerSub-index"))
            )

            // 🔹 Join Code (GUID) lookup
            .addAttribute(String.class, a -> a.name("joinCode")
                .getter(Company::getJoinCode)
                .setter(Company::setJoinCode)
                .tags(StaticAttributeTags.secondaryPartitionKey("joinCode-index"))
            )

            /* =====================
               COMPANY DETAILS
            ===================== */
            .addAttribute(String.class, a -> a.name("name")
                .getter(Company::getName)
                .setter(Company::setName)
            )
            .addAttribute(String.class, a -> a.name("address")
                .getter(Company::getAddress)
                .setter(Company::setAddress)
            )
            .addAttribute(String.class, a -> a.name("city")
                .getter(Company::getCity)
                .setter(Company::setCity)
            )
            .addAttribute(String.class, a -> a.name("country")
                .getter(Company::getCountry)
                .setter(Company::setCountry)
            )
            .addAttribute(String.class, a -> a.name("email")
                .getter(Company::getEmail)
                .setter(Company::setEmail)
            )
            .addAttribute(String.class, a -> a.name("licenseNo")
                .getter(Company::getLicenseNo)
                .setter(Company::setLicenseNo)
            )
            .addAttribute(String.class, a -> a.name("logo")
                .getter(Company::getLogo)
                .setter(Company::setLogo)
            )
            .addAttribute(String.class, a -> a.name("phoneNumber")
                .getter(Company::getPhoneNumber)
                .setter(Company::setPhoneNumber)
            )
            .addAttribute(String.class, a -> a.name("trn")
                .getter(Company::getTrn)
                .setter(Company::setTrn)
            )
            .addAttribute(String.class, a -> a.name("website")
                .getter(Company::getWebsite)
                .setter(Company::setWebsite)
            )
            .addAttribute(String.class, a -> a.name("state")
                .getter(Company::getState)
                .setter(Company::setState)
            )
              .addAttribute(String.class, a -> a.name("description")
                .getter(Company::getDescription)
                .setter(Company::setDescription)
            )
            .addAttribute(
                EnhancedType.listOf(String.class),
                a -> a.name("operatingCountries")
                    .getter(Company::getOperatingCountries)
                    .setter(Company::setOperatingCountries)
            )

            /* =====================
               AUDIT FIELDS
            ===================== */
            .addAttribute(Instant.class, a -> a.name("createdAt")
                .getter(Company::getCreatedAt)
                .setter(Company::setCreatedAt)
            )
            .addAttribute(Instant.class, a -> a.name("updatedAt")
                .getter(Company::getUpdatedAt)
                .setter(Company::setUpdatedAt)
            )

            .build();



        public static final TableSchema<Branch> BRANCH_SCHEMA = TableSchema.builder(Branch.class)
                        .newItemSupplier(Branch::new)
                        .addAttribute(String.class, a -> a.name("companyId")
                                        .getter(Branch::getCompanyId)
                                        .setter(Branch::setCompanyId)
                                        .tags(StaticAttributeTags.primaryPartitionKey()))
                        .addAttribute(String.class, a -> a.name("branchId")
                                        .getter(Branch::getBranchId)
                                        .setter(Branch::setBranchId)
                                        .tags(StaticAttributeTags.primarySortKey()))
                        .addAttribute(String.class, a -> a.name("name")
                                        .getter(Branch::getName)
                                        .setter(Branch::setName))
                        .addAttribute(String.class, a -> a.name("address")
                                        .getter(Branch::getAddress)
                                        .setter(Branch::setAddress))
                        .addAttribute(String.class, a -> a.name("city")
                                        .getter(Branch::getCity)
                                        .setter(Branch::setCity))
                        .addAttribute(String.class, a -> a.name("country")
                                        .getter(Branch::getCountry)
                                        .setter(Branch::setCountry))
                        .addAttribute(String.class, a -> a.name("phoneNumber")
                                        .getter(Branch::getPhoneNumber)
                                        .setter(Branch::setPhoneNumber))
                        .addAttribute(String.class, a -> a.name("email")
                                        .getter(Branch::getEmail)
                                        .setter(Branch::setEmail))
                        .addAttribute(String.class, a -> a.name("managerName")
                                        .getter(Branch::getManagerName)
                                        .setter(Branch::setManagerName))
                        .addAttribute(Double.class, a -> a.name("latitude")
                                        .getter(Branch::getLatitude)
                                        .setter(Branch::setLatitude))
                        .addAttribute(Double.class, a -> a.name("longitude")
                                        .getter(Branch::getLongitude)
                                        .setter(Branch::setLongitude))
                        .addAttribute(String.class, a -> a.name("geoHash5")
                                        .getter(Branch::getGeoHash5)
                                        .setter(Branch::setGeoHash5)
                                        .tags(StaticAttributeTags.secondaryPartitionKey(Branch.GEOHASH5_GSI)))
                        .addAttribute(String.class, a -> a.name("geoHash6")
                                        .getter(Branch::getGeoHash6)
                                        .setter(Branch::setGeoHash6))
                        .addAttribute(String.class, a -> a.name("timezone")
                                        .getter(Branch::getTimezone)
                                        .setter(Branch::setTimezone))
                        .addAttribute(EnhancedType.mapOf(EnhancedType.of(String.class), EnhancedType.mapOf(String.class, String.class)), a -> a.name("weeklySchedule")
                                        .getter(Branch::getWeeklySchedule)
                                        .setter(Branch::setWeeklySchedule))
                        .addAttribute(EnhancedType.listOf(EnhancedType.mapOf(String.class, String.class)), a -> a.name("closures")
                                        .getter(Branch::getClosures)
                                        .setter(Branch::setClosures))
                        .addAttribute(String.class, a -> a.name("regularOpeningTime")
                                        .getter(Branch::getRegularOpeningTime)
                                        .setter(Branch::setRegularOpeningTime))
                        .addAttribute(String.class, a -> a.name("regularClosingTime")
                                        .getter(Branch::getRegularClosingTime)
                                        .setter(Branch::setRegularClosingTime))
                        .addAttribute(String.class, a -> a.name("dayOpeningTime")
                                        .getter(Branch::getDayOpeningTime)
                                        .setter(Branch::setDayOpeningTime))
                        .addAttribute(String.class, a -> a.name("dayClosingTime")
                                        .getter(Branch::getDayClosingTime)
                                        .setter(Branch::setDayClosingTime))
                        .addAttribute(String.class, a -> a.name("dayScheduleDate")
                                        .getter(Branch::getDayScheduleDate)
                                        .setter(Branch::setDayScheduleDate))
                        .addAttribute(Double.class, a -> a.name("ratingAverage")
                                        .getter(Branch::getRatingAverage)
                                        .setter(Branch::setRatingAverage))
                        .addAttribute(Integer.class, a -> a.name("ratingCount")
                                        .getter(Branch::getRatingCount)
                                        .setter(Branch::setRatingCount))
                        .addAttribute(Long.class, a -> a.name("ratingSum")
                                        .getter(Branch::getRatingSum)
                                        .setter(Branch::setRatingSum))
                        .addAttribute(Boolean.class, a -> a.name("isActive")
                                        .getter(Branch::getIsActive)
                                        .setter(Branch::setIsActive))
                        .addAttribute(Instant.class, a -> a.name("createdAt")
                                        .getter(Branch::getCreatedAt)
                                        .setter(Branch::setCreatedAt))
                        .addAttribute(Instant.class, a -> a.name("locationUpdatedAt")
                                        .getter(Branch::getLocationUpdatedAt)
                                        .setter(Branch::setLocationUpdatedAt))
                        .addAttribute(Instant.class, a -> a.name("updatedAt")
                                        .getter(Branch::getUpdatedAt)
                                        .setter(Branch::setUpdatedAt))
                        .build();

public static final TableSchema<Category> CATEGORY_SCHEMA =
        TableSchema.builder(Category.class)
            .newItemSupplier(Category::new)

            // ---------- Primary Key ----------
            .addAttribute(String.class, a -> a.name("companyId")
                .getter(Category::getCompanyId)
                .setter(Category::setCompanyId)
                .tags(StaticAttributeTags.primaryPartitionKey()))

            .addAttribute(String.class, a -> a.name("categoryId")
                .getter(Category::getCategoryId)
                .setter(Category::setCategoryId)
                .tags(StaticAttributeTags.primarySortKey()))

            // ---------- GSI: Parent Category ----------
            .addAttribute(String.class, a -> a.name("parentCategoryId")
                .getter(Category::getParentCategoryId)
                .setter(Category::setParentCategoryId)
                .tags(StaticAttributeTags.secondaryPartitionKey(Category.PARENT_GSI)))

            // ---------- Business Fields ----------
            .addAttribute(String.class, a -> a.name("name")
                .getter(Category::getName)
                .setter(Category::setName))

            .addAttribute(EnhancedType.listOf(String.class), a -> a.name("parentCategories")
                .getter(Category::getParentCategories)
                .setter(Category::setParentCategories))

            .addAttribute(String.class, a -> a.name("description")
                .getter(Category::getDescription)
                .setter(Category::setDescription))

            .addAttribute(String.class, a -> a.name("imageUrl")
                .getter(Category::getImageUrl)
                .setter(Category::setImageUrl))

            .addAttribute(Integer.class, a -> a.name("displayOrder")
                .getter(Category::getDisplayOrder)
                .setter(Category::setDisplayOrder))

            .addAttribute(Boolean.class, a -> a.name("isActive")
                .getter(Category::getIsActive)
                .setter(Category::setIsActive))

            .addAttribute(Instant.class, a -> a.name("createdAt")
                .getter(Category::getCreatedAt)
                .setter(Category::setCreatedAt))

            .addAttribute(Instant.class, a -> a.name("updatedAt")
                .getter(Category::getUpdatedAt)
                .setter(Category::setUpdatedAt))

            .build();


        public static final TableSchema<Product> PRODUCT_SCHEMA = TableSchema.builder(Product.class)
                        .newItemSupplier(Product::new)
                        .addAttribute(String.class, a -> a.name("companyId")
                                        .getter(Product::getCompanyId)
                                        .setter(Product::setCompanyId)
                                        .tags(StaticAttributeTags.primaryPartitionKey(),
                                                        StaticAttributeTags.secondarySortKey(Product.CATEGORY_GSI)))
                        .addAttribute(String.class, a -> a.name("branchProductKey")
                                        .getter(Product::getBranchProductKey)
                                        .setter(Product::setBranchProductKey)
                                        .tags(StaticAttributeTags.primarySortKey()))
                        .addAttribute(String.class, a -> a.name("branchId")
                                        .getter(Product::getBranchId)
                                        .setter(Product::setBranchId))
                        .addAttribute(String.class, a -> a.name("productId")
                                        .getter(Product::getProductId)
                                        .setter(Product::setProductId))
                        .addAttribute(String.class, a -> a.name("companyName")
                                        .getter(Product::getCompanyName)
                                        .setter(Product::setCompanyName))
                        .addAttribute(String.class, a -> a.name("companyLogo")
                                        .getter(Product::getCompanyLogo)
                                        .setter(Product::setCompanyLogo))
                        .addAttribute(String.class, a -> a.name("name")
                                        .getter(Product::getName)
                                        .setter(Product::setName))
                        .addAttribute(String.class, a -> a.name("description")
                                        .getter(Product::getDescription)
                                        .setter(Product::setDescription))
                        .addAttribute(String.class, a -> a.name("category")
                                        .getter(Product::getCategory)
                                        .setter(Product::setCategory)
                                        .tags(StaticAttributeTags.secondaryPartitionKey(Product.CATEGORY_GSI)))
                        .addAttribute(String.class, a -> a.name("sku")
                                        .getter(Product::getSku)
                                        .setter(Product::setSku))
                        .addAttribute(String.class, a -> a.name("barcode")
                                        .getter(Product::getBarcode)
                                        .setter(Product::setBarcode))
                        .addAttribute(BigDecimal.class, a -> a.name("price")
                                        .getter(Product::getPrice)
                                        .setter(Product::setPrice))
                        .addAttribute(BigDecimal.class, a -> a.name("costPrice")
                                        .getter(Product::getCostPrice)
                                        .setter(Product::setCostPrice))
                        .addAttribute(String.class, a -> a.name("unit")
                                        .getter(Product::getUnit)
                                        .setter(Product::setUnit))
                        .addAttribute(Integer.class, a -> a.name("stockQuantity")
                                        .getter(Product::getStockQuantity)
                                        .setter(Product::setStockQuantity))
                        .addAttribute(Integer.class, a -> a.name("reorderLevel")
                                        .getter(Product::getReorderLevel)
                                        .setter(Product::setReorderLevel))
                        .addAttribute(String.class, a -> a.name("imageUrl")
                                        .getter(Product::getImageUrl)
                                        .setter(Product::setImageUrl))
                        .addAttribute(Boolean.class, a -> a.name("isActive")
                                        .getter(Product::getIsActive)
                                        .setter(Product::setIsActive))
                        .addAttribute(Double.class, a -> a.name("longitude")
                                        .getter(Product::getLongitude)
                                        .setter(Product::setLongitude))
                        .addAttribute(Double.class, a -> a.name("latitude")
                                        .getter(Product::getLatitude)
                                        .setter(Product::setLatitude))
                        .addAttribute(Instant.class, a -> a.name("createdAt")
                                        .getter(Product::getCreatedAt)
                                        .setter(Product::setCreatedAt))
                        .addAttribute(Instant.class, a -> a.name("updatedAt")
                                        .getter(Product::getUpdatedAt)
                                        .setter(Product::setUpdatedAt))
                        .build();

        public static final TableSchema<BranchReview> BRANCH_REVIEW_SCHEMA = TableSchema.builder(BranchReview.class)
                        .newItemSupplier(BranchReview::new)
                        .addAttribute(String.class, a -> a.name("branchKey")
                                        .getter(BranchReview::getBranchKey)
                                        .setter(BranchReview::setBranchKey)
                                        .tags(StaticAttributeTags.primaryPartitionKey(),
                                                        StaticAttributeTags.secondarySortKey(BranchReview.BY_USER_BRANCH_INDEX)))
                        .addAttribute(String.class, a -> a.name("reviewId")
                                        .getter(BranchReview::getReviewId)
                                        .setter(BranchReview::setReviewId)
                                        .tags(StaticAttributeTags.primarySortKey()))
                        .addAttribute(String.class, a -> a.name("branchId")
                                        .getter(BranchReview::getBranchId)
                                        .setter(BranchReview::setBranchId)
                                        .tags(StaticAttributeTags.secondaryPartitionKey(BranchReview.BY_BRANCH_INDEX)))
                        .addAttribute(String.class, a -> a.name("userId")
                                        .getter(BranchReview::getUserId)
                                        .setter(BranchReview::setUserId)
                                        .tags(StaticAttributeTags.secondaryPartitionKey(BranchReview.BY_USER_BRANCH_INDEX)))
                        .addAttribute(String.class, a -> a.name("userName")
                                        .getter(BranchReview::getUserName)
                                        .setter(BranchReview::setUserName))
                        .addAttribute(Integer.class, a -> a.name("rating")
                                        .getter(BranchReview::getRating)
                                        .setter(BranchReview::setRating))
                        .addAttribute(String.class, a -> a.name("comment")
                                        .getter(BranchReview::getComment)
                                        .setter(BranchReview::setComment))
                        .addAttribute(Boolean.class, a -> a.name("isDeleted")
                                        .getter(BranchReview::getIsDeleted)
                                        .setter(BranchReview::setIsDeleted))
                        .addAttribute(Instant.class, a -> a.name("createdAt")
                                        .getter(BranchReview::getCreatedAt)
                                        .setter(BranchReview::setCreatedAt)
                                        .tags(StaticAttributeTags.secondarySortKey(BranchReview.BY_BRANCH_INDEX)))
                        .addAttribute(Instant.class, a -> a.name("updatedAt")
                                        .getter(BranchReview::getUpdatedAt)
                                        .setter(BranchReview::setUpdatedAt))
                        .build();

        public static final TableSchema<OrderItem> ORDER_ITEM_SCHEMA = TableSchema.builder(OrderItem.class)
                        .newItemSupplier(OrderItem::new)
                        .addAttribute(String.class, a -> a.name("productId")
                                        .getter(OrderItem::getProductId)
                                        .setter(OrderItem::setProductId))
                        .addAttribute(String.class, a -> a.name("productName")
                                        .getter(OrderItem::getProductName)
                                        .setter(OrderItem::setProductName))
                        .addAttribute(String.class, a -> a.name("sku")
                                        .getter(OrderItem::getSku)
                                        .setter(OrderItem::setSku))
                        .addAttribute(Integer.class, a -> a.name("quantity")
                                        .getter(OrderItem::getQuantity)
                                        .setter(OrderItem::setQuantity))
                        .addAttribute(BigDecimal.class, a -> a.name("unitPrice")
                                        .getter(OrderItem::getUnitPrice)
                                        .setter(OrderItem::setUnitPrice))
                        .addAttribute(BigDecimal.class, a -> a.name("totalPrice")
                                        .getter(OrderItem::getTotalPrice)
                                        .setter(OrderItem::setTotalPrice))
                        .build();

        public static final TableSchema<Order> ORDER_SCHEMA = TableSchema.builder(Order.class)
        .newItemSupplier(Order::new)

        // =========================
        // PRIMARY KEY + DATE_GSI PK
        // =========================

        .addAttribute(String.class, a -> a.name("companyId")
                .getter(Order::getCompanyId)
                .setter(Order::setCompanyId)
                .tags(
                        StaticAttributeTags.primaryPartitionKey(),
                        StaticAttributeTags.secondaryPartitionKey(Order.DATE_GSI)
                ))

        // =========================
        // PRIMARY SORT KEY
        // =========================

        .addAttribute(String.class, a -> a.name("orderId")
                .getter(Order::getOrderId)
                .setter(Order::setOrderId)
                .tags(StaticAttributeTags.primarySortKey()))

        // =========================
        // ✅ NEW ATTRIBUTE FOR CUSTOMER ORDERS
        // USER_GSI PARTITION KEY
        // =========================

        .addAttribute(String.class, a -> a.name("userId")
                .getter(Order::getUserId)
                .setter(Order::setUserId)
                .tags(
                        StaticAttributeTags.secondaryPartitionKey(Order.USER_GSI)
                ))

        // =========================
        // DATE_GSI SORT KEY
        // USER_GSI SORT KEY
        // =========================

        .addAttribute(Instant.class, a -> a.name("orderDate")
                .getter(Order::getOrderDate)
                .setter(Order::setOrderDate)
                .tags(
                        StaticAttributeTags.secondarySortKey(Order.DATE_GSI),
                        StaticAttributeTags.secondarySortKey(Order.USER_GSI)
                ))

        // =========================
        // OTHER ATTRIBUTES (UNCHANGED)
        // =========================

        .addAttribute(String.class, a -> a.name("status")
                .getter(Order::getStatus)
                .setter(Order::setStatus))

        .addAttribute(String.class, a -> a.name("branchId")
                .getter(Order::getBranchId)
                .setter(Order::setBranchId))

        .addAttribute(String.class, a -> a.name("deliveryAddress")
                .getter(Order::getDeliveryAddress)
                .setter(Order::setDeliveryAddress))

        .addAttribute(
                EnhancedType.listOf(
                        EnhancedType.documentOf(OrderItem.class, ORDER_ITEM_SCHEMA)),
                a -> a.name("items")
                        .getter(Order::getItems)
                        .setter(Order::setItems)
        )

        .addAttribute(BigDecimal.class, a -> a.name("subtotal")
                .getter(Order::getSubtotal)
                .setter(Order::setSubtotal))

        .addAttribute(BigDecimal.class, a -> a.name("taxAmount")
                .getter(Order::getTaxAmount)
                .setter(Order::setTaxAmount))

        .addAttribute(BigDecimal.class, a -> a.name("discountAmount")
                .getter(Order::getDiscountAmount)
                .setter(Order::setDiscountAmount))

        .addAttribute(BigDecimal.class, a -> a.name("totalAmount")
                .getter(Order::getTotalAmount)
                .setter(Order::setTotalAmount))

        .addAttribute(String.class, a -> a.name("paymentMethod")
                .getter(Order::getPaymentMethod)
                .setter(Order::setPaymentMethod))

        .addAttribute(String.class, a -> a.name("paymentStatus")
                .getter(Order::getPaymentStatus)
                .setter(Order::setPaymentStatus))

        .addAttribute(String.class, a -> a.name("issueStatus")
                .getter(Order::getIssueStatus)
                .setter(Order::setIssueStatus))

        .addAttribute(Instant.class, a -> a.name("etaAt")
                .getter(Order::getEtaAt)
                .setter(Order::setEtaAt))

        .addAttribute(Instant.class, a -> a.name("slaDeadlineAt")
                .getter(Order::getSlaDeadlineAt)
                .setter(Order::setSlaDeadlineAt))

        .addAttribute(
                EnhancedType.listOf(
                        EnhancedType.documentOf(OrderTimelineEvent.class, ORDER_TIMELINE_EVENT_SCHEMA)),
                a -> a.name("timelineEvents")
                        .getter(Order::getTimelineEvents)
                        .setter(Order::setTimelineEvents)
        )

        .addAttribute(
                EnhancedType.listOf(String.class),
                a -> a.name("processedIdempotencyKeys")
                        .getter(Order::getProcessedIdempotencyKeys)
                        .setter(Order::setProcessedIdempotencyKeys)
        )

        .addAttribute(
                EnhancedType.mapOf(String.class, String.class),
                a -> a.name("workflowMetadata")
                        .getter(Order::getWorkflowMetadata)
                        .setter(Order::setWorkflowMetadata)
        )

        .addAttribute(String.class, a -> a.name("notes")
                .getter(Order::getNotes)
                .setter(Order::setNotes))

        .addAttribute(Instant.class, a -> a.name("createdAt")
                .getter(Order::getCreatedAt)
                .setter(Order::setCreatedAt))

        .addAttribute(Instant.class, a -> a.name("updatedAt")
                .getter(Order::getUpdatedAt)
                .setter(Order::setUpdatedAt))

        .build();


        public static final TableSchema<Account> ACCOUNT_SCHEMA = TableSchema.builder(Account.class)
                        .newItemSupplier(Account::new)
                        .addAttribute(String.class, a -> a.name("companyAccountKey")
                                        .getter(Account::getCompanyAccountKey)
                                        .setter(Account::setCompanyAccountKey)
                                        .tags(StaticAttributeTags.primaryPartitionKey()))
                        .addAttribute(Instant.class, a -> a.name("date")
                                        .getter(Account::getDate)
                                        .setter(Account::setDate)
                                        .tags(StaticAttributeTags.primarySortKey()))
                        .addAttribute(String.class, a -> a.name("companyId")
                                        .getter(Account::getCompanyId)
                                        .setter(Account::setCompanyId))
                        .addAttribute(String.class, a -> a.name("accountName")
                                        .getter(Account::getAccountName)
                                        .setter(Account::setAccountName))
                        .addAttribute(BigDecimal.class, a -> a.name("balance")
                                        .getter(Account::getBalance)
                                        .setter(Account::setBalance))
                        .addAttribute(BigDecimal.class, a -> a.name("previousBalance")
                                        .getter(Account::getPreviousBalance)
                                        .setter(Account::setPreviousBalance))
                        .addAttribute(String.class, a -> a.name("lastOrderId")
                                        .getter(Account::getLastOrderId)
                                        .setter(Account::setLastOrderId))
                        .addAttribute(Instant.class, a -> a.name("createdAt")
                                        .getter(Account::getCreatedAt)
                                        .setter(Account::setCreatedAt))
                        .addAttribute(Instant.class, a -> a.name("updatedAt")
                                        .getter(Account::getUpdatedAt)
                                        .setter(Account::setUpdatedAt))
                        .build();
     // ✅ UserCompanyRole schema (CORRECT)
public static final TableSchema<UserCompanyRole> USER_COMPANY_ROLE_SCHEMA =
    TableSchema.builder(UserCompanyRole.class)
        .newItemSupplier(UserCompanyRole::new)

        /* ============================
           PRIMARY KEY
        ============================ */

        .addAttribute(String.class, a -> a.name("userId")
            .getter(UserCompanyRole::getUserId)
            .setter(UserCompanyRole::setUserId)
            .tags(StaticAttributeTags.primaryPartitionKey()))

        .addAttribute(String.class, a -> a.name("companyBranch")
            .getter(UserCompanyRole::getCompanyBranch)
            .setter(UserCompanyRole::setCompanyBranch)
            .tags(StaticAttributeTags.primarySortKey()))

        /* ============================
           GSI: companyId-index
        ============================ */

        .addAttribute(String.class, a -> a.name("companyId")
            .getter(UserCompanyRole::getCompanyId)
            .setter(UserCompanyRole::setCompanyId)
            .tags(StaticAttributeTags.secondaryPartitionKey("companyId-index")))

        // ⚠️ DERIVED ATTRIBUTE — NO SETTER
        .addAttribute(String.class, a -> a.name("branchUser")
            .getter(UserCompanyRole::getBranchUser)
            .setter((item, value) -> {}) // ✅ NO-OP setter
            .tags(StaticAttributeTags.secondarySortKey("companyId-index")))

        /* ============================
           OTHER ATTRIBUTES
        ============================ */

        .addAttribute(String.class, a -> a.name("role")
            .getter(UserCompanyRole::getRole)
            .setter(UserCompanyRole::setRole))

        .build();

public static final TableSchema<CompanyJoinRequest> COMPANY_JOIN_REQUEST_SCHEMA =
        TableSchema.builder(CompanyJoinRequest.class)
                .newItemSupplier(CompanyJoinRequest::new)

                /* =========================
                   PRIMARY + GSI PARTITION KEYS
                ========================== */

                .addAttribute(String.class, a -> a.name("companyId")
                        .getter(CompanyJoinRequest::getCompanyId)
                        .setter(CompanyJoinRequest::setCompanyId)
                        .tags(
                                StaticAttributeTags.primaryPartitionKey(),
                                StaticAttributeTags.secondaryPartitionKey("company-status-index")
                        ))

                .addAttribute(String.class, a -> a.name("requestId")
                        .getter(CompanyJoinRequest::getRequestId)
                        .setter(CompanyJoinRequest::setRequestId)
                        .tags(StaticAttributeTags.primarySortKey()))

                .addAttribute(String.class, a -> a.name("userId")
                        .getter(CompanyJoinRequest::getUserId)
                        .setter(CompanyJoinRequest::setUserId)
                        .tags(
                                StaticAttributeTags.secondaryPartitionKey("user-company-index")
                        ))

                /* =========================
                   SORT KEYS
                ========================== */

                .addAttribute(String.class, a -> a.name("companyStatus")
                        .getter(CompanyJoinRequest::getCompanyStatus)
                        .setter(CompanyJoinRequest::setCompanyStatus)
                        .tags(
                                StaticAttributeTags.secondarySortKey("company-status-index")
                        ))

                .addAttribute(String.class, a -> a.name("userCompany")
                        .getter(CompanyJoinRequest::getUserCompany)
                        .setter(CompanyJoinRequest::setUserCompany)
                        .tags(
                                StaticAttributeTags.secondarySortKey("user-company-index")
                        ))

                /* =========================
                   OTHER FIELDS
                ========================== */

                .addAttribute(String.class, a -> a.name("status")
                        .getter(CompanyJoinRequest::getStatus)
                        .setter(CompanyJoinRequest::setStatus))

                .addAttribute(String.class, a -> a.name("message")
                        .getter(CompanyJoinRequest::getMessage)
                        .setter(CompanyJoinRequest::setMessage))

                .addAttribute(Instant.class, a -> a.name("requestedAt")
                        .getter(CompanyJoinRequest::getRequestedAt)
                        .setter(CompanyJoinRequest::setRequestedAt))

                .addAttribute(Instant.class, a -> a.name("reviewedAt")
                        .getter(CompanyJoinRequest::getReviewedAt)
                        .setter(CompanyJoinRequest::setReviewedAt))

                .addAttribute(String.class, a -> a.name("reviewedBy")
                        .getter(CompanyJoinRequest::getReviewedBy)
                        .setter(CompanyJoinRequest::setReviewedBy))

                .addAttribute(Instant.class, a -> a.name("createdAt")
                        .getter(CompanyJoinRequest::getCreatedAt)
                        .setter(CompanyJoinRequest::setCreatedAt))

                .addAttribute(Instant.class, a -> a.name("updatedAt")
                        .getter(CompanyJoinRequest::getUpdatedAt)
                        .setter(CompanyJoinRequest::setUpdatedAt))

                .build();





}
