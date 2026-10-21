package com.seattrack.web;

import com.seattrack.dao.BookingDao;
import com.seattrack.model.RegistrationView;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@WebServlet("/workshops")
public class WorkshopCatalogServlet extends HttpServlet {
    private final BookingDao bookingDao = new BookingDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String department = request.getParameter("department");
            if (department != null && department.isBlank()) {
                department = null;
            }
            request.setAttribute("workshops", bookingDao.findWorkshops(department));
            request.setAttribute("departments", bookingDao.findDepartments());
            long count = bookingDao.countRegistrations();
            getServletContext().setAttribute("registrationCount", count);
            HttpSession session = request.getSession();
            request.setAttribute("sessionRegistrationIds", sessionRegistrationIds(session));
            request.setAttribute("registrations", sessionRegistrations(session));
            request.getRequestDispatcher("/WEB-INF/views/catalog.jsp").forward(request, response);
        } catch (SQLException error) {
            getServletContext().log("Failed to load workshop catalog.", error);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("errorMessage", "The catalog could not be loaded. Check the MySQL database connection.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private Set<Long> sessionRegistrationIds(HttpSession session) {
        Object value = session.getAttribute("registrationIds");
        if (value instanceof Set<?> ids) {
            Set<Long> typedIds = new HashSet<>();
            for (Object id : ids) {
                if (id instanceof Long registrationId) {
                    typedIds.add(registrationId);
                }
            }
            return typedIds;
        }
        return Collections.emptySet();
    }

    private java.util.List<RegistrationView> sessionRegistrations(HttpSession session) {
        Object value = session.getAttribute("registrations");
        java.util.List<RegistrationView> registrations = new ArrayList<>();
        if (value instanceof java.util.List<?> values) {
            for (Object registration : values) {
                if (registration instanceof RegistrationView registrationView) {
                    registrations.add(registrationView);
                }
            }
        }
        session.setAttribute("registrations", registrations);
        return registrations;
    }
}
