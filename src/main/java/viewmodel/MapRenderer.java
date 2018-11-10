package viewmodel;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import model.Map.Cell;
import model.Map.Occupant.Crate;
import model.Map.Occupant.Player;
import model.Map.Occupiable.DestTile;
import model.Map.Occupiable.Occupiable;
import model.Map.Occupiable.Tile;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static viewmodel.Config.LEVEL_EDITOR_TILE_SIZE;

/**
 * Renders maps onto canvases
 */
public class MapRenderer {
    private static Image wall = null;
    private static Image crateOnTile = null;
    private static Image crateOnDest = null;

    private static Image playerOnTile = null;
    private static Image playerOnDest = null;

    private static Image dest = null;
    private static Image tile = null;

    static {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();

            URL wallRes = loader.getResource("assets/images/wall.png");
            URL crateOnTileRes = loader.getResource("assets/images/crateOnTile.png");
            URL crateOnDestRes = loader.getResource("assets/images/crateOnDest.png");
            URL playerOnTileRes = loader.getResource("assets/images/playerOnTile.png");
            URL playerOnDestRes = loader.getResource("assets/images/playerOnDest.png");
            URL destRes = loader.getResource("assets/images/dest.png");
            URL tileRes = loader.getResource("assets/images/tile.png");

            assert wallRes != null;
            assert crateOnTileRes != null;
            assert crateOnDestRes != null;
            assert playerOnTileRes != null;
            assert playerOnDestRes != null;
            assert destRes != null;
            assert tileRes != null;

            wall = new Image(wallRes.toURI().toString());
            crateOnTile = new Image(crateOnTileRes.toURI().toString());
            crateOnDest = new Image(crateOnDestRes.toURI().toString());
            playerOnTile = new Image(playerOnTileRes.toURI().toString());
            playerOnDest = new Image(playerOnDestRes.toURI().toString());
            dest = new Image(destRes.toURI().toString());
            tile = new Image(tileRes.toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new IllegalStateException("Cannot load image resources");
        }
    }

    private static class TileImageMapping {
        private Image tile;
        private Image tileWithCrate;
        private Image tileWithPlayer;

        private TileImageMapping(Image tile, Image withCrate, Image withPlayer) {
            this.tile = tile;
            this.tileWithCrate = withCrate;
            this.tileWithPlayer = withPlayer;

            assert this.tile.getWidth() == tileWithCrate.getWidth();
            assert this.tile.getHeight() == tileWithCrate.getHeight();
            assert this.tile.getWidth() == tileWithPlayer.getWidth();
            assert this.tile.getHeight() == tileWithPlayer.getHeight();
        }
    }

    private final static Map<Class<? extends Tile>, TileImageMapping> IMAGE_MAPPING = new HashMap<Class<? extends Tile>, TileImageMapping>() {{
        put(Tile.class, new TileImageMapping(tile, crateOnTile, playerOnTile));
        put(DestTile.class, new TileImageMapping(dest, crateOnDest, playerOnDest));
    }};
    private final static Map<LevelEditorCanvas.Brush, Image> BRUSH_IMAGE_MAP = new HashMap<LevelEditorCanvas.Brush, Image>() {{
        put(LevelEditorCanvas.Brush.CRATE_ON_DEST, crateOnDest);
        put(LevelEditorCanvas.Brush.CRATE_ON_TILE, crateOnTile);
        put(LevelEditorCanvas.Brush.DEST, dest);
        put(LevelEditorCanvas.Brush.PLAYER_ON_DEST, playerOnDest);
        put(LevelEditorCanvas.Brush.PLAYER_ON_TILE, playerOnTile);
        put(LevelEditorCanvas.Brush.TILE, tile);
        put(LevelEditorCanvas.Brush.WALL, wall);
    }};

    /**
     * Render the map onto the canvas. This method can be used in Level Editor
     * <p>
     * Hint: set the canvas height and width as a multiple of the rows and cols
     *
     * @param canvas The canvas to be rendered onto
     * @param map    The map holding the current state of the game
     */
    static void render(@NotNull Canvas canvas, @NotNull LevelEditorCanvas.Brush[][] map) {
        canvas.setWidth(map[0].length * LEVEL_EDITOR_TILE_SIZE);
        canvas.setHeight(map.length * LEVEL_EDITOR_TILE_SIZE);

        for (int r = 0; r < map.length; ++r) {
            for (int c = 0; c < map[r].length; ++c) {
                Image image = BRUSH_IMAGE_MAP.get(map[r][c]);
                assert image != null;

                canvas.getGraphicsContext2D().drawImage(image, c * LEVEL_EDITOR_TILE_SIZE, r * LEVEL_EDITOR_TILE_SIZE);
            }
        }
    }

    /**
     * Render the map onto the canvas. This method can be used in GamePlayPane and LevelSelectPane
     * <p>
     * Hint: set the canvas height and width as a multiple of the rows and cols
     *
     * @param canvas The canvas to be rendered onto
     * @param map    The map holding the current state of the game
     */
    public static void render(@NotNull Canvas canvas, @NotNull Cell[][] map) {
        canvas.setWidth(map[0].length * LEVEL_EDITOR_TILE_SIZE);
        canvas.setHeight(map.length * LEVEL_EDITOR_TILE_SIZE);

        for (int r = 0; r < map.length; ++r) {
            for (int c = 0; c < map[r].length; ++c) {
                Image image;

                if (map[r][c] instanceof Tile) {
                    image = getTileImage((Tile) map[r][c]);
                } else {
                    image = wall;
                }

                canvas.getGraphicsContext2D().drawImage(image, c * LEVEL_EDITOR_TILE_SIZE, r * LEVEL_EDITOR_TILE_SIZE);
            }
        }
    }

    /**
     * Returns the image for a given tile.
     *
     * @param t Tile to display.
     * @return Image of the tile.
     */
    private static @NotNull Image getTileImage(@NotNull final Tile t) {
        Image image;
        if (t.getOccupant().orElse(null) instanceof Crate) {
            image = IMAGE_MAPPING.get(t.getClass()).tileWithCrate;
        } else if (t.getOccupant().orElse(null) instanceof Player) {
            image = IMAGE_MAPPING.get(t.getClass()).tileWithPlayer;
        } else if (!t.getOccupant().isPresent()) {
            image = IMAGE_MAPPING.get(t.getClass()).tile;
        } else {
            throw new IllegalArgumentException("No asset for unknown occupant type");
        }

        assert image != null;

        return image;
    }
}
