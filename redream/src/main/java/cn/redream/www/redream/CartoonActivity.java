package cn.redream.www.redream;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CartoonActivity extends AppCompatActivity implements TabHost.OnTabChangeListener,NavigationView.OnNavigationItemSelectedListener {
    static final String CARTOON_URL = "http://12club.nankai.edu.cn/programs";



    static final String VIDEO_URL = "http://12club.nankai.edu.cn/programs";
    static final String SEARCH_URL = "http://12club.nankai.edu.cn/search";
    static final String LOCAL_DIR = "inankai/comic";
    TabHost tabHost;
    String responseCartoon;
    String responseVideo;
    String responseSearch;
    Context context;
    Handler handler;
    WXShareUtil share;
    private String domain = "http://12club.nankai.edu.cn";
    private SearchView searchCartoon;
    private SearchView searchVideo;
    private ListView cartoonList;
    private ListView videoList;
    private ListView localList;
    private List<Map<String, Object>> cartoonListItems = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> videoListItems = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> localListItems = new ArrayList<Map<String, Object>>();
    private Spinner searchType;
    private android.support.v7.widget.SearchView movieSearch;
    private String searchTypeStr="name";  //默认搜索片名
    private ArrayAdapter adapterType;
    private MenuItem thisActMenuItem;//
    private NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cartoon);

        init();
        //为导航栏注册监听事件
        this.navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        cartoonList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToDescIntent(parent, view, position, id);
            }
        });
        videoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToDescIntent(parent, view, position, id);
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
        //设置放大镜的图标为黑色
        int search_mag_icon_id = searchCartoon.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView  search_mag_icon = (ImageView)searchCartoon.findViewById(search_mag_icon_id);//获取搜索图标
