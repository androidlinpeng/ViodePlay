package com.loading;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Formatter;
import java.util.Locale;


public class APIModuleDemo extends UZModule implements View.OnClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private static final String TAG = "APIModuleDemo";
    private UZModuleContext mJsCallback;
    private RelativeLayout.LayoutParams layoutParams;
    private View rootview = null;
    private View videoContainer = null;
    private View mediaController = null;
    private View bottomController = null;
    private VideoView videoView = null;
    private ImageView toggleScreen = null;
    private ImageView back = null;
    private TextView tvTitle = null;
    private ProgressBar progress = null;
    private ImageView play = null;
    private TextView curMediaTime = null;
    private TextView mediaTime = null;
    private TextView videoSpeed = null;
    private View speedLayout = null;
    private TextView speed_3 = null;
    private TextView speed_2 = null;
    private TextView speed_1 = null;
    private ProgressBar mediaProgress = null;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private String videoUrl;
    private String title;
    private boolean playing = false;
    private int x;
    private int y;
    private int width;
    private int height;
    private int position = 0;

    private MediaPlayer mediaPlayer;

    private static final int WHAT_UPDATE_PROGRESS = 1;
    private static final int WHAT_HIDE_PLAY_VIEW = 2;
    private static final int DELAYED_TIME = 5000;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_UPDATE_PROGRESS:
                    if (APIModuleDemo.this.videoView.isPlaying()) {
                        APIModuleDemo.this.runOnUiThreadDelay(new Runnable() {
                            public void run() {
                                APIModuleDemo.this.setProgress();
                                if (APIModuleDemo.this.videoView.isPlaying()) {
                                    sendEmptyMessage(WHAT_UPDATE_PROGRESS);
                                }

                            }
                        }, 500);
                    }
                    break;
                case 2:
                    APIModuleDemo.this.mediaController.post(new Runnable() {
                        public void run() {
                            APIModuleDemo.this.mediaController.setVisibility(View.GONE);
                        }
                    });
            }

        }
    };

    public APIModuleDemo(UZWebView webView) {
        super(webView);
    }

    public void jsmethod_openplayer(UZModuleContext moduleContext) {
        if (!this.playing) {
            this.openVideoPlay(moduleContext);
            this.playing = true;
        } else {
            this.removeViewFromCurWindow(this.rootview);
            this.openVideoPlay(moduleContext);
        }

    }

    public void jsmethod_closeplayer(UZModuleContext moduleContext) {
        this.mJsCallback = moduleContext;
        if (null != this.mJsCallback) {
            JSONObject json = new JSONObject();
            try {
                if (this.playing) {
                    int curOrentation = this.mContext.getRequestedOrientation();
                    if (curOrentation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        this.mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        this.toggleScreen.setImageResource(UZResourcesIDFinder.getResDrawableID("ic_fullscreen_white_36dp"));
                        this.smallScreenPlayer();
                        json.put("back", false);
                    } else if (curOrentation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        json.put("back", true);
                        this.removeViewFromCurWindow(this.rootview);
                        this.playing = false;
                    }
                } else {
                    json.put("back", true);
                }
                this.mJsCallback.success(json, true);
                this.mJsCallback = null;
            } catch (JSONException var4) {
                var4.printStackTrace();
            }
        }

    }

    private void openVideoPlay(UZModuleContext moduleContext) {
        this.mJsCallback = moduleContext;
        this.x = moduleContext.optInt("x");
        this.y = moduleContext.optInt("y");
        this.width = moduleContext.optInt("w");
        this.height = moduleContext.optInt("h");
//        this.width = getScreenWidth();
        this.videoUrl = moduleContext.optString("url");
        this.title = moduleContext.optString("title");
        if (this.width == 0) {
            this.width = -1;
        }

        if (this.height == 0) {
            this.height = -1;
        }

        this.layoutParams = new RelativeLayout.LayoutParams(width, height);
        this.layoutParams.leftMargin = this.x;
        this.layoutParams.topMargin = this.y;
        int LayoutId = UZResourcesIDFinder.getResLayoutID("view_video_play");
        this.rootview = this.createItemRootView((ViewGroup) null, LayoutId);
        this.insertViewToCurWindow(this.rootview, this.layoutParams);
    }

    private View createItemRootView(ViewGroup parentView, int layout) {
        View rootView = View.inflate(this.getContext(), layout, parentView);
        this.videoContainer = rootView.findViewById(UZResourcesIDFinder.getResIdID("video_container"));
        this.mediaController = rootView.findViewById(UZResourcesIDFinder.getResIdID("media_controller"));
        this.bottomController = rootView.findViewById(UZResourcesIDFinder.getResIdID("bottom_controller"));
        this.videoView = rootView.findViewById(UZResourcesIDFinder.getResIdID("video_view"));
        Uri uri = Uri.parse(videoUrl);
        this.videoView.setVideoURI(uri);
        this.toggleScreen = rootView.findViewById(UZResourcesIDFinder.getResIdID("toggle_screen"));
        this.back = rootView.findViewById(UZResourcesIDFinder.getResIdID("back"));
        this.tvTitle = rootView.findViewById(UZResourcesIDFinder.getResIdID("title"));
        this.play = rootView.findViewById(UZResourcesIDFinder.getResIdID("play"));
        this.curMediaTime = rootView.findViewById(UZResourcesIDFinder.getResIdID("time_current"));
        this.mediaTime = rootView.findViewById(UZResourcesIDFinder.getResIdID("time"));
        this.videoSpeed = rootView.findViewById(UZResourcesIDFinder.getResIdID("speed"));
        this.speedLayout = rootView.findViewById(UZResourcesIDFinder.getResIdID("speed_layout"));
        this.speed_3 = rootView.findViewById(UZResourcesIDFinder.getResIdID("speed_3"));
        this.speed_2 = rootView.findViewById(UZResourcesIDFinder.getResIdID("speed_2"));
        this.speed_1 = rootView.findViewById(UZResourcesIDFinder.getResIdID("speed_1"));
        this.toggleScreen = rootView.findViewById(UZResourcesIDFinder.getResIdID("toggle_screen"));
        this.progress = rootView.findViewById(UZResourcesIDFinder.getResIdID("progress"));
        this.mediaProgress = rootView.findViewById(UZResourcesIDFinder.getResIdID("mediacontroller_progress"));
        if (this.mediaProgress instanceof SeekBar) {
            SeekBar seekBar = (SeekBar) this.mediaProgress;
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        long duration = (long) APIModuleDemo.this.videoView.getDuration();
                        long newposition = duration * (long) progress / 1000L;
                        APIModuleDemo.this.videoView.seekTo((int) newposition);
                        if (APIModuleDemo.this.curMediaTime != null) {
                            APIModuleDemo.this.curMediaTime.setText(APIModuleDemo.this.stringForTime((int) newposition));
                        }

                    }
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                    APIModuleDemo.this.handler.removeMessages(WHAT_UPDATE_PROGRESS);
                    APIModuleDemo.this.handler.removeMessages(WHAT_HIDE_PLAY_VIEW);
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    APIModuleDemo.this.setProgress();
                    APIModuleDemo.this.updatePausePlay();
                    APIModuleDemo.this.handler.sendEmptyMessage(WHAT_UPDATE_PROGRESS);
                    APIModuleDemo.this.handler.sendEmptyMessageDelayed(WHAT_HIDE_PLAY_VIEW, DELAYED_TIME);
                }
            });
            this.mediaProgress.setMax(1000);
        }

        this.videoView.setOnPreparedListener(this);
        this.videoView.setOnCompletionListener(this);
        this.toggleScreen.setOnClickListener(this);
        this.back.setOnClickListener(this);
        this.play.setOnClickListener(this);
        this.videoSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedLayout.setVisibility(View.VISIBLE);
            }
        });
        speedLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedLayout.setVisibility(View.GONE);
            }
        });
        speed_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePlayerSpeed(1.0f, 1);
            }
        });
        speed_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePlayerSpeed(1.5f, 2);
            }
        });
        speed_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePlayerSpeed(1.75f, 3);
            }
        });
        this.videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (what == 1 && extra == -1004 && !APIModuleDemo.isBlank(videoUrl)) {
                    APIModuleDemo.this.videoView.stopPlayback();
                    APIModuleDemo.this.videoView.setVideoPath(videoUrl);
                    APIModuleDemo.this.videoView.seekTo(APIModuleDemo.this.position);
                }
                return true;
            }
        });
        this.videoContainer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!APIModuleDemo.this.speedLayout.isShown()) {
                    if (APIModuleDemo.this.mediaController.isShown()) {
                        APIModuleDemo.this.mediaController.setVisibility(View.GONE);
                    } else {
                        APIModuleDemo.this.mediaController.setVisibility(View.VISIBLE);
                        APIModuleDemo.this.handler.removeMessages(WHAT_HIDE_PLAY_VIEW);
                        APIModuleDemo.this.handler.sendEmptyMessageDelayed(WHAT_HIDE_PLAY_VIEW, DELAYED_TIME);
                    }
                } else {
                    speedLayout.setVisibility(View.GONE);
                }
            }
        });
        this.mFormatBuilder = new StringBuilder();
        this.mFormatter = new Formatter(this.mFormatBuilder, Locale.getDefault());
        if (!isBlank(title)) {
            tvTitle.setText(title);
        }
        return rootView;
    }

    private void changePlayerSpeed(float speed, int index) {
        Log.i(TAG, "changePlayerSpeed: speed " + speed);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
                runOnUiThreadDelay(new Runnable() {
                    @Override
                    public void run() {
                        videoView.start();
                    }
                }, 2000);
            } catch (Exception e) {
                Log.i(TAG, "changePlayerSpeed: e ", e);
            }
            speed_1.setTextColor(getContext().getResources().getColor(UZResourcesIDFinder.getResColorID(index == 1 ? "green" : "white")));
            speed_2.setTextColor(getContext().getResources().getColor(UZResourcesIDFinder.getResColorID(index == 2 ? "green" : "white")));
            speed_3.setTextColor(getContext().getResources().getColor(UZResourcesIDFinder.getResColorID(index == 3 ? "green" : "white")));
            speedLayout.setVisibility(View.GONE);
        }
    }

    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.i(TAG, "onPrepared: ");
        this.mediaPlayer = mediaPlayer;
        this.progress.setVisibility(View.GONE);
        this.bottomController.setVisibility(View.VISIBLE);
        this.videoView.setBackgroundColor(Color.TRANSPARENT);
        this.videoView.start();
        this.handler.sendEmptyMessage(WHAT_UPDATE_PROGRESS);
        this.handler.sendEmptyMessageDelayed(WHAT_HIDE_PLAY_VIEW, DELAYED_TIME);
        this.updatePausePlay();
    }

    public void onClick(View view) {
        int id = view.getId();
        int backId = UZResourcesIDFinder.getResIdID("back");
        int playId = UZResourcesIDFinder.getResIdID("play");
        int toggle_screenId = UZResourcesIDFinder.getResIdID("toggle_screen");
        if (id == backId) {
            int curOrentation = this.mContext.getRequestedOrientation();
            if (curOrentation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                this.mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                this.toggleScreen.setImageResource(UZResourcesIDFinder.getResDrawableID("ic_fullscreen_white_36dp"));
                this.smallScreenPlayer();
//                this.back.setVisibility(View.GONE);
            } else {
                this.removeViewFromCurWindow(this.rootview);
                this.playing = false;
                try {
                    JSONObject json = new JSONObject();
                    json.put("back", true);
                    mJsCallback.success(json, true);
                    mJsCallback = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (id == toggle_screenId) {
            this.toggleOrientation();
        } else if (id == playId) {
            this.pauseVideo();
        }

    }

    private void pauseVideo() {
        if (this.videoView.isPlaying()) {
            this.videoView.pause();
            this.handler.sendEmptyMessage(WHAT_UPDATE_PROGRESS);
        } else {
            this.videoView.start();
            this.handler.sendEmptyMessage(WHAT_UPDATE_PROGRESS);
        }

        this.updatePausePlay();
    }

    private void toggleOrientation() {
        int curOrentation = this.mContext.getRequestedOrientation();
        if (curOrentation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            this.mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            this.toggleScreen.setImageResource(UZResourcesIDFinder.getResDrawableID("ic_fullscreen_white_36dp"));
            this.smallScreenPlayer();
//            this.back.setVisibility(View.GONE);
        } else if (curOrentation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            this.mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            this.toggleScreen.setImageResource(UZResourcesIDFinder.getResDrawableID("ic_fullscreen_exit_white_36dp"));
            this.fullScreenPlayer();
            this.back.setVisibility(View.VISIBLE);
        }

    }

    private void smallScreenPlayer() {
        RelativeLayout.LayoutParams layoutP = (RelativeLayout.LayoutParams) this.rootview.getLayoutParams();
        layoutP.width = this.dip2px((float) width);
        layoutP.height = this.dip2px((float) height);
        layoutP.leftMargin = this.dip2px((float) this.x);
        layoutP.topMargin = this.dip2px((float) this.y);
        this.layoutParams = layoutP;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.videoContainer.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        this.videoContainer.setLayoutParams(layoutParams);
    }

    private void fullScreenPlayer() {
        this.layoutParams.width = -1;
        this.layoutParams.height = -1;
        this.layoutParams.leftMargin = 0;
        this.layoutParams.topMargin = 0;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.videoContainer.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        this.videoContainer.setLayoutParams(layoutParams);
    }

    private int setProgress() {
        this.position = this.videoView.getCurrentPosition();
        int duration = this.videoView.getDuration();
        if (this.mediaProgress != null) {
            if (duration > 0) {
                long pos = 1000L * (long) this.position / (long) duration;
                this.mediaProgress.setProgress((int) pos);
            }

            int percent = this.videoView.getBufferPercentage();
            this.mediaProgress.setSecondaryProgress(percent * 10);
        }

        if (this.mediaTime != null) {
            this.mediaTime.setText(this.stringForTime(duration));
        }

        if (this.curMediaTime != null) {
            this.curMediaTime.setText(this.stringForTime(this.position));
        }

        return this.position;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = totalSeconds / 60 % 60;
        int hours = totalSeconds / 3600;
        this.mFormatBuilder.setLength(0);
        return hours > 0 ? this.mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString() : this.mFormatter.format("%02d:%02d", minutes, seconds).toString();
    }

    private void updatePausePlay() {
        if (this.videoView.isPlaying()) {
            this.play.setImageResource(UZResourcesIDFinder.getResDrawableID("ic_media_pause"));
        } else {
            this.play.setImageResource(UZResourcesIDFinder.getResDrawableID("ic_media_play"));
        }

    }

    public void onCompletion(MediaPlayer mediaPlayer) {
        this.updatePausePlay();
    }

    private static boolean isBlank(String s) {
        return s == null || s.equals("") || s.equals("null");
    }

    private int dip2px(float dpValue) {
        float scale = this.mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5F);
    }

    private int getScreenWidth() {
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }

    private int getScreenHeight() {
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getHeight();
    }
}
