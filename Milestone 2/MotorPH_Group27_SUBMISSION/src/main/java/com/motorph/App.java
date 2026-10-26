package com.motorph;

import com.motorph.config.DatabaseConfig;
import com.motorph.controller.AuthController;
import com.motorph.controller.EmployeeController;
import com.motorph.controller.PayrollController;
import com.motorph.controller.StaticController;
import com.motorph.dao.EmployeeDAO;
import com.motorph.dao.PayrollDAO;
import com.motorph.dao.UserDAO;
import com.motorph.security.SessionManager;
import com.motorph.service.AuthService;
import com.motorph.service.EmployeeManager;
import com.motorph.service.PayrollCalculator;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Entry point. Wires the three tiers together:
 *   Presentation (controller)  ->  Logic (service)  ->  Data Access (dao)  ->  MySQL
 */
public class App {
    public static void main(String[] args) throws Exception {
        DatabaseConfig config = DatabaseConfig.getInstance(); // loads .env exactly once

        UserDAO userDAO = new UserDAO();
        EmployeeDAO employeeDAO = new EmployeeDAO();
        PayrollDAO payrollDAO = new PayrollDAO();

        AuthService authService = new AuthService(userDAO);
        EmployeeManager employeeManager = new EmployeeManager(employeeDAO);
        PayrollCalculator payrollCalculator = new PayrollCalculator(employeeDAO, payrollDAO);
        SessionManager sessions = new SessionManager();

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", config.getAppPort()), 0);
        server.createContext("/", new StaticController());
        AuthController auth = new AuthController(authService, sessions);
        server.createContext("/api/login", auth);
        server.createContext("/api/logout", auth);
        server.createContext("/api/employees", new EmployeeController(employeeManager, sessions));
        server.createContext("/api/payroll", new PayrollController(payrollCalculator, sessions));
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("MotorPH Payroll running at http://localhost:" + config.getAppPort());
    }
}
