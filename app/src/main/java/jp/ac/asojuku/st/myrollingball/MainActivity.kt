package jp.ac.asojuku.st.myrollingball

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import java.util.*


class MainActivity : AppCompatActivity() ,SensorEventListener,SurfaceHolder.Callback{


    private var surfaceWidth:Int =0     //サーフェスの幅
    private var surfaceHeight:Int= 0    //サーフェスの高さ

    private val radius = 20.0f          //ボールの半径を表す定数
    private val coef = 1000.0f          //ボールの移動量を調整するための係数

    private var ballX :Float = 0f       //ボールの現在のx座標
    private var ballY :Float = 0f       //ボールの現在のy座標
    private var vx :Float = 0f          //ボールのx方向への加速度
    private var vy :Float = 0f          //ボールのy方向への加速度
    private var time:Long =0L           //前回時間の保持

    private var flag =false             //フラグ

    //ブロックの縦横
    private var hidari :Float =0f
    private var migi :Float =0f
    private var ue:Float =0f
    private var sita :Float =0f

    //ブロック2の縦横
    private var hidari2 :Float =0f
    private var migi2 :Float =0f
    private var ue2:Float =0f
    private var sita2 :Float =0f

    //ゴールの縦横
    private var hidarig :Float =0f
    private var migig :Float =0f
    private var ueg:Float =0f
    private var sitag :Float =0f



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT //画面を縦方向にロックする

        setContentView(R.layout.activity_main)

        val holder = surfaceView.holder
        holder.addCallback(this)
    }

    override fun onResume() {
        super.onResume()

        reset_button.setOnClickListener{resetTapped()}
        blocksize()
        goalsize()
        if(flag == true){
            game_text.setText("GAME OVER")
        }
    }


    //使わない
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event ==null){ return }

        //ボールの描画の計算処理
        if(time==0L){
            time = System.currentTimeMillis()   //最初のタイミングでは現在時刻を保持
        }




        if(event.sensor.type == Sensor.TYPE_ACCELEROMETER){

            //センサーのx.yの値を取得
            val x = -event.values[0]
            val y = event.values[1]

            //経過時間を計算(今の時間-前の時間=経過時間)
            var t = (System.currentTimeMillis()-time).toFloat()
            //今の時間を「前の時間」として保存
            time = System.currentTimeMillis()
            t /=1000.0f

            //移動距離を計算(ボールをどれくらい動かすか)
            val dx = vx * t + x * t * t /2.0f
            val dy = vy * t + y * t * t /2.0f

            ballX += dx * coef

            //メートルをピクセルのCMに補正してボールのY座標に足しこむ
            ballY += dy * coef

            //今の各方向の加速度を更新
            vx += x*t
            vy += y*t

            //画面の端に来たら跳ね返る処理
            //左右について
            if(ballX - radius < 0 && vx < 0){
                vx = -vx /1.5f
                ballX = radius



            }else if(ballX + radius > surfaceWidth && vx>0){
                vx = -vx /1.5f
                ballX = surfaceWidth -radius

            }
            //上下について
            if(ballY - radius < 0 && vy < 0){
                //上にぶつかった時
                vy = -vy /1.5f
                ballY = radius

            }else if(ballY + radius > surfaceHeight && vy>0){
                //にぶつかった時
                vy = -vy /1.5f
                ballY = surfaceHeight -radius

            }

            //障害物ブロックの当たり判定
            if(ballX  + radius >hidari && ballX  - radius <migi && ballY  + radius>ue && ballY  - radius<sita){

                gameOver()
            }

            //障害物ブロック2の当たり判定
            if(ballX  + radius >hidari2 && ballX  - radius <migi2 && ballY  + radius>ue2 && ballY  - radius<sita2){

                gameOver()
            }

            //ゴールの当たり判定
            if(ballX  + radius >hidarig && ballX  - radius <migig && ballY  + radius>ueg && ballY  - radius<sitag){

                gameClear()
            }


            blocksize()
            drawCanvas()


        }
    }



    //RESET 後
    override fun surfaceChanged(p0: SurfaceHolder?, format: Int, wight: Int, height: Int) {
        surfaceWidth = wight
        surfaceHeight = height

        //ボール初期位置の設定
        ballreset()
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        //加速度センサーのリスナーを登録する流れ
        //センサーマネージャを取得
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //センサーマネージャから加速度センサーを取得
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        //加速度センサーのリスナーをOSに登録
        sensorManager.registerListener(
                this,   //リスナー
                accSensor,      //加速度センサー
                SensorManager.SENSOR_DELAY_GAME //センシングの頻度
        )
    }

    private fun drawCanvas(){
        val canvas = surfaceView.holder.lockCanvas()
        canvas.drawColor(Color.RED)
        canvas.drawCircle(ballX,ballY,radius, Paint().apply {
            color = Color.CYAN
        })


        //障害物ブロック
        canvas.drawRect(hidari,ue,migi,sita,Paint().apply {
            color = Color.GREEN
        })
        //障害物ブロック2
        canvas.drawRect(hidari2,ue2,migi2,sita2,Paint().apply {
            color = Color.GREEN
        })



        //ゴール
        canvas.drawRect(hidarig,ueg,migig,sitag,Paint().apply {
            color = Color.BLACK
        })
        surfaceView.holder.unlockCanvasAndPost(canvas)
    }

    //ボールを初期位置に戻す
    private fun ballreset(){
        ballX = (surfaceHeight-radius+10).toFloat()
        ballY = (surfaceWidth-radius+10).toFloat()
    }

    private fun gameOver(){
        game_text.setText("GAME OVER")
        flag =true
    }

    private fun gameClear(){
        game_text.setText("GAME CLEAR")
        flag = true
    }

    //Resetボタンが押されたらボールを初期位置に戻し、文字を初期に戻す
    private fun resetTapped(){
        ballreset()
        blocksize()
        goalsize()
        game_text.setText("")
        vx = 0f          //ボールのx方向への加速度
        vy = 0f          //ボールのy方向への加速度
        flag = false
    }

    //ブロックのサイズを決定する
    private fun blocksize(){
        //乱数でブロックの縦横のサイズを決める
        migi = Random().nextInt(400)+100.toFloat()
        sita = Random().nextInt(400)+100.toFloat()
        ue = Random().nextInt(200)+100.toFloat()
        hidari = Random().nextInt(100)+100.toFloat()

        migi2 = Random().nextInt(660)+50.toFloat()
        sita2 = Random().nextInt(800)+100.toFloat()
        ue2 = Random().nextInt(500)+100.toFloat()
        hidari2 = Random().nextInt(600)+50.toFloat()

    }

    private fun goalsize(){
        //乱数での縦横のサイズを決める
        migig = Random().nextInt(100)+20.toFloat()
        sitag = Random().nextInt(200)+40.toFloat()
        ueg = Random().nextInt(70)+40.toFloat()
        hidarig = Random().nextInt(50)+20.toFloat()
    }



}
