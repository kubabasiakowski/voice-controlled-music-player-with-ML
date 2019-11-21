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

public class SongsOnPlaylistAdapter extends BaseAdapter {

    Context context;
    ArrayList<Song> songList;
    DatabaseHelper dbHelper;

    public SongsOnPlaylistAdapter(Context context, ArrayList<Song> songList){
        this.context = context;
        this.songList = songList;
        dbHelper = new DatabaseHelper(context);
    }

    @Override
    public int getCount() {
        return this.songList.size();
    }

    @Override
    public Object getItem(int position) {
        return songList.get(position);
    }

    @Override
    public long getItemId(int position) {
        final Song song = songList.get(position);
        return song.getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.song_on_playlist_row, null);
        TextView songName = (TextView)convertView.findViewById(R.id.SongNameOnPlaylist);

        Button btnDeleteSong = (Button)convertView.findViewById(R.id.btnDeleteSongFromPlaylist);

        final Song song = songList.get(position);

        songName.setText(song.getName());

        btnDeleteSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dbHelper.deleteSong(song.getId());
                songList.remove(position);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

}
