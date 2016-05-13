package cn.redream.www.redream;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicTabActivity extends AppCompatActivity implements TabHost.OnTabChangeListener,NavigationView.OnNavigationItemSelectedListener {
    TabHost tabHost;
    String response;
    Context context;
    WXShareUtil share;
    final String LOCAL_DIR="inankai/music";
    private static final int MSG_NET_ERROR =0x140 ;

    private SearchView searchArtist;
    private SearchView searchTitle;
    private ListView localList;
    private ListView titleList;
    private ListView artistList;
    private String[] artistNames=new String[]{"陈奕迅","刘德华","周杰伦","王菲","张信哲","邓紫棋","汪峰","那英","张学友","薛之谦","林俊杰","Bigbang","张杰","TFBOYS "};
    private String[] artistDesc=new String[]{"香港实力派歌手、演员","四大天王之一","台湾R&B创作型歌手","华语歌坛天后","一代情歌王子","香港新生代唱做歌手","中国新摇滚乐的代表人物之一",
            "大陆歌坛天后","香港实力派歌手演员","内地流行男歌手","新加坡唱作俱佳音乐人","韩国人气团体，KPOP界领军人物","内地男歌手","95后组合"};
    private int[] artistImgsId=new int[]{R.mipmap.chenyixun,R.mipmap.liudehua,R.mipmap.zhoujielun,R.mipmap.wangfei,R.mipmap.zhangxinzhe,R.mipmap.dengziqi,
    R.mipmap.wangfeng,R.mipmap.naying,R.mipmap.zhangxueyou,R.mipmap.xuezhiqian,R.mipmap.linjunjie,R.mipmap.bigbang,R.mipmap.zhangjie,R.mipmap.tfboys};

    private String[] singNames=new String[]{"孤独患者","同桌的你","默","我怀念的","红豆","那时候的我","十年","不要说话","爱情转移","匆匆那年"};
    private String[] singDesc=new String[]{"陈奕迅","老狼","那英","孙燕姿","王菲","刘惜君","陈奕迅","陈奕迅","陈奕迅","王菲"};
    private String[] singId=new String[]{"95979","16470","98226","78471","11952","95971","37427","78459","82310","30322"};
    private int[] singImgsId=new int[]{R.mipmap.music_1,R.mipmap.music_2,R.mipmap.music_3,R.mipmap.music_4,R.mipmap.music_5,R.mipmap.music_6,
            R.mipmap.music_7,R.mipmap.music_8};

    //mp3播放器
    private MusicService player;
    int curSongNum=-1;
    View lastPlayView=null;
    View curPlayView=null;
    View nextPlayView=null;
    //这里必须用全局变量来初始化，从redreamApp里获取
//    int lastPlayViewPos;
//    int curPlayViewPos;
//    int nextPlayViewPos;
    MusicPlayAdapter musicPlayAdapter;
    RedreamApp redreamApp;
    private Spinner searchType;
    private android.support.v7.widget.SearchView movieSearch;
    private String searchTypeStr="name";  //默认搜索片名
    private ArrayAdapter adapterType;
    private MenuItem thisActMenuItem;//
    private NavigationView navigationView;

    final String dirName="inankai/music";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_tab);

        //设置状态栏


        init();

        //设置放大镜的图标为黑色
        int search_mag_icon_id1 = searchArtist.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView  search_mag_icon1 = (ImageView)searchArtist.findViewById(search_mag_icon_id1);//获取搜索图标
//        ImageView search_button = (ImageView) searchCartoon.findViewById(android.R.id.search_button);
        search_mag_icon1.setImageResource(R.mipmap.search3);//图标都是用src的
        //将其展开后改变图标才有用
        searchArtist.setIconifiedByDefault(false);
        //初始时失去焦点
        searchArtist.setFocusable(false);

        //设置字体为黑色
        int id1 = searchArtist.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView1 = (TextView) searchArtist.findViewById(id1);
        textView1.setTextColor(Color.BLACK);
        textView1.setTextSize(14);
        textView1.setHintTextColor(getResources().getColor(R.color.grey));

        int search_mag_icon_id2 = searchTitle.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView  search_mag_icon2 = (ImageView)searchTitle.findViewById(search_mag_icon_id2);//获取搜索图标
