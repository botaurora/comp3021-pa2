package viewmodel.panes;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
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
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        levelsListView = new ListView<>(LevelManager.getInstance().getLevelNames());
        centerContainer = new VBox(20);
        levelPreview = new Canvas();

        connectComponents();
        styleComponents();
        setCallbacks();

        try {
            Path testPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("assets/maps/01-easy.txt").toURI());
            Path actualPath = testPath.getParent().toAbsolutePath();

            LevelManager.getInstance().setMapDirectory(actualPath.toString());
            LevelManager.getInstance().loadLevelNamesFromDisk();
        } catch (NullPointerException e) {
            throw new IllegalStateException("Cannot find bundled maps");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the highlighted level in {@link LevelSelectPane#levelsListView}.
     */
    public void updateHighlightedLevel() {
        String currentLevel = LevelManager.getInstance().currentLevelNameProperty().get();

        levelsListView.getSelectionModel().clearSelection();
        if (currentLevel != null && LevelManager.getInstance().getLevelNames().stream().anyMatch(it -> it.equals(currentLevel))) {
            levelsListView.getSelectionModel().select(currentLevel);
        } else {
            LevelManager.getInstance().currentLevelNameProperty().set("");
            playButton.setDisable(true);
        }
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

        playButton.setDisable(true);
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
            LevelManager manager = LevelManager.getInstance();

            try {
                manager.setLevel(levelsListView.getSelectionModel().getSelectedItem());
                manager.resetNumRestarts();
                manager.resetLevelTimer();

                SceneManager.getInstance().showGamePlayScene();

                manager.startLevelTimer();
            } catch (InvalidMapException | FileNotFoundException e) {
                throw new IllegalStateException("Cannot find destination map to load!", e);
            }
        });
        levelsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("oldValue = " + oldValue + " newValue = " + newValue);

            if (newValue == null || levelsListView.getItems().stream().noneMatch(it -> it.equals(newValue))) {
                levelPreview.setWidth(0);
                levelPreview.setHeight(0);
                return;
            }

            try {
                LevelManager.getInstance().setLevel(newValue);
                MapRenderer.render(levelPreview, LevelManager.getInstance().getGameLevel().getMap().getCells());

                playButton.setDisable(false);
            } catch (InvalidMapException | FileNotFoundException e) {
                if ((System.getenv("CI") != null && System.getenv("CI").equals("true"))) {
                    System.out.println("CI environment detected: Skipping popup");
                } else {
                    Alert box = new Alert(Alert.AlertType.WARNING);
                    box.setHeaderText("Invalid map!");
                    box.setContentText("Please select another level.");
                    box.showAndWait();
                }

                Platform.runLater(() -> {
                    levelsListView.getSelectionModel().clearSelection();
                    levelsListView.getItems().remove(newValue);
                });
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
            commitMapDirectoryChange(folder);
        }
    }

    /**
     * Helper function to commit the directory change to {@link LevelManager}.
     * <p>
     * Used as a convenience function for test cases to switch between directories.
     *
     * @param dir Directory where map files should be loaded from.
     */
    private void commitMapDirectoryChange(File dir) {
        levelsListView.getSelectionModel().clearSelection();

        LevelManager.getInstance().setMapDirectory(dir.getAbsolutePath());
        LevelManager.getInstance().loadLevelNamesFromDisk();
    }
}
