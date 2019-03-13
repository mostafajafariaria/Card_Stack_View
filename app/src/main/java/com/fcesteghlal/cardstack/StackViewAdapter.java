package com.fcesteghlal.cardstack;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;


import java.util.List;

//Objects is your model
public class StackViewAdapter extends ArrayAdapter<Object> {

    private List<Object> objects;

    StackViewAdapter(@NonNull Context context, List<Object> objects) {
        super(context, R.layout.layout_stack_cards);
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_stack_cards, parent, false);
        }
        return view;
    }

    @Override
    public int getCount() {
        return objects.size();
    }
}
