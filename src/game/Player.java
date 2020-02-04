package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

import com.google.gson.Gson;

public class Player implements Runnable {
    private static final Gson GSON = new Gson();

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

        if (currentGame != null) {
            currentGame.playerReady(this);
            currentGame.messageFromPlayer(this, "Player " + uuid + " connected" + "\n");
        }

    }

    public GameMoveDto getMove() {
        output.println("get_move");
        try {
            int receiveBufferSize = socket.getReceiveBufferSize();
            byte[] buff = new byte[receiveBufferSize];
            int read = socket.getInputStream().read(buff);
            String message = new String(buff, 0, read);
            try {
                return GSON.fromJson(message, GameMoveDto.class);
            }
            catch(Exception e) {
                System.out.println(message);
                return null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("game interrupted", e);
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


    static class GameMoveDto {
        TileDto tile1;
        TileDto tile2;

        public GameMoveDto(TileDto tile1, TileDto tile2) {
            this.tile1 = tile1;
            this.tile2 = tile2;
        }

        @Override
        public String toString() {
            return "Tile 1: " + tile1.toString() + "\n" + "Tile 2: " + tile2.toString();
        }

        static class TileDto {
            int idx;
            String image;

            public TileDto(int idx, String image) {
                this.idx = idx;
                this.image = image;
            }

            @Override
            public String toString() {
                return "TileDto{" +
                        "idx=" + idx +
                        ", image='" + image + '\'' +
                        '}';
            }
        }
    }
}
