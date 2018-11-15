package com.rokid.ai.sdkdemo.model;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.rokid.ai.basic.util.FileUtil;
import com.rokid.ai.basic.util.Logger;

import java.io.File;
import java.io.IOException;

/**
 * model接口 -> 媒体播放控制实现类
 */
public class MediaPlayManagerImpl implements IMediaPlayManager {


    private static final int REPLAY_MUSIC = 111;

    private MediaPlayer mMediaPlayer;
    private Context mContext;
    private boolean mAudioPalying;

    public MediaPlayManagerImpl(Context context) {

        mMediaPlayer = new MediaPlayer();
        this.mContext = context;
    }

    private Handler mPlayHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg != null && msg.what == REPLAY_MUSIC) {
                String fileName = (String) msg.obj;
                boolean sd = msg.arg1 == 1;
                playSdCardSource(fileName, sd);
            }
        }
    };

    @Override
    public void startPlayMedia() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            String name = "test.mp3";
            copyMusicFile(name);
            boolean useSdCard = false;
            File file = new File(Environment.getExternalStorageDirectory(), name);
            if (file.exists() && file.isFile()) {
                useSdCard = true;
            }
            mAudioPalying = true;
            playSdCardSource(name, useSdCard);
        }
    }

    @Override
    public void stopPlayMedia() {
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mAudioPalying = false;
        }
        if (mPlayHandler != null) {
            mPlayHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void releaseMedia() {
        if(mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        if (mPlayHandler != null) {
            mPlayHandler.removeCallbacksAndMessages(null);
        }
    }

    public void copyMusicFile(String name) {
        if (!TextUtils.isEmpty(name)) {
            String newPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + name;
            FileUtil.copyFileFromAssets(mContext, name, newPath);
        }
    }


    /**
     * 播放SD卡根目录的文件
     *
     * @param fileName 文件名
     */
    private void playSdCardSource(final String fileName, final boolean isSdCard) {
        if (TextUtils.isEmpty(fileName)) {
            return;
        }
        try {
            mMediaPlayer.reset();
            if (isSdCard) {
                File mFile = new File(Environment.getExternalStorageDirectory(), fileName);
                Logger.d("MediaPlayManagerImpl", "playSdCardSource() SdCard soure = " + mFile.getPath() );
                mMediaPlayer.setDataSource(mFile.getPath());
            } else {
                AssetManager assetManager = mContext.getAssets();
                AssetFileDescriptor fileDescriptor = assetManager.openFd(fileName);

                mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                        fileDescriptor.getStartOffset(),
                        fileDescriptor.getLength());

                Logger.d("MediaPlayManagerImpl", "playSdCardSource() Assets soure = " + fileName );
            }
            mMediaPlayer.prepare();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Message msg = new Message();
                    msg.obj = fileName;
                    msg.what = REPLAY_MUSIC;
                    msg.arg1 = isSdCard ? 1:0;

                    if (mPlayHandler != null) {
                        mPlayHandler.removeMessages(REPLAY_MUSIC);
                        mPlayHandler.sendMessageDelayed(msg, 2000);
                    }
                }
            });

            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
