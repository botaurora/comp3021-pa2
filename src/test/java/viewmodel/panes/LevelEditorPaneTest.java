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
public class LevelEditorPaneTest extends ApplicationTest {
    /**
     * Displays the Level Editor scene.
     *
     * @param stage Primary Stage.
     */
    @Override
    @Start
    public void start(Stage stage) {
        Platform.runLater(() -> SceneManager.getInstance().setStage(stage));
        Platform.runLater(() -> SceneManager.getInstance().showLevelEditorScene());

        waitForFxEvents();
    }

    /**
     * Tests "Return" button.
     */
    @Test
    void testReturnToMainMenu() {
        Parent currentRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(currentRoot instanceof LevelEditorPane);

        Node leftVBoxNode = currentRoot.getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof VBox && ((VBox) it).getChildren().size() == 6)
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
}
