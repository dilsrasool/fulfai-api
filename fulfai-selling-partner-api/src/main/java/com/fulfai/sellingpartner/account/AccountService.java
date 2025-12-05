package com.fulfai.sellingpartner.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.stream.Collectors;

import com.fulfai.common.dto.PaginatedResponse;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AccountService {

    @Inject
    AccountRepository accountRepository;

    @Inject
    AccountMapper accountMapper;

    /**
     * Get account history for a company and account name.
     */
    public PaginatedResponse<AccountResponseDTO> getAccountHistory(String companyId, String accountName,
            String nextToken, Integer limit) {
        String effectiveAccountName = accountName != null ? accountName : Account.DEFAULT_ACCOUNT_NAME;
        Log.debugf("Getting account history for company: %s, account: %s", companyId, effectiveAccountName);

        PaginatedResponse<Account> response = accountRepository.getByCompanyAndAccount(
                companyId, effectiveAccountName, nextToken, limit);

        return PaginatedResponse.<AccountResponseDTO>builder()
                .items(response.getItems().stream()
                        .map(accountMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nextToken(response.getNextToken())
                .hasMore(response.isHasMore())
                .build();
    }

    /**
     * Get the latest account balance for a company.
     */
    public AccountResponseDTO getLatestBalance(String companyId, String accountName) {
        String effectiveAccountName = accountName != null ? accountName : Account.DEFAULT_ACCOUNT_NAME;
        Log.debugf("Getting latest balance for company: %s, account: %s", companyId, effectiveAccountName);

        Account account = accountRepository.getLatestByCompanyAndAccount(companyId, effectiveAccountName);
        if (account != null) {
            return accountMapper.toResponseDTO(account);
        }
        return null;
    }

    /**
     * Add amount to account balance (called when order is delivered).
     */
    public AccountResponseDTO addToBalance(String companyId, String accountName, BigDecimal amount, String orderId) {
        String effectiveAccountName = accountName != null ? accountName : Account.DEFAULT_ACCOUNT_NAME;
        Log.debugf("Adding to balance for company: %s, account: %s, amount: %s, orderId: %s",
                companyId, effectiveAccountName, amount, orderId);

        Instant now = Instant.now();

        // Get current balance
        Account latestAccount = accountRepository.getLatestByCompanyAndAccount(companyId, effectiveAccountName);
        BigDecimal previousBalance = latestAccount != null ? latestAccount.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance = previousBalance.add(amount);

        // Create new account entry
        Account account = new Account();
        account.setCompanyAccountKey(Account.buildCompanyAccountKey(companyId, effectiveAccountName));
        account.setDate(now);
        account.setCompanyId(companyId);
        account.setAccountName(effectiveAccountName);
        account.setBalance(newBalance);
        account.setPreviousBalance(previousBalance);
        account.setLastOrderId(orderId);
        account.setCreatedAt(now);
        account.setUpdatedAt(now);

        accountRepository.save(account);
        Log.debugf("Updated balance for company: %s, account: %s, newBalance: %s",
                companyId, effectiveAccountName, newBalance);

        return accountMapper.toResponseDTO(account);
    }
}
