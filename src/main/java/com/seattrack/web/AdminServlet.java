package com.seattrack.web;

import com.seattrack.dao.BookingDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    private final BookingDao bookingDao = new BookingDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String table = request.getParameter("table");
        if (table == null || table.isBlank()) {
            table = "WORKSHOPS";
        }
        if (!table.equals("WORKSHOPS") && !table.equals("REGISTRATIONS")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Choose WORKSHOPS or REGISTRATIONS.");
            return;
        }
        try {
            request.setAttribute("selectedTable", table);
            request.setAttribute("columns", bookingDao.readAdminColumns(table));
            request.setAttribute("rows", bookingDao.readAdminTable(table));
            request.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(request, response);
        } catch (SQLException error) {
            getServletContext().log("Failed to read the selected admin table.", error);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("errorMessage", "The table could not be read from MySQL.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
}
