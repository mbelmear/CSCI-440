package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Artist extends Model {

    Long artistId;
    String name;
    String originalName;
    public Artist() {
    }

    private Artist(ResultSet results) throws SQLException {
        name = results.getString("Name");
        artistId = results.getLong("ArtistId");
        originalName = name;
    }

    public List<Album> getAlbums(){
        return Album.getForArtist(artistId);
    }

    public Long getArtistId() {
        return artistId;
    }

    public void setArtist(Artist artist) {
        this.artistId = artist.getArtistId();
    }

    public String getOriginalName(){
        return originalName;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<Artist> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Artist> all(int page, int count) {
        int offset = (page - 1) * count;
        try {
            try(Connection connect = DB.connect();
                PreparedStatement stmt = connect.prepareStatement("SELECT * FROM artists LIMIT ? OFFSET ?")){
                ArrayList<Artist> result = new ArrayList<>();
                stmt.setInt(1, count);
                stmt.setInt(2, offset);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    result.add(new Artist(resultSet));
                }
                return result;
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public static Artist find(long i) {
        try{
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT * FROM artists WHERE ArtistId = ?")) {
                stmt.setLong(1, i);
                ResultSet resultSet = stmt.executeQuery();
                if(resultSet.next()){
                    return new Artist(resultSet);
                }
                else{
                    return null;
                }
            }
        } catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean verify(){
        _errors.clear();
        if (name == null || name.trim().isEmpty()){
            addError("Name can't be null or blank");
        }
        return !hasErrors();
    }

    @Override
    public boolean create(){
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO Artists (Name) VALUES (?)")) {
                stmt.setString(1, getName());
                int result = stmt.executeUpdate();
                this.artistId = DB.getLastID(conn);
                return result == 1;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean update() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE artists SET Name=? WHERE ArtistId=? AND Name=?")) {
                stmt.setString(1, getName());
                stmt.setLong(2, getArtistId());
                stmt.setString(3, getOriginalName());
                int update = stmt.executeUpdate();
                return update == 1;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    @Override
    public void delete() {

        // albums
        List<Album> albums = this.getAlbums();
        for (Album a : albums) {
            a.delete();
        }

        String query = "DELETE FROM artists WHERE ArtistId=?";
        try(Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, getArtistId());
            stmt.executeUpdate();
        } catch (SQLException sqlException){
            throw new RuntimeException(sqlException);
        }
    }

}
