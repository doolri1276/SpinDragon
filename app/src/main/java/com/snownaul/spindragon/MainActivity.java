package com.snownaul.spindragon;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mp;
    SoundPool sp;
    int sdUiButton;

    View dialog=null;
    Animation ani;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mp=MediaPlayer.create(this,R.raw.dragon_flight);
        mp.setLooping(true);

        sp=new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        sdUiButton=sp.load(this,R.raw.ui_button,1);

    }

    @Override
    protected void onResume() {
        if(G.isMusic){
            mp.setVolume(0.5f,0.5f);//보통은 0.7로 많이 쓴다.
        }else{
            mp.setVolume(0,0);
        }

        mp.start();
        mp.setLooping(true);

        super.onResume();
    }

    @Override
    protected void onPause() {
        if(mp!=null&&mp.isPlaying()){
            mp.pause();
        }
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

    public void clickStart(View v){
        if(G.isSound) sp.play(sdUiButton,0.5f,0.5f,0,0,1);
        Intent intent=new Intent(this,GameActivity.class);
        startActivity(intent);
    }

    public void clickExit(View v){
        if(G.isSound) sp.play(sdUiButton,0.5f,0.5f,0,0,1);
        finish();
    }

    public void clickSetting(View v){
        appearDialog(R.id.dialog_setting);
    }

    //다이얼로그 보이는 작업 메소드
    void appearDialog(int resId){
        if(dialog!=null) return;
        if(G.isSound){
            sp.play(sdUiButton,1,1,1,0,1);
        }

        dialog=findViewById(resId);
        dialog.setVisibility(View.VISIBLE);

        ani= AnimationUtils.loadAnimation(this,R.anim.appear_dialog);
        dialog.startAnimation(ani);
    }

    void disappearDialog(){
        ani=AnimationUtils.loadAnimation(this,R.anim.disappear_dialog);
        ani.setAnimationListener(animationListener);
        dialog.startAnimation(ani);
    }

    Animation.AnimationListener animationListener=new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            dialog.setVisibility(View.GONE);
            dialog=null;

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };


}
