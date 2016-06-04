package io.vov.vitamio.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.R;
import io.vov.vitamio.utils.ScreenResolution;

import static android.view.View.GONE;

/**
 * 针对段视频的播放组件，功能：
 * 1.竖屏播放，大小根据视频自适应。
 * 2.播放状态下地步提供横条显示进度。
 * 3.点击显示大进度条，可选择进度，再点击暂停，非暂停状态3s变回小进度条。
 */
public class ShortVideoView extends RelativeLayout {
    private static final int FADE_OUT = 0x10001;
    private static final int SHOW_PROGRESS = 0x10002;
    private static final int HIND_PROGRESS = 0x10003;
    public static final int VIDEO_LAYOUT_ORIGIN = 0;
    public static final int VIDEO_LAYOUT_SCALE = 1;
    public static final int VIDEO_LAYOUT_STRETCH = 2;
    public static final int VIDEO_LAYOUT_ZOOM = 3;
    public static final int VIDEO_LAYOUT_FIT_PARENT = 4;
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private static final int STATE_SUSPEND = 6;
    private static final int STATE_RESUME = 7;
    private static final int STATE_SUSPEND_UNSUPPORTED = 8;
    private static final int STATE_CONTROLLER_SIMPLE = 0;
    private static final int STATE_CONTROLLER_DETAILE = 1;
    private static final int STATE_CONTROLLER_PAUSED = 2;
    private static final int STATE_CONTROLLER_END = 3;
    private static final long MAX_PROGRESS = 1000;
    private static final int UPDATE_INTERVAL = 1000;
    private final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("mm:ss", Locale.CANADA);


    private boolean mHardwareDecoder = false;

    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;
    private int mControllerState = STATE_CONTROLLER_SIMPLE;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private int mCurrentBufferPercentage;
    private int mVideoLayout = VIDEO_LAYOUT_SCALE;
    private int mBufSize;
    private int mVideoChroma = MediaPlayer.VIDEOCHROMA_RGBA;
    private int mCurrentProgress;


    private float mVideoAspectRatio;
    private float mAspectRatio = 0;

    private long mSeekWhenPrepared; // recording the seek position while preparing
    private long mDuration;

    private int mVideoCurrentTime;

    private Uri mUri;
    private Map<String, String> mHeaders;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder = null;

    private MediaPlayer mMediaPlayer = null;

    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private MediaPlayer.OnErrorListener mOnErrorListener;
    private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    private MediaPlayer.OnTimedTextListener mOnTimedTextListener;
    private MediaPlayer.OnInfoListener mOnInfoListener;
    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;

    private View mMediaBufferingIndicator;
    private TextView mTvCurrentTime;
    private TextView mTvToltalTime;
    private SeekBar mSbProgress;
    private ProgressBar mPbProgress;
    private ImageView mIvFullScreen;
    private ImageView mIvPause;
    private View mTvRepeat;
    private LinearLayout mLayoutController;

    private Context mContext;

//    private

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            if (mMediaPlayer != null && mCurrentState == STATE_SUSPEND && mTargetState == STATE_RESUME) {
                mMediaPlayer.setDisplay(mSurfaceHolder);
                resume();
            } else {
                openVideo();
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0)
                    seekTo(mSeekWhenPrepared);
                start();
                // TODO 更改控制界面状态
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mSurfaceHolder = null;
            // TODO 更改控制界面状态
//            if (mMediaController != null)
//                mMediaController.hide();
            release(true);
        }
    };

    MediaPlayer.OnPreparedListener mBaseOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
//            Log.d("onPrepared");

            mCurrentState = STATE_PREPARED;
            // mTargetState = STATE_PLAYING;

            // Get the capabilities of the player for this stream
            //TODO mCanPause

            if (mOnPreparedListener != null)
                mOnPreparedListener.onPrepared(mMediaPlayer);

            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            mVideoAspectRatio = mp.getVideoAspectRatio();
            getDuration();
            mTvToltalTime.setText(TIME_FORMAT.format(new Date(mDuration)));
            // TODO 更改控制界面
