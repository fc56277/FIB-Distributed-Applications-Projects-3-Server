/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.components.utils.user;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 * @author alumne
 */
public class SessionVerifier {

    public static void verifySession(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {
        String[] urlTokens = req.getRequestURI().split("/");
        String message = "Must login in order to get access to " + urlTokens[urlTokens.length - 1];
        SessionVerifier.verifySession(req, resp, message, "index");
    }

    public static void verifySession(
            HttpServletRequest req,
            HttpServletResponse resp,
            String message,
            String failedResource
    ) throws ServletException, IOException {
        if (req.getSession().getAttribute("username") == null
                | req.getSession().getAttribute("password") == null) {
            req.setAttribute("message", message);
            req.getRequestDispatcher(failedResource).forward(req, resp);
        }
    }
}