//        ImageView search_button = (ImageView) searchCartoon.findViewById(android.R.id.search_button);
        search_mag_icon2.setImageResource(R.mipmap.search3);//图标都是用src的

        //将其展开后改变图标才有用
        searchTitle.setIconifiedByDefault(false);
        //初始时失去焦点
        searchTitle.setFocusable(false);

        //设置字体为黑色
        int id2 = searchTitle.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView2 = (TextView) searchTitle.findViewById(id2);
        textView2.setTextColor(Color.BLACK);
        textView2.setTextSize(14);
        textView2.setHintTextColor(getResources().getColor(R.color.grey));

        searchArtist.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                new Thread() {
                    @Override
                    public void run() {
                        String query_gb = strToGb2312(query);
                        try {
                            response = GetPostUtil.sendPostGbk("http://music.nankai.edu.cn/artistlist.php?iffit=yes", "searchtype=artist&searchstring=" + query_gb);
                            //发送消息通知ui线程更新UI组件
                            handler.sendEmptyMessage(0x123);
                        } catch (IOException e) {
                            //网络错误
                            handler.sendEmptyMessage(MSG_NET_ERROR);
                            e.printStackTrace();
                        }

                    }
                }.start();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchTitle.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                new Thread() {
                    @Override
                    public void run() {
                        String query_gb = strToGb2312(query);
                        try {
                            response = GetPostUtil.sendPostGbk("http://music.nankai.edu.cn/main.php?iframeID=layer_d_3_I", "searchtype=title&searchstring=" + query_gb);
                            //发送消息通知ui线程更新UI组件
                            handler.sendEmptyMessage(0x124);
                        } catch (IOException e) {
                            //网络错误
                            handler.sendEmptyMessage(MSG_NET_ERROR);
                            e.printStackTrace();
                        }

                    }
                }.start();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        artistList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //传递参数
                ListView listView = (ListView) parent;
                final HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
                //打开下一个activity
//                Intent intent = new Intent();
//                //CurActivity 是当前activity, NextActivity 是要跳转至的activity
//                intent.setClass(context, MusicResultActivity.class);
//                intent.putExtra("artist", map.get("personName").toString());
//                intent.putExtra("header", map.get("header").toString());
//                startActivity(intent);

                //选中第一个tab
                tabHost.setCurrentTab(1);
                LinearLayout loading= (LinearLayout) findViewById(R.id.loadingAnim);
                loading.setVisibility(View.VISIBLE);
                new Thread() {
                    @Override
                    public void run() {
                        String query_gb = strToGb2312(map.get("personName").toString());
                        try {
                            response = GetPostUtil.sendPostGbk("http://music.nankai.edu.cn/main.php?iframeID=layer_d_3_I", "searchtype=artist&searchstring=" + query_gb);
                            //发送消息通知ui线程更新UI组件
                            handler.sendEmptyMessage(0x126);
                        } catch (IOException e) {
                            //网络错误
                            handler.sendEmptyMessage(MSG_NET_ERROR);
                            e.printStackTrace();
                        }

                    }
                }.start();
            }
        });

        titleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //传递参数
                ListView listView= (ListView) parent;
                MusicDownloadAdapter adapter = (MusicDownloadAdapter) ((ListView) parent).getAdapter();
                ImageView imageView = (ImageView) view.findViewById(R.id.downloadIcon);
                imageView.setImageResource(R.mipmap.icon_download_active);

                HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
                if (redreamApp.musicUrlList.contains(map.get("id"))){
                    Toast.makeText(MusicTabActivity.this, "已经下载", Toast.LENGTH_SHORT).show();
                    return;
                }
                adapter.addDownloadId((String) map.get("id"));
                redreamApp.musicUrlList.add((String) map.get("id"));
                Toast.makeText(getApplicationContext(), map.get("singName")+"正在后台下载，保存在inankai/music", Toast.LENGTH_LONG).show();
                final String url="http://music.nankai.edu.cn/download.php?id="+map.get("id");
                final String name=map.get("artist")+"___"+map.get("singName")+"___"+map.get("id")+".mp3";

                new Thread() {
                    @Override
                    public void run() {

                        downloadMp3(url, name, dirName);
                        //貌似线程里不可以用更新界面的东西，老出错
//                        Toast.makeText(getApplicationContext(), map.get("singName")+"正在下载", Toast.LENGTH_SHORT).show();
                        Message msg=new Message();
                        msg.obj=name;
                        msg.what=0x125;
                        handler.sendMessage(msg);
                    }

                }.start();


//                Intent intent = new Intent();
//                Uri uri = Uri.parse("http://music.nankai.edu.cn/download.php?id="+map.get("id"));
//                intent.setDataAndType(uri, "audio/x-mpeg");
//                intent.setAction(Intent.ACTION_VIEW);
//                startActivity(intent);

