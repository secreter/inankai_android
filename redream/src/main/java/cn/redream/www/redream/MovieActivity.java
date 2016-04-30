package cn.redream.www.redream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MovieActivity extends AppCompatActivity
        implements TabHost.OnTabChangeListener, NavigationView.OnNavigationItemSelectedListener {
    public static final String HOME_PAGE_URL="http://222.30.44.37/";
    public static final String MOVIE_INFO_URL="http://222.30.44.37/filminfo.php?id=";
    public static final String SMALL_PIC_URL="http://222.30.44.37/posterimgs/small/";
    public static final String BIG_PIC_URL="http://222.30.44.37/posterimgs/big/";
    public static final String GET_IP_URL_BEFORE="http://222.30.44.37/joyview/joyview_getip.php?version=1.2.0.3&filmid=";
    public static final String GET_IP_URL_AFTER="&getnum=2&d_from=8&d_to=8&port=8080";
    public static final String SEARCH_URL="http://222.30.44.37/filmclass.php?action=search";
    public static final String CATEGORY_URL="http://222.30.44.37/filmclass.php";
    private static final int MSG_UPDATE_POSTER =0x130 ;
    static final String LOCAL_DIR="inankai/movie";
    String response;
    String typeNumResponse;
    Context context=this;
    public Handler handler;
    WXShareUtil share;
    public Map<String,Bitmap> moviePosterMap=new HashMap<String,Bitmap>();
    private String domain="http://222.30.44.37/";
//    private String domain="http://movie.nku.cn/";
    private ListView movieTypeList;
    private GridView movieGridView;
    private String movieType;
    private String searchType="name";  //默认搜索片名
    private String searchString;
    private List<Map<String,Object>> typeListData=new ArrayList<>();
    private List<Map<String,Object>> movieListData=new ArrayList<>();
    private MenuItem thisActMenuItem;
    private NavigationView navigationView;
    private TabHost tabHost;
    private Spinner searchSpinner;
    private SearchView searchMovie;
    private String[] typeArr={
            "最新","剧情","喜剧","爱情","科幻","奇幻","动作","悬疑","惊悚","战争","动画","记录","外语教学","其它"
    };
    private String[] typeArrShow={
            "最新/New","剧情/Drama","喜剧/Comedy","爱情/Romance","科幻/SciFi","奇幻/Fantasy","动作/Action","悬疑/Mystery","惊悚/Thriller","战争/War","动画/Cartoon","记录/Documentary","外语教学/Teaching","其它/Other"
    };
    private String[] typeNum=new String[20];
    private int[] typeMap={18,14,4,8,17,7,11,2,16,5,10,13,3,6};
    private ListView localList;
    private ArrayAdapter<CharSequence> adapterType;

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

        this.navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //
        init();
    }

    protected void onResume() {
        super.onResume();
        //起始化进入该页面时设置其本身被选中
        thisActMenuItem= navigationView.getMenu().getItem(2);
        thisActMenuItem.setChecked(true); // 改变item选中状态
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

        String title = "ipv6电视、光影传奇、十二社区、桃源音乐，在南开用inankai就够了，不走流量哦~";
        String desc = "一款南开必备神器，墙裂推荐！";
        String url = "http://inankai.cn";
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ink);

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
            String title = "ipv6电视、光影传奇、十二社区、桃源音乐，在南开用inankai就够了，不走流量哦~";
            String desc = "一款南开必备神器，墙裂推荐！";
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
    @Override
    public void onTabChanged(String tabId) {
        if ("tab1".equals(tabId)) {
        }
        if ("tab2".equals(tabId)) {
        }
        tabHost.setCurrentTabByTag(tabId);
        updateTab(tabHost);
    }

    private void initTabHost() {
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        //不能少setup()哟~~~
        tabHost.setup();
//        //去除原来的灰色的难看的系统自带下划线
//        tabHost.getTabWidget().setStripEnabled(true);

        TabHost.TabSpec tab1 = tabHost.newTabSpec("tab1")
                .setIndicator("分类") //创建标题
                .setContent(R.id.tab1);//创建内容
        //添加第一个标签页
        tabHost.addTab(tab1);
        TabHost.TabSpec tab2 = tabHost.newTabSpec("tab2")
                .setIndicator("片库") //创建标题
                .setContent(R.id.tab2);//创建内容
        //添加第一个标签页
        tabHost.addTab(tab2);
        TabHost.TabSpec tab3 = tabHost.newTabSpec("tab3")
                .setIndicator("本地", getResources() //放置图标
                        .getDrawable(R.mipmap.games_control))
                .setContent(R.id.tab3);//创建内容
        // 添加第一个标签页
        tabHost.addTab(tab3);
        updateTab(tabHost);//初始化Tab的颜色，和字体的颜色
        tabHost.setOnTabChangedListener(this);// 选择监听器

        final TabWidget tabWidget = tabHost.getTabWidget();

    }
    private void updateTab(final TabHost tabHost) {

        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            View view = tabHost.getTabWidget().getChildAt(i);
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextSize(15);
            tv.setTextColor(getResources().getColor(R.color.colorBlank));
            if (tabHost.getCurrentTab() == i) {//选中
                view.setBackgroundResource(R.drawable.tab_focused);

                tv.setTextColor(getResources().getColor(R.color.colorBlank));
            } else {//不选中
                view.setBackgroundResource(R.drawable.tab_normal);

                tv.setTextColor(getResources().getColor(R.color.colorGray));
            }

        }
    }
    private void initSearchView(){
        searchSpinner = (Spinner) findViewById(R.id.movieSpinner);

        //将可选内容与ArrayAdapter连接起来
        adapterType = ArrayAdapter.createFromResource(this, R.array.search_type, android.R.layout.simple_spinner_item);

        //设置下拉列表的风格
        adapterType.setDropDownViewResource(R.layout.drop_down_item);

        //将adapter2 添加到spinner中
        searchSpinner.setAdapter(adapterType);

        //添加事件Spinner事件监听
        searchSpinner.setOnItemSelectedListener(new SpinnerXMLSelectedListener());

        //设置默认值
        searchSpinner.setVisibility(View.VISIBLE);



        searchMovie= (SearchView) findViewById(R.id.searchMovie);
        //设置放大镜的图标为黑色
        int search_mag_icon_id = searchMovie.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView search_mag_icon = (ImageView)searchMovie.findViewById(search_mag_icon_id);//获取搜索图标
//        ImageView search_button = (ImageView) searchMovie.findViewById(android.R.id.search_button);
        search_mag_icon.setImageResource(R.mipmap.search3);//图标都是用src的
        //将其展开后改变图标才有用
        searchMovie.setIconifiedByDefault(false);
        //初始时失去焦点
        searchMovie.setFocusable(false);
        //设置字体为黑色
        int id = searchMovie.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchMovie.findViewById(id);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(14);
        textView.setHintTextColor(getResources().getColor(R.color.grey));
    }
    //使用XML形式操作
    class SpinnerXMLSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            switch (arg2){
                case 0:
                    searchType="name";
                    break;
                case 1:
                    searchType="director";
                    break;
                case 2:
                    searchType="actor";
                    break;
            }
