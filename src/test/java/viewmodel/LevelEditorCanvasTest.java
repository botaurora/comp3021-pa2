package viewmodel;

import com.sun.javafx.fxml.PropertyNotFoundException;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import viewmodel.customNodes.NumberTextField;
import viewmodel.panes.LevelEditorPane;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Miscellaneous tests for {@link LevelEditorCanvas} not part of the official demo.
 */
public class LevelEditorCanvasTest extends ApplicationTest {
    private static final Class<?> LEVEL_EDITOR_CANVAS_CLAZZ = LevelEditorCanvas.class;

    private Node listViewNode;
    private Node rowBoxNode;
    private Node colBoxNode;
    private Node newGridNode;
    private Node canvasNode;

    /**
     * Assigns the member fields to the appropriate nodes.
     */
    @BeforeEach
    void setupEach() {
        Parent currentRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(currentRoot instanceof LevelEditorPane);

        Node leftVBoxNode = currentRoot.getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof VBox && ((VBox) it).getChildren().size() == 6)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        Node centerVBoxNode = currentRoot.getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof VBox && ((VBox) it).getChildren().size() == 1)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        listViewNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof ListView)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        rowBoxNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof BorderPane && ((Label) ((BorderPane) it).getLeft()).getText().equals("Rows"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        colBoxNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof BorderPane && ((Label) ((BorderPane) it).getLeft()).getText().equals("Columns"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        newGridNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("New Grid"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        canvasNode = ((VBox) centerVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Canvas)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        setBoardSize(null, null);
    }

    @AfterEach
    void cleanupEach() {
        try {
            Files.walk(Paths.get(""), 1)
                    .filter(file -> file.toString().endsWith(".txt"))
                    .forEach(f -> f.toFile().delete());
        } catch (IOException e) {
            fail("Unable to delete files");
        }
    }

    /**
     * Displays the Level Editor scene.
     *
     * @param stage Primary Stage.
     */
    @Override
    @Start
    public void start(Stage stage) {
        Platform.runLater(() -> SceneManager.getInstance().setStage(stage));
        Platform.runLater(() -> SceneManager.getInstance().showLevelEditorScene());

        waitForFxEvents();
    }

    /**
     * Helper function for setting the board size and resetting the board.
     *
     * @param rows New number of rows, or {@code null} if using the previous value.
     * @param cols New number of columns, or {@code null} if using the previous value.
     */
    private void setBoardSize(@Nullable Integer rows, @Nullable Integer cols) {
        if (rows != null && cols != null) {
            final NumberTextField rowField = ((NumberTextField) ((BorderPane) rowBoxNode).getRight());
            final NumberTextField colField = ((NumberTextField) ((BorderPane) colBoxNode).getRight());

            rowField.clear();
            colField.clear();
            rowField.replaceSelection(rows.toString());
            colField.replaceSelection(cols.toString());
        }
        waitForFxEvents();

        clickOn(newGridNode);
        waitForFxEvents();
    }

    /**
     * Tests toggling a player on a tile.
     */
    @Test
    void testTogglePlayerOnTile() {
        final LevelEditorCanvas canvas = (LevelEditorCanvas) canvasNode;

        try {
            Method m = LEVEL_EDITOR_CANVAS_CLAZZ.getDeclaredMethod("togglePlayerOnTile", LevelEditorCanvas.Brush.class);
            m.setAccessible(true);

            for (LevelEditorCanvas.Brush brush : LevelEditorCanvas.Brush.values()) {
                LevelEditorCanvas.Brush toggledBrush = ((LevelEditorCanvas.Brush) m.invoke(canvas, brush));
                switch (brush) {
                    case DEST:
                        assertEquals(LevelEditorCanvas.Brush.PLAYER_ON_DEST, toggledBrush);
                        break;
                    case PLAYER_ON_DEST:
                        assertEquals(LevelEditorCanvas.Brush.DEST, toggledBrush);
                        break;
                    case TILE:
                        assertEquals(LevelEditorCanvas.Brush.PLAYER_ON_TILE, toggledBrush);
                        break;
                    case PLAYER_ON_TILE:
                        assertEquals(LevelEditorCanvas.Brush.TILE, toggledBrush);
                        break;
                    default:
                        assertEquals(brush, toggledBrush);
                        break;
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail();
        }
    }

    /**
     * Tests exporting a canvas into a file.
     */
    @Test
    void testExportToFile() {
        setBoardSize(3, 3);

        final LevelEditorCanvas canvas = (LevelEditorCanvas) canvasNode;

        @SuppressWarnings("unchecked") final ListView<LevelEditorCanvas.Brush> listView = (ListView<LevelEditorCanvas.Brush>) listViewNode;

        listView.getSelectionModel().select(LevelEditorCanvas.Brush.WALL);
        waitForFxEvents();

        clickOn(offset(canvasNode, -32.0, -32.0));
        waitForFxEvents();

        // WALL now on tile (0, 0)

        listView.getSelectionModel().select(LevelEditorCanvas.Brush.CRATE_ON_DEST);
        waitForFxEvents();

        clickOn(offset(canvasNode, 0.0, 0.0));
        waitForFxEvents();

        // CRATE_ON_DEST now on (1, 1)

        listView.getSelectionModel().select(LevelEditorCanvas.Brush.PLAYER_ON_TILE);
        waitForFxEvents();

        clickOn(offset(canvasNode, 32.0, 32.0));
        waitForFxEvents();

        // PLAYER_ON_TILE now on (2, 2)

        try {
            Method m = LEVEL_EDITOR_CANVAS_CLAZZ.getDeclaredMethod("exportToFile", File.class);
            m.setAccessible(true);

            m.invoke(canvas, new File("output.txt"));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail();
        }

        File f = new File("output.txt");
        assertTrue(f.exists());

        try {
            List<String> lines = Files.readAllLines(f.toPath());

            assertEquals(5, lines.size());
            assertEquals("3", lines.get(0));
            assertEquals("3", lines.get(1));
            assertEquals("#..", lines.get(2));
            assertEquals(".$.", lines.get(3));
            assertEquals("..@", lines.get(4));
        } catch (IOException e) {
            fail();
        }
    }
}
