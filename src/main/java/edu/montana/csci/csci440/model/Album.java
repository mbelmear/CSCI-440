package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Album extends Model {

    Long albumId;
    Long artistId;
    String title;

    public Album() {
    }

    private Album(ResultSet results) throws SQLException {
        title = results.getString("Title");
        albumId = results.getLong("AlbumId");
        artistId = results.getLong("ArtistId");
    }

    public Artist getArtist() {
        return Artist.find(artistId);
    }

    public void setArtist(Artist artist) {
        artistId = artist.getArtistId();
    }

    public List<Track> getTracks() {
        return Track.forAlbum(albumId);
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbum(Album album) {
        this.albumId = album.getAlbumId();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public Long getArtistId() {
        return artistId;
    }

    @Override
    public boolean create(){
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO Albums (Title, ArtistId) VALUES (?, ?)")) {
                stmt.setString(1, getTitle());
                stmt.setLong(2, getArtistId());
                int result = stmt.executeUpdate();
                this.albumId = DB.getLastID(conn);
                return result == 1;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean verify() {
        _errors.clear();
        if (artistId == null) {
            addError("ArtistId can't be null");
        }
        if (title == null || title.trim().isEmpty()) {
            addError("Title can't be null or blank");
        }
        return !hasErrors();
    }

    @Override
    public boolean update() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE albums SET Title = ?, ArtistId = ? WHERE AlbumId = ?")) {
                stmt.setString(1, getTitle());
                stmt.setLong(2, getArtistId());
                stmt.setLong(3, getAlbumId());
                int result = stmt.executeUpdate();
                return result == 1;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    @Override
    public void delete() {

        // tracks
        List<Track> tracks = this.getTracks();
        for (Track t : tracks) {
            t.delete();
        }

        String query = "DELETE FROM albums WHERE AlbumId=?";
        try(Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, getAlbumId());
            stmt.executeUpdate();
        } catch (SQLException sqlException){
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Album> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Album> all(int page, int count) {
        int offset = (page - 1) * count;
        try {
            try(Connection connect = DB.connect();
                PreparedStatement stmt = connect.prepareStatement("SELECT * FROM albums LIMIT ? OFFSET ?")){
                ArrayList<Album> result = new ArrayList<>();
                stmt.setInt(1, count);
                stmt.setInt(2, offset);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    result.add(new Album(resultSet));
                }
                return result;
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public static Album find(long i) {
        try{
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT * FROM albums WHERE AlbumId = ?")) {
                stmt.setLong(1, i);
                ResultSet resultSet = stmt.executeQuery();
                if(resultSet.next()){
                    return new Album(resultSet);
                }
                else{
                    return null;
                }
            }
        } catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    public static List<Album> getForArtist(Long artistId) {
        String query = "SELECT * FROM albums WHERE ArtistId=?";

        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, artistId);

            ResultSet results = stmt.executeQuery();
            List<Album> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Album(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

}