//                MediaPlayer mediaPlayer = new MediaPlayer();
//                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置媒体流类型
//                mediaPlayer.reset();
//
//                try {
//                    mediaPlayer.setDataSource("http://music.nankai.edu.cn/download.php?id="+map.get("id")); // 设置数据源
//                    mediaPlayer.prepare(); // prepare自动播放
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                mediaPlayer.start();//开始播放

            }
        });
        localList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //传递参数
                ListView listView = (ListView) parent;
//                SimpleAdapter adapter = (SimpleAdapter) ((ListView) parent).getAdapter();
                MusicPlayAdapter adapter = (MusicPlayAdapter) ((ListView) parent).getAdapter();
                int itemCount = adapter.getCount();    //常常在变，可能下载或删除了歌曲
                redreamApp.lastPlayViewPos = (position == 0) ? itemCount - 1 : position - 1;
                redreamApp.curPlayViewPos = position;
                redreamApp.nextPlayViewPos = (position == itemCount - 1) ? 0 : position + 1;
//                Log.v("pcy", "lastPlayViewPos:" + redreamApp.lastPlayViewPos);
//                Log.v("pcy", "curPlayViewPos:" + redreamApp.curPlayViewPos);
//                Log.v("pcy", "nextPlayViewPos:" + redreamApp.nextPlayViewPos);
                lastPlayView = curPlayView;
                curPlayView = view;

                player.setSongNum(position);
                musicPlayAdapter.changeSelected(position, player.getState());

                //redreamApp.setMusicPlayPosition(position);
                redreamApp.setMusicPlayState(player.getState());

//                nextPlayView=parent.getChildAt((position+1)%parent.getCount() - parent.getFirstVisiblePosition());
//                if (player.getSongNum() == position) {
//
//                    //do nothing
//                } else {
//                    view.setBackgroundColor(getResources().getColor(R.color.eggplant));
//                    //获取上次播放的view
////                    View lastView = parent.getChildAt(player.getSongNum() - parent.getFirstVisiblePosition());
//                    //第一次播放不用换颜色
//                    if (player.getSongNum() != -1) {
//                        lastPlayView.setBackgroundColor(getResources().getColor(R.color.huibai));
//                        ImageView buttonView = (ImageView) (lastPlayView.findViewById(R.id.playOrPause));
//                        buttonView.setVisibility(View.GONE);
//                    }
//                }

//                player.setSongNum(position);
//                curSongNum = position;
//                ImageView playOrpause = (ImageView) view.findViewById(R.id.playOrPause);
//                if (player.getState() == MusicService.IS_PLAY) {
//                    playOrpause.setImageResource(R.mipmap.button_play);
//                } else {
//                    playOrpause.setImageResource(R.mipmap.button_pause);
//                }
//                playOrpause.setVisibility(View.VISIBLE);


            }
        });
