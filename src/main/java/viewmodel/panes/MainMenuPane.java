package viewmodel.panes;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import viewmodel.SceneManager;

import java.util.Arrays;

/**
 * Represents the main menu in the game
 */
public class MainMenuPane extends BorderPane {

    private VBox container;
    private Label title;
    private Button playButton;
    private Button levelEditorButton;
    private Button settingsButton;
    private Button quitButton;

    /**
     * Instantiate the member components and connect and style them. Also set the callbacks.
     */
    public MainMenuPane() {
        container = new VBox();
        title = new Label("Sokoban");
        playButton = new Button("Play");
        levelEditorButton = new Button("Level Editor");
        settingsButton = new Button("About / Settings");
        quitButton = new Button("Quit");

        connectComponents();
        styleComponents();
        setCallbacks();
    }

    /**
     * Connects the components together (think adding them into another, setting their positions, etc).
     * Use this for reference.
     */
    private void connectComponents() {
        container.getChildren().addAll(
                title,
                playButton,
                levelEditorButton,
                settingsButton,
                quitButton
        );
        this.setCenter(container);
    }

    /**
     * Apply CSS styling to components.
     */
    private void styleComponents() {
        container.getStyleClass().add("big-vbox");

        for (Button b : Arrays.asList(playButton, levelEditorButton, settingsButton, quitButton)) {
            b.getStyleClass().add("big-button");
        }
    }

    /**
     * Set the event handlers for the 4 buttons, 3 of which switch to different scene, and 1 of which exits the program.
     */
    private void setCallbacks() {
        playButton.setOnAction(event -> SceneManager.getInstance().showGamePlayScene());
        levelEditorButton.setOnAction(event -> SceneManager.getInstance().showLevelEditorScene());
        settingsButton.setOnAction(event -> SceneManager.getInstance().showSettingsMenuScene());
        quitButton.setOnAction(event -> Platform.exit());
    }
}
