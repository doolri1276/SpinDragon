package com.snownaul.spindragon;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by alfo6-11 on 2018-03-28.
 */

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    Context context;
    SurfaceHolder holder;

    int width,height;
    GameThread gameThread;

    public GameView(Context context, AttributeSet attrs) {//xml로 만들거면 2개짜리로 해야하는 거임

        super(context, attrs);
        this.context=context;
        holder=getHolder();
        holder.addCallback(this);

    }//GameView 생성자

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //생성자 끝나고 이 GameView가 화면에 보여지면 자동 호출


    }//surfaceCreated()

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // (화면이 가려졌다 나올때마다 또 실행이 된다.)
        //surfaceCreated()가 실행된 후 자동 실행
        //Game진행 작업 시작!!
        if(gameThread==null){//객체 없는가? 처음 시작했는가?
            this.width=getWidth();
            this.height=getHeight();

            gameThread =new GameThread();
            gameThread.start();
        }else{
            //게임 재시작!! (resume)
            gameThread.resumeThread();



        }

    }//surfaceChanged()

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //GameView가 화면에서 보이지 않으면 자동 실행

    }//surfaceDestroyed()

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action=event.getAction();
        int x,y;
        switch (action){
            case MotionEvent.ACTION_DOWN:
                x=(int)event.getX();
                y=(int)event.getY();

                gameThread.touchDown(x,y);

                break;
            case MotionEvent.ACTION_UP:
                x=(int)event.getX();
                y=(int)event.getY();

                gameThread.touchUp(x,y);

                break;
            case MotionEvent.ACTION_MOVE:
                x=(int)event.getX();
                y=(int)event.getY();

                gameThread.touchMove(x,y);

                break;
        }

        return true;
    }//onTouchEvent()

    void stopGame(){
        gameThread.stopThread();
    }

    void pauseGame(){
        gameThread.pauseThread();
    }

    void resumeGame(){
        gameThread.resumeThread();
    }

    //Inner class...////////////////////////////////////////////////////////////////////////////////
    //Game의 모든 작업을 수행하는 직원객체(Thread)
    class GameThread extends Thread{

        boolean isRun=true;
        boolean isWait=false;

        Bitmap imgBack;
        Bitmap imgJoypad;
        Bitmap[] imgMissile=new Bitmap[3];
        Bitmap[][] imgPlayer=new Bitmap[3][4];
        Bitmap[][] imgEnemy=new Bitmap[3][4];
        Bitmap[][] imgGauge=new Bitmap[2][];
        Bitmap[] imgDust =new Bitmap[6];
        Bitmap[] imgItem=new Bitmap[7];
        Bitmap[] imgBombs=new Bitmap[30];
        Bitmap imgProtect;
        Bitmap imgStrong;
        Bitmap imgBomb;
        Bitmap imgAttacked;

        //폭탄버튼변수들
        Rect recBomb;
        boolean isBomb;
        Paint bombPaint=new Paint();

        int protectRad;//보호막이미지의 반지름
        int protectAng;//보호막이미지 회전각도

        int posBack=0;  //배경 이미지의 x좌표

        //조이패드 변수들
        int jpx,jpy,jpr;    //조이패드 이미지의 중심좌표
        boolean isJoypad=false;

        //Bitmap의 투명도(alpha)를 적용하기 위한 Paint
        Paint paint=new Paint();

        //플레이어 객체 참조변수
        Player me;
        int playerKind;//플레이어 종류

        //미사일들 리스트 객체
        ArrayList<Missile> missiles=new ArrayList<>();
        //적군들 리스트
        ArrayList<Enemy> enemies=new ArrayList<>();
        //Dust들 리스트
        ArrayList<Dust> dusts=new ArrayList<>();
        //Item들 리스트
        ArrayList<Item> items=new ArrayList<>();
        //Bomb들 리스트
        ArrayList<Bomb> bombs=new ArrayList<>();
        //공격받은 효과 리스트
        ArrayList<Attacked> attackeds=new ArrayList<>();

        Random rnd=new Random();

        //미사일 발사 간격
        int missileGap=3;

        int level=0;
        int[] levelScores;

        //아이템 지속시간
        int fastItem=0;
        int protectItem=0;
        int magnetItem=0;
        int strongItem=0;

        int attackedTime=0;


        int bomb=3;//폭탄 개수
        int score=0;
        int coin=0;

        SoundPool sp;
        int sdChDie, sdFire, sdCoin, sdGem, sdProtect, sdItem, sdMonDie, sdExplosion;
        int sdLevelUp,sdBegin;

        Vibrator vibrator; //진동관리자..

        //초기값 설정작업 메소드 (마치 생성자처럼)
        void init(){
            //그림들을 Bitmap객체로 생성!!
            createBitmaps();

            //플레이어 객체 생성!!!
            playerKind=rnd.nextInt(3);
            me=new Player(width,height,imgPlayer,playerKind);

            //조이패드 초기값
            jpx=jpr+10;
            jpy=height-jpr-10;

            //TextView들에 값 설정!!
            setTextView();

            //효과음변수들 작업
            sp=new SoundPool(10, AudioManager.STREAM_MUSIC,0);//기본값 0
            //maxStreams = 내가 동시에 플레이 가능한 음원 갯수(보통 최대 10개쓴다.)
            //game의 음악은 음질 그대로 STREAM_MUSIC을 쓰는 것을 권장하고있다.
            //음원에 우선순위를 줄 수 있다. 숫자가 높을수록 나 틀어줘! 가 된다.
            //우선순위
            // 5 sdChDie
            // 4 sdProtect
            // 3 sdItem
            // 3 sdGem
            // 2 sdCoin
            // 1 sdMonDie
            // 0 sdFire

            sdChDie=sp.load(context,R.raw.ch_die,1);
            sdFire=sp.load(context,R.raw.fireball,1);
            sdCoin=sp.load(context,R.raw.get_coin,1);
            sdGem=sp.load(context,R.raw.get_gem,1);
            sdProtect=sp.load(context,R.raw.get_invincible,1);
            sdItem=sp.load(context,R.raw.get_item,1);
            sdMonDie=sp.load(context,R.raw.mon_die,1);
            sdExplosion=sp.load(context,R.raw.bigexplosion,1);

            sdBegin=sp.load(context,R.raw.begin,1);
            sdLevelUp=sp.load(context,R.raw.level_up,1);

            //진동관리자 객체 얻어오기
            vibrator= (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

            levelScores=new int[]{1,500,1000,2000,4000,8000,10000,30000,60000,100000, 150000, 210000, 280000, 360000};

        }//init()

        //GameActivity의 TextView에 값 설정
        void setTextView(){
            //Activity클래스의 runOnUIThread()와 같은 역할을 하는 메소드
            post(new Runnable() {
                @Override
                public void run() {

                    GameActivity ga=(GameActivity)context;
                    String s;

                    s=String.format("%07d",score);
                    ga.tvScore.setText(s);

                    s=String.format("%04d",coin);
                    ga.tvCoin.setText(s);

                    s=String.format("%04d",G.gem);
                    ga.tvGem.setText(s);

                    s=String.format("%04d",bomb);
                    ga.tvBomb.setText(s);

                    s=String.format("%07d",G.champion);
                    ga.tvChampion.setText(s);


                }
            });
        }

        //그림을 Bitmap객체로 만들어내는 작업 메소드
        void createBitmaps(){

            Resources res=context.getResources();

            //공격당한 이미지
            imgAttacked=BitmapFactory.decodeResource(res,R.drawable.critical);
            imgAttacked=Bitmap.createScaledBitmap(imgAttacked,height/4,height/4,true);

            //폭탄 버튼 이미지
            int bombSize=height/5;
            imgBomb=BitmapFactory.decodeResource(res,R.drawable.btn_bomb);
            imgBomb=Bitmap.createScaledBitmap(imgBomb,bombSize,bombSize,true);
            recBomb=new Rect(width-bombSize,height-bombSize,width,height);

            //보호막 이미지
            imgProtect=BitmapFactory.decodeResource(res,R.drawable.effect_protect);
            imgProtect=Bitmap.createScaledBitmap(imgProtect,height/3,height/3,true);
            protectRad=imgProtect.getWidth()/2;

            //강화 미사일 이미지
            imgStrong=BitmapFactory.decodeResource(res,R.drawable.bullet_04);
            imgStrong=Bitmap.createScaledBitmap(imgStrong,height/10,height/10,true);

            //Item 이미지
            for(int i=0;i<7;i++){
                imgItem[i]=BitmapFactory.decodeResource(res,R.drawable.item_0_coin+i);
                imgItem[i]=Bitmap.createScaledBitmap(imgItem[i],height/16,height/16,true);
            }

            //Dust 이미지
            Bitmap img=BitmapFactory.decodeResource(res,R.drawable.dust);
            float[] ratio=new float[]{1.0f, 1.4f, 0.5f, 0.1f,0.3f, 1.7f};
            for(int i=0;i<6;i++){
                int size= (int) (height/9*ratio[i]);
                imgDust[i]=Bitmap.createScaledBitmap(img, size, size,true);
            }

            //Bomb 이미지
            Bitmap bombImg=BitmapFactory.decodeResource(res,R.drawable.explosion_01);
            ratio=new float[]{1f,1.3f,1.5f,0.7f,2f,2.5f,2.2f,3.2f,2.8f,1f,0.7f,2f,2.5f,2.2f,3.2f,1f,1.3f,1.5f,0.7f,2f,2.5f,2.2f,3.2f,2.8f,1f,0.7f,2f,2.5f,2.2f,3.2f};
            for(int i=0;i<ratio.length;i++){
                int size=(int)(height/8*ratio[i]);
                imgBombs[i]=Bitmap.createScaledBitmap(bombImg,size,size,true);
            }

            //Guage 이미지
            imgGauge[0]=new Bitmap[5];
            imgGauge[1]=new Bitmap[3];
            for(int i=0;i<imgGauge.length;i++){
                for(int k=0;k<imgGauge[i].length;k++){
                    if(i==0){
                        imgGauge[i][k]=BitmapFactory.decodeResource(res,R.drawable.gauge_step5_01+k);
                        imgGauge[i][k]=Bitmap.createScaledBitmap(imgGauge[i][k],height/9,height/36,true);
                    }else{
                        imgGauge[i][k]=BitmapFactory.decodeResource(res,R.drawable.gauge_step3_01+k);
                        imgGauge[i][k]=Bitmap.createScaledBitmap(imgGauge[i][k],height/9,height/36,true);
                    }


                }
            }

            //미사일 이미지
            for(int i=0;i<3;i++){
                imgMissile[i]=BitmapFactory.decodeResource(res,R.drawable.bullet_01+i);
                imgMissile[i]=Bitmap.createScaledBitmap(imgMissile[i],height/10,height/10,true);
            }

            //조이패드 이미지
            imgJoypad=BitmapFactory.decodeResource(res,R.drawable.img_joypad);
            imgJoypad=Bitmap.createScaledBitmap(imgJoypad,height/3,height/3,true);
            jpr=imgJoypad.getWidth()/2;

            //배경 이미지
            imgBack=BitmapFactory.decodeResource(res,R.drawable.back_01+rnd.nextInt(6));
            imgBack=Bitmap.createScaledBitmap(imgBack,width,height,true);

            //적군 이미지
            for(int i=0;i<3;i++){
                for(int j=0;j<3;j++){
                    imgEnemy[i][j]=BitmapFactory.decodeResource(res,R.drawable.enemy_a_01+(i*3)+j);
                    imgEnemy[i][j]=Bitmap.createScaledBitmap(imgEnemy[i][j],height/9,height/9,true);
                }
                imgEnemy[i][3]=imgEnemy[i][1];
            }

            //플레이어 이미지
            for(int i=0;i<3;i++){
                imgPlayer[0][i]=BitmapFactory.decodeResource(res,R.drawable.char_a_01+i);
                imgPlayer[0][i]=Bitmap.createScaledBitmap(imgPlayer[0][i],height/8,height/8,true);
                                                                            //기반으로 할 때는 w/h중 짧은 쪽을 잡아랏!
            }
            imgPlayer[0][3]=imgPlayer[0][1];

            for(int i=0;i<3;i++){
                imgPlayer[1][i]=BitmapFactory.decodeResource(res,R.drawable.char_b_01+i);
                imgPlayer[1][i]=Bitmap.createScaledBitmap(imgPlayer[1][i],height/8,height/8,true);
                //기반으로 할 때는 w/h중 짧은 쪽을 잡아랏!
            }
            imgPlayer[1][3]=imgPlayer[1][1];

            for(int i=0;i<3;i++){
                imgPlayer[2][i]=BitmapFactory.decodeResource(res,R.drawable.char_c_01+i);
                imgPlayer[2][i]=Bitmap.createScaledBitmap(imgPlayer[2][i],height/8,height/8,true);
                //기반으로 할 때는 w/h중 짧은 쪽을 잡아랏!
            }
            imgPlayer[2][3]=imgPlayer[2][1];






        }//createBitmaps()

        //Resource(Bitmap객체) 제거하기
        void removeResource(){

            if(sp!=null){
                sp.release();                   sp=null;
            }

            imgBomb.recycle();                  imgBomb=null;
            imgProtect.recycle();               imgProtect=null;
            imgStrong.recycle();                imgStrong=null;
            imgAttacked.recycle();              imgAttacked=null;

            for(int i=0;i<imgBombs.length;i++){
                imgBombs[i].recycle();          imgBombs[i]=null;
            }

            for(int i=0;i<imgItem.length;i++){
                imgItem[i].recycle();           imgItem[i]=null;
            }

            for(int i=0;i<imgDust.length;i++){
                imgDust[i].recycle();           imgDust[i]=null;
            }

            for(int i=0;i<imgGauge.length;i++){
                for(int k=0;k<imgGauge[1].length;k++){
                    imgGauge[i][k].recycle();   imgGauge[i][k]=null;
                }
            }

            for(int i=0;i<imgMissile.length;i++){
                imgMissile[i].recycle();        imgMissile[i]=null;
            }

            imgJoypad.recycle();                imgJoypad=null;
            imgBack.recycle();                  imgBack=null;

            for(int i=0;i<3;i++){
                for(int k=0;k<3;k++){//그림은 3개뿐이라서 3개만 없애면 됨.
                    imgPlayer[i][k].recycle();  imgPlayer[i][k]=null;
                }
                imgPlayer[i][3]=null;
            }

            for(int i=0;i<3;i++){
                for(int k=0;k<3;k++){//그림은 3개뿐이라서 3개만 없애면 됨.
                    imgEnemy[i][k].recycle();   imgEnemy[i][k]=null;
                }
                imgEnemy[i][3]=null;
            }

        }//removeResource()

        //레벨업이 가능한지 확인하기
        void checkLevelUp(){
            if(level>=levelScores.length)return;
            if(score>=levelScores[level]){
                level++;
                me.HP++;
                setLife();

                if(level==1){
                    sp.play(sdBegin,1,1,6,0,1);
                }else{
                    sp.play(sdLevelUp,1,1,6,0,1);
                }

            }

        }

        //레벨에 따라 차등화된 적군 생성
        void makeEnemies(){
            //적군 만들기..
            int p=rnd.nextInt(15-level);
            if(p==0){
                             

                enemies.add(new Enemy(width,height,imgEnemy,me.x,me.y,imgGauge));
            }
        }

        //2.1 화면에 보여질 객체들 만들기
        void makeAll(){

            checkLevelUp();

            makeEnemies();

            //미사일 만들기..
            if(fastItem>0){
                if(G.isSound){
                    sp.play(sdFire,0.1f,0.1f,0,0,1);
                }
                missiles.add(new Missile(width,height,imgMissile,me.x,me.y,me.angle,me.kind));
            }else{
                missileGap--;
                if(missileGap==0){
                    if(G.isSound){
                        sp.play(sdFire,0.1f,0.1f,0,0,1);
                    }
                    missiles.add(new Missile(width,height,imgMissile,me.x,me.y,me.angle,me.kind));
                    missileGap=3;
                }
            }


        }//makeAll()

        //2.2 움직이는 모든 작업
        void moveAll(){

            //맞은 효과 움직이기
            for(int i=attackeds.size()-1;i>=0;i--){
                Attacked t=attackeds.get(i);

                t.move(me.x,me.y);
                if(t.isDead) attackeds.remove(i);
            }

            //아이템들 움직이기
            for(int i=items.size()-1;i>=0;i--){
                Item t=items.get(i);

                if(magnetItem>0 && t.kind<2) t.move(me.x,me.y);
                else t.move();

                if(t.isDead)items.remove(i);
            }

            //Dust들 움직이기.
            for(int i=dusts.size()-1;i>=0;i--){
                Dust t=dusts.get(i);

                t.move();
                if(t.isDead)dusts.remove(i);
            }

            //Bombs들 움직이기
            for(int i=bombs.size()-1;i>=0;i--){
                Bomb t=bombs.get(i);

                t.move();
                if(t.isDead)bombs.remove(i);
            }

            //적군들 움직이기.
            for(int i=enemies.size()-1;i>=0;i--){
                Enemy t=enemies.get(i);

                t.move(me.x,me.y);
                if(t.isOut){
                    enemies.remove(i);
                }else if(t.isDead){
                    //점수 획득
                    score+=((t.kind+1)*10);
                    setTextView();

                    //폭발효과 생성
                    dusts.add(new Dust(imgDust,t.x,t.y));

                    //효과음
                    if(G.isSound){
                        sp.play(sdMonDie,0.5f,0.5f,1,0,1);
                    }

                    //아이템 생성
                    //if(new Random().nextInt(3)==0)
                        items.add(new Item(width, height, imgItem,t.x,t.y));

                    enemies.remove(i);
                }

            }

            //미사일들 움직이기.
            for(int i=missiles.size()-1;i>=0;i--){
                Missile t=missiles.get(i);

                t.move();
                if(t.isDead) missiles.remove(i);

            }

            //플레이어 움직이기
            me.move();

            //배경 움지깅기
            posBack-=width/600;//제일 작게 할수 있는게 한 600이라 600을 씀
            if(posBack<=-width){
                posBack+=width;
            }

            //아이템 지속시간 체크 메소드 호출
            checkItemTime();

        }//moveAll()

        //아이템 지속시간 체크 작업 메소드
        void checkItemTime(){
            if(fastItem>0){
                fastItem--;
                if(fastItem==0){
                    me.da=3;//회전 변화량 원상태로 복귀
                }
            }

            if(protectItem>0)protectItem--;
            if(magnetItem>0)magnetItem--;
            if(strongItem>0)strongItem--;
        }

        //아이템의 종류에 따른 작업메소드
        void actionItem(int kind){
            switch (kind){
                case 0://Coin       바로동작X
                    if(G.isSound){
                        sp.play(sdCoin,1,1,2,0,1);
                    }
                    coin++;
                    setTextView();
                    break;

                case 1://Gem        바로동작X
                    if(G.isSound){
                        sp.play(sdGem,1,1,3,0,1);
                    }
                    G.gem++;
                    setTextView();

                    break;

                case 2://Fast
                    if(G.isSound){
                        sp.play(sdItem,0.7f,0.7f,3,0,1);
                    }
                    me.da=9;//회전 변화량 증가
                    fastItem=200;//아이템시간 적용

                    break;

                case 3://Protect
                    if(G.isSound){
                        sp.play(sdProtect,0.5f,0.5f,4,0,1);
                    }
                    protectItem=200;
                    break;

                case 4://Magnet
                    if(G.isSound){
                        sp.play(sdItem,0.7f,0.7f,3,0,1);
                    }
                    magnetItem=200;
                    break;

                case 5://Bomb       바로동작X
                    if(G.isSound){
                        sp.play(sdItem,0.7f,0.7f,3,0,1);
                    }
                    bomb++;
                    setTextView();
                    break;

                case 6://Strong
                    if(G.isSound){
                        sp.play(sdItem,0.7f,0.7f,3,0,1);
                    }
                    strongItem=200;
                    break;
            }

        }

        void setLife(){
            post(new Runnable() {
                @Override
                public void run() {
                    if(me.HP>=5)return;
                    GameActivity ga=(GameActivity)context;
                    ga.lives[me.HP].setVisibility(GONE);
                }
            });
        }

        //2.3 모든 충돌체크 작업
        void checkCollision(){

            //플레이어와 적군의 충돌
            for(Enemy t:enemies){

                //보호막이 있는가?
                if(protectItem>0){                                        //딱 여기만 다름
                    if(Math.pow(me.x-t.x,2)+Math.pow(me.y-t.y,2)<=Math.pow(protectRad+t.w,2)){
                        t.isDead=true;
                    }
                }else{
                    if(Math.pow(me.x-t.x,2)+Math.pow(me.y-t.y,2)<=Math.pow(me.w+t.w,2)){
                        t.isDead=true;

                        me.HP--;

                        protectItem+=20;

                        setLife();

                        //진동효과
                        if(G.isVibrate){
                            vibrator.vibrate(500);
                        }


                        if(G.isSound){
                            sp.play(sdChDie,1,1,5,0,1);
                        }

                        attackeds.add(new Attacked(imgAttacked,me.x,me.y));



                        if(me.HP<=0){
                            //GAME OVER////////////////////
                            //효과음


                            //GameoverActivity실행/////////
                            //본사(GameActivity)에 요청
                            Message msg=new Message();
                            Bundle data=new Bundle();

                            //기본 자료형이면 뭐든지 다 넣을 수 있다.
                            data.putInt("Score",score);
                            data.putInt("Coin",coin);

                            msg.setData(data);//데이터가 하나만 넣을 수 있는데 이곳에 Bundle로 달라고 한다.
                                              //번들은 보따리로 수도없는 데이터를 넣는것이 가능하다.
                            ((GameActivity)context).handler.sendMessage(msg);
                        }

                    }
                }
            }

            //플레이어와 아이템의 충돌
            for(Item t:items){
                if(Math.pow(me.x-t.x,2)+Math.pow(me.y-t.y,2)<=Math.pow(me.w+t.w,2)){

                    //아이템의 종류에 따른 동작 수행
                    actionItem(t.kind);

                    //아이템 제거
                    t.isDead=true;
                    break;
                }
            }

            //미사일과 적군의 충돌
            for(Missile t: missiles){
                for(Enemy et:enemies){
                    if(Math.pow(t.x-et.x,2)+Math.pow(t.y-et.y,2)<=Math.pow(t.w+et.w,2)){
                        //적군의 HP를 줄이기..
                        et.damaged(t.kind+1);
                        if(strongItem==0) t.isDead=true;

                        score+=5;
                        setTextView();

                        break;
                    }
                }
            }


        }//checkCollision()

        //2.4 화면에 그려내는 모든 작업
        void drawAll(Canvas canvas){

            //배경그리기...
            canvas.drawBitmap(imgBack,posBack,0,null);
            canvas.drawBitmap(imgBack,posBack+width,0,null);

            //적군들 그리기
            for(Enemy t:enemies){
                canvas.save();
                canvas.rotate(t.angle,t.x,t.y);
                canvas.drawBitmap(t.img,t.x-t.w,t.y-t.h,null);
                canvas.restore();

                if(t.kind>0){
                    canvas.drawBitmap(t.imgG,t.x-t.w,t.y+t.h,null);

                }
            }

            //미사일들 그리기
            for(Missile t:missiles){
                canvas.save();
                canvas.rotate(t.angle,t.x,t.y);
                canvas.drawBitmap(strongItem>0?imgStrong:t.img,t.x-t.w,t.y+t.h,null);
                canvas.restore();
            }

            //공격당한 이미지 그리기
            for(Attacked t: attackeds){
                canvas.drawBitmap(t.img,t.x-t.w,t.y-t.h,null);
            }

            //아이템들 그리기
            for(Item t:items){
                canvas.drawBitmap(t.img,t.x-t.w,t.y-t.h,null);
            }



            //플레이어 그리기
            canvas.save();
            canvas.rotate(me.angle,me.x,me.y);
            canvas.drawBitmap(me.img,me.x-me.w,me.y-me.h,null);
            canvas.restore();

            //보호막 이미지 그리기
            if(protectItem>0){
                protectAng+=15;
                if(protectAng>=360) protectAng-=360;
                canvas.save();
                canvas.rotate(protectAng,me.x,me.y);
                canvas.drawBitmap(imgProtect,me.x-protectRad,me.y-protectRad,null);
                canvas.restore();
            }



            //Dust들 그리기
            for(Dust t:dusts){
                for(int i=0;i<t.img.length;i++){
                    canvas.drawBitmap(t.img[i],t.x[i]-t.rad[i],t.y[i]-t.rad[i],null);

                }
            }

            //공격당한 이미지 그리기
            for(Attacked t: attackeds){
                canvas.drawBitmap(t.img,t.x-t.w,t.y-t.h,null);
            }

            //폭탄터진것들 그리기
            for(Bomb t: bombs){
                bombPaint.setAlpha(t.alpha);
                for(int i=0;i<t.img.length;i++){
                    canvas.drawBitmap(t.img[i],t.x[i]-t.rad[i],t.y[i]-t.rad[i],bombPaint);
                }
            }

            //조이패드 그리기
            paint.setAlpha(isJoypad?240:120);
            canvas.drawBitmap(imgJoypad,jpx-jpr,jpy-jpr,paint);

            //폭탄버튼 그리기
            paint.setAlpha(isBomb?240:120);
            canvas.drawBitmap(imgBomb,recBomb.left,recBomb.top,paint);

        }//drawAll()

        //터치 작업 메소드/////////////
        void touchDown(int x, int y){
            //터치다운 한 x,y지점이 조이패드인가?
            if(Math.pow(x-jpx,2)+Math.pow(y-jpy,2)<=Math.pow(jpr,2)){
                //터치다운한 x,y와 조이패드의 중심좌표(jpx,jpy)사이의 각도 계산
                me.radian=Math.atan2(jpy-y,x-jpx);

                isJoypad=true;
                me.canMove=true;






            }

            //터치다운 한 x,y지점이 폭탄버튼인가?
            if(recBomb.contains(x,y)){
                isBomb=true;

                if(bomb>0){//폭탄개수가 1개 이상인가?
                    bombs.add(new Bomb(width,height,imgBombs));
                    sp.play(sdExplosion,1,1,4,0,1);
                    bomb--;
                    setTextView();
                    //폭발효과 적용..

                    for(Enemy t: enemies){
                        if(t.wasShown)t.isDead=true;
                    }

                }
            }

        }//touchDown()

        void touchUp(int x, int y){
            isJoypad=false;

            if(me!=null)
                me.canMove=false;

            isBomb=false;

        }//touchUP()

        void touchMove(int x, int y){
            //조이패드의 각도를 계산
            if(isJoypad){
                me.radian=Math.atan2(jpy-y,x-jpx);
            }


        }//touchMove()
        /////////////////////////////

        @Override
        public void run() {

            //초기값 설정!!
            init();

            Canvas canvas=null;

            while (isRun){
                //1. canvas 얻어오기
                canvas=holder.lockCanvas();

                //2. canvas에 원하는 작업 수행
                try{
                    synchronized (holder){

                        //2.1 화면에 보여질 객체들 만들기
                        makeAll();
                        //2.2 움직이기
                        moveAll();
                        //2.3 충돌체크
                        checkCollision();

                        //2.4 그려내는 작업
                        drawAll(canvas);

                    }//synchronized문
                }finally {
                    //3. holder에게 캔버스를 본사로 전송(post)
                    holder.unlockCanvasAndPost(canvas);
                }

                //일시정지(pause)에 대한 체크 (무조건 3번이 끝난 다음에 할 것)
                if(isWait){
                    try {
                        synchronized (this){
                            wait();//GameThread 정지!! 보통 if(isWait)부터 sync를 걸기 시작함.
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }//while문

            removeResource();

        }//run()

        void stopThread(){
            isRun=false;
            synchronized (this){//계속반복문을 돌아야 끝낫다는걸 확인가능하기 때문에 혹시 자고있을까봐
                this.notify();  //깨울려고 하는 것
            }
        }

        void pauseThread(){
            isWait=true;
        }

        void resumeThread(){
            isWait=false;

            synchronized (this){
                this.notify();
            }
        }

    }//GameThread CLASS
    ////////////////////////////////////////////////////////////////////////////////////////////////

}//GameView CLASS
