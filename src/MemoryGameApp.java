import java.io.IOException;
import java.net.ServerSocket;

public class MemoryGameApp {

    public static void main(String[] args) {
        final int serverPort = 7171;
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            new MyServer(serverSocket);
        }
        catch (IOException e) {
           throw new RuntimeException(e);
        }
    }
}
