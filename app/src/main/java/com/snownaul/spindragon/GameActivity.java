package com.snownaul.spindragon;

import android.content.Intent;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class GameActivity extends AppCompatActivity {

    GameView gv;

    TextView tvScore;
    TextView tvCoin;
    TextView tvGem;
    TextView tvBomb;
    TextView tvChampion;

    View dialog=null;
    Animation ani;

    MediaPlayer mp;

    ToggleButton tbMusic;
    ToggleButton tbSound;
    ToggleButton tbVibrate;

    SoundPool sp;
    int sdUiButton;
    int sdRound01;

    ImageView[] lives=new ImageView[5];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gv=findViewById(R.id.gv);

        tvScore=findViewById(R.id.tv_score);
        tvCoin=findViewById(R.id.tv_coin);
        tvGem=findViewById(R.id.tv_gem);
        tvBomb=findViewById(R.id.tv_bomb);
        tvChampion=findViewById(R.id.tv_champion);

        mp=MediaPlayer.create(this,R.raw.my_friend_dragon);


        tbMusic=findViewById(R.id.tb_music);
        tbSound=findViewById(R.id.tb_sound);
        tbVibrate=findViewById(R.id.tb_vibrate);

        tbMusic.setOnCheckedChangeListener(checkedChangeListener);
        tbSound.setOnCheckedChangeListener(checkedChangeListener);
        tbVibrate.setOnCheckedChangeListener(checkedChangeListener);



//        tbMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(G.isSound){
//                    sp.play(sdUiButton,1,1,1,0,1);
//                }
//                G.isMusic=isChecked;
//                if(G.isMusic){
//                    mp.setVolume(0.5f,0.5f);
//                }
//                else mp.setVolume(0,0);
//            }
//        });
//
//        tbSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                G.isSound=isChecked;
//
//                if(isChecked){
//                    sp.play(sdUiButton,0.5f,0.5f,1,0,1);
//                }
//
//            }
//        });
//
//        tbVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(G.isSound){
//                    sp.play(sdUiButton,1,1,1,0,1);
//                }
//                G.isVibrate=isChecked;
//            }
//        });

        sp=new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        sdUiButton=sp.load(this,R.raw.ui_button,1);
        sdRound01=sp.load(this,R.raw.round_1,1);

        for(int i=0;i<lives.length;i++){
            lives[i]=findViewById(R.id.life01+i);
        }

        tbMusic.setChecked(G.isMusic);
        tbSound.setChecked(G.isSound);
        tbVibrate.setChecked(G.isVibrate);



    }

    CompoundButton.OnCheckedChangeListener checkedChangeListener=new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()){
                case R.id.tb_music:
                    Log.i("MyTag","null 확인하자 : G.isSound : "+G.isSound);
                    Log.i("MyTag","null 확인하자 : sp : "+(sp==null));
                    Log.i("MyTag","null 확인하자 : sdui : "+sdUiButton);
                    if(G.isSound) sp.play(sdUiButton,0.5f,0.5f,0,0,1);
                    G.isMusic=isChecked;
                    if(G.isMusic)mp.setVolume(0.5f,0.5f);
                    else mp.setVolume(0,0);
                    break;

                case R.id.tb_sound:
                    G.isSound=isChecked;
                    if(G.isSound) sp.play(sdUiButton,0.5f,0.5f,0,0,1);
                    break;

                case R.id.tb_vibrate:
                    if(G.isSound) sp.play(sdUiButton,0.5f,0.5f,0,0,1);
                    G.isVibrate=isChecked;

                    break;
            }
        }
    };

    @Override
    protected void onResume() {



        if(mp!=null){
            if(G.isMusic)mp.setVolume(0.5f,0.5f);
            else mp.setVolume(0,0);
        }
        mp.start();
        mp.setLooping(true);

        sp.play(sdRound01,1,1,1,0,1);

        super.onResume();
    }

    //액티비티가 화면에서 보이지 않게 되면 자동으로 실행되는 메소드
    @Override
    protected void onPause() {

        if(mp!=null)mp.pause();

        gv.pauseGame();
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if(mp!=null){
            mp.stop();
            mp.release();
            mp=null;
        }

        if(sp!=null){
            sp.release();
            sp=null;
        }

        super.onDestroy();
    }

    //액티비티의 뒤로가기 버튼을 클릭했을때 자동으로 실행되는 메소드
    @Override
    public void onBackPressed() {
        if(dialog!=null) return;

        gv.pauseGame();

        //다이얼로그 보이기
        dialog=findViewById(R.id.dialog_quit);
        ani= AnimationUtils.loadAnimation(this,R.anim.appear_dialog_quit);
        dialog.setVisibility(View.VISIBLE);
        dialog.startAnimation(ani);
    }

    public void clickPause(View v){
        if(dialog!=null) return;

        gv.pauseGame();

        dialog=findViewById(R.id.dialog_pause);
        ani=AnimationUtils.loadAnimation(this,R.anim.appear_dialog_pause);
        dialog.setVisibility(View.VISIBLE);
        dialog.startAnimation(ani);

    }

    public void clickQuit(View v){

        if(dialog!=null) return;

        gv.pauseGame();

        //다이얼로그 보이기
        dialog=findViewById(R.id.dialog_quit);
        ani= AnimationUtils.loadAnimation(this,R.anim.appear_dialog_quit);
        dialog.setVisibility(View.VISIBLE);
        dialog.startAnimation(ani);

    }

    public void clickShopClass(View v){
        appearDialog(R.id.dialog_shop);
        //clickDialog(v);
    }

    public void clickShopItem(View v){
        appearDialog(R.id.dialog_shop);
        //clickDialog(v);
    }

    public void clickSetting(View v){
        appearDialog(R.id.dialog_setting);
        //clickDialog(v);
    }

    //다이얼로그 보이는 작업 메소드
    void appearDialog(int resId){
        if(dialog!=null) return;
        if(G.isSound){
            sp.play(sdUiButton,1,1,1,0,1);
        }
        gv.pauseGame();

        dialog=findViewById(resId);
        dialog.setVisibility(View.VISIBLE);

        ani=AnimationUtils.loadAnimation(this,R.anim.appear_dialog);
        dialog.startAnimation(ani);
    }

    void disappearDialog(){
        ani=AnimationUtils.loadAnimation(this,R.anim.disappear_dialog);
        ani.setAnimationListener(animationListener);
        dialog.startAnimation(ani);
    }

    public void clickBtn(View v){
        if(G.isSound){
            sp.play(sdUiButton,1,1,1,0,1);
        }
        switch (v.getId()){
            case R.id.dialog_quit_ok:
                //게임 종료

                //GameThread의 동작 종료..
                gv.stopGame();

                finish();
                break;

            case R.id.dialog_quit_cancel:
                dialog.setVisibility(View.GONE);
                dialog=null;

                gv.resumeGame();

                break;

            case R.id.dialog_pause_play:
                ani=AnimationUtils.loadAnimation(this,R.anim.dissappear_dialog_pause);
                ani.setAnimationListener(animationListener);
                dialog.startAnimation(ani);

                break;

            case R.id.dialog_shop_check:
                disappearDialog();
                break;

            case R.id.dialog_setting_check:
                disappearDialog();
        }//switch 문
    }//clickBtn()

    Animation.AnimationListener animationListener=new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            dialog.setVisibility(View.GONE);
            dialog=null;

            gv.resumeGame();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    //GameThread로 부터 메세지를 전달받는 객체 생성
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            gv.stopGame();

            Bundle data=msg.getData();

            Intent intent=new Intent(GameActivity.this,GameoverActivity.class);
            intent.putExtra("Data",data);
            startActivity(intent);

            finish();
        }
    };

}//GameActivity CLASS