//            if (mMediaController != null)
//                mMediaController.setEnabled(true);

            long seekToPosition = mSeekWhenPrepared;
            if (seekToPosition != 0)
                seekTo(seekToPosition);
            mControllerHandler.removeMessages(SHOW_PROGRESS);
            mControllerHandler.sendEmptyMessage(SHOW_PROGRESS);
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                setVideoLayout(mVideoLayout, mAspectRatio);
                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                    if (mTargetState == STATE_PLAYING) {
                        start();
                        // TODO 更改控制界面
//                        if (mMediaController != null)
//                            mMediaController.show();
                    } else if (!isPlaying() && (seekToPosition != 0 || getCurrentPosition() > 0)) {
                        // TODO 更改控制界面
//                        if (mMediaController != null)
//                            mMediaController.show(0);
                    }
                }
            } else if (mTargetState == STATE_PLAYING) {
                start();
            }
        }
    };

    MediaPlayer.OnVideoSizeChangedListener mBaseOnSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//            Log.d("onVideoSizeChanged: (%dx%d)", width, height);
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            mVideoAspectRatio = mp.getVideoAspectRatio();
            if (mVideoWidth != 0 && mVideoHeight != 0)
                setVideoLayout(mVideoLayout, mAspectRatio);
        }
    };

    private MediaPlayer.OnCompletionListener mBaseOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            mLayoutController.setVisibility(VISIBLE);
            mPbProgress.setVisibility(GONE);
            mIvPause.setVisibility(GONE);
            mTvRepeat.setVisibility(VISIBLE);
            mControllerHandler.removeMessages(HIND_PROGRESS);
            mControllerState = STATE_CONTROLLER_END;
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            // TODO 更改控制界面状态
//            if (mMediaController != null)
//                mMediaController.hide();
            if (mOnCompletionListener != null)
                mOnCompletionListener.onCompletion(mMediaPlayer);
        }
    };

    private MediaPlayer.OnErrorListener mBaseOnErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            // TODO 更改控制界面状态

            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err))
                    return true;
            }

            if (getWindowToken() != null) {
                int message = 0;
                message = framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK ? getResources().getIdentifier("VideoView_error_text_invalid_progressive_playback", "string", mContext.getPackageName()) : getResources().getIdentifier("VideoView_error_text_unknown", "string", mContext.getPackageName());

                new AlertDialog.Builder(mContext).setTitle(getResources().getIdentifier("VideoView_error_title", "string", mContext.getPackageName())).setMessage(message).setPositiveButton(getResources().getIdentifier("VideoView_error_button", "string", mContext.getPackageName()), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (mOnCompletionListener != null)
                            mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                }).setCancelable(false).show();
            }
            return true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBaseOnBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mCurrentBufferPercentage = percent;
            if (mOnBufferingUpdateListener != null)
                mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
        }
    };

    private MediaPlayer.OnInfoListener mBaseOnInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
//            Log.d("onInfo: (%d, %d)", what, extra);

            if (MediaPlayer.MEDIA_INFO_UNKNOW_TYPE == what) {
//                Log.e(" VITAMIO--TYPE_CHECK  stype  not include  onInfo mediaplayer unknow type ");
            }

            if (MediaPlayer.MEDIA_INFO_FILE_OPEN_OK == what) {
                long buffersize = mMediaPlayer.audioTrackInit();
                mMediaPlayer.audioInitedOk(buffersize);
            }