//        localList.setOnFocusChangeListener(new MyOnFocusChangeListener());
    }

      Handler handler=new Handler(){
         //注意，桃源网站使用的是gb2312编码，所以提交、得到的文字都要转编码
        @Override
        public void handleMessage(Message msg) {
            List<Map<String,Object>> listItems=new ArrayList<Map<String,Object>>();
            if (msg.what==0x123){
                Pattern p= Pattern.compile("<a id='artist.+>(.+)</a>");
                Matcher m=p.matcher(response);
                int i=0;
                while(m.find()){
                    Map<String,Object> listItem=new HashMap<String,Object>();
                    listItem.put("header",R.mipmap.chenyixun);
                    listItem.put("personName",m.group(1));
                    listItem.put("desc","ink media");
                    listItems.add(listItem);
                }
                if (i==0){
                    Toast.makeText(context,"暂时没有搜到结果~换歌手个试试吧~",Toast.LENGTH_LONG).show();
                }
                SimpleAdapter simpleAdapter=new SimpleAdapter(context,listItems,R.layout.listview,new String[]{"header","personName","desc"},new int[]{R.id.header,R.id.name,R.id.desc});
                artistList.setAdapter(simpleAdapter);
            }
            if (msg.what==0x124){
                Pattern p= Pattern.compile("<font color=#666666>(.+)</font>[\\s\\S]+?<a href=\"waitorder.+id=(\\d+).+><font color=green>(.+)</font></a>");
                Matcher m=p.matcher(response);
                int i=0;
                while(m.find()){
                    i++;
                    System.out.println(m.group(1));
                    System.out.println(m.group(2));
                    System.out.println(m.group(3));
                    Map<String,Object> listItem=new HashMap<String,Object>();
                    listItem.put("singImg",R.mipmap.songs);
                    listItem.put("singName",m.group(3));
                    listItem.put("artist",m.group(1));
                    listItem.put("id",m.group(2));
                    listItems.add(listItem);
                }
                MusicDownloadAdapter simpleAdapter=new MusicDownloadAdapter(context,listItems);
                titleList.setAdapter(simpleAdapter);
                if (i==0){
                    Toast.makeText(context,"暂时没有搜到结果~换个试试吧~",Toast.LENGTH_LONG).show();
                }
            }
            if (msg.what==0x125){
                Toast.makeText(context,"下载成功！",Toast.LENGTH_SHORT).show();

                player.addSongNum();   //现在正在播放的songNum已经是这首歌的上一个的了，所以加加
                player.flashPlayList();
                int newSongPosition;
                GetFileList getFileListObj=new GetFileList(Environment.getExternalStorageDirectory()+"/"+LOCAL_DIR,false);
                ArrayList<String> fileList=getFileListObj.getFileArrayList();
                initLocal();
                newSongPosition=fileList.indexOf(msg.obj);
                if (newSongPosition<=redreamApp.curPlayViewPos){
                    //模拟点击,模拟点击了下一首歌两次,之前用上面的方法还是有问题，要维护的东西太多
                    localList.performItemClick(localList.getChildAt(redreamApp.nextPlayViewPos), redreamApp.nextPlayViewPos, localList.getItemIdAtPosition(redreamApp.nextPlayViewPos));
                    localList.performItemClick(localList.getChildAt(redreamApp.curPlayViewPos), redreamApp.curPlayViewPos, localList.getItemIdAtPosition(redreamApp.curPlayViewPos));

                }
            }
            if (msg.what==0x126){

                Pattern p= Pattern.compile("<font color=#666666>(.+)</font>[\\s\\S]+?<a href=\"waitorder.+id=(\\d+).+><font color=green>(.+)</font></a>");
                Matcher m=p.matcher(response);
                int i=0;
                while(m.find()){
                    i++;
                    System.out.println(m.group(1));
                    System.out.println(m.group(2));
                    System.out.println(m.group(3));
                    Map<String,Object> listItem=new HashMap<String,Object>();
                    listItem.put("singImg",R.mipmap.songs);
                    listItem.put("singName",m.group(3));
                    listItem.put("artist",m.group(1));
                    listItem.put("id",m.group(2));
                    listItems.add(listItem);
                }
                MusicDownloadAdapter simpleAdapter=new MusicDownloadAdapter(context,listItems);
                titleList.setAdapter(simpleAdapter);
                if (i==0){
                    Toast.makeText(context,"暂时没有搜到结果~换个试试吧~",Toast.LENGTH_LONG).show();
                }
                LinearLayout loading= (LinearLayout) findViewById(R.id.loadingAnim);
                loading.setVisibility(View.GONE);
            }
            if (msg.what==MusicService.PLAY_NEXT){
                Toast.makeText(context,"播放下一首！",Toast.LENGTH_SHORT).show();
                //下载成功本地就多一首歌，更新本地列表
                flashPlayList(nextPlayView);
            }
            if(msg.what==MSG_NET_ERROR){
                TextView text= (TextView) findViewById(R.id.text);
                text.setText("网络错误，请确保连接南开大学wifi！");
                Toast.makeText(context,"网络错误，请确保连接南开大学wifi！",Toast.LENGTH_LONG).show();
            }


        }
    };
    private void flashPlayList(View view){
        //模拟点击,模拟点击了下一首歌
        localList.performItemClick(localList.getChildAt(redreamApp.nextPlayViewPos), redreamApp.nextPlayViewPos, localList.getItemIdAtPosition(redreamApp.nextPlayViewPos));
    }
    /**

     * listview获得焦点和失去焦点时背景颜色的变化

     * @author long

     *
     */
