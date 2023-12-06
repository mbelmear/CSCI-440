package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;

public class Track extends Model {

    private Long trackId;
    private Long albumId;
    private Long mediaTypeId;
    private Long genreId;
    private String name;
    private Long milliseconds;
    private Long bytes;
    private BigDecimal unitPrice;

    private Artist artistcache;
    private Album albumcache;

    public static final String REDIS_CACHE_KEY = "cs440-tracks-count-cache";

    public Track() {
        mediaTypeId = 1l;
        genreId = 1l;
        milliseconds  = 0l;
        bytes  = 0l;
        unitPrice = new BigDecimal("0");
    }

    Track(ResultSet results) throws SQLException {
        name = results.getString("Name");
        milliseconds = results.getLong("Milliseconds");
        bytes = results.getLong("Bytes");
        unitPrice = results.getBigDecimal("UnitPrice");
        trackId = results.getLong("TrackId");
        albumId = results.getLong("AlbumId");
        mediaTypeId = results.getLong("MediaTypeId");
        genreId = results.getLong("GenreId");

        albumcache = getAlbum();
        artistcache = albumcache.getArtist();
    }

    public static Long count() {
        Jedis redisClient = new Jedis(); // use this class to access redis and create a cache

        if(redisClient.exists(REDIS_CACHE_KEY)) {
            return Long.parseLong(redisClient.get(REDIS_CACHE_KEY));
        }

        String query = "SELECT COUNT(*) as Count FROM tracks";

        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                long c = results.getLong("Count");
                redisClient.set(REDIS_CACHE_KEY, String.valueOf(results.getLong("Count")));
                return c;
            } else {
                throw new IllegalStateException("Should find a count!");
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public Album getAlbum() {
        return Album.find(albumId);
    }

    public MediaType getMediaType() {
        return null;
    }
    public Genre getGenre() {
        return null;
    }
    public List<Playlist> getPlaylists() {
        try {
            try (Connection connect = DB.connect();
                 PreparedStatement stmt = connect.prepareStatement(
                         "SELECT p.* FROM playlists p " +
                                 "JOIN playlist_track pt ON p.PlaylistId = pt.PlaylistId " +
                                 "WHERE pt.TrackId = ?")) {
                stmt.setLong(1, this.trackId); // Assuming trackId is the ID of the current track
                try (ResultSet resultSet = stmt.executeQuery()) {
                    List<Playlist> playlists = new ArrayList<>();
                    while (resultSet.next()) {
                        playlists.add(new Playlist(resultSet));
                    }
                    return playlists;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(Long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public void setAlbum(Album album) {
        if (album != null) {
            albumId = album.getAlbumId();
        } else {
            albumId = null;
        }
    }

    public Long getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(Long mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
    }

    public String getArtistName() {
        return artistcache.getName();
    }

    public String getAlbumTitle() {
        return albumcache.getTitle();
    }

    public static List<Track> advancedSearch(int page, int count,
                                             String search, Integer artistId, Integer albumId,
                                             Integer maxRuntime, Integer minRuntime) {
        LinkedList<Object> args = new LinkedList<>();

        String query = "SELECT tracks.*, albums.ArtistId FROM tracks " +
                "JOIN albums ON tracks.AlbumId = albums.AlbumId " +
                "WHERE name LIKE ?";

        args.add("%" + search + "%");

        if (artistId != null) {
            query += " AND albums.ArtistId=? ";
            args.add(artistId);
        }

        if (albumId != null) {
            query += " AND albums.AlbumId=? ";
            args.add(albumId);
        }

        if (maxRuntime != null){
            query += " AND tracks.Milliseconds < ?";
            args.add(maxRuntime);
        }

        if (minRuntime != null){
            query += " AND tracks.Milliseconds > ?";
            args.add(minRuntime);
        }

        query += " LIMIT ?";
        args.add(count);

        try (Connection conn = DB.connect();  PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                stmt.setObject(i + 1, arg);
            }

            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }

            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Track> search(int page, int count, String orderBy, String search) {
        String query = "SELECT tracks.*, albums.Title as AlbumTitle, artists.Name as ArtistName " +
                "FROM tracks " +
                "JOIN albums ON albums.AlbumId=tracks.AlbumId " +
                "JOIN artists ON artists.ArtistId=albums.ArtistId " +
                "WHERE tracks.Name LIKE ? OR AlbumTitle LIKE ? OR ArtistName LIKE ? " +
                "LIMIT ? " +
                "OFFSET ?";

        search = "%" + search + "%";

        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, search);
            stmt.setString(2, search);
            stmt.setString(3, search);
            stmt.setInt(4, count);
            stmt.setInt(5, count * (page - 1));

            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Track> forAlbum(Long albumId) {
        String query = "SELECT * FROM tracks WHERE AlbumId=?";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, albumId);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // Sure would be nice if java supported default parameter values
    public static List<Track> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Track> all(int page, int count) {
        int offset = (page - 1) * count;
        try {
            try(Connection connect = DB.connect();
                PreparedStatement stmt = connect.prepareStatement("SELECT * FROM tracks LIMIT ? OFFSET ?")){
                ArrayList<Track> result = new ArrayList<>();
                stmt.setInt(1, count);
                stmt.setInt(2, offset);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    result.add(new Track(resultSet));
                }
                return result;
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public static List<Track> all(int page, int count, String orderBy) {
        int offset = (page - 1) * count;
        try {
            try(Connection connect = DB.connect();
                PreparedStatement stmt = connect.prepareStatement("SELECT * FROM tracks ORDER BY " + orderBy + " LIMIT ? OFFSET ?")){
                ArrayList<Track> result = new ArrayList<>();
                stmt.setInt(1, count);
                stmt.setInt(2, offset);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    result.add(new Track(resultSet));
                }
                return result;
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean verify() {
        _errors.clear(); // clear any existing errors
        // name
        if(getName() == null || getName().isEmpty()) {
            addError("Name cannot be null or blank!");
        }
        // must have album (i.e. albumId)
        if(this.albumId == null) {
            addError("Album cannot be null or blank!");
        }
        return !hasErrors();
    }

    @Override
    public boolean create() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO tracks (Name, AlbumId, MediaTypeId, GenreId, Milliseconds, Bytes, UnitPrice) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setString(1, getName());
                stmt.setObject(2, getAlbumId(), Types.INTEGER);
                stmt.setLong(3, getMediaTypeId());
                stmt.setLong(4, getGenreId());
                stmt.setLong(5, getMilliseconds());
                stmt.setLong(6, getBytes());
                stmt.setBigDecimal(7, getUnitPrice());

                int result = stmt.executeUpdate();
                this.trackId = DB.getLastID(conn);

                Jedis redisClient = new Jedis();
                redisClient.del(REDIS_CACHE_KEY);

                return result == 1;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }
    public static Track find(long id) {
        try {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM tracks WHERE TrackId = ?")) {
                stmt.setLong(1, id);
                try (ResultSet resultSet = stmt.executeQuery()) {
                    return resultSet.next() ? new Track(resultSet) : null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete() {
        List<Playlist> playlists = this.getPlaylists();
        for(Playlist p : playlists) {
            p.removeTrack(this);
        }

        String invoice_query = "DELETE FROM invoice_items WHERE TrackId=?";

        try(Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(invoice_query)) {
            stmt.setLong(1, getTrackId());
            stmt.executeUpdate();
        } catch (SQLException sqlException){
            throw new RuntimeException(sqlException);
        }


        String query = "DELETE FROM tracks WHERE TrackId=?";

        try(Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, getTrackId());
            stmt.executeUpdate();
        } catch (SQLException sqlException){
            throw new RuntimeException(sqlException);
        }
    }

    @Override
    public boolean update() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE tracks SET AlbumId=?, MediaTypeId=?, GenreId=?, Name=?, Milliseconds=?, Bytes=?, UnitPrice=? WHERE TrackId=?")) {
                stmt.setLong(1, getAlbumId());
                stmt.setLong(2, getMediaTypeId());
                stmt.setLong(3, getGenreId());
                stmt.setString(4, getName());
                stmt.setLong(5, getMilliseconds());
                stmt.setLong(6, getBytes());
                stmt.setBigDecimal(7, getUnitPrice());
                stmt.setLong(8, getTrackId());
                int update = stmt.executeUpdate();
                return update == 1;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }
}