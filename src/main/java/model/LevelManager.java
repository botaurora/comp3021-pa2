package model;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Exceptions.InvalidMapException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Keeps track of the current GameLevel and level name. Also tracks information
 * that's related to this game level but not specific to the map of the game
 * level, i.e. how long the player has been playing the level, how many restarts, etc.
 */
public class LevelManager {
    private static final LevelManager ourInstance = new LevelManager();
    private final ObservableList<String> levelNames = FXCollections.observableArrayList();
    private final StringProperty curLevelNameProperty = new SimpleStringProperty();
    private final IntegerProperty curGameLevelExistedDuration = new SimpleIntegerProperty();
    private final IntegerProperty curGameLevelNumRestarts = new SimpleIntegerProperty();
    private final GameLevel gameLevel = new GameLevel();
    private Timer t = new Timer(true); //declare as daemon, so application exits when Platform.exit is called
    private String mapDirectory = "";

    private LevelManager() {
    }

    public static LevelManager getInstance() {
        return ourInstance;
    }

    public void setMapDirectory(String mapDirectory) {
        this.mapDirectory = mapDirectory;
    }

    public GameLevel getGameLevel() {
        return gameLevel;
    }

    /**
     * Clears and loads the the level names into {@link #levelNames}. Can be done succinctly using
     * Streams, Predicates, and Consumers. Load the files by alphabetical sorted order.
     * <p>
     * Hints: Files.walk(Paths.get(mapDirectory), 1) returns a Stream of files 1 folder deep
     */
    public void loadLevelNamesFromDisk() {
        // TODO(Derppening): Check
        try (Stream<Path> stream = Files.walk(Paths.get(mapDirectory), 1)) {
            List<String> files = stream
                    .filter(f -> f.toFile().isFile())
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(it -> it.endsWith(".txt"))
                    .sorted(String::compareTo)
                    .collect(Collectors.toList());

            levelNames.addAll(files);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        }
    }

    public ObservableList<String> getLevelNames() {
        return levelNames;
    }

    public StringProperty currentLevelNameProperty() {
        return curLevelNameProperty;
    }

    /**
     * Sets the current level based on the level name (i.e. the map filename). Although the level existed duration
     * should be reset, the timer should not be started yet.
     * <p>
     * Hints: don't forget to update the level name and existed duration properties, and load the map for
     * the GameLevel object.
     *
     * @param levelName The level name to set
     * @throws InvalidMapException if the map was invalid
     */
    public void setLevel(String levelName) throws InvalidMapException {
        // TODO(Derppening): Check

        // Reset everything
        resetLevelTimer();
        resetNumRestarts();

        if (levelName == null) {
            // what will i do? :(
            throw new NullPointerException("levelName is null");
        }

        try {
            gameLevel.loadMap(Paths.get(mapDirectory, levelName).normalize().toAbsolutePath().toString());
        } catch (InvalidMapException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Starts the timer, which updates {@link #curGameLevelExistedDuration} every second.
     * <p>
     * Hint: {@link java.util.Timer#scheduleAtFixedRate(TimerTask, long, long)} and
     * {@link javafx.application.Platform#runLater(Runnable)} are required
     */
    public void startLevelTimer() {
        // TODO(Derppening): Check
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                curGameLevelExistedDuration.add(1);
            }
        }, 0, 1000);
        Platform.runLater(this::startLevelTimer);
    }

    /**
     * Cancels the existing timer and assigns it to a new instance
     */
    public void resetLevelTimer() {
        t.cancel();
        t = new Timer(true);
    }

    /**
     * Increment the number of restarts the user has performed on the current GameLevel
     */
    public void incrementNumRestarts() {
        curGameLevelNumRestarts.set(curGameLevelNumRestarts.get() + 1);
    }

    /**
     * Reset the number of restarts the user has performed on the current GameLevel
     */
    public void resetNumRestarts() {
        curGameLevelNumRestarts.set(0);
    }

    /**
     * @return The name of the level which appears immediately after the current level name inside {@link #levelNames}.
     * If the current level is the last level, this function returns null. You may assume that the current level
     * name is always valid.
     */
    public String getNextLevelName() {
        // TODO(Derppening): Check

        int i = levelNames.indexOf(curLevelNameProperty.toString());
        if (i == -1) {
            throw new IllegalStateException("Current Level Name is not in Level Names list!");
        }

        if (i + 1 < levelNames.size()) {
            return levelNames.get(i + 1);
        } else {
            return null;
        }
    }

    public IntegerProperty curGameLevelExistedDurationProperty() {
        return curGameLevelExistedDuration;
    }

    public IntegerProperty curGameLevelNumRestartsProperty() {
        return curGameLevelNumRestarts;
    }
}

