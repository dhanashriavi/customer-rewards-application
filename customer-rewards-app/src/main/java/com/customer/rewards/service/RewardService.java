package com.customer.rewards.service;

import com.customer.rewards.exception.CustomerNotFoundException;
import com.customer.rewards.model.RewardSummary;
import com.customer.rewards.model.Transaction;
import com.customer.rewards.repository.TransactionRepository;
import com.customer.rewards.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class responsible for calculating reward points for customers based on their transactions.
 */
@Service
@Slf4j
public class RewardService {

    private final TransactionRepository transactionRepository;

    public RewardService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Retrieves the reward summary for a specific customer.
     *
     * @param customerId the ID of the customer
     * @return the reward summary containing monthly and total reward points
     * @throws CustomerNotFoundException if no transactions are found for the customer
     */
    public RewardSummary getRewardsByCustomer(String customerId) {
        List<Transaction> transactions = transactionRepository.findByCustomerId(customerId);

        if (transactions == null || transactions.isEmpty()) {
            throw new CustomerNotFoundException("No transactions found for customer: " + customerId);
        }

        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3).with(TemporalAdjusters.firstDayOfMonth());

        Map<Month, Integer> monthlyPoints = new HashMap<>();
        int totalPoints = 0;

        for (Transaction transaction : transactions) {
            if (transaction.getDate().isAfter(LocalDateTime.now()) || transaction.getDate().isBefore(threeMonthsAgo)) {
                continue; // Skip future transactions and transactions older than 3 months
            }
            int points = calculateRewardPoints(transaction.getAmount());
            if (points > 0) { // Only add points if they are greater than 0
                Month month = transaction.getDate().getMonth();
                monthlyPoints.merge(month, points, Integer::sum);
                totalPoints += points;
            }
        }



        return RewardSummary.builder()
                .customerId(customerId)
                .monthlyPoints(monthlyPoints)
                .totalPoints(totalPoints)
                .build();
    }



    /**
     * Calculates reward points based on the transaction amount using predefined thresholds.
     *
     * @param amount the transaction amount
     * @return the calculated reward points
     */
    private int calculateRewardPoints(double amount) {
        if (amount < 0) {
            return 0; // No points for negative amounts
        }

        int points = 0;

        if (amount > Constants.UPPER_THRESHOLD) {
            points += (int) ((amount - Constants.UPPER_THRESHOLD) * Constants.TWO_POINTS);
            points += (int) ((Constants.UPPER_THRESHOLD - Constants.LOWER_THRESHOLD) * Constants.ONE_POINT);
        } else if (amount > Constants.LOWER_THRESHOLD) {
            points += (int) ((amount - Constants.LOWER_THRESHOLD) * Constants.ONE_POINT);
        }


        return points;
    }



    /**
     * Retrieves the reward summaries for all customers.
     *
     * @return a list of reward summaries for all customers
     */
    public List<RewardSummary> getAllCustomerRewards() {
        List<Transaction> transactions = transactionRepository.findAllCustomerIds();

        List<String> customerIds = transactions.stream()
                .map(Transaction::getCustomerId)
                .distinct()
                .toList();

        log.info("Getting Customers: {}", customerIds);
        return customerIds.stream()
                .map(this::getRewardsByCustomer)
                .collect(Collectors.toList());
    }
}