//        ImageView search_button = (ImageView) searchCartoon.findViewById(android.R.id.search_button);
        search_mag_icon.setImageResource(R.mipmap.search3);//图标都是用src的
        //将其展开后改变图标才有用
        searchCartoon.setIconifiedByDefault(false);
        //初始时失去焦点
        searchCartoon.setFocusable(false);
        //设置字体为黑色
        int id = searchCartoon.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchCartoon.findViewById(id);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(14);
        textView.setHintTextColor(getResources().getColor(R.color.grey));
        searchCartoon.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final String params = "utf8=✓&authenticity_token=/HRSxXrHqZsNVwHWJ6Ou/++URxIfKIS5MFyx1Qkz69E=&keyword=" + query;
                new Thread() {
                    @Override
                    public void run() {
                        responseSearch = GetPostUtil.sendPost(SEARCH_URL, params);
                        Pattern p = Pattern.compile("<a class='pic_link' href='(.*)'[\\s\\S]+?data-original='(.+)'[\\s\\S]+?<div class='title_box'>[\\s\\S]+?\">(.+)</a>[\\s\\S]+?<div class='lastest_update'>[\\s\\S]+?\">(.+)</a>[\\s\\S]+?<div class='update_date'>(.+)</div>[\\s\\S]+?<div class='rank_value'>(.+)</div>");
                        final Matcher m = p.matcher(responseSearch);
                        cartoonListItems.clear();
                        while (m.find()) {
                            System.out.println(domain + m.group(1));
                            System.out.println(m.group(1));
                            final Map<String, Object> listItem = new HashMap<String, Object>();
                            listItem.put("descLink", m.group(1));
                            listItem.put("imgLink", m.group(2));
                            listItem.put("name", m.group(3));
                            listItem.put("latest", "最近更新" + m.group(4));
                            listItem.put("time", m.group(5));
                            listItem.put("downloadCount", m.group(6));
                            cartoonListItems.add(listItem);
                        }
                        //发送消息通知ui线程更新UI组件
                        handler.sendEmptyMessage(0x123);
                    }
                }.start();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    //用于菜单的同步
    @Override
    protected void onResume() {
        super.onResume();
        //起始化进入该页面时设置其本身被选中
        thisActMenuItem= navigationView.getMenu().getItem(3);
        thisActMenuItem.setChecked(true); // 改变item选中状态

    }

    public void goToDescIntent(AdapterView<?> parent, View view, int position, long id) {
        //传递参数
        ListView listView = (ListView) parent;
        HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
        //传递复杂些的参数 解决方法是：外层套个List
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        list.add(map);
        Intent intent = new Intent();
        intent.setClass(context, CartoonDescActivity.class);
        intent.putExtra("descLink", map.get("descLink").toString());
        intent.putExtra("name", map.get("name").toString());
        intent.putExtra("time", map.get("time").toString());
        intent.putExtra("latest", map.get("latest").toString());
        intent.putExtra("downloadCount", map.get("downloadCount").toString());
        intent.putExtra("imgLink", map.get("imgLink").toString());
        startActivity(intent);
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
                .setIndicator("动漫") //创建标题
                .setContent(R.id.tab1);//创建内容
        //添加第一个标签页
        tabHost.addTab(tab1);
        TabHost.TabSpec tab2 = tabHost.newTabSpec("tab2")
                .setIndicator("剧集") //创建标题
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

    private void initCartoon() {
        //创建一个List集合，list集合的元素是Map
        new Thread() {
            @Override
            public void run() {
                responseCartoon = GetPostUtil.sendGet(CARTOON_URL, "category_id=1");
                Pattern p = Pattern.compile("<a class='pic_link' href='(.*)'[\\s\\S]+?data-original='(.+)'[\\s\\S]+?<div class='title_box'>[\\s\\S]+?\">(.+)</a>[\\s\\S]+?<div class='lastest_update'>[\\s\\S]+?\">(.+)</a>[\\s\\S]+?<div class='update_date'>(.+)</div>[\\s\\S]+?<div class='rank_value'>(.+)</div>");
                final Matcher m = p.matcher(responseCartoon);
                int i = 0;
                while (m.find()) {
                    System.out.println(domain + m.group(1));
                    System.out.println(m.group(1));
                    final Map<String, Object> listItem = new HashMap<String, Object>();
                    listItem.put("descLink", m.group(1));
                    listItem.put("imgLink", m.group(2));
                    listItem.put("name", m.group(3));
                    listItem.put("latest", "最近更新" + m.group(4));
                    listItem.put("time", m.group(5));
                    listItem.put("downloadCount", m.group(6));
                    cartoonListItems.add(listItem);
                }
                //发送消息通知ui线程更新UI组件
                handler.sendEmptyMessage(0x123);
            }
        }.start();


    }

    private void initVideo() {
        new Thread() {
            @Override
            public void run() {
                responseVideo = GetPostUtil.sendGet(CARTOON_URL, "category_id=6");
                Pattern p = Pattern.compile("<a class='pic_link' href='(.*)'[\\s\\S]+?data-original='(.+)'[\\s\\S]+?<div class='title_box'>[\\s\\S]+?\">(.+)</a>[\\s\\S]+?<div class='lastest_update'>[\\s\\S]+?\">(.+)</a>[\\s\\S]+?<div class='update_date'>(.+)</div>[\\s\\S]+?<div class='rank_value'>(.+)</div>");
                final Matcher m = p.matcher(responseVideo);
                int i = 0;
                while (m.find()) {
                    System.out.println(domain + m.group(1));
                    System.out.println(m.group(1));
                    final Map<String, Object> listItem = new HashMap<String, Object>();
                    listItem.put("descLink", m.group(1));
                    listItem.put("imgLink", m.group(2));
                    listItem.put("name", m.group(3));
                    listItem.put("latest", "最近更新" + m.group(4));
                    listItem.put("time", m.group(5));
                    listItem.put("downloadCount", m.group(6));
                    videoListItems.add(listItem);
                }
                //发送消息通知ui线程更新UI组件
                handler.sendEmptyMessage(0x126);
            }
        }.start();


    }

    private void initLocal() {
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

    private void init() {
        context = this;
        cartoonList = (ListView) findViewById(R.id.cartoonList);
        videoList = (ListView) findViewById(R.id.videoList);
        localList = (ListView) findViewById(R.id.localList);
        searchCartoon = (SearchView) findViewById(R.id.searchCartoon);
        share = new WXShareUtil(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //注册导航栏
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        initTabHost();
        initCartoon();
        initVideo();
        initLocal();
        handMessage();
        this.registerForContextMenu(cartoonList);
        this.registerForContextMenu(videoList);
        this.registerForContextMenu(localList);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v == localList) {
            menu.add(0, 20, 2, "删除");
        } else {
            menu.add(0, 11, 2, "分享到朋友圈");
            menu.add(0, 12, 3, "分享给微信好友");
        }


    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo;
        menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = menuInfo.position;
        //注意，这里targetView是linearlayout，要getParent()才是ListView
        ListView listView = (ListView) menuInfo.targetView.getParent();
        final HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(index);
        final String url = "http://www.redream.cn/testpaper.php";
        String title = map.get("name").toString();
        String description = "我正在看南开内网电影哦~你也来看看吧!";
        ImageView view;
        Bitmap bmp;
        switch (item.getItemId()) {

            case 11:
                //getChildAt 注意，这里传入的是可见区域的索引，不是全部
                view = (ImageView) listView.getChildAt(index - listView.getFirstVisiblePosition()).findViewById(R.id.cartoonPoster);
                view.setDrawingCacheEnabled(true);
                view.buildDrawingCache();  //启用DrawingCache并创建位图
//        Matrix matrix = new Matrix();//缩放,太大了微信限制
                bmp = scaleBitmap(view.getDrawingCache(), 50, 80); //创建一个DrawingCache的拷贝，因为DrawingCache得到的位图在禁用后会被回收
                view.setDrawingCacheEnabled(false);  //禁用DrawingCahce否则会影响性能
                share.sendUrl(url, true, title, description, bmp);
                break;
            case 12:
                view = (ImageView) listView.getChildAt(index - listView.getFirstVisiblePosition()).findViewById(R.id.cartoonPoster);
                view.setDrawingCacheEnabled(true);
                view.buildDrawingCache();  //启用DrawingCache并创建位图
                bmp = scaleBitmap(view.getDrawingCache(), 50, 80); //创建一个DrawingCache的拷贝，因为DrawingCache得到的位图在禁用后会被回收
                view.setDrawingCacheEnabled(false);  //禁用DrawingCahce否则会影响性能
                share.sendUrl(url, false, title, description, bmp);
                break;
            case 20:
                String path = Environment.getExternalStorageDirectory() + "/" + LOCAL_DIR + "/" + title;
                Log.v("pcy", path);
                if (deleteFile(path)) {
                    Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                    initLocal();
                } else {
                    Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                }

                break;

        }
        return true;
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

    //获取网络图片资源，返回类型是Bitmap，用于设置在ListView中
    public Bitmap getBitmap(String httpUrl, int reqWidth, int reqHeight) {
        Bitmap bmp = null;
        Bitmap thumbBmp = null;
        //ListView中获取网络图片
        try {
            URL url = new URL(httpUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStream is = conn.getInputStream();
//            InputStream is = url.openStream();
            // 给定的BitmapFactory设置解码的参数
//            final BitmapFactory.Options options = new BitmapFactory.Options();
            // 从解码器中获取原始图片的宽高，这样避免了直接申请内存空间
//            options.inJustDecodeBounds = true;
            bmp = BitmapFactory.decodeStream(is);
//
            thumbBmp = Bitmap.createScaledBitmap(bmp, reqWidth, reqHeight, true);
            //释放图像占用的资源
            bmp.recycle();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return thumbBmp;
    }

    public synchronized static Bitmap scaleBitmap(Bitmap bitmap, float w, float h) {
        if (bitmap == null) {//判断Bitmap
            return null;
        }
        int width = bitmap.getWidth(); //获取宽度
        int height = bitmap.getHeight();//获取高度
        Matrix matrix = new Matrix(); //实例化一个Martrix对象
        float scaleW = w / (float) width;
        float scaleH = h / (float) height;
        matrix.postScale(scaleW, scaleH);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }

    private void handMessage() {
        handler = new Handler() {
            //注意，桃源网站使用的是gb2312编码，所以提交、得到的文字都要转编码
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == 0x123) {

                    AsyncCartoonListViewAdapter adapter = new AsyncCartoonListViewAdapter(context, cartoonListItems);
                    cartoonList.setAdapter(adapter);

                }
                if (msg.what == 0x124) {
//                Pattern p= Pattern.compile("<font color=#666666>(.+)</font>[\\s\\S]+<a href=\"waitorder.+id=(\\d+).+><font color=green>(.+)</font></a>");
//                Matcher m=p.matcher(responseCartoon);
//                int i=0;
//                while(m.find()){
//                    System.out.println(m.group(1));
//                    System.out.println(m.group(2));
//                    System.out.println(m.group(3));
//                    Map<String,Object> listItem=new HashMap<String,Object>();
//                    listItem.put("header",R.mipmap.chenyixun);
//                    listItem.put("personName",m.group(3));
//                    listItem.put("desc",m.group(1));
//                    listItem.put("id",m.group(2));
//                    videoListItems.add(listItem);
//                }
//                SimpleAdapter simpleAdapter=new SimpleAdapter(context,videoListItems,R.tab_focused.listview_cartoon,new String[]{"header","personName","desc"},new int[]{R.id.header,R.id.name,R.id.desc});
//                videoList.setAdapter(simpleAdapter);
                }
                if (msg.what == 0x125) {
                    Toast.makeText(context, "下载成功！", Toast.LENGTH_SHORT).show();
                }
                if (msg.what == 0x126) {

                    AsyncCartoonListViewAdapter adapter = new AsyncCartoonListViewAdapter(context, videoListItems);
                    videoList.setAdapter(adapter);

                }
            }
        };
    }

    public boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

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

        String title = "redream";
        String desc = "ipv6电视、光影传奇、十二社区、桃源音乐，在南开用Redream就够了，不走流量哦~";
        String url = "http://www.redream.cn/main/ipv6tv.php";
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.redream);

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
//            movieTpye();
            intent = new Intent(this, MovieActivity.class);
            startActivity(intent);
        } else if (id == R.id.cartoon) {

        } else if (id == R.id.music) {
            intent = new Intent(this, MusicTabActivity.class);
            startActivity(intent);
        } else if (id == R.id.testpaper) {
            intent = new Intent(this, TestpaperActivity.class);
            startActivity(intent);
        } else if (id == R.id.treehole) {
            intent = new Intent(this, TreeholeActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            String title = "ipv6电视、光影传奇、十二社区、桃源音乐，在南开用Redream就够了，不走流量哦~";
            String desc = "一款南开必备神器，墙裂推荐！";
            String url = "http://www.redream.cn/main/ipv6tv.php";
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.redream);
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
                .setIcon(R.mipmap.redream)
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

