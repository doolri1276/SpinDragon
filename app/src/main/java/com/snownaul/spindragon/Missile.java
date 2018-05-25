package com.snownaul.spindragon;

import android.graphics.Bitmap;

/**
 * Created by alfo6-11 on 2018-03-29.
 */

public class Missile {

    int width,height;

    Bitmap img;
    int x,y;
    int w,h;
    boolean isDead=false;

    double radian;  //이동 각도
    int speed;      //이동 속도

    int angle;      //회전 각도

    int kind;       //종류

    public Missile(int width, int height, Bitmap[] imgs, int px, int py, int pAng, int pKind) {
        this.width = width;        this.height = height;
        x=px; y=py;
        kind=pKind;

        img=imgs[kind];
        w=img.getWidth()/2;
        h=img.getHeight()/2;

        speed=w/4;

        angle=pAng;
        radian=Math.toRadians(270-angle);


    }

    void move(){

        x=(int)(x+Math.cos(radian)*speed);
        y=(int)(y-Math.sin(radian)*speed);

        //화면밖에 나갔는가?
        if( x<-w || x>width+w || y<-h || y>height+h ){
            isDead=true;
        }

    }

}
