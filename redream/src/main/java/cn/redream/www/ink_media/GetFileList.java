package cn.redream.www.ink_media;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by acer on 2016/3/21.
 */
public class GetFileList {
    ArrayList<String> flieList;
    File dir;
    boolean isRecursive;
    public GetFileList(String dirPath,boolean isRecursive){
        flieList=new ArrayList<>();
        dir=new File(dirPath);
        this.isRecursive=isRecursive;

    }
    public void run(File fileDir){
        if (!fileDir.exists())return;
        File[] files = fileDir.listFiles();
        for(int i=0; i<files.length; i++){
            System.out.println(files[i].getAbsolutePath());
            if(files[i].isDirectory()){
                if (isRecursive){
                    try{
                        run(files[i]);
                    }catch(Exception e){

                    }
                }
            }else{
                flieList.add(files[i].getName());
            }
        }
    }
    public ArrayList<String> getFileArrayList(){
        run(dir);
        return flieList;
    }

}
