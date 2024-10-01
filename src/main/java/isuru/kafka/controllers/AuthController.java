package isuru.kafka.controllers;

import io.ably.lib.rest.AblyRest;
import io.ably.lib.rest.Auth;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.Capability;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.io.IOException;

@RestController
public class AuthController {
    private AblyRest ablyRest;

    @Value( "${ABLY_API_KEY}" )
    private void setAblyRest(String apiKey) throws AblyException {
        ablyRest = new AblyRest(apiKey);
    }

    /* Issue token requests to clients sending a request to the /auth endpoint */
    @RequestMapping("/auth")
    public String auth(HttpServletRequest request, HttpServletResponse response) throws AblyException {
        String username = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase("username")) {
                    username = cookie.getValue();
                    break;
                }
            }
        }
        Auth.TokenParams tokenParams = getTokenParams(username);
        return createTokenRequest(tokenParams, response);
    }

    public Auth.TokenParams getTokenParams(String username) throws AblyException {
        Auth.TokenParams tokenParams = new Auth.TokenParams();
        tokenParams.capability = Capability.c14n("{ '*': ['subscribe'] }");
        if (username != null) {
            tokenParams.clientId = username;
        }
        return tokenParams;
    }

    public String createTokenRequest(Auth.TokenParams tokenParams, HttpServletResponse response) {
        Auth.TokenRequest tokenRequest;
        try {
            tokenRequest = ablyRest.auth.createTokenRequest(tokenParams, null);
            response.setHeader("Content-Type", "application/json");
            return tokenRequest.asJson();
        } catch (AblyException e) {
            response.setStatus(500);
            return "Error requesting token: " + e.getMessage();
        }
    }

    /* set a cookie when the user logs in */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(@RequestParam(name = "username", defaultValue = "anonymouse") String username, HttpServletResponse response) throws IOException {
        /* login the user without credentials. This is an over simplified authentication system to keep this tutorial simple */
        response.addCookie(new Cookie("username", username));
        response.sendRedirect("/");
        return "redirect:/";
    }

    /* clear the cookie when the user logs out */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        for (Cookie cookie : request.getCookies()) {
            if(cookie.getName().equalsIgnoreCase("username")) {
                cookie.setValue(null);
                cookie.setMaxAge(0);
                cookie.setPath(request.getContextPath());
                response.addCookie(cookie);
            }
        }
        response.sendRedirect("/");
        return "redirect:/";
    }
}
