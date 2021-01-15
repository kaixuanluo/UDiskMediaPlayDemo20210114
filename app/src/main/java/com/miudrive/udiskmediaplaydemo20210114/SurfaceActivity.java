package com.miudrive.udiskmediaplaydemo20210114;

import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;

/**
 * @author created by luokaixuan
 * @date 2021/1/14
 * 这个类是用来干嘛的
 */
public class SurfaceActivity extends BaseActivity implements SurfaceHolder.Callback {

    private SurfaceView mSv;
    private SurfaceHolder mSurfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_surface);

        mSv = findViewById(R.id.sv);

        findViewById(R.id.select_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileManager();
            }
        });

        mSv.setKeepScreenOn(true);
        //获取 画布容器
        mSurfaceHolder = mSv.getHolder();
        //设置 画布为透明
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        //设置 画布显示为常亮
        mSurfaceHolder.setKeepScreenOn(true);
        //设置 画布生命周期回调
        mSurfaceHolder.addCallback(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (mIsPrepared) {
            mMediaPlayer.setSurface(holder.getSurface());
            mMediaPlayer.start();
        } else {
            if (mLastSurface == null) {
                mLastSurface = holder.getSurface();
            }
            beforePrepareWork();
            startPrepare();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

}
