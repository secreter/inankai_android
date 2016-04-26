package cn.redream.www.redream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXEmojiObject;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMusicObject;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by acer on 2016/3/15.
 */
public class WXShareUtil {
    private String apppId="wx2310d9e8ca8e2f1d";
    private IWXAPI api;
    final boolean PYQ=true;
    final boolean FRIEND=false;

    private Context context;
    //将apppid注册到微信里
    public WXShareUtil(Context context){
        api=WXAPIFactory.createWXAPI(context, apppId);
        api.registerApp(apppId);
        this.context=context;
    }

    //启动微信
    public void launchWeixin(){
        api.openWXApp();
    }
    //向好友或朋友圈发送文本
    public void shareText(String title, final boolean isPyq){
        //创建EditText用于输入文本
        final EditText editText=new EditText(context);
        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setText("iclothes");
        final AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("iclothes文本");
        //将EditText与对话框绑定
        builder.setView(editText);
        builder.setMessage("要分享的文本");
        builder.setPositiveButton("分享", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = editText.getText().toString();
                if (text == null || text.length() == 0) {
                    return;
                }
                //创建一个用于封装待分享文本的WXTextObject对象
                WXTextObject wxTextObject = new WXTextObject();
                wxTextObject.text = text;
                //创建WXMediMessage对象，用于android客户端向微信发送数据
                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = wxTextObject;
                msg.description = text;
                //创建一个用于请求微信客户端的SendMessageToWX.Req对象
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.message = msg;
                //设置请求唯一标识
                req.transaction = buildTransaction("text");
                //表示发送给朋友还是朋友圈
                req.scene = isPyq ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
                //发送给客户端
                api.sendReq(req);
            }
        });
        builder.setNegativeButton("取消",null);
        final AlertDialog alertDialog=builder.create();
        alertDialog.show();

    }
    //为请求生成一个唯一标识
    private  String buildTransaction(final String type){
        return (type==null)?String.valueOf(System.currentTimeMillis()):type+System.currentTimeMillis();
    }
    //发送二进制格式图像
    public void sendBinaryImg(int imgId,boolean isPyq){
        //1.获取bitmap图像对象
        Bitmap bitmap= BitmapFactory.decodeResource(context.getResources(), imgId);
        //2.创建WXImageObject对象并包装bitmap
        WXImageObject imgObj=new WXImageObject(bitmap);
        //3.创建WXMediMessage对象并包装对象WXImageObject
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        //4.压缩图像
        Bitmap thumbBmp=Bitmap.createScaledBitmap(bitmap,120,150,true);
        //释放图像占用的资源
        bitmap.recycle();
        msg.thumbData=bmpToByteArray(thumbBmp,true); //设置缩略图
        //5.创建一个用于请求微信客户端的SendMessageToWX.Req对象
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.message = msg;
        //设置请求唯一标识
        req.transaction = buildTransaction("img");
        req.scene = isPyq ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        //发送给客户端
        api.sendReq(req);

    }
    //将bitmap转换为byte数组
    private byte[] bmpToByteArray(final Bitmap bitmap,final Boolean needRecycle){
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        if (needRecycle){
            bitmap.recycle();
        }
        byte[] result=outputStream.toByteArray();
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    //发送本地图像
    public void sendLocalImg(String path,boolean isPyq){
        //1.判断图像是否存在

        File file=new File(path);
        if (!file.exists()){
            Toast.makeText(context, path + "文件不存在！", Toast.LENGTH_LONG).show();
            return;
        }
        //2.创建WXImageObject对象并包装bitmap
        WXImageObject imgObj=new WXImageObject();
        //设置图像文件路径
        imgObj.setImagePath(path);
        //3.创建WXMediMessage对象并包装对象WXImageObject
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        //4.压缩图像
        Bitmap bitmap=BitmapFactory.decodeFile(path);
        Bitmap thumbBmp=Bitmap.createScaledBitmap(bitmap,120,150,true);
        //释放图像占用的资源
        bitmap.recycle();
        msg.thumbData=bmpToByteArray(thumbBmp,true); //设置缩略图
        //5.创建一个用于请求微信客户端的SendMessageToWX.Req对象
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.message = msg;
        //设置请求唯一标识
        req.transaction = buildTransaction("img");
        req.scene = isPyq ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        //发送给客户端
        api.sendReq(req);
    }
    //发送url图片
    public void sendUrlImg (final String url, final boolean isPyq){
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                //String url="http://e.hiphotos.baidu.com/image/h%3D200/sign=2c672a01adc3793162688129dbc5b784/09fa513d269759ee69fa88ccb0fb43166d22df0b.jpg";
                //2.创建WXImageObject对象并包装bitmap
                WXImageObject imgObj=new WXImageObject();
                //设置图像文件路径
                //为什么我的这个里面没有这个方法
//                imgObj.imageUrl=url;
                imgObj.imagePath=url;
                //3.创建WXMediMessage对象并包装对象WXImageObject
                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = imgObj;
                //4.压缩图像
                Bitmap bitmap= null;
                try {
                    bitmap = BitmapFactory.decodeStream(new URL(url).openStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap thumbBmp=Bitmap.createScaledBitmap(bitmap,120,150,true);
                //释放图像占用的资源
                bitmap.recycle();
                msg.thumbData=bmpToByteArray(thumbBmp,true); //设置缩略图
                //5.创建一个用于请求微信客户端的SendMessageToWX.Req对象
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.message = msg;
                //设置请求唯一标识
                req.transaction = buildTransaction("img");
                req.scene = isPyq ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
                //发送给客户端
                api.sendReq(req);
            }
        });
        thread.start();
    }
    //发送音频
    public void sendUrlAudio (String url,boolean isPyq,String title,String description,int imgId){
        //1.创建一个WXMusicObject对象,用来指定音乐url
        WXMusicObject wxMusicObject=new WXMusicObject();
       // wxMusicObject.musicUrl="http://music.nankai.edu.cn/download.php?id=58481";
        wxMusicObject.musicUrl=url;
        //2.创建对象
        WXMediaMessage msg=new WXMediaMessage();
        msg.mediaObject=wxMusicObject;
        msg.title=title;
        msg.description=description;
        //3.设置缩略图
        Bitmap bitmap= BitmapFactory.decodeResource(context.getResources(),imgId);
        msg.thumbData=bmpToByteArray(bitmap,true);
        //4.创建SendMessageToWX.Req对象
        SendMessageToWX.Req req=new SendMessageToWX.Req();
        req.transaction=buildTransaction("music");
        req.message=msg;
        req.scene = isPyq ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        //发送给客户端
        api.sendReq(req);
    }

    //sendUrlVideo发送url视频
    public void sendUrlVideo (String url,boolean isPyq,String title,String description,int imgId){
        WXVideoObject videoObject=new WXVideoObject();
        //videoObject.videoUrl="http://v.youku.com/v_show/id_XMTQ5OTU1Njc3Mg==.html";
        videoObject.videoUrl=url;
        //2.创建对象
        WXMediaMessage msg=new WXMediaMessage();
        msg.mediaObject=videoObject;
        msg.title=title;
        msg.description=description;
        //3.设置缩略图
        Bitmap bitmap= BitmapFactory.decodeResource(context.getResources(),imgId);
        msg.thumbData=bmpToByteArray(bitmap,true);
        //4.创建SendMessageToWX.Req对象
        SendMessageToWX.Req req=new SendMessageToWX.Req();
        req.transaction=buildTransaction("video");
        req.message=msg;
        req.scene = isPyq ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        //发送给客户端
        api.sendReq(req);
    }
    //发送url
    public void sendUrl(String url,boolean isPyq,String title,String description,Bitmap bitmap){
        //1.创建一个WXWebpageObject对象封装url
        WXWebpageObject webObj=new WXWebpageObject();
        webObj.webpageUrl=url;
        //2.创建对象
        WXMediaMessage msg=new WXMediaMessage(webObj);
        //msg.mediaObject=webObj;
        msg.title=title;
        msg.description=description;
        //3.设置缩略图
//        Bitmap bitmap= BitmapFactory.decodeResource(context.getResources(),R.mipmap.zhoujielun);
        msg.thumbData=bmpToByteArray(bitmap,true);
        //4.创建SendMessageToWX.Req对象
        SendMessageToWX.Req req=new SendMessageToWX.Req();
        req.transaction=buildTransaction("webpage");
        req.message=msg;
        req.scene = isPyq ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        //发送给客户端
        api.sendReq(req);

    }
    //分享表情sendEmotion
    public void sendEmotion(String path,boolean isPyq){
        //1.创建一个WXEmojiObject对象
        WXEmojiObject ojiObj=new WXEmojiObject();
//        ojiObj.emojiPath="/sdcard/iclothes/1.jpg";//gif
        //2.创建对象
        ojiObj.emojiPath=path;
        WXMediaMessage msg=new WXMediaMessage(ojiObj);
        //msg.mediaObject=webObj;
        msg.title="Redream";
        msg.description="Redream";
        //3.设置缩略图
        Bitmap bitmap= BitmapFactory.decodeResource(context.getResources(),R.mipmap.zhoujielun);
        msg.thumbData=bmpToByteArray(bitmap,true);
        //4.创建SendMessageToWX.Req对象
        SendMessageToWX.Req req=new SendMessageToWX.Req();
        req.transaction=buildTransaction("emoji");
        req.message=msg;
        req.scene = isPyq ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        //发送给客户端
        api.sendReq(req);
    }

}
