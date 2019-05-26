package pl.kamylus.bank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

class Bank {
    private static final Logger logger = LoggerFactory.getLogger(Bank.class);
    private final Map<String, Account> accounts;

    Bank(final List<Account> accounts) {
        Objects.requireNonNull(accounts);

        this.accounts = prepareAccountsMap(accounts);
    }

    private static Map<String, Account> prepareAccountsMap(final List<Account> accounts) {
        return accounts.stream().collect(Collectors.toMap(Account::getId, account -> account,
                (a1, a2) -> {
                    throw new IllegalArgumentException("Duplicated account id: " + a1.getId());
                },
                HashMap::new));
    }

    void transferMoney(final String sourceAccountId, final String destinationAccountId, final BigDecimal amount) {
        validateTransferParameters(sourceAccountId, destinationAccountId, amount);

        final Account source = accounts.get(sourceAccountId);
        final Account destination = accounts.get(destinationAccountId);

        Object lock1;
        Object lock2;
        if (sourceAccountId.compareTo(destinationAccountId) < 0) {
            lock1 = source;
            lock2 = destination;
        } else {
            lock1 = destination;
            lock2 = source;
        }

        synchronized (lock1) {
            synchronized (lock2) {
                if (source.getMoneyAmount().compareTo(amount) < 0) {
                    throw new IllegalArgumentException("Insufficient amount (" + source.getMoneyAmount()
                            + ") on the source account");
                }

                source.withdraw(amount);
                destination.deposit(amount);

                logger.info("Transferred {} from {} to {}. Amount on source: {}. Amount on destination: {}",
                        amount, sourceAccountId, destinationAccountId, source.getMoneyAmount(), destination.getMoneyAmount());
            }
        }
    }

    private void validateTransferParameters(final String sourceAccountId, final String destinationAccountId,
                                            final BigDecimal amount) {
        if (sourceAccountId == null || sourceAccountId.isEmpty())
            throw new IllegalArgumentException("SourceAccountId cannot be null or empty");

        if (destinationAccountId == null || destinationAccountId.isEmpty())
            throw new IllegalArgumentException("DestinationAccountId cannot be null or empty");

        if (amount == null)
            throw new IllegalArgumentException("Amount cannot be null");

        if (sourceAccountId.equals(destinationAccountId))
            throw new IllegalArgumentException("Source and destination account ids cannot be the same");

        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount (" + amount + ") cannot be less or equal to 0");

        checkAccountExistence(sourceAccountId);
        checkAccountExistence(destinationAccountId);
    }

    private void checkAccountExistence(final String accountId) {
        if (accounts.get(accountId) == null)
            throw new IllegalArgumentException("Account '" + accountId + "' does not exist");
    }
}
