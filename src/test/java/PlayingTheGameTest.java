import com.sun.javafx.fxml.PropertyNotFoundException;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import viewmodel.SceneManager;
import viewmodel.customNodes.GameplayInfoPane;
import viewmodel.panes.GameplayPane;
import viewmodel.panes.LevelSelectPane;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Test cases for all conditions as specified by Compulsory Demo Tasks, Playing The Game section.
 */
public class PlayingTheGameTest extends ApplicationTest {
    /**
     * Keycode sequence to deadlock 02-easy.txt with lookahead deadlock detection (Bonus Task 3).
     */
    private static final KeyCode[] DEADLOCK_MOVES_1 = {
            KeyCode.A,
            KeyCode.D,
            KeyCode.S,
            KeyCode.A
    };

    /**
     * Keycode sequence to deadlock 02-easy.txt without lookahead deadlock detection.
     * <p>
     * Use this in combination with {@code DEADLOCK_MOVES_1}.
     */
    private static final KeyCode[] DEADLOCK_MOVES_2 = {
            KeyCode.D,
            KeyCode.S,
            KeyCode.S,
            KeyCode.S,
            KeyCode.D,
            KeyCode.S,
            KeyCode.A
    };

    /**
     * Keycode sequence to win 02-easy.txt.
     */
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

    private static final Class<?> LEVEL_SELECT_CLAZZ = LevelSelectPane.class;
    private static final Class<?> GAMEPLAY_CLAZZ = GameplayPane.class;
    private static final Class<?> GAMEPLAY_INFO_CLAZZ = GameplayInfoPane.class;

    private LevelManager levelManager;

