
package com.raselahomed467.advancedmedia3;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

public class AdvancedVideoTimeline extends AndroidNonvisibleComponent {

    private final Context context;

    private SeekBar seekBar;
    private ViewGroup parentLayout;

    private AdvancedMedia3 player;

    private final Handler handler =
        new Handler(Looper.getMainLooper());

    private boolean isDragging = false;
    private boolean isEnabled = true;

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {

            updateProgress();

            if (seekBar != null) {
                handler.postDelayed(this, 500);
            }
        }
    };

    public AdvancedVideoTimeline(ComponentContainer container) {
        super(container.$form());

        context = container.$context();
    }

    // Create timeline inside Arrangement
    @SimpleFunction(description = "Creates the video timeline inside an Arrangement.")
    public boolean Create(AndroidViewComponent arrangement) {

        try {
            View parent = arrangement.getView();

            if (!(parent instanceof ViewGroup)) {
                ErrorOccurred("Arrangement is not a ViewGroup.");
                return false;
            }

            parentLayout = (ViewGroup) parent;

            DestroyTimeline();

            seekBar = new SeekBar(context);
            seekBar.setMax(1000);
            seekBar.setProgress(0);
            seekBar.setEnabled(isEnabled);

            parentLayout.addView(
                seekBar,
                new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            );

            seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                        SeekBar bar,
                        int progress,
                        boolean fromUser
                    ) {

                        if (fromUser && player != null) {

                            long duration = player.GetDuration();

                            if (duration > 0) {

                                long position =
                                    (duration * progress) / 1000;

                                ProgressChanged(
                                    position,
                                    duration
                                );
                            }
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar bar) {
                        isDragging = true;
                        SeekStarted();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar bar) {

                        isDragging = false;

                        if (player != null) {

                            long duration = player.GetDuration();

                            if (duration > 0) {

                                long position =
                                    (duration * bar.getProgress()) / 1000;

                                player.SeekTo(position);
                                SeekFinished(position);
                            }
                        }
                    }
                }
            );

            handler.removeCallbacks(updateRunnable);
            handler.post(updateRunnable);

            TimelineCreated();

            return true;

        } catch (Exception e) {

            ErrorOccurred(e.getMessage());
            return false;
        }
    }

    // Connect timeline with player
    @SimpleFunction(description = "Connects this timeline to an AdvancedMedia3 player.")
    public void SetPlayer(AdvancedMedia3 advancedPlayer) {

        player = advancedPlayer;

        updateProgress();
    }

    // Enable or disable timeline
    @SimpleFunction(description = "Enables or disables timeline dragging.")
    public void SetEnabled(boolean enabled) {

        isEnabled = enabled;

        if (seekBar != null) {
            seekBar.setEnabled(enabled);
        }
    }

    // Update progress
    private void updateProgress() {

        if (seekBar == null || player == null || isDragging) {
            return;
        }

        long duration = player.GetDuration();
        long position = player.GetCurrentPosition();

        if (duration <= 0) {
            seekBar.setProgress(0);
            return;
        }

        int progress = (int) (
            (position * 1000) / duration
        );

        seekBar.setProgress(
            Math.max(0, Math.min(1000, progress))
        );
    }

    // Get current timeline progress
    @SimpleFunction(description = "Returns timeline progress from 0 to 1000.")
    public int GetProgress() {

        return seekBar != null
            ? seekBar.getProgress()
            : 0;
    }

    // Remove timeline
    @SimpleFunction(description = "Destroys the timeline view.")
    public void DestroyTimeline() {

        handler.removeCallbacks(updateRunnable);

        if (seekBar != null) {

            if (seekBar.getParent() instanceof ViewGroup) {

                ((ViewGroup) seekBar.getParent())
                    .removeView(seekBar);
            }

            seekBar = null;
        }

        parentLayout = null;
        isDragging = false;
    }

    // Events
    @SimpleEvent(description = "Fires when timeline is created.")
    public void TimelineCreated() {
        EventDispatcher.dispatchEvent(
            this,
            "TimelineCreated"
        );
    }

    @SimpleEvent(description = "Fires when user starts dragging timeline.")
    public void SeekStarted() {
        EventDispatcher.dispatchEvent(
            this,
            "SeekStarted"
        );
    }

    @SimpleEvent(description = "Fires when user finishes seeking.")
    public void SeekFinished(long positionMs) {
        EventDispatcher.dispatchEvent(
            this,
            "SeekFinished",
            positionMs
        );
    }

    @SimpleEvent(description = "Fires when timeline progress changes.")
    public void ProgressChanged(
        long positionMs,
        long durationMs
    ) {
        EventDispatcher.dispatchEvent(
            this,
            "ProgressChanged",
            positionMs,
            durationMs
        );
    }

    @SimpleEvent(description = "Fires when timeline encounters an error.")
    public void ErrorOccurred(String message) {
        EventDispatcher.dispatchEvent(
            this,
            "ErrorOccurred",
            message == null ? "Unknown error" : message
        );
    }
}
