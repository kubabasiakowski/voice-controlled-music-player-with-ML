package com.example.kuba.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kuba on 26.03.2019.
 */

public class DatabaseHelper  extends SQLiteOpenHelper {

    private static final String DB_NAME = "musicplayer_databse";
    private static final int DB_VERSION = 1;

    // tabela users
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "_id";
    private static final String COLUMN_USER_NAME = "name";
    private static final String COLUMN_USER_SURNAME = "surname";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_PASSWORD = "password";

    //tabela playlists
    private static final String TABLE_PLAYLISTS = "playlists";
    private static final String COLUMN_PLAYLIST_ID = "_id";
    private static final String COLUMN_PLAYLIST_NAME = "name";
    private static final String COLUMN_PLAYLIST_USER_ID = "user_id";

    //tabela tracks
    private static final String TABLE_TRACKS = "tracks";
    private static final String COLUMN_TRACK_ID = "_id";
    private static final String COLUMN_TRACK_NAME = "name";
    private static final String COLUMN_TRACK_PATH = "path";
    private static final String COLUMN_TRACK_PLAYLIST_ID = "playlist_id";

    //tabela recognized_words
    private static final String TABLE_COMMANDS = "commands";
    private static final String COLUMN_COMMAND_ID = "_id";
    private static final String COLUMN_COMMAND_NAME = "name";
    private static final String COLUMN_COMMAND_OPERATION = "operation";
    private static final String COLUMN_COMMAND_USER_ID = "user_id";

    //SQL statements
    private static final String SQL_CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_NAME + " TEXT NOT NULL, "
            + COLUMN_USER_SURNAME + " TEXT NOT NULL, "
            + COLUMN_USER_EMAIL + " TEXT NOT NULL, "
            + COLUMN_USER_PASSWORD + " TEXT NOT NULL "
            + ");";

    private static final String SQL_CREATE_TABLE_PLAYLISTS = "CREATE TABLE " + TABLE_PLAYLISTS + "("
            + COLUMN_PLAYLIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_PLAYLIST_NAME + " TEXT NOT NULL, "
            + COLUMN_PLAYLIST_USER_ID + " INTEGER NOT NULL "
            + ");";

    private static final String SQL_CREATE_TABLE_TRACKS = "CREATE TABLE " + TABLE_TRACKS + "("
            + COLUMN_TRACK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TRACK_NAME + " TEXT NOT NULL, "
            + COLUMN_TRACK_PATH + " TEXT NOT NULL, "
            + COLUMN_TRACK_PLAYLIST_ID + " INTEGER "
            + ");";

    private static final String SQL_CREATE_TABLE_COMMANDS = "CREATE TABLE " + TABLE_COMMANDS + "("
            + COLUMN_COMMAND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_COMMAND_NAME + " TEXT NOT NULL, "
            + COLUMN_COMMAND_OPERATION + " TEXT NOT NULL, "
            + COLUMN_COMMAND_USER_ID + " INTEGER NOT NULL "
            + ");";

    //konstruktor
    public DatabaseHelper(Context context){
        super(context, DB_NAME,null, DB_VERSION);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_USERS);
        db.execSQL(SQL_CREATE_TABLE_PLAYLISTS);
        db.execSQL(SQL_CREATE_TABLE_TRACKS);
        db.execSQL(SQL_CREATE_TABLE_COMMANDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //clear data
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMANDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        //create tables again
        onCreate(db);
    }

