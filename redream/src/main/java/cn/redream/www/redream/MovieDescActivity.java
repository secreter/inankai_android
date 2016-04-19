package cn.redream.www.redream;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
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
    private String response;
    private String responseRealUrl;
    private Map<String,String> infoMap=new HashMap<String,String>();
    private String[] infoMapKey=new String[]{
            "director","actor","country","type","language","time","date","count","movieDesc"
    };
    private String[] infoMapKeyZh=new String[]{
            "导演：","主演：","国家：","类型：","语言：","时间：","上映日期：","下载量：","剧情"
    };
    private List<String> downloadUrlList=new ArrayList<String>();//list只有一个元素的是电影，有多个的是连续剧
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


    private ListView buttonListView;
    private List<Map<String,String>> buttonListData=new ArrayList<Map<String,String>>();

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
//                        SimpleAdapter simpleAdapter=new SimpleAdapter(context,buttonListData,R.layout.listview_download_movie,new String[]{"download","play"},new int[]{R.id.movieDownload,R.id.moviePlay});
//                        buttonListView.setAdapter(simpleAdapter);
                        MovieDownloadListViewAdapter adapter=new MovieDownloadListViewAdapter(context,buttonListData);
                        buttonListView.setAdapter(adapter);
                    }else{
                        buttonListData.get(0).put("download","下载");
                        buttonListData.get(0).put("play","播放");
//                        SimpleAdapter simpleAdapter=new SimpleAdapter(context,buttonListData,R.layout.listview_download_movie,new String[]{"download","play"},new int[]{R.id.movieDownload,R.id.moviePlay});
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
                if (msg.what==0x126){
                    Toast.makeText(getApplicationContext(), movieName+"下载成功！", Toast.LENGTH_SHORT).show();
                    moviePosterView.setImageBitmap(posterImg);
                }
                if (msg.what==0x126){
                    LinearLayout loading= (LinearLayout) findViewById(R.id.loadingAnim);
                    loading.setVisibility(View.GONE);
                }

            }
        };
    }

    private void init(){

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
                Toast.makeText(context,"click",Toast.LENGTH_LONG).show();
                new Thread(){
                    @Override
                    public void run() {
                        ListView listView= (ListView) parent;
                        HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
                        Message msg=new Message();
                        msg.what=0x125;
                        msg.obj=getRealDownloadUrl((String) map.get("download"));
                        handler.sendMessage(msg);

                    }
                }.start();

            }
        });

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
        Toast.makeText(getApplicationContext(), movieName+"正在后台下载，保存在Redream/movie", Toast.LENGTH_LONG).show();
        Log.v("pcy", (String) view.getTag());

        new Thread() {
            @Override
            public void run() {
                final String url=getRealDownloadUrl((String) view.getTag());
                final String name=movieName+".rmvb";
                final String dirName="Redream/movie";
                downloadMp3(url, name, dirName);
                handler.sendEmptyMessage(0x126);
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
                final String url=getRealDownloadUrl((String) view.getTag());
                String title=movieName;
                String description="我正用Redream在看南开内网电影哦~";
                share.sendUrlAudio(url, true, title, description, R.mipmap.movie_share);
                handler.sendEmptyMessage(0x127);
            }

        }.start();

    }
    private void getInfo(){
        new Thread() {
            @Override
            public void run() {
                response = GetPostUtil.sendGetGbk(MOVIE_INFO_URL, "id="+posterimgId);
                Pattern p = Pattern.compile("<span style=\"color: #016A9F\">(.*)</span>");
                final Matcher m = p.matcher(response);
                int i = 0;
                while(m.find()){
                    infoMap.put(infoMapKey[i], infoMapKeyZh[i]+m.group(1));
                    i++;
                }

                //剧情
                Pattern p2 = Pattern.compile("align=\"justify\">([\\s\\S]*?)</td>");
                final Matcher m2 = p2.matcher(response);
                m2.find();
                String desc=m2.group(1);
                desc=desc.replaceAll("&nbsp;"," ");

                infoMap.put(infoMapKey[8], desc+desc);    //之前用这个infoMapKey[i]，结果网站上条目数目不一，有时会超出数组界限

                //下载链接
                //这个链接还要处理好几次<a onclick="haveclick();hotmovie()" href="joyview://MjAxNlyxvLCufDc0NzB8sbywri5ybXZifDg5MzUyNzc4OXxA" target="_self">下载播放</a>
                Pattern p3 = Pattern.compile("href=\"joyview://(.*)\" target=\"_self\">(.*)</a>");
                final Matcher m3 = p3.matcher(response);
                String realUrl;
                while (m3.find()) {
                    System.out.println(m3.group(2));
                    Map<String, String> listItem = new HashMap<String, String>();

//                    System.out.println(realUrl);
                    listItem.put("downloadLink",m3.group(1));//存的是joyview的base64信息
                    listItem.put("download", "下载"+m3.group(2));
                    listItem.put("play", "播放" + m3.group(2));
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
        responseRealUrl = GetPostUtil.sendGetGbk(GET_IP_UR, "version=1.2.0.3&filmid=" + movieId + GET_IP_URL_AFTER);
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

                int length = 0;
                //将输入流中的内容先输入到buffer中缓存，然后用输出流写到文件中
                while((length = input.read(buffer)) != -1)
                {
                    output.write(buffer,0,length);
                    System.out.println(length);
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