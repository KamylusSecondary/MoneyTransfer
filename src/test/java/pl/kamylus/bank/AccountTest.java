package pl.kamylus.bank;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.jupiter.api.Assertions.*;

class AccountTest {
    private static final BigDecimal TEN_ZERO_ONE = new BigDecimal("10.01");

    @Test()
    void constructionNullIdTest() {
        assertThrows(NullPointerException.class, () -> new Account(null, BigDecimal.ZERO));
    }

    @Test()
    void constructionNullAmountTest() {
        assertThrows(NullPointerException.class, () -> new Account("id", null));
    }

    @Test()
    void constructionAndGettersTest() {
        final String id = "id";
        final BigDecimal amount = new BigDecimal("10.12");
        final Account account = new Account(id, amount);

        assertSame(id, account.getId());
        assertSame(amount, account.getMoneyAmount());
    }

    @Test()
    void depositNullTest() {
        final Account account = new Account("id", new BigDecimal("1.23"));

        assertThrows(NullPointerException.class, () -> account.deposit(null));
    }

    @Test()
    void depositZeroTest() {
        final Account account = new Account("id", new BigDecimal("1.23"));

        assertThrows(IllegalArgumentException.class, () -> account.deposit(BigDecimal.ZERO));
    }

    @Test()
    void depositNegativeTest() {
        final Account account = new Account("id", new BigDecimal("1.23"));

        assertThrows(IllegalArgumentException.class, () -> account.deposit(new BigDecimal("-1")));
    }

    @Test()
    void depositSmallIntegerTest() {
        final Account account = new Account("id", new BigDecimal("5.2"));

        account.deposit(new BigDecimal("7"));

        assertThat(account.getMoneyAmount(), comparesEqualTo(new BigDecimal("12.2")));
    }

    @Test()
    void depositSmallDecimalTest() {
        final Account account = new Account("id", new BigDecimal("1.5"));

        account.deposit(new BigDecimal("3.6"));

        assertThat(account.getMoneyAmount(), comparesEqualTo(new BigDecimal("5.1")));
    }

    @Test()
    void depositMultipleSmallDecimalTest() {
        final Account account = new Account("id", BigDecimal.ZERO);

        for (int i = 0; i < 1000; ++i)
            account.deposit(new BigDecimal("0.1"));

        assertThat(account.getMoneyAmount(), comparesEqualTo(new BigDecimal("100")));
    }

    @Test()
    void depositBigIntegerTest() {
        final Account account = new Account("id", TEN_ZERO_ONE);

        account.deposit(new BigDecimal("123456789098765"));

        assertThat(account.getMoneyAmount(), comparesEqualTo(new BigDecimal("123456789098775.01")));
    }

    @Test()
    void depositBigDecimalTest() {
        final Account account = new Account("id", TEN_ZERO_ONE);

        account.deposit(new BigDecimal("123756789098765.99"));

        assertThat(account.getMoneyAmount(), comparesEqualTo(new BigDecimal("123756789098776")));
    }

    @Test()
    void withdrawNullTest() {
        final Account account = new Account("id", new BigDecimal("1.23"));

        assertThrows(NullPointerException.class, () -> account.withdraw(null));
    }

    @Test()
    void withdrawZeroTest() {
        final Account account = new Account("id", new BigDecimal("11.23"));

        assertThrows(IllegalArgumentException.class, () -> account.withdraw(BigDecimal.ZERO));
    }

    @Test()
    void withdrawNegativeTest() {
        final Account account = new Account("id", new BigDecimal("1.1"));

        assertThrows(IllegalArgumentException.class, () -> account.withdraw(new BigDecimal("-2")));
    }

    @Test()
    void withdrawZeroResultTest() {
        final Account account = new Account("id", TEN_ZERO_ONE);

        account.withdraw(TEN_ZERO_ONE);

        assertEquals(0, account.getMoneyAmount().compareTo(BigDecimal.ZERO));
    }

    @Test()
    void withdrawNegativeResultTest() {
        final Account account = new Account("id", new BigDecimal("0.67"));

        account.withdraw(new BigDecimal("5.27"));

        assertThat(account.getMoneyAmount(), comparesEqualTo(new BigDecimal("-4.6")));
    }

    @Test()
    void withdrawSmallIntegerTest() {
        final Account account = new Account("id", new BigDecimal("5.2"));

        account.withdraw(new BigDecimal("3"));

        assertThat(account.getMoneyAmount(), comparesEqualTo(new BigDecimal("2.2")));
    }

    @Test()
    void withdrawSmallDecimalTest() {
        final Account account = new Account("id", new BigDecimal("6.5"));

        account.withdraw(new BigDecimal("3.6"));

        assertThat(account.getMoneyAmount(), comparesEqualTo(new BigDecimal("2.9")));
    }

    @Test()
    void withdrawMultipleSmallDecimalTest() {
        final Account account = new Account("id", new BigDecimal("100.12"));

        for (int i = 0; i < 1000; ++i)
            account.withdraw(new BigDecimal("0.1"));

        assertThat(account.getMoneyAmount(), comparesEqualTo(new BigDecimal("0.12")));
    }

    @Test()
    void withdrawBigIntegerTest() {
        final Account account = new Account("id", new BigDecimal("123456789098766.01"));

        account.withdraw(new BigDecimal("123456789098765"));

        assertThat(account.getMoneyAmount(), comparesEqualTo(new BigDecimal("1.01")));
    }

    @Test()
    void withdrawBigDecimalTest() {
        final Account account = new Account("id", new BigDecimal("123756789098766.03"));

        account.withdraw(new BigDecimal("123756789098765.99"));

        assertThat(account.getMoneyAmount(), comparesEqualTo(new BigDecimal("0.04")));
    }
}