    public void addUser(String name, String surname, String email, String password) {
       SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USER_SURNAME, surname);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        db.insert(TABLE_USERS,null,values);
        //long id = db.insertWithOnConflict(TABLE_USERS,null,values,SQLiteDatabase.CONFLICT_IGNORE);

    }

    public boolean checkIfExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_EMAIL +"=?", new String[]{email});

        if (mCursor.moveToFirst())
        {
            return true;
            /* record exist */
        }
        else
        {
            return false;
            /* record not exist */
        }
    }

    //metoda login zwraca id uzytkownika
    public int login(String email, String password){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + TABLE_USERS
                + " WHERE "+ COLUMN_USER_EMAIL
                + "=? AND " + COLUMN_USER_PASSWORD + "=?", new String[]{email, password});
        if(mCursor.getCount()>0) {
            mCursor.moveToFirst();
            return mCursor.getInt(mCursor.getColumnIndex(COLUMN_USER_ID));
        }
        else
            return 0;
    }

    //metoda zwraca obiekt User
    public User selectUser(int id){
        User user = new User();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + TABLE_USERS
                + " WHERE "+ COLUMN_USER_ID
                + "=?", new String[]{String.valueOf(id)});
        mCursor.moveToFirst();
        user.setId(id);
        user.setName(mCursor.getString(mCursor.getColumnIndex(COLUMN_USER_NAME)));
        user.setSurname(mCursor.getString(mCursor.getColumnIndex(COLUMN_USER_SURNAME)));
        user.setEmail(mCursor.getString(mCursor.getColumnIndex(COLUMN_USER_EMAIL)));
        user.setPassword(mCursor.getString(mCursor.getColumnIndex(COLUMN_USER_PASSWORD)));
        return user;
    }

    //dodawanie playlisty
    public void addPlaylist(String name, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAYLIST_NAME, name);
        values.put(COLUMN_PLAYLIST_USER_ID, userId);
        db.insert(TABLE_PLAYLISTS,null,values);

    }

    //select playlists where user id
    public ArrayList<Playlist> selectPlaylists(int userId){
        ArrayList<Playlist> playlists = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM "
                + TABLE_PLAYLISTS + " WHERE "+ COLUMN_PLAYLIST_USER_ID + "=?", new String[]{String.valueOf(userId)});
        if(mCursor.moveToFirst()){
            do{
                playlists.add(new Playlist(
                        mCursor.getInt(0),
                        mCursor.getString(1),
                        mCursor.getInt(2)
                ));
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        return playlists;
    }

    //zwraca liczbe utworów playlisty
    public int countNumberOfTracksInPlaylist(int playlistId){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRACKS
                + " WHERE " + COLUMN_TRACK_PLAYLIST_ID + "=?", new String[]{String.valueOf(playlistId)}).getCount();
    }

    //usuwa playliste
    public void deletePlaylist(int playlistId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYLISTS,COLUMN_PLAYLIST_ID + " =?", new String[]{String.valueOf(playlistId)});
    }

    public String getPlaylistName(int playlistId){
        SQLiteDatabase db = this.getReadableDatabase();
         Cursor mCursor = db.rawQuery("SELECT " + COLUMN_PLAYLIST_NAME +
                " FROM " + TABLE_PLAYLISTS + " WHERE " + COLUMN_PLAYLIST_ID + "=?", new String[]{String.valueOf(playlistId)});
        if(mCursor.moveToFirst()){
                String name = mCursor.getString(0);
                return name;
        }
        mCursor.close();
        return "brak";
    }

    public Playlist getPlaylistByNameAndUserId(String playlistName, int userId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM "
                 + TABLE_PLAYLISTS + " WHERE " + COLUMN_PLAYLIST_NAME + "=? AND "
                 + COLUMN_PLAYLIST_USER_ID + "=?", new String[]{playlistName, String.valueOf(userId)});
        if(mCursor.moveToFirst()){
            int id = mCursor.getInt(0);
            String name = mCursor.getString(1).toLowerCase();
            int usrId = mCursor.getInt(2);
            Playlist playlist = new Playlist(id,name,usrId);

            return playlist;
        }
        mCursor.close();
        return null;
    }

    //select tracks where playlist id
    public ArrayList<Song> selectSongs(int playlistId){
        ArrayList<Song> songs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM "
                + TABLE_TRACKS + " WHERE "+ COLUMN_TRACK_PLAYLIST_ID + "=?", new String[]{String.valueOf(playlistId)});
        if(mCursor.moveToFirst()){
            do{
                songs.add(new Song(
                        mCursor.getInt(0),
                        mCursor.getString(1),
                        mCursor.getString(2),
                        mCursor.getInt(3)
                ));
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        return songs;
    }

    //usuwa piosenke z playlisty
    public void deleteSong(int songId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRACKS,COLUMN_TRACK_ID + " =?", new String[]{String.valueOf(songId)});
    }

    //dodaje wybrane utwory do konkretnej playlisty
    public void addSongsToPlaylist(int playlistId, Song song){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values;
        values = new ContentValues();
        values.put(COLUMN_TRACK_NAME, song.getName());
        values.put(COLUMN_TRACK_PATH, song.getPath());
        values.put(COLUMN_TRACK_PLAYLIST_ID, playlistId);
        db.insert(TABLE_TRACKS,null,values);
    }
}
