package pl.kamylus.bank;

import spark.Spark;

class Server {
    private Server() {
        throw new UnsupportedOperationException("Server class cannot be instantiated");
    }

    static void start(final int port, final Bank bank) {
        Spark.port(port);

        Spark.init();
        Spark.awaitInitialization();

        Spark.post("/transferMoney", new TransferMoneyHandler(bank));
    }

    static void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
