package cn.redream.www.ink_media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by acer on 2016/3/19.
 */
public class AsyncMovieListViewAdapter extends BaseAdapter {
    public static final String SMALL_PIC_URL="http://222.30.44.37/posterimgs/small/";
    private static final int MSG_UPDATE_POSTER =0x130 ;
    List<Map<String,Object>> list ;
    Context context;
    Handler handler;
    Map<String,Bitmap> moviePosterMap=new HashMap<String,Bitmap>();
    public AsyncMovieListViewAdapter(Context context, List<Map<String, Object>> list){
        this.list = list;
        this.context = context;
        this.handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==MSG_UPDATE_POSTER){
                    ImageView moviePoster;
                    moviePoster= (ImageView) msg.obj;
                    Log.v("pcy:", "msg posterId:" + msg.arg1);
                    moviePoster.setImageBitmap(moviePosterMap.get(String.valueOf(msg.arg1)));
                    Log.v("pcy:", "msg posterId:" + msg.arg1);
                }
            }
        };
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
        TextView movieName;
        TextView movieType;
        TextView movieDesc;
        ImageView moviePoster;

        System.out.println("调用getView方法，显示position="+position+"项");
        if(convertView!=null){
            movieName = (TextView) convertView.findViewById(R.id.movieName);
            movieType = (TextView) convertView.findViewById(R.id.movieType);
            movieDesc = (TextView) convertView.findViewById(R.id.movieDesc);
            moviePoster= (ImageView) convertView.findViewById(R.id.moviePoster);
            System.out.println(movieName.getText());
        }else{
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView =  layoutInflater.inflate(R.layout.listview_movie, null);
            movieName = (TextView)(convertView.findViewById(R.id.movieName));
            movieType = (TextView) convertView.findViewById(R.id.movieType);
            movieDesc = (TextView) convertView.findViewById(R.id.movieDesc);
            moviePoster= (ImageView) convertView.findViewById(R.id.moviePoster);
        }
        if (list.get(position).get("movieName")!=null){
            movieName.setText(list.get(position).get("movieName").toString());
        }
        if (list.get(position).get("movieType")!=null){
            movieType.setText(list.get(position).get("movieType").toString());
        }
        if (list.get(position).get("movieDesc")!=null){
            movieDesc.setText(list.get(position).get("movieDesc").toString());
        }



        //设置异步加载完成前的默认图片，不设置安卓会使用第一屏的图片设置下面几屏，看的时候很混乱
        moviePoster.setImageResource(R.mipmap.movie_loading);
        asynicPoster(moviePoster,  list.get(position).get("posterimgId").toString());
//        if(position%2==0)
//        {
//            movieName.setBackgroundColor(Color.WHITE);
//        }
//        else{
//            movieName.setBackgroundColor(Color.GRAY);
//        }
        return convertView;
    }
    //异步加载并缓存
    private void asynicPoster(final ImageView moviePoster,  final String posterId){
        final Message msg = new Message();
        msg.obj=moviePoster;
        msg.arg1=Integer.parseInt(posterId);
        msg.what=MSG_UPDATE_POSTER;
        final String id=posterId;
        if (moviePosterMap.containsKey(posterId)){
            Log.v("pcy:","containsKey:"+posterId);
            moviePoster.setImageBitmap(moviePosterMap.get(posterId));
        }else{
            Log.v("pcy:","NOtContainsKey:"+posterId);
            final String posterLink=SMALL_PIC_URL+posterId+".jpg";
            new Thread(){
                @Override
                public void run() {
                    moviePosterMap.put(id + "", getBitmap(posterLink));
//                    moviePoster.setImageBitmap(moviePosterMap.get(posterId));
                    handler.sendMessage(msg);
                }
            }.start();
        }
    }
    //获取网络图片资源，返回类型是Bitmap，用于设置在ListView中
    public Bitmap getBitmap(String httpUrl){
        Bitmap bmp = null;
        try {
            URL url = new URL(httpUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            InputStream is = conn.getInputStream();
            bmp = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bmp;
    }
}
