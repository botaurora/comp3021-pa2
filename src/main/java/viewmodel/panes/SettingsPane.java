package viewmodel.panes;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import viewmodel.AudioManager;
import viewmodel.Config;
import viewmodel.SceneManager;

import java.util.Arrays;

/**
 * Represents the settings pane in the game
 */
public class SettingsPane extends BorderPane {
    private VBox leftContainer;
    private Button returnButton;
    private Button toggleSoundFXButton;
    private VBox centerContainer;
    private TextArea infoText;

    /**
     * Instantiate the member components and connect and style them. Also set the callbacks.
     * Hints:
     * Use 20 for the VBox spacing.
     * The text of {@link SettingsPane#toggleSoundFXButton} should depend on {@link AudioManager}
     * Use {@link Config#getAboutText()} for the infoText
     */
    public SettingsPane() {
        leftContainer = new VBox(20);
        returnButton = new Button("Return");
        toggleSoundFXButton = new Button();
        centerContainer = new VBox(20);
        infoText = new TextArea(Config.getAboutText());

        updateSoundToggleText();

        connectComponents();
        styleComponents();
        setCallbacks();
    }

    /**
     * Connects the components together (think adding them into another, setting their positions, etc).
     */
    private void connectComponents() {
        leftContainer.getChildren().addAll(
                returnButton,
                toggleSoundFXButton
        );
        centerContainer.getChildren().addAll(
                infoText
        );

        this.setLeft(leftContainer);
        this.setCenter(centerContainer);
    }

    /**
     * Apply CSS styling to components.
     * <p>
     * Also set the text area to not be editable, but allow text wrapping.
     */
    private void styleComponents() {
        leftContainer.getStyleClass().add("side-menu");
        infoText.getStyleClass().add("text-area");
        infoText.setEditable(false);
        infoText.setWrapText(true);
        infoText.setPrefHeight(Config.HEIGHT);

        centerContainer.getStyleClass().add("big-vbox");

        for (Button b : Arrays.asList(returnButton, toggleSoundFXButton)) {
            b.getStyleClass().add("big-button");
        }
    }

    /**
     * Set the event handler for the 2 buttons.
     * The return button should go to the main menu scene
     */
    private void setCallbacks() {
        toggleSoundFXButton.setOnAction(event -> {
            AudioManager.getInstance().setEnabled(!AudioManager.getInstance().isEnabled());
            updateSoundToggleText();
        });
        returnButton.setOnAction(event -> SceneManager.getInstance().showMainMenuScene());
    }

    /**
     * Updates the text of {@link #toggleSoundFXButton} from the current value of {@link AudioManager#isEnabled()}.
     */
    private void updateSoundToggleText() {
        if (AudioManager.getInstance().isEnabled()) {
            toggleSoundFXButton.setText("Disable Sound FX");
        } else {
            toggleSoundFXButton.setText("Enable Sound FX");
        }
    }
}
