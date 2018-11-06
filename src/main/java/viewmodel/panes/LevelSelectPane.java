package viewmodel.panes;

import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import model.Exceptions.InvalidMapException;
import model.LevelManager;
import model.Map.Cell;
import viewmodel.MapRenderer;
import viewmodel.SceneManager;

import java.io.File;
import java.util.Arrays;

/**
 * Represents the main menu in the game
 */
public class LevelSelectPane extends BorderPane {
    private VBox leftContainer;
    private Button returnButton;
    private Button playButton;
    private Button chooseMapDirButton;
    private ListView<String> levelsListView;
    private VBox centerContainer;
    private Canvas levelPreview;

    /**
     * Instantiate the member components and connect and style them. Also set the callbacks.
     * Use 20 for VBox spacing
     */
    public LevelSelectPane() {
        leftContainer = new VBox(20);
        returnButton = new Button("Return");
        playButton = new Button("Play");
        chooseMapDirButton = new Button("Choose map directory");
        levelsListView = new ListView<>();
        centerContainer = new VBox(20);
        levelPreview = new Canvas();

        playButton.setDisable(true);

        connectComponents();
        styleComponents();
        setCallbacks();
    }

    /**
     * Connects the components together (think adding them into another, setting their positions, etc). Reference
     * the other classes in the {@link javafx.scene.layout.Pane} package.
     */
    private void connectComponents() {
        leftContainer.getChildren().addAll(
                returnButton,
                chooseMapDirButton,
                levelsListView,
                playButton
        );
        centerContainer.getChildren().addAll(
                levelPreview
        );

        levelsListView.setItems(LevelManager.getInstance().getLevelNames());

        this.setLeft(leftContainer);
        this.setCenter(centerContainer);
    }

    /**
     * Apply CSS styling to components. Also sets the {@link LevelSelectPane#playButton} to be disabled.
     */
    private void styleComponents() {
        leftContainer.getStyleClass().add("side-menu");
        centerContainer.getStyleClass().add("big-vbox");

        for (Button b : Arrays.asList(returnButton, chooseMapDirButton, playButton)) {
            b.getStyleClass().add("big-button");
        }
    }

    /**
     * Set the event handlers for the 3 buttons and listview.
     * <p>
     * Hints:
     * The return button should show the main menu scene
     * The chooseMapDir button should prompt the user to choose the map directory, and load the levels
     * The play button should set the current level based on the current level name (see LevelManager), show
     * the gameplay scene, and start the level timer.
     * The listview, based on which item was clicked, should set the current level (see LevelManager), render the
     * preview (see {@link MapRenderer#render(Canvas, Cell[][])}}, and set the play button to enabled.
     */
    private void setCallbacks() {
        returnButton.setOnAction(event -> SceneManager.getInstance().showMainMenuScene());
        chooseMapDirButton.setOnAction(event -> promptUserForMapDirectory());
        playButton.setOnAction(event -> {
            System.out.println(this.getClass().getSimpleName() + ": STUB");
            // TODO(Derppening): STUB
        });
        levelsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                LevelManager.getInstance().setLevel(newValue);
                MapRenderer.render(levelPreview, LevelManager.getInstance().getGameLevel().getMap().getCells());

                playButton.setDisable(false);
            } catch (InvalidMapException e) {
                System.err.println("Invalid map selected!");
                e.printStackTrace();
            }
        });
    }

    /**
     * Popup a DirectoryChooser window to ask the user where the map folder is stored.
     * Update the LevelManager's map directory afterwards, and potentially
     * load the levels from disk using LevelManager (if the user didn't cancel out the window)
     */
    private void promptUserForMapDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        File folder = chooser.showDialog(null);

        if (folder != null) {
            LevelManager.getInstance().setMapDirectory(folder.getAbsolutePath());
            LevelManager.getInstance().loadLevelNamesFromDisk();

            levelsListView.getSelectionModel().clearSelection();
        }
    }
}
