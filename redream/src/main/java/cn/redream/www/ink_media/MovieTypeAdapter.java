package cn.redream.www.ink_media;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by acer on 2016/3/23.
 */
public class MovieTypeAdapter extends BaseAdapter {
    List<Map<String,Object>> list ;
    Context context;
    Handler handler;
    public MovieTypeAdapter(Context context, List<Map<String, Object>> list){
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
        TextView typeName;
        TextView movieNum;
        ImageView arrow;
        LinearLayout linearLayout;

        System.out.println("调用getView方法，显示position=" + position + "项");
        if(convertView!=null){
            typeName = (TextView) convertView.findViewById(R.id.name);
            movieNum = (TextView) convertView.findViewById(R.id.desc);
            arrow= (ImageView) convertView.findViewById(R.id.arrow);
            linearLayout= (LinearLayout) convertView.findViewById(R.id.textwrap);
        }else{
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView =  layoutInflater.inflate(R.layout.listview_movie_type, null);
            typeName = (TextView)(convertView.findViewById(R.id.name));
            movieNum = (TextView) convertView.findViewById(R.id.desc);
            arrow= (ImageView) convertView.findViewById(R.id.arrow);
            linearLayout= (LinearLayout) convertView.findViewById(R.id.textwrap);
        }

        typeName.setText(list.get(position).get("name").toString());
        movieNum.setText(list.get(position).get("desc").toString());
        arrow.setImageResource(R.mipmap.arrow);


        return convertView;
    }


}
