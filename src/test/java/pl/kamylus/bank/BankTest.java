package pl.kamylus.bank;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class BankTest {
    private static final String SOURCE_ACCOUNT = "source";
    private static final String DESTINATION_ACCOUNT = "destination";

    @Test
    void constructionNullAccountsTest() {
        assertThrows(NullPointerException.class, () -> new Bank(null));
    }

    @Test
    void constructionDuplicatedAccountIdsTest() {
        final LinkedList<Account> accounts = new LinkedList<>();
        accounts.add(new Account("dup", BigDecimal.ZERO));
        accounts.add(new Account("dup", BigDecimal.ONE));
        accounts.add(new Account("id", BigDecimal.ONE));

        final Exception exc = assertThrows(IllegalArgumentException.class, () -> new Bank(accounts));
        assertEquals("Duplicated account id: dup", exc.getMessage());
    }

    @Test
    void transferMoneyNullSourceAccountIdTest() {
        final Bank bank = new Bank(new LinkedList<>());

        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> bank.transferMoney(null, "dest", BigDecimal.ONE));
        assertEquals("SourceAccountId cannot be null or empty", exc.getMessage());
    }

    @Test
    void transferMoneyEmptySourceAccountIdTest() {
        final Bank bank = new Bank(new LinkedList<>());

        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> bank.transferMoney("", DESTINATION_ACCOUNT, BigDecimal.ONE));
        assertEquals("SourceAccountId cannot be null or empty", exc.getMessage());
    }

    @Test
    void transferMoneyNullDestinationAccountIdTest() {
        final Bank bank = new Bank(new LinkedList<>());

        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> bank.transferMoney(SOURCE_ACCOUNT, null, BigDecimal.ONE));
        assertEquals("DestinationAccountId cannot be null or empty", exc.getMessage());
    }

    @Test
    void transferMoneyEmptyDestinationAccountIdTest() {
        final Bank bank = new Bank(new LinkedList<>());

        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> bank.transferMoney(SOURCE_ACCOUNT, "", BigDecimal.ONE));
        assertEquals("DestinationAccountId cannot be null or empty", exc.getMessage());
    }

    @Test
    void transferMoneyNullAmountTest() {
        final Bank bank = new Bank(new LinkedList<>());

        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> bank.transferMoney(SOURCE_ACCOUNT, DESTINATION_ACCOUNT, null));
        assertEquals("Amount cannot be null", exc.getMessage());
    }

    @Test
    void transferMoneySameAccountsTest() {
        final Bank bank = new Bank(new LinkedList<>());

        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> bank.transferMoney("same", "same", BigDecimal.ONE));
        assertEquals("Source and destination account ids cannot be the same", exc.getMessage());
    }

    @Test
    void transferMoneyZeroAmountTest() {
        final Bank bank = new Bank(new LinkedList<>());

        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> bank.transferMoney(SOURCE_ACCOUNT, DESTINATION_ACCOUNT, BigDecimal.ZERO));
        assertEquals("Amount (0) cannot be less or equal to 0", exc.getMessage());
    }

    @Test
    void transferMoneyNegativeAmountTest() {
        final Bank bank = new Bank(new LinkedList<>());

        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> bank.transferMoney(SOURCE_ACCOUNT, DESTINATION_ACCOUNT, new BigDecimal("-5.12")));
        assertEquals("Amount (-5.12) cannot be less or equal to 0", exc.getMessage());
    }

    @Test
    void transferMoneySourceAccountNotExistsTest() {
        final Bank bank = new Bank(new LinkedList<>());

        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> bank.transferMoney(SOURCE_ACCOUNT, DESTINATION_ACCOUNT, BigDecimal.ONE));
        assertEquals("Account '" + SOURCE_ACCOUNT + "' does not exist", exc.getMessage());
    }

    @Test
    void transferMoneyDestinationAccountNotExistsTest() {
        final LinkedList<Account> accounts = new LinkedList<>();
        accounts.add(new Account(SOURCE_ACCOUNT, BigDecimal.ZERO));
        final Bank bank = new Bank(accounts);

        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> bank.transferMoney(SOURCE_ACCOUNT, DESTINATION_ACCOUNT, BigDecimal.ONE));
        assertEquals("Account '" + DESTINATION_ACCOUNT + "' does not exist", exc.getMessage());
    }

    @Test
    void transferMoneyNoEnoughMoneyTest() {
        final LinkedList<Account> accounts = new LinkedList<>();
        accounts.add(new Account(SOURCE_ACCOUNT, new BigDecimal(10)));
        accounts.add(new Account(DESTINATION_ACCOUNT, BigDecimal.ZERO));
        final Bank bank = new Bank(accounts);

        Exception exc = assertThrows(IllegalArgumentException.class,
                () -> bank.transferMoney(SOURCE_ACCOUNT, DESTINATION_ACCOUNT, new BigDecimal("10.01")));
        assertEquals("Insufficient amount (10) on the source account", exc.getMessage());
    }

    @Test
    void transferMoneySuccessTest() {
        final LinkedList<Account> accounts = new LinkedList<>();

        final Account sourceAccount = mock(Account.class);
        when(sourceAccount.getMoneyAmount()).thenReturn(new BigDecimal("100"));
        when(sourceAccount.getId()).thenReturn("acc1");

        final Account destinationAccount = mock(Account.class);
        when(destinationAccount.getId()).thenReturn("acc2");

        accounts.add(sourceAccount);
        accounts.add(destinationAccount);
        final Bank bank = new Bank(accounts);

        final BigDecimal amountToTransfer = new BigDecimal("10");
        bank.transferMoney("acc1", "acc2", amountToTransfer);

        verify(sourceAccount, times(1)).withdraw(amountToTransfer);
        verify(destinationAccount, times(1)).deposit(amountToTransfer);
    }

    @Test
    void transferMoneySuccessInverseLockTest() {
        final LinkedList<Account> accounts = new LinkedList<>();

        final Account sourceAccount = mock(Account.class);
        when(sourceAccount.getMoneyAmount()).thenReturn(new BigDecimal("100"));
        when(sourceAccount.getId()).thenReturn(SOURCE_ACCOUNT);

        final Account destinationAccount = mock(Account.class);
        when(destinationAccount.getId()).thenReturn(DESTINATION_ACCOUNT);

        accounts.add(sourceAccount);
        accounts.add(destinationAccount);
        final Bank bank = new Bank(accounts);

        final BigDecimal amountToTransfer = new BigDecimal("10");
        bank.transferMoney(SOURCE_ACCOUNT, DESTINATION_ACCOUNT, amountToTransfer);

        verify(sourceAccount, times(1)).withdraw(amountToTransfer);
        verify(destinationAccount, times(1)).deposit(amountToTransfer);
    }
}