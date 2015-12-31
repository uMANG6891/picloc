package com.umang.picloc;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.umang.picloc.utility.JSC;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by umang on 29/12/15.
 */
public class HorizontalImageAdapter {

    List<JSONObject> LIST_DATA;

    AppCompatActivity con;
    LayoutInflater inflater;
    LinearLayout llImagesMain;

    public HorizontalImageAdapter(AppCompatActivity con, List<JSONObject> jo, LinearLayout llImagesMain) {
        this.con = con;
        this.llImagesMain = llImagesMain;

        LIST_DATA = jo;

        inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        notifyDataSetChange();
    }

    private void notifyDataSetChange() {
        showImages();
    }

    public void showImages() {
        int length = LIST_DATA == null ? 0 : LIST_DATA.size();
        if (length > 0) {
            llImagesMain.removeAllViews();
            llImagesMain.setVisibility(View.VISIBLE);

            View view;
            ImageView iv;
            TextView tv;

            String title;
            for (int i = 0; i < length; i++) {
//                Debug.e(LIST_DATA.get(i).toString());

                JSONObject jo = LIST_DATA.get(i);
                JSONObject joImg = JSC.strToJOb(JSC.getJString(jo, "images"));
                JSONObject joImgLowRes = JSC.strToJOb(JSC.getJString(joImg, "low_resolution"));

                JSONObject joUser = JSC.strToJOb(JSC.getJString(jo, "user"));
                if (joUser != null) {
                    title = JSC.getJString(joUser, "full_name") + " (" + JSC.getJString(joUser, "username") + ")";
                } else {
                    title = "UNKNOWN";
                }

                view = inflater.inflate(R.layout.item_image_view, llImagesMain, false);
                iv = (ImageView) view.findViewById(R.id.item_image);
                tv = (TextView) view.findViewById(R.id.item_text);
                tv.setText(title);
                final int finalI = i;
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        JSONObject jo = LIST_DATA.get(finalI);
                        Intent i = new Intent(con, ImageViewActivity.class);
                        i.putExtra(ImageViewActivity.EXTRA_IMAGE_DATA, jo.toString());
                        con.startActivity(i);
                    }
                });
                llImagesMain.addView(view);
                Glide.with(con)
                        .load(JSC.getJString(joImgLowRes, "url"))
                        .into(iv);
            }
        } else {
            llImagesMain.setVisibility(View.GONE);
        }
    }

    public void swapData(List<JSONObject> jo) {
        LIST_DATA = jo;
        notifyDataSetChange();
    }
}
