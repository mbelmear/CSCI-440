package edu.montana.csci.csci440.homework;

import edu.montana.csci.csci440.DBTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class Homework1 extends DBTest {

    @Test
    /*
     * Write a query in the string below that returns all artists that have an 'A' in their name
     */
    void selectArtistsWhoseNameHasAnAInIt(){
        List<Map<String, Object>> results = executeSQL(
                "SELECT name " +
                        "FROM artists " +
                        "WHERE name LIKE '%a%'");
        assertEquals(211, results.size());
    }

    @Test
    /*
     * Write a query in the string below that returns all artists that have more than one album
     */
    void selectAllArtistsWithMoreThanOneAlbum(){
        List<Map<String, Object>> results = executeSQL(
                "SELECT artists.Name, COUNT(albums.AlbumId) AS AlbumCount\n" +
                        "FROM artists\n" +
                        "JOIN albums ON artists.ArtistId = albums.ArtistId\n" +
                        "GROUP BY artists.Name\n" +
                        "HAVING AlbumCount > 1;");

        assertEquals(56, results.size());
        assertEquals("AC/DC", results.get(0).get("Name"));
    }

    @Test
        /*
         * Write a query in the string below that returns all tracks longer than six minutes along with the
         * album and artist name
         */
    void selectTheTrackAndAlbumAndArtistForAllTracksLongerThanSixMinutes() {
        List<Map<String, Object>> results = executeSQL(
                "SELECT tracks.Name as TrackName,\n" +
                        "       albums.Title as AlbumTitle,\n" +
                        "       artists.Name as ArtistsName\n" +
                        "FROM tracks\n" +
                        "JOIN albums on tracks.AlbumId = albums.AlbumId\n" +
                        "JOIN artists on albums.ArtistId = artists.ArtistId\n" +
                        "WHERE tracks.Milliseconds / 60000 >= 6;");

        assertEquals(623, results.size());
    }

}