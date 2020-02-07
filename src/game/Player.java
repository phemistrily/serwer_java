package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import com.google.gson.Gson;

public class Player implements Runnable {
    private static final Gson GSON = new Gson();

    final UUID uuid;
    final Socket socket;
    public int points = 1000;
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
        output.println("WELCOME");
        output.println(uuid);

        if (currentGame != null) {
            currentGame.playerReady(this);
            currentGame.messageFromPlayer(this, "Player " + uuid + " connected" + "\n");
        }

    }

    public GameMoveDto getMove() throws IOException {
        output.println("get_move");
        var isSuccess = false;
        GameMoveDto dto = null;
        while(!isSuccess){
            try {
                var dtoJson = input.readLine();
                dto = GSON.fromJson(dtoJson, GameMoveDto.class);
                isSuccess = dto != null;
            }
            catch (Exception ex) {
                isSuccess = false;
            }
        }
        var dtoJson1 = input.readLine();
        dto = GSON.fromJson(dtoJson1, GameMoveDto.class);
        return dto;
//        try {
//            int receiveBufferSize = socket.getReceiveBufferSize();
//            byte[] buff = new byte[receiveBufferSize];
//            int read = socket.getInputStream().read(buff);
//            String message = new String(buff, 0, read);
//            try {
//                return GSON.fromJson(message, GameMoveDto.class);
//            }
//            catch(Exception e) {
//                System.out.println(message);
//                return null;
//            }
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//            throw new RuntimeException("game interrupted", e);
//        }
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

    public void success(int enemyPoints) {
        this.points += 500;
        
        System.out.println(points);
        output.println("successMove");
        output.println(this.points);
        output.println(enemyPoints);
        output.println("success");
    }

    public void fail(int enemyPoints) {
        this.points -= 100;
        System.out.println(points);
        output.println("failMove");
        output.println(this.points);
        output.println(enemyPoints);
        output.println("fail");
    }

    public void syncPoints(int enemyPoints) {
        output.println("syncPoints");
        output.println(this.points);
        output.println(enemyPoints);
    }

    public void syncTiles(ArrayList<String> removeButtonList, Integer countRemoveButton) {
        output.println("syncTiles");
        output.println(countRemoveButton);
        Iterator i = removeButtonList.iterator();
        while (i.hasNext()) {
            String d = String.valueOf(i.next());
            output.println(d);
        }
        output.println("endSyncTiles");
    }

    public void endGame() {
        output.println("endgame");
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
            String idx;
            String image;

            public TileDto(String idx, String image) {
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
