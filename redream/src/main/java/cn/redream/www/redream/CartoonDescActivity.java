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
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.widget.Toast.LENGTH_SHORT;

public class CartoonDescActivity extends AppCompatActivity {
    public static final String FTP_DOWN_LOADING = "ftp文件正在下载";
    public static final String FTP_DOWN_SUCCESS = "ftp文件下载成功";
    public static final String FTP_DOWN_FAIL = "ftp文件下载失败";
    Context context=this;
    private ArrayList<HashMap<String,Object>> listMap;
    private HashMap<String,Object> map;
    private ImageView imageView;
    private TextView nameTv;
    private TextView latestTv;
    private TextView timeTv;
    private TextView downloadCountTv;
    private TextView fansubTv;
    private TextView stateTv;
    private ListView listview;
    private String descLink;
    private String imgLink;
    private String ftpAddr;
    private String response;
    private Handler handler;
    private Bitmap bitmap;
    private String domain="http://12club.nankai.edu.cn";
    final Map<String,Object> listItem=new HashMap<String,Object>();
    private List<Map<String,Object>>  downloadList=new ArrayList<Map<String,Object>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cartoon_desc);
        imageView= (ImageView) findViewById(R.id.img);
        nameTv=(TextView) findViewById(R.id.name);
        timeTv= (TextView) findViewById(R.id.time);
        latestTv= (TextView) findViewById(R.id.latest);
        fansubTv= (TextView) findViewById(R.id.fansub);
        stateTv= (TextView) findViewById(R.id.state);
        downloadCountTv= (TextView) findViewById(R.id.downloadCount);
        listview= (ListView) findViewById(R.id.download_list);


        descLink=getIntent().getStringExtra("descLink");
        imgLink=getIntent().getStringExtra("imgLink");
        listItem.put("descLink", descLink);
        listItem.put("name", getIntent().getStringExtra("name"));
        listItem.put("latest", "最近更新"+getIntent().getStringExtra("latest"));
        listItem.put("time", getIntent().getStringExtra("time"));
        listItem.put("downloadCount", getIntent().getStringExtra("downloadCount"));
        init();
        handMessage();
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                ImageView imageView = (ImageView) view.findViewById(R.id.img);
                imageView.setImageResource(R.mipmap.download_active);
                final HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
                Toast.makeText(context, domain + map.get("downloadLink").toString(), LENGTH_SHORT).show();
                final String filename = listItem.get("name") + map.get("episode").toString() + ".MP4";
                final String localPath = Environment.getExternalStorageDirectory() + "/Redream/comic";
                new Thread() {
                    @Override
                    public void run() {

                        Map<String, List<String>> headerMap = GetPostUtil.getHeaer(domain + map.get("downloadLink").toString(), "");

                        ftpAddr = headerMap.get("Location").get(0);
                        try {
                            ftpAddr = URLDecoder.decode(ftpAddr, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        handler.sendEmptyMessage(0x124);
                        //获取ftp相对地址
                        Pattern pat = Pattern.compile("ftp://Cartoon:Cartoon@222\\.30\\.60\\.30:21([\\s\\S]*)");
                        final Matcher mat = pat.matcher(ftpAddr);
                        String reletivePath = null;
                        if (mat.find()) {
                            reletivePath = mat.group(1);
                        } else {
                            System.out.println("fail");
                        }

                        // 下载
                        try {
//							String name="/cartoon2/16-01/haruchika/";
                            //单文件下载
                            new FTP().downloadSingleFile(reletivePath, localPath, filename, new FTP.DownLoadProgressListener() {

                                @Override
                                public void onDownLoadProgress(String currentStep, long downProcess, File file) {
                                    Log.d("cartoonDownload", currentStep);
                                    if (currentStep.equals(FTP_DOWN_SUCCESS)) {
                                        Log.d("cartoonDownload", "-----xiazai--successful");
                                    } else if (currentStep.equals(FTP_DOWN_LOADING)) {
                                        Log.d("cartoonDownload", "-----xiazai---" + downProcess + "%");
                                    }
                                }

                            });

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        //发送消息通知ui线程更新UI组件
                        handler.sendEmptyMessage(0x125);
                    }
                }.start();
            }
        });

//        this.registerForContextMenu(listview);

    }
    //不支持
