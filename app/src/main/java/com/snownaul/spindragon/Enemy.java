package com.snownaul.spindragon;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created by alfo6-11 on 2018-03-30.
 */

public class Enemy {

    int width,height;

    Bitmap img;
    int x,y;
    int w,h;
    boolean isDead=false;   //죽었는가? -> 점수 획득
    boolean isOut=false;    //화면밖으로 나갔는가? -> 점수 노노
    boolean wasShown=false; //화면에 보여진 적이 있는가?

    Rect rect;//화면 사이즈 사각형 좌표 관리 객체

    Bitmap[] imgs;
    int index;//날개짓 이미지 번호
    int loop=0;

    int kind;//종류

    double radian;  //이동 각도
    int speed;      //이동 속도

    int angle;      //회전 각도
    int HP;

    Bitmap[] imgGs;
    Bitmap imgG;

    public Enemy(int width, int height, Bitmap[][] imgSrc, int px, int py, Bitmap[][] imgGauges) {

        this.width = width; this.height = height;

        Random rnd=new Random();
        //종류 [0]white, [1]yellow, [2]pink
        int n=rnd.nextInt(10);
        kind=n<5?0:n<8?1:2;

        //체력 white:1, yellow:5, pink:3
        HP=kind==0?1:kind==1?5:3;
        imgs=imgSrc[kind];
        img=imgs[index];
        w=img.getWidth()/2;
        h=img.getHeight()/2;

        int a=rnd.nextInt(360);
        double r=Math.toRadians(a);
        x= (int) (width/2+Math.cos(r)*width);
        y= (int) (height/2-Math.sin(r)*width);

        radian=Math.atan2(y-py,px-x);
        angle= (int) (270-Math.toDegrees(radian));

        speed=kind==0?w/4:kind==1?w/7:w/10;

        //화면 사이즈 사각형 객체 생성
        rect=new Rect(0,0,width,height);

        if (kind > 0) {
            imgGs=imgGauges[kind-1];
            imgG=imgGs[0];
        }


    }//Enemy 생성자

    void damaged(int n){
        HP-=n;

        if(HP<=0){
            isDead=true;
            return;
        }

        if(imgG!=null){
            imgG=imgGs[imgGs.length-HP];
        }

    }

    void move(int px,int py){

        //pink인 적군은 각도를 움직일때마다 다시 계산
        if(kind==2){
            radian=Math.atan2(y-py,px-x);
            angle= (int) (270-Math.toDegrees(radian));
        }

        //날개짓 애니메이션
        loop++;
        if(loop%3==0){
            index++;
            if(index>3) index=0;

            img=imgs[index];
        }


        //이동
        x=(int)(x+Math.cos(radian)*speed);
        y=(int)(y-Math.sin(radian)*speed);

        if (rect.contains(x,y))wasShown=true;

        //화면에 한번이라도 보인 적이 있는가?
        if(wasShown){
            //화면 밖으로 나갔는지
            if(x<-w||x>width+w||y<-h||y>height+h){
                isOut=true;
            }
        }




    }//move()
}
