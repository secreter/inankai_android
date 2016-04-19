package cn.redream.www.redream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MovieActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String HOME_PAGE_URL="http://222.30.44.37/";
    public static final String MOVIE_INFO_URL="http://222.30.44.37/filminfo.php?id=";
    public static final String SMALL_PIC_URL="http://222.30.44.37/posterimgs/small/";
    public static final String BIG_PIC_URL="http://222.30.44.37/posterimgs/big/";
    public static final String GET_IP_URL_BEFORE="http://222.30.44.37/joyview/joyview_getip.php?version=1.2.0.3&filmid=";
    public static final String GET_IP_URL_AFTER="&getnum=2&d_from=8&d_to=8&port=8080";
    public static final String SEARCH_URL="http://222.30.44.37/filmclass.php?action=search";
    public static final String CATEGORY_URL="http://222.30.44.37/filmclass.php";
    private static final int MSG_UPDATE_POSTER =0x130 ;
    String response;
    Context context=this;
    public Handler handler;
    WXShareUtil share;
    public Map<String,Bitmap> moviePosterMap=new HashMap<String,Bitmap>();
    private String domain="http://222.30.44.37/";
//    private String domain="http://movie.nku.cn/";
    private ListView movieListView;
    private String movieType;
    private String searchType="name";  //默认搜索片名
    private String searchString;
    private List<Map<String,Object>> movieListData=new ArrayList<Map<String,Object>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //
        init();
    }

    @Override
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
//        getMenuInflater().inflate(R.menu.movie, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
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

        } else if (id == R.id.cartoon) {
            intent = new Intent(this, CartoonActivity.class);
            startActivity(intent);
        }else if (id == R.id.music) {
            intent = new Intent(this, MusicTabActivity.class);
            startActivity(intent);
        } else if (id == R.id.treehole) {
            intent = new Intent(this, TreeholeActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void initList() {
        //创建一个List集合，list集合的元素是Map
        new Thread() {
            @Override
            public void run() {
                String url;
                String params;
                Intent intent=getIntent();
                Pattern p;
                int i = 0;
                if (intent.getStringExtra("movieType")!=null&&!intent.getStringExtra("movieType").equals("")){
                    //按类型
                    url=CATEGORY_URL;
                    params="page=0&class=type&content="+intent.getStringExtra("movieType");
                    response = GetPostUtil.sendGetGbk(url,params);

                    p = Pattern.compile("<a href=\"javascript:layer1On\\('(\\d+)'\\)\"><img border=\"0\" src=\"(.*)\" width=\"86\" height=\"121\"></a>");
                    final Matcher m = p.matcher(response);
                    while (m.find()) {
                        System.out.println(domain + m.group(1));
                        System.out.println(m.group(1));
                        final Map<String, Object> listItem = new HashMap<String, Object>();
                        listItem.put("posterimgId", m.group(1));
                        try {
                            listItem.put("movieType", URLDecoder.decode(intent.getStringExtra("movieType"),"gbk"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        listItem.put("link", m.group(2));
                        movieListData.add(listItem);
                    }
                }else if (intent.getStringExtra("searchstring")!=null){
                    url=SEARCH_URL;
                    params="searchtype="+intent.getStringExtra("searchtype")+"&searchstring="+intent.getStringExtra("searchstring");
                    response = GetPostUtil.sendPostGbk(url,params);

                    p = Pattern.compile("<a href=\"javascript:layer1On\\('(\\d+)'\\)\"><img border=\"0\" src=\"(.*)\" width=\"86\" height=\"121\"></a>");
                    final Matcher m = p.matcher(response);
                    while (m.find()) {
                        System.out.println(domain + m.group(1));
                        System.out.println(m.group(1));
                        final Map<String, Object> listItem = new HashMap<String, Object>();
                        listItem.put("posterimgId", m.group(1));
                        try {
                            listItem.put("movieType", URLDecoder.decode(intent.getStringExtra("searchstring"), "gbk"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        listItem.put("link", m.group(2));
                        movieListData.add(listItem);
                    }
                }else{
                    url=HOME_PAGE_URL;
                    response = GetPostUtil.sendGetGbk(url,null);
                    //这个有title
                    p = Pattern.compile("<a href=\"javascript:layer1On\\('(\\d+)'\\)\" title=\"(.*)\"><img border=\"0\" src=\"(.*)\" width=\"86\" height=\"121\"></a>");
                    final Matcher m = p.matcher(response);
                    while (m.find()) {
                        System.out.println(domain + m.group(1));
                        System.out.println(m.group(1));
                        final Map<String, Object> listItem = new HashMap<String, Object>();
                        listItem.put("posterimgId", m.group(1));
                        listItem.put("movieType", m.group(2));
                        listItem.put("link", m.group(3));
                        movieListData.add(listItem);
                    }
                }



                Pattern p2 = Pattern.compile("<td><div class=\"out2\"><span style=\"color: #016A9F\">(.*)</span><br>(.*)</div></td>");
                final Matcher m2 = p2.matcher(response);
                while (m2.find()) {
                    System.out.println(m2.group(1));
                    movieListData.get(i).put("movieName", m2.group(1));
                    movieListData.get(i).put("movieDesc", m2.group(2));
                    i++;
                }



                //发送消息通知ui线程更新UI组件
                handler.sendEmptyMessage(0x123);


            }
        }.start();
    }


    private void handMessage(){
        handler=new Handler(){
            //注意，桃源网站使用的是gb2312编码，所以提交、得到的文字都要转编码
            @Override
            public void handleMessage(Message msg) {

                if (msg.what==0x123){

                    AsyncMovieListViewAdapter asyncMovieListViewAdapter =new AsyncMovieListViewAdapter(context,movieListData);
//                    simpleAdapter.setViewBinder(new MyViewBinder());
                    movieListView.setAdapter(asyncMovieListViewAdapter);
                    LinearLayout loading= (LinearLayout) findViewById(R.id.loadingAnim);
                    loading.setVisibility(View.GONE);
                    if (asyncMovieListViewAdapter.getCount()==0){
                        Toast.makeText(context,"暂时没有搜到相关电影，换个词语试试吧~",Toast.LENGTH_LONG).show();
                    }

                }


            }
        };
    }
    private void init(){
        movieListView= (ListView) findViewById(R.id.movieList);
        initList();
        handMessage();
        movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView= (ListView) parent;
                HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
                Intent intent=new Intent();
                intent.setClass(context, MovieDescActivity.class);
                intent.putExtra("posterimgId", map.get("posterimgId").toString());
                intent.putExtra("movieName", map.get("movieName").toString());
                startActivity(intent);
            }
        });
    }
    private String strToGbk(String uft8Str){
        String str=null;
        try {
            str=new String(uft8Str.getBytes("utf-8"),"gb2312" );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }
    private String strToutf8(String gbkStr){
        String str=null;
        try {
            str=new String(gbkStr.getBytes("gb2312"),"utf-8" );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }




}
