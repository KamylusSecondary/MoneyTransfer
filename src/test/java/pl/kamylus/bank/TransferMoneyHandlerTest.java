package pl.kamylus.bank;

import org.junit.jupiter.api.Test;
import spark.Request;
import spark.Response;

import java.math.BigDecimal;
import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TransferMoneyHandlerTest {

    private static final int UNPROCESSABLE_ENTITY_ERROR = 422;

    private static final String AMOUNT_PARAMETER = "amount";
    private static final String SOURCE_ACCOUNT_PARAMETER = "sourceAccountId";
    private static final String DESTINATION_ACCOUNT_PARAMETER = "destinationAccountId";

    private static final String SOURCE_ACCOUNT = "source";
    private static final String DESTINATION_ACCOUNT = "destination";

    private static final String ERROR = "error";
    private static final String OK = "OK";

    @Test
    void constructionNullBankTest() {
        assertThrows(NullPointerException.class, () -> new TransferMoneyHandler(null));
    }

    @Test
    void handleNullAmountTest() {
        final Bank bank = mock(Bank.class);
        final TransferMoneyHandler handler = new TransferMoneyHandler(bank);
        final Request request = mock(Request.class);
        when(request.queryParams(AMOUNT_PARAMETER)).thenReturn(null);
        final Response response = mock(Response.class);

        final Object result = handler.handle(request, response);

        assertEquals("Amount cannot be null or empty", result);
        verify(response, times(1)).status(UNPROCESSABLE_ENTITY_ERROR);
    }

    @Test
    void handleEmptyAmountTest() {
        final Bank bank = mock(Bank.class);
        final TransferMoneyHandler handler = new TransferMoneyHandler(bank);
        final Request request = mock(Request.class);
        when(request.queryParams(AMOUNT_PARAMETER)).thenReturn("");
        final Response response = mock(Response.class);

        final Object result = handler.handle(request, response);

        assertEquals("Amount cannot be null or empty", result);
        verify(response, times(1)).status(UNPROCESSABLE_ENTITY_ERROR);
    }

    @Test
    void handleNotNumberAmountTest() {
        final Bank bank = mock(Bank.class);
        final TransferMoneyHandler handler = new TransferMoneyHandler(bank);
        final Request request = mock(Request.class);
        when(request.queryParams(AMOUNT_PARAMETER)).thenReturn("abc");
        final Response response = mock(Response.class);

        final Object result = handler.handle(request, response);

        assertEquals("Character a is neither a decimal digit number, decimal point, nor \"e\" notation exponential mark.", result);
        verify(response, times(1)).status(UNPROCESSABLE_ENTITY_ERROR);
    }

    @Test
    void handleSuccessTest() {
        final Bank bank = mock(Bank.class);
        final TransferMoneyHandler handler = new TransferMoneyHandler(bank);
        final Request request = mock(Request.class);
        when(request.queryParams(SOURCE_ACCOUNT_PARAMETER)).thenReturn(SOURCE_ACCOUNT);
        when(request.queryParams(DESTINATION_ACCOUNT_PARAMETER)).thenReturn(DESTINATION_ACCOUNT);
        when(request.queryParams(AMOUNT_PARAMETER)).thenReturn("10");
        final Response response = mock(Response.class);

        final Object result = handler.handle(request, response);

        verify(bank, times(1)).transferMoney(SOURCE_ACCOUNT, DESTINATION_ACCOUNT, new BigDecimal("10"));
        assertEquals(OK, result);
    }

    @Test
    void handleIllegalArgumentTest() {
        final Bank bank = mock(Bank.class);
        doThrow(new IllegalArgumentException(ERROR)).when(bank).transferMoney(
                SOURCE_ACCOUNT, DESTINATION_ACCOUNT, new BigDecimal("10"));
        final TransferMoneyHandler handler = new TransferMoneyHandler(bank);
        final Request request = mock(Request.class);
        when(request.queryParams(SOURCE_ACCOUNT_PARAMETER)).thenReturn(SOURCE_ACCOUNT);
        when(request.queryParams(DESTINATION_ACCOUNT_PARAMETER)).thenReturn(DESTINATION_ACCOUNT);
        when(request.queryParams(AMOUNT_PARAMETER)).thenReturn("10");
        final Response response = mock(Response.class);

        final Object result = handler.handle(request, response);

        verify(bank, times(1)).transferMoney(SOURCE_ACCOUNT, DESTINATION_ACCOUNT, new BigDecimal("10"));
        verify(response, times(1)).status(UNPROCESSABLE_ENTITY_ERROR);
        assertEquals(ERROR, result);
    }

    @Test
    void handleOtherExceptionTest() {
        final Bank bank = mock(Bank.class);
        doThrow(new RuntimeException(ERROR)).when(bank).transferMoney(
                SOURCE_ACCOUNT, DESTINATION_ACCOUNT, new BigDecimal("10"));
        final TransferMoneyHandler handler = new TransferMoneyHandler(bank);
        final Request request = mock(Request.class);
        when(request.queryParams(SOURCE_ACCOUNT_PARAMETER)).thenReturn(SOURCE_ACCOUNT);
        when(request.queryParams(DESTINATION_ACCOUNT_PARAMETER)).thenReturn(DESTINATION_ACCOUNT);
        when(request.queryParams(AMOUNT_PARAMETER)).thenReturn("10");
        final Response response = mock(Response.class);

        final Object result = handler.handle(request, response);

        verify(bank, times(1)).transferMoney(SOURCE_ACCOUNT, DESTINATION_ACCOUNT, new BigDecimal("10"));
        verify(response, times(1)).status(HttpURLConnection.HTTP_INTERNAL_ERROR);
        assertEquals(ERROR, result);
    }
}