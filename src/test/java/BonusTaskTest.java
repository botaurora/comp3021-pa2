import com.sun.javafx.fxml.PropertyNotFoundException;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.LevelManager;
import model.Map.Map;
import model.Map.Occupant.Crate;
import model.Map.Occupant.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import viewmodel.SceneManager;
import viewmodel.customNodes.GameplayInfoPane;
import viewmodel.panes.GameplayPane;
import viewmodel.panes.LevelSelectPane;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Test cases for all implemented bonus tasks.
 */
public class BonusTaskTest extends ApplicationTest {
    /**
     * Keycode sequence for winning 01-easy.txt.
     */
    private static final KeyCode[] MAP1_WIN_MOVES = {
            KeyCode.A,
            KeyCode.A,
            KeyCode.D,
            KeyCode.W,
            KeyCode.W,
            KeyCode.S,
            KeyCode.D,
            KeyCode.D,
            KeyCode.A,
            KeyCode.S,
            KeyCode.S
    };

    /**
     * Keycode sequence for deadlocking 02-easy.txt.
     */
    private static final KeyCode[] MAP2_DEADLOCK_MOVES = {
            KeyCode.A,
            KeyCode.W,
            KeyCode.A,
            KeyCode.S
    };

    /**
     * Keycode sequence for winning 03-easy.txt.
     */
    private static final KeyCode[] MAP3_WIN_MOVES = {
            KeyCode.S,
            KeyCode.D,
            KeyCode.S,
            KeyCode.D,
            KeyCode.S,
            KeyCode.S,
            KeyCode.A,
            KeyCode.A,
            KeyCode.D,
            KeyCode.D,
            KeyCode.W,
            KeyCode.W,
            KeyCode.A,
            KeyCode.S,
            KeyCode.A,
            KeyCode.D,
            KeyCode.W,
            KeyCode.W,
            KeyCode.A,
            KeyCode.S,
            KeyCode.S,
            KeyCode.W,
            KeyCode.W,
            KeyCode.W,
            KeyCode.W,
            KeyCode.D,
            KeyCode.S,
            KeyCode.S,
            KeyCode.S,
            KeyCode.A,
            KeyCode.S,
            KeyCode.D,
            KeyCode.W,
            KeyCode.D,
            KeyCode.S
    };

    /**
     * Keycode sequence for winning 04-easy.txt.
     */
    private static final KeyCode[] MAP4_WIN_MOVES = {
            KeyCode.S,
            KeyCode.D,
            KeyCode.D,
            KeyCode.S,
            KeyCode.S,
            KeyCode.D,
            KeyCode.S,
            KeyCode.S,
            KeyCode.A,
            KeyCode.A,
            KeyCode.W,
            KeyCode.A,
            KeyCode.A,
            KeyCode.S,
            KeyCode.A,
            KeyCode.W,
            KeyCode.D,
            KeyCode.D,
            KeyCode.D,
            KeyCode.S,
            KeyCode.A,
            KeyCode.A,
            KeyCode.D,
            KeyCode.D,
            KeyCode.D,
            KeyCode.D,
            KeyCode.W,
            KeyCode.W,
            KeyCode.A,
            KeyCode.W,
            KeyCode.W,
            KeyCode.A,
            KeyCode.A,
            KeyCode.S,
            KeyCode.S,
            KeyCode.W,
            KeyCode.W,
            KeyCode.D,
            KeyCode.D,
            KeyCode.S,
            KeyCode.S,
            KeyCode.D,
            KeyCode.S,
            KeyCode.S,
            KeyCode.A,
            KeyCode.A,
            KeyCode.W,
            KeyCode.A,
            KeyCode.A
    };

    /**
     * Keycode sequence for deadlocking 05-normal.txt.
     */
    private static final KeyCode[] MAP5_DEADLOCK_MOVES = {
            KeyCode.D,
            KeyCode.D,
            KeyCode.D,
            KeyCode.W,
            KeyCode.D,
            KeyCode.S
    };

