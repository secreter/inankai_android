package cn.redream.www.redream;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by acer on 2016/3/20.
 */
public class MovieDownloadListViewAdapter extends BaseAdapter {
    List<Map<String,String>> list ;
    Context context;
    public MovieDownloadListViewAdapter(Context context,List<Map<String,String>> list){
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
        Button download;
        Button play;
        Button share;
        if(convertView!=null){
            play= (Button) convertView.findViewById(R.id.moviePlay);
            download = (Button) convertView.findViewById(R.id.movieDownload);
            share = (Button) convertView.findViewById(R.id.movieShare);
            System.out.println(share.getText());
        }else{
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView =  layoutInflater.inflate(R.layout.listview_download_movie, null);
            play= (Button) convertView.findViewById(R.id.moviePlay);
            download = (Button) convertView.findViewById(R.id.movieDownload);
            share = (Button) convertView.findViewById(R.id.movieShare);
        }
        download.setTag(list.get(position).get("downloadLink"));

        download.setText(list.get(position).get("download"));
        play.setTag(list.get(position).get("downloadLink"));
        play.setText(list.get(position).get("play"));
        share.setTag(list.get(position).get("downloadLink"));


        return convertView;
    }
}
