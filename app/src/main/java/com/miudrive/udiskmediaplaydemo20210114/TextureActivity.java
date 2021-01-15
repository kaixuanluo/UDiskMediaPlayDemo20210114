package com.miudrive.udiskmediaplaydemo20210114;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import java.io.File;

/**
 * @author created by luokaixuan
 * @date 2021/1/14
 * 这个类是用来干嘛的
 */
public class TextureActivity extends BaseActivity implements TextureView.SurfaceTextureListener {

    /**
     * 视频显示容器
     */
    private TextureView mTextureView;
    /**
     * 上次显示的视频容器
     * 当前activity切换到后台，当前 SurfaceTexture 会被销毁，activity 重新显示，重新使用上次的 SurfaceTexture
     * 让视频接着播放
     */
    private SurfaceTexture mLastSurfaceTexture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_texture);

        mTextureView = findViewById(R.id.tv);
        findViewById(R.id.select_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileManager();
            }
        });

        findViewById(R.id.surface_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TextureActivity.this, SurfaceActivity.class));
            }
        });

        //保持 屏幕常亮
        mTextureView.setKeepScreenOn(true);
        //设置 生命周期
        mTextureView.setSurfaceTextureListener(this);

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
//判断 上次的 视频容器是否是空的，当前界面不销毁，mLastSurfaceTexture 不会为空
        if (mLastSurfaceTexture == null) {
            //上次的 视频容器是空的
            //当前界面不销毁，mLastSurfaceTexture 不会为空 这里只会初始化一次
            //赋值 上次的视频容器为当前视频容器
            mLastSurfaceTexture = surfaceTexture;
            //实例化 视频画布
            mLastSurface = new Surface(surfaceTexture);
        } else {
            //上次的 视频容器不为空，刷新视频容器
            mTextureView.setSurfaceTexture(mLastSurfaceTexture);
        }

        if (mIsPrepared) {
            mMediaPlayer.setSurface(mLastSurface);
        } else {
            File file = new File(url);
            mUri = Uri.fromFile(file);
            if (file.exists()) {
                Log.d(TAG, "onCreate: file.exists()");
                String upLoadFileName = file.getName();
//            url = file.getPath();
            } else {
                Log.d(TAG, "onCreate: file not exists()");
            }
            beforePrepareWork();
            startPrepare();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "onSurfaceTextureDestroyed: surface:" + surface);
        //销毁了，通知 视频播放器 播放视图销毁了
        //记录销毁的视频播放器视图
        mLastSurfaceTexture = surface;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
