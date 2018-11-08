import com.sun.javafx.fxml.PropertyNotFoundException;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import viewmodel.LevelEditorCanvas;
import viewmodel.SceneManager;
import viewmodel.customNodes.NumberTextField;
import viewmodel.panes.LevelEditorPane;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class MarkingSchemeTest extends ApplicationTest {
    @BeforeAll
    static void setup() {
        System.out.println();
    }

    @Override
    @Start
    public void start(Stage stage) {
        Platform.runLater(() -> SceneManager.getInstance().setStage(stage));
        Platform.runLater(() -> SceneManager.getInstance().showMainMenuScene());

        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);
    }

    private Optional<Stage> getTopModalStage() {
        final List<Window> allWindows = new ArrayList<>(robotContext().getWindowFinder().listWindows());
        return Optional.ofNullable(
                (Stage) allWindows.stream()
                        .filter(it -> it instanceof Stage)
                        .filter(it -> ((Stage) it)
                                .getModality() == Modality.APPLICATION_MODAL)
                        .findFirst()
                        .orElse(null));
    }

    @Test
    void testCreatingNewMaps() {
        System.out.println("Running MarkingSchemeTest::testCreatingNewMaps()");
        System.out.println("\tCreating new maps: From the level editor pane");

        // click on level editor button
        {
            System.out.println("Initialization Stage");

            Parent currentRoot = SceneManager.getInstance().getStage().getScene().getRoot();

            final Node targetVBox = currentRoot.getChildrenUnmodifiable().stream()
                    .filter(it -> it instanceof VBox && !((VBox) it).getChildrenUnmodifiable().isEmpty())
                    .findFirst()
                    .orElseThrow(PropertyNotFoundException::new);
            final Node targetButton = ((VBox) targetVBox).getChildrenUnmodifiable().stream()
                    .filter(it -> it instanceof Button && ((Button) it).getText().equals("Level Editor"))
                    .findFirst()
                    .orElseThrow(PropertyNotFoundException::new);
            clickOn(targetButton);

            // wait for it to switch scenes
            WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);
        }

        Parent currentRoot = SceneManager.getInstance().getStage().getScene().getRoot();
        assertTrue(currentRoot instanceof LevelEditorPane);

        // retrieve all nodes
        final Node leftVBoxNode = currentRoot.getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof VBox && ((VBox) it).getChildren().size() == 6)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        final Node listViewNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof ListView)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        final Node rowBoxNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof BorderPane && ((Label) ((BorderPane) it).getLeft()).getText().equals("Rows"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        final Node colBoxNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof BorderPane && ((Label) ((BorderPane) it).getLeft()).getText().equals("Columns"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        final Node newGridNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("New Grid"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        final Node saveNode = ((VBox) leftVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Button && ((Button) it).getText().equals("Save"))
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        final Node centerVBoxNode = currentRoot.getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof VBox && ((VBox) it).getChildren().size() == 1)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);
        final Node canvasNode = ((VBox) centerVBoxNode).getChildrenUnmodifiable().stream()
                .filter(it -> it instanceof Canvas)
                .findFirst()
                .orElseThrow(PropertyNotFoundException::new);

        // a.
        {
            System.out.println("Stage A: Select brushes and draw element on canvas");

            final LevelEditorCanvas canvas = (LevelEditorCanvas) canvasNode;

            @SuppressWarnings("unchecked") final ListView<LevelEditorCanvas.Brush> listView = (ListView<LevelEditorCanvas.Brush>) listViewNode;
            for (int i = 0; i < listView.getItems().size(); ++i) {
                final LevelEditorCanvas.Brush brush = listView.getItems().get(i);

                listView.getSelectionModel().clearAndSelect(i);
                clickOn(offset(canvasNode, -64.0, -64.0));

                Class<?> clazz = LevelEditorCanvas.class;
                try {
                    final Field mapField = clazz.getDeclaredField("map");
                    mapField.setAccessible(true);
                    final LevelEditorCanvas.Brush[][] map = (LevelEditorCanvas.Brush[][]) mapField.get(canvas);

                    assertEquals(brush, map[0][0]);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    fail(e.getMessage());
                }
            }
        }

        // b.
        {
            System.out.println("Stage B: Move Player-on-Destination to Player-on-Tile");

            final LevelEditorCanvas canvas = (LevelEditorCanvas) canvasNode;

            @SuppressWarnings("unchecked") final ListView<LevelEditorCanvas.Brush> listView = (ListView<LevelEditorCanvas.Brush>) listViewNode;

            listView.getSelectionModel().select(LevelEditorCanvas.Brush.PLAYER_ON_DEST);
            clickOn(offset(canvasNode, -64.0, -64.0));

            listView.getSelectionModel().select(LevelEditorCanvas.Brush.PLAYER_ON_TILE);
            clickOn(offset(canvasNode, 64.0, 64.0));

            Class<?> clazz = LevelEditorCanvas.class;
            try {
                final Field mapField = clazz.getDeclaredField("map");
                mapField.setAccessible(true);
                final LevelEditorCanvas.Brush[][] map = (LevelEditorCanvas.Brush[][]) mapField.get(canvas);

                assertEquals(LevelEditorCanvas.Brush.DEST, map[0][0]);
                assertEquals(LevelEditorCanvas.Brush.PLAYER_ON_TILE, map[4][4]);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail(e.getMessage());
            }
        }

        // c.
        {
            System.out.println("Stage C: Move Player-on-Tile");

            final LevelEditorCanvas canvas = (LevelEditorCanvas) canvasNode;

            @SuppressWarnings("unchecked") final ListView<LevelEditorCanvas.Brush> listView = (ListView<LevelEditorCanvas.Brush>) listViewNode;

            listView.getSelectionModel().select(LevelEditorCanvas.Brush.PLAYER_ON_TILE);
            clickOn(offset(canvasNode, -64.0, -64.0));
            clickOn(offset(canvasNode, 64.0, 64.0));

            Class<?> clazz = LevelEditorCanvas.class;
            try {
                final Field mapField = clazz.getDeclaredField("map");
                mapField.setAccessible(true);
                final LevelEditorCanvas.Brush[][] map = (LevelEditorCanvas.Brush[][]) mapField.get(canvas);

                assertEquals(LevelEditorCanvas.Brush.TILE, map[0][0]);
                assertEquals(LevelEditorCanvas.Brush.PLAYER_ON_TILE, map[4][4]);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail(e.getMessage());
            }
        }

        // d.
        {
            System.out.println("Stage D: Place Player-on-Tile to Top-Left");

            @SuppressWarnings("unchecked") final ListView<LevelEditorCanvas.Brush> listView = (ListView<LevelEditorCanvas.Brush>) listViewNode;

            listView.getSelectionModel().select(LevelEditorCanvas.Brush.PLAYER_ON_TILE);
            clickOn(offset(canvasNode, -64.0, -64.0));
        }

        // e.
        {
            System.out.println("Stage E: Resize Map");

            final NumberTextField rowField = ((NumberTextField) ((BorderPane) rowBoxNode).getRight());
            final NumberTextField colField = ((NumberTextField) ((BorderPane) colBoxNode).getRight());
            final LevelEditorCanvas canvas = (LevelEditorCanvas) canvasNode;

            rowField.clear();
            colField.clear();
            rowField.replaceSelection("4");
            colField.replaceSelection("4");

            clickOn(newGridNode);

            Class<?> clazz = LevelEditorCanvas.class;
            try {
                final Field mapField = clazz.getDeclaredField("map");
                mapField.setAccessible(true);
                final LevelEditorCanvas.Brush[][] map = (LevelEditorCanvas.Brush[][]) mapField.get(canvas);

                assertEquals(4, map.length);
                for (LevelEditorCanvas.Brush[] b : map) {
                    assertEquals(4, b.length);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail(e.getMessage());
            }
        }

        // f.
        {
            System.out.println("Stage F: Crate-on-Tile and Player-on-Tile Coexistence");

            final LevelEditorCanvas canvas = (LevelEditorCanvas) canvasNode;

            @SuppressWarnings("unchecked") final ListView<LevelEditorCanvas.Brush> listView = (ListView<LevelEditorCanvas.Brush>) listViewNode;

            listView.getSelectionModel().select(LevelEditorCanvas.Brush.CRATE_ON_TILE);
            clickOn(offset(canvasNode, -48.0, -48.0));

            listView.getSelectionModel().select(LevelEditorCanvas.Brush.PLAYER_ON_TILE);
            clickOn(offset(canvasNode, 48.0, 48.0));

            Class<?> clazz = LevelEditorCanvas.class;
            try {
                final Field mapField = clazz.getDeclaredField("map");
                mapField.setAccessible(true);
                final LevelEditorCanvas.Brush[][] map = (LevelEditorCanvas.Brush[][]) mapField.get(canvas);

                assertEquals(LevelEditorCanvas.Brush.CRATE_ON_TILE, map[0][0]);
                assertEquals(LevelEditorCanvas.Brush.PLAYER_ON_TILE, map[3][3]);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail(e.getMessage());
            }
        }

        // g.
        {
            System.out.println("Stage G: Save Map with Unsatisfied Preconditions");

            final NumberTextField rowField = ((NumberTextField) ((BorderPane) rowBoxNode).getRight());
            final NumberTextField colField = ((NumberTextField) ((BorderPane) colBoxNode).getRight());
            @SuppressWarnings("unchecked") final ListView<LevelEditorCanvas.Brush> listView = (ListView<LevelEditorCanvas.Brush>) listViewNode;

            Stage dialog = null;

            rowField.clear();
            colField.clear();
            rowField.replaceSelection("4");
            colField.replaceSelection("4");

            clickOn(newGridNode);
            clickOn(saveNode);

            dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
            assertNotNull(dialog);

            // TODO(Derppening): Match behavior with sample JAR

            type(KeyCode.ENTER);

            listView.getSelectionModel().select(LevelEditorCanvas.Brush.CRATE_ON_DEST);
            clickOn(offset(canvasNode, -48.0, -48.0));
            listView.getSelectionModel().select(LevelEditorCanvas.Brush.PLAYER_ON_DEST);
            clickOn(offset(canvasNode, -16.0, -16.0));
            listView.getSelectionModel().select(LevelEditorCanvas.Brush.CRATE_ON_TILE);
            clickOn(offset(canvasNode, 16.0, 16.0));
            listView.getSelectionModel().select(LevelEditorCanvas.Brush.DEST);
            clickOn(offset(canvasNode, 48.0, 48.0));

            clickOn(saveNode);

            dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
            assertNotNull(dialog);

            // TODO(Derppening): Match behavior with sample JAR

            type(KeyCode.ENTER);

            listView.getSelectionModel().select(LevelEditorCanvas.Brush.DEST);
            clickOn(offset(canvasNode, -16.0, -16.0));

            clickOn(saveNode);

            dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
            assertNotNull(dialog);

            // TODO(Derppening): Match behavior with sample JAR

            type(KeyCode.ENTER);

            rowField.clear();
            colField.clear();
            rowField.replaceSelection("3");
            colField.replaceSelection("3");

            clickOn(newGridNode);
            clickOn(saveNode);

            dialog = getTopModalStage().orElseThrow(NoSuchElementException::new);
            assertNotNull(dialog);

            // TODO(Derppening): Match behavior with sample JAR

            type(KeyCode.ENTER);
        }

        System.out.println("Completed MarkingSchemeTest::testCreatingNewMaps()");
        System.out.println();
    }
}
