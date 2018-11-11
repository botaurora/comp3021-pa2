import com.sun.javafx.fxml.PropertyNotFoundException;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.LevelManager;
import model.Map.Cell;
import model.Map.Occupant.Crate;
import model.Map.Occupant.Player;
import model.Map.Occupiable.Occupiable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import viewmodel.SceneManager;
import viewmodel.customNodes.GameplayInfoPane;
import viewmodel.panes.GameplayPane;
import viewmodel.panes.LevelSelectPane;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class PlayingTheGameTest extends ApplicationTest {
    private static final KeyCode[] DEADLOCK_MOVES = {
            KeyCode.A,
            KeyCode.D,
            KeyCode.S,
            KeyCode.A,
            KeyCode.D,
            KeyCode.S,
            KeyCode.S,
            KeyCode.S,
            KeyCode.D,
            KeyCode.S,
            KeyCode.A
    };

    private static final KeyCode[] WIN_MOVES = {
            KeyCode.S,
            KeyCode.S,
            KeyCode.S,
            KeyCode.D,
            KeyCode.S,
            KeyCode.S,
            KeyCode.A,
            KeyCode.A,
            KeyCode.W,
            KeyCode.D,
            KeyCode.S,
            KeyCode.D,
            KeyCode.W,
            KeyCode.A,
            KeyCode.W,
            KeyCode.D,
            KeyCode.D,
            KeyCode.D,
            KeyCode.S,
            KeyCode.D,
            KeyCode.W,
            KeyCode.W,
            // first crate in
            KeyCode.S,
            KeyCode.A,
            KeyCode.A,
            KeyCode.A,
            KeyCode.A,
            KeyCode.W,
            KeyCode.W,
            KeyCode.W,
            // player position restored
            KeyCode.W,
            KeyCode.A,
            KeyCode.A,
            KeyCode.S,
            KeyCode.D,
            KeyCode.W,
            KeyCode.D,
            KeyCode.S,
            // second crate replaces first crate original position
            KeyCode.S,
            KeyCode.S,
            KeyCode.S,
            KeyCode.D,
            KeyCode.S,
            KeyCode.S,
            KeyCode.A,
            KeyCode.A,
            KeyCode.W,
            KeyCode.D,
            KeyCode.S,
            KeyCode.D,
            KeyCode.W,
            KeyCode.A,
            KeyCode.W,
            KeyCode.D,
            KeyCode.D,
            KeyCode.D,
            KeyCode.S,
            KeyCode.D,
            KeyCode.W,
            // second crate in
            KeyCode.A,
            KeyCode.A,
            KeyCode.A,
            KeyCode.A,
            KeyCode.W,
            KeyCode.W,
            KeyCode.W,
            // player position restored
            KeyCode.A,
            KeyCode.A,
            KeyCode.S,
            KeyCode.D,
            KeyCode.W,
            KeyCode.D,
            // third crate replaces first crate original position
            KeyCode.S,
            KeyCode.S,
            KeyCode.S,
            KeyCode.D,
            KeyCode.S,
            KeyCode.S,
            KeyCode.A,
            KeyCode.A,
            KeyCode.W,
            KeyCode.D,
            KeyCode.S,
            KeyCode.D,
            KeyCode.W,
            KeyCode.A,
            KeyCode.W,
            KeyCode.D,
            KeyCode.D,
            KeyCode.D,
            KeyCode.S,
            KeyCode.D,
            KeyCode.W,
            KeyCode.W
    };

    private LevelManager levelManager;

    @BeforeEach
    void setupEach() {
        levelManager = LevelManager.getInstance();

        try {
            Path testPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("assets/maps/01-easy.txt").toURI());
            Path actualPath = testPath.getParent().toAbsolutePath();

            LevelManager.getInstance().setMapDirectory(actualPath.toString());
            Platform.runLater(() -> LevelManager.getInstance().loadLevelNamesFromDisk());
        } catch (URISyntaxException e) {
            fail();
        }
    }

    @AfterEach
    void cleanupEach() {
        System.setOut(System.out);
        System.setErr(System.err);
    }

    @Override
    @Start
    public void start(Stage stage) {
        Platform.runLater(() -> SceneManager.getInstance().setStage(stage));
        Platform.runLater(() -> SceneManager.getInstance().showLevelSelectMenuScene());

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
     * Performs the keystrokes necessary to load the default map directory.
     * <p>
     * Asserts failure if the current pane is not {@link LevelSelectPane} or the number of entries is not 14 after
     * loading.
     */
    void loadDefaultMapDirectory() {
        try {
            Path testPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("assets/maps/01-easy.txt").toURI());
            Path actualPath = testPath.getParent().getParent().toAbsolutePath();

            LevelManager.getInstance().setMapDirectory(actualPath.toString());
            Platform.runLater(() -> LevelManager.getInstance().loadLevelNamesFromDisk());
        } catch (URISyntaxException e) {
            fail();
        }

        Parent currentRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(currentRoot instanceof LevelSelectPane);

        Node leftVBoxNode = currentRoot.getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof VBox && ((VBox) it).getChildrenUnmodifiable().size() == 4)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        Node chooseDirNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("Choose map directory"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        Node levelsListViewNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof ListView)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        clickOn(chooseDirNode);
        type(KeyCode.BACK_SPACE);

        // TODO(Derppening): Find Platform-independent way to test this
        type(
                KeyCode.B,
                KeyCode.RIGHT,
                KeyCode.R,
                KeyCode.E,
                KeyCode.S,
                KeyCode.RIGHT,
                KeyCode.RIGHT,
                KeyCode.RIGHT,
                KeyCode.M,
                KeyCode.RIGHT,
                KeyCode.ENTER
        );

        assertEquals(14, ((ListView<?>) levelsListViewNode).getItems().size());
    }

    /**
     * <p>Tests for Condition 1:</p>
     *
     * <p>Click `Choose map directory`, but cancel without selecting a directory, and ensure program has no errors</p>
     */
    @Test
    void testCancelDirectorySelection() {
        Parent currentRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(currentRoot instanceof LevelSelectPane);

        Node leftVBoxNode = currentRoot.getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof VBox && ((VBox) it).getChildrenUnmodifiable().size() == 4)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        Node chooseDirNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("Choose map directory"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        final ByteArrayOutputStream stderrContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stderrContent));

        clickOn(chooseDirNode);
        type(KeyCode.ESCAPE);

        assertTrue(stderrContent.toString().isEmpty());
    }

    /**
     * <p>Tests for Condition 2:</p>
     *
     * <p>Click `Choose map directory`, and choose the map directory. Ensure the maps are loaded into the list view</p>
     */
    @Test
    void testSelectMapDirectory() {
        loadDefaultMapDirectory();
    }

    @Test
    void testMapDisplay() {
        // TODO(Derppening): Think of a way to do it
    }

    @Test
    void testBasicGameParameters() {
        Parent levelSelectRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(levelSelectRoot instanceof LevelSelectPane);

        Node leftVBoxNode = levelSelectRoot.getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof VBox && ((VBox) it).getChildrenUnmodifiable().size() == 4)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        Node levelsListViewNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof ListView)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        Node playNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("Play"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        @SuppressWarnings("unchecked") final ListView<String> listView = (ListView<String>) levelsListViewNode;
        listView.getSelectionModel().select("02-easy.txt");

        clickOn(playNode);

        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

        Parent gameplayRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(gameplayRoot instanceof GameplayPane);

        Node bottomBarNode = ((BorderPane) gameplayRoot).getBottom();

        Node infoNode = ((HBox) bottomBarNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof GameplayInfoPane)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        GameplayInfoPane infoPane = ((GameplayInfoPane) infoNode);

        Class<?> clazz = GameplayInfoPane.class;
        try {
            Field levelNameField = clazz.getDeclaredField("levelNameLabel");
            Field timerField = clazz.getDeclaredField("timerLabel");
            Field numMovesField = clazz.getDeclaredField("numMovesLabel");
            Field numRestartsField = clazz.getDeclaredField("numRestartsLabel");

            levelNameField.setAccessible(true);
            timerField.setAccessible(true);
            numMovesField.setAccessible(true);
            numRestartsField.setAccessible(true);

            String timerFieldValue = ((Label) timerField.get(infoPane)).getText();

            assertEquals("Level: 02-easy.txt", ((Label) levelNameField.get(infoPane)).getText());
            assertTrue(((Label) timerField.get(infoPane)).getText().startsWith("Time: 00:0"));
            assertEquals("Moves: 0", ((Label) numMovesField.get(infoPane)).getText());
            assertEquals("Restarts: 0", ((Label) numRestartsField.get(infoPane)).getText());

            WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

            assertNotEquals(timerFieldValue, ((Label) timerField.get(infoPane)).getText());

            Cell[][] map = levelManager.getGameLevel().getMap().getCells();

            // construct a dual-crate scenario
            type(KeyCode.S, 3);
            type(KeyCode.W, 4);
            type(KeyCode.A, 2);
            type(
                    KeyCode.S,
                    KeyCode.D,
                    KeyCode.W,
                    KeyCode.D
            );
            type(KeyCode.S, 3);
            assertTrue(((Occupiable) map[4][3]).getOccupant().orElse(null) instanceof Player);
            assertTrue(((Occupiable) map[5][3]).getOccupant().orElse(null) instanceof Crate);
            assertTrue(((Occupiable) map[6][3]).getOccupant().orElse(null) instanceof Crate);
            assertEquals("Moves: 16", ((Label) numMovesField.get(infoPane)).getText());

            // test push dual crates
            type(KeyCode.S);
            assertTrue(((Occupiable) map[5][3]).getOccupant().orElse(null) instanceof Crate);
            assertTrue(((Occupiable) map[6][3]).getOccupant().orElse(null) instanceof Crate);
            assertEquals("Moves: 16", ((Label) numMovesField.get(infoPane)).getText());

            // construct a wall-crate scenario
            type(KeyCode.W, KeyCode.A);
            assertTrue(((Occupiable) map[3][2]).getOccupant().orElse(null) instanceof Player);
            assertTrue(((Occupiable) map[3][1]).getOccupant().orElse(null) instanceof Crate);
            assertEquals("Moves: 18", ((Label) numMovesField.get(infoPane)).getText());

            // test push crate into wall
            type(KeyCode.A);
            assertTrue(((Occupiable) map[3][1]).getOccupant().orElse(null) instanceof Crate);
            assertEquals("Moves: 18", ((Label) numMovesField.get(infoPane)).getText());

            assertEquals("Restarts: 0", ((Label) numRestartsField.get(infoPane)).getText());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail();
        }
    }

    @Test
    void testGameRestart() {
        Parent levelSelectRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(levelSelectRoot instanceof LevelSelectPane);

        Node leftVBoxNode = levelSelectRoot.getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof VBox && ((VBox) it).getChildrenUnmodifiable().size() == 4)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        Node levelsListViewNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof ListView)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        Node playNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("Play"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        @SuppressWarnings("unchecked") final ListView<String> listView = (ListView<String>) levelsListViewNode;
        listView.getSelectionModel().select("02-easy.txt");

        clickOn(playNode);

        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

        Parent gameplayRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(gameplayRoot instanceof GameplayPane);

        Node bottomBarNode = ((BorderPane) gameplayRoot).getBottom();

        Node infoNode = ((HBox) bottomBarNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof GameplayInfoPane)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        Node restartNode = ((HBox) bottomBarNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("Restart"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        GameplayInfoPane infoPane = ((GameplayInfoPane) infoNode);

        Class<?> clazz = GameplayInfoPane.class;
        try {
            Field levelNameField = clazz.getDeclaredField("levelNameLabel");
            Field timerField = clazz.getDeclaredField("timerLabel");
            Field numMovesField = clazz.getDeclaredField("numMovesLabel");
            Field numRestartsField = clazz.getDeclaredField("numRestartsLabel");

            levelNameField.setAccessible(true);
            timerField.setAccessible(true);
            numMovesField.setAccessible(true);
            numRestartsField.setAccessible(true);

            type(KeyCode.S, 3);

            WaitForAsyncUtils.sleep(2, TimeUnit.SECONDS);

            String timerFieldValue = ((Label) timerField.get(infoPane)).getText();

            clickOn(restartNode);

            assertEquals("Level: 02-easy.txt", ((Label) levelNameField.get(infoPane)).getText());
            assertTrue(((Label) timerField.get(infoPane)).getText().startsWith("Time: 00:0"));
            assertNotEquals(timerFieldValue, ((Label) timerField.get(infoPane)).getText());
            assertEquals("Moves: 0", ((Label) numMovesField.get(infoPane)).getText());
            assertEquals("Restarts: 1", ((Label) numRestartsField.get(infoPane)).getText());

            WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

            assertNotEquals(timerFieldValue, ((Label) timerField.get(infoPane)).getText());

            Cell[][] map = levelManager.getGameLevel().getMap().getCells();

            assertTrue(((Occupiable) map[2][2]).getOccupant().orElse(null) instanceof Crate);
            assertTrue(((Occupiable) map[2][3]).getOccupant().orElse(null) instanceof Player);
            assertTrue(((Occupiable) map[3][2]).getOccupant().orElse(null) instanceof Crate);
            assertTrue(((Occupiable) map[3][3]).getOccupant().orElse(null) instanceof Crate);

            clickOn(restartNode);

            assertEquals("Restarts: 2", ((Label) numRestartsField.get(infoPane)).getText());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail();
        }
    }

    @Test
    void testGameDeadlock() {
        Parent levelSelectRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(levelSelectRoot instanceof LevelSelectPane);

        Node leftVBoxNode = levelSelectRoot.getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof VBox && ((VBox) it).getChildrenUnmodifiable().size() == 4)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        Node levelsListViewNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof ListView)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        Node playNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("Play"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        @SuppressWarnings("unchecked") final ListView<String> listView = (ListView<String>) levelsListViewNode;
        listView.getSelectionModel().select("02-easy.txt");

        clickOn(playNode);

        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

        Parent gameplayRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(gameplayRoot instanceof GameplayPane);

        Node bottomBarNode = ((BorderPane) gameplayRoot).getBottom();

        Node infoNode = ((HBox) bottomBarNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof GameplayInfoPane)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        GameplayInfoPane infoPane = ((GameplayInfoPane) infoNode);

        Class<?> clazz = GameplayInfoPane.class;
        try {
            Field levelNameField = clazz.getDeclaredField("levelNameLabel");
            Field timerField = clazz.getDeclaredField("timerLabel");
            Field numMovesField = clazz.getDeclaredField("numMovesLabel");
            Field numRestartsField = clazz.getDeclaredField("numRestartsLabel");

            levelNameField.setAccessible(true);
            timerField.setAccessible(true);
            numMovesField.setAccessible(true);
            numRestartsField.setAccessible(true);

            type(DEADLOCK_MOVES);

            Stage dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
            assertNotNull(dialog);

            type(KeyCode.SPACE);

            assertEquals("Level: 02-easy.txt", ((Label) levelNameField.get(infoPane)).getText());
            assertTrue(((Label) timerField.get(infoPane)).getText().startsWith("Time: 00:0"));
            assertEquals("Moves: 0", ((Label) numMovesField.get(infoPane)).getText());
            assertEquals("Restarts: 1", ((Label) numRestartsField.get(infoPane)).getText());

            type(DEADLOCK_MOVES);

            dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
            assertNotNull(dialog);

            type(KeyCode.RIGHT, KeyCode.SPACE);

            assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof LevelSelectPane);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail();
        }
    }

    @Test
    void testGameWinNextLevel() {
        Parent levelSelectRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(levelSelectRoot instanceof LevelSelectPane);

        Node leftVBoxNode = levelSelectRoot.getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof VBox && ((VBox) it).getChildrenUnmodifiable().size() == 4)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        Node levelsListViewNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof ListView)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        Node playNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("Play"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        @SuppressWarnings("unchecked") final ListView<String> listView = (ListView<String>) levelsListViewNode;
        listView.getSelectionModel().select("02-easy.txt");

        clickOn(playNode);

        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

        Parent gameplayRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(gameplayRoot instanceof GameplayPane);

        Node bottomBarNode = ((BorderPane) gameplayRoot).getBottom();

        Node infoNode = ((HBox) bottomBarNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof GameplayInfoPane)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        GameplayInfoPane infoPane = ((GameplayInfoPane) infoNode);

        type(WIN_MOVES);

        Stage dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
        assertNotNull(dialog);

        type(KeyCode.SPACE);

        Class<?> clazz = GameplayInfoPane.class;
        try {
            Field levelNameField = clazz.getDeclaredField("levelNameLabel");
            Field timerField = clazz.getDeclaredField("timerLabel");
            Field numMovesField = clazz.getDeclaredField("numMovesLabel");
            Field numRestartsField = clazz.getDeclaredField("numRestartsLabel");

            levelNameField.setAccessible(true);
            timerField.setAccessible(true);
            numMovesField.setAccessible(true);
            numRestartsField.setAccessible(true);

            assertEquals("Level: 03-easy.txt", ((Label) levelNameField.get(infoPane)).getText());
            assertTrue(((Label) timerField.get(infoPane)).getText().startsWith("Time: 00:0"));
            assertEquals("Moves: 0", ((Label) numMovesField.get(infoPane)).getText());
            assertEquals("Restarts: 0", ((Label) numRestartsField.get(infoPane)).getText());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail();
        }
    }

    @Test
    void testGameWinQuit() {
        Parent levelSelectRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(levelSelectRoot instanceof LevelSelectPane);

        Node leftVBoxNode = levelSelectRoot.getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof VBox && ((VBox) it).getChildrenUnmodifiable().size() == 4)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        Node levelsListViewNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof ListView)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        Node playNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("Play"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        @SuppressWarnings("unchecked") final ListView<String> listView = (ListView<String>) levelsListViewNode;
        listView.getSelectionModel().select("02-easy.txt");

        clickOn(playNode);

        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

        Parent gameplayRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(gameplayRoot instanceof GameplayPane);

        type(WIN_MOVES);

        Stage dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
        assertNotNull(dialog);

        // TODO(Derppening): Ask TA about discrepancy for Marking Scheme vs JAR
        type(KeyCode.RIGHT, KeyCode.SPACE);
        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof LevelSelectPane);
    }
}