//            Log.d("onInfo: (%d, %d)", what, extra);

            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, what, extra);
            } else if (mMediaPlayer != null) {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    mMediaPlayer.pause();
                    if (mMediaBufferingIndicator != null)
                        mMediaBufferingIndicator.setVisibility(View.VISIBLE);
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    mMediaPlayer.start();
                    if (mMediaBufferingIndicator != null)
                        mMediaBufferingIndicator.setVisibility(GONE);
                }
            }
            return true;
        }
    };

    private MediaPlayer.OnSeekCompleteListener mBaseOnSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            if (mControllerState == STATE_CONTROLLER_PAUSED) {
                mMediaPlayer.pause();
            }
            if (mOnSeekCompleteListener != null)
                mOnSeekCompleteListener.onSeekComplete(mp);
        }
    };

    private MediaPlayer.OnTimedTextListener mBaseOnTimedTextListener = new MediaPlayer.OnTimedTextListener() {
        @Override
        public void onTimedTextUpdate(byte[] pixels, int width, int height) {
            if (mOnTimedTextListener != null)
                mOnTimedTextListener.onTimedTextUpdate(pixels, width, height);
        }

        @Override
        public void onTimedText(String text) {
            mTvCurrentTime.setText(text);
            updateControllerState();
            if (mOnTimedTextListener != null)
                mOnTimedTextListener.onTimedText(text);
        }
    };

    private Handler mControllerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FADE_OUT:
                    break;
                case SHOW_PROGRESS:
                    updateControllerState();
                    sendEmptyMessageDelayed(SHOW_PROGRESS,UPDATE_INTERVAL);
                    break;
                case HIND_PROGRESS:
                    mLayoutController.setVisibility(GONE);
                    mPbProgress.setVisibility(VISIBLE);
                    mControllerState = STATE_CONTROLLER_SIMPLE;
                    break;
            }
        }
    };

    public ShortVideoView(Context context) {
        super(context);
        init(null, 0);
    }

    public ShortVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ShortVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mContext = getContext();
        initView();
        mVideoWidth = 0;
        mVideoHeight = 0;
        // this value only use Hardware decoder before Android 2.3
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB && mHardwareDecoder) {
            mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
        if (mContext instanceof Activity)
            ((Activity) mContext).setVolumeControlStream(AudioManager.STREAM_MUSIC);


//        // Load attributes
//        final TypedArray a = mContext.obtainStyledAttributes(
//                attrs, R.styleable.ShortVideoView, defStyle, 0);
//
//        mExampleString = a.getString(
//                R.styleable.ShortVideoView_exampleString);
//        mExampleColor = a.getColor(
//                R.styleable.ShortVideoView_exampleColor,
//                mExampleColor);
//        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
//        // values that should fall on pixel boundaries.
//        mExampleDimension = a.getDimension(
//                R.styleable.ShortVideoView_exampleDimension,
//                mExampleDimension);
//
//        if (a.hasValue(R.styleable.ShortVideoView_exampleDrawable)) {
//            mExampleDrawable = a.getDrawable(
//                    R.styleable.ShortVideoView_exampleDrawable);
//            mExampleDrawable.setCallback(this);
//        }
//
//        a.recycle();

    }

    private void initView() {
        View contentView = inflate(mContext, R.layout.short_video_view,null);
        contentView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        addView(contentView);
        mSurfaceView = (SurfaceView) findViewById(R.id.short_video_view_surfaceview);
        mSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888); // PixelFormat.RGB_565
        mSurfaceView.getHolder().addCallback(mSHCallback);

        mTvCurrentTime = (TextView) findViewById(R.id.short_video_view_tv_current_time);
        mTvToltalTime = (TextView) findViewById(R.id.short_video_view_tv_total_time);
        mPbProgress = (ProgressBar) findViewById(R.id.short_video_view_pb_progress);
        mSbProgress = (SeekBar) findViewById(R.id.short_video_view_sb_progress);
        mIvFullScreen  = (ImageView) findViewById(R.id.short_video_view_iv_full_screen);
        mLayoutController = (LinearLayout) findViewById(R.id.short_video_view_layout_big_controller);
        mIvPause = (ImageView) findViewById(R.id.short_video_view_iv_pause);
        mTvRepeat = findViewById(R.id.short_video_view_tv_repeat);

        mSurfaceView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mControllerState){
                    case STATE_CONTROLLER_SIMPLE:
                        mLayoutController.setVisibility(VISIBLE);
                        mPbProgress.setVisibility(GONE);
                        mControllerHandler.sendEmptyMessageDelayed(HIND_PROGRESS,3000);
                        mControllerState = STATE_CONTROLLER_DETAILE;
                        break;
                    case STATE_CONTROLLER_DETAILE:
                        pause();
                        mIvPause.setVisibility(VISIBLE);
                        mControllerHandler.removeMessages(HIND_PROGRESS);
                        mControllerState = STATE_CONTROLLER_PAUSED;
                        break;
                    case STATE_CONTROLLER_PAUSED:
                        resume();
                        mIvPause.setVisibility(GONE);
                        mControllerHandler.sendEmptyMessageDelayed(HIND_PROGRESS,3000);
                        mControllerState = STATE_CONTROLLER_DETAILE;
                        break;
                    case STATE_CONTROLLER_END:
                        mMediaPlayer.seekTo(0);
                        mLayoutController.setVisibility(VISIBLE);
                        mPbProgress.setVisibility(GONE);
                        mIvPause.setVisibility(GONE);
                        mTvRepeat.setVisibility(GONE);
                        mControllerHandler.sendEmptyMessageDelayed(HIND_PROGRESS,3000);
                        mControllerState = STATE_CONTROLLER_DETAILE;
                        break;
                    default:
                        break;
                }
            }
        });
        mPbProgress.setMax((int) MAX_PROGRESS);
        mSbProgress.setMax((int) MAX_PROGRESS);
        mSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser)
                    return;
                long duration = mMediaPlayer.getDuration();
                long toPos = duration * progress/MAX_PROGRESS;
                mMediaPlayer.seekTo(toPos);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
