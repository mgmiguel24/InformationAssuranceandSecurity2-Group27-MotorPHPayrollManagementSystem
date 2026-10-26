package com.motorph.controller;

import com.motorph.security.SessionManager;
import com.motorph.service.EmployeeManager;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Set;

/** GET /api/employees?q=keyword   (ADMIN, HR) */
public class EmployeeController extends BaseController {
    private final EmployeeManager employeeManager;
    private final SessionManager sessions;

    public EmployeeController(EmployeeManager employeeManager, SessionManager sessions) {
        this.employeeManager = employeeManager;
        this.sessions = sessions;
    }

    @Override
    protected void process(HttpExchange ex) throws IOException {
        requireMethod(ex, "GET");
        sessions.require(token(ex), Set.of("ADMIN", "HR"));
        String q = query(ex).getOrDefault("q", "");
        send(ex, 200, Json.employees(employeeManager.search(q)));
    }
}
