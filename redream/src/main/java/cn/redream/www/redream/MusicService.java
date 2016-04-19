package cn.redream.www.redream;

import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by acer on 2016/3/22.
 */
public class MusicService {
    public static final int PLAY_NEXT=0x150;
    public static final int IS_PLAY=1;
    public static final int IS_PAUSE=2;
    public static final int NOT=0;
    private static final File MUSIC_PATH = new File(Environment
            .getExternalStorageDirectory()+"/Redream/music");// 找到music存放的路径。
    public List<String> musicList;// 存放找到的所有mp3的绝对路径。
    public MediaPlayer player; // 定义多媒体对象
    public int songNum=-1; // 当前播放的歌曲在List中的下标
    public String songName; // 当前播放的歌曲名
    Handler handler;
    public int state=NOT;
    class MusicFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3")&&name.contains("___"));//返回当前目录所有以.mp3结尾的文件
        }
    }
    public MusicService() {
        musicList = new ArrayList<String>();
        player = new MediaPlayer();
        if (!MUSIC_PATH.exists())return;
        if (MUSIC_PATH.listFiles(new MusicFilter()).length > 0) {
            for (File file : MUSIC_PATH.listFiles(new MusicFilter())) {
                musicList.add(file.getAbsolutePath());
            }
        }

    }
    //必须要在new之后立即设置handler
    public void sethandler(Handler handler){
        this.handler=handler;
    }
    public void setPlayName(String dataSource) {
        File file = new File(dataSource);//假设为D:\\mm.mp3
        String name = file.getName();//name=mm.mp3
        int index = name.lastIndexOf(".");//找到最后一个.
        songName = name.substring(0, index);//截取为mm
    }
    public void setSongNum(int num){
        if (this.songNum==num){
            if (player.isPlaying()){
                player.pause();     //暂停当前歌曲
                state=IS_PAUSE;
            }else{
                player.start();
                state=IS_PLAY;
            }
        }else{
            //这里是在播放过程中切换到另一首歌
            this.songNum=num;
            state=IS_PLAY;
            start();
        }

    }
    public int getSongNum(){
        return this.songNum;
    }
    public void reduceSongNum(){
        songNum--;
    }
    public void addSongNum(){
        songNum++;
    }
    public void flashPlayList() {
        if (!MUSIC_PATH.exists())return;
        musicList.clear();
        if (MUSIC_PATH.listFiles(new MusicFilter()).length > 0) {
            for (File file : MUSIC_PATH.listFiles(new MusicFilter())) {
                musicList.add(file.getAbsolutePath());
            }
        }

    }
    public int getState(){
        return this.state;
    }
    public void start() {
        try {
            player.reset(); //重置多媒体
            String dataSource = musicList.get(songNum);//得到当前播放音乐的路径
            setPlayName(dataSource);//截取歌名
            player.setDataSource(dataSource);//为多媒体对象设置播放路径
            player.prepare();//准备播放
            player.start();//开始播放
            //setOnCompletionListener 当当前多媒体对象播放完成时发生的事件
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer arg0) {
                    next();//如果当前歌曲播放完毕,自动播放下一首.
                    //先把状态置为暂停，下面发消息模拟点击下一首就播放了
                    player.pause();
                    state=IS_PAUSE;
                    handler.sendEmptyMessage(PLAY_NEXT);

                }
            });
        } catch (Exception e) {
            Log.v("MusicService", e.getMessage());
        }
    }

    public void next() {
        songNum = songNum == musicList.size() - 1 ? 0 : songNum + 1;
        start();
        Message msg=new Message();
        //next songNum
        msg.arg1=songNum;
    }

    public void last() {
        songNum = songNum == 0 ? musicList.size() - 1 : songNum - 1;
        start();
    }

    public void pause() {
        if (player.isPlaying())
            player.pause();
        else
            player.start();
    }

    public void stop() {
        if (player.isPlaying()) {
            player.stop();
        }
    }
}
