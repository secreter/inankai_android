package cn.redream.www.redream;

import android.app.Application;
import android.os.Handler;

/**
 * Created by acer on 2016/3/23.
 */
public class RedreamApp extends Application {
    //当Activity返回上一个Activity时，会调用onDestroy（）函数，再次进入时会onStrat()函数重新创建MusicTabActivity，如果用MusicTabActivityde
    //curPlayViewPos,就会是-1，被重置
    public int lastPlayViewPos=-1;
    public int curPlayViewPos=-1;
    public int nextPlayViewPos=-1;
    public int musicItemCount=-1;

    //全局保存播放状态，因为musictabActivity常常切到其他Actyvity
    public int musicPlayState=-1;
//    public int musicPlayPosition=-1;
    private MusicService player=new MusicService();
    private Handler musicHandler = null;    //在其他页面接受下一首的消息


    public void setMusicPlayState(int musicPlayState){
        this.musicPlayState=musicPlayState;
    }
    public int getMusicPlayState(){
        return musicPlayState;
    }
//    public void setMusicPlayPosition(int musicPlayPosition){
//        this.musicPlayPosition=musicPlayPosition;
//    }
//    public int getMusicPlayPosition(){
//        return musicPlayPosition;
//    }
    public MusicService getPlayer(){
        return player;
    }
    public  void setMusicHandler(Handler handler){
        musicHandler=handler;
    }
    public Handler getMusicHandler(){
        return musicHandler;
    }
    public void setLastPlayViewPos(int lastPlayViewPos){
        this.lastPlayViewPos=lastPlayViewPos;
    }
    public int getLastPlayViewPos(){
        return lastPlayViewPos;
    }
    public void setCurPlayViewPos(int curPlayViewPos){
        this.curPlayViewPos=curPlayViewPos;
    }
    public int getCurPlayViewPos(){
        return curPlayViewPos;
    }
    public void setNextPlayViewPos(int nextPlayViewPos){
        this.nextPlayViewPos=nextPlayViewPos;
    }
    public int getNextPlayViewPos(){
        return nextPlayViewPos;
    }
    //当下载歌曲后，musicItemCount改变了，nextPlayViewPos可能也会变
    public void flashNextPos(){
        nextPlayViewPos = (curPlayViewPos == musicItemCount - 1) ? 0 : curPlayViewPos + 1;
    }
}
