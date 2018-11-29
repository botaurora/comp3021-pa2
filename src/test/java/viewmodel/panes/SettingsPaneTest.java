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
import viewmodel.AudioManager;
import viewmodel.SceneManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Miscellaneous tests for {@link SettingsPane}.
 */
public class SettingsPaneTest extends ApplicationTest {
    /**
     * Displays the Level Editor scene.
     *
     * @param stage Primary Stage.
     */
    @Override
    @Start
    public void start(Stage stage) {
        Platform.runLater(() -> SceneManager.getInstance().setStage(stage));
        Platform.runLater(() -> SceneManager.getInstance().showSettingsMenuScene());

        waitForFxEvents();
    }

    /**
     * Tests "Return" button.
     */
    @Test
    void testReturnToMainMenu() {
        Parent currentRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(currentRoot instanceof SettingsPane);

        Node leftVBoxNode = currentRoot.getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof VBox && ((VBox) it).getChildren().size() == 2)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        Node returnNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("Return"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        clickOn(returnNode);
        waitForFxEvents();

        assertTrue(SceneManager.getInstance().getStage().getScene().getRoot() instanceof MainMenuPane);
    }

    @Test
    void testToggleSoundFX() {
        final AudioManager audioManager = AudioManager.getInstance();

        Parent currentRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(currentRoot instanceof SettingsPane);

        Node leftVBoxNode = currentRoot.getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof VBox && ((VBox) it).getChildren().size() == 2)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        Node toggleNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("Disable Sound FX"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        clickOn(toggleNode);
        waitForFxEvents();

        assertFalse(audioManager.isEnabled());

        clickOn(toggleNode);
        waitForFxEvents();

        assertTrue(audioManager.isEnabled());
    }
}