//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        menu.add(0, 10, 1, "在线播放");
//
//    }
//    @Override
//    public  boolean onContextItemSelected(MenuItem item){
//        AdapterView.AdapterContextMenuInfo menuInfo;
//        menuInfo= (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        int index=menuInfo.position;
//        //注意，这里targetView是linearlayout，要getParent()才是ListView
//        ListView listView= (ListView) menuInfo.targetView.getParent();
//        final HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(index);
//        final String url=domain + map.get("downloadLink").toString();
//        switch (item.getItemId()){
//
//            case 10:
//                System.out.println(url);
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                Uri uri = Uri.parse(url);
//                intent.setData(uri);
//                startActivity(intent);
//                break;
//        }
//        return true;
//    }
    private void init(){

        new Thread() {
            @Override
            public void run() {
                bitmap=getBitmap(domain + imgLink, 200, 280);
                response = GetPostUtil.sendGet(descLink, "");
                Pattern p= Pattern.compile("字幕组:</td><td class='val'>(.*)</td>[\\s\\S]+当前状态:</td><td class='val'>(.*)</td>");
                final Matcher m= p.matcher(response);
                while (m.find()){
                    System.out.println(m.group(1));
                    listItem.put("fansub", m.group(1));
                    listItem.put("state",m.group(2));
                }
                //下载链接<a href="/program_items/14369" class="download_link" title="第 3 话">第 3 话</a>
                Pattern p2= Pattern.compile("<a href=\"(.*)\" class=\"download_link\" title=\"(.*)\">(.*)</a>\n" +
                        "          <span class='filesize'>(.*)</span>");
                Matcher m2= p2.matcher(response);
                boolean is_match=false;
                while (m2.find()){
                    is_match=true;
                    Map<String,Object> downloadMap=new HashMap<String, Object>();
                    System.out.println(m2.group(1));
                    downloadMap.put("downloadLink", m2.group(1));
                    downloadMap.put("img", R.mipmap.download_default);
                    downloadMap.put("episode", m2.group(2));
                    downloadMap.put("storage",m2.group(4));
                    downloadList.add(downloadMap);
                }
                if(!is_match){
                    //有的没有文件大小。。。
                    p2= Pattern.compile("<a href=\"(.*)\" class=\"download_link\" title=\"(.*)\">(.*)</a>");
                    m2= p2.matcher(response);
                    while (m2.find()){
                        Map<String,Object> downloadMap=new HashMap<String, Object>();
                        System.out.println(m2.group(1));
                        downloadMap.put("downloadLink", m2.group(1));
                        downloadMap.put("img", R.mipmap.download_default);
                        downloadMap.put("episode", m2.group(2));
                        downloadMap.put("storage","未知");
                        downloadList.add(downloadMap);
                    }
                }

                //发送消息通知ui线程更新UI组件
                handler.sendEmptyMessage(0x123);
            }
        }.start();
    }
    //获取网络图片资源，返回类型是Bitmap，用于设置在ListView中
    public Bitmap getBitmap(String httpUrl,int reqWidth,int reqHeight){
        Bitmap bmp = null;
        Bitmap thumbBmp=null;
        //ListView中获取网络图片
        try {
            URL url = new URL(httpUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            InputStream is = conn.getInputStream();
            bmp = BitmapFactory.decodeStream(is);
            thumbBmp=Bitmap.createScaledBitmap(bmp, reqWidth, reqHeight, true);
            //释放图像占用的资源
            bmp.recycle();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return thumbBmp;
    }
    private void handMessage(){
        handler=new Handler(){
            //注意，桃源网站使用的是gb2312编码，所以提交、得到的文字都要转编码
            @Override
            public void handleMessage(Message msg) {

                if (msg.what==0x123){
                    imageView.setImageBitmap(bitmap);
                    nameTv.setText(listItem.get("name").toString());
                    fansubTv.setText(listItem.get("fansub").toString());
                    timeTv.setText(listItem.get("time").toString());
                    stateTv.setText(listItem.get("state").toString());
                    downloadCountTv.setText(listItem.get("downloadCount").toString());

                    SimpleAdapter simpleAdapter=new SimpleAdapter(context,downloadList,R.layout.listview_download_cartoon,new String[]{"img","episode","storage"},new int[]{R.id.img,R.id.episode,R.id.storage});
                    listview.setAdapter(simpleAdapter);

                    LinearLayout loading= (LinearLayout) findViewById(R.id.loadingAnim);
                    loading.setVisibility(View.GONE);

                }
                if (msg.what==0x124){
                    System.out.println(ftpAddr);
                    Toast.makeText(context,"正在下载",Toast.LENGTH_LONG).show();
                }
                if (msg.what==0x125){
                    System.out.println(25);
                    Toast.makeText(context,"下载完成",Toast.LENGTH_LONG).show();
                }

            }
        };
    }
}
