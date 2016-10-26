package cn.redream.www.ink_media;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RemoteViews;
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

public class MovieDescActivity extends AppCompatActivity {
    //这里用光影传奇的域名dns解析特别慢
    public static final String HOME_PAGE_URL="http://222.30.44.37/";
    public static final String MOVIE_INFO_URL="http://222.30.44.37/filminfo.php";
    public static final String SMALL_PIC_URL="http://222.30.44.37/posterimgs/small/";
    public static final String BIG_PIC_URL="http://222.30.44.37/posterimgs/big/";
    public static final String GET_IP_UR="http://222.30.44.37/joyview/joyview_getip.php";
    public static final String GET_IP_URL_BEFORE="http://222.30.44.37/joyview/joyview_getip.php?version=1.2.0.3&filmid=";
    public static final String GET_IP_URL_AFTER="&getnum=2&d_from=8&d_to=8&port=8080";
    public static final String SEARCH_URL="http://222.30.44.37/filmclass.php?action=search";
    public static final String CATEGORY_URL="http://222.30.44.37/filmclass.php?page=0&class=type&content=";
    private Context context=this;
    private static final int MSG_NET_ERROR =0x140 ;
    RedreamApp redreamApp;
    private String response;
    private String responseRealUrl;
    private Map<String,String> infoMap=new HashMap<String,String>();
    private String[] infoMapKey=new String[]{
            "director","actor","country","type","language","time","version","date","count","movieDesc"
    };
    private String[] infoMapKeyZh=new String[]{
            "导演：","主演：","国家：","类型：","语言：","片长：","版本", "上映日期：","下载量：","剧情"
    };
    private List<String> downloadUrlList=new ArrayList<>();//list只有一个元素的是电影，有多个的是连续剧
    private Bitmap posterImg;
    private Handler handler;
    private String  posterimgId;
    private String movieName;
    private ImageView moviePosterView;
    private TextView nameView;
    private TextView directorView;
    private TextView actorView;
    private TextView countryView;
    private TextView languageView;
    private TextView timeView;
    private TextView dateView;
    private TextView movieDescView;
    WXShareUtil share;

    private int len;
    private int notification_id=0;
    private List<String> downloadName=new ArrayList<>();
//    private NotificationManager manager;
//    private Notification notif;


