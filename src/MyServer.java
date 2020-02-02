import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import game.Game;
import game.Player;

public class MyServer {
    final ExecutorService executorService;
    Set<Game> activeGames = new LinkedHashSet<>();

    public MyServer(ServerSocket serverSocket) {
        executorService = Executors.newFixedThreadPool(200);

        while (true) {
            try {
                Player player = new Player(serverSocket.accept(), UUID.randomUUID());
                executorService.execute(player);

                Player player2 = new Player(serverSocket.accept(), UUID.randomUUID());
                executorService.execute(player2);

                Game game = new Game(player, player2);
                executorService.execute(game);

                activeGames.add(game);
            }
            catch (Exception ex) {
                System.out.println("Server exception: " + ex.getMessage());
            }
        }
    }
}