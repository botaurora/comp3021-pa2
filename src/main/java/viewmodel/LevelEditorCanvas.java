package viewmodel;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static viewmodel.Config.LEVEL_EDITOR_TILE_SIZE;

/**
 * Extends the Canvas class to provide functionality for generating maps and saving them.
 */
public class LevelEditorCanvas extends Canvas {
    private int rows;
    private int cols;

    private Brush[][] map;

    //Stores the last location the player was standing at
    private int oldPlayerRow = -1;
    private int oldPlayerCol = -1;

    /**
     * Call the super constructor. Also resets the map to all {@link Brush#TILE}.
     * Hint: each square cell in the grid has size {@link Config#LEVEL_EDITOR_TILE_SIZE}
     *
     * @param rows The number of rows in the map
     * @param cols The number of tiles in the map
     */
    public LevelEditorCanvas(int rows, int cols) {
        super();

        resetMap(rows, cols);
    }

    /**
     * Setter function. Also resets the map
     *
     * @param rows The number of rows in the map
     * @param cols The numbers of cols in the map
     */
    public void changeSize(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        resetMap(rows, cols);
    }

    /**
     * Assigns {@link LevelEditorCanvas#map} to a new instance, sets all the values to {@link Brush#TILE}, and
     * renders the canvas with the updated map.
     *
     * @param rows The number of rows in the map
     * @param cols The numbers of cols in the map
     */
    private void resetMap(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;

        this.oldPlayerRow = -1;
        this.oldPlayerCol = -1;

        map = new Brush[rows][cols];
        for (Brush[] brushes : map) {
            Arrays.fill(brushes, Brush.TILE);
        }

        renderCanvas();
    }

    /**
     * Render the map using {@link MapRenderer}
     */
    private void renderCanvas() {
        Platform.runLater(() -> MapRenderer.render(this, map));
    }

    /**
     * Sets the applicable {@link LevelEditorCanvas#map} cell to the brush the user currently has selected.
     * In other words, when the user clicks somewhere on the canvas, we translate that into updating one of the
     * tiles in our map.
     * <p>
     * There can only be 1 player on the map. As such, if the user clicks a new position using the player brush,
     * the old location must have the player removed, leaving behind either a tile or a destination underneath,
     * whichever the player was originally standing on.
     * <p>
     * Hint:
     * Don't forget to update the player ({@link Brush#PLAYER_ON_DEST} or {@link Brush#PLAYER_ON_TILE})'s last
     * known position.
     * <p>
     * Finally, render the canvas.
     *
     * @param brush The currently selected brush
     * @param x     Mouse click coordinate x
     * @param y     Mouse click coordinate y
     */
    public void setTile(Brush brush, double x, double y) {
        int mappedR = (int) Math.floor(y) / LEVEL_EDITOR_TILE_SIZE;
        int mappedC = (int) Math.floor(x) / LEVEL_EDITOR_TILE_SIZE;

        if (brush.equals(Brush.PLAYER_ON_DEST) || brush.equals(Brush.PLAYER_ON_TILE)) {
            if (oldPlayerCol != -1 && oldPlayerRow != -1) {
                map[oldPlayerRow][oldPlayerCol] = togglePlayerOnTile(map[oldPlayerRow][oldPlayerCol]);
            }

            oldPlayerRow = mappedR;
            oldPlayerCol = mappedC;
        }

        map[mappedR][mappedC] = brush;

        renderCanvas();
    }

    /**
     * Toggles the presence of player on a brush tile.
     * <p>
     * If the tile already has a non-player occupying or the tile is a wall, has no effect.
     *
     * @param brush Original brush item.
     * @return New brush item with the player removed if originally present, or the player added if originally absent.
     */
    private Brush togglePlayerOnTile(Brush brush) {
        if (brush.equals(Brush.TILE)) {
            return Brush.PLAYER_ON_TILE;
        } else if (brush.equals(Brush.PLAYER_ON_TILE)) {
            return Brush.TILE;
        } else if (brush.equals(Brush.PLAYER_ON_DEST)) {
            return Brush.DEST;
        } else if (brush.equals(Brush.DEST)) {
            return Brush.PLAYER_ON_DEST;
        } else {
            return brush;
        }
    }

