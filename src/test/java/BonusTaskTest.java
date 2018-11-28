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

        Platform.runLater(() -> getTopModalStage().get().close());
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
}