//        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
//        setMeasuredDimension(width, height);
//    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mControllerHandler.removeMessages(SHOW_PROGRESS);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mControllerHandler.removeMessages(SHOW_PROGRESS);
    }

    /**
     * Set the display options
     *
     * @param layout      <ul>
     *                    <li>{@link #VIDEO_LAYOUT_ORIGIN}
     *                    <li>{@link #VIDEO_LAYOUT_SCALE}
     *                    <li>{@link #VIDEO_LAYOUT_STRETCH}
     *                    <li>{@link #VIDEO_LAYOUT_FIT_PARENT}
     *                    <li>{@link #VIDEO_LAYOUT_ZOOM}
     *                    </ul>
     * @param aspectRatio video aspect ratio, will audo detect if 0.
     */
    public void setVideoLayout(int layout, float aspectRatio) {
        ViewGroup.LayoutParams lp = getLayoutParams();
        Pair<Integer, Integer> res = ScreenResolution.getResolution(mContext);
        int windowWidth = res.first.intValue(), windowHeight = res.second.intValue();
        float windowRatio = windowWidth / (float) windowHeight;
        float videoRatio = aspectRatio <= 0.01f ? mVideoAspectRatio : aspectRatio;
        mSurfaceHeight = mVideoHeight;
        mSurfaceWidth = mVideoWidth;
        if (VIDEO_LAYOUT_ORIGIN == layout && mSurfaceWidth < windowWidth && mSurfaceHeight < windowHeight) {
            lp.width = (int) (mSurfaceHeight * videoRatio);
            lp.height = mSurfaceHeight;
        } else if (layout == VIDEO_LAYOUT_ZOOM) {
            lp.width = windowRatio > videoRatio ? windowWidth : (int) (videoRatio * windowHeight);
            lp.height = windowRatio < videoRatio ? windowHeight : (int) (windowWidth / videoRatio);
        } else if (layout == VIDEO_LAYOUT_FIT_PARENT) {
            ViewGroup parent = (ViewGroup) getParent();
            float parentRatio = ((float) parent.getWidth()) / ((float) parent.getHeight());
            lp.width = (parentRatio < videoRatio) ? parent.getWidth() : Math.round(((float) parent.getHeight()) * videoRatio);
            lp.height = (parentRatio > videoRatio) ? parent.getHeight() : Math.round(((float) parent.getWidth()) / videoRatio);
        } else {
            boolean full = layout == VIDEO_LAYOUT_STRETCH;
            lp.width = (full || windowRatio < videoRatio) ? windowWidth : (int) (videoRatio * windowHeight);
            lp.height = (full || windowRatio > videoRatio) ? windowHeight : (int) (windowWidth / videoRatio);
        }
        mSurfaceView.setLayoutParams(lp);
        mSurfaceView.getHolder().setFixedSize(mSurfaceWidth, mSurfaceHeight);
//        Log.d("VIDEO: %dx%dx%f, Surface: %dx%d, LP: %dx%d, Window: %dx%dx%f", mVideoWidth, mVideoHeight, mVideoAspectRatio, mSurfaceWidth, mSurfaceHeight, lp.width, lp.height, windowWidth, windowHeight, windowRatio);
        mVideoLayout = layout;
        mAspectRatio = aspectRatio;
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    public void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate)
                mTargetState = STATE_IDLE;
        }
    }

    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null)
            return;
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);

        release(false);
        try {
            mDuration = -1;
            mCurrentBufferPercentage = 0;
            mMediaPlayer = new MediaPlayer(mContext, mHardwareDecoder);
            mMediaPlayer.setOnPreparedListener(mBaseOnPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mBaseOnSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mBaseOnCompletionListener);
            mMediaPlayer.setOnErrorListener(mBaseOnErrorListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBaseOnBufferingUpdateListener);
            mMediaPlayer.setOnInfoListener(mBaseOnInfoListener);
            mMediaPlayer.setOnSeekCompleteListener(mBaseOnSeekCompleteListener);
            mMediaPlayer.setOnTimedTextListener(mBaseOnTimedTextListener);

//            Log.d(" set user optional --------  ");
            HashMap<String, String> options = new HashMap<String, String>();
            options.put("rtsp_transport", "tcp"); // udp
            //	options.put("user-agent", "userAgent");
            //	options.put("cookies", "cookies");
            options.put("analyzeduration", "1000000");
            mMediaPlayer.setDataSource(mContext, mUri, mHeaders);

            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setBufferSize(mBufSize);
            mMediaPlayer.setVideoChroma(mVideoChroma == MediaPlayer.VIDEOCHROMA_RGB565 ? MediaPlayer.VIDEOCHROMA_RGB565 : MediaPlayer.VIDEOCHROMA_RGBA);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
//            attachMediaController();
        } catch (IOException ex) {
//            Log.e("Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mBaseOnErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
//            Log.e("Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mBaseOnErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
    }

    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }

        mTargetState = STATE_PLAYING;
    }

    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void resume() {
        if (mSurfaceHolder == null && mCurrentState == STATE_SUSPEND) {
            mTargetState = STATE_RESUME;
        } else if (mCurrentState == STATE_SUSPEND_UNSUPPORTED) {
            openVideo();
        }else{
            mMediaPlayer.start();
        }
    }

    public void seekTo(long msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    public long getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0)
                return mDuration;
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    public long getCurrentPosition() {
        if (isInPlaybackState())
            return mMediaPlayer.getCurrentPosition();
        return 0;
    }

    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    public int getBufferPercentage() {
        if (mMediaPlayer != null)
            return mCurrentBufferPercentage;
        return 0;
    }

    protected boolean isInPlaybackState() {
        return (mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    private void updateControllerState(){
        if (mMediaPlayer==null)
            return;
        long position = mMediaPlayer.getCurrentPosition();
        long duration = mMediaPlayer.getDuration();
        long pos = MAX_PROGRESS * position / duration;
        mPbProgress.setProgress((int) pos);
        mSbProgress.setProgress((int) pos);
        mTvToltalTime.setText(TIME_FORMAT.format(new Date(duration)));
        mTvCurrentTime.setText(TIME_FORMAT.format(new Date(position)));
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    public void setBufferSize(int bufSize) {
        mBufSize = bufSize;
    }

    /**
     * Must set before {@link #setVideoURI}
     *
     * @param chroma
     */
    public void setVideoChroma(int chroma) {
        mSurfaceView.getHolder().setFormat(chroma == MediaPlayer.VIDEOCHROMA_RGB565 ? PixelFormat.RGB_565 : PixelFormat.RGBA_8888); // PixelFormat.RGB_565
        mVideoChroma = chroma;
    }

    public void setMediaBufferingIndicator(View mediaBufferingIndicator) {
        if (mMediaBufferingIndicator != null)
            mMediaBufferingIndicator.setVisibility(View.GONE);
        mMediaBufferingIndicator = mediaBufferingIndicator;
    }

    public void addOnCompletionListener(MediaPlayer.OnCompletionListener onCompletionListener){
        mOnCompletionListener = onCompletionListener;
    }

    public void addOnErrotListener(MediaPlayer.OnErrorListener onErrorListener){
        mOnErrorListener = onErrorListener;
    }

    public void addOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener){
        mOnBufferingUpdateListener = onBufferingUpdateListener;
    }

    public void addOnInfoListener(MediaPlayer.OnInfoListener onInfoListener){
        mOnInfoListener = onInfoListener;
    }

    public void addOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener onSeekCompleteListener){
        mOnSeekCompleteListener = onSeekCompleteListener;
    }

    public void add(MediaPlayer.OnTimedTextListener onTimedTextListener){
        mOnTimedTextListener = onTimedTextListener;
    }
}
