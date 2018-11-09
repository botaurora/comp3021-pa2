import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

@ExtendWith(ApplicationExtension.class)
public class SceneManagerTest extends ApplicationTest {
    @Override
    @Start
    public void start(Stage stage) {
        Platform.runLater(() -> SceneManager.getInstance().setStage(stage));

        WaitForAsyncUtils.sleep(100, TimeUnit.MILLISECONDS);
    }

    @Test
    void testShowMainMenuScene() {
        Platform.runLater(() -> SceneManager.getInstance().showMainMenuScene());
        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof MainMenuPane);
    }

    @Test
    void testShowLevelSelectMenuScene() {
        Platform.runLater(() -> SceneManager.getInstance().showLevelSelectMenuScene());
        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof LevelSelectPane);
    }

    @Test
    void testShowLevelEditorScene() {
        Platform.runLater(() -> SceneManager.getInstance().showLevelEditorScene());
        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof LevelEditorPane);
    }

    @Test
    void testShowSettingsMenuScene() {
        Platform.runLater(() -> SceneManager.getInstance().showSettingsMenuScene());
        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof SettingsPane);
    }
}
