package com.fulfai.sellingpartner.analytics.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.fulfai.sellingpartner.metrics.repository.*;
import com.fulfai.sellingpartner.analytics.dto.*;
import com.fulfai.sellingpartner.branch.BranchRepository;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class AnalyticsService {

    @Inject SellerDailyMetricsRepository sellerRepo;
    @Inject ProductDailyMetricsRepository productRepo;
    @Inject InventorySnapshotRepository inventoryRepo;
    @Inject OrderStatusSummaryRepository orderRepo;
    @Inject BranchRepository branchRepo;

    /* =========================================================
       COMPANY DASHBOARD (ALL BRANCHES AGGREGATED)
    ========================================================= */
    public BranchDashboardDTO getCompanyDashboard(String companyId) {

        /* ---------- seller metrics ---------- */
        var seller = sellerRepo.getLast30Days(companyId);

        double revenue = seller.stream()
                .mapToDouble(s -> s.revenue())
                .sum();

        int orders = seller.stream()
                .mapToInt(s -> s.orders())
                .sum();

        int avg = orders == 0 ? 0 : (int) (revenue / orders);

        var trend = seller.stream()
                .map(s -> new DailyPointDTO(s.date(), s.revenue()))
                .toList();


        /* ---------- top products ---------- */
        String today = LocalDate.now().toString();

        var top = productRepo.getTopProducts(companyId, today, 5)
                .stream()
                .map(p -> new TopProductDTO(
                        p.productId(),
                        p.units(),
                        p.revenue()
                ))
                .toList();


        /* ---------- all branches ---------- */
        var branches = branchRepo.getAll(companyId);

        List<String> branchIds = branches.stream()
                .map(b -> b.getBranchId())
                .toList();


        /* ---------- low stock (aggregate) ---------- */
        var low = branchIds.stream()
                .flatMap(branchId ->
                        inventoryRepo.getLowStock(companyId, branchId).stream()
                )
                .map(s -> new LowStockDTO(
                        s.branchId(),
                        s.productId(),
                        s.stock(),
                        s.reorderLevel()
                ))
                .toList();


        /* ---------- order status summary ---------- */
        var status = orderRepo.getCompanyTodaySummary(companyId, branchIds);


        /* ---------- final ---------- */
        return new BranchDashboardDTO(
                revenue,
                orders,
                avg,
                trend,
                top,
                low,
                status
        );
    }


    /* =========================================================
       BRANCH DASHBOARD (single branch only)
    ========================================================= */
    public BranchDashboardDTO getBranchDashboard(String companyId, String branchId) {

    String today = LocalDate.now().toString();

    /* =============================
       SELLER TREND (safe)
    ============================= */
    var seller = sellerRepo.getLast30Days(companyId);

    double revenue = seller == null ? 0 :
            seller.stream().mapToDouble(s -> s.revenue()).sum();

    int orders = seller == null ? 0 :
            seller.stream().mapToInt(s -> s.orders()).sum();

    int avg = orders == 0 ? 0 : (int)(revenue / orders);

    var trend = seller == null
            ? List.<DailyPointDTO>of()
            : seller.stream()
                    .map(s -> new DailyPointDTO(s.date(), s.revenue()))
                    .toList();


    /* =============================
       TOP PRODUCTS (safe)
    ============================= */
    var top = productRepo
            .getTopProducts(companyId, today, 5)
            .stream()
            .map(p -> new TopProductDTO(
                    p.productId(),
                    p.units(),
                    p.revenue()
            ))
            .toList();


    /* =============================
       LOW STOCK (branch only)
    ============================= */
    var low = inventoryRepo
            .getLowStock(companyId, branchId)
            .stream()
            .map(s -> new LowStockDTO(
                    s.branchId(),
                    s.productId(),
                    s.stock(),
                    s.reorderLevel()
            ))
            .toList();


    /* =============================
       ORDER STATUS (FIXED METHOD)
    ============================= */
    var status = orderRepo
            .getBranchDay(companyId, branchId, today)   // ✅ correct method
            .orElseGet(() -> new OrderStatusSummaryDTO(0,0,0,0,0,0));


    return new BranchDashboardDTO(
            revenue,
            orders,
            avg,
            trend,
            top,
            low,
            status
    );
}

}
