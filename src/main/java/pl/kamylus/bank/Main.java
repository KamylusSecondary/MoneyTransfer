package pl.kamylus.bank;

import java.math.BigDecimal;
import java.util.LinkedList;

class Main {
    private static final int SERVER_PORT = 4567;

    public static void main(String[] args) {
        Server.start(SERVER_PORT, prepareBank());
    }

    private static Bank prepareBank() {
        final LinkedList<Account> accounts = new LinkedList<>();
        accounts.add(new Account("acc1", new BigDecimal("100")));
        accounts.add(new Account("acc2", new BigDecimal("200")));
        accounts.add(new Account("acc3", BigDecimal.ZERO));

        return new Bank(accounts);
    }
}
