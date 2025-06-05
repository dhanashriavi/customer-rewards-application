# Customer Reward Application

## 📋 Overview
The Customer Reward Application is a Spring Boot-based RESTful application that calculates and reports customer reward points based on purchase transactions. It applies a point system:
- 🦋 2 points for every dollar spent over $100
- 🦋 1 point for every dollar spent between $50 and $100

The application persists transaction data in MongoDB and provides an API endpoint to retrieve monthly and total reward points for each customer.

## 🛠️ Technologies Used
| Technology              | Description                      |
|-------------------------|----------------------------------|
| **Java 17**             | Modern Java version              |
| **Spring Boot 3.x**     | Microservice framework           |
| **Spring Data MongoDB** | MongoDB integration              |
| **MongoDB**             | NoSQL database                   |
| **JUnit 5**             | Unit testing framework           |
| **Mockito**             | Mocking for unit tests           |
| **Maven**               | Dependency management            |
| **Jackson Datatype**    | JSON support for LocalDateTime   |
| **Swagger**             | REST API Documentation           |

## 🏗️ Architecture
# Spring Boot Rewards Application

This is a Spring Boot application that manages rewards. The application follows a layered architecture with controllers, services, and repositories.

## Architecture

```plaintext
+---------------------+
|   Spring Boot App   |
+---------------------+
          |
+---------------------+       +---------------------+
|  RewardsController  | ----> |    RewardService    |
+---------------------+       +---------------------+
          |                           |
+---------------------+       +---------------------+
| TransactionRepository | <--- |       MongoDB      |
+---------------------+       +---------------------+


## 📁 Package Structure

| Package                | Description                                           |
|------------------------|-------------------------------------------------------|
| **controller**         | REST API layer                                        |
| **service**            | Business logic                                        |
| **repository**         | MongoDB data access                                   |
| **model**              | POJOs (Transaction, RewardSummary)                    |
| **exception**          | Custom exceptions & global handler                    |
| **util**               | Constants and helpers                                 |
| **configuration**      | Configure the Jackson objectMapper and Swagger documentation |
| **DataLoader.java**    | Loads sample transactions                             |
                            |

## 🔢 Reward Calculation Logic

The reward points are calculated based on the transaction amount using the following logic:

```java
if (amount > 100) {
    points = (amount - 100) * 2 + 50;
} else if (amount > 50) {
    points = (amount - 50) * 1;
} else {
    points = 0;
}
## 📅 Sample Dataset

Below is a sample dataset for transactions:

```json
[
  {
    "customerId": "cust1",
    "amount": 120,
    "date": "2024-04-15T10:00:00"
  },
  {
    "customerId": "cust2",
    "amount": 75,
    "date": "2024-04-16T11:30:00"
  },
  {
    "customerId": "cust3",
    "amount": 45,
    "date": "2024-04-17T09:15:00"
  }
]
## API Endpoints

### Get Rewards by Customer ID

**Endpoint:** `GET /api/rewards/{customerId}`

**Path Variable:** `customerId` (e.g., `cust1`)

**Response:** JSON object with monthly and total points

```json
{
  "customerId": "cust1",
  "monthlyPoints": {
    "APRIL": 90,
    "MAY": 110
  },
  "totalPoints": 200
}
# Customer Rewards Service

## Constants
Defined in `com.customer.rewards.util.Constants.java`:
```java
public class Constants {
    public static final int LOWER_THRESHOLD = 50;
    public static final int UPPER_THRESHOLD = 100;
    public static final int ONE_POINT = 1;
    public static final int TWO_POINTS = 2;
}
# Customer Rewards Service

## Unit Testing and Integration Testing
- Full unit tests with Mockito and JUnit 5.
- 100% coverage of business logic branches.
- Test location: `src/test/java/com/customer/rewards/`

## How to Run

### Prerequisites
- Java 17+
- MongoDB running locally or in the cloud
- Maven
- MongoDB Database: `rewardsdb`

### Access API
- **Get Customer Rewards**
   [http://localhost:9193/api/rewards/cust1]
- **Get All Customers**
(http://localhost:9193/api/rewards/get-all-customer)

### Testing with Postman
Use Postman to test the API endpoints.



