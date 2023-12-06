package edu.montana.csci.csci440.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InvoiceItem extends Model {

    Long invoiceLineId;
    Long invoiceId;
    Long trackId;
    BigDecimal unitPrice;
    Long quantity;
    String TrackName;
    String AlbumName;
    String ArtistName;
    Invoice invoice;

    public InvoiceItem(ResultSet resultSet) throws SQLException {
        invoiceLineId = resultSet.getLong("InvoiceLineId");
        trackId = resultSet.getLong("TrackId");
        unitPrice = resultSet.getBigDecimal("UnitPrice");
        quantity = resultSet.getLong("Quantity");
        TrackName = resultSet.getString("TrackName");
        AlbumName = resultSet.getString("AlbumName");
        ArtistName = resultSet.getString("ArtistName");
    }

    public Track getTrack() {
        return Track.find(trackId);
    }
    public Invoice getInvoice() {
        return invoice;
    }

    public Long getInvoiceLineId() {
        return invoiceLineId;
    }

    public void setInvoiceLineId(Long invoiceLineId) {
        this.invoiceLineId = invoiceLineId;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
}
