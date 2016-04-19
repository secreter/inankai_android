package cn.redream.www.redream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CctvActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,AdapterView.OnItemClickListener {
    private GridView gridView;
    private List<Map<String, Object>> dataList;
    private SimpleAdapter adapter;
    private TvList tvList;
    private Spinner searchType;
    private android.support.v7.widget.SearchView movieSearch;
    private ArrayAdapter adapterType;
    private String movieType;
    private String searchTypeStr="name";  //默认搜索片名
    Context context=this;
    WXShareUtil share;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cctv);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        init();
    }
    private void init(){
        gridView = (GridView) findViewById(R.id.gridviewLocal);
        //1、准备数据源
        //2、新建适配器
        //3、加载适配器
        //4、配置事件监听器
        dataList = new ArrayList<Map<String, Object>>();
        adapter = new SimpleAdapter(this, getData(), R.layout.grview_local, new String[]{"icon", "text"}, new int[]{R.id.icon, R.id.text});
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
        share=new WXShareUtil(this);
    }
    private List<Map<String, Object>> getData() {
        tvList=new TvList();
        int len=tvList.ccTvName.length;
        for (int i = 0; i < len; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("icon", tvList.ccTvIcon[i]);
            map.put("text", tvList.ccTvName[i]);
            map.put("link", tvList.ccTvLink[i]);
            dataList.add(map);
        }
        return dataList;
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        String title="ipv6电视、光影传奇、十二社区、桃源音乐，在南开用Redream就够了，不走流量哦~";
        String desc="一款南开必备神器，墙裂推荐！";
        String url="http://www.redream.cn/main/ipv6tv.php";
        Bitmap bitmap= BitmapFactory.decodeResource(getResources(), R.mipmap.redream);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share_pyq) {
            share.sendUrl(url,true,title,desc,bitmap);
            return true;
        }
        if (id == R.id.action_share_friend) {
            share.sendUrl(url,false,title,desc,bitmap);
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


        } else if (id == R.id.movie) {
            movieTpye();
        }else if (id == R.id.cartoon) {
            intent = new Intent(this, CartoonActivity.class);
            startActivity(intent);
        } else if (id == R.id.music) {
            intent = new Intent(this, MusicTabActivity.class);
            startActivity(intent);
        }else if (id == R.id.testpaper) {
            intent = new Intent(this, TestpaperActivity.class);
            startActivity(intent);
        } else if (id == R.id.treehole) {
            intent = new Intent(this, TreeholeActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_share) {
            String title="ipv6电视、光影传奇、十二社区、桃源音乐，在南开用Redream就够了，不走流量哦~";
            String desc="一款南开必备神器，墙裂推荐！";
            String url="http://www.redream.cn/main/ipv6tv.php";
            Bitmap bitmap= BitmapFactory.decodeResource(getResources(), R.mipmap.redream);
            share.sendUrl(url,true,title,desc,bitmap);
        } else if (id == R.id.nav_send) {
            intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, "我是" + position, Toast.LENGTH_SHORT).show();
        GridView listView= (GridView) parent;
        HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
        Intent intent=new Intent();
        intent.setClass(this, TvplaerActivity.class);
        intent.putExtra("link", map.get("link").toString());
        startActivity(intent);
    }
    public void movieTpye(){
        LinearLayout typeLayout= (LinearLayout) getLayoutInflater()
                .inflate(R.layout.movie_type,null);
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.redream)
                .setTitle("电影类型")
                .setView(typeLayout)
                .create()
                .show();
        searchType = (Spinner) typeLayout.findViewById(R.id.movieSpinner);
        movieSearch= (SearchView) typeLayout.findViewById(R.id.movieSearch);

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

        movieSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
    public void goToMovies(View view){
        movieType= (String) view.getTag();
        Intent intent = new Intent(this, MovieActivity.class);
        try {
            intent.putExtra("movieType",URLEncoder.encode(movieType,"gbk"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        startActivity(intent);

    }
}
