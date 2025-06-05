**Customer Reward Application**
📋 Overview
The Customer Reward Application is a Spring Boot-based RESTful application that calculates and reports customer reward points based on purchase transactions. It applies a point system:
🦋 2 points for every dollar spent over $100
🦋 1 point for every dollar spent between $50 and $100
The application persists transaction data in MongoDB and provides an API endpoint to retrieve monthly and total reward points for each customer.
🛠️ Technologies Used
Technology	Description
Java 17	Modern Java version
Spring Boot 3.x	Microservice framework
Spring Data MongoDB	MongoDB integration
MongoDB	NoSQL database
JUnit 5	Unit testing framework
Mockito	Mocking for unit tests
Maven	Dependency management
Jackson Datatype	JSON support for LocalDateTime
Swagger	REST API Documentation
🏗️ Architecture
+---------------------+
|   Spring Boot App   |
+---------------------+
         |
+---------------------+       +---------------------+
| RewardsController   | ----> |   RewardService     |
+---------------------+       +---------------------+
         |                          |
+---------------------+       +---------------------+
| TransactionRepository | <--- |      MongoDB       |
+---------------------+       +---------------------+

📁 Package Structure
com.customer.rewards
├── controller       --> REST API layer
├── service          --> Business logic
├── repository       --> MongoDB data access
├── model            --> POJOs (Transaction, RewardSummary)
├── exception        --> Custom exceptions & global handler
├── util             --> Constants and helpers
├── configuration    --> Configure the Jackson objectMapper and Swagger documentation
└── DataLoader.java  --> Loads sample transactions

🔢 Reward Calculation Logic
if (amount > 100) {
    points = (amount - 100) * 2 + 50;
} else if (amount > 50) {
    points = (amount - 50) * 1;
} else {
    points = 0;
}
Implemented in RewardService#calculatePoints(double amount).
📅 Sample Dataset
Loaded at application startup using DataLoader.java from resources/transactions.json.
[
 {
 "customerId": "cust1",
 "amount": 120,
 "date": "2024-04-15T10:00:00"
 },
... 
]
🔗 API Endpoints
Get Rewards by Customer ID
**GET /api/rewards/{customerId}**
**Path Variable**: customerId (e.g. cust1)
**Response**: JSON object with monthly and total points
{
 "customerId": "cust1",
 "monthlyPoints": {
 "APRIL": 90,
 "MAY": 110
 },
 "totalPoints": 200
}
Get Rewards for All Customers
**GET /api/rewards/get-all-customer**
**Response**: List of reward summaries for all customers
🔒 Error Handling
Handled globally using @RestControllerAdvice and @ExceptionHandler.
Example: No Data Found
{
 "timestamp": "2025-06-02T10:00:00",
 "status": 404,
 "error": "Not Found",
 "message": "No transactions found for customer: CUST123"
}
🔍 Constants
Defined in com.customer.rewards.util.Constants.java:
public class Constants {
 public static final int LOWER_THRESHOLD = 50;
 public static final int UPPER_THRESHOLD = 100;
 public static final int ONE_POINT = 1;
 public static final int TWO_POINTS = 2;
}
🧪 Unit Testing and Integration Testing
Full unit tests with Mockito and JUnit 5.
100% coverage of business logic branches.
Test location: src/test/java/com/customer/rewards/
🏃‍♂️ How to Run
Prerequisites
- Java 17+
- MongoDB running locally or in cloud
- Maven
-MongoDB : rewardsdb 
Access API
[http://localhost:9193/api/rewards/cust1]
(http://localhost:9193/api/rewards/get-all-customer)
Testing with Postman
Use Postman to test the API endpoints.