    @BeforeEach
    void setupEach() {
        levelManager = LevelManager.getInstance();

        try {
            Path testPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("assets/maps/01-easy.txt").toURI());
            Path actualPath = testPath.getParent().toAbsolutePath();

            Method m = LEVEL_SELECT_CLAZZ.getDeclaredMethod("commitMapDirectoryChange", File.class);
            m.setAccessible(true);

            Parent currentRoot = SceneManager.getInstance().getStage().getScene().getRoot();
            assertTrue(currentRoot instanceof LevelSelectPane);

            Platform.runLater(() -> {
                try {
                    m.invoke(currentRoot, actualPath.toFile());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    fail();
                }
            });

            waitForFxEvents();
        } catch (URISyntaxException | NoSuchMethodException e) {
            fail();
        }
    }

    /**
     * Resets {@code System.out} and {@code System.err} to the original location.
     */
    @AfterEach
    void cleanupEach() {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
    }

    @Override
    @Start
    public void start(Stage stage) {
        Platform.runLater(() -> SceneManager.getInstance().setStage(stage));
        Platform.runLater(() -> SceneManager.getInstance().showLevelSelectMenuScene());

        waitForFxEvents();
    }

    /**
     * @return Topmost modal stage, or null if there are none.
     */
    private Optional<Stage> getTopModalStage() {
        final List<Window> allWindows = new ArrayList<>(robotContext().getWindowFinder().listWindows());
        return Optional.ofNullable(
                (Stage) allWindows.stream()
                        .filter(it -> it instanceof Stage)
                        .filter(it -> ((Stage) it).getModality() == Modality.APPLICATION_MODAL)
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
        Parent currentRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(currentRoot instanceof LevelSelectPane);

        Node leftVBoxNode = currentRoot.getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof VBox && ((VBox) it).getChildrenUnmodifiable().size() == 4)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        Node levelsListViewNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof ListView)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        try {
            Path testPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("assets/maps/01-easy.txt").toURI());
            Path actualPath = testPath.getParent().getParent().toAbsolutePath();

            Method m = LEVEL_SELECT_CLAZZ.getDeclaredMethod("commitMapDirectoryChange", File.class);
            m.setAccessible(true);

            Platform.runLater(() -> {
                try {
                    m.invoke(currentRoot, actualPath.toFile());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    fail();
                }
            });
            waitForFxEvents();

            assertEquals(0, ((ListView<?>) levelsListViewNode).getItems().size());
        } catch (URISyntaxException | NoSuchMethodException e) {
            fail();
        }

        try {
            Path testPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("assets/maps/01-easy.txt").toURI());
            Path actualPath = testPath.getParent().toAbsolutePath();

            Method m = LEVEL_SELECT_CLAZZ.getDeclaredMethod("commitMapDirectoryChange", File.class);
            m.setAccessible(true);

            Platform.runLater(() -> {
                try {
                    m.invoke(currentRoot, actualPath.toFile());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    fail();
                }
            });
            waitForFxEvents();

            assertEquals(15, ((ListView<?>) levelsListViewNode).getItems().size());
        } catch (URISyntaxException | NoSuchMethodException e) {
            fail();
        }
    }

    /**
     * <p>Tests for Condition 1:</p>
     *
     * <p>Click `Choose map directory`, but cancel without selecting a directory, and ensure program has
     * no errors</p>
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
        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

        type(KeyCode.ESCAPE);
        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

        assertTrue(stderrContent.toString().isEmpty());
    }

    /**
     * <p>Tests for Condition 2:</p>
     *
     * <p>Click `Choose map directory`, and choose the map directory. Ensure the maps are loaded into the
     * list view</p>
     */
    @Test
    void testSelectMapDirectory() {
        loadDefaultMapDirectory();
    }

    /**
     * <p>Tests for Condition 4.1:</p>
     *
     * <p>Move around a few times, and ensure the following:</p>
     * <ul>
     * <li>Level name is correct</li>
     * <li>Timer is working correctly</li>
     * <li>Moves counter is working correctly</li>
     * <li>The map elements are updated correctly in accordance with user input</li>
     * <li>The move sound is played (Not tested here)</li>
     * <li>You cannot push crates into walls, or two crates at once</li>
     * </ul>
     */
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
        waitForFxEvents();

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

        try {
            Field levelNameField = GAMEPLAY_INFO_CLAZZ.getDeclaredField("levelNameLabel");
            Field timerField = GAMEPLAY_INFO_CLAZZ.getDeclaredField("timerLabel");
            Field numMovesField = GAMEPLAY_INFO_CLAZZ.getDeclaredField("numMovesLabel");
            Field numRestartsField = GAMEPLAY_INFO_CLAZZ.getDeclaredField("numRestartsLabel");

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
            type(KeyCode.S, 4);
            type(KeyCode.W, 5);
            type(KeyCode.A, 2);
            type(
                    KeyCode.S,
                    KeyCode.D,
                    KeyCode.W,
                    KeyCode.D
            );
            type(KeyCode.S, 4);
            waitForFxEvents();

            assertTrue(((Occupiable) map[5][3]).getOccupant().orElse(null) instanceof Player);
            assertTrue(((Occupiable) map[6][3]).getOccupant().orElse(null) instanceof Crate);
            assertTrue(((Occupiable) map[7][3]).getOccupant().orElse(null) instanceof Crate);
            assertEquals("Moves: 19", ((Label) numMovesField.get(infoPane)).getText());

            // test push dual crates
            type(KeyCode.S);
            waitForFxEvents();

            assertTrue(((Occupiable) map[6][3]).getOccupant().orElse(null) instanceof Crate);
            assertTrue(((Occupiable) map[7][3]).getOccupant().orElse(null) instanceof Crate);
            assertEquals("Moves: 19", ((Label) numMovesField.get(infoPane)).getText());

            // restart the map
            clickOn(restartNode);
            waitForFxEvents();

            map = levelManager.getGameLevel().getMap().getCells();

            assertEquals("Level: 02-easy.txt", ((Label) levelNameField.get(infoPane)).getText());
            assertTrue(((Label) timerField.get(infoPane)).getText().startsWith("Time: 00:0"));
            assertEquals("Moves: 0", ((Label) numMovesField.get(infoPane)).getText());
            assertEquals("Restarts: 1", ((Label) numRestartsField.get(infoPane)).getText());

            // construct a wall-crate scenario
            type(KeyCode.A);
            waitForFxEvents();

            assertTrue(((Occupiable) map[2][2]).getOccupant().orElse(null) instanceof Player, "Got " + ((Occupiable) map[2][2]).getOccupant().orElse(null).toString());
            assertTrue(((Occupiable) map[2][1]).getOccupant().orElse(null) instanceof Crate);
            assertEquals("Moves: 1", ((Label) numMovesField.get(infoPane)).getText());

            // test push crate into wall
            type(KeyCode.A);
            waitForFxEvents();

            assertTrue(((Occupiable) map[2][1]).getOccupant().orElse(null) instanceof Crate);
            assertEquals("Moves: 1", ((Label) numMovesField.get(infoPane)).getText());

            assertEquals("Restarts: 1", ((Label) numRestartsField.get(infoPane)).getText());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail();
        }
    }

    /**
     * <p>Tests for Condition 4.2:</p>
     *
     * <p>Restart the game, and ensure:</p>
     * <ul>
     * <li>The map has been reset</li>
     * <li>The timer has been reset and is functioning normally</li>
     * <li>The moves counter has been reset</li>
     * <li>The number of restarts has been incremented</li>
     * </ul>
     */
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
        waitForFxEvents();

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

        try {
            Field levelNameField = GAMEPLAY_INFO_CLAZZ.getDeclaredField("levelNameLabel");
            Field timerField = GAMEPLAY_INFO_CLAZZ.getDeclaredField("timerLabel");
            Field numMovesField = GAMEPLAY_INFO_CLAZZ.getDeclaredField("numMovesLabel");
            Field numRestartsField = GAMEPLAY_INFO_CLAZZ.getDeclaredField("numRestartsLabel");

            levelNameField.setAccessible(true);
            timerField.setAccessible(true);
            numMovesField.setAccessible(true);
            numRestartsField.setAccessible(true);

            type(KeyCode.S, 3);

            WaitForAsyncUtils.sleep(2, TimeUnit.SECONDS);

            String timerFieldValue = ((Label) timerField.get(infoPane)).getText();

            clickOn(restartNode);
            waitForFxEvents();

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
            waitForFxEvents();

            assertEquals("Restarts: 2", ((Label) numRestartsField.get(infoPane)).getText());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail();
        }
    }

    /**
     * <p>Tests for Condition 4.3:</p>
     *
     * <p>Deadlock the level, and ensure:</p>
     * <ul>
     * <li>The deadlock popup shows</li>
     * <li>The deadlock sound is played (Not tested here)</li>
     * <li>The restart option works as described above</li>
     * </ul>
     */
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
        waitForFxEvents();

        Parent gameplayRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(gameplayRoot instanceof GameplayPane);

        GameplayPane gameplayPane = ((GameplayPane) gameplayRoot);
        Node bottomBarNode = gameplayPane.getBottom();

        Node infoNode = ((HBox) bottomBarNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof GameplayInfoPane)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        GameplayInfoPane infoPane = ((GameplayInfoPane) infoNode);

        try {
            Field levelNameField = GAMEPLAY_INFO_CLAZZ.getDeclaredField("levelNameLabel");
            Field timerField = GAMEPLAY_INFO_CLAZZ.getDeclaredField("timerLabel");
            Field numMovesField = GAMEPLAY_INFO_CLAZZ.getDeclaredField("numMovesLabel");
            Field numRestartsField = GAMEPLAY_INFO_CLAZZ.getDeclaredField("numRestartsLabel");

            levelNameField.setAccessible(true);
            timerField.setAccessible(true);
            numMovesField.setAccessible(true);
            numRestartsField.setAccessible(true);

            type(DEADLOCK_MOVES_1);
            waitForFxEvents();

            Stage dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
            assertNotNull(dialog);

            if (System.getenv("CI") != null && System.getenv("CI").equals("true")) {
                Method m = GAMEPLAY_CLAZZ.getDeclaredMethod("doRestartAction");
                m.setAccessible(true);

                Platform.runLater(() -> {
                    try {
                        m.invoke(gameplayPane);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        fail();
                    }
                });
            } else {
                type(KeyCode.SPACE);
            }
            waitForFxEvents();

            assertEquals("Level: 02-easy.txt", ((Label) levelNameField.get(infoPane)).getText());
            assertTrue(((Label) timerField.get(infoPane)).getText().startsWith("Time: 00:0"));
            assertEquals("Moves: 0", ((Label) numMovesField.get(infoPane)).getText());
            assertEquals("Restarts: 1", ((Label) numRestartsField.get(infoPane)).getText());

            type(DEADLOCK_MOVES_1);
            waitForFxEvents();

            if (!getTopModalStage().isPresent()) {
                type(DEADLOCK_MOVES_2);
                waitForFxEvents();
            }

            dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
            assertNotNull(dialog);

            if (System.getenv("CI") != null && System.getenv("CI").equals("true")) {
                Method m = GAMEPLAY_CLAZZ.getDeclaredMethod("doReturnToLevelSelectMenu");
                m.setAccessible(true);

                Platform.runLater(() -> {
                    try {
                        m.invoke(gameplayPane);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        fail();
                    }
                });
            } else {
                type(KeyCode.RIGHT, KeyCode.SPACE);
            }
            waitForFxEvents();

            assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof LevelSelectPane);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
            fail();
        }
    }

    /**
     * <p>Tests for Condition 4.4.1:</p>
     *
     * <p>Win the level, and ensure</p>
     * <ul>
     * <li>The level clear popup shows</li>
     * <li>Click Next Level, and ensure:</li>
     * <ul>
     * <li>The map has been reset</li>
     * <li>The timer has been reset and is functioning normally</li>
     * <li>The moves counter has been reset</li>
     * <li>The number of restarts has been reset</li>
     * </ul>
     * </ul>
     */
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
        waitForFxEvents();

        Parent gameplayRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(gameplayRoot instanceof GameplayPane);

        GameplayPane gameplayPane = ((GameplayPane) gameplayRoot);
        Node bottomBarNode = gameplayPane.getBottom();

        Node infoNode = ((HBox) bottomBarNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof GameplayInfoPane)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        GameplayInfoPane infoPane = ((GameplayInfoPane) infoNode);

        type(WIN_MOVES);
        waitForFxEvents();

        Stage dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
        assertNotNull(dialog);

        if (System.getenv("CI") != null && System.getenv("CI").equals("true")) {
            try {
                Method m = GAMEPLAY_CLAZZ.getDeclaredMethod("doLoadNextLevel");
                m.setAccessible(true);

                Platform.runLater(() -> {
                    try {
                        m.invoke(gameplayPane);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        fail();
                    }
                });
            } catch (NoSuchMethodException e) {
                fail();
            }
        } else {
            type(KeyCode.SPACE);
        }
        waitForFxEvents();

        try {
            Field levelNameField = GAMEPLAY_INFO_CLAZZ.getDeclaredField("levelNameLabel");
            Field timerField = GAMEPLAY_INFO_CLAZZ.getDeclaredField("timerLabel");
            Field numMovesField = GAMEPLAY_INFO_CLAZZ.getDeclaredField("numMovesLabel");
            Field numRestartsField = GAMEPLAY_INFO_CLAZZ.getDeclaredField("numRestartsLabel");

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

    /**
     * <p>Tests for Condition 4.4.2:</p>
     *
     * <p>Win the level, and ensure</p>
     * <ul>
     * <li>The level clear popup shows</li>
     * <li>Click Quit to Menu, and ensure:</li>
     * <ul>
     * <li>A popup shows, asking if you're sure about returning to the menu</li>
     * <li>Clicking OK brings you back to the menu</li>
     * </ul>
     * </ul>
     */
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
        waitForFxEvents();

        Parent gameplayRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(gameplayRoot instanceof GameplayPane);

        GameplayPane gameplayPane = ((GameplayPane) gameplayRoot);

        type(WIN_MOVES);
        waitForFxEvents();

        Stage dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
        assertNotNull(dialog);

        if (System.getenv("CI") != null && System.getenv("CI").equals("true")) {
            try {
                Method m = GAMEPLAY_CLAZZ.getDeclaredMethod("doReturnToLevelSelectMenu");
                m.setAccessible(true);

                Platform.runLater(() -> {
                    try {
                        m.invoke(gameplayPane);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        fail();
                    }
                });
            } catch (NoSuchMethodException e) {
                fail();
            }
        } else {
            type(KeyCode.RIGHT, KeyCode.SPACE);
        }
        waitForFxEvents();

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof LevelSelectPane);
    }
}
