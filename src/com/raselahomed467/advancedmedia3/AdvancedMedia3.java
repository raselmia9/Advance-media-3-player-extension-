
package com.raselahomed467.advancedmedia3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

import java.util.HashMap;
import java.util.Map;

public class AdvancedMedia3 extends AndroidNonvisibleComponent {

    private final ComponentContainer container;
    private final Context context;

    private ExoPlayer exoPlayer;
    private PlayerView playerView;
    private ViewGroup parentLayout;

    private String currentUrl = "";

    private final Map<String, String> requestHeaders =
        new HashMap<>();

    public AdvancedMedia3(ComponentContainer container) {
        super(container.$form());

        this.container = container;
        this.context = container.$context();
    }

    // Create Player
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
                parentLayout = (ViewGroup) parent;
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

    // Generic HTTP Header
    @SimpleFunction(description = "Adds or updates an HTTP request header.")
    public void SetHeader(String name, String value) {

        if (name == null || name.trim().isEmpty()) {
            ErrorOccurred("Header name is empty.");
            return;
        }

        if (value == null) {
            requestHeaders.remove(name.trim());
        } else {
            requestHeaders.put(name.trim(), value);
        }
    }

    // Common Headers
    @SimpleFunction(description = "Sets the Referer header.")
    public void SetReferer(String value) {
        SetHeader("Referer", value);
    }

    @SimpleFunction(description = "Sets the Cookie header.")
    public void SetCookie(String value) {
        SetHeader("Cookie", value);
    }

    @SimpleFunction(description = "Sets the User-Agent header.")
    public void SetUserAgent(String value) {
        SetHeader("User-Agent", value);
    }

    @SimpleFunction(description = "Sets the Origin header.")
    public void SetOrigin(String value) {
        SetHeader("Origin", value);
    }

    @SimpleFunction(description = "Sets the Authorization header.")
    public void SetAuthorization(String value) {
        SetHeader("Authorization", value);
    }

    @SimpleFunction(description = "Removes all HTTP request headers.")
    public void ClearHeaders() {
        requestHeaders.clear();
    }

    // Load URL with configured headers
    @SimpleFunction(description = "Loads a video URL using the configured HTTP headers.")
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

            DefaultHttpDataSource.Factory dataSourceFactory =
                new DefaultHttpDataSource.Factory();

            dataSourceFactory.setDefaultRequestProperties(
                new HashMap<>(requestHeaders)
            );

            DefaultMediaSourceFactory mediaSourceFactory =
                new DefaultMediaSourceFactory(context);

            mediaSourceFactory.setDataSourceFactory(
                dataSourceFactory
            );

            MediaItem mediaItem =
                MediaItem.fromUri(currentUrl);

            exoPlayer.setMediaSource(
                mediaSourceFactory.createMediaSource(mediaItem)
            );

            exoPlayer.prepare();

        } catch (Exception e) {
            ErrorOccurred(e.getMessage());
        }
    }

    @SimpleFunction(description = "Starts or resumes video playback.")
    public void Play() {
        if (exoPlayer != null) {
            exoPlayer.play();
        }
    }

    @SimpleFunction(description = "Pauses video playback.")
    public void Pause() {
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    @SimpleFunction(description = "Toggles Play and Pause.")
    public void PlayPause() {
        if (exoPlayer == null) return;

        if (exoPlayer.isPlaying()) {
            exoPlayer.pause();
        } else {
            exoPlayer.play();
        }
    }

    @SimpleFunction(description = "Seeks to a position in milliseconds.")
    public void SeekTo(long positionMs) {
        if (exoPlayer != null) {
            exoPlayer.seekTo(Math.max(0, positionMs));
        }
    }

    @SimpleFunction(description = "Returns current playback position in milliseconds.")
    public long GetCurrentPosition() {
        return exoPlayer != null
            ? exoPlayer.getCurrentPosition()
            : 0;
    }

    @SimpleFunction(description = "Returns video duration in milliseconds.")
    public long GetDuration() {
        if (exoPlayer == null) return 0;

        long duration = exoPlayer.getDuration();

        return duration == Player.TIME_UNSET ? 0 : duration;
    }

    @SimpleFunction(description = "Checks whether video is playing.")
    public boolean IsPlaying() {
        return exoPlayer != null && exoPlayer.isPlaying();
    }

    @SimpleFunction(description = "Enables or disables the player controller.")
    public void SetControllerEnabled(boolean enabled) {
        if (playerView != null) {
            playerView.setUseController(enabled);
        }
    }

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

    @SimpleFunction(description = "Releases video player resources.")
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
    @SimpleEvent(description = "Fires when player is created.")
    public void PlayerCreated() {
        EventDispatcher.dispatchEvent(this, "PlayerCreated");
    }

    @SimpleEvent(description = "Fires when playback state changes.")
    public void PlaybackStateChanged(String state) {
        EventDispatcher.dispatchEvent(
            this, "PlaybackStateChanged", state
        );
    }

    @SimpleEvent(description = "Fires when playing status changes.")
    public void PlayingChanged(boolean isPlaying) {
        EventDispatcher.dispatchEvent(
            this, "PlayingChanged", isPlaying
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
