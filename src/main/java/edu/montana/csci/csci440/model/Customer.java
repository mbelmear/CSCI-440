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

public class Customer extends Model {

    private Long customerId;
    private Long supportRepId;
    private String firstName;
    private String lastName;
    private String email;

    public Employee getSupportRep() {
         return Employee.find(supportRepId);
    }

    public List<Invoice> getInvoices(){
        try {
            try (Connection connect = DB.connect();
                 PreparedStatement stmt = connect.prepareStatement(
                         "SELECT * FROM invoices WHERE CustomerId = ?")) {
                stmt.setLong(1, this.getCustomerId());  // Adjust the method name as needed

                ArrayList<Invoice> result = new ArrayList<>();
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    result.add(new Invoice(resultSet));
                }

                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching invoices for customer: " + this.getCustomerId(), e);
        }    }

    public Customer(){
    }
    private Customer(ResultSet results) throws SQLException {
        firstName = results.getString("FirstName");
        lastName = results.getString("LastName");
        customerId = results.getLong("CustomerId");
        supportRepId = results.getLong("SupportRepId");
        email = results.getString("Email");
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getSupportRepId() {
        return supportRepId;
    }

    public static List<Customer> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Customer> all(int page, int count) {
        int offset = (page - 1) * count;
        try {
            try(Connection connect = DB.connect();
                PreparedStatement stmt = connect.prepareStatement("SELECT * FROM customers LIMIT ? OFFSET ?")){
                ArrayList<Customer> result = new ArrayList<>();
                stmt.setInt(1, count);
                stmt.setInt(2, offset);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    result.add(new Customer(resultSet));
                }
                return result;
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }    }

    public static Customer find(long customerId) {
        try{
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT * FROM customers WHERE CustomerId = ?")) {
                stmt.setLong(1, customerId);
                ResultSet resultSet = stmt.executeQuery();
                if(resultSet.next()){
                    return new Customer(resultSet);
                }
                else{
                    return null;
                }
            }
        } catch(SQLException e){
            throw new RuntimeException(e);
        }    }

    public static List<Customer> forEmployee(long employeeId) {
        String query = "SELECT * FROM customers WHERE SupportRepId=?";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, employeeId);
            ResultSet results = stmt.executeQuery();
            List<Customer> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Customer(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }
}
