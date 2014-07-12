
package com.shake;

import android.app.Activity;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FileExplorer extends ListActivity {
 
    private List<String> item = null;
    private List<String> path = null;
    private final String root="/", startingRoot = "/sdcard/download";
    private TextView myPath;
    private File currentFile;
    private final String SEARCH = "SEARCH";
    //private ListView listview;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_explorer_layout);
        myPath = (TextView)findViewById(R.id.path);
        
        
        //listview = (ListView) findViewById(R.id.list);
        
        getDir(startingRoot);
    }
    
    @Override
    protected void onStop(){
        super.onStop();
        /*Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_IDENTIFIER, "poopybutt");
        setResult(Activity.RESULT_OK, resultIntent);
        finish();*/
    }
    
    private void getDir(String dirPath){
        myPath.setText("Location: " + dirPath);

        item = new ArrayList<String>();
        path = new ArrayList<String>();

        File f = new File(dirPath);
        File[] files = f.listFiles();

        if(!dirPath.equals(root)){
            item.add(root);
            path.add(root);
            item.add("../");
            path.add(f.getParent());
        }
        for (File file : files) {
            if(file.isDirectory()){
                path.add(file.getPath());
                item.add(file.getName() + "/");
            }
            else if (file.getName().endsWith(".gif") || file.getName().endsWith(".mp3")){
                System.out.println(file.getPath());
                path.add(file.getPath());
                item.add(file.getName());
            }
        }
        
        CustomList adapter = new CustomList(this, item.toArray(new String[item.size()]), path.toArray(new String[path.size()]));
        setListAdapter(adapter);
        //ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.row, item);
        //setListAdapter(fileList);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        File file = new File(path.get(position));
        currentFile = file;

        if (file.isDirectory()){
            if(file.canRead()){
                getDir(path.get(position));
            }
            else{
              new AlertDialog.Builder(this)
                  .setTitle("[" + file.getName() + "] folder can't be read!")
                  .setPositiveButton("OK", 
                      new DialogInterface.OnClickListener() {

                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              // TODO Auto-generated method stub
                          }
                  }).show();
            }
        }
        else{
            String type = currentFile.getName().endsWith(".gif") ? "GIF" : "MP3";
            new AlertDialog.Builder(this)
                .setTitle("Set " + file.getName() + " as main " + type + "?")
                .setNegativeButton("NO", null)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*Bitmap bitmap = BitmapFactory.decodeFile(currentFile.getAbsolutePath());
                        setContentView(R.layout.test_image_view);
                        ImageView img = (ImageView) findViewById(R.id.img);
                        img.setImageDrawable(bitmap);*/
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(SEARCH, currentFile.getAbsolutePath());
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                }).show();
        }
    }
}