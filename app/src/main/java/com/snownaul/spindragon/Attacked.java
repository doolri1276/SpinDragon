package com.snownaul.spindragon;

import android.graphics.Bitmap;

/**
 * Created by alfo6-11 on 2018-04-04.
 */

public class Attacked {

    Bitmap img;
    int x,y;
    int w,h;
    boolean isDead=false;

    int life=15;

    public Attacked(Bitmap img, int x, int y) {

        this.img = img;
        this.x = x;
        this.y = y;
        w=img.getWidth()/2;
        h=img.getHeight()/2;
    }

    void move(int px,int py){
        x=px;
        y=py;
        life--;
        if(life<=0) isDead=true;
    }
}
