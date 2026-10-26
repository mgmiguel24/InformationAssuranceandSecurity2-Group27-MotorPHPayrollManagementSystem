package com.motorph.controller;

import com.motorph.model.User;
import com.motorph.security.SessionManager;
import com.motorph.service.AccessDeniedException;
import com.motorph.service.PayrollCalculator;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * POST /api/payroll/compute  (ADMIN, HR)
 * GET  /api/payroll/history?employeeId=  (ADMIN, HR any; EMPLOYEE own record only)
 */
public class PayrollController extends BaseController {
    private final PayrollCalculator payrollCalculator;
    private final SessionManager sessions;

    public PayrollController(PayrollCalculator payrollCalculator, SessionManager sessions) {
        this.payrollCalculator = payrollCalculator;
        this.sessions = sessions;
    }

    @Override
    protected void process(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();

        if (path.endsWith("/compute")) {
            requireMethod(ex, "POST");
            sessions.require(token(ex), Set.of("ADMIN", "HR"));
            Map<String, String> b = form(ex);
            send(ex, 200, Json.payslip(payrollCalculator.compute(
                    b.get("employeeId"), b.get("periodStart"), b.get("periodEnd"), b.get("hoursWorked"))));
            return;
        }

        if (path.endsWith("/history")) {
            requireMethod(ex, "GET");
            User user = sessions.require(token(ex), Set.of("ADMIN", "HR", "EMPLOYEE"));
            String requested = query(ex).getOrDefault("employeeId", "");
            if (user.getRole().equals("EMPLOYEE")
                    && !requested.trim().equals(String.valueOf(user.getEmployeeId()))) {
                throw new AccessDeniedException("Employees can only view their own payslips.");
            }
            send(ex, 200, Json.payslips(payrollCalculator.history(requested)));
            return;
        }

        send(ex, 404, Json.error("Not found."));
    }
}
