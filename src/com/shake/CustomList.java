package com.shake;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomList extends ArrayAdapter<String>{
    private final Activity context;
    private final String[] web;
    private final String[] imageUri;
    //private final Integer[] imageId;
    
    public CustomList(Activity context, String[] web, String[] imageUri) {
        super(context, R.layout.list_single, web);
        this.context = context;
        this.web = web;
        //this.imageId = imageId;
        this.imageUri = imageUri;
    }
    
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_single, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(web[position]);
        //imageView.setImageResource(imageId[position]);
        imageView.setImageURI(Uri.parse(imageUri[position]));
        //imageView.setImageBitmap(BitmapFactory.decodeFile(new File(imageUri[position])));
        return rowView;
    }
}