
package com.raselahomed467.advancedmedia3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import androidx.media3.exoplayer.ExoPlayer;

import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

public class AdvancedMedia3 extends AndroidNonvisibleComponent {

    private final ComponentContainer container;
    private final Context context;

    private ExoPlayer exoPlayer;
    private PlayerView playerView;
    private ViewGroup parentLayout;

    private String currentUrl = "";

    public AdvancedMedia3(ComponentContainer container) {
        super(container.$form());

        this.container = container;
        this.context = container.$context();
    }

    // Create Player inside Arrangement
    @SimpleFunction(description = "Creates the video player inside an Arrangement.")
    public boolean Create(AndroidViewComponent arrangement) {

        try {
            View parent = arrangement.getView();

            if (!(parent instanceof ViewGroup)) {
                ErrorOccurred("Arrangement is not a ViewGroup.");
                return false;
            }

            parentLayout = (ViewGroup) parent;

            if (exoPlayer != null) {
                ReleasePlayer();
            }

            exoPlayer = new ExoPlayer.Builder(context).build();

            playerView = new PlayerView(context);
            playerView.setPlayer(exoPlayer);
            playerView.setUseController(true);
            playerView.setShowBuffering(
                PlayerView.SHOW_BUFFERING_WHEN_PLAYING
            );

            playerView.setResizeMode(
                AspectRatioFrameLayout.RESIZE_MODE_FIT
            );

            parentLayout.addView(
                playerView,
                new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            );

            exoPlayer.addListener(new Player.Listener() {

                @Override
                public void onPlaybackStateChanged(int state) {

                    String stateName;

                    switch (state) {
                        case Player.STATE_IDLE:
                            stateName = "IDLE";
                            break;

                        case Player.STATE_BUFFERING:
                            stateName = "BUFFERING";
                            break;

                        case Player.STATE_READY:
                            stateName = "READY";
                            break;

                        case Player.STATE_ENDED:
                            stateName = "ENDED";
                            break;

                        default:
                            stateName = "UNKNOWN";
                            break;
                    }

                    PlaybackStateChanged(stateName);
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    PlayingChanged(isPlaying);
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    ErrorOccurred(
                        error.errorCodeName + ": " + error.getMessage()
                    );
                }
            });

            PlayerCreated();
            return true;

        } catch (Exception e) {
            ErrorOccurred(e.getMessage());
            return false;
        }
    }

    // Load video URL
    @SimpleFunction(description = "Loads a video from a URL.")
    public void LoadUrl(String url) {

        if (exoPlayer == null) {
            ErrorOccurred("Create the player first.");
            return;
        }

        if (url == null || url.trim().isEmpty()) {
            ErrorOccurred("Video URL is empty.");
            return;
        }

        try {
            currentUrl = url.trim();

            MediaItem mediaItem = MediaItem.fromUri(currentUrl);

            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();

        } catch (Exception e) {
            ErrorOccurred(e.getMessage());
        }
    }

    // Play video
    @SimpleFunction(description = "Starts or resumes video playback.")
    public void Play() {
        if (exoPlayer != null) {
            exoPlayer.play();
        }
    }

    // Pause video
    @SimpleFunction(description = "Pauses video playback.")
    public void Pause() {
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    // Toggle playback
    @SimpleFunction(description = "Toggles Play and Pause.")
    public void PlayPause() {
        if (exoPlayer == null) return;

        if (exoPlayer.isPlaying()) {
            exoPlayer.pause();
        } else {
            exoPlayer.play();
        }
    }

    // Seek video
    @SimpleFunction(description = "Seeks to a position in milliseconds.")
    public void SeekTo(long positionMs) {
        if (exoPlayer != null) {
            exoPlayer.seekTo(Math.max(0, positionMs));
        }
    }

    // Current position
    @SimpleFunction(description = "Returns the current playback position in milliseconds.")
    public long GetCurrentPosition() {
        return exoPlayer != null
            ? exoPlayer.getCurrentPosition()
            : 0;
    }

    // Video duration
    @SimpleFunction(description = "Returns video duration in milliseconds.")
    public long GetDuration() {
        if (exoPlayer == null) return 0;

        long duration = exoPlayer.getDuration();

        return duration == Player.TIME_UNSET ? 0 : duration;
    }

    // Playing status
    @SimpleFunction(description = "Checks whether the video is playing.")
    public boolean IsPlaying() {
        return exoPlayer != null && exoPlayer.isPlaying();
    }

    // Enable or disable default controller
    @SimpleFunction(description = "Enables or disables the player controller.")
    public void SetControllerEnabled(boolean enabled) {
        if (playerView != null) {
            playerView.setUseController(enabled);
        }
    }

    // Resize modes: FIT, FILL, ZOOM
    @SimpleFunction(description = "Sets video resize mode: FIT, FILL or ZOOM.")
    public void SetResizeMode(String mode) {

        if (playerView == null || mode == null) return;

        switch (mode.toUpperCase()) {

            case "FILL":
                playerView.setResizeMode(
                    AspectRatioFrameLayout.RESIZE_MODE_FILL
                );
                break;

            case "ZOOM":
                playerView.setResizeMode(
                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                );
                break;

            default:
                playerView.setResizeMode(
                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                );
                break;
        }
    }

    // Release player resources
    @SimpleFunction(description = "Releases the video player resources.")
    public void ReleasePlayer() {

        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }

        if (playerView != null) {

            if (playerView.getParent() instanceof ViewGroup) {
                ((ViewGroup) playerView.getParent())
                    .removeView(playerView);
            }

            playerView.setPlayer(null);
            playerView = null;
        }

        parentLayout = null;
        currentUrl = "";
    }

    // Events
    @SimpleEvent(description = "Fires when the player is created.")
    public void PlayerCreated() {
        EventDispatcher.dispatchEvent(this, "PlayerCreated");
    }

    @SimpleEvent(description = "Fires when playback state changes.")
    public void PlaybackStateChanged(String state) {
        EventDispatcher.dispatchEvent(
            this,
            "PlaybackStateChanged",
            state
        );
    }

    @SimpleEvent(description = "Fires when playing status changes.")
    public void PlayingChanged(boolean isPlaying) {
        EventDispatcher.dispatchEvent(
            this,
            "PlayingChanged",
            isPlaying
        );
    }

    @SimpleEvent(description = "Fires when a playback error occurs.")
    public void ErrorOccurred(String message) {
        EventDispatcher.dispatchEvent(
            this,
            "ErrorOccurred",
            message == null ? "Unknown error" : message
        );
    }
}
