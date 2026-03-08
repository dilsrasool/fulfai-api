/*
$env:DDB_ENDPOINT="http://localhost:4566"

$env:COMPANY_TABLE="FulfAI-dev-Company"
$env:BRANCH_TABLE="FulfAI-dev-Branch"
$env:CATEGORY_TABLE="FulfAI-dev-Category"
$env:PRODUCT_TABLE="FulfAI-dev-Product"
$env:ORDER_TABLE="FulfAI-dev-Order"
$env:ACCOUNT_TABLE="FulfAI-dev-Account"
$env:USER_COMPANY_ROLE_TABLE="FulfAI-dev-UserCompanyRole"
$env:COMPANY_JOIN_REQUEST_TABLE="FulfAI-dev-CompanyJoinRequest"

mvn clean compile exec:java "-Dexec.mainClass=com.fulfai.sellingpartner.TestDataSeeder"



*/

package com.fulfai.sellingpartner;

import java.net.URI;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

import com.fulfai.sellingpartner.account.Account;
import com.fulfai.sellingpartner.branch.Branch;
import com.fulfai.sellingpartner.category.Category;
import com.fulfai.sellingpartner.company.Company;
import com.fulfai.sellingpartner.companyJoinRequest.CompanyJoinRequest;
import com.fulfai.sellingpartner.order.Order;
import com.fulfai.sellingpartner.order.OrderItem;
import com.fulfai.sellingpartner.product.Product;
import com.fulfai.sellingpartner.UserCompanyRole.UserCompanyRole;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

public class TestDataSeeder {

    // =========================
    // CONFIG
    // =========================
    private static final Region REGION = Region.US_EAST_1;

    // If local DynamoDB, set env: DDB_ENDPOINT=http://localhost:8000
    private static final String DDB_ENDPOINT = System.getenv().getOrDefault("DDB_ENDPOINT", "");

    // Table names (change if your names differ)
    private static final String COMPANY_TABLE = System.getenv().getOrDefault("COMPANY_TABLE", "FulfAI-dev-company");
    private static final String BRANCH_TABLE = System.getenv().getOrDefault("BRANCH_TABLE", "FulfAI-dev-branch");
    private static final String CATEGORY_TABLE = System.getenv().getOrDefault("CATEGORY_TABLE", "FulfAI-dev-category");
    private static final String PRODUCT_TABLE = System.getenv().getOrDefault("PRODUCT_TABLE", "FulfAI-dev-product");
    private static final String ORDER_TABLE = System.getenv().getOrDefault("ORDER_TABLE", "FulfAI-dev-order");
    private static final String ACCOUNT_TABLE = System.getenv().getOrDefault("ACCOUNT_TABLE", "FulfAI-dev-account");
    private static final String USER_COMPANY_ROLE_TABLE = System.getenv().getOrDefault("USER_COMPANY_ROLE_TABLE", "FulfAI-dev-user_company_role");
    private static final String COMPANY_JOIN_REQUEST_TABLE = System.getenv().getOrDefault("COMPANY_JOIN_REQUEST_TABLE", "FulfAI-dev-company_join_request");

    private static final Random R = new Random();

    public static void main(String[] args) {

        DynamoDbClient dynamoDbClient = buildDynamoClient();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        DynamoDbTable<Company> companyTable = enhancedClient.table(COMPANY_TABLE, Schemas.COMPANY_SCHEMA);
        DynamoDbTable<Branch> branchTable = enhancedClient.table(BRANCH_TABLE, Schemas.BRANCH_SCHEMA);
        DynamoDbTable<Category> categoryTable = enhancedClient.table(CATEGORY_TABLE, Schemas.CATEGORY_SCHEMA);
        DynamoDbTable<Product> productTable = enhancedClient.table(PRODUCT_TABLE, Schemas.PRODUCT_SCHEMA);
        DynamoDbTable<Order> orderTable = enhancedClient.table(ORDER_TABLE, Schemas.ORDER_SCHEMA);
        DynamoDbTable<Account> accountTable = enhancedClient.table(ACCOUNT_TABLE, Schemas.ACCOUNT_SCHEMA);
        DynamoDbTable<UserCompanyRole> userCompanyRoleTable = enhancedClient.table(USER_COMPANY_ROLE_TABLE, Schemas.USER_COMPANY_ROLE_SCHEMA);
        DynamoDbTable<CompanyJoinRequest> joinRequestTable = enhancedClient.table(COMPANY_JOIN_REQUEST_TABLE, Schemas.COMPANY_JOIN_REQUEST_SCHEMA);

        System.out.println("🚀 Seeding test data...");

        // 1) Companies
        List<Company> companies = seedCompanies(companyTable);

        // 2) Branches
        Map<String, List<Branch>> companyBranches = seedBranches(branchTable, companies);

        // 3) Categories
        Map<String, List<Category>> companyCategories = seedCategories(categoryTable, companies);

        // 4) Products
        Map<String, List<Product>> companyProducts = seedProducts(productTable, companies, companyBranches, companyCategories);

        // 5) Orders
        seedOrders(orderTable, companies, companyBranches, companyProducts);

        // 6) Accounts
        seedAccounts(accountTable, companies);

        // 7) UserCompanyRole
        seedUserCompanyRoles(userCompanyRoleTable, companies, companyBranches);

        // 8) Join Requests
        seedJoinRequests(joinRequestTable, companies);

        System.out.println("✅ Done. Test data inserted successfully.");
    }

