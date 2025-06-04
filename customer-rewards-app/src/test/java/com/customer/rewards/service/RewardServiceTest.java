package com.customer.rewards.service;


import com.customer.rewards.exception.CustomerNotFoundException;
import com.customer.rewards.model.RewardSummary;
import com.customer.rewards.model.Transaction;
import com.customer.rewards.repository.TransactionRepository;
import com.customer.rewards.util.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RewardService} using Spring Boot context and @MockBean.
 */
@SpringBootTest
class RewardServiceTest {

    @Autowired
    private RewardService rewardService;

    @MockBean
    private TransactionRepository transactionRepository;

    /**
     * Should return correct reward summary for valid transactions.
     */
    @Test
    void shouldReturnRewardSummaryForValidTransactions() {
        String customerId = "cust123";
        List<Transaction> transactions = List.of(
                new Transaction("1", customerId, 120.0, LocalDateTime.of(2024, 1, 10, 10, 0)),
                new Transaction("2", customerId, 80.0, LocalDateTime.of(2024, 2, 15, 10, 0)),
                new Transaction("3", customerId, 45.0, LocalDateTime.of(2024, 3, 20, 10, 0)) // Below threshold
        );

        when(transactionRepository.findByCustomerId(customerId)).thenReturn(transactions);

        RewardSummary summary = rewardService.getRewardsByCustomer(customerId);

        assertEquals(customerId, summary.getCustomerId());
        assertEquals(90, summary.getMonthlyPoints().get(Month.JANUARY));
        assertEquals(30, summary.getMonthlyPoints().get(Month.FEBRUARY));
        assertEquals(120, summary.getTotalPoints());

        verify(transactionRepository).findByCustomerId(customerId);
    }


    /**
     * Should throw ResourceNotFoundException when no transactions exist.
     */
    @Test
    void shouldThrowExceptionForNoTransactions() {
        String customerId = "emptyUser";

        when(transactionRepository.findByCustomerId(customerId)).thenReturn(Collections.emptyList());

        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> rewardService.getRewardsByCustomer(customerId)
        );

        assertEquals("No transactions found for customer: " + customerId, exception.getMessage());
        verify(transactionRepository).findByCustomerId(customerId);
    }

    /**
     * Should return 0 reward points when transaction amount is exactly at lower threshold.
     */
    @Test
    void shouldReturnZeroPointsForLowerThresholdTransaction() {
        String customerId = "custLow";
        List<Transaction> transactions = List.of(
                new Transaction("1", customerId, Constants.LOWER_THRESHOLD, LocalDateTime.now())
        );

        when(transactionRepository.findByCustomerId(customerId)).thenReturn(transactions);

        RewardSummary summary = rewardService.getRewardsByCustomer(customerId);

        assertEquals(0, summary.getTotalPoints());
    }

    /**
     * Should calculate points correctly for transaction between lower and upper threshold.
     */
    @Test
    void shouldCalculatePointsForMiddleRangeTransaction() {
        String customerId = "custMid";
        List<Transaction> transactions = List.of(
                new Transaction("1", customerId, 75.0, LocalDateTime.of(2024, 4, 1, 12, 0))
        );

        when(transactionRepository.findByCustomerId(customerId)).thenReturn(transactions);

        RewardSummary summary = rewardService.getRewardsByCustomer(customerId);

        assertEquals(25, summary.getTotalPoints());
        assertEquals(25, summary.getMonthlyPoints().get(Month.APRIL));
    }

    /**
     * Should calculate correct points for transaction above upper threshold.
     */
    @Test
    void shouldCalculatePointsForHighValueTransaction() {
        String customerId = "custHigh";
        List<Transaction> transactions = List.of(
                new Transaction("1", customerId, 200.0, LocalDateTime.of(2024, 5, 1, 12, 0))
        );

        when(transactionRepository.findByCustomerId(customerId)).thenReturn(transactions);

        int expectedPoints = (int) ((200 - Constants.UPPER_THRESHOLD) * Constants.TWO_POINTS)
                + (int) ((Constants.UPPER_THRESHOLD - Constants.LOWER_THRESHOLD) * Constants.ONE_POINT);

        RewardSummary summary = rewardService.getRewardsByCustomer(customerId);

        assertEquals(expectedPoints, summary.getTotalPoints());
        assertEquals(expectedPoints, summary.getMonthlyPoints().get(Month.MAY));
    }


    /**
     * Should throw  CustomerNotFoundException for invalid customer ID.
     */
    @Test
    void shouldThrowExceptionForInvalidCustomerId() {
        String invalidCustomerId = "invalidCust";

        when(transactionRepository.findByCustomerId(invalidCustomerId)).thenReturn(Collections.emptyList());

        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> rewardService.getRewardsByCustomer(invalidCustomerId)
        );

        assertEquals("No transactions found for customer: " + invalidCustomerId, exception.getMessage());
        verify(transactionRepository).findByCustomerId(invalidCustomerId);
    }

    /**
     * Should handle transactions with negative amounts.
     */
    @Test
    void shouldHandleNegativeTransactionAmounts() {
        String customerId = "custNegative";
        List<Transaction> transactions = List.of(
                new Transaction("1", customerId, -50.0, LocalDateTime.now())
        );

        when(transactionRepository.findByCustomerId(customerId)).thenReturn(transactions);

        RewardSummary summary = rewardService.getRewardsByCustomer(customerId);

        assertEquals(0, summary.getTotalPoints());
        assertTrue(summary.getMonthlyPoints().isEmpty(), "Monthly points should be empty for negative transaction amounts");
    }

    /**
     * Should handle transactions with future dates.
     */
    @Test
    void shouldHandleFutureDateTransactions() {
        String customerId = "custFuture";
        List<Transaction> transactions = List.of(
                new Transaction("1", customerId, 100.0, LocalDateTime.now().plusDays(1))
        );

        when(transactionRepository.findByCustomerId(customerId)).thenReturn(transactions);

        RewardSummary summary = rewardService.getRewardsByCustomer(customerId);

        assertEquals(0, summary.getTotalPoints());
        assertTrue(summary.getMonthlyPoints().isEmpty());
    }

    /**
     * Should return reward summaries for all customers.
     */
    @Test
    void testGetAllCustomerRewards() {
        List<Transaction> transactions = List.of(
                new Transaction("1", "customer1", 120.0, LocalDateTime.now().minusMonths(1)),
                new Transaction("2", "customer1", 80.0, LocalDateTime.now().minusMonths(2)),
                new Transaction("3", "customer2", 200.0, LocalDateTime.now().minusMonths(3))
        );

        when(transactionRepository.findAllCustomerIds()).thenReturn(transactions);
        when(transactionRepository.findByCustomerId("customer1")).thenReturn(
                List.of(
                        new Transaction("1", "customer1", 120.0, LocalDateTime.now().minusMonths(1)),
                        new Transaction("2", "customer1", 80.0, LocalDateTime.now().minusMonths(2))
                )
        );
        when(transactionRepository.findByCustomerId("customer2")).thenReturn(
                List.of(
                        new Transaction("3", "customer2", 200.0, LocalDateTime.now().minusMonths(3))
                )
        );

        List<RewardSummary> rewardSummaries = rewardService.getAllCustomerRewards();

        assertNotNull(rewardSummaries);
        assertEquals(2, rewardSummaries.size());
        assertEquals("customer1", rewardSummaries.get(0).getCustomerId());
        assertEquals("customer2", rewardSummaries.get(1).getCustomerId());
    }

}
