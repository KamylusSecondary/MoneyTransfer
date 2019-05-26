package pl.kamylus.bank;

import spark.Request;
import spark.Response;
import spark.Route;

import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.Objects;

class TransferMoneyHandler implements Route {

    private static final int UNPROCESSABLE_ENTITY_ERROR = 422;

    private final Bank bank;

    TransferMoneyHandler(final Bank bank) {
        Objects.requireNonNull(bank);

        this.bank = bank;
    }

    @Override
    public Object handle(final Request request, final Response response) {
        try {
            transferMoney(request);
        } catch (final IllegalArgumentException exc) {
            response.status(UNPROCESSABLE_ENTITY_ERROR);
            return exc.getMessage();
        } catch (final Exception exc) {
            response.status(HttpURLConnection.HTTP_INTERNAL_ERROR);
            return exc.getMessage();
        }

        return "OK";
    }

    private void transferMoney(final Request request) {
        final String sourceAccountId = request.queryParams("sourceAccountId");
        final String destinationAccountId = request.queryParams("destinationAccountId");
        final String amountText = request.queryParams("amount");

        if (amountText == null || amountText.isEmpty())
            throw new IllegalArgumentException("Amount cannot be null or empty");

        BigDecimal amount = new BigDecimal(amountText);

        bank.transferMoney(sourceAccountId, destinationAccountId, amount);
    }
}