//    private class MyOnFocusChangeListener implements View.OnFocusChangeListener {
//        @Override
//        public void onFocusChange(View view, boolean hasFocus) {
//
//            //判断是否有焦点，，如果有焦点则设置背景色为想要的颜色或者背景图片，当失去焦点的时候再设置为原来的颜色
//
//            if(hasFocus == true){
//                Log.v("pcy","MyOnFocusChangeListener");
//                //获得焦点
////                videoView.setSelector(android.R.color.white) ;
//                view.setBackgroundColor(getResources().getColor(R.color.eggplant));
//            }   else{
//                //失去焦点
////                videoView.setSelector(R.color.unselected) ;
//                view.setBackgroundColor(getResources().getColor(R.color.huibai));
//            }
//        }
//    }



    private void updateTab(final TabHost tabHost) {
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            View view = tabHost.getTabWidget().getChildAt(i);
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextSize(15);
            tv.setTextColor(getResources().getColor(R.color.colorBlank));
//            tv.setTypeface(Typeface.SERIF, 2); // 设置字体和风格
            if (tabHost.getCurrentTab() == i) {//选中
                view.setBackgroundResource(R.drawable.tab_focused);

                tv.setTextColor(getResources().getColor(R.color.colorBlank));
            } else {//不选中
                view.setBackgroundResource(R.drawable.tab_normal);

                tv.setTextColor(getResources().getColor(R.color.colorGray));
            }

        }
    }


    //重写tabhost
    @Override
    public void onTabChanged(String tabId) {
        if("tab1".equals(tabId)) {

        }
        if("tab2".equals(tabId)) {
            //destroy mars
        }
        tabHost.setCurrentTabByTag(tabId);
        updateTab(tabHost);
    }
    private void initTabHost(){
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        //不能少setup()哟~~~
        tabHost.setup();
        TabHost.TabSpec tab1=tabHost.newTabSpec("tab1")
                .setIndicator("歌手") //创建标题
                .setContent(R.id.tab1);//创建内容
        //添加第一个标签页
        tabHost.addTab(tab1);
        TabHost.TabSpec tab2=tabHost.newTabSpec("tab2")
                .setIndicator("歌曲") //创建标题
                .setContent(R.id.tab2);//创建内容
        //添加第一个标签页
        tabHost.addTab(tab2);
        TabHost.TabSpec tab3=tabHost.newTabSpec("tab3")
                .setIndicator("本地",getResources() //放置图标
                        .getDrawable(R.mipmap.chenyixun))
                .setContent(R.id.tab3);//创建内容
        // 添加第一个标签页
        tabHost.addTab(tab3);
        updateTab(tabHost);//初始化Tab的颜色，和字体的颜色
        tabHost.setOnTabChangedListener(this);// 选择监听器

        final TabWidget tabWidget = tabHost.getTabWidget();

//注销系统自带下划线！！！
        tabWidget.setStripEnabled(false);
    }
    private void initArtist(){
        //创建一个List集合，list集合的元素是Map
        List<Map<String,Object>> listItems=new ArrayList<Map<String,Object>>();
        for (int i=0;i<artistNames.length;i++){
            Map<String,Object> listItem=new HashMap<String,Object>();
            listItem.put("header",artistImgsId[i]);
            listItem.put("personName",artistNames[i]);
            listItem.put("desc",artistDesc[i]);
            listItems.add(listItem);
        }
        //创建一个simpleAdapter
        SimpleAdapter simpleAdapter=new SimpleAdapter(this,listItems,R.layout.listview,new String[]{"header","personName","desc"},new int[]{R.id.header,R.id.name,R.id.desc});
        artistList.setAdapter(simpleAdapter);
    }
    //初始化歌名
    private void initTitle(){
        List<Map<String,Object>> listItems=new ArrayList<Map<String,Object>>();
        for (int i=0;i<singNames.length;i++){
            Map<String,Object> listItem=new HashMap<String,Object>();
//            listItem.put("singImg",singImgsId[i%8]);
            listItem.put("singImg",R.mipmap.songs);
            listItem.put("singName",singNames[i]);
            listItem.put("artist",singDesc[i]);
            listItem.put("id",singId[i]);
            listItems.add(listItem);
        }
//        SimpleAdapter simpleAdapter=new SimpleAdapter(this,listItems,R.layout.listview_song,new String[]{"singImg","singName","artist"},new int[]{R.id.header,R.id.name,R.id.desc});
//        titleList.setAdapter(simpleAdapter);
        MusicDownloadAdapter simpleAdapter=new MusicDownloadAdapter(this,listItems);
        titleList.setAdapter(simpleAdapter);
    }
    private void initLocal(){
        List<Map<String,Object>> listItems=new ArrayList<Map<String,Object>>();
        GetFileList getFileListObj=new GetFileList(Environment.getExternalStorageDirectory()+"/"+LOCAL_DIR,false);
        ArrayList<String> fileList=getFileListObj.getFileArrayList();
        int len=fileList.size();
        for (int i=0;i<len;i++){
            //singer___singName.mp3
            Pattern p= Pattern.compile("(.*)___(.*)___(.*)\\.mp3");
            Matcher m=p.matcher(fileList.get(i));
            if (m.find()){
                Map<String,Object> listItem=new HashMap<String,Object>();
                listItem.put("singImg",R.mipmap.defaultimg);
                listItem.put("singName",m.group(2));
                listItem.put("artist",m.group(1));
                listItem.put("id",m.group(3));
                listItems.add(listItem);
            }

        }

        musicPlayAdapter=new MusicPlayAdapter(this,listItems);
        redreamApp.musicItemCount=musicPlayAdapter.getCount();
        redreamApp.flashNextPos();  //刷新下一首歌的位置
        localList.setAdapter(musicPlayAdapter);


    }
    private void init(){
        context=this;
        artistList= (ListView) findViewById(R.id.artistList);
        titleList= (ListView) findViewById(R.id.titleList);
        localList= (ListView) findViewById(R.id.albumList);
        searchArtist= (SearchView) findViewById(R.id.searchArtist);
        searchTitle= (SearchView) findViewById(R.id.searchTitle);
        share=new WXShareUtil(this);
        redreamApp = ((RedreamApp)getApplicationContext());   //要在initLocal();后

        this.navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //注册导航栏
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        initTabHost();
        initArtist();
        initTitle();
        initLocal();


        player=redreamApp.getPlayer();//player要全局，退出一个页面到其它还能播放
        player.sethandler(handler);

        this.registerForContextMenu(titleList);
        this.registerForContextMenu(localList);
    }

    private String strToGb2312(String str){
        try {
            str = URLEncoder.encode(str.trim(), "gb2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v==titleList){
            menu.add(0, 10, 1, "下载");
            menu.add(0,11,2,"分享到朋友圈");
            menu.add(0,12,3,"分享给微信好友");
        }

        if (v==localList){
            menu.add(0, 20, 1, "删除");
            menu.add(0,11,2,"分享到朋友圈");
            menu.add(0,12,3,"分享给微信好友");
        }


    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo;
        menuInfo= (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index=menuInfo.position;
        //注意，这里targetView是linearlayout，要getParent()才是ListView
        ListView listView= (ListView) menuInfo.targetView.getParent();
        final HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(index);
        int imgId;
        final String url="http://music.nankai.edu.cn/download.php?id="+map.get("id");
        final String name=map.get("artist")+"___"+map.get("singName")+"___"+map.get("id")+".mp3";

        String title=map.get("singName").toString()+" | inankai media";
        String description="我正在听南开内网音乐哦~";

        switch (item.getItemId()){
            case 10:

                Toast.makeText(getApplicationContext(), map.get("singName")+"正在后台下载，保存在inankai/music", Toast.LENGTH_LONG).show();
                new Thread() {
                    @Override
                    public void run() {

                        downloadMp3(url, name, dirName);
                        //貌似线程里不可以用更新界面的东西，老出错
//                        Toast.makeText(getApplicationContext(), map.get("singName")+"正在下载", Toast.LENGTH_SHORT).show();
                        Message msg=new Message();
                        msg.obj=name;
                        msg.what=0x125;
                        handler.sendMessage(msg);
                    }

                }.start();

                break;
            case 11:
//                imgId=Integer.parseInt(map.get("singImg").toString());
                share.sendUrlAudio(url,true,title,description,R.mipmap.ink);
                break;
            case 12:
//                imgId=Integer.parseInt(map.get("singImg").toString());
                share.sendUrlAudio(url,false,title,description,R.mipmap.ink);
                break;
            case 20:
                String filePath=Environment.getExternalStorageDirectory()+"/"+LOCAL_DIR+"/"+name;
                int return_code=deleteFile(filePath,player.getSongNum());
                switch (return_code){
                    case -2:
                        Toast.makeText(this,title+"删除失败，请查看文件是否存在",Toast.LENGTH_SHORT).show();
                        break;
                    case -1:
                        Toast.makeText(this,title+"删除成功",Toast.LENGTH_SHORT).show();
                        player.flashPlayList();
                        //为什么我不在redreamApp一起更新这3个呢，因为item总数会变，在临界值处mod总数时会出错
                        //不可能删的是第0首，所以不用考虑减出负数
//                        redreamApp.setLastPlayViewPos(redreamApp.getLastPlayViewPos()-1);
//                        redreamApp.setCurPlayViewPos(redreamApp.getLastPlayViewPos());
//                        redreamApp.setNextPlayViewPos(redreamApp.getCurPlayViewPos());
                        player.reduceSongNum();   //现在正在播放的songNum已经是这首歌的下一个的了，所以减减
                        initLocal();
                        //模拟点击,模拟点击了上一首歌两次,之前用上面的方法还是有问题，要维护的东西太多
                        localList.performItemClick(localList.getChildAt(redreamApp.lastPlayViewPos), redreamApp.lastPlayViewPos, localList.getItemIdAtPosition(redreamApp.lastPlayViewPos));
                        localList.performItemClick(localList.getChildAt(redreamApp.curPlayViewPos), redreamApp.curPlayViewPos, localList.getItemIdAtPosition(redreamApp.curPlayViewPos));
                        break;
                    case 0:
                        Toast.makeText(this,map.get("singName").toString()+"删除失败，不能删除正在播放的歌曲",Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(this,map.get("singName").toString()+"删除成功",Toast.LENGTH_SHORT).show();
                        initLocal();
                        player.flashPlayList();  //播放列表总数变了
                        player.reduceSongNum();
                        break;

                }

                break;


        }
        return true;
    }
    private void downloadMp3(String urlStr,String fileName,String path){
        /*
        @Project: Android_MyDownload
                * @Desciption: 利用Http协议下载文件并存储到SDCard
        1.创建一个URL对象
        2.通过URL对象,创建一个HttpURLConnection对象
        3.得到InputStream
        4.从InputStream当中读取数据
                存到SDCard
        1.取得SDCard路径
        2.利用读取大文件的IO读法，读取文件
         */

        //String fileName="2.mp3";
        OutputStream output=null;
        try {
                /*
                 * 通过URL取得HttpURLConnection
                 * 要网络连接成功，需在AndroidMainfest.xml中进行权限配置
                 * <uses-permission android:name="android.permission.INTERNET" />
                 */
            URL url=new URL(urlStr);
            HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            conn.connect();
            //取得inputStream，并将流中的信息写入SDCard

                /*
                 * 写前准备
                 * 1.在AndroidMainfest.xml中进行权限配置
                 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
                 * 取得写入SDCard的权限
                 * 2.取得SDCard的路径： Environment.getExternalStorageDirectory()
                 * 3.检查要保存的文件上是否已经存在
                 * 4.不存在，新建文件夹，新建文件
                 * 5.将input流中的信息写入SDCard
                 * 6.关闭流
                 */
            String SDCard= Environment.getExternalStorageDirectory()+"";
            String pathName=SDCard+"/"+path+"/"+fileName;//文件存储路径
            String dir=SDCard+"/"+path;

            File file=new File(pathName);
            InputStream input=conn.getInputStream();
            if(file.exists()){
                System.out.println("exits");
                return;
            }else{
                if (!new File(dir).exists()){
                    new File(dir).mkdirs();//新建文件夹
                }


                file.createNewFile();//新建文件
                output=new FileOutputStream(file);
                //读取大文件
                byte[] buffer=new byte[4*1024];
//                int i=0;
//                while(input.read(buffer)!=-1){
//                    output.write(buffer);
//                    System.out.println(i++);
//                }
                //上面的方法下载的音乐不全，还卡
                int length = 0;
                //将输入流中的内容先输入到buffer中缓存，然后用输出流写到文件中
                while((length = input.read(buffer)) != -1)
                {
                    output.write(buffer,0,length);
                }
                output.flush();
                output.close();
                //Toast.makeText(context,fileName+"下载成功！",Toast.LENGTH_SHORT).show();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{

        }
    }
    /**
     * 删除单个文件
     * @param   sPath    被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public int deleteFile(String sPath,int playingSongNum) {
        int ERROR=-2;
        int PLAYING_POS=0;
        int LESS_THEN_PLAYING_POS=-1;
        int MORE_THEN_PLAYING_POS=1;
        boolean flag = false;
        File file = new File(sPath);
        int deletePosition;
        GetFileList getFileListObj=new GetFileList(Environment.getExternalStorageDirectory()+"/"+LOCAL_DIR,false);
        ArrayList<String> fileList=getFileListObj.getFileArrayList();
        deletePosition=fileList.indexOf(file.getName());
        //正在播放不允许删
        if(deletePosition==playingSongNum)
            return PLAYING_POS;
        if(deletePosition<playingSongNum&&file.isFile() && file.exists()) {
            file.delete();
            return LESS_THEN_PLAYING_POS;
        }
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            return MORE_THEN_PLAYING_POS;
        }
        return ERROR;

    }



    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //测试生命周期，因为music会跳到其他页面，也要正常播放
    //Activity创建或者从后台重新回到前台时被调用
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart called.");
    }

    //Activity从后台重新回到前台时被调用
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart called.");
    }

    //Activity创建或者从被覆盖、后台重新回到前台时被调用
    @Override
    protected void onResume() {
        super.onResume();

        //用于菜单的同步
        //起始化进入该页面时设置其本身被选中
        thisActMenuItem= navigationView.getMenu().getItem(4);
        thisActMenuItem.setChecked(true); // 改变item选中状态

        Log.i(TAG, "onResume called.");
    }

    //Activity窗口获得或失去焦点时被调用,在onResume之后或onPause之后
    /*@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.i(TAG, "onWindowFocusChanged called.");
    }*/

    //Activity被覆盖到下面或者锁屏时被调用
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause called.");
        //有可能在执行完onPause或onStop后,系统资源紧张将Activity杀死,所以有必要在此保存持久数据
    }

    //退出当前Activity或者跳转到新Activity时被调用
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop called.");
    }

    //退出当前Activity时被调用,调用之后Activity就结束了
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestory called.");
    }

    /**
     * Activity被系统杀死时被调用.
     * 例如:屏幕方向改变时,Activity被销毁再重建;当前Activity处于后台,系统资源紧张将其杀死.
     * 另外,当跳转到其他Activity或者按Home键回到主屏时该方法也会被调用,系统是为了保存当前View组件的状态.
     * 在onPause之前被调用.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("param", param);
        Log.i(TAG, "onSaveInstanceState called. put param: " + param);
        super.onSaveInstanceState(outState);
    }

    /**
     * Activity被系统杀死后再重建时被调用.
     * 例如:屏幕方向改变时,Activity被销毁再重建;当前Activity处于后台,系统资源紧张将其杀死,用户又启动该Activity.
     * 这两种情况下onRestoreInstanceState都会被调用,在onStart之后.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        param = savedInstanceState.getInt("param");
        Log.i(TAG, "onRestoreInstanceState called. get param: " + param);
        super.onRestoreInstanceState(savedInstanceState);
    }
    private static final String TAG = "MusicTabActivity";
    private int param = 1;
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        String title = "ink media | 不花流量的影音神器";
        String desc = "电视电影动漫音乐，畅享无流量！南开人，你值得拥有。";
        String url = "http://inankai.cn";
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ink);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share_pyq) {
            share.sendUrl(url, true, title, desc, bitmap);
            return true;
        }
        if (id == R.id.action_share_friend) {
            share.sendUrl(url, false, title, desc, bitmap);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.local_tv) {
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);

        } else if (id == R.id.cc_tv) {
            intent = new Intent(this, CctvActivity.class);
            startActivity(intent);

        } else if (id == R.id.movie) {
            intent = new Intent(this, MovieActivity.class);
            startActivity(intent);
        } else if (id == R.id.cartoon) {
            intent = new Intent(this, CartoonActivity.class);
            startActivity(intent);
        } else if (id == R.id.music) {

        } else if (id == R.id.testpaper) {
            intent = new Intent(this, TestpaperActivity.class);
            startActivity(intent);
        } else if (id == R.id.treehole) {
            intent = new Intent(this, TreeholeActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            String title = "ink media | 不花流量的影音神器";
            String desc = "电视电影动漫音乐，畅享无流量！南开人，你值得拥有。";
            String url = "http://inankai.cn";
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ink);
            share.sendUrl(url, true, title, desc, bitmap);
        } else if (id == R.id.nav_send) {
            intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void movieTpye(){
        LinearLayout typeLayout= (LinearLayout) getLayoutInflater()
                .inflate(R.layout.movie_type,null);
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ink)
                .setTitle("电影类型")
                .setView(typeLayout)
                .create()
                .show();
        searchType = (Spinner) typeLayout.findViewById(R.id.movieSpinner);
        movieSearch= (android.support.v7.widget.SearchView) typeLayout.findViewById(R.id.movieSearch);

        //将可选内容与ArrayAdapter连接起来
        adapterType = ArrayAdapter.createFromResource(this, R.array.search_type, android.R.layout.simple_spinner_item);

        //设置下拉列表的风格
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //将adapter2 添加到spinner中
        searchType.setAdapter(adapterType);

        //添加事件Spinner事件监听
        searchType.setOnItemSelectedListener(new SpinnerXMLSelectedListener());

        //设置默认值
        searchType.setVisibility(View.VISIBLE);

        movieSearch.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(context, MovieActivity.class);
                intent.putExtra("searchtype", searchTypeStr);
                try {
                    intent.putExtra("searchstring", URLEncoder.encode(query, "gbk"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }
    //使用XML形式操作
    class SpinnerXMLSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
//            searchTypeStr= (String) adapterType.getItem(arg2);
            switch (arg2){
                case 0:
                    searchTypeStr="name";
                    break;
                case 1:
                    searchTypeStr="director";
                    break;
                case 2:
                    searchTypeStr="actor";
                    break;
            }
//            if(searchTypeStr=="片名")searchTypeStr="name";
//            if(searchTypeStr=="导演")searchTypeStr="director";
//            if(searchTypeStr=="主演")searchTypeStr="actor";
        }

        public void onNothingSelected(AdapterView<?> arg0) {

        }

    }
}
