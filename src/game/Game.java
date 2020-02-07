package game;

import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Game implements Runnable {
    private final Player player1;
    private final Player player2;
    private Player activePlayer;
    private boolean player1Ready = false;
    private boolean player2Ready = false;
    private String[] tilesMap = {
            "img1", "img2", "img3", "img4", "img5",
            "img7", "img10", "img6", "img2", "img8",
            "img3", "img4", "img9", "img7", "img10",
            "img9", "img5", "img1", "img8", "img6",
    };
    private ArrayList<String> removeButtonList = new ArrayList<String>(200);
    private Integer countRemoveButton = 0;

    public Game(Player player1, Player player2) {
        try {
            this.player1 = player1;
            this.player2 = player2;
            this.activePlayer = ACTIVE_PLAYER.getRandomPlayer(player1, player2);
            this.player1.setActiveGame(this);
            this.player2.setActiveGame(this);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] shuffleArray(String[] tilesMap) {
        Random rnd = ThreadLocalRandom.current();
        Integer legnthArray = tilesMap.length - 1;
        for (int i = legnthArray; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            String a = tilesMap[index];
            tilesMap[index] = tilesMap[i];
            tilesMap[i] = a;
        }
        return tilesMap;
    }

    private void start() throws IOException {
        // Game start
        broadcast("Game start!");
        this.tilesMap = shuffleArray(this.tilesMap);
        int moves = 0;
        Player opponent = activePlayer == player1 ? player2 : player1;
        broadcast("Active player: " + activePlayer.uuid);
        broadcast("Oponent: " + opponent.uuid);
        broadcast("Game pushed");
        for (int i = 0; i < tilesMap.length;i++) {
            player1.consumeMessage(tilesMap[i]);
            player2.consumeMessage(tilesMap[i]);
        }
        player1.consumeMessage("endTilesMap");
        player2.consumeMessage("endTilesMap");

        Clock clock = Clock.systemDefaultZone();
        long currentTime = clock.millis();
        String startGameTime = Long.toString(currentTime);
        player1.startGameTimeMessage(startGameTime);
        player2.startGameTimeMessage(startGameTime);

        while(countRemoveButton != 20){
            System.out.println("--Początek  while: ");
            System.out.println(countRemoveButton);
            if (countRemoveButton == 20) {
                player1.endGame();
                player2.endGame();
            }
            var move = activePlayer.getMove();
            System.out.println("Active player: ");
            System.out.println(move);
            if((move.tile1.idx.equals(move.tile2.idx)) && (!move.tile1.image.equals(move.tile2.image)))
            {
                removeButtonList.add(move.tile1.image);
                removeButtonList.add(move.tile2.image);
                countRemoveButton +=2;
                System.out.println(countRemoveButton);
                activePlayer.syncTiles(removeButtonList, countRemoveButton);
                opponent.syncTiles(removeButtonList, countRemoveButton);
                activePlayer.success(opponent.points);
                opponent.syncPoints(activePlayer.points);
            }
            else
            {
                activePlayer.fail(opponent.points);
                opponent.syncPoints(activePlayer.points);
            }
            if (countRemoveButton == 20) {
                player1.endGame();
                player2.endGame();
            }
            var opponentMove = opponent.getMove();
            System.out.println("Opponent player: ");
            System.out.println(opponentMove);
            if((opponentMove.tile1.idx.equals(opponentMove.tile2.idx)) && (!opponentMove.tile1.image.equals(opponentMove.tile2.image)))
            {
                removeButtonList.add(opponentMove.tile1.image);
                removeButtonList.add(opponentMove.tile2.image);
                countRemoveButton +=2;
                System.out.println(countRemoveButton);
                activePlayer.syncTiles(removeButtonList, countRemoveButton);
                opponent.syncTiles(removeButtonList, countRemoveButton);
                opponent.success(activePlayer.points);
                activePlayer.syncPoints(opponent.points);
            }
            else
            {
                opponent.fail(activePlayer.points);
                activePlayer.syncPoints(opponent.points);
            }
//            if(move.tile1.idx == opponentMove.tile1.idx && move.tile2.idx == opponentMove.tile2.idx){
//                opponent.success();
//                activePlayer.fail();
//            }
        }
        player1.endGame();
        player2.endGame();
//        broadcast();
//        while (moves < 10) {
//            System.out.println("Move: #" + moves);
//            move = activePlayer.getMove();
//            broadcast("Player [" + activePlayer.uuid + "] did [" + move + "]");
//            move = oponent.getMove();
//            broadcast("Player [" + oponent.uuid + "] did [" + move + "]");
//            moves++;
//        }
    }

    // One of the  players sent  message, pass it to the other player
    public void messageFromPlayer(Player player, String message) {
        if (player == player1) {
            if (player2Ready) {
                player2.output.println(message);
            }
        }
        else {
            if (player1Ready) {
                player1.output.println(message);
            }
        }
    }

    // When thread set up Player data, notify the game
    public void playerReady(Player player) throws IOException {
        if (player1.equals(player)) {
            player1Ready = true;
            broadcast("Player " + player.uuid + " connected");
        }
        else if (player2.equals(player)) {
            player2Ready = true;
            broadcast("Player " + player.uuid + " connected");
        }
        else {
            throw new IllegalStateException("This player is not part of this game: " + player.uuid);
        }

        if (player1Ready && player2Ready) {
            start();
        }
    }

    private void broadcast(String message) {
        player1.consumeMessage("[broadcast]:" + message);
        player2.consumeMessage("[broadcast]:" + message);
    }

    @Override
    public void run() {
        if (player1Ready && player2Ready) {
            broadcast("Game initialized...");
        }
    }

    enum ACTIVE_PLAYER {
        PLAYER_1, PLAYER_2;

        static Player getRandomPlayer(Player player1, Player player2) {
            ACTIVE_PLAYER active = ACTIVE_PLAYER.values()[(int) (Math.random() * ACTIVE_PLAYER.values().length)];
            return active == PLAYER_1 ? player1 : player2;
        }
    }
}
