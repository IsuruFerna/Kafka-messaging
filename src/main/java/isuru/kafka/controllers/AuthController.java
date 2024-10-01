package isuru.kafka.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class AuthController {

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
