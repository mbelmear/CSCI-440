package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class Invoice extends Model {

    Long invoiceId;
    String billingAddress;
    String billingCity;
    String billingState;
    String billingCountry;
    String billingPostalCode;
    BigDecimal total;

    public Invoice() {
        // new employee for insert
    }

    Invoice(ResultSet results) throws SQLException {
        billingAddress = results.getString("BillingAddress");
        billingCity = results.getString("BillingCity");
        billingState = results.getString("BillingState");
        billingCountry = results.getString("BillingCountry");
        billingPostalCode = results.getString("BillingPostalCode");
        total = results.getBigDecimal("Total");
        invoiceId = results.getLong("InvoiceId");
    }

    public List<InvoiceItem> getInvoiceItems(){
        try{
            try(Connection connect = DB.connect();
                PreparedStatement stmt = connect.prepareStatement("SELECT tracks.Name AS TrackName, tracks.TrackId AS TrackId, " +
                        "albums.Title AS AlbumName, " +
                        "artists.Name AS ArtistName, " +
                        "invoice_items.InvoiceLineId AS InvoiceLineId, " +
                        "invoice_items.UnitPrice AS UnitPrice, " +
                        "invoice_items.Quantity AS Quantity " +
                        "FROM invoices " +
                        "JOIN invoice_items ON invoices.InvoiceId = invoice_items.InvoiceLineId " +
                        "JOIN tracks ON invoice_items.TrackId = tracks.TrackId " +
                        "JOIN albums ON tracks.AlbumId = albums.AlbumId " +
                        "JOIN artists ON albums.ArtistId = artists.ArtistId " +
                        "WHERE invoices.InvoiceId = ?")) {
                stmt.setLong(1, this.getInvoiceId());
                ArrayList<InvoiceItem> result = new ArrayList<>();
                ResultSet resultSet = stmt.executeQuery();
                while(resultSet.next()){
                    result.add(new InvoiceItem(resultSet));
                }
                return result;
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
    public Customer getCustomer() {
        return null;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getBillingCity() {
        return billingCity;
    }

    public void setBillingCity(String billingCity) {
        this.billingCity = billingCity;
    }

    public String getBillingState() {
        return billingState;
    }

    public void setBillingState(String billingState) {
        this.billingState = billingState;
    }

    public String getBillingCountry() {
        return billingCountry;
    }

    public void setBillingCountry(String billingCountry) {
        this.billingCountry = billingCountry;
    }

    public String getBillingPostalCode() {
        return billingPostalCode;
    }

    public void setBillingPostalCode(String billingPostalCode) {
        this.billingPostalCode = billingPostalCode;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public static List<Invoice> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Invoice> all(int page, int count) {
        int offset = (page - 1) * count;
        try {
            try(Connection connect = DB.connect();
                PreparedStatement stmt = connect.prepareStatement("SELECT * FROM invoices LIMIT ? OFFSET ?")){
                ArrayList<Invoice> result = new ArrayList<>();
                stmt.setInt(1, count);
                stmt.setInt(2, offset);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    result.add(new Invoice(resultSet));
                }
                return result;
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public static Invoice find(long invoiceId) {
        try{
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT * FROM invoices WHERE InvoiceId = ?")) {
                stmt.setLong(1, invoiceId);
                ResultSet resultSet = stmt.executeQuery();
                if(resultSet.next()){
                    return new Invoice(resultSet);
                }
                else{
                    return null;
                }
            }
        } catch(SQLException e){
            throw new RuntimeException(e);
        }
    }
}
