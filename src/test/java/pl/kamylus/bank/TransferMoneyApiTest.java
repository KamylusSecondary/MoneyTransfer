package pl.kamylus.bank;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.kamylus.bank.helper.HttpUtils;
import pl.kamylus.bank.helper.MultipleTransfersRunnable;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TransferMoneyApiTest {

    private static final int UNPROCESSABLE_ENTITY_ERROR = 422;

    private HttpURLConnection connection;

    private Bank bank;
    private Map<String, Account> accounts;

    @BeforeEach
    void prepareServerAndConnection() throws IOException {
        prepareBank();
        Server.start(HttpUtils.SERVER_PORT, bank);
        connection = HttpUtils.prepareConnection();
    }

    @AfterEach
    void stopServer() {
        connection.disconnect();
        Server.stop();
    }

    @Test
    void transferSmallIntegerTest() throws IOException {
        writeHttp("sourceAccountId=acc1&destinationAccountId=acc2&amount=100");
        final String response = readHttpResponse();

        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals(HttpUtils.OK_RESPONSE, response);
        assertAmountEqualTo("acc1", BigDecimal.ZERO);
        assertAmountEqualTo("acc2", new BigDecimal("300"));
    }

    @Test
    void transferBigIntegerTest() throws IOException {
        writeHttp("sourceAccountId=acc4&destinationAccountId=acc5&amount=10000000000");
        final String response = readHttpResponse();

        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals(HttpUtils.OK_RESPONSE, response);
        assertAmountEqualTo("acc4", new BigDecimal("990000000000"));
        assertAmountEqualTo("acc5", new BigDecimal("10000000001"));
    }

    @Test
    void transferSmallDecimalTest() throws IOException {
        writeHttp("sourceAccountId=acc1&destinationAccountId=acc3&amount=23.15");
        final String response = readHttpResponse();

        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals(HttpUtils.OK_RESPONSE, response);
        assertAmountEqualTo("acc1", new BigDecimal("76.85"));
        assertAmountEqualTo("acc3", new BigDecimal("23.15"));
    }

    @Test
    void transferMultipleDecimalDigitsTest() throws IOException {
        writeHttp("sourceAccountId=acc1&destinationAccountId=acc3&amount=23.1523456");
        final String response = readHttpResponse();

        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals(HttpUtils.OK_RESPONSE, response);
        assertAmountEqualTo("acc1", new BigDecimal("76.8476544"));
        assertAmountEqualTo("acc3", new BigDecimal("23.1523456"));
    }

    @Test
    void transferBigDecimalTest() throws IOException {
        writeHttp("sourceAccountId=acc4&destinationAccountId=acc5&amount=10000000000.99");
        final String response = readHttpResponse();

        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals(HttpUtils.OK_RESPONSE, response);
        assertAmountEqualTo("acc4", new BigDecimal("989999999999.01"));
        assertAmountEqualTo("acc5", new BigDecimal("10000000001.99"));
    }

    @Test
    void transferEntireMoneyTest() throws IOException {
        writeHttp("sourceAccountId=acc2&destinationAccountId=acc3&amount=200");
        final String response = readHttpResponse();

        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals(HttpUtils.OK_RESPONSE, response);
        assertAmountEqualTo("acc2", BigDecimal.ZERO);
        assertAmountEqualTo("acc3", new BigDecimal("200"));
    }

    @Test
    void transferMultipleTimesTest() throws IOException {
        for (int i = 0; i < 500; ++i) {
            connection = HttpUtils.prepareConnection();

            writeHttp("sourceAccountId=acc1&destinationAccountId=acc3&amount=0.01");
            final String response = readHttpResponse();

            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            assertEquals(HttpUtils.OK_RESPONSE, response);
        }

        assertAmountEqualTo("acc1", new BigDecimal("95"));
        assertAmountEqualTo("acc3", new BigDecimal("5"));
    }

    @Test
    void noEnoughMoneyTest() throws IOException {
        writeHttp("sourceAccountId=acc1&destinationAccountId=acc3&amount=100.01");
        final int responseCode = connection.getResponseCode();
        final String response = readHttpResponse();

        assertEquals(UNPROCESSABLE_ENTITY_ERROR, responseCode);
        assertEquals("Insufficient amount (100) on the source account", response);
        assertAmountEqualTo("acc1", new BigDecimal("100"));
        assertAmountEqualTo("acc3", BigDecimal.ZERO);
    }

    @Test
    void sourceAccountNotFoundTest() throws IOException {
        writeHttp("sourceAccountId=SOURCE&destinationAccountId=acc3&amount=1");
        final int responseCode = connection.getResponseCode();
        final String response = readHttpResponse();

        assertEquals(UNPROCESSABLE_ENTITY_ERROR, responseCode);
        assertEquals("Account 'SOURCE' does not exist", response);
        assertAmountEqualTo("acc3", BigDecimal.ZERO);
    }

    @Test
    void destinationAccountNotFoundTest() throws IOException {
        writeHttp("sourceAccountId=acc1&destinationAccountId=DEST&amount=1");
        final int responseCode = connection.getResponseCode();
        final String response = readHttpResponse();

        assertEquals(UNPROCESSABLE_ENTITY_ERROR, responseCode);
        assertEquals("Account 'DEST' does not exist", response);
        assertAmountEqualTo("acc1", new BigDecimal("100"));
    }

    @Test
    void sourceAccountNotProvidedTest() throws IOException {
        writeHttp("destinationAccountId=acc3&amount=1");
        final int responseCode = connection.getResponseCode();
        final String response = readHttpResponse();

        assertEquals(UNPROCESSABLE_ENTITY_ERROR, responseCode);
        assertEquals("SourceAccountId cannot be null or empty", response);
        assertAmountEqualTo("acc3", BigDecimal.ZERO);
    }

    @Test
    void destinationAccountNotProvidedTest() throws IOException {
        writeHttp("sourceAccountId=acc1&amount=1");
        final int responseCode = connection.getResponseCode();
        final String response = readHttpResponse();

        assertEquals(UNPROCESSABLE_ENTITY_ERROR, responseCode);
        assertEquals("DestinationAccountId cannot be null or empty", response);
        assertAmountEqualTo("acc1", new BigDecimal("100"));
    }

    @Test
    void amountNotProvidedTest() throws IOException {
        writeHttp("sourceAccountId=acc1&destinationAccountId=acc3");
        final int responseCode = connection.getResponseCode();
        final String response = readHttpResponse();

        assertEquals(UNPROCESSABLE_ENTITY_ERROR, responseCode);
        assertEquals("Amount cannot be null or empty", response);
        assertAmountEqualTo("acc1", new BigDecimal("100"));
        assertAmountEqualTo("acc3", BigDecimal.ZERO);
    }

    @Test
    void negativeAmountTest() throws IOException {
        writeHttp("sourceAccountId=acc1&destinationAccountId=acc3&amount=-10");
        final int responseCode = connection.getResponseCode();
        final String response = readHttpResponse();

        assertEquals(UNPROCESSABLE_ENTITY_ERROR, responseCode);
        assertEquals("Amount (-10) cannot be less or equal to 0", response);
        assertAmountEqualTo("acc1", new BigDecimal("100"));
        assertAmountEqualTo("acc3", BigDecimal.ZERO);
    }

    @Test
    void zeroAmountTest() throws IOException {
        writeHttp("sourceAccountId=acc1&destinationAccountId=acc3&amount=0");
        final int responseCode = connection.getResponseCode();
        final String response = readHttpResponse();

        assertEquals(UNPROCESSABLE_ENTITY_ERROR, responseCode);
        assertEquals("Amount (0) cannot be less or equal to 0", response);
        assertAmountEqualTo("acc1", new BigDecimal("100"));
        assertAmountEqualTo("acc3", BigDecimal.ZERO);
    }

    @Test
    void notNumberAmountTest() throws IOException {
        writeHttp("sourceAccountId=acc1&destinationAccountId=acc3&amount=abc");
        final int responseCode = connection.getResponseCode();
        final String response = readHttpResponse();

        assertEquals(UNPROCESSABLE_ENTITY_ERROR, responseCode);
        assertEquals("Character a is neither a decimal digit number, decimal point, nor \"e\" notation exponential mark.", response);
        assertAmountEqualTo("acc1", new BigDecimal("100"));
        assertAmountEqualTo("acc3", BigDecimal.ZERO);
    }

    @Test
    void sameAccountsTest() throws IOException {
        writeHttp("sourceAccountId=acc1&destinationAccountId=acc1&amount=10");
        final int responseCode = connection.getResponseCode();
        final String response = readHttpResponse();

        assertEquals(UNPROCESSABLE_ENTITY_ERROR, responseCode);
        assertEquals("Source and destination account ids cannot be the same", response);
        assertAmountEqualTo("acc1", new BigDecimal("100"));
        assertAmountEqualTo("acc3", BigDecimal.ZERO);
    }

    @Test
    void multithreadedTest() throws InterruptedException {
        Runnable transferRunnable =
                new MultipleTransfersRunnable("acc1", "acc2", "0.01", 500);

        final int THREADS_COUNT = 10;

        Thread[] transferThreads = new Thread[THREADS_COUNT];

        for (int i = 0; i < THREADS_COUNT; ++i) {
            transferThreads[i] = new Thread(transferRunnable);
            transferThreads[i].start();
        }

        for (int i = 0; i < THREADS_COUNT; ++i) {
            transferThreads[i].join();
        }

        assertAmountEqualTo("acc1", new BigDecimal("50"));
        assertAmountEqualTo("acc2", new BigDecimal("250"));
    }

    @Test
    void preventDeadlocksTest() throws InterruptedException {
        Runnable transferRunnable =
                new MultipleTransfersRunnable("acc1", "acc2", "0.01", 500);
        Runnable transferBackRunnable =
                new MultipleTransfersRunnable("acc2", "acc1", "0.01", 500);

        final int THREADS_COUNT = 10;

        Thread[] transferThreads = new Thread[THREADS_COUNT];
        Thread[] transferBackThreads = new Thread[THREADS_COUNT];

        for (int i = 0; i < THREADS_COUNT; ++i) {
            transferThreads[i] = new Thread(transferRunnable);
            transferThreads[i].start();
        }

        for (int i = 0; i < THREADS_COUNT; ++i) {
            transferBackThreads[i] = new Thread(transferBackRunnable);
            transferBackThreads[i].start();
        }

        for (int i = 0; i < THREADS_COUNT; ++i) {
            transferThreads[i].join();
            transferBackThreads[i].join();
        }

        assertAmountEqualTo("acc1", new BigDecimal("100"));
        assertAmountEqualTo("acc2", new BigDecimal("200"));
    }

    private String readHttpResponse() throws IOException {
        return HttpUtils.readHttpResponse(connection);
    }

    private void writeHttp(final String content) throws IOException {
        HttpUtils.writeHttpRequest(content, connection);
    }

    private void assertAmountEqualTo(final String accountId, final BigDecimal expectedAmount) {
        assertThat(accounts.get(accountId).getMoneyAmount(), Matchers.comparesEqualTo(expectedAmount));
    }

    private void prepareBank() {
        final LinkedList<Account> accountsList = new LinkedList<>();
        accountsList.add(new Account("acc1", new BigDecimal("100")));
        accountsList.add(new Account("acc2", new BigDecimal("200")));
        accountsList.add(new Account("acc3", BigDecimal.ZERO));
        accountsList.add(new Account("acc4", new BigDecimal("1000000000000")));
        accountsList.add(new Account("acc5", BigDecimal.ONE));

        accounts = accountsList.stream().collect(Collectors.toMap(Account::getId, account -> account));

        bank = new Bank(accountsList);
    }

}
