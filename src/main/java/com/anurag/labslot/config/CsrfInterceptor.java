package com.anurag.labslot.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.IOException;
import java.security.MessageDigest;

@Component
public class CsrfInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws IOException {
        String method = request.getMethod();
        if (method.equals("GET") || method.equals("HEAD") || method.equals("OPTIONS")) return true;
        HttpSession session = request.getSession(false);
        String expected = session == null ? null : (String) session.getAttribute("csrfToken");
        String provided = request.getHeader("X-CSRF-TOKEN");
        if (expected != null && provided != null && MessageDigest.isEqual(expected.getBytes(java.nio.charset.StandardCharsets.UTF_8), provided.getBytes(java.nio.charset.StandardCharsets.UTF_8))) return true;
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"Reload the page and retry the request.\",\"fieldErrors\":{}}");
        return false;
    }
}