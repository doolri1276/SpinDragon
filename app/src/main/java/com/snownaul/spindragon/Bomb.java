package com.snownaul.spindragon;

import android.graphics.Bitmap;

import java.util.Random;

/**
 * Created by alfo6-11 on 2018-04-04.
 */

public class Bomb {

    int width,height;

    Bitmap[] img;
    int[] x;
    int[] y;
    int[] rad;

    double[] radian;
    int[] speed;

    boolean isDead=false;
    int life=20;
    int alpha=255;

    public Bomb(int width, int height, Bitmap[] img) {
        this.width = width;
        this.height = height;
        this.img = img;
        radian=new double[img.length];
        x=new int[img.length];
        y=new int[img.length];
        rad=new int[img.length];
        speed=new int[img.length];

        for(int i=0;i<x.length;i++){
            rad[i]=img[i].getWidth()/2;
        }

        Random rnd=new Random();

        for(int i=0;i<img.length;i++){
            x[i]=rnd.nextInt(width-rad[i]*2)+rad[i];
            y[i]=rnd.nextInt(height-rad[i]*2)+rad[i];
            int angle=rnd.nextInt(360);
            radian[i]=Math.toRadians(angle);

            speed[i]=rad[i]/8;

        }
    }

    void move(){
        for(int i=0;i<img.length;i++){
            x[i]=(int)(x[i]+Math.cos(radian[i])*speed[i]);
            y[i]=(int)(y[i]-Math.sin(radian[i])*speed[i]);
        }

        life--;
        alpha-=30;
        if(life<=0) isDead=true;
    }
}
