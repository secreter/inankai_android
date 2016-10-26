package cn.redream.www.ink_media;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

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

public class MusicResultActivity extends AppCompatActivity {
    private String artist;
    private String header;
    private String artist_gb_urlencode;
    Context context=this;

    private String response;
    private ListView resultList;
    private TextView textView;
    private ScrollView scrollView;
    public RedreamApp redreamApp;
    Handler musicHandler;
    WXShareUtil share;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_result);

        share=new WXShareUtil(this);
        redreamApp = ((RedreamApp)getApplicationContext());
        musicHandler=redreamApp.getMusicHandler();
        artist=getIntent().getStringExtra("artist");
        header=getIntent().getStringExtra("header");
        try {
            artist_gb_urlencode = URLEncoder.encode(artist, "gb2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        resultList= (ListView) findViewById(R.id.resultList);
        new Thread() {
            @Override
            public void run() {
                try {
                    response = GetPostUtil.sendPostGbk("http://music.nankai.edu.cn/main.php?iframeID=layer_d_3_I", "searchtype=artist&searchstring=" + artist_gb_urlencode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //发送消息通知ui线程更新UI组件
                handler.sendEmptyMessage(0x123);
            }
        }.start();
        resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //传递参数
                ListView listView = (ListView) parent;
                HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), map.get("id") + "", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                Uri uri = Uri.parse("http://music.nankai.edu.cn/download.php?id=" + map.get("id"));
                intent.setDataAndType(uri, "audio/mp3");
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "end", Toast.LENGTH_SHORT).show();
            }
        });
        this.registerForContextMenu(resultList);

    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(0, 10, 1, "下载");
        menu.add(0,11,2,"分享到朋友圈");
        menu.add(0,12,3,"分享给微信好友");

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo;
        menuInfo= (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index=menuInfo.position;
        //注意，这里targetView是linearlayout，要getParent()才是ListView
        ListView listView= (ListView) menuInfo.targetView.getParent();
        final HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(index);
        final String url="http://music.nankai.edu.cn/download.php?id="+map.get("id");
        final String name=map.get("artist")+"___"+map.get("singName")+"___"+map.get("id")+".mp3";
        final String dirName="Redream/music";
        String title=map.get("singName").toString();
        String description="我正在听南开内网音乐哦~";
        int imgId=Integer.parseInt(map.get("singImg").toString());
        switch (item.getItemId()){
            case 10:
                Toast.makeText(getApplicationContext(), map.get("singName")+"正在后台下载，保存在Redream/music", Toast.LENGTH_LONG).show();
                new Thread() {
                    @Override
                    public void run() {

                        downloadMp3(url, name, dirName);
                        //貌似线程里不可以用更新界面的东西，老出错
//                        Toast.makeText(getApplicationContext(), map.get("singName")+"正在下载", Toast.LENGTH_SHORT).show();
                        handler.sendEmptyMessage(0x125);
                    }

                }.start();

                break;
            case 11:
                share.sendUrlAudio(url, true, title, description, imgId);
                break;
            case 12:
                share.sendUrlAudio(url, false, title, description, imgId);
                break;
            case 20:
                menuInfo= (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Toast.makeText(this,menuInfo+"",Toast.LENGTH_SHORT).show();
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

    Handler handler=new Handler(){
        //注意，桃源网站使用的是gb2312编码，所以提交、得到的文字都要转编码
        @Override
        public void handleMessage(Message msg) {
            List<Map<String,Object>> listItems=new ArrayList<Map<String,Object>>();
            if (msg.what==0x123){
                Pattern p= Pattern.compile("<a href=\"waitorder.+id=(\\d+).+><font color=green>(.+)</font></a>");
                Matcher m=p.matcher(response);
                while(m.find()) {

                    Map<String, Object> listItem = new HashMap<String, Object>();
                    listItem.put("singImg", header);
                    listItem.put("singName",m.group(2) );
                    listItem.put("artist", artist);
                    listItem.put("id", m.group(1));
                    listItems.add(listItem);
                }
//                scrollView.removeView(textView);
                SimpleAdapter simpleAdapter=new SimpleAdapter(context,listItems,R.layout.listview,new String[]{"singImg","singName","artist"},new int[]{R.id.header,R.id.name,R.id.desc});
                resultList.setAdapter(simpleAdapter);

                //取消加载动画
                LinearLayout loading= (LinearLayout) findViewById(R.id.loadingAnim);
                loading.setVisibility(View.GONE);
            }
            if (msg.what==0x124){
                Pattern p= Pattern.compile("<font color=#666666>(.+)</font>[\\s\\S]+<a href=\"waitorder.+id=(\\d+).+><font color=green>(.+)</font></a>");
                Matcher m=p.matcher(response);
                int i=0;
                while(m.find()){
                    System.out.println(m.group(1));
                    System.out.println(m.group(2));
                    System.out.println(m.group(3));
                    Map<String,Object> listItem=new HashMap<String,Object>();
                    listItem.put("header",R.mipmap.chenyixun);
                    listItem.put("tltle",m.group(3));
                    listItem.put("desc",m.group(1));
                    listItem.put("id",m.group(2));
                    listItems.add(listItem);
                }
                SimpleAdapter simpleAdapter=new SimpleAdapter(context,listItems,R.layout.listview,new String[]{"header","tltle","desc"},new int[]{R.id.header,R.id.name,R.id.desc});
                resultList.setAdapter(simpleAdapter);
            }


        }
    };
    private String strToGb2312(String str){
        try {
            str = URLEncoder.encode(str.trim(), "gb2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    ///////////////////////////////////////////////////////////////////////////////////
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
    private static final String TAG = "MusicResultActivity";
    private int param = 1;
}