    // =========================
    // Dynamo client
    // =========================
    private static DynamoDbClient buildDynamoClient() {

        DynamoDbClientBuilder builder = DynamoDbClient.builder()
                .region(REGION)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("dummy", "dummy")
                        )
                );

        if (!DDB_ENDPOINT.isBlank()) {
            builder.endpointOverride(URI.create(DDB_ENDPOINT));
            System.out.println("🔧 Using DynamoDB endpoint: " + DDB_ENDPOINT);
        }

        return builder.build();
    }

    // =========================
    // Seed: Companies
    // =========================
    private static List<Company> seedCompanies(DynamoDbTable<Company> table) {
        List<Company> list = new ArrayList<>();

        list.add(makeCompany("FreshMart", "Dubai", "UAE", "ownerSub-1001"));
        list.add(makeCompany("GadgetHub", "Riyadh", "KSA", "ownerSub-1002"));
        list.add(makeCompany("Bloom & Co", "Karachi", "Pakistan", "ownerSub-1003"));

        for (Company c : list) {
            table.putItem(c);
            System.out.println("🏢 Inserted Company: " + c.getName() + " | id=" + c.getId());
        }

        return list;
    }

    private static Company makeCompany(String name, String city, String country, String ownerSub) {
        Company c = new Company();

        c.setId("comp-" + uuid8());
        c.setOwnerSub(ownerSub);
        c.setJoinCode(UUID.randomUUID().toString());

        c.setName(name);
        c.setAddress(city + " Business Center, Street 10");
        c.setCity(city);
        c.setCountry(country);
        c.setEmail("support@" + name.toLowerCase().replace(" ", "") + ".com");
        c.setLicenseNo("LIC-" + (10000 + R.nextInt(90000)));
        c.setLogo("https://dummycdn.com/logo/" + name.toLowerCase().replace(" ", "-") + ".png");
        c.setPhoneNumber("+0000000000");
        c.setTrn("TRN-" + (100000 + R.nextInt(900000)));
        c.setWebsite("https://www." + name.toLowerCase().replace(" ", "") + ".com");
        c.setState("N/A");
        c.setDescription("Multi-tenant ecommerce company: " + name);
        c.setOperatingCountries(List.of(country, "UAE", "KSA"));

        Instant now = Instant.now();
        c.setCreatedAt(now);
        c.setUpdatedAt(now);

        return c;
    }

    // =========================
    // Seed: Branches
    // =========================
    private static Map<String, List<Branch>> seedBranches(DynamoDbTable<Branch> table, List<Company> companies) {
        Map<String, List<Branch>> map = new HashMap<>();

        for (Company c : companies) {
            List<Branch> branches = new ArrayList<>();

            branches.add(makeBranch(c.getId(), c.getName() + " - Main", c.getCity(), c.getCountry()));
            branches.add(makeBranch(c.getId(), c.getName() + " - Outlet", c.getCity(), c.getCountry()));

            for (Branch b : branches) {
                table.putItem(b);
                System.out.println("🏬 Inserted Branch: " + b.getName() + " | companyId=" + b.getCompanyId());
            }

            map.put(c.getId(), branches);
        }

        return map;
    }

    private static Branch makeBranch(String companyId, String name, String city, String country) {
        Branch b = new Branch();
        b.setCompanyId(companyId);
        b.setBranchId("br-" + uuid8());
        b.setName(name);
        b.setAddress(city + " - Warehouse Zone");
        b.setCity(city);
        b.setCountry(country);
        b.setPhoneNumber("+0000000000");
        b.setEmail(name.toLowerCase().replace(" ", "") + "@example.com");
        b.setManagerName(randomFrom(List.of("Ali", "Sara", "John", "Fatima", "Omar")) + " Manager");
        b.setIsActive(true);
        b.setCreatedAt(Instant.now());
        b.setUpdatedAt(Instant.now());
        return b;
    }

    // =========================
    // Seed: Categories
    // =========================
    private static Map<String, List<Category>> seedCategories(DynamoDbTable<Category> table, List<Company> companies) {

        Map<String, List<Category>> map = new HashMap<>();

        Map<String, List<String>> categoryTree = new LinkedHashMap<>();
        categoryTree.put("Food", List.of("Fruits", "Vegetables", "Snacks", "Beverages"));
        categoryTree.put("Electronics", List.of("Mobile Phones", "Headphones", "TVs"));
        categoryTree.put("Accessories", List.of("Wallets", "Bags", "Watches"));
        categoryTree.put("Gadgets", List.of("Smart Home", "Fitness", "Office"));
        categoryTree.put("Flowers", List.of("Bouquets", "Roses", "Gift Boxes"));
        categoryTree.put("Utensils", List.of("Cookware", "Cutlery", "Storage"));

        for (Company c : companies) {
            List<Category> all = new ArrayList<>();

            Map<String, Category> parents = new HashMap<>();

            // parents
            for (String parentName : categoryTree.keySet()) {
                Category parent = new Category();
                parent.setCompanyId(c.getId());
                parent.setCategoryId("cat-" + uuid8());
                parent.setParentCategoryId("ROOT");
                parent.setParentCategories(List.of("ROOT"));
                parent.setName(parentName);
                parent.setDescription(parentName + " products");
                parent.setImageUrl("https://dummycdn.com/category/" + parentName.toLowerCase() + ".png");
                parent.setDisplayOrder(R.nextInt(20));
                parent.setIsActive(true);
                parent.setCreatedAt(Instant.now());
                parent.setUpdatedAt(Instant.now());

                table.putItem(parent);
                all.add(parent);
                parents.put(parentName, parent);

                System.out.println("📦 Inserted Category(PARENT): " + parentName + " | companyId=" + c.getId());
            }

            // children
            for (Map.Entry<String, List<String>> e : categoryTree.entrySet()) {
                Category parent = parents.get(e.getKey());

                for (String childName : e.getValue()) {
                    Category child = new Category();
                    child.setCompanyId(c.getId());
                    child.setCategoryId("cat-" + uuid8());
                    child.setParentCategoryId(parent.getCategoryId());
                    child.setParentCategories(List.of("ROOT", parent.getCategoryId()));
                    child.setName(childName);
                    child.setDescription(childName + " under " + parent.getName());
                    child.setImageUrl("https://dummycdn.com/category/" + childName.toLowerCase().replace(" ", "-") + ".png");
                    child.setDisplayOrder(R.nextInt(50));
                    child.setIsActive(true);
                    child.setCreatedAt(Instant.now());
                    child.setUpdatedAt(Instant.now());

                    table.putItem(child);
                    all.add(child);

                    System.out.println("📦 Inserted Category(CHILD): " + childName + " | companyId=" + c.getId());
                }
            }

            map.put(c.getId(), all);
        }

        return map;
    }

    // =========================
    // Seed: Products
    // =========================
    private static Map<String, List<Product>> seedProducts(
            DynamoDbTable<Product> table,
            List<Company> companies,
            Map<String, List<Branch>> companyBranches,
            Map<String, List<Category>> companyCategories
    ) {

        Map<String, List<Product>> map = new HashMap<>();

        List<String> productNames = List.of(
                "Premium Dates Box",
                "Organic Honey Jar",
                "Bluetooth Earbuds",
                "Smart Watch Pro",
                "Leather Wallet",
                "Rose Bouquet Deluxe",
                "Kitchen Knife Set",
                "Thermal Flask Bottle",
                "Wireless Charger Pad",
                "LED Desk Lamp",
                "USB-C Cable",
                "Ceramic Plate Set",
                "Chocolate Gift Pack",
                "Perfume Atomizer",
                "Power Bank 20000mAh"
        );

        for (Company c : companies) {
            List<Product> all = new ArrayList<>();

            List<Branch> branches = companyBranches.get(c.getId());
            List<Category> categories = companyCategories.get(c.getId());

            // leaf categories only (children)
            List<Category> leaf = new ArrayList<>();
            for (Category cat : categories) {
                if (cat.getParentCategoryId() != null && !"ROOT".equals(cat.getParentCategoryId())) {
                    leaf.add(cat);
                }
            }

            for (Branch b : branches) {
                int perBranch = 20;

                for (int i = 0; i < perBranch; i++) {
                    Category cat = leaf.get(R.nextInt(leaf.size()));
                    String productId = "prod-" + uuid8();

                    Product p = new Product();
                    p.setCompanyId(c.getId());
                    p.setBranchId(b.getBranchId());
                    p.setProductId(productId);

                    // IMPORTANT: your SK is branchProductKey
                    p.setBranchProductKey(b.getBranchId() + "#" + productId);

                    p.setCompanyName(c.getName());
                    p.setCompanyLogo(c.getLogo());

                    p.setName(productNames.get(R.nextInt(productNames.size())) + " (" + (i + 1) + ")");
                    p.setDescription("Test product for " + c.getName());
                    p.setCategory(cat.getName()); // your schema uses String category

                    p.setSku("SKU-" + uuid8().toUpperCase());
                    p.setBarcode("BC" + (100000000 + R.nextInt(900000000)));

                    BigDecimal cost = money(10, 90);
                    BigDecimal price = cost.multiply(BigDecimal.valueOf(1.35)).setScale(2, BigDecimal.ROUND_HALF_UP);

                    p.setCostPrice(cost);
                    p.setPrice(price);

                    p.setUnit(randomFrom(List.of("pcs", "box", "kg", "pack")));
                    p.setStockQuantity(10 + R.nextInt(200));
                    p.setReorderLevel(5 + R.nextInt(25));
                    p.setImageUrl("https://dummycdn.com/product/" + productId + ".png");
                    p.setIsActive(true);

                    p.setLatitude(24.7136);
                    p.setLongitude(46.6753);

                    p.setCreatedAt(Instant.now());
                    p.setUpdatedAt(Instant.now());

                    table.putItem(p);
                    all.add(p);
                }
            }

            System.out.println("🧾 Inserted Products for companyId=" + c.getId() + " => " + all.size());
            map.put(c.getId(), all);
        }

        return map;
    }

    // =========================
    // Seed: Orders
    // =========================
    private static void seedOrders(
            DynamoDbTable<Order> table,
            List<Company> companies,
            Map<String, List<Branch>> companyBranches,
            Map<String, List<Product>> companyProducts
    ) {

        List<String> statuses = List.of("PENDING", "PAID", "CANCELLED", "DELIVERED");
        List<String> paymentMethods = List.of("CASH", "CARD", "ONLINE");
        List<String> paymentStatuses = List.of("UNPAID", "PAID", "FAILED");

        for (Company c : companies) {
            List<Branch> branches = companyBranches.get(c.getId());
            List<Product> products = companyProducts.get(c.getId());

            int ordersCount = 25;

            for (int o = 0; o < ordersCount; o++) {
                Branch branch = branches.get(R.nextInt(branches.size()));

                Order order = new Order();
                order.setCompanyId(c.getId());
                order.setOrderId("ord-" + uuid8());
                order.setBranchId(branch.getBranchId());

                // last 14 days
                Instant orderDate = Instant.now().minusSeconds(3600L * 24L * (1 + R.nextInt(14)));
                order.setOrderDate(orderDate);

                order.setStatus(randomFrom(statuses));

                int itemsCount = 1 + R.nextInt(5);
                List<OrderItem> items = new ArrayList<>();

                BigDecimal subtotal = BigDecimal.ZERO;

                for (int i = 0; i < itemsCount; i++) {
                    Product p = products.get(R.nextInt(products.size()));
                    int qty = 1 + R.nextInt(4);

                    OrderItem it = new OrderItem();
                    it.setProductId(p.getProductId());
                    it.setProductName(p.getName());
                    it.setSku(p.getSku());
                    it.setQuantity(qty);
                    it.setUnitPrice(p.getPrice());
                    it.setTotalPrice(p.getPrice().multiply(BigDecimal.valueOf(qty)));

                    subtotal = subtotal.add(it.getTotalPrice());
                    items.add(it);
                }

                BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.05)).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal discount = BigDecimal.valueOf(R.nextInt(15)).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal total = subtotal.add(tax).subtract(discount).setScale(2, BigDecimal.ROUND_HALF_UP);

                order.setItems(items);
                order.setSubtotal(subtotal.setScale(2, BigDecimal.ROUND_HALF_UP));
                order.setTaxAmount(tax);
                order.setDiscountAmount(discount);
                order.setTotalAmount(total);

                order.setPaymentMethod(randomFrom(paymentMethods));
                order.setPaymentStatus(randomFrom(paymentStatuses));
                order.setNotes("Seeded order for testing");

                order.setCreatedAt(Instant.now());
                order.setUpdatedAt(Instant.now());

                table.putItem(order);
            }

            System.out.println("🛒 Inserted Orders for companyId=" + c.getId());
        }
    }

    // =========================
    // Seed: Accounts
    // =========================
    private static void seedAccounts(DynamoDbTable<Account> table, List<Company> companies) {
        for (Company c : companies) {

            BigDecimal running = money(500, 2000);

            for (int d = 0; d < 10; d++) {
                LocalDate day = LocalDate.now().minusDays(d);
                Instant dateInstant = day.atStartOfDay().toInstant(ZoneOffset.UTC);

                Account a = new Account();
                a.setCompanyAccountKey(c.getId() + "#MAIN");
                a.setDate(dateInstant);
                a.setCompanyId(c.getId());
                a.setAccountName("Main Account");

                BigDecimal prev = running;
                BigDecimal change = money(-150, 300);
                running = running.add(change);

                a.setPreviousBalance(prev.setScale(2, BigDecimal.ROUND_HALF_UP));
                a.setBalance(running.setScale(2, BigDecimal.ROUND_HALF_UP));
                a.setLastOrderId("ord-" + uuid8());

                a.setCreatedAt(Instant.now());
                a.setUpdatedAt(Instant.now());

                table.putItem(a);
            }

            System.out.println("💰 Inserted Accounts for companyId=" + c.getId());
        }
    }

    // =========================
    // Seed: UserCompanyRole
    // =========================
    private static void seedUserCompanyRoles(
            DynamoDbTable<UserCompanyRole> table,
            List<Company> companies,
            Map<String, List<Branch>> companyBranches
    ) {
        List<String> users = List.of("user-100", "user-101", "user-102", "user-103");
        List<String> roles = List.of("OWNER", "ADMIN", "MANAGER", "STAFF");

        for (String userId : users) {

            // each user gets 2 random company assignments
            List<Company> shuffled = new ArrayList<>(companies);
            Collections.shuffle(shuffled);

            int assignCount = Math.min(2, shuffled.size());

            for (int i = 0; i < assignCount; i++) {
                Company c = shuffled.get(i);
                Branch b = companyBranches.get(c.getId()).get(R.nextInt(companyBranches.get(c.getId()).size()));

                UserCompanyRole ucr = new UserCompanyRole();
                ucr.setUserId(userId);

                // SK = companyBranch
                ucr.setCompanyBranch(c.getId() + "#" + b.getBranchId());

                ucr.setCompanyId(c.getId());
                ucr.setRole(randomFrom(roles));

                table.putItem(ucr);
                System.out.println("👤 Inserted UserCompanyRole: " + userId + " -> " + c.getId());
            }
        }
    }

    // =========================
    // Seed: CompanyJoinRequest
    // =========================
    private static void seedJoinRequests(
            DynamoDbTable<CompanyJoinRequest> table,
            List<Company> companies
    ) {
        List<String> users = List.of("user-200", "user-201", "user-202");
        List<String> statuses = List.of("PENDING", "APPROVED", "REJECTED");

        for (Company c : companies) {
            for (String userId : users) {

                String status = randomFrom(statuses);

                CompanyJoinRequest r = new CompanyJoinRequest();
                r.setCompanyId(c.getId());
                r.setRequestId("req-" + uuid8());

                r.setUserId(userId);
                r.setStatus(status);

                // GSI keys
                r.setCompanyStatus(status + "#" + Instant.now().toString());
                r.setUserCompany(c.getId() + "#" + status);

                r.setMessage("Request to join " + c.getName() + " as staff.");
                r.setRequestedAt(Instant.now().minusSeconds(3600L * (1 + R.nextInt(24))));

                if (!"PENDING".equals(status)) {
                    r.setReviewedAt(Instant.now());
                    r.setReviewedBy("admin-" + uuid8());
                }

                r.setCreatedAt(Instant.now());
                r.setUpdatedAt(Instant.now());

                table.putItem(r);
            }

            System.out.println("📝 Inserted JoinRequests for companyId=" + c.getId());
        }
    }

    // =========================
    // Helpers
    // =========================
    private static String uuid8() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static <T> T randomFrom(List<T> list) {
        return list.get(R.nextInt(list.size()));
    }

    private static BigDecimal money(int min, int max) {
        int val = min + R.nextInt((max - min) + 1);
        return BigDecimal.valueOf(val).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}