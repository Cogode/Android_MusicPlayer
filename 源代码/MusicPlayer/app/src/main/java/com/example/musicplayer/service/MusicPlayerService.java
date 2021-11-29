package com.example.musicplayer.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.io.FileDescriptor;
import java.io.IOException;

public class MusicPlayerService extends Service {
    private MusicPlayerBinder mBinder;

    public static class MusicPlayerBinder extends Binder {
        private MediaPlayer player;

        public MusicPlayerBinder() {
            this.player = new MediaPlayer();
        }

        public void setLooping(boolean looping) {
            player.setLooping(looping);
        }

        public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
            player.setOnPreparedListener(listener);
        }

        public void start() {
            player.start();
        }

        public void seekTo(int position) {
            player.seekTo(position);
        }

        public void pause() {
            player.pause();
        }

        public void stop() {
            player.stop();
        }

        public void reset() {
            player.reset();
        }

        public void setDataSource(FileDescriptor fd, long offset, long length) {
            try {
                player.setDataSource(fd, offset, length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void prepare() {
            try {
                player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
            player.setOnCompletionListener(listener);
        }

        public int getDuration() {
            return player.getDuration();
        }

        public int getCurrentPosition() {
            return player.getCurrentPosition();
        }

        public void release() {
            player.release();
        }
    }

    public MusicPlayerService() {
    }

    @Override
    public void onCreate() {
        mBinder = new MusicPlayerBinder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}