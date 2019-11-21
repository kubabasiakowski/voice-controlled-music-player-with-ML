package com.example.kuba.musicplayer;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class RecognizedCommandsAdapter extends BaseAdapter {

    Context context;
    ArrayList<Command> commandList;
    DatabaseHelper dbHelper;

    public RecognizedCommandsAdapter(Context context, ArrayList<Command> commandList){
        this.context = context;
        this.commandList = commandList;
        dbHelper = new DatabaseHelper(context);
    }

    @Override
    public int getCount() {
        return this.commandList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.commandList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.command_on_list_row, null);
        TextView commandName = (TextView)convertView.findViewById(R.id.commandNameTextView);
        TextView commandOperation = (TextView)convertView.findViewById(R.id.commandOperationTextView);

        final Command command = commandList.get(position);

        commandName.setText(command.getName());
        commandOperation.setText(command.getOperation());

        return convertView;
    }
}
