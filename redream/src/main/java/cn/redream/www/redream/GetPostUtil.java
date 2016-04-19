package cn.redream.www.redream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * Created by acer on 2015/9/11.
 */
public class GetPostUtil {
/*
* 向指定url发送GET方式的请求
* @param url发送请求的URL
* @param params请求参数，形式name1=value1&name2=value2
* @return URL代表远程资源的响应
*
*
* */
    public static String sendGet(String url,String params){
        String result="";
        BufferedReader in=null;
        try{
            String urlName=url+"?"+params;
            URL realUrl=new URL(urlName);
            //打开和url之间的连接
            URLConnection conn=realUrl.openConnection();
            //设置url的通用属性
            conn.setRequestProperty("accept","*/*");
            conn.setRequestProperty("connection","Keep-Alive");
            conn.setRequestProperty("user-agent","Mozilla/4.0(compatible;MSIE 6.0;Windows NT 5.1;SV1)");
            //建立实际的连接
            conn.connect();
            //获取所有的响应字段
            Map<String,List<String>> map=conn.getHeaderFields();
            //遍历所有的响应头
            for (String key:map.keySet()) {
                System.out.println(key+"---->"+map.get(key));

            }
            //定义Bufferedreader输入流来读取url的响应
            in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while((line=in.readLine())!=null){
                result+="\n"+line;
            }
        } catch (MalformedURLException e) {
            System.out.println("GET请求出现异常"+e);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //使用final来关闭输入流
        finally{
            try{
                if (in!=null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    public static String sendGetGbk(String url,String params){
        String result="";
        BufferedReader in=null;
        try{
            String urlName=url+"?"+params;
            URL realUrl=new URL(urlName);
            //打开和url之间的连接
            URLConnection conn=realUrl.openConnection();
            //设置url的通用属性
            conn.setRequestProperty("accept","*/*");
            conn.setRequestProperty("connection","Keep-Alive");
            conn.setRequestProperty("user-agent","Mozilla/4.0(compatible;MSIE 6.0;Windows NT 5.1;SV1)");
            //建立实际的连接
            conn.connect();
            //获取所有的响应字段
            Map<String,List<String>> map=conn.getHeaderFields();
            //遍历所有的响应头
            for (String key:map.keySet()) {
                System.out.println(key+"---->"+map.get(key));

            }
            //定义Bufferedreader输入流来读取url的响应
            in=new BufferedReader(new InputStreamReader(conn.getInputStream(),"gb2312"));
            String line;
            while((line=in.readLine())!=null){
                result+="\n"+line;
            }
        } catch (MalformedURLException e) {
            System.out.println("GET请求出现异常"+e);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //使用final来关闭输入流
        finally{
            try{
                if (in!=null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    /*
    * 向指定url发送POST方式的请求
    * @param url发送请求的URL
    * @param params请求参数，形式name1=value1&name2=value2
    * @return URL代表远程资源的响应
    *
    *
    * */
    public static String sendPost(String url,String params){
        PrintWriter out=null;
        BufferedReader in=null;
        String result="";
        try {
            URL realUrl=new URL(url);
            URLConnection conn=realUrl.openConnection();
            //设置url的通用属性
            conn.setRequestProperty("accept","*/*");
            conn.setRequestProperty("connection","Keep-Alive");
            conn.setRequestProperty("user-agent","Mozilla/4.0(compatible;MSIE 6.0;Windows NT 5.1;SV1)");
            //发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //获取URLConnection对象对应的输入流
            out=new PrintWriter(conn.getOutputStream());
            //发送请求参数
            out.print(params);
            //flush输出的缓冲
            out.flush();
            //定义Bufferedreader输入流来读取url的响应
            in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while((line=in.readLine())!=null){
                result+="\n"+line;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (out!=null){
                out.close();
            }
            if (in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    public static String sendPostGbk(String url,String params){
        PrintWriter out=null;
        BufferedReader in=null;
        String result="";
        try {
            URL realUrl=new URL(url);
            URLConnection conn=realUrl.openConnection();
            //设置url的通用属性
            conn.setRequestProperty("accept","*/*");
            conn.setRequestProperty("connection","Keep-Alive");
            conn.setRequestProperty("user-agent","Mozilla/4.0(compatible;MSIE 6.0;Windows NT 5.1;SV1)");
            //发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //获取URLConnection对象对应的输入流
            out=new PrintWriter(conn.getOutputStream());
            //发送请求参数
            out.print(params);
            //flush输出的缓冲
            out.flush();
            //定义Bufferedreader输入流来读取url的响应
            in=new BufferedReader(new InputStreamReader(conn.getInputStream(),"gb2312"));
            String line;
            while((line=in.readLine())!=null){
                result+="\n"+line;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (out!=null){
                out.close();
            }
            if (in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    public static Map<String,List<String>> getHeaer(String url,String params){
        Map<String,List<String>> map=null;
        try{
            String urlName=url+"?"+params;
            URL realUrl=new URL(urlName);
            //打开和url之间的连接
            URLConnection conn=realUrl.openConnection();
            //设置url的通用属性
            conn.setRequestProperty("accept","*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0(compatible;MSIE 6.0;Windows NT 5.1;SV1)");
            //建立实际的连接
            conn.connect();
            //获取所有的响应字段
            map=conn.getHeaderFields();
            //遍历所有的响应头
            for (String key:map.keySet()) {
                System.out.println(key+"---->"+map.get(key));

            }

        } catch (MalformedURLException e) {
            System.out.println("GET请求出现异常"+e);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }
}
