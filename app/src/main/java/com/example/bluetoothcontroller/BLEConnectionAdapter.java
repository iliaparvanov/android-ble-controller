package com.example.bluetoothcontroller;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class BLEConnectionAdapter extends ArrayAdapter<ScanResult> {

    private static final String LOG_TAG = BLEConnectionAdapter.class.getSimpleName();

    public BLEConnectionAdapter(ArrayList<ScanResult> list, Context context) {
        super(context, 0, list);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(
                    R.layout.listview_with_button, parent, false);
        }

        //Handle TextView and display string from your list
        TextView listItemText = (TextView) view.findViewById(R.id.list_item_string);
        listItemText.setText(getItem(position).toString());

        //Handle buttons and add onClickListeners
//        Button connectBtn = view.findViewById(R.id.connect_btn);
//
//        connectBtn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
////                System.out.println("Something");
////                notifyDataSetChanged();
//
//            }
//        });

        return view;
    }


}

