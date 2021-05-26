package my.java.cars.project.client;

import my.java.cars.project.exceptionlogger.ErrorLogger;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";

    private static  ByteBuffer buffer = ByteBuffer.allocateDirect(2024);

    public static boolean serverRunning = false;


    public void run() {

        try (SocketChannel clientChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            clientChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to the server.");
            serverRunning = true;

            while (true) {
                System.out.print("Enter <help> if you need information about the available commands.");
                String message = scanner.nextLine();

                if (message == null || message.isEmpty()) {
                    System.out.println("Invalid command");
                    continue;
                }

                buffer.clear();
                buffer.put(message.getBytes());
                buffer.flip();
                clientChannel.write(buffer);

                System.out.println("Sending request <"
                        + message
                        + "> to our server..."
                        + System.lineSeparator());

                buffer.clear();
                clientChannel.read(buffer);
                buffer.flip();

                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);

                String response = new String(byteArray, StandardCharsets.UTF_8);
                if(response.startsWith("Disconnected")){
                    System.out.println(response);
                    break;
                }
                else {
                    System.out.println(response);
                }

            }
        } catch (Exception e) {
            String errorTextForClient = "There is problem with the server";
            System.out.println(errorTextForClient);
            ErrorLogger.logClientError(e);
        }
    }
    public static void main(String... args) {
        Client cryptoClient = new Client();
        cryptoClient.run();

    }
}
