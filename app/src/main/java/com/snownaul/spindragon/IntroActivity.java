package com.snownaul.spindragon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class IntroActivity extends AppCompatActivity {

    ImageView iv;

    //스케쥴관리 객체(비서 객체)
    Timer timer = new Timer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        iv=findViewById(R.id.iv);
        //View에게 애니메이션 효과를 주는 객체 생성
        //appear_logo.xml문서를 섞어서 Animation 객체로 생성
        Animation ani= AnimationUtils.loadAnimation(this,R.anim.appear_logo);
        iv.startAnimation(ani);



        //4초후에 MainActivity 실행!!!!
        timer.schedule(task,3000);

        //저장된 데이터들 로딩하기
        loadData();
    }

    void loadData(){
        SharedPreferences pref=getSharedPreferences("Data",MODE_PRIVATE);

        G.gem=pref.getInt("Gem",0);
        G.champion=pref.getInt("Champion",0);

        G.isMusic=pref.getBoolean("Music",true);
        G.isSound=pref.getBoolean("Sound",true);
        G.isVibrate=pref.getBoolean("Vibrate",true);

        G.championImg=pref.getString("ChampionImg",null);
    }

    //timer의 스케쥴링 작업을 수행하는 객체 생성
    TimerTask task=new TimerTask() {
        @Override
        public void run() {//이친구는 Thread다. start는 timer가 해준다.
            //스케쥴링에 의해 3초후에 이 메소드 실행
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
    };


}
