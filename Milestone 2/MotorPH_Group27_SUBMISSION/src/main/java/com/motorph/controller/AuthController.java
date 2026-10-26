package com.motorph.controller;

import com.motorph.model.User;
import com.motorph.security.SessionManager;
import com.motorph.service.AuthService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

/** POST /api/login, POST /api/logout */
public class AuthController extends BaseController {
    private final AuthService authService;
    private final SessionManager sessions;

    public AuthController(AuthService authService, SessionManager sessions) {
        this.authService = authService;
        this.sessions = sessions;
    }

    @Override
    protected void process(HttpExchange ex) throws IOException {
        requireMethod(ex, "POST");
        String path = ex.getRequestURI().getPath();

        if (path.endsWith("/logout")) {
            sessions.invalidate(token(ex));
            ex.getResponseHeaders().add("Set-Cookie", SESSION_COOKIE + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Strict");
            send(ex, 200, "{\"message\":\"Logged out.\"}");
            return;
        }

        Map<String, String> body = form(ex);
        User user = authService.login(body.get("username"), body.get("password"));
        String token = sessions.create(user);
        ex.getResponseHeaders().add("Set-Cookie",
                SESSION_COOKIE + "=" + token + "; Path=/; HttpOnly; SameSite=Strict; Max-Age=1800");
        send(ex, 200, "{\"message\":\"Login successful.\",\"username\":" + Json.str(user.getUsername())
                + ",\"role\":" + Json.str(user.getRole())
                + ",\"employeeId\":" + user.getEmployeeId()
                + ",\"token\":" + Json.str(token) + "}");
    }
}
