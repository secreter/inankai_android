package cn.redream.www.redream;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by acer on 2016/3/23.
 */
public class MusicPlayAdapter extends BaseAdapter {
    public static final int IS_PLAY=1;
    public static final int IS_PAUSE=2;
    public static final int NOT=0;
    RedreamApp redreamApp;
    List<Map<String,Object>> list ;
    Context context;
    Handler handler;
    //貌似Adapter在Activity调用onDestroy（）后还存在，不然为什么返回主页后再进来mSelect的值没有被置-1
    int mSelect = -1;   //选中项
    int isPlay = NOT;   //选中项
    public MusicPlayAdapter(Context context, List<Map<String, Object>> list){
        redreamApp = ((RedreamApp)context.getApplicationContext());
        this.list = list;
        this.context = context;
        mSelect=redreamApp.getCurPlayViewPos();
        isPlay=redreamApp.getMusicPlayState();
    }
    public void changeSelected(int positon,int isPlay){ //刷新方法
            mSelect = positon;
            this.isPlay=isPlay;
            notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//     if(convertView==null){
        LayoutInflater factory = LayoutInflater.from(context);
        View v = (View) factory.inflate(R.layout.listview_music_local, null);
        TextView name = (TextView) v.findViewById(R.id.name);
        TextView desc = (TextView) v.findViewById(R.id.desc);
        ImageView header= (ImageView) v.findViewById(R.id.header);
        ImageView playOrPause= (ImageView) v.findViewById(R.id.playOrPause);
//        tv.setText("test");
//     }
        name.setText(list.get(position).get("singName").toString());
        desc.setText(list.get(position).get("artist").toString());
        //设置异步加载完成前的默认图片，不设置安卓会使用第一屏的图片设置下面几屏，看的时候很混乱
        header.setImageResource(R.mipmap.defaultimg);

        if(mSelect==position){
            v.setBackgroundResource(R.color.eggplant);  //选中项背景
            switch (isPlay){
                case IS_PAUSE:
                    playOrPause.setImageResource(R.mipmap.button_pause);
                    break;
                case IS_PLAY:
                    playOrPause.setImageResource(R.mipmap.button_play);
                    break;
            }

        }else{
            v.setBackgroundResource(R.color.colorBlank);  //其他项背景
        }

        return v;
    }


}
