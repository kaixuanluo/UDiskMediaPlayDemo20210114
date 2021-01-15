package com.miudrive.udiskmediaplaydemo20210114;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;

public class BaseActivity extends AppCompatActivity implements MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnErrorListener {

    protected MediaPlayer mMediaPlayer;

    protected String url = "";
    //    protected String url = "/storage/7A2D-D648/BLACKPINK_265.mp4";
    protected Uri mUri;
    /**
     * 上次视频显示容器
     */
    protected Surface mLastSurface;

    protected String TAG = getClass().getSimpleName();

    /**
     * 当前播放器是否准备好了
     */
    protected boolean mIsPrepared;
    /**
     * 当前播放器是否播放完毕了
     */
    private boolean mIsComplete;
    /**
     * 当前播放器是否播放错误了。
     */
    private boolean mIsError;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUri = Uri.parse("android.resource://" + getPackageName() + "/raw/test");

        verifyStoragePermissions();

    }

    public void verifyStoragePermissions() {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(this,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 定义 请求返回码
     */
    public static final int IMPORT_REQUEST_CODE = 10005;

    public void openFileManager() {

        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            //设置类型，我这里是任意类型，可以过滤文件类型
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, IMPORT_REQUEST_CODE);
        } catch (Exception e) {
            Log.d(TAG, "openFileManager: e:" + e);
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Log.d(TAG, "onActivityResult: uri:" + uri);
                if (uri == null) {
                    Log.d(TAG, "onActivityResult: uri == null");
                } else {
                    String path = getPath(this, uri);
                    Log.d(TAG, "onActivityResult: path:" + path);
                    if (path != null) {
                        File file = new File(path);
                        if (file.exists()) {
                            String upLoadFilePath = file.toString();
                            String upLoadFileName = file.getName();
                            url = file.getPath();
                            mUri = Uri.fromFile(file);
                            beforePrepareWork();
                            startPrepare();
                        }
                    }
                }
            }
            Log.e("导入失败", "");
        } else if (requestCode == 1) {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    protected void startPrepare() {
        //初始化播放器
        try {
            //必须在surface创建后才能初始化MediaPlayer, 否则不会显示图像
            //在 surface 的 surfaceCreated 方法里面调用初始化播放器
            //判断当前播放器是否为空，当前播放器只允许存在一个，置空播放器后才能再次实例化
            if (mMediaPlayer == null) {
                Log.d(TAG, "startPrepare() mMediaPlayer == null ... ");
                //实例化播放器
                mMediaPlayer = new MediaPlayer();
                //设置显示视频显示在SurfaceView上
                try {
                    //设置 音频类型
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    //设置 播放面板
                    mMediaPlayer.setSurface(mLastSurface);
                    //设置 播放器 准备状态的监听
                    mMediaPlayer.setOnPreparedListener(this);
                    //设置 播放器 播放完成的监听
                    mMediaPlayer.setOnCompletionListener(this);
                    //设置 播放器 缓冲中的监听
                    mMediaPlayer.setOnBufferingUpdateListener(this);
                    //设置 播放器 尺寸变化的监听
                    mMediaPlayer.setOnVideoSizeChangedListener(this);
                    //设置 播放器 播放错误的监听
                    mMediaPlayer.setOnErrorListener(this);
                } catch (Exception e) {
                    //实例化 播放器失败了，不做任何操作，也无法做操作
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "mMediaPlayer.setDataSource(musicCurrentPlay.getTrackUrl()); begin ... ");
            //设置 播放器准备成功为 假
            mIsPrepared = false;
            //设置 播放器播放完成为 假
            mIsComplete = false;
            //设置 播放器播放出错了为 假
            mIsError = false;
            Log.d(TAG, "mMediaPlayer.setDataSource(url); " + url);
            Log.d(TAG, "mMediaPlayer.setDataSource(uri); " + mUri);
            //设置 播放器的播放源
//            mMediaPlayer.setDataSource(url);
            mMediaPlayer.setDataSource(this, mUri);
            Log.d(TAG, "mMediaPlayer.setDataSource(musicCurrentPlay.getTrackUrl()); callbackEnd ... ");
            Log.d(TAG, "mMediaPlayer.prepareAsync(); ... ");
            //调用 播放器的异步准备
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
            //播放器调用 prepareAsync 异常了。
            Log.d(TAG, " catch (Exception e) 1 ... " + e.getMessage());
            Log.d(TAG, " catch (Exception e) 2 ... " + e.getCause());
            Log.d(TAG, " catch (Exception e) 3 ... " + e.toString());
            //设置 播放器 出错了
            mIsError = true;
            //回调 播放器错误给 主线程
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mIsPrepared = true;
        Log.d(TAG, "onPrepared: onPrepared");
//判断 播放器是否为空
        if (mp == null) {
            Log.d(TAG, "playUrlWithProgress player == null ... ");
        } else {
            Log.d(TAG, "playUrlWithProgress surfaceDestroyPosition == 0 ... ");
            //调用 开始播放
            mp.start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    /**
     * 每次的重新播放都会走reset(） 导致多次点击 会出现阻塞Ui的情况，导致我们的系统直接ANR掉，
     * 因此 我们在做MediaPlayer时，都需要在reset()方法前调用一次stop()，
     * 停止当前的音频这样，就避免了多次快速点击而导致的UI阻塞问题。
     */
    protected void beforePrepareWork() {
        resetAll();
    }

    private void resetAll() {
        if (mMediaPlayer == null) {
        } else {
            Log.d(TAG, "playPreWork() begin ... ");
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            Log.d(TAG, "playPreWork() callbackEnd ... ");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetAll();
    }
}