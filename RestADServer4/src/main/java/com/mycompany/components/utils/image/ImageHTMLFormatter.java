package com.mycompany.components.utils.image;

import com.mycompany.components.serverModels.Image;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.List;

public class ImageHTMLFormatter {

    public static void printImages(HttpServletRequest req, List<Image> images, JspWriter out, String message) throws IOException {
        if (images.size() == 0) {
            out.println(message);
        } else {
            for (Image image : images) {
                out.println("<div>");
                out.println("<p id=\"fname\"> Title: " + image.getTitle() + "</p>");
                out.println("<img id=" + image.getTitle() + " src=\"data:image/jpeg;base64," + image.getBase64() + "\"/>");
                if (req.getSession().getAttribute("username").toString().equals(image.getCreator())) {
                    out.println("<a id=\"modify\" href=\"modifyimage\">Modify Image</a>");
                    out.println("<a id=\"delete\" href=\"deleteimage\">Delete Image</a>");
                }
                out.println("</div>");
            }
        }
    }
}
