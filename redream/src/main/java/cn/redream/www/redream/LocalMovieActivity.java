package cn.redream.www.redream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalMovieActivity extends AppCompatActivity {
    static final String LOCAL_DIR="Redream/movie";
    private ListView localList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_movie);

        init();
    }
    private void initLocal(){
        List<Map<String,Object>> listItems=new ArrayList<Map<String,Object>>();
        GetFileList getFileListObj=new GetFileList(Environment.getExternalStorageDirectory()+"/"+LOCAL_DIR,false);
        ArrayList<String> fileList=getFileListObj.getFileArrayList();
        int len=fileList.size();
        for (int i=0;i<len;i++){
            Map<String,Object> listItem=new HashMap<String,Object>();
            listItem.put("img",R.mipmap.movie_play);
            listItem.put("name",fileList.get(i));
            listItems.add(listItem);
        }
        SimpleAdapter simpleAdapter=new SimpleAdapter(this,listItems,R.layout.listview_cartoon_local,new String[]{"img","name"},new int[]{R.id.header,R.id.name});
        localList.setAdapter(simpleAdapter);
    }
    private void init(){
        localList= (ListView) findViewById(R.id.localList);
        initLocal();
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
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v==localList){
            menu.add(0,20,2,"删除");
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
