package viewmodel.panes;

import com.sun.javafx.fxml.PropertyNotFoundException;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import viewmodel.SceneManager;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Miscellaneous tests for {@link LevelEditorPane}.
 */
public class MainMenuPaneTest extends ApplicationTest {
    /**
     * Displays the Level Editor scene.
     *
     * @param stage Primary Stage.
     */
    @Override
    @Start
    public void start(Stage stage) {
        Platform.runLater(() -> SceneManager.getInstance().setStage(stage));
        Platform.runLater(() -> SceneManager.getInstance().showMainMenuScene());

        waitForFxEvents();
    }

    @Test
    void testPlay() {
        Parent currentRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(currentRoot instanceof MainMenuPane);

        Node playButton = ((VBox) ((MainMenuPane) currentRoot).getCenter()).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("Play"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        clickOn(playButton);
        waitForFxEvents();

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof LevelSelectPane);
    }

    @Test
    void testLevelEditor() {
        Parent currentRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(currentRoot instanceof MainMenuPane);

        Node levelEditorButton = ((VBox) ((MainMenuPane) currentRoot).getCenter()).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("Level Editor"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        clickOn(levelEditorButton);
        waitForFxEvents();

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof LevelEditorPane);
    }

    @Test
    void testSettings() {
        Parent currentRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(currentRoot instanceof MainMenuPane);

        Node settingsButton = ((VBox) ((MainMenuPane) currentRoot).getCenter()).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("About / Settings"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        clickOn(settingsButton);
        waitForFxEvents();

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof SettingsPane);
    }
}
