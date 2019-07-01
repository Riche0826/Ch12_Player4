package flag.com.tw.ch12_player4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements // 實做三個監聽介面
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        SensorEventListener {
    Uri uri;
    TextView txvName, txvUri;
    boolean isVideo = false;

    Button btnPlay, btnStop;
    CheckBox ckbLoop;
    MediaPlayer mper;
    Toast tos;
    SensorManager sm;
    Sensor sr;
    int delay = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //設定螢幕不休棉

        txvName = (TextView) findViewById(R.id.txvName);
        txvUri = (TextView) findViewById(R.id.txvUri);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnStop = (Button) findViewById(R.id.btnStop);
        ckbLoop = (CheckBox) findViewById(R.id.chbLoop);


        uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.welcome);

        txvName.setText("welcome.mp3");
        txvUri.setText("程式內的歌曲：" + uri.toString());

        mper = new MediaPlayer();
        mper.setOnPreparedListener(this);
        mper.setOnErrorListener(this);
        mper.setOnCompletionListener(this);
        tos = Toast.makeText(this, "", Toast.LENGTH_LONG);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sr = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        prepareMedia();

    }

    void prepareMedia() {
        btnPlay.setText("播放");
        btnPlay.setEnabled(false);
        btnStop.setEnabled(false);

        try{
            mper.reset();
            mper.setDataSource(this, uri);
            mper.setLooping(ckbLoop.isChecked());
            mper.prepareAsync();
        }catch (Exception e){
            tos.setText("指定音檔錯誤" + e.toString());
            tos.show();
        }
    }

    public void onPick(View v){
        Intent it = new Intent(Intent.ACTION_GET_CONTENT);

        if(v.getId() == R.id.btnPickAudio){
            it.setType("audio/*");
            startActivityForResult(it, 100);
        }else {
            it.setType("video/*");
            startActivityForResult(it, 101);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            isVideo = (requestCode == 101);
            uri = data.getData();
            txvName.setText(getFilename(uri));
            txvUri.setText("檔案 URI:" + uri.toString());
            prepareMedia();
        }
    }

    String getFilename(Uri uri) {
        String fileName = null;
        String[] colName = {MediaStore.MediaColumns.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(uri, colName, null, null, null);

        cursor.moveToFirst();
        fileName = cursor.getString(0);
        cursor.close();
        return fileName;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mper.seekTo(0); // 將播放位置歸0
        btnPlay.setText("播放");
        btnStop.setEnabled(false); // 停止紐設為不能按
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        tos.setText("發生錯誤導致停止");
        tos.show();
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        btnPlay.setEnabled(true); //準備好時 播放紐可以按
    }

    public void onMpPlay(View v){ // 按下播放後的動作
        if(isVideo){
            Intent it = new Intent(this, Video.class); // 建立開啟video Activity的intent

            it.putExtra("uri", uri.toString()); // 將影片的Uri以"uri"為名加入intent中

            startActivity(it);
            return; // 結束onMpPlay
        }

        if(mper.isPlaying()){
            mper.pause();
            btnPlay.setText("繼續");
        }else {
            mper.start();
            btnPlay.setText("暫停");
            btnStop.setEnabled(true);
        }
    }

    public void onMpStop(View v){ // 按下停止後的動作
        mper.pause();
        mper.seekTo(0); // 將播放位置歸0
        btnPlay.setText("播放");
        btnStop.setEnabled(false);
    }

    public void onMpLoop(View v){ // 選取重複播放
        if(ckbLoop.isChecked()){
            mper.setLooping(true);
        }else {
            mper.setLooping(false);
        }
    }

    public void onMpBackward(View v){ // 倒轉動作
        if(!btnStop.isEnabled()){ // 如果音樂沒播放則沒動作
            return;
        }

        int len = mper.getDuration(); // 取得音樂長度
        int pos = mper.getCurrentPosition(); // 取得現在音樂播放至幾分幾秒
        pos -= 10000; // 倒帶10秒
        if(pos < 0) pos = 0; // 如果現在音樂秒數小於0 將它歸0
        mper.seekTo(0); // 如果現在音樂秒數小於0 將它歸0
        tos.setText("倒退10秒" + pos / 1000 + "/" + len / 1000);
        tos.show();
    }

    public void onMpForward(View v){ // 快轉動作
        if(!btnStop.isEnabled()) { // 如果音樂沒播放則沒動作
            return;
        }

        int len = mper.getDuration(); // 取得音樂長度
        int pos = mper.getCurrentPosition(); // 取得現在音樂播放至幾分幾秒
        pos += 10000; // 快轉10秒
        if(pos > len) pos = len; // 如果播放時間超過音樂長度 將播放時間等於音樂長度
        mper.seekTo(pos); // 如果播放時間超過音樂長度 將播放時間等於音樂長度
        tos.setText("前進10秒" + pos / 1000 + "/" + len / 1000);
        tos.show();

    }

    public void onMpInfo(View v){ // 顯示播放至幾秒
        if(!btnStop.isEnabled()) { // 如果音樂沒播放則沒動作
            return;
        }

        int len = mper.getDuration(); // 取得音樂長度
        int pos = mper.getCurrentPosition(); // 取得現在音樂播放至幾分幾秒

        tos.setText("播放至：" + pos / 1000 + "/" + len / 1000);
        tos.show();
    }

    @Override
    protected void onPause(){
        super.onPause();

        if(mper.isPlaying()){
            btnPlay.setText("繼續");
            mper.pause();
        }
        sm.unregisterListener(this);
    }

    @Override
    protected void onDestroy(){
        mper.release();
        super.onDestroy();
    }

    @Override
    protected void onResume(){
        super.onResume();

        sm.registerListener(this, sr, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x, y, z;
        x = sensorEvent.values[0];
        y = sensorEvent.values[1];
        z = sensorEvent.values[2];

        if(Math.abs(x) < 1 && Math.abs(y) < 1 && z < -9){
            if(mper.isPlaying()){
                btnPlay.setText("繼續");
                mper.pause();
            }
        }else{
            if(delay > 0){
                delay-- ;
            }else {
                if(Math.abs(x) + Math.abs(y) + Math.abs(z) > 32){
                    if(btnPlay.isEnabled()){
                        onMpPlay(null);
                    }
                    delay = 5;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
