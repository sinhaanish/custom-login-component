package com.adobe.aem.guides.wknd.core.servlets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

@Component(
    service = Servlet.class,
    property = {
        ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_POST,
        ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/wknd/login",
        "service.description=" + "Login Servlet",
        "service.vendor=" + "WKND"
    }
)
public class LoginServlet extends SlingAllMethodsServlet {

    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class);
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {


        String username = request.getParameter("j_username");
        String password = request.getParameter("j_password");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonObject jsonResponse = new JsonObject();

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Username and password are required");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        String loginUrl = request.getScheme() + "://" + request.getServerName() + ":" +
                          request.getServerPort() + "/j_security_check";

        HttpPost loginPost = new HttpPost(loginUrl);
        List<BasicNameValuePair> formParams = new ArrayList<>();
        formParams.add(new BasicNameValuePair("j_username", username));
        formParams.add(new BasicNameValuePair("j_password", password));
        loginPost.setEntity(new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpClientContext context = HttpClientContext.create();
            CloseableHttpResponse loginResponse = httpClient.execute(loginPost, context);

            int statusCode = loginResponse.getStatusLine().getStatusCode();
            if (statusCode == 302) {
                // Success - set login-token if available
                List<Cookie> cookies = context.getCookieStore().getCookies();
                for (Cookie cookie : cookies) {
                    if ("login-token".equals(cookie.getName())) {
                        javax.servlet.http.Cookie servletCookie = new javax.servlet.http.Cookie(
                                cookie.getName(), cookie.getValue());
                        servletCookie.setPath("/");
                        servletCookie.setHttpOnly(true);
                        response.addCookie(servletCookie);
                    }
                }

                jsonResponse.addProperty("success", true);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Invalid username or password");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Server error: " + e.getMessage());
        }

        response.getWriter().write(jsonResponse.toString());
    }
} 