package com.bjdodson.pocketbox.upnp.statemachine;

import java.net.URI;

import org.teleal.cling.support.avtransport.impl.state.AbstractState;
import org.teleal.cling.support.avtransport.impl.state.Playing;
import org.teleal.cling.support.model.AVTransport;
import org.teleal.cling.support.model.SeekMode;

import android.media.MediaPlayer;
import android.util.Log;

import com.bjdodson.pocketbox.upnp.MediaRenderer;
import com.bjdodson.pocketbox.upnp.PlaylistManagerService;

public class PBPlaying extends Playing<AVTransport> {
	private static final String TAG = "jinzora";
	
    public PBPlaying(AVTransport transport) {
        super(transport);
    }

    @Override
    public void onEntry() {
        super.onEntry();

        final MediaPlayer player = MediaRenderer.getInstance().getMediaPlayer();
        if (!player.isPlaying()) {
        	// we are playing if we arrive from Paused state. Otherwise, a track
        	// has been set for us to play.
	        new Thread() {
	        	public void run() {
	        		try {
	                	player.prepare(); // playback started in onPrepareListener
	                } catch (Exception e) {
	                	Log.e(TAG, "Error playing track", e);
	                }
	        	};
	        }.start();
        }
    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {
    	Log.d(TAG, "called Playing::setTransportURI with " + uri);
    	if (!PlaylistManagerService.META_PLAYLIST_CHANGED.equals(metaData)) {
    		PlaylistManagerService pmService = MediaRenderer.getInstance().getPlaylistManager();
    		pmService.setAVTransportURI(getTransport().getInstanceId(), uri.toString(), metaData);
    	}
    	
        return PBPlaying.class;
    }

    @Override
    public Class<? extends AbstractState> stop() {
        // Stop playing!
    	Log.d(TAG, "Playing::stop called");
    	MediaPlayer player = MediaRenderer.getInstance().getMediaPlayer();
    	if (player.isPlaying()) {
    		player.stop();
    	}
        return PBStopped.class;
    }

	@Override
	public Class<? extends AbstractState> next() {
		Log.d(TAG, "Playing::next called");
		return PBTransitionHelpers.next(this, PBPlaying.class);
	}

	@Override
	public Class<? extends AbstractState> pause() {
		Log.d(TAG, "Playing::pause called");
		return PBPaused.class;
	}

	@Override
	public Class<? extends AbstractState> play(String speed) {
		Log.d(TAG, "Playing::play called");
		return null;
	}

	@Override
	public Class<? extends AbstractState> previous() {
		Log.d(TAG, "Playing::prev called");
		return null;
	}

	@Override
	public Class<? extends AbstractState> seek(SeekMode unit, String target) {
		if (unit.equals(SeekMode.REL_TIME)) {
			MediaPlayer player = MediaRenderer.getInstance().getMediaPlayer();
			player.seekTo(PBTransitionHelpers.timeInMS(target));
		}
		return null;
	}
}