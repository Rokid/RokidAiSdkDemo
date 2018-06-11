package com.rokid.ai.sdkdemo.model;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.text.TextUtils;

import com.rokid.ai.audioai.util.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * model接口 -> 媒体播放控制实现类
 */
public class MediaPlayManagerImpl implements IMediaPlayManager {


    private static final String TAG = "MediaPlayManagerImpl";

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
            copyFileFromAssets(mContext, name, newPath);
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


    /**
     * 从assets目录中复制文件
     *
     * @param context Context 使用CopyFiles类的Activity
     * @param oldPath String  原文件路径
     * @param newPath String  复制后路径
     */
    public void copyFileFromAssets(Context context, String oldPath, String newPath) {

        try {
            //如果是文件
            File newFile = new File(newPath);
            if (newFile.exists() && newFile.isFile()) {
                // 文件存在，不进行拷贝
                Logger.d(TAG, "copyFileFromAssets 文件存在： path = " + newPath);
            } else {
                // 文件不存在，进行拷贝
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(newFile);

                byte[] buffer = new byte[1024];
                int byteCount = 0;

                //循环从输入流读取 buffer字节
                while((byteCount = is.read(buffer)) != -1) {
                    //将读取的输入流写入到输出流
                    fos.write(buffer, 0, byteCount);
                }

                //刷新缓冲区
                fos.flush();
                is.close();
                fos.close();

                Logger.d(TAG, "copyFileFromAssets oldPath = " + oldPath + ", newPath = " + newPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
