package cn.redream.www.redream;

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
public class AsyncCartoonListViewAdapter extends BaseAdapter {
//    private String domain="http://12club.nankai.edu.cn";
    private String domain="http://222.30.60.30";
    private static final int MSG_UPDATE_POSTER =0x130 ;
    List<Map<String,Object>> list ;
    Context context;
    Handler handler;
    Map<String,Bitmap> moviePosterMap=new HashMap<String,Bitmap>();
    public AsyncCartoonListViewAdapter(Context context, List<Map<String, Object>> list){
        this.list = list;
        this.context = context;
        this.handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==MSG_UPDATE_POSTER){
                    Map<String,Object> keyAndPoster= (Map<String, Object>) msg.obj;
                    ImageView moviePoster=(ImageView) keyAndPoster.get("imageView");
                    String imgPath= (String)keyAndPoster.get("imgPath");
                    moviePoster.setImageBitmap(moviePosterMap.get(imgPath));
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
        TextView cartoonName;
        TextView cartoonLatest;
        TextView cartoonTime;
        TextView downloadCount;
        ImageView cartoonPoster;
        String imgPath;

        System.out.println("调用getView方法，显示position=" + position + "项");
        if(convertView!=null){
            cartoonName = (TextView) convertView.findViewById(R.id.name);
            cartoonLatest = (TextView) convertView.findViewById(R.id.latest);
            cartoonTime = (TextView) convertView.findViewById(R.id.time);
            downloadCount = (TextView) convertView.findViewById(R.id.downloadCount);
            cartoonPoster= (ImageView) convertView.findViewById(R.id.cartoonPoster);
        }else{
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView =  layoutInflater.inflate(R.layout.listview_cartoon, null);
            cartoonName = (TextView)(convertView.findViewById(R.id.name));
            cartoonLatest = (TextView) convertView.findViewById(R.id.latest);
            cartoonTime = (TextView) convertView.findViewById(R.id.time);
            downloadCount = (TextView) convertView.findViewById(R.id.downloadCount);
            cartoonPoster= (ImageView) convertView.findViewById(R.id.cartoonPoster);
        }

        cartoonName.setText(list.get(position).get("name").toString());
        cartoonLatest.setText(list.get(position).get("latest").toString());
        cartoonTime.setText(list.get(position).get("time").toString());
        downloadCount.setText(list.get(position).get("downloadCount").toString());
        //设置异步加载完成前的默认图片，不设置安卓会使用第一屏的图片设置下面几屏，看的时候很混乱
        cartoonPoster.setImageResource(R.mipmap.movie_loading);
        imgPath=list.get(position).get("imgLink").toString();
        asynicPoster(cartoonPoster, imgPath);
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
    private void asynicPoster(final ImageView moviePoster,  final String imgPath){
        final String imgUrl=domain+imgPath;
        final Message msg = new Message();
        Map<String,Object> keyAndPoster=new HashMap<String,Object>();
        keyAndPoster.put("imageView",moviePoster);
        keyAndPoster.put("imgPath",imgPath);
        msg.obj=keyAndPoster;
        msg.what=MSG_UPDATE_POSTER;
        if (moviePosterMap.containsKey(imgPath)){
            Log.v("pcy:","containsKey:"+imgPath);
            moviePoster.setImageBitmap(moviePosterMap.get(imgPath));
        }else{
            Log.v("pcy:","NOtContainsKey:"+imgPath);
            new Thread(){
                @Override
                public void run() {
                    moviePosterMap.put(imgPath, getBitmap(imgUrl,200,280));
//                    moviePoster.setImageBitmap(moviePosterMap.get(posterId));
                    handler.sendMessage(msg);
                }
            }.start();
        }
    }
    //获取网络图片资源，返回类型是Bitmap，用于设置在ListView中
    public Bitmap getBitmap(String httpUrl,int reqWidth,int reqHeight){
        Bitmap bmp = null;
        Bitmap thumbBmp=null;
        //ListView中获取网络图片
        try {
            URL url = new URL(httpUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            InputStream is = conn.getInputStream();
//            InputStream is = url.openStream();
            // 给定的BitmapFactory设置解码的参数
//            final BitmapFactory.Options options = new BitmapFactory.Options();
            // 从解码器中获取原始图片的宽高，这样避免了直接申请内存空间
//            options.inJustDecodeBounds = true;
            bmp = BitmapFactory.decodeStream(is);
//
            thumbBmp=Bitmap.createScaledBitmap(bmp, reqWidth, reqHeight, true);
            //释放图像占用的资源
            bmp.recycle();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return thumbBmp;
    }
}