//            if(searchTypeStr=="片名")searchTypeStr="name";
//            if(searchTypeStr=="导演")searchTypeStr="director";
//            if(searchTypeStr=="主演")searchTypeStr="actor";
        }

        public void onNothingSelected(AdapterView<?> arg0) {

        }

    }
    private void initTypeList(){
        movieTypeList= (ListView) findViewById(R.id.typeList);
        new Thread() {
            @Override
            public void run() {
                String url = HOME_PAGE_URL;
                typeNumResponse = GetPostUtil.sendGetGbk(url, null);
                //这个有title
                Pattern p = Pattern.compile("<font color=#6a6969>\\((\\d+)\\)</font>");
                final Matcher m = p.matcher(typeNumResponse);
                //只要“分类”里的数字
                for (int i = 0; i < 18; i++) {
                    m.find();
                    typeNum[i]=m.group(1);
                }
                typeNum[18]="24";

                //发送消息通知ui线程更新UI组件
                handler.sendEmptyMessage(0x124);
            }

        }.start();


    }

    private void initMovieList(String type) {
        if (type != null) {
            try {
                movieType=URLEncoder.encode(type, "gbk");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        movieListData.clear();
        //创建一个List集合，list集合的元素是Map
        new Thread() {



            @Override
            public void run() {
                String url;
                String params;
                Intent intent=getIntent();
                Pattern p;
                int i = 0;
                if (movieType!=null){
                    //按类型
                    url=CATEGORY_URL;
                    params="page=0&class=type&content="+movieType;
                    response = GetPostUtil.sendGetGbk(url,params);

                    p = Pattern.compile("<a href=\"javascript:layer1On\\('(\\d+)'\\)\"><img border=\"0\" src=\"(.*)\" width=\"86\" height=\"121\"></a>");
                    final Matcher m = p.matcher(response);
                    while (m.find()) {
                        System.out.println(domain + m.group(1));
                        System.out.println(m.group(1));
                        final Map<String, Object> listItem = new HashMap<String, Object>();
                        listItem.put("posterimgId", m.group(1));
                        try {
                            listItem.put("movieType", URLDecoder.decode(movieType,"gbk"));
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
    private void initLocal() {
        localList= (ListView) findViewById(R.id.localList);
        List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
        GetFileList getFileListObj = new GetFileList(Environment.getExternalStorageDirectory() + "/" + LOCAL_DIR, false);
        ArrayList<String> fileList = getFileListObj.getFileArrayList();
        int len = fileList.size();
        for (int i = 0; i < len; i++) {
            Map<String, Object> listItem = new HashMap<String, Object>();
            listItem.put("img", R.mipmap.movie_play);
            listItem.put("name", fileList.get(i));
            listItems.add(listItem);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, listItems, R.layout.listview_cartoon_local, new String[]{"img", "name"}, new int[]{R.id.header, R.id.name});
        localList.setAdapter(simpleAdapter);
    }

    private void handMessage(){
        handler=new Handler(){
            //注意，桃源网站使用的是gb2312编码，所以提交、得到的文字都要转编码
            @Override
            public void handleMessage(Message msg) {

                if (msg.what==0x123){

                    AsyncMovieGridviewAdapter asyncmovieGridviewAdapter =new AsyncMovieGridviewAdapter(context,movieListData);
//                    simpleAdapter.setViewBinder(new MyViewBinder());
                    movieGridView.setAdapter(asyncmovieGridviewAdapter);
                    LinearLayout loading= (LinearLayout) findViewById(R.id.loadingAnim);
                    loading.setVisibility(View.GONE);
                    if (asyncmovieGridviewAdapter.getCount()==0){
                        Toast.makeText(context,"暂时没有搜到相关电影，换个词语试试吧~",Toast.LENGTH_LONG).show();
                    }

                }
                //更新typelist
                if (msg.what==0x124){
                    for (int i = 0; i < typeArr.length; i++) {
                        Map<String, Object> listItem = new HashMap<String, Object>();
                        listItem.put("type",typeArr[i]);
                        listItem.put("name", typeArrShow[i]);
                        listItem.put("desc", typeNum[typeMap[i]]+"Movies");
                        typeListData.add(listItem);
                    }
                    MovieTypeAdapter adapter=new MovieTypeAdapter(context,typeListData);
                    movieTypeList.setAdapter(adapter);
                }

            }
        };
    }
    private void init(){
        movieGridView= (GridView) findViewById(R.id.movieGridview);
        initTabHost();
        initSearchView();
        initTypeList();
        initMovieList(null);
        initLocal();
        handMessage();
        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GridView gridView = (GridView) parent;
                HashMap<String, Object> map = (HashMap<String, Object>) gridView.getItemAtPosition(position);
                Intent intent = new Intent();
                intent.setClass(context, MovieDescActivity.class);
                intent.putExtra("posterimgId", map.get("posterimgId").toString());
                intent.putExtra("movieName", map.get("movieName").toString());
                startActivity(intent);
            }
        });
        movieTypeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
                String type = map.get("type").toString() != "最新" ? map.get("type").toString() : null;
                initMovieList(type);
                //选中第一个tab
                tabHost.setCurrentTab(1);
                LinearLayout loading = (LinearLayout) findViewById(R.id.loadingAnim);
                loading.setVisibility(View.VISIBLE);
            }
        });
        localList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                String path = Environment.getExternalStorageDirectory() + "/" + LOCAL_DIR + "/" + map.get("fileName");
                intent.setDataAndType(Uri.fromFile(new File(path)), "video/*");
                startActivity(intent);
            }
        });
        this.registerForContextMenu(localList);
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
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v==localList){
            menu.add(0,20,2,"删除");
//            menu.add(0,21,3,"menu");
        }else{
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
        String title=map.get("name").toString();

        switch (item.getItemId()){
            case 20:
                String path=Environment.getExternalStorageDirectory()+"/"+LOCAL_DIR+"/"+title;
                Log.v("pcy", path);
                if (deleteFile(path)){
                    Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                    initLocal();
                }else{
                    Toast.makeText(this,"删除失败",Toast.LENGTH_SHORT).show();
                }

                break;
            case 21:
//                AlertDialog dialog = new AlertDialog.Builder(this)
//                        .setTitle("title").setMessage("message").create();
//                Window window = dialog.getWindow();
//                window.setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置
//                //window.setWindowAnimations(R.style.mystyle);  //添加动画
//                dialog.show();

                LinearLayout typeLayout= (LinearLayout) getLayoutInflater()
                        .inflate(R.layout.share_menu,null);
                AlertDialog dialog=new AlertDialog.Builder(this, R.style.Dialog_Fullscreen)
                        .setIcon(R.mipmap.ink)
                        .setTitle("电影类型")
                        .setView(typeLayout)
                        .create();
                dialog.show();


                //宽度全屏
                Window win = dialog.getWindow();
                win.setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置,底部显示
                win.getDecorView().setPadding(0, 0, 0, 0);
                WindowManager.LayoutParams lp = win.getAttributes();
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                win.setAttributes(lp);



                break;

        }
        return true;
    }
    public boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag=true;
        }
        return flag;
    }



}
