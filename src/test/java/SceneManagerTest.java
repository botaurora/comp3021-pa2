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
    @BeforeAll
    static void setup() {
        System.out.println();
    }

    @Override
    @Start
    public void start(Stage stage) {
        Platform.runLater(() -> SceneManager.getInstance().setStage(stage));

        WaitForAsyncUtils.sleep(100, TimeUnit.MILLISECONDS);
    }

    @Test
    void testShowMainMenuScene() {
        System.out.println("SceneManagerTest::testShowMainMenuScene()");

        Platform.runLater(() -> SceneManager.getInstance().showMainMenuScene());
        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof MainMenuPane);
    }

    @Test
    void testShowLevelSelectMenuScene() {
        System.out.println("SceneManagerTest::testShowLevelSelectMenuScene()");

        Platform.runLater(() -> SceneManager.getInstance().showLevelSelectMenuScene());
        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof LevelSelectPane);
    }

    @Test
    void testShowLevelEditorScene() {
        System.out.println("SceneManagerTest::testShowLevelEditorScene()");

        Platform.runLater(() -> SceneManager.getInstance().showLevelEditorScene());
        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof LevelEditorPane);
    }

    @Test
    void testShowSettingsMenuScene() {
        System.out.println("SceneManagerTest::testShowSettingsMenuScene()");

        Platform.runLater(() -> SceneManager.getInstance().showSettingsMenuScene());
        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof SettingsPane);
    }
}