    /**
     * Saves the current map to file. Should prompt the user for the save directory before saving the file to
     * the selected location.
     */
    public void saveToFile() {
        //TODO: Check
        if (isInvalidMap()) {
            return;
        }

        File f = getTargetSaveDirectory();
        if (f != null) {
            try {
                if (!f.createNewFile()) {
                    System.err.println("Unable to create new file!");
                    return;
                }

                try (BufferedWriter bf  = new BufferedWriter(new PrintWriter(f))) {
                    bf.write(Integer.valueOf(rows).toString());
                    bf.newLine();
                    bf.write(Integer.valueOf(cols).toString());
                    bf.newLine();

                    for (int i = 0; i < rows; ++i) {
                        Arrays.stream(map[i]).map(Brush::getRep).forEachOrdered(it -> {
                            try {
                                bf.write(it);
                            } catch (IOException e) {
                                System.err.println("Unable to write data!");
                                e.printStackTrace();

                                if (!f.delete()) {
                                    System.err.println("Unable to delete file!");
                                }
                            }
                        });

                    }
                }
            } catch (IOException e) {
                System.err.println("Unable to write data!");
                e.printStackTrace();
            }
        }
    }

    /**
     * Hint: {@link FileChooser} is needed. Also add an extension filter with the following information:
     * description: "Normal text file"
     * extension: "*.txt"
     *
     * @return The directory the user chose to save the map in.
     */
    private File getTargetSaveDirectory() {
        //TODO: Check

        FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Normal text file", Collections.singletonList("*.txt")));

        return chooser.showSaveDialog(null);
    }

    /**
     * Check if the map is valid for saving.
     * Conditions to check:
     * 1. Map must at least have 3 rows and 3 cols
     * 2. Must have a player
     * 3. Balanced number of crates and destinations
     * 4. At least 1 crate and destination
     * <p>
     * Show an Alert if there's an error.
     *
     * @return If the map is invalid
     */
    private boolean isInvalidMap() {
        String reasonText = "";

        List<Brush> cells = Arrays.stream(map).flatMap(Arrays::stream).collect(Collectors.toList());
        long numOfPlayers = cells.parallelStream()
                .filter(it -> it.equals(Brush.PLAYER_ON_DEST) || it.equals(Brush.PLAYER_ON_TILE))
                .count();
        long numOfCrates = cells.parallelStream()
                .filter(it -> it.equals(Brush.CRATE_ON_DEST) || it.equals(Brush.CRATE_ON_TILE))
                .count();
        long numOfDests = cells.parallelStream()
                .filter(it -> it.equals(Brush.DEST) || it.equals(Brush.CRATE_ON_DEST) || it.equals(Brush.PLAYER_ON_DEST))
                .count();

        if (numOfCrates < 1 || numOfDests < 1) {
            reasonText = "Please create at least 1 crate and destination.";
        } else if (numOfCrates != numOfDests) {
            reasonText = "Imbalanced number of crates and destinations";
        } else if (numOfPlayers != 1) {
            reasonText = "Please add a player.";
        } else if (rows < 3 || cols < 3) {
            reasonText = "Minimum size is 3 rows and 3 cols";
        }

        if (!reasonText.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Error");
            a.setHeaderText("Could not save map!");
            a.setContentText(reasonText);
            a.show();
        }

        return !reasonText.isEmpty();
    }

    /**
     * Represents the currently selected brush when the user is making a new map
     */
    public enum Brush {
        TILE("Tile", '.'),
        PLAYER_ON_TILE("Player on Tile", '@'),
        PLAYER_ON_DEST("Player on Destination", '&'),
        CRATE_ON_TILE("Crate on Tile", 'c'),
        CRATE_ON_DEST("Crate on Destination", '$'),
        WALL("Wall", '#'),
        DEST("Destination", 'C');

        private final String text;
        private final char rep;

        Brush(String text, char rep) {
            this.text = text;
            this.rep = rep;
        }

        public static Brush fromChar(char c) {
            for (Brush b : Brush.values()) {
                if (b.getRep() == c) {
                    return b;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return text;
        }

        char getRep() {
            return rep;
        }
    }


}

