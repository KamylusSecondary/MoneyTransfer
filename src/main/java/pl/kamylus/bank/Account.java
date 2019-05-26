package pl.kamylus.bank;

import java.math.BigDecimal;
import java.util.Objects;

class Account {
    private final String id;
    private BigDecimal moneyAmount;

    Account(final String id, final BigDecimal moneyAmount) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(moneyAmount);

        this.id = id;
        this.moneyAmount = moneyAmount;
    }

    String getId() {
        return id;
    }

    BigDecimal getMoneyAmount() {
        return moneyAmount;
    }

    void deposit(final BigDecimal amount) {
        validateAmount(amount);

        moneyAmount = moneyAmount.add(amount);
    }

    void withdraw(final BigDecimal amount) {
        validateAmount(amount);

        moneyAmount = moneyAmount.subtract(amount);
    }

    private void validateAmount(final BigDecimal amount) {
        Objects.requireNonNull(amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount cannot be less or equal to 0");
    }
}
