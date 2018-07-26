package com.rokid.ai.sdkdemo.model;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.text.TextUtils;

import com.rokid.ai.audioai.util.FileUtil;
import com.rokid.ai.audioai.util.Logger;

import java.io.File;
import java.io.IOException;

/**
 * model接口 -> 媒体播放控制实现类
 */
public class MediaPlayManagerImpl implements IMediaPlayManager {


    private MediaPlayer mMediaPlayer;
    private Context mContext;



    public MediaPlayManagerImpl(Context context) {

        mMediaPlayer = new MediaPlayer();
        this.mContext = context;
    }

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
            playSdCardSource(name, useSdCard);
        }
    }

    @Override
    public void stopPlayMedia() {
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    @Override
    public void releaseMedia() {
        if(mMediaPlayer != null) {
            mMediaPlayer.release();
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
    private void playSdCardSource(String fileName, boolean isSdCard) {
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
            mMediaPlayer.setLooping(true);
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
