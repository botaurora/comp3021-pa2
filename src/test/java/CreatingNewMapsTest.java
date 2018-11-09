import com.sun.javafx.fxml.PropertyNotFoundException;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import viewmodel.LevelEditorCanvas;
import viewmodel.SceneManager;
import viewmodel.customNodes.NumberTextField;
import viewmodel.panes.LevelEditorPane;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TODO(Derppening): Ask if the tests are well sequenced.
 *
 * Test cases for all conditions as specified by Compulsory Demo Tasks, Creating New Maps section.
 */
public class CreatingNewMapsTest extends ApplicationTest {
    private Node listViewNode;
    private Node rowBoxNode;
    private Node colBoxNode;
    private Node newGridNode;
    private Node saveNode;
    private Node canvasNode;

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
        saveNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("Save"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        canvasNode = ((VBox) centerVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Canvas)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        setBoardSize(null, null);
    }

    @Override
    @Start
    public void start(Stage stage) {
        Platform.runLater(() -> SceneManager.getInstance().setStage(stage));
        Platform.runLater(() -> SceneManager.getInstance().showLevelEditorScene());

        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);
    }

    /**
     * @return Topmost modal stage, or null if there are none.
     */
    private Optional<Stage> getTopModalStage() {
        final List<Window> allWindows = new ArrayList<>(robotContext().getWindowFinder().listWindows());
        return Optional.ofNullable(
                (Stage) allWindows.stream()
                        .filter(it -> it instanceof Stage)
                        .filter(it -> ((Stage) it)
                                .getModality() == Modality.APPLICATION_MODAL)
                        .findFirst()
                        .orElse(null));
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

        clickOn(newGridNode);
    }

    /**
     * <p>Tests for Condition A:</p>
     *
     * <p>Select each of the 7 brushes in the list view, and ensure they each correctly draw the element on
     * the canvas.</p>
     */
    @Test
    void testSelectBrushAndDrawCanvas() {
        setBoardSize(5, 5);

        final LevelEditorCanvas canvas = (LevelEditorCanvas) canvasNode;

        @SuppressWarnings("unchecked") final ListView<LevelEditorCanvas.Brush> listView = (ListView<LevelEditorCanvas.Brush>) listViewNode;
        for (int i = 0; i < listView.getItems().size(); ++i) {
            final LevelEditorCanvas.Brush brush = listView.getItems().get(i);

            listView.getSelectionModel().clearAndSelect(i);
            clickOn(offset(canvasNode, -64.0, -64.0));

            Class<?> clazz = LevelEditorCanvas.class;
            try {
                final Field mapField = clazz.getDeclaredField("map");
                mapField.setAccessible(true);
                final LevelEditorCanvas.Brush[][] map = (LevelEditorCanvas.Brush[][]) mapField.get(canvas);

                assertEquals(brush, map[0][0]);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail(e.getMessage());
            }
        }
    }

    /**
     * <p>Tests for Condition B:</p>
     *
     * <p>Select Player on Destination brush, and click somewhere on the grid. Then, select Player on
     * Tile brush, and click elsewhere. Ensure that:</p>
     * <ul>
     * <li>The player is drawn in the new location</li>
     * <li>The player is removed from the old location</li>
     * <li>The old location now shows the destination tile dot</li>
     * </ul>
     */
    @Test
    void testPlayerReplacement() {
        setBoardSize(5, 5);

        final LevelEditorCanvas canvas = (LevelEditorCanvas) canvasNode;

        @SuppressWarnings("unchecked") final ListView<LevelEditorCanvas.Brush> listView = (ListView<LevelEditorCanvas.Brush>) listViewNode;

        listView.getSelectionModel().select(LevelEditorCanvas.Brush.PLAYER_ON_DEST);
        clickOn(offset(canvasNode, -64.0, -64.0));

        listView.getSelectionModel().select(LevelEditorCanvas.Brush.PLAYER_ON_TILE);
        clickOn(offset(canvasNode, 64.0, 64.0));

        Class<?> clazz = LevelEditorCanvas.class;
        try {
            final Field mapField = clazz.getDeclaredField("map");
            mapField.setAccessible(true);
            final LevelEditorCanvas.Brush[][] map = (LevelEditorCanvas.Brush[][]) mapField.get(canvas);

            assertEquals(LevelEditorCanvas.Brush.DEST, map[0][0]);
            assertEquals(LevelEditorCanvas.Brush.PLAYER_ON_TILE, map[4][4]);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail(e.getMessage());
        }
    }

    /**
     * <p>Tests for Condition C:</p>
     *
     * <p>Select Player on Tile brush, and click somewhere on the grid. Then, using the same brush, click
     * elsewhere. Ensure that:</p>
     * <ul>
     * <li>The player is drawn in the new location</li>
     * <li>The player is removed from the old location</li>
     * <li>The old location is now a normal tile</li>
     * </ul>
     */
    @Test
    void testMovePlayer() {
        setBoardSize(5, 5);

        final LevelEditorCanvas canvas = (LevelEditorCanvas) canvasNode;

        @SuppressWarnings("unchecked") final ListView<LevelEditorCanvas.Brush> listView = (ListView<LevelEditorCanvas.Brush>) listViewNode;

        listView.getSelectionModel().select(LevelEditorCanvas.Brush.PLAYER_ON_TILE);
        clickOn(offset(canvasNode, -64.0, -64.0));
        clickOn(offset(canvasNode, 64.0, 64.0));

        Class<?> clazz = LevelEditorCanvas.class;
        try {
            final Field mapField = clazz.getDeclaredField("map");
            mapField.setAccessible(true);
            final LevelEditorCanvas.Brush[][] map = (LevelEditorCanvas.Brush[][]) mapField.get(canvas);

            assertEquals(LevelEditorCanvas.Brush.TILE, map[0][0]);
            assertEquals(LevelEditorCanvas.Brush.PLAYER_ON_TILE, map[4][4]);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail(e.getMessage());
        }
    }

    /**
     * <p>Performs action as specified by Condition D.</p>
     *
     * <p>Select Player on Tile brush, and click the top left location.</p>
     */
    private void placePlayerOnTopLeft() {
        setBoardSize(5, 5);

        @SuppressWarnings("unchecked") final ListView<LevelEditorCanvas.Brush> listView = (ListView<LevelEditorCanvas.Brush>) listViewNode;

        listView.getSelectionModel().select(LevelEditorCanvas.Brush.PLAYER_ON_TILE);
        clickOn(offset(canvasNode, -64.0, -64.0));
    }

    /**
     * <p>Tests for Condition D+E:</p>
     *
     * <p>Select Player on Tile brush, and click the top left location.</p>
     *
     * <p>Without exiting the level editor, change map dimensions to 4 by 4, and click new grid. Ensure
     * that</p>
     * <ul>
     * <li>The map is resized appropriately</li>
     * <li>The map is reset</li>
     * </ul>
     */
    @Test
    void testResizeMap() {
        setBoardSize(5, 5);

        final LevelEditorCanvas canvas = (LevelEditorCanvas) canvasNode;

        // technically, i need to reset the map to the state of Condition D, so...
        placePlayerOnTopLeft();

        setBoardSize(4, 4);

        Class<?> clazz = LevelEditorCanvas.class;
        try {
            final Field mapField = clazz.getDeclaredField("map");
            mapField.setAccessible(true);
            final LevelEditorCanvas.Brush[][] map = (LevelEditorCanvas.Brush[][]) mapField.get(canvas);

            assertEquals(4, map.length);
            for (LevelEditorCanvas.Brush[] b : map) {
                assertEquals(4, b.length);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail(e.getMessage());
        }
    }

    /**
     * <p>Tests for Condition F:</p>
     *
     * <p>Select Crate on Tile brush, and click the top left location (where the player used to be). Now,
     * select Player on Tile brush, and place player on bottom right corner.</p>
     * <ul>
     * <li>Ensure that the crate does not disappear</li>
     * </ul>
     */
    @Test
    void testOccupantOnTileCoexistence() {
        setBoardSize(4, 4);

        final LevelEditorCanvas canvas = (LevelEditorCanvas) canvasNode;

        @SuppressWarnings("unchecked") final ListView<LevelEditorCanvas.Brush> listView = (ListView<LevelEditorCanvas.Brush>) listViewNode;

        listView.getSelectionModel().select(LevelEditorCanvas.Brush.CRATE_ON_TILE);
        clickOn(offset(canvasNode, -48.0, -48.0));

        listView.getSelectionModel().select(LevelEditorCanvas.Brush.PLAYER_ON_TILE);
        clickOn(offset(canvasNode, 48.0, 48.0));

        Class<?> clazz = LevelEditorCanvas.class;
        try {
            final Field mapField = clazz.getDeclaredField("map");
            mapField.setAccessible(true);
            final LevelEditorCanvas.Brush[][] map = (LevelEditorCanvas.Brush[][]) mapField.get(canvas);

            assertEquals(LevelEditorCanvas.Brush.CRATE_ON_TILE, map[0][0]);
            assertEquals(LevelEditorCanvas.Brush.PLAYER_ON_TILE, map[3][3]);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail(e.getMessage());
        }
    }

    /**
     * TODO(Derppening): Refactor into even smaller test cases
     * <p>Tests for Condition G:</p>
     *
     * <p>Have the student demonstrate that the following map conditions cannot be violated when trying to
     * save the map:</p>
     * <ul>
     * <li>Map dimensions smaller than 3x3</li>
     * <li>Map without a player</li>
     * <li>Imbalanced number of crates and destinations. Ensure that Crate on Destination, Player on
     * Destination, Crate on Tile, and Destination are all used</li>
     * <li>Having fewer than 1 crate/destination pair</li>
     * </ul>
     */
    @Test
    void testSaveMapWithUnsatisfiedPreconditions() {
        setBoardSize(4, 4);

        @SuppressWarnings("unchecked") final ListView<LevelEditorCanvas.Brush> listView = (ListView<LevelEditorCanvas.Brush>) listViewNode;
        Stage dialog = null;

        setBoardSize(null, null);
        clickOn(saveNode);

        dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
        assertNotNull(dialog);

        // TODO(Derppening): Match behavior with sample JAR

        type(KeyCode.ENTER);

        listView.getSelectionModel().select(LevelEditorCanvas.Brush.CRATE_ON_DEST);
        clickOn(offset(canvasNode, -48.0, -48.0));
        listView.getSelectionModel().select(LevelEditorCanvas.Brush.PLAYER_ON_DEST);
        clickOn(offset(canvasNode, -16.0, -16.0));
        listView.getSelectionModel().select(LevelEditorCanvas.Brush.CRATE_ON_TILE);
        clickOn(offset(canvasNode, 16.0, 16.0));
        listView.getSelectionModel().select(LevelEditorCanvas.Brush.DEST);
        clickOn(offset(canvasNode, 48.0, 48.0));

        clickOn(saveNode);

        dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
        assertNotNull(dialog);

        // TODO(Derppening): Match behavior with sample JAR

        type(KeyCode.ENTER);

        listView.getSelectionModel().select(LevelEditorCanvas.Brush.DEST);
        clickOn(offset(canvasNode, -16.0, -16.0));

        clickOn(saveNode);

        dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
        assertNotNull(dialog);

        // TODO(Derppening): Match behavior with sample JAR

        type(KeyCode.ENTER);

        setBoardSize(3, 3);

        clickOn(saveNode);

        dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
        assertNotNull(dialog);

        // TODO(Derppening): Match behavior with sample JAR

        type(KeyCode.ENTER);
    }
}
