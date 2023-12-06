package edu.montana.csci.csci440.helpers;

import edu.montana.csci.csci440.model.Employee;

import java.util.*;

public class EmployeeHelper {

    public static String makeEmployeeTree() {
        Employee employee = Employee.find(1); // root employee
        Map<Long, List<Employee>> employeeMap = new HashMap<>();

        Employee.all().forEach(e -> {
            long reports = e.getReportsTo();
            List<Employee> reportsTo = (employeeMap.get(reports) != null) ? employeeMap.get(reports) : new LinkedList<>();
            employeeMap.putIfAbsent(e.getReportsTo(), reportsTo);
            reportsTo.add(e);
        });

        return "<ul>" + makeTree(Objects.requireNonNull(employee), employeeMap) + "</ul>";
    }

    public static String makeTree(Employee employee, Map<Long, List<Employee>> employeeMap) {
        String list = "<li><a href='/employees" + employee.getEmployeeId() + "'>"
                + employee.getEmail() + "</a><ul>";
        List<Employee> reports = employeeMap.get(employee.getEmployeeId());
        if(reports != null){
            for (Employee report : reports) {
                list += makeTree(report, employeeMap);
            }
        }
        return list + "</ul></li>";
    }
}