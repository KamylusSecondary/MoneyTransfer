# Simple Money Transfer

## General info
This is a simple Java application which:
- runs http server on the particular port (hardcoded, default: 4567)
- creates a few 'bank accounts' in memory
- allows transfer money from the one account to the another using REST API

## REST API
- Server creates REST endpoint on http://localhost:4567/transferMoney. 
Port can be changed in the code (Main.java file).
- The only supported operation is *transferMoney* (POST).
- Required parameters for the operation:
    - *sourceAccountId* (String) - id of the account from which money will be transferred,
    for example: *acc1*
    - *destinationAccountId* (String) - id of the account to which money will be transferred,
    for example: *acc2*
    - *amount* (BigDecimal) - amount of the money to be transferred, for example: 100.12
- Assumptions for the parameters (covered by tests):
    - all of the parameters should be provided
    - accounts' ids should exist in memory of the application
    - sourceAccountId and destinationAccountId should be different
    - amount should be a number
    - amount cannot be less than or equal to 0
    - there should be enough money on source account to perform transfer
- Returned values:
    - HTTP OK (200) and *OK* string when operation performed successfully
    - HTTP Unprocessable Entity (422) and exception string 
        when operation failed due to not met above conditions for the parameters
    - HTTP Internal Server Error (500) and exception string in other cases
- Sample cURL request:  
*curl -d 'sourceAccountId=acc1&destinationAccountId=acc2&amount=100' http://localhost:4567/transferMoney*  

## Default accounts    
Default accounts created by application (*prepareBank* method in Main.java):
- id: *acc1*, starting amount of the money: 100
- id: *acc2*, amount: 200
- id: *acc3*, amount: 0  

## Code
### Build
Application can be build using Maven. Proper pom.xml file is created.
### Application 
Application code is placed under *src/main/java/pl/kamylus/bank* path. Brief description of the files:
- Main.java - starting point of the application. Creates default accounts and starts the server.
- Account.java - stores info about an account (id and amount of the money). Allows withdrawing and depositing of the money.
    At this stage, there is no synchronization (regarding concurrency) and there is possible to have a debit 
    (negative amount of the money after withdrawing). 
- Bank.java - stores data about accounts and allows money transfer. There are implemented synchronization 
    (by locking accounts in strictly defined order) and checking if there is enough money to perform transfer.
- Server.java - simple wrapper for Spark server which starts and stops the REST endpoint.
- TransferMoneyHandler - handles transfer money in terms of REST operation.
### Tests
Tests are placed under *src/test/java/pl/kamylus/bank* path. Brief description:
- AccountTest.java, BankTest.Java, TransferMoneyHandlerTest.java - unit tests.
- TransferMoneyApiTest - integration test for the REST API.
- helper/HttpUtils.java - helper methods for sending/reading http data.
- helper/MultipleTransferRunnable - implementation of Runnable interface which is used 
    for multithreaded tests.
### Libraries/frameworks used
- Spark Java (http://sparkjava.com/) - lightweight framework for creating web applications.
- Slf4j (https://www.slf4j.org/) - logger, used mainly by Spark.
- JUnit 5 (https://junit.org/junit5/) - testing framework.
- Hamcrest (http://hamcrest.org/) - external 'matcher'. Used for more robust BigDecimal assertions.
- Mockito (https://site.mockito.org/) - mocking objects in the tests.

## Possible improvements
The application is very simple, as requested in the requirements. However I would like to list some 
of the possible improvements to inform the recruiters that I'm aware of them:
- Store accounts in real database.
- Implement authentication.
- Implement additional bank operations, for example creating account, getting info of the account, etc.
- Store additional info for the account (for example transaction history, personal data of the owner, etc).
- Return better response for the operation, for example JSON with status, final amount of the money, etc.
- Implement configuration, for example for the server (port, max threads), 
    for bank (number of allowed decimal places in the amount), etc.
- Add logging.
- Implement idempotence, i.e. do not allow to process the same request multiple times. 