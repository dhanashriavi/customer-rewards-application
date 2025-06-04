package com.customer.rewards.controller;

import com.customer.rewards.exception.CustomerNotFoundException;
import com.customer.rewards.model.RewardSummary;
import com.customer.rewards.service.RewardService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration-style test for {@link RewardsController} using Spring context.
 */
@SpringBootTest
class RewardsControllerTest {

    @Autowired
    private RewardsController rewardsController;

    @MockBean
    private RewardService rewardService;

    /**
     * Should return reward summary when a valid customer ID is provided.
     */
    @Test
    void shouldReturnRewardSummary_WhenCustomerIdIsValid() {
        // Arrange
        String customerId = "CUST123";
        Map<Month, Integer> monthlyPoints = new HashMap<>();
        monthlyPoints.put(Month.JANUARY, 120);
        monthlyPoints.put(Month.FEBRUARY, 50);

        RewardSummary expectedSummary = new RewardSummary(customerId, monthlyPoints, 170);
        when(rewardService.getRewardsByCustomer(customerId)).thenReturn(expectedSummary);

        // Act
        RewardSummary actualSummary = rewardsController.getRewards(customerId);

        // Assert
        assertNotNull(actualSummary, "Reward summary should not be null");
        assertEquals(customerId, actualSummary.getCustomerId(), "Customer ID should match");
        assertEquals(170, actualSummary.getTotalPoints(), "Total points should match");
        assertEquals(2, actualSummary.getMonthlyPoints().size(), "Monthly points should contain 2 entries");
        assertEquals(120, actualSummary.getMonthlyPoints().get(Month.JANUARY));
        assertEquals(50, actualSummary.getMonthlyPoints().get(Month.FEBRUARY));

        verify(rewardService, times(1)).getRewardsByCustomer(customerId);
    }

    /**
     * Should return reward summaries for all customers.
     */
    @Test
    void shouldReturnAllCustomerRewards() {
        // Arrange
        Map<Month, Integer> monthlyPoints1 = new HashMap<>();
        monthlyPoints1.put(Month.JANUARY, 120);
        monthlyPoints1.put(Month.FEBRUARY, 50);

        Map<Month, Integer> monthlyPoints2 = new HashMap<>();
        monthlyPoints2.put(Month.JANUARY, 80);
        monthlyPoints2.put(Month.FEBRUARY, 30);

        RewardSummary summary1 = new RewardSummary("CUST123", monthlyPoints1, 170);
        RewardSummary summary2 = new RewardSummary("CUST456", monthlyPoints2, 110);

        when(rewardService.getAllCustomerRewards()).thenReturn(List.of(summary1, summary2));

        // Act
        List<RewardSummary> actualSummaries = rewardsController.getAllCustomerRewards();

        // Assert
        assertNotNull(actualSummaries, "Reward summaries should not be null");
        assertEquals(2, actualSummaries.size(), "There should be 2 reward summaries");
        assertEquals("CUST123", actualSummaries.get(0).getCustomerId(), "First customer ID should match");
        assertEquals(170, actualSummaries.get(0).getTotalPoints(), "First total points should match");
        assertEquals("CUST456", actualSummaries.get(1).getCustomerId(), "Second customer ID should match");
        assertEquals(110, actualSummaries.get(1).getTotalPoints(), "Second total points should match");

        verify(rewardService, times(1)).getAllCustomerRewards();
    }

    /**
     * Should handle case when no reward summaries are found.
     */
    @Test
    void shouldHandleNoCustomerRewards() {
        // Arrange
        when(rewardService.getAllCustomerRewards()).thenReturn(List.of());

        // Act
        List<RewardSummary> actualSummaries = rewardsController.getAllCustomerRewards();

        // Assert
        assertNotNull(actualSummaries, "Reward summaries should not be null");
        assertTrue(actualSummaries.isEmpty(), "Reward summaries should be empty");

        verify(rewardService, times(1)).getAllCustomerRewards();
    }

    /**
     * Should handle invalid customer ID.
     */
    @Test
    void shouldHandleInvalidCustomerId() {
        // Arrange
        String invalidCustomerId = "INVALID123";
        when(rewardService.getRewardsByCustomer(invalidCustomerId)).thenThrow(new CustomerNotFoundException("No transactions found for customer: " + invalidCustomerId));

        // Act & Assert
        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> rewardsController.getRewards(invalidCustomerId)
        );

        assertEquals("No transactions found for customer: " + invalidCustomerId, exception.getMessage());
        verify(rewardService, times(1)).getRewardsByCustomer(invalidCustomerId);
    }
}
