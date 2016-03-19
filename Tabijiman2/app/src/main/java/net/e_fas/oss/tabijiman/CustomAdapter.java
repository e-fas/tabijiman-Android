package net.e_fas.oss.tabijiman;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<CustomData> {

    private LayoutInflater layoutInflater_;

    public CustomAdapter(Context context, int textViewResourceId, List<CustomData> objects) {
        super(context, textViewResourceId, objects);
        layoutInflater_ = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 特定の行(position)のデータを得る
        CustomData item = getItem(position);

        if (item.getIsSection()) {

            convertView = layoutInflater_.inflate(R.layout.custom_section, null);
            TextView SectionName = (TextView) convertView.findViewById(R.id.SectionName);
            SectionName.setText(item.getSectionName());

        } else {

            convertView = layoutInflater_.inflate(R.layout.custom_layout, null);

            // CustomDataのデータをViewの各Widgetにセットする
            ImageView imageView;
            imageView = (ImageView) convertView.findViewById(R.id.FrameView);
            imageView.setImageBitmap(item.getImageData());

            TextView TitleView;
            TitleView = (TextView) convertView.findViewById(R.id.TitleLabel);
            TitleView.setText(item.getTitleLabel());

            TextView DescView;
            DescView = (TextView) convertView.findViewById(R.id.DescLabel);
            DescView.setText(item.getDescLabel());
        }

        return convertView;
    }
}