    private static final Class<?> MAP_CLAZZ = Map.class;
    private static final Class<?> LEVEL_SELECT_CLAZZ = LevelSelectPane.class;
    private static final Class<?> GAMEPLAY_CLAZZ = GameplayPane.class;
    private static final Class<?> GAMEPLAY_INFO_CLAZZ = GameplayInfoPane.class;

    private LevelManager levelManager;

    /**
     * Cached path to the directory containing all maps.
     */
    private Path mapsPath;

    {
        try {
            Path path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("assets/maps/01-easy.txt").toURI());
            mapsPath = path.getParent().toAbsolutePath();
        } catch (URISyntaxException e) {
            mapsPath = null;
            fail();
        }
    }

    /**
     * Caches the current instance of {@link LevelManager}, and loads all maps from
     * {@link BonusTaskTest#mapsPath}.
     */
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
     * Resets {@code System.out} and {@code System.err} to the original location, and restores all maps
     * to the original directory.
     */
    @AfterEach
    void cleanupEach() {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));

        try {
            Files.walk(Paths.get(mapsPath.getParent().toString()), 1)
                    .filter(f -> f.toFile().isFile())
                    .filter(it -> it.getFileName().toString().endsWith(".txt"))
                    .forEach(it -> {
                        try {
                            Files.move(it, Paths.get(mapsPath.toString(), it.getFileName().toString()));
                        } catch (IOException e) {
                            fail();
                        }
                    });
        } catch (IOException e) {
            fail();
        }
    }

    /**
     * Displays the level selection scene.
     *
     * @param stage Primary stage of the application.
     */
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
     * <p>Tests for implementation of Bonus Task 1.</p>
     *
     * <ul>
     * <li>Load the maps from the “maps” folder</li>
     * <li>Select 01-easy.txt and start playing</li>
     * <li>Delete 02-easy.txt</li>
     * <li>Win 01-easy and go to the next level</li>
     * <li>The game should popup a dialog warning that 02-easy is missing</li>
     * <li>The game should then load 03-easy.txt</li>
     * </ul>
     */
    @Test
    void testBonusTask1() {
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
        listView.getSelectionModel().select("01-easy.txt");

        clickOn(playNode);
        waitForFxEvents();

        Parent gameplayRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(gameplayRoot instanceof GameplayPane);

        GameplayPane gameplayPane = (GameplayPane) gameplayRoot;
        Node bottomBarNode = gameplayPane.getBottom();

        Node infoNode = ((HBox) bottomBarNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof GameplayInfoPane)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        GameplayInfoPane infoPane = ((GameplayInfoPane) infoNode);

        try {
            Files.move(Paths.get(mapsPath.toString(), "02-easy.txt"), Paths.get(mapsPath.getParent().toString(), "02-easy.txt"));
        } catch (IOException e) {
            fail();
        }

        type(MAP1_WIN_MOVES);
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

        dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
        assertNotNull(dialog);

        Platform.runLater(() -> getTopModalStage().ifPresent(Stage::close));
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
     * <p>Tests for implementation of Bonus Task 3.</p>
     *
     * <ul>
     * <li>Start a game</li>
     * <li>Push one crate to a non-destination location where the crate cannot be further moved</li>
     * <li>The game should report a deadlock</li>
     * </ul>
     */
    @Test
    void testBonusTask3() {
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

        type(MAP2_DEADLOCK_MOVES);
        waitForFxEvents();

        Stage dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
        assertNotNull(dialog);

        if (System.getenv("CI") != null && System.getenv("CI").equals("true")) {
            System.out.println("CI environment detected: Skipping dialog");
        } else {
            type(KeyCode.SPACE);
        }
    }

    /**
     * <p>Tests for implementation of Bonus Task 4.</p>
     *
     * <ul>
     * <li>Prepare a folder containing invalid maps such as random text files</li>
     * <li>Start the game and let the game load the folder</li>
     * <li>Select an invalid map</li>
     * <li>An alert should popup and warns that this is an invalid map</li>
     * <li>The map should be removed from the list</li>
     * </ul>
     */
    @Test
    void testBonusTask4() {
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

        @SuppressWarnings("unchecked") final ListView<String> listView = (ListView<String>) levelsListViewNode;
        Platform.runLater(() -> listView.getSelectionModel().select("00-invalid.txt"));
        waitForFxEvents();

        if (System.getenv("CI") != null && System.getenv("CI").equals("true")) {
            System.out.println("CI environment detected: Skipping check for dialog");
        } else {
            Stage dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
            assertNotNull(dialog);

            Platform.runLater(() -> getTopModalStage().ifPresent(Stage::close));
            waitForFxEvents();
        }

        assertEquals(0, listView.getItems().filtered(it -> it.equals("00-invalid.txt")).size());
    }

    /**
     * <p>Tests for implementation of Bonus Task 5.</p>
     *
     * <ul>
     * <li>Start a game</li>
     * <li>Make some moves</li>
     * <li>Click undo once and it should undo one move</li>
     * <li>Make some moves</li>
     * <li>Click undo until it goes back to the initial state of the game</li>
     * </ul>
     */
    @Test
    void testBonusTask5() {
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
        listView.getSelectionModel().select("01-easy.txt");

        clickOn(playNode);
        waitForFxEvents();

        Parent gameplayRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(gameplayRoot instanceof GameplayPane);

        GameplayPane gameplayPane = (GameplayPane) gameplayRoot;
        Node bottomBarNode = gameplayPane.getBottom();

        Node undoNode = ((HBox) bottomBarNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("Undo"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        Button undoButton = ((Button) undoNode);

        Player initialPlayer = null;
        try {
            Field f = MAP_CLAZZ.getDeclaredField("player");
            f.setAccessible(true);

            Player p = ((Player) f.get(levelManager.getGameLevel().getMap()));
            initialPlayer = new Player(p.getR(), p.getC());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail();
        }
        List<Crate> initialCrates = new ArrayList<>();
        levelManager.getGameLevel().getMap().getCrates().forEach(it -> initialCrates.add(it.clone()));

        type(KeyCode.A);
        waitForFxEvents();

        Player player = null;
        try {
            Field f = MAP_CLAZZ.getDeclaredField("player");
            f.setAccessible(true);

            Player p = ((Player) f.get(levelManager.getGameLevel().getMap()));
            player = new Player(p.getR(), p.getC());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail();
        }
        List<Crate> crates = new ArrayList<>();
        levelManager.getGameLevel().getMap().getCrates().forEach(it -> crates.add(it.clone()));

        type(KeyCode.A);
        waitForFxEvents();

        clickOn(undoNode);
        waitForFxEvents();

        try {
            Field f = MAP_CLAZZ.getDeclaredField("player");
            f.setAccessible(true);

            Player p = ((Player) f.get(levelManager.getGameLevel().getMap()));

            assertEquals(player.getR(), p.getR());
            assertEquals(player.getC(), p.getC());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail();
        }

        assertTrue(levelManager.getGameLevel().getMap().getCrates().stream().allMatch(it -> crates.stream().anyMatch(c -> it.getR() == c.getR() && it.getC() == c.getC())));

        type(
                KeyCode.A,
                KeyCode.D,
                KeyCode.W,
                KeyCode.W,
                KeyCode.S
        );

        while (!undoButton.isDisabled()) {
            clickOn(undoButton);
            waitForFxEvents();
        }

        try {
            Field f = MAP_CLAZZ.getDeclaredField("player");
            f.setAccessible(true);

            Player p = ((Player) f.get(levelManager.getGameLevel().getMap()));

            assertEquals(initialPlayer.getR(), p.getR());
            assertEquals(initialPlayer.getC(), p.getC());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail();
        }

        assertTrue(levelManager.getGameLevel().getMap().getCrates().stream().allMatch(it -> initialCrates.stream().anyMatch(c -> it.getR() == c.getR() && it.getC() == c.getC())));
    }

    /**
     * <p>Tests for implementation of Bonus Task 7.</p>
     *
     * <ul>
     * <li>On Level Select screen, choose /assets/maps/ as the Map Directory and choose "03-easy.txt" to
     * play.</li>
     * <li>Play the game to clear "03-easy.txt" level.</li>
     * <li>When the level clear pop-up appears, choose "Next level" to go to "04-easy.txt" map.</li>
     * <li>Play the game to clear “04-easy.txt” level.</li>
     * <li>When the level clear pop-up appears, choose “Return”.</li>
     * <li>Now the program should return to Level Select screen, while the level "04-easy.txt" is
     * highlighted in the ListView and rendered on the previewing Canvas.</li>
     * <li>Click on "Play" to play "04-easy.txt". The program should go to the Gameplay screen where its
     * user can play “04-easy.txt”.</li>
     * <li>Play the game to clear this level again.</li>
     * <li>When the level clear pop-up appears this time, choose "Next level" to go to "05-normal.txt"
     * map.</li>
     * <li>Play the game but make moves to get the game deadlocked.</li>
     * <li>When the level deadlocked pop-up appears, choose "Return".</li>
     * <li>Now the program should return to Level Select screen, while the level "05-normal.txt" is
     * highlighted in the ListView and rendered on the previewing Canvas.</li>
     * </ul>
     */
    @Test
    void testBonusTask7() {
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
        listView.getSelectionModel().select("03-easy.txt");

        clickOn(playNode);
        waitForFxEvents();

        {
            Parent gameplayRoot = SceneManager.getInstance().getStage().getScene().getRoot();
            assertTrue(gameplayRoot instanceof GameplayPane);

            GameplayPane gameplayPane = (GameplayPane) gameplayRoot;
            Node bottomBarNode = gameplayPane.getBottom();

            Node infoNode = ((HBox) bottomBarNode).getChildrenUnmodifiable().stream()
                    .filter(it -> it instanceof GameplayInfoPane)
                    .findFirst()
                    .orElseThrow(PropertyNotFoundException::new);
            GameplayInfoPane infoPane = ((GameplayInfoPane) infoNode);

            type(MAP3_WIN_MOVES);
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

                assertEquals("Level: 04-easy.txt", ((Label) levelNameField.get(infoPane)).getText());
                assertTrue(((Label) timerField.get(infoPane)).getText().startsWith("Time: 00:0"));
                assertEquals("Moves: 0", ((Label) numMovesField.get(infoPane)).getText());
                assertEquals("Restarts: 0", ((Label) numRestartsField.get(infoPane)).getText());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail();
            }

            type(MAP4_WIN_MOVES);
            waitForFxEvents();

            dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
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
        }

        levelSelectRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertEquals(levelSelectRoot, SceneManager.getInstance().getStage().getScene().getRoot());

        assertEquals("04-easy.txt", listView.getSelectionModel().selectedItemProperty().get());

        clickOn(playNode);
        waitForFxEvents();

        {
            Parent gameplayRoot = SceneManager.getInstance().getStage().getScene().getRoot();
            assertTrue(gameplayRoot instanceof GameplayPane);

            GameplayPane gameplayPane = (GameplayPane) gameplayRoot;
            Node bottomBarNode = gameplayPane.getBottom();

            Node infoNode = ((HBox) bottomBarNode).getChildrenUnmodifiable().stream()
                    .filter(it -> it instanceof GameplayInfoPane)
                    .findFirst()
                    .orElseThrow(PropertyNotFoundException::new);
            GameplayInfoPane infoPane = ((GameplayInfoPane) infoNode);

            type(MAP4_WIN_MOVES);
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

                assertEquals("Level: 05-normal.txt", ((Label) levelNameField.get(infoPane)).getText());
                assertTrue(((Label) timerField.get(infoPane)).getText().startsWith("Time: 00:0"));
                assertEquals("Moves: 0", ((Label) numMovesField.get(infoPane)).getText());
                assertEquals("Restarts: 0", ((Label) numRestartsField.get(infoPane)).getText());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail();
            }

            type(MAP5_DEADLOCK_MOVES);
            waitForFxEvents();

            dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
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
        }

        levelSelectRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertEquals(levelSelectRoot, SceneManager.getInstance().getStage().getScene().getRoot());

        assertEquals("05-normal.txt", listView.getSelectionModel().selectedItemProperty().get());
    }
}
