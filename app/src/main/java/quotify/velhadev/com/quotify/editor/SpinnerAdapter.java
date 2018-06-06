package quotify.velhadev.com.quotify.editor;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import quotify.velhadev.com.quotify.R;

/**
 * Created by abhishek on 16/03/18.
 */

public class SpinnerAdapter extends ArrayAdapter<Pair<String, String>> {

    private Context context;

    public SpinnerAdapter(@NonNull Context context, ArrayList<Pair<String, String>> fonts) {
        super(context, 0, fonts);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view;

        if(convertView == null)
            view = LayoutInflater.from(context).inflate(R.layout.spinner_item_layout, parent, false);
        else
            view = convertView;

        Pair<String, String> fonts = getItem(position);

        TextView textView = view.findViewById(R.id.spinner_textView);
        textView.setTypeface(Typeface.createFromAsset(context.getAssets(), fonts.second), Typeface.BOLD);
        textView.setText(fonts.first);
        return view;
    }

    @NonNull
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view;

        if(convertView == null)
            view = LayoutInflater.from(context).inflate(R.layout.spinner_item_layout, parent, false);
        else
            view = convertView;

        Pair<String, String> fonts = getItem(position);

        TextView textView = view.findViewById(R.id.spinner_textView);
        textView.setTypeface(Typeface.createFromAsset(context.getAssets(), fonts.second), Typeface.BOLD);
        textView.setText(fonts.first);
        return view;
    }

}
