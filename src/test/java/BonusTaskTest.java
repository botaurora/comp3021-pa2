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
import model.Map.Cell;
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

import java.io.File;
import java.io.IOException;
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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class BonusTaskTest extends ApplicationTest {
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

    private LevelManager levelManager;

    private static final KeyCode[] WIN_MAP1_MOVES = {
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

    private static final KeyCode[] DEADLOCK_MAP2_MOVES = {
            KeyCode.A,
            KeyCode.W,
            KeyCode.A,
            KeyCode.S
    };

    private static final KeyCode[] WIN_MAP3_MOVES = {
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

    private static final KeyCode[] WIN_MAP4_MOVES = {
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

    private static final KeyCode[] DEADLOCK_MAP5_MOVES = {
            KeyCode.D,
            KeyCode.D,
            KeyCode.D,
            KeyCode.W,
            KeyCode.D,
            KeyCode.S
    };

    @BeforeEach
    void setupEach() {
        levelManager = LevelManager.getInstance();

        try {
            Path testPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("assets/maps/01-easy.txt").toURI());
            Path actualPath = testPath.getParent().toAbsolutePath();

            Class<?> clazz = LevelSelectPane.class;
            Method m = clazz.getDeclaredMethod("commitMapDirectoryChange", File.class);
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

    @AfterEach
    void cleanupEach() {
        System.setOut(System.out);
        System.setErr(System.err);

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

        type(WIN_MAP1_MOVES);
        waitForFxEvents();

        Stage dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
        assertNotNull(dialog);

        if (System.getenv("CI") != null && System.getenv("CI").equals("true")) {
            Class<?> gpClazz = GameplayPane.class;
            try {
                Method m = gpClazz.getDeclaredMethod("doLoadNextLevel");
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

        type(DEADLOCK_MAP2_MOVES);
        waitForFxEvents();

        Stage dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
        assertNotNull(dialog);
    }

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
            Class<?> clazz = Map.class;
            Field f = clazz.getDeclaredField("player");
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
            Class<?> clazz = Map.class;
            Field f = clazz.getDeclaredField("player");
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
            Class<?> clazz = Map.class;
            Field f = clazz.getDeclaredField("player");
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
            Class<?> clazz = Map.class;
            Field f = clazz.getDeclaredField("player");
            f.setAccessible(true);

            Player p = ((Player) f.get(levelManager.getGameLevel().getMap()));

            assertEquals(initialPlayer.getR(), p.getR());
            assertEquals(initialPlayer.getC(), p.getC());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail();
        }

        assertTrue(levelManager.getGameLevel().getMap().getCrates().stream().allMatch(it -> initialCrates.stream().anyMatch(c -> it.getR() == c.getR() && it.getC() == c.getC())));
    }

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

            Class<?> gpInfoClazz = GameplayInfoPane.class;

            type(WIN_MAP3_MOVES);
            waitForFxEvents();

            Stage dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
            assertNotNull(dialog);

            if (System.getenv("CI") != null && System.getenv("CI").equals("true")) {
                Class<?> gpClazz = GameplayPane.class;
                try {
                    Method m = gpClazz.getDeclaredMethod("doLoadNextLevel");
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
                Field levelNameField = gpInfoClazz.getDeclaredField("levelNameLabel");
                Field timerField = gpInfoClazz.getDeclaredField("timerLabel");
                Field numMovesField = gpInfoClazz.getDeclaredField("numMovesLabel");
                Field numRestartsField = gpInfoClazz.getDeclaredField("numRestartsLabel");

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

            type(WIN_MAP4_MOVES);
            waitForFxEvents();

            dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
            assertNotNull(dialog);

            if (System.getenv("CI") != null && System.getenv("CI").equals("true")) {
                Class<?> gpClazz = GameplayPane.class;
                try {
                    Method m = gpClazz.getDeclaredMethod("doReturnToLevelSelectMenu");
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

            Class<?> gpInfoClazz = GameplayInfoPane.class;

            type(WIN_MAP4_MOVES);
            waitForFxEvents();

            Stage dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
            assertNotNull(dialog);

            if (System.getenv("CI") != null && System.getenv("CI").equals("true")) {
                Class<?> gpClazz = GameplayPane.class;
                try {
                    Method m = gpClazz.getDeclaredMethod("doLoadNextLevel");
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
                Field levelNameField = gpInfoClazz.getDeclaredField("levelNameLabel");
                Field timerField = gpInfoClazz.getDeclaredField("timerLabel");
                Field numMovesField = gpInfoClazz.getDeclaredField("numMovesLabel");
                Field numRestartsField = gpInfoClazz.getDeclaredField("numRestartsLabel");

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

            type(DEADLOCK_MAP5_MOVES);
            waitForFxEvents();

            dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
            assertNotNull(dialog);

            if (System.getenv("CI") != null && System.getenv("CI").equals("true")) {
                Class<?> gpClazz = GameplayPane.class;
                try {
                    Method m = gpClazz.getDeclaredMethod("doReturnToLevelSelectMenu");
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