    private ListView buttonListView;
    private List<Map<String,String>> buttonListData=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_desc);
        init();
    }
    private void handMessage(){
        handler=new Handler(){
            //注意，桃源网站使用的是gb2312编码，所以提交、得到的文字都要转编码
            @Override
            public void handleMessage(Message msg) {

                if (msg.what==0x123){
                    nameView.setText(movieName);
                    directorView.setText(infoMap.get("director"));
                    actorView.setText(infoMap.get("actor"));
                    countryView.setText(infoMap.get("country"));
                    languageView.setText(infoMap.get("language"));
                    timeView.setText(infoMap.get("time"));
                    dateView.setText(infoMap.get("date"));
                    movieDescView.setText(infoMap.get("movieDesc").replace("<br>",""));
                    //判断集数
                    if (buttonListData.size()>1){
//                        SimpleAdapter simpleAdapter=new SimpleAdapter(context,buttonListData,R.tab_focused.listview_download_movie,new String[]{"download","play"},new int[]{R.id.movieDownload,R.id.moviePlay});
//                        buttonListView.setAdapter(simpleAdapter);
                        MovieDownloadListViewAdapter adapter=new MovieDownloadListViewAdapter(context,buttonListData);
                        buttonListView.setAdapter(adapter);
                    }else{
                        buttonListData.get(0).put("download","下载");
                        buttonListData.get(0).put("play","播放");
//                        SimpleAdapter simpleAdapter=new SimpleAdapter(context,buttonListData,R.tab_focused.listview_download_movie,new String[]{"download","play"},new int[]{R.id.movieDownload,R.id.moviePlay});
//                        buttonListView.setAdapter(simpleAdapter);
                        MovieDownloadListViewAdapter adapter=new MovieDownloadListViewAdapter(context,buttonListData);
                        buttonListView.setAdapter(adapter);
                    }
                    fixListViewHeight(buttonListView);

                    LinearLayout loading= (LinearLayout) findViewById(R.id.loadingAnim);
                    loading.setVisibility(View.GONE);


                }
                if (msg.what==0x124){
                    moviePosterView.setImageBitmap(posterImg);
                }
                if(msg.what==0x125){
                    LinearLayout loading= (LinearLayout) findViewById(R.id.loadingAnim);
                    loading.setVisibility(View.GONE);

                    System.out.println((String) msg.obj);
                    String realUrl=(String)msg.obj;
                    Intent intent = new Intent(Intent.ACTION_VIEW);
//        String type = "video/* ";
                    Uri uri = Uri.parse(realUrl);
//        intent.setDataAndType(uri, type);
                    intent.setData(uri);
                    startActivity(intent);

                }

                if (msg.what==0x127){
                    LinearLayout loading= (LinearLayout) findViewById(R.id.loadingAnim);
                    loading.setVisibility(View.GONE);
                }
                for (int i = 0; i < notification_id; i++) {
                    if (msg.what==i){
                        NotificationManager manager;
                        Notification notif;

                        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        notif= (Notification) msg.obj;
                        notif.contentView.setTextViewText(R.id.content_view_text1, downloadName.get(i) + "已下载" + Math.ceil(100 * (float) msg.arg2 / msg.arg1) + "%"+"共"+ msg.arg1 /1048576+"M");
                        notif.contentView.setProgressBar(R.id.content_view_progress, msg.arg1, msg.arg2, false);
                        manager.notify(i, notif);
                        if (msg.arg2== msg.arg1){
                            manager.cancel(i);
                            Toast.makeText(getApplicationContext(), downloadName.get(i)+"下载成功！", Toast.LENGTH_SHORT).show();
                        }
//
//                    moviePosterView.setImageBitmap(posterImg);
                    }
                }

            }
        };
    }

    private void init(){
        redreamApp = ((RedreamApp)getApplicationContext());   //用他的movieUrlList来存储正在下载的cartoon，不让再次点击覆盖下载
        moviePosterView= (ImageView) findViewById(R.id.moviePoster);
        nameView= (TextView) findViewById(R.id.movieName);
        directorView= (TextView) findViewById(R.id.director);
        actorView= (TextView) findViewById(R.id.actor);
        countryView= (TextView) findViewById(R.id.country);
        languageView= (TextView) findViewById(R.id.language);
        timeView= (TextView) findViewById(R.id.time);
        dateView= (TextView) findViewById(R.id.date);
        movieDescView= (TextView) findViewById(R.id.movieDesc);
        buttonListView= (ListView) findViewById(R.id.download_list);
        share=new WXShareUtil(this);
        Intent intent=getIntent();
        posterimgId=intent.getStringExtra("posterimgId");
        movieName=intent.getStringExtra("movieName");

//        posterimgId="1933";
        handMessage();
        getInfo();
        getPosterImg();
        buttonListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                Toast.makeText(context, "click", Toast.LENGTH_LONG).show();
                new Thread() {
                    @Override
                    public void run() {
                        ListView listView = (ListView) parent;
                        HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
                        Message msg = new Message();
                        msg.what = 0x125;
                        msg.obj = getRealDownloadUrl((String) map.get("download"));
                        handler.sendMessage(msg);

                    }
                }.start();

            }
        });

    }
    private void initProgress(String name){
        NotificationManager manager;
        Notification notif;
        Intent intent = new Intent(this,MovieActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, notification_id, intent, 0);
        notification_id++;
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notif = new Notification();
        notif.icon = R.mipmap.ink;
        notif.tickerText = "新通知";

        //通知栏显示所用到的布局文件
        notif.contentView = new RemoteViews(getPackageName(), R.layout.notification_view);
        notif.contentView.setTextViewText(R.id.content_view_text1, name + "正在下载");
        notif.contentIntent = pIntent;
        manager.notify(notification_id, notif);


    }
    public void movie_play(final View view){
        LinearLayout loading= (LinearLayout) findViewById(R.id.loadingAnim);
        loading.setVisibility(View.INVISIBLE);
        TextView loadingText= (TextView) findViewById(R.id.loadingText);
        loadingText.setText("正在获取电影链接，请稍等一下下哦~部分手机不支持，请下载后观看~");
        new Thread(){
            @Override
            public void run() {
                ListView listView= (ListView) view.getParent().getParent();
//                HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
                Message msg=new Message();
                msg.what=0x125;
                msg.obj=getRealDownloadUrl((String) view.getTag());
                handler.sendMessage(msg);

            }
        }.start();
    }
    public void movie_download(final View view){
        final String name;
        Button btn= (Button) view;
        name= movieName+(String) btn.getText().subSequence(2,btn.getText().length());
        if (redreamApp.movieUrlList.contains(name)){
            Toast.makeText(getApplicationContext(), name+"已经下载", Toast.LENGTH_LONG).show();
            return;
        }
        redreamApp.movieUrlList.add(name);
        Toast.makeText(getApplicationContext(), name+"正在后台下载，保存在inankai/movie", Toast.LENGTH_LONG).show();
        Log.v("pcy", (String) view.getTag());
//        initProgress(name);




        //新建一个电影下载线程
        new Thread() {
            @Override
            public void run() {
                NotificationManager manager;   //通知管理器
                Notification notif;             //通知实体
                Intent intent = new Intent(context,MovieActivity.class);     //新建一个意图
                //Notification 接下来要跳转的意图
                PendingIntent pIntent = PendingIntent.getActivity(context, notification_id, intent, 0);

                manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notif = new Notification();
                notif.icon = R.mipmap.ink;    //配置状态栏的通知图标、文字
                notif.tickerText = "inankai";
                //通知栏显示所用到的布局文件
                notif.contentView = new RemoteViews(getPackageName(), R.layout.notification_view);
                notif.contentView.setTextViewText(R.id.content_view_text1, name+"正在下载");
                notif.contentIntent = pIntent;
                manager.notify(notification_id, notif);   //发出带有唯一id的通知
                downloadName.add(name);
                final Message msg=new Message();          //创建一个消息对象
                msg.obj=notif;                            //记住通知对象
                msg.what=notification_id;                 //记住通知id
                notification_id++;                        //id++,因为可以同时下载多部电影

                final String url=getRealDownloadUrl((String) view.getTag());   //获取下载链接
                final String filename=name+".rmvb";
                final String dirName="inankai/movie";
                downloadMp3(url, filename, dirName, msg);                      //下载
            }
        }.start();
    }
    public void movie_share(final View view){
        LinearLayout loading= (LinearLayout) findViewById(R.id.loadingAnim);
        loading.setVisibility(View.INVISIBLE);
        TextView loadingText= (TextView) findViewById(R.id.loadingText);
        loadingText.setText("正在为您生成电影链接，请稍等一下下哦~");
        new Thread() {
            @Override
            public void run() {
//                final String url=getRealDownloadUrl((String) view.getTag());
//                final String url=MOVIE_INFO_URL+"?id="+posterimgId;
                final String url="http://inankai.cn/new/index.html#app";
                String title=movieName+" | ink media";
                String description="ink media看南开内网电影,免流量哦~";

                share.sendUrl(url, true, title, description, BitmapFactory.decodeResource(getResources(), R.mipmap.ink));
                handler.sendEmptyMessage(0x127);
            }

        }.start();

    }
    private void getInfo(){
        new Thread() {
            @Override
            public void run() {
                try {
                    response = GetPostUtil.sendGetGbk(MOVIE_INFO_URL, "id="+posterimgId);
                } catch (MalformedURLException e) {
                    //网络错误
                    handler.sendEmptyMessage(MSG_NET_ERROR);
                    e.printStackTrace();
                    return;
                } catch (IOException e) {
                    handler.sendEmptyMessage(MSG_NET_ERROR);
                    e.printStackTrace();
                }
                Pattern p = Pattern.compile(">\\s+(.+)<span style=\"color: #016A9F\">(.*)</span>");
                final Matcher m = p.matcher(response);
                int i = 0;
                while(m.find()){
                    infoMap.put(infoMapKey[i], m.group(1)+m.group(2));
                    i++;
                }

                //剧情
                Pattern p2 = Pattern.compile("align=\"justify\">([\\s\\S]*?)</td>");
                final Matcher m2 = p2.matcher(response);
                m2.find();
                String desc=m2.group(1);
                desc=desc.replaceAll("&nbsp;"," ");

                infoMap.put(infoMapKey[9], desc);    //之前用这个infoMapKey[i]，结果网站上条目数目不一，有时会超出数组界限

                //下载链接
                //这个链接还要处理好几次<a onclick="haveclick();hotmovie()" href="joyview://MjAxNlyxvLCufDc0NzB8sbywri5ybXZifDg5MzUyNzc4OXxA" target="_self">下载播放</a>
                Pattern p3 = Pattern.compile("href=\"joyview://(.*)\" target=\"_self\">(.*)</a>");
                final Matcher m3 = p3.matcher(response);
                String realUrl;
                int j=0;
                while (m3.find()) {
                    j++;
                    System.out.println(m3.group(2));
                    Map<String, String> listItem = new HashMap<>();

//                    System.out.println(realUrl);
                    listItem.put("downloadLink",m3.group(1));//存的是joyview的base64信息
                    listItem.put("download", "下载"+m3.group(2));
                    listItem.put("play", "播放" + m3.group(2));
                    buttonListData.add(listItem);
                }
                if (j==0){
                    //没匹配到，居然上传空。。。。
                    Map<String, String> listItem = new HashMap<>();
                    buttonListData.add(listItem);
                }



                handler.sendEmptyMessage(0x123);

            }
        }.start();
    }
    private void getPosterImg(){
        new Thread() {
            @Override
            public void run() {
                posterImg=getBitmap(BIG_PIC_URL+posterimgId+".jpg");

                handler.sendEmptyMessage(0x124);

            }
        }.start();
    }
    //获取网络图片资源，返回类型是Bitmap，用于设置在ListView中
    public Bitmap getBitmap(String httpUrl){
        Bitmap bmp = null;
        try {
            URL url = new URL(httpUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            InputStream is = conn.getInputStream();
            bmp = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bmp;
    }
    private String getRealDownloadUrl(String joyviowStr){
        String realUrl=null;
        if (joyviowStr == null) return null;
        try {
            realUrl=new String(Base64.decode(joyviowStr,Base64.DEFAULT),"gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //解码后  episode\破产姐妹第五季|7382|2.Broke.Girls.S05E08.720p.HDTV.X264-DIMENSION.rmvb|136319461|@
        Pattern p = Pattern.compile("(.*)\\|(.*)\\|(.*)\\|(.*)\\|@");
        final Matcher m = p.matcher(realUrl);
        String movieName=null;
        String movieId=null;
        while(m.find()){
            movieName=m.group(3);
            movieId=m.group(2);
        }
        System.out.println(movieName);
        try {
            responseRealUrl = GetPostUtil.sendGetGbk(GET_IP_UR, "version=1.2.0.3&filmid=" + movieId + GET_IP_URL_AFTER);
        } catch (MalformedURLException e) {
            //网络错误
            handler.sendEmptyMessage(MSG_NET_ERROR);
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            handler.sendEmptyMessage(MSG_NET_ERROR);
            e.printStackTrace();
        }
        Pattern p2 = Pattern.compile("\\*(.*?)\\|(.*?)\\|(.*?)");
        final Matcher m2 = p2.matcher(responseRealUrl);
        System.out.println(responseRealUrl);
        m2.find();
        m2.group(1);
        try {
            realUrl=m2.group(1)+"/"+ URLEncoder.encode(movieName,"GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(realUrl);


        return realUrl;
    }
    //动态调节listview高度
    public void fixListViewHeight(ListView listView) {
        // 如果没有设置数据适配器，则ListView没有子项，返回。
        ListAdapter listAdapter = listView.getAdapter();
        int totalHeight = 0;
        if (listAdapter == null) {
            return;
        }
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listViewItem = listAdapter.getView(i , null, listView);
            // 计算子项View 的宽高
            listViewItem.measure(0, 0);
            // 计算所有子项的高度和
            totalHeight += listViewItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        // listView.getDividerHeight()获取子项间分隔符的高度
        // params.height设置ListView完全显示需要的高度
        params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
    private void downloadMp3(String urlStr,String fileName,String path,Message msg_final){
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
            //http://222.30.44.35:80/episode/%D0%D0%CA%AC%D7%DF%C8%E2%B5%DA%C6%DF%BC%BE/The.Walking.Dead.S07E01.720p.HDTV.x264-KILLERS.rmvb
            //出了点问题，获取到的ip地址找不到资源，抓包发现，不断请求，最后222.30.44.33有资源。2016.10.15
            urlStr=urlStr.replace("222.30.44.35","222.30.44.33");  //这一行硬编码真是恶心。。。想想怎么把它弄掉
            URL url=new URL(urlStr);
            HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            conn.connect();
            //获取相应的文件长度
            int fileLength = conn.getContentLength();

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
                   boolean b=new File(dir).mkdirs();//新建文件夹



                }


                file.createNewFile();//新建文件
                output=new FileOutputStream(file);
                //读取大文件
                byte[] buffer=new byte[4*1024];

                int length = 0;
                int done_len=0;
                int i=0;
                //将输入流中的内容先输入到buffer中缓存，然后用输出流写到文件中
                while((length = input.read(buffer)) != -1)
                {
                    output.write(buffer, 0, length);
//                    System.out.println(length);
                    done_len=done_len+length;
                    System.out.println(done_len+"/"+fileLength);
                    if (i++%1000==0){
                        Message msg=new Message();
                        msg.what=msg_final.what;
                        msg.obj=msg_final.obj;
                        msg.arg1=fileLength;
                        msg.arg2=done_len;
                        handler.sendMessage(msg);
                    }
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


}
