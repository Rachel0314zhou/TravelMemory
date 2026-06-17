package com.example.travelmemory.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.R;
import com.example.travelmemory.adapter.TripRecapAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.DayRecord;
import com.example.travelmemory.model.Photo;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TravelConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class TripRecapActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int tripId = -1;
    private int currentUserId = -1;
    private MediaPlayer mediaPlayer;
    private Button btnMusic;
    private boolean userPausedMusic = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_recap);

        dbHelper = new DatabaseHelper(this);
        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);

        if (currentUserId <= 0 || tripId <= 0 || !dbHelper.isTripOwnedByUser(tripId, currentUserId)) {
            Toast.makeText(this, "请先选择自己的旅行", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        findViewById(R.id.btn_recap_back).setOnClickListener(v -> finish());
        btnMusic = findViewById(R.id.btn_recap_music);
        btnMusic.setOnClickListener(v -> toggleMusic());
        bindRecap();
        startBgm();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !userPausedMusic && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            updateMusicButton();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updateMusicButton();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseBgm();
    }

    private void bindRecap() {
        Trip trip = dbHelper.getTripForUser(tripId, currentUserId);
        if (trip == null) {
            Toast.makeText(this, "无权查看这段旅行回顾", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView title = findViewById(R.id.tv_recap_title);
        title.setText(trip.getName());

        List<DayRecord> dayRecords = dbHelper.getDayRecordsForTrip(tripId);
        List<Photo> photos = dbHelper.getPhotosForTrip(tripId);

        ViewPager2 viewPager = findViewById(R.id.view_pager_recap);
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPager.setAdapter(new TripRecapAdapter(this, dayRecords, photos));
        viewPager.setPageTransformer((page, position) -> {
            float distance = Math.abs(position);
            page.setAlpha(1f - Math.min(distance, 0.55f));
            page.setScaleX(0.92f + (1f - Math.min(distance, 1f)) * 0.08f);
            page.setScaleY(0.92f + (1f - Math.min(distance, 1f)) * 0.08f);
        });
    }

    private void startBgm() {
        try {
            File bgmFile = createGeneratedBgmFile();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(bgmFile.getAbsolutePath());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
            updateMusicButton();
        } catch (Exception e) {
            btnMusic.setEnabled(false);
            btnMusic.setText("无音乐");
            Toast.makeText(this, "音乐暂时无法播放", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleMusic() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            userPausedMusic = true;
            mediaPlayer.pause();
        } else {
            userPausedMusic = false;
            mediaPlayer.start();
        }
        updateMusicButton();
    }

    private void updateMusicButton() {
        if (btnMusic == null || mediaPlayer == null) return;
        btnMusic.setText(mediaPlayer.isPlaying() ? "暂停" : "播放");
    }

    private void releaseBgm() {
        if (mediaPlayer == null) return;
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private File createGeneratedBgmFile() throws IOException {
        File bgmFile = new File(getCacheDir(), "trip_recap_bgm.wav");
        if (bgmFile.exists() && bgmFile.length() > 44) {
            return bgmFile;
        }

        int sampleRate = 8000;
        int durationSeconds = 4;
        int sampleCount = sampleRate * durationSeconds;
        byte[] pcm = new byte[sampleCount * 2];
        for (int i = 0; i < sampleCount; i++) {
            double t = i / (double) sampleRate;
            double wave = Math.sin(2 * Math.PI * 392 * t) * 0.25
                    + Math.sin(2 * Math.PI * 523.25 * t) * 0.18;
            short sample = (short) (wave * Short.MAX_VALUE);
            pcm[i * 2] = (byte) (sample & 0xff);
            pcm[i * 2 + 1] = (byte) ((sample >> 8) & 0xff);
        }

        FileOutputStream output = new FileOutputStream(bgmFile);
        writeWavHeader(output, pcm.length, sampleRate);
        output.write(pcm);
        output.flush();
        output.close();
        return bgmFile;
    }

    private void writeWavHeader(FileOutputStream output, int dataLength, int sampleRate) throws IOException {
        int byteRate = sampleRate * 2;
        output.write(new byte[]{
                'R', 'I', 'F', 'F',
                (byte) ((36 + dataLength) & 0xff), (byte) (((36 + dataLength) >> 8) & 0xff),
                (byte) (((36 + dataLength) >> 16) & 0xff), (byte) (((36 + dataLength) >> 24) & 0xff),
                'W', 'A', 'V', 'E',
                'f', 'm', 't', ' ',
                16, 0, 0, 0,
                1, 0,
                1, 0,
                (byte) (sampleRate & 0xff), (byte) ((sampleRate >> 8) & 0xff),
                (byte) ((sampleRate >> 16) & 0xff), (byte) ((sampleRate >> 24) & 0xff),
                (byte) (byteRate & 0xff), (byte) ((byteRate >> 8) & 0xff),
                (byte) ((byteRate >> 16) & 0xff), (byte) ((byteRate >> 24) & 0xff),
                2, 0,
                16, 0,
                'd', 'a', 't', 'a',
                (byte) (dataLength & 0xff), (byte) ((dataLength >> 8) & 0xff),
                (byte) ((dataLength >> 16) & 0xff), (byte) ((dataLength >> 24) & 0xff)
        });
    }
}
