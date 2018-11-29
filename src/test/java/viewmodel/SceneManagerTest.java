package viewmodel;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import viewmodel.SceneManager;
import viewmodel.panes.LevelEditorPane;
import viewmodel.panes.LevelSelectPane;
import viewmodel.panes.MainMenuPane;
import viewmodel.panes.SettingsPane;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(ApplicationExtension.class)
public class SceneManagerTest extends ApplicationTest {
    @Override
    @Start
    public void start(Stage stage) {
        Platform.runLater(() -> SceneManager.getInstance().setStage(stage));

        waitForFxEvents();
    }

    /**
     * Tests whether main menu scene can be displayed.
     */
    @Test
    void testShowMainMenuScene() {
        Platform.runLater(() -> SceneManager.getInstance().showMainMenuScene());
        waitForFxEvents();

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof MainMenuPane);
    }

    /**
     * Tests whether level select scene can be displayed.
     */
    @Test
    void testShowLevelSelectMenuScene() {
        Platform.runLater(() -> SceneManager.getInstance().showLevelSelectMenuScene());
        waitForFxEvents();

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof LevelSelectPane);

        // TODO(Derppening): Click return and assert
    }

    /**
     * Tests whether level editor scene can be displayed.
     */
    @Test
    void testShowLevelEditorScene() {
        Platform.runLater(() -> SceneManager.getInstance().showLevelEditorScene());
        waitForFxEvents();

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof LevelEditorPane);

        // TODO(Derppening): Click return and assert
    }

    /**
     * Tests whether settings scene can be displayed.
     */
    @Test
    void testShowSettingsMenuScene() {
        Platform.runLater(() -> SceneManager.getInstance().showSettingsMenuScene());
        waitForFxEvents();

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof SettingsPane);

        // TODO(Derppening): Click return and assert
    }
}
