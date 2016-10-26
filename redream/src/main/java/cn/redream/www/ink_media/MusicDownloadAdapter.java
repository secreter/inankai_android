package cn.redream.www.ink_media;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by acer on 2016/3/23.
 */
public class MusicDownloadAdapter extends BaseAdapter {
    List<Map<String,Object>> list ;
    Context context;
    Handler handler;
    List<String> download_id_list=new ArrayList<>();
    Map<String,Object> map=new HashMap<>();
    public MusicDownloadAdapter(Context context, List<Map<String, Object>> list){
        this.list = list;
        this.context = context;
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
        CircleImageView header;
        TextView name;
        TextView desc;
        ImageView downloadIcon;

        System.out.println("调用getView方法，显示position=" + position + "项");
        if(convertView!=null){
            header= (CircleImageView) convertView.findViewById(R.id.header);
            name = (TextView) convertView.findViewById(R.id.name);
            desc = (TextView) convertView.findViewById(R.id.desc);
            downloadIcon= (ImageView) convertView.findViewById(R.id.downloadIcon);
        }else{
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView =  layoutInflater.inflate(R.layout.listview_song, null);
            header= (CircleImageView) convertView.findViewById(R.id.header);
            name = (TextView)(convertView.findViewById(R.id.name));
            desc = (TextView) convertView.findViewById(R.id.desc);
            downloadIcon= (ImageView) convertView.findViewById(R.id.downloadIcon);
        }

        name.setText(list.get(position).get("singName").toString());
        desc.setText(list.get(position).get("artist").toString());
        header.setImageResource(R.mipmap.songs);
        map= (Map<String, Object>) getItem(position);
        if (!download_id_list.contains(map.get("id"))){
            downloadIcon.setImageResource(R.mipmap.icon_download_normal);
        }else{
            downloadIcon.setImageResource(R.mipmap.icon_download_active);
        }

        return convertView;
    }
    public void addDownloadId(String str){
        download_id_list.add(str);
    }


}