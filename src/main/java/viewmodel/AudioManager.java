package viewmodel;

import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles audio related events.
 */
public class AudioManager {
    private static AudioManager instance = new AudioManager();
    //keep a reference to the sound until it finishes playing, to prevent GC from prematurely recollecting it
    private final Set<MediaPlayer> soundPool = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private boolean enabled = true;

    private AudioManager() {
    }

    public static AudioManager getInstance() {
        return instance;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Plays the sound. If disabled, simply return.
     * <p>
     * Hint:
     * {@link MediaPlayer#play()} and {@link MediaPlayer#dispose()} are used.
     * When creating a new MediaPlayer object, add it into the soundpool before playing it.
     * Also set a callback for when the sound has completed: remove it from the soundpool, and dispose of the
     * sound in a newly created thread using the dispose method.
     * <p>
     * Make sure to set any threads you create as daemon threads so that the application will exit cleanly.
     *
     * @param name the name of the sound file to be played, excluding .mp3
     */
    private void playFile(String name) {
        //TODO(Derppening): Check
        final URL audioUrl = Thread.currentThread().getContextClassLoader().getResource("assets/audio/" + name +".mp3");
        assert audioUrl != null;

        try {
            Media m = new Media(audioUrl.toURI().toString());

            try {
                MediaPlayer player = new MediaPlayer(m);

                player.onEndOfMediaProperty().setValue(() -> {
                    soundPool.remove(player);
                    Thread t = new Thread(player::dispose);
                    t.setDaemon(true);
                    t.start();
                });

                soundPool.add(player);
                player.play();
            } catch (MediaException e) {
                // not handled
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new IllegalStateException("Cannot load " + name + ".mp3");
        }
    }

    public void playMoveSound() {
        playFile("move");
    }

    public void playWinSound() {
        playFile("win");
    }

    public void playDeadlockSound() {
        playFile("deadlock");
    }
}
