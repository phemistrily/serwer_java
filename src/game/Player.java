package game;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class Player implements Runnable {
    final UUID uuid;
    final Socket socket;
    Game currentGame;
    BufferedReader input;
    PrintWriter output;

    public Player(Socket socket, UUID uuid) {
        this.uuid = uuid;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            setup();
        }
        catch (IOException e) {
            System.out.println("Exception communicating" + e.getMessage());
        }
    }

    private void setup() throws IOException {
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
        output.println("WELCOME " + uuid + "\n");
        output.println("PlayerId");
        output.println(uuid);
        if (currentGame != null) {
            currentGame.playerReady(this);
            currentGame.messageFromPlayer(this, "Player " + uuid + " connected" + "\n");
        }

    }

    public String getMove() {
        output.println("get_move");
        try {
            int receiveBufferSize = socket.getReceiveBufferSize();
            byte[] buff = new byte[receiveBufferSize];
            int read = socket.getInputStream().read(buff);
            return new String(buff, 0, read);
        }
        catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void setActiveGame(Game game) throws IOException {
        currentGame = game;
        if (input != null && output != null) {
            game.playerReady(this);
        }
    }

    private void publishMessage(String message) {
        if (currentGame != null) {
            currentGame.messageFromPlayer(this, message + "\n");
        }
    }

    public void consumeMessage(String message) {
        if (output != null) {
            output.println(message);
            output.flush();
        }
    }


}
