package com.motorph.controller;

import com.motorph.dao.DataAccessException;
import com.motorph.service.AccessDeniedException;
import com.motorph.service.AuthenticationException;
import com.motorph.service.ValidationException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Presentation tier. This is the ONLY layer that builds HTTP responses.
 * DAOs and services throw exceptions; this class turns them into status codes:
 *   ValidationException     -> 400 Bad Request
 *   AuthenticationException -> 401 Unauthorized
 *   AccessDeniedException   -> 403 Forbidden
 *   DataAccessException     -> 500 (generic message, SQL details stay in server log)
 */
public abstract class BaseController implements HttpHandler {

    protected static final String SESSION_COOKIE = "MOTORPH_SESSION";

    @Override
    public final void handle(HttpExchange ex) throws IOException {
        try {
            addSecurityHeaders(ex);
            process(ex);
        } catch (ValidationException e) {
            send(ex, 400, Json.error(e.getMessage()));
        } catch (AuthenticationException e) {
            send(ex, 401, Json.error(e.getMessage()));
        } catch (AccessDeniedException e) {
            send(ex, 403, Json.error(e.getMessage()));
        } catch (DataAccessException e) {
            System.err.println("[ERROR] " + e.getMessage() + " :: " + e.getCause());
            send(ex, 500, Json.error("A server error occurred. Please try again later."));
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected: " + e);
            send(ex, 500, Json.error("A server error occurred. Please try again later."));
        } finally {
            ex.close();
        }
    }

    protected abstract void process(HttpExchange ex) throws IOException;

    protected void requireMethod(HttpExchange ex, String method) {
        if (!method.equalsIgnoreCase(ex.getRequestMethod())) {
            throw new ValidationException("Use HTTP " + method + " for this endpoint.");
        }
    }

    protected Map<String, String> query(HttpExchange ex) {
        return parse(ex.getRequestURI().getRawQuery());
    }

    protected Map<String, String> form(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            byte[] body = in.readNBytes(10_000); // cap request size
            return parse(new String(body, StandardCharsets.UTF_8));
        }
    }

    protected String token(HttpExchange ex) {
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) return auth.substring(7).trim();
        String cookie = ex.getRequestHeaders().getFirst("Cookie");
        if (cookie == null) return null;
        for (String part : cookie.split(";")) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2 && kv[0].equals(SESSION_COOKIE)) return kv[1];
        }
        return null;
    }

    protected void send(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void addSecurityHeaders(HttpExchange ex) {
        ex.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        ex.getResponseHeaders().set("X-Frame-Options", "DENY");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
    }

    private static Map<String, String> parse(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isEmpty()) return map;
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=", 2);
            String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(k, v);
        }
        return map;
    }
}
