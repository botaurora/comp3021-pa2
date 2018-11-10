import com.sun.javafx.fxml.PropertyNotFoundException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.LevelManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import viewmodel.SceneManager;
import viewmodel.panes.LevelSelectPane;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class PlayingTheGameTest extends ApplicationTest {
    private LevelManager levelManager;

    @BeforeEach
    void setupEach() {
        levelManager = LevelManager.getInstance();
        levelManager.setMapDirectory("");

        Class<?> clazz = LevelManager.class;
        try {
            Field mapList = clazz.getDeclaredField("levelNames");
            mapList.setAccessible(true);

            Platform.runLater(() -> {
                try {
                    ((ObservableList<?>) mapList.get(levelManager)).clear();
                } catch (IllegalAccessException e) {
                    fail(e.getMessage());
                }
            });
        } catch (NoSuchFieldException e) {
            fail(e.getMessage());
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
        for (char c : "build/resources/main/assets/maps/".toCharArray()) {
            KeyCode kc = null;
            if (Character.isAlphabetic(c)) {
                kc = KeyCode.getKeyCode(Character.valueOf(Character.toUpperCase(c)).toString());
            } else if (c == '/') {
                kc = KeyCode.SLASH;
            } else {
                fail("Path contains undefined characters");
            }

            type(kc);
        }
        type(KeyCode.ENTER);

        assertEquals(14, ((ListView<?>) levelsListViewNode).getItems().size());
    }

    @Test
    void testMapDisplay() {
        // TODO(Derppening): Think of a way to do it
    }

    @Test
    void testBasicGameParameters() {
        // TODO(Derppening)
    }

    @Test
    void testGameRestart() {
        // TODO(Derppening)
    }

    @Test
    void testGameDeadlock() {
        // TODO(Derppening)
    }

    @Test
    void testGameWinNextLevel() {
        // TODO(Derppening)
    }

    @Test
    void testGameWinQuit() {
        // TODO(Derppening)
    }
}
