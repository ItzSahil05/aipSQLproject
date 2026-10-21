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
import java.util.Set;
import java.util.List;

@WebServlet("/cancel")
public class CancelRegistrationServlet extends HttpServlet {
    private final BookingDao bookingDao = new BookingDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("registrationIds") instanceof Set<?> rawIds)) {
            response.sendRedirect(request.getContextPath() + "/workshops");
            return;
        }
        long registrationId;
        try {
            registrationId = Long.parseLong(request.getParameter("registrationId"));
        } catch (NumberFormatException | NullPointerException error) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid registration ID.");
            return;
        }
        Set<Long> registrationIds = longSet(session, "registrationIds");
        if (!registrationIds.contains(registrationId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You can only cancel a registration from this session.");
            return;
        }
        try {
            BookingDao.CancelResult result = bookingDao.cancel(registrationId);
            if (result.cancelled()) {
                registrationIds.remove(registrationId);
                longSet(session, "workshopIds").remove(result.workshopId());
                sessionRegistrations(session).removeIf(registration ->
                        registration.registrationId() == registrationId);
                getServletContext().setAttribute("registrationCount", result.registrationCount());
                response.sendRedirect(request.getContextPath() + "/workshops?cancelled=true");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/workshops?error=registration-not-found");
        } catch (SQLException error) {
            getServletContext().log("Failed to cancel a registration.", error);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("errorMessage", "The cancellation could not be completed. Check the MySQL database connection.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private Set<Long> longSet(HttpSession session, String attribute) {
        Object value = session.getAttribute(attribute);
        Set<Long> ids = new java.util.HashSet<>();
        if (value instanceof Set<?> values) {
            for (Object id : values) {
                if (id instanceof Long longId) {
                    ids.add(longId);
                }
            }
        }
        session.setAttribute(attribute, ids);
        return ids;
    }

    private List<RegistrationView> sessionRegistrations(HttpSession session) {
        Object value = session.getAttribute("registrations");
        java.util.ArrayList<RegistrationView> registrations = new java.util.ArrayList<>();
        if (value instanceof List<?> values) {
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
