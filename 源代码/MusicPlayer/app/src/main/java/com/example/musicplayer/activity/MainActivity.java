package com.example.musicplayer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.MyRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private Button statusBtn;
    private Button stopBtn;
    private Button typeBtn;
    private Button lastOneBtn;
    private Button nextOneBtn;
    private Button songsBtn;
    private int SONG_STATUS = 0;    // -1 表示暂停，0 表示未开始，1 表示正在播放
    private ArrayList<String> songsList = new ArrayList<>();
    private int currentIndex = 0;
    private RecyclerView recyclerView;
    private MyRecyclerViewAdapter adapter;
    private AssetManager assetManager;
    private MediaPlayer player;
    private AssetFileDescriptor assetFd;
    private DrawerLayout drawerLayout;
    private TextView startTime;
    private TextView endTime;
    private SeekBar seekBar;
    private boolean isChangingSeekBar = false;
    private Timer timer = new Timer();
    private TextView headTitle;
    private int SONG_PLAY_TYPE = 0;    // -1 表示循环，0 表示顺序，1 表示随机
    private CircleImageView imageView;
    private RotateAnimation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        assetManager = getAssets();
        songsList = getSongsList();
        player = new MediaPlayer();
        player.setLooping(false);
        drawerLayout = findViewById(R.id.drawerLayout);
        startTime = findViewById(R.id.start_time);
        endTime = findViewById(R.id.end_time);
        seekBar = findViewById(R.id.seekBar);
        headTitle = findViewById(R.id.header_title);
        headTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        headTitle.setFocusable(true);
        headTitle.setFocusableInTouchMode(true);
        headTitle.setSingleLine();
        headTitle.setSelected(true);
        imageView = findViewById(R.id.imageView);

        player.setOnPreparedListener(mediaPlayer -> {
            headTitle.setText(songsList.get(currentIndex).split(".mp3")[0]);
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                refreshProcessTime();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isChangingSeekBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isChangingSeekBar = false;
                if(SONG_STATUS != 1) {
                    player.start();
                    SONG_STATUS = 1;
                    statusBtn.setText("暂停");
                }
                player.seekTo(seekBar.getProgress());
                int position = player.getCurrentPosition();
                startTime.setText(getProgressTime(position / 1000));
            }
        });

        statusBtn = findViewById(R.id.status_btn);
        statusBtn.setOnClickListener(view -> {
            if(SONG_STATUS == 0 || SONG_STATUS == -1) {
                player.start();
                SONG_STATUS = 1;
                statusBtn.setText("暂停");
            }
            else if(SONG_STATUS == 1) {
                player.pause();
                SONG_STATUS = -1;
                statusBtn.setText("播放");
            }
        });

        stopBtn = findViewById(R.id.stop_btn);
        stopBtn.setOnClickListener(view -> {
            player.stop();
            SONG_STATUS = 0;
            statusBtn.setText("播放");
            try {
                assetFd = assetManager.openFd(songsList.get(currentIndex));
                refreshCover();
                timer.cancel();
                player.reset();
                player.setDataSource(assetFd.getFileDescriptor(), assetFd.getStartOffset(), assetFd.getLength());
                player.prepare();
                refreshSeekBarProcess();
            } catch(Exception e) {
                Toast.makeText(MainActivity.this, "停止失败", Toast.LENGTH_SHORT).show();
            }
        });

        lastOneBtn = findViewById(R.id.lastOne_btn);
        lastOneBtn.setOnClickListener(view -> {
            try {
                player.stop();
                SONG_STATUS = 0;
                currentIndex = getModulus(currentIndex - 1, songsList.size());
                assetFd = assetManager.openFd(songsList.get(currentIndex));
                refreshCover();
                timer.cancel();
                player.reset();
                player.setDataSource(assetFd.getFileDescriptor(), assetFd.getStartOffset(), assetFd.getLength());
                player.prepare();
                player.start();
                SONG_STATUS = 1;
                statusBtn.setText("暂停");
                refreshSeekBarProcess();
            } catch(Exception e) {
                Toast.makeText(MainActivity.this, "上一首自动播放失败", Toast.LENGTH_SHORT).show();
                SONG_STATUS = 0;
                statusBtn.setText("播放");
            }
        });

        nextOneBtn = findViewById(R.id.nextOne_btn);
        nextOneBtn.setOnClickListener(view -> {
            try {
                player.stop();
                SONG_STATUS = 0;
                currentIndex = getModulus(currentIndex + 1, songsList.size());
                assetFd = assetManager.openFd(songsList.get(currentIndex));
                refreshCover();
                timer.cancel();
                player.reset();
                player.setDataSource(assetFd.getFileDescriptor(), assetFd.getStartOffset(), assetFd.getLength());
                player.prepare();
                player.start();
                SONG_STATUS = 1;
                statusBtn.setText("暂停");
                refreshSeekBarProcess();
            } catch(Exception e) {
                Toast.makeText(MainActivity.this, "下一首自动播放失败", Toast.LENGTH_SHORT).show();
                SONG_STATUS = 0;
                statusBtn.setText("播放");
            }
        });

        songsBtn = findViewById(R.id.songs_btn);
        songsBtn.setOnClickListener(view -> drawerLayout.openDrawer(Gravity.RIGHT));

        typeBtn = findViewById(R.id.type_btn);
        typeBtn.setOnClickListener(view -> {
            if(SONG_PLAY_TYPE == -1) {
                player.setLooping(false);
                SONG_PLAY_TYPE = 1;
                typeBtn.setText("随机");
            }
            else if(SONG_PLAY_TYPE == 0) {
                player.setLooping(true);
                SONG_PLAY_TYPE = -1;
                typeBtn.setText("单曲");
            }
            else if(SONG_PLAY_TYPE == 1) {
                SONG_PLAY_TYPE = 0;
                typeBtn.setText("顺序");
            }
        });

        player.setOnCompletionListener(mediaPlayer -> {
            if(SONG_PLAY_TYPE == 0)
                currentIndex = getModulus(currentIndex + 1, songsList.size());
            else if(SONG_PLAY_TYPE == 1)
                currentIndex = random(songsList.size());
            try {
                assetFd = assetManager.openFd(songsList.get(currentIndex));
                refreshCover();
                timer.cancel();
                player.reset();
                player.setDataSource(assetFd.getFileDescriptor(), assetFd.getStartOffset(), assetFd.getLength());
                player.prepare();
                player.start();
                SONG_STATUS = 1;
                statusBtn.setText("暂停");
                refreshSeekBarProcess();
            } catch(Exception e) {
                Toast.makeText(MainActivity.this, "列表播放失败", Toast.LENGTH_SHORT).show();
                SONG_STATUS = 0;
                statusBtn.setText("播放");
            }
        });

        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                recyclerView = findViewById(R.id.recyclerView);
                LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                recyclerView.setLayoutManager(layoutManager);
                adapter = new MyRecyclerViewAdapter(songsList, view -> {
                    drawerLayout.closeDrawers();
                    String song = ((TextView) view.findViewById(R.id.textView)).getText().toString() + ".mp3";
                    play(song);
                }, view -> {
                    String song = ((TextView) ((View) view.getParent()).findViewById(R.id.textView)).getText().toString() + ".mp3";
                    for(int i = 0; i < songsList.size(); i ++) {
                        if(songsList.get(i).equals(song))
                            songsList.remove(i);
                    }
                    adapter.notifyDataSetChanged();
                });
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(0);
                View addSong = findViewById(R.id.add_song);
                addSong.setOnClickListener(view -> {
                    
                });
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        try {
            currentIndex = random(songsList.size());
            assetFd = assetManager.openFd(songsList.get(currentIndex));
            refreshCover();
            player.reset();
            player.setDataSource(assetFd.getFileDescriptor(), assetFd.getStartOffset(), assetFd.getLength());
            player.prepare();
            refreshSeekBarProcess();
        } catch(Exception e) {
            Toast.makeText(MainActivity.this, "歌曲初始化失败", Toast.LENGTH_SHORT).show();
        }

        initAnimation();
    }

    private ArrayList<String> getSongsList() {
        ArrayList<String> list = new ArrayList<>();
        try {
            String[] songs = assetManager.list("");
            for(int i = 0; i < songs.length; i ++)
                if(songs[i].indexOf(".mp3") != -1)
                    list.add(songs[i]);
        } catch(Exception e) {
            Toast.makeText(MainActivity.this, "歌曲加载失败", Toast.LENGTH_SHORT).show();
        }
        return list;
    }

    private void play(String song) {
        try {
            for(int i = 0; i < songsList.size(); i ++) {
                if(song.equals(songsList.get(i))) {
                    currentIndex = i;
                    break;
                }
            }
            assetFd = assetManager.openFd(song);
            refreshCover();
            timer.cancel();
            player.reset();
            player.setDataSource(assetFd.getFileDescriptor(), assetFd.getStartOffset(), assetFd.getLength());
            player.prepare();
            player.start();
            SONG_STATUS = 1;
            statusBtn.setText("暂停");
            refreshSeekBarProcess();
        } catch(Exception e) {
            Toast.makeText(MainActivity.this, "自动播放失败", Toast.LENGTH_SHORT).show();
            SONG_STATUS = 0;
            statusBtn.setText("播放");
        }
    }

    private void refreshCover() {
        MediaMetadataRetriever dataRetriever = new MediaMetadataRetriever();
        dataRetriever.setDataSource(assetFd.getFileDescriptor(), assetFd.getStartOffset(), assetFd.getLength());
        byte[] bytes = dataRetriever.getEmbeddedPicture();
        Bitmap cover = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imageView.setImageBitmap(cover);
    }

    private void initAnimation() {
        animation = new RotateAnimation(0f, 360f, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(10000);
        animation.setRepeatCount(RotateAnimation.INFINITE);
        animation.setRepeatMode(RotateAnimation.RESTART);
        LinearInterpolator linearInterpolator = new LinearInterpolator();
        animation.setInterpolator(linearInterpolator);
        imageView.setAnimation(animation);
    }

    private void refreshProcessTime() {
        int duration = player.getDuration() / 1000;
        int position = player.getCurrentPosition();
        startTime.setText(getProgressTime(position / 1000));
        endTime.setText(getProgressTime(duration));
    }

    private void refreshSeekBarProcess() {
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(! isChangingSeekBar) {
                    seekBar.setMax(player.getDuration());
                    seekBar.setProgress(player.getCurrentPosition());
                }
            }
        },0,50);
    }

    public static String getProgressTime(int time) {
        int minute;
        int second;
        if(time >= 60) {
            minute = time / 60;
            second = time % 60;
            if(minute < 10) {
                if(second < 10) {
                    return "0" + minute + ":" + "0" + second;
                }
                else {
                    return "0" + minute + ":" + second;
                }
            }
            else {
                if(second < 10) {
                    return minute + ":" + "0" + second;
                }
                else {
                    return minute + ":" + second;
                }
            }
        }
        else {
            second = time;
            if(second >= 0 && second < 10) {
                return "00:" + "0" + second;
            }
            else {
                return "00:" + second;
            }
        }
    }

    public static int random(int max) {
        return new Random().nextInt(max);
    }

    public int getModulus(int m, int n) {
        if(n > 0) {
            while(m < 0)
                m += n;
            while(m > n - 1)
                m -= n;
        }
        else
            m = 0;
        return m;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(player != null)
            player.release();
    }
}
