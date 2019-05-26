package pl.kamylus.bank.helper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {

    public static final int SERVER_PORT = 4567;
    public static final String OK_RESPONSE = "OK";

    private static final String SERVER_URL = "http://localhost:" + SERVER_PORT + "/transferMoney";

    private HttpUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static HttpURLConnection prepareConnection() throws IOException {
        final URL url = new URL(SERVER_URL);

        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        return connection;
    }

    public static void writeHttpRequest(final String content, final HttpURLConnection connection) throws IOException {
        try (final OutputStream os = connection.getOutputStream()) {
            os.write(content.getBytes());
            os.flush();
        }
    }

    public static String readHttpResponse(final HttpURLConnection connection) throws IOException {
        final int responseCode = connection.getResponseCode();

        if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST)
            return readFromStream(connection.getErrorStream());

        return readFromStream(connection.getInputStream());
    }

    private static String readFromStream(final InputStream inputStream) throws IOException {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {

            final StringBuilder responseBuilder = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                responseBuilder.append(line);
            }

            return responseBuilder.toString();
        }
    }
}
