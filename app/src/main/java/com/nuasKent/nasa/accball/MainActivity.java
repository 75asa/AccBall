package com.nuasKent.nasa.accball;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity
        implements SensorEventListener, SurfaceHolder.Callback {

    // センサー全体を管理するクラス
    SensorManager mSensorManager;
    // 子センサークラスインスタンス
    Sensor mAccSensor;
    // サーフィス＝描画専用クラス
    SurfaceHolder mHolder;
    // サーフェスビューの幅
    int mSurfaceWidth;
    // サーフェスビューの高さ
    int mSurfaceHeight;

    static final float RADIUS = 50.0f;      // ボールを描画する時の半径を現す定数
    static final float COEF = 1000.0f;      // ボールの移動量を調整するための係数

    float mBallX;       // ボールの現在のx座標
    float mBallY;       // ボールの現在のy座標
    float mVX;          // ボールのx軸方向への速度
    float mVY;          // ボールのy軸方向への速度

    long mFrom;         // 前回、センサーから加速度を取得した時間
    long mTo;           // 今回、センサーから加速度を取得した時間

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 画面が回転しないように縦にロック
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        // Androidシステムに用意された各種サービスを管理するマネージャクラスを生成するメソッド
        mSensorManager =
                (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        // Sensorクラスのインスタンスを取得、センサーの型は加速度
        mAccSensor = mSensorManager.
                getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SurfaceView surfaceView =
                (SurfaceView)findViewById(R.id.surfaceView);
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Debug用にセンサーの座標を記録する
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Log.d("MainActivity",
                            "x=" + String.valueOf(event.values[0]) +
                            "y=" + String.valueOf(event.values[1]) +
                            "z=" + String.valueOf(event.values[2]));

            // センサーから取得した書く座標の加速度データを変数に各々格納
            // x軸の値は、画面の描画方向に合わせる為に反転
            float x = -event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // currentTimeMills()で現在の時間をミリ秒単位で取得可能
            // 前回センサーから取得した時間と今回のとの差を取得することで経過時間を取得
            // ミリ秒単位の経過時間を秒単位に最後に修正
            mTo = System.currentTimeMillis();
            float t = (float)(mTo - mFrom);
            t = t /1000.0f;

            // x軸、y軸方向各々の移動距離を計算
            float dx = mVX * t + x * t * t / 2.0f;
            float dy = mVY * t + y * t * t / 2.0f;
            // 移動後のボールのx・y座標を取得
            // 係数COEFは移動距離dx、dyがメートル単位のためPixel単位の画面表示に最適化するため
            mBallX = mBallX + dx * COEF;
            mBallY = mBallY + dy * COEF;
            // 加速度センサーからの値を受け取った時に再度計算をする為に、測度をmVX, mVYに保存
            mVX = mVX + x * t;
            mVY = mVY + y * t;

            // ボールが画面内に収まるように、x座標が０より小さい時と、サーフェスの幅より大きい場合に測度を反転
            // ボールの半径RADIUSも考慮、確実に処理する為に速度の方向も確認する
            if (mBallX - RADIUS < 0 && mVX < 0) {
                mVX = -mVX / 1.5f;
                mBallX = RADIUS;
            } else if (mBallX + RADIUS > mSurfaceWidth && mVX > 0) {
                mVX = -mVX / 1.5f;
                mBallX = mSurfaceWidth - RADIUS;
            }

            // ☝︎と同じようにy座標も速度を反転させる
            if (mBallY - RADIUS < 0 && mVY < 0) {
                mVY = -mVY / 1.5f;
                mBallY = RADIUS;
            } else if (mBallY + RADIUS > mSurfaceHeight && mVY > 0) {
                mVY = -mVY /1.5f;
                mBallY = mSurfaceHeight - RADIUS;
            }

            // ボール移動後の現在時刻を変数に取得
            mFrom = System.currentTimeMillis();
            // ボールの描画書き出し
            drawCanvas();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /*
    @Override
    // センサーの監視を開始する
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccSensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    // センサーの監視を終了する
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
    */

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // 経過時間の初期設定
        mFrom = System.currentTimeMillis();
        mSensorManager.registerListener(this, mAccSensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;

        // ボール位置の初期設定
        mBallX = width / 2;
        mBallY = height / 2;
        mVX = 0;
        mVY = 0;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSensorManager.unregisterListener(this);
    }

    private void drawCanvas() {
        // インスタンスを取得しサーフェスをロックする
        // これにより描画スレッドにて自前に同期処理を行う必要がなくなる
        Canvas c = mHolder.lockCanvas();
        c.drawColor(Color.YELLOW);
        Paint paint = new Paint();
        paint.setColor(Color.MAGENTA);
        c.drawCircle(mBallX, mBallY, RADIUS, paint);
        mHolder.unlockCanvasAndPost(c);
    }
}
