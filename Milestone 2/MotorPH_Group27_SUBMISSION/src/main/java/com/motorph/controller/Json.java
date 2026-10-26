package com.motorph.controller;

import com.motorph.model.Employee;
import com.motorph.model.Payslip;

import java.util.List;
import java.util.stream.Collectors;

/** Tiny JSON writer so the project needs no extra libraries. */
final class Json {
    private Json() { }

    static String str(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '<' -> sb.append("\\u003c");
                case '>' -> sb.append("\\u003e");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }

    static String error(String message) {
        return "{\"error\":" + str(message) + "}";
    }

    static String employee(Employee e) {
        return "{\"employeeId\":" + e.getEmployeeId()
                + ",\"name\":" + str(e.getFullName())
                + ",\"position\":" + str(e.getPosition())
                + ",\"department\":" + str(e.getDepartment())
                + ",\"basicSalary\":" + e.getBasicSalary().toPlainString()
                + ",\"hourlyRate\":" + e.getHourlyRate().toPlainString() + "}";
    }

    static String employees(List<Employee> list) {
        return "[" + list.stream().map(Json::employee).collect(Collectors.joining(",")) + "]";
    }

    static String payslip(Payslip p) {
        return "{\"employeeId\":" + p.employeeId()
                + ",\"employeeName\":" + str(p.employeeName())
                + ",\"periodStart\":" + str(p.periodStart().toString())
                + ",\"periodEnd\":" + str(p.periodEnd().toString())
                + ",\"hoursWorked\":" + p.hoursWorked().toPlainString()
                + ",\"basicPay\":" + p.basicPay().toPlainString()
                + ",\"allowances\":" + p.allowances().toPlainString()
                + ",\"grossPay\":" + p.grossPay().toPlainString()
                + ",\"sss\":" + p.sss().toPlainString()
                + ",\"philhealth\":" + p.philhealth().toPlainString()
                + ",\"pagibig\":" + p.pagibig().toPlainString()
                + ",\"withholdingTax\":" + p.withholdingTax().toPlainString()
                + ",\"netPay\":" + p.netPay().toPlainString() + "}";
    }

    static String payslips(List<Payslip> list) {
        return "[" + list.stream().map(Json::payslip).collect(Collectors.joining(",")) + "]";
    }
}
