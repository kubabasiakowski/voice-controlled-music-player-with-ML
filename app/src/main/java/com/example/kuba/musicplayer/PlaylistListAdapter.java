package com.example.kuba.musicplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class PlaylistListAdapter extends BaseAdapter {

    Context context;
    ArrayList<Playlist> playlistList;
    DatabaseHelper dbHelper;

    public PlaylistListAdapter(Context context, ArrayList<Playlist> playlistList){
        this.context = context;
        this.playlistList = playlistList;
        dbHelper = new DatabaseHelper(context);
    }

    @Override
    public int getCount() {
        return this.playlistList.size();
    }

    @Override
    public Object getItem(int position) {
        return playlistList.get(position);
    }

    @Override
    public long getItemId(int position) {
        final Playlist playlist = playlistList.get(position);
        return playlist.getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.playlist_list_row, null);
        TextView playlistName = (TextView)convertView.findViewById(R.id.PlaylistNameInListTextView);
        TextView trackNumber = (TextView)convertView.findViewById(R.id.NumberOfTracksTextView);

        Button btnDeletePlaylist = (Button)convertView.findViewById(R.id.btnDeletePlaylist);

        final Playlist playlist = playlistList.get(position);

        playlistName.setText(playlist.getName());
        trackNumber.setText("Liczba utworów: "+ String.valueOf(dbHelper.countNumberOfTracksInPlaylist(playlist.getId())));

        btnDeletePlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dbHelper.deletePlaylist(playlist.getId());
                playlistList.remove(position);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

}
