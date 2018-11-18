package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import model.Exceptions.InvalidMapException;
import model.Map.Cell;
import model.Map.Map;
import model.Map.Occupant.Crate;
import model.Map.Occupiable.DestTile;
import model.Map.Occupiable.Occupiable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * A class that loads, stores, modifies, and keeps track of the game map win/deadlock condition. Also keeps tracks
 * of information about this current level, e.g. how many moves the player has made.
 */
public class GameLevel {

    private final IntegerProperty numPushes = new SimpleIntegerProperty(0);
    private Map map;

    public IntegerProperty numPushesProperty() {
        return numPushes;
    }

    public Map getMap() {
        return map;
    }

    /**
     * Loads and reads the map line by line, instantiates and initializes map
     *
     * @param filename the map text filename
     * @throws InvalidMapException when the map is invalid
     */
    public void loadMap(String filename) throws InvalidMapException {
        File f = new File(filename);
        try (Scanner reader = new Scanner(f)) {
            int numRows = reader.nextInt();
            int numCols = reader.nextInt();
            reader.nextLine();

            char[][] rep = new char[numRows][numCols];
            for (int r = 0; r < numRows; r++) {
                String row = reader.nextLine();
                for (int c = 0; c < numCols; c++) {
                    rep[r][c] = row.charAt(c);
                }
            }

            map = new Map();
            map.initialize(numRows, numCols, rep);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return Whether or not the win condition has been satisfied
     */
    public boolean isWin() {
        return map.getDestTiles().stream().allMatch(DestTile::isCompleted);
    }

    /**
     * When no crates can be moved but the game is not won, then deadlock has occurred
     *
     * @return Whether deadlock has occurred
     */
    public boolean isDeadlocked() {
        return map.getCrates().stream().anyMatch(c -> !isCrateMovable(c)) && !isWin();
    }

    /**
     * Checks whether a crate is movable.
     *
     * @param c Crate to check.
     * @return True if the crate can still be moved.
     */
    private boolean isCrateMovable(Crate c) {
        final Cell[][] cells = map.getCells();

        Cell leftCell = cells[c.getR() - 1][c.getC()];
        Cell rightCell = cells[c.getR() + 1][c.getC()];
        Cell upCell = cells[c.getR()][c.getC() - 1];
        Cell downCell = cells[c.getR()][c.getC() + 1];

        if (leftCell instanceof Occupiable && rightCell instanceof Occupiable) {
            if (!(((Occupiable) leftCell).getOccupant().isPresent() && ((Occupiable) rightCell).getOccupant().isPresent())) {
                return true;
            }
        }

        if (upCell instanceof Occupiable && downCell instanceof Occupiable) {
            if (!(((Occupiable) upCell).getOccupant().isPresent() && ((Occupiable) downCell).getOccupant().isPresent())) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param c The char corresponding to a move from the user
     *          w: up
     *          a: left
     *          s: down
     *          d: right
     * @return Whether or not the move was successful
     */
    public boolean makeMove(char c) {
        boolean madeMove = false;
        switch (c) {
            case 'w':
                madeMove = map.movePlayer(Map.Direction.UP);
                break;
            case 'a':
                madeMove = map.movePlayer(Map.Direction.LEFT);
                break;
            case 's':
                madeMove = map.movePlayer(Map.Direction.DOWN);
                break;
            case 'd':
                madeMove = map.movePlayer(Map.Direction.RIGHT);
                break;
        }
        if (madeMove) {
            numPushes.setValue(numPushes.getValue() + 1);
        }
        return madeMove;
    }
}
