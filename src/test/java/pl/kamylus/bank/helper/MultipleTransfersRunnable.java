package pl.kamylus.bank.helper;

import java.io.IOException;
import java.net.HttpURLConnection;

public class MultipleTransfersRunnable implements Runnable {

    private final String sourceAccount;
    private final String destinationAccount;
    private final String amount;
    private final int iterations;

    public MultipleTransfersRunnable(final String sourceAccount, final String destinationAccount, final String amount,
                                     final int iterations) {
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.iterations = iterations;
    }

    @Override
    public void run() {
        for (int i = 0; i < iterations; ++i) {
            try {
                final HttpURLConnection connection = HttpUtils.prepareConnection();
                HttpUtils.writeHttpRequest("sourceAccountId=" + sourceAccount +
                        "&destinationAccountId=" + destinationAccount +
                        "&amount=" + amount, connection);

                HttpUtils.readHttpResponse(connection);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
}