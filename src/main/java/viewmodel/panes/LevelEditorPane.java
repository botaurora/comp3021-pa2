package viewmodel.panes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import viewmodel.Config;
import viewmodel.LevelEditorCanvas;
import viewmodel.SceneManager;
import viewmodel.customNodes.NumberTextField;

import java.util.Arrays;

import static viewmodel.LevelEditorCanvas.Brush;

/**
 * Represents the level editor in the game
 */
public class LevelEditorPane extends BorderPane {
    private final LevelEditorCanvas levelEditor;
    private VBox leftContainer;
    private Button returnButton;
    private Label rowText;
    private NumberTextField rowField;
    private Label colText;
    private NumberTextField colField;
    private BorderPane rowBox; //holds the rowText and rowField side by side
    private BorderPane colBox; //holds the colText and colField side by side
    private Button newGridButton;
    private ObservableList<Brush> brushList;
    private ListView<Brush> selectedBrush = new ListView<>();
    private Button saveButton;
    private VBox centerContainer;

    /**
     * Instantiate the member components and connect and style them. Also set the callbacks.
     * <p>
     * Hints: {@link LevelEditorPane#rowField} and {@link LevelEditorPane#colField} should be initialized to "5".
     * {@link LevelEditorPane#levelEditor} should be initialized to 5 rows and 5 columns.
     * {@link LevelEditorPane#brushList} should be initialized with all values of the {@link Brush} enum.
     * Use 20 for VBox spacing
     */
    public LevelEditorPane() {
        levelEditor = new LevelEditorCanvas(5, 5);
        leftContainer = new VBox(20);
        returnButton = new Button("Return");
        rowText = new Label("Rows");
        rowField = new NumberTextField("5");
        colText = new Label("Columns");
        colField = new NumberTextField("5");
        rowBox = new BorderPane(null, null, rowField, null, rowText);
        colBox = new BorderPane(null, null, colField, null, colText);
        newGridButton = new Button("New Grid");
        brushList = FXCollections.observableList(Arrays.asList(Brush.values()));
        saveButton = new Button("Save");
        centerContainer = new VBox(20);

        connectComponents();
        styleComponents();
        setCallbacks();
    }

    /**
     * Connects the components together (think adding them into another, setting their positions, etc). Reference
     * the other classes in the {@link javafx.scene.layout.Pane} package.
     * <p>
     * Also sets {@link LevelEditorPane#selectedBrush}'s items, and selects the first.
     */
    private void connectComponents() {
        leftContainer.getChildren().addAll(
                returnButton,
                rowBox,
                colBox,
                newGridButton,
                selectedBrush,
                saveButton
        );
        centerContainer.getChildren().addAll(
                levelEditor
        );

        selectedBrush.setItems(brushList);
        selectedBrush.getSelectionModel().select(0);

        this.setLeft(leftContainer);
        this.setCenter(centerContainer);
    }

    /**
     * Apply CSS styling to components.
     */
    private void styleComponents() {
        leftContainer.getStyleClass().add("side-menu");
        centerContainer.getStyleClass().add("big-vbox");

        for (Button b : Arrays.asList(returnButton, newGridButton, saveButton)) {
            b.getStyleClass().add("big-button");
        }

        selectedBrush.setFixedCellSize(Config.LIST_CELL_HEIGHT);
    }

    /**
     * Sets the event handlers for the 3 buttons and 1 canvas.
     * <p>
     * Hints:
     * The save button should save the current LevelEditorCanvas to file.
     * The new grid button should change the LevelEditorCanvas size based on the entered values
     * The return button should switch back to the main menu scene
     * The LevelEditorCanvas, upon mouse click, should call {@link LevelEditorCanvas#setTile(Brush, double, double)},
     * passing in the currently selected brush and mouse click coordinates
     */
    private void setCallbacks() {
        saveButton.setOnAction(event -> levelEditor.saveToFile());
        newGridButton.setOnAction(event -> levelEditor.changeSize(rowField.getValue(), colField.getValue()));
        returnButton.setOnAction(event -> SceneManager.getInstance().showMainMenuScene());
        levelEditor.setOnMouseClicked(event -> levelEditor.setTile(selectedBrush.getSelectionModel().getSelectedItem(), event.getX(), event.getY()));
    }
}
