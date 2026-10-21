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
import java.util.HashSet;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final BookingDao bookingDao = new BookingDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String name = request.getParameter("studentName");
        String email = request.getParameter("studentEmail");
        String workshopIdText = request.getParameter("workshopId");
        long workshopId;
        if (name == null || name.isBlank() || name.trim().length() > 150
                || email == null || email.isBlank() || email.trim().length() > 254
                || !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || workshopIdText == null) {
            showCatalogError(request, response, "Enter a valid name and email, then choose a workshop.");
            return;
        }
        try {
            workshopId = Long.parseLong(workshopIdText);
            if (workshopId <= 0) {
                throw new NumberFormatException("Workshop ID must be positive.");
            }
        } catch (NumberFormatException error) {
            showCatalogError(request, response, "Choose a valid workshop.");
            return;
        }

        HttpSession session = request.getSession();
        Set<Long> bookedWorkshops = longSet(session, "workshopIds");
        if (bookedWorkshops.contains(workshopId)) {
            showCatalogError(request, response, "This session has already booked this workshop.");
            return;
        }

        try {
            BookingDao.BookingResult result = bookingDao.register(workshopId, name.trim(), email.trim().toLowerCase(Locale.ROOT));
            if (result.status() == BookingDao.BookingStatus.SOLD_OUT) {
                response.sendRedirect(request.getContextPath() + "/workshops?error=sold-out");
                return;
            }
            if (result.status() == BookingDao.BookingStatus.NOT_FOUND) {
                showCatalogError(request, response, "That workshop no longer exists.");
                return;
            }
            if (result.status() == BookingDao.BookingStatus.ALREADY_REGISTERED) {
                showCatalogError(request, response, "This email is already registered for that workshop.");
                return;
            }

            RegistrationView registration = result.registration();
            bookedWorkshops.add(workshopId);
            longSet(session, "registrationIds").add(registration.registrationId());
            sessionRegistrations(session).add(registration);
            getServletContext().setAttribute("registrationCount", result.registrationCount());
            request.setAttribute("registration", registration);
            request.setAttribute("registrations", sessionRegistrations(session));
            request.getRequestDispatcher("/ticket").forward(request, response);
        } catch (SQLException error) {
            getServletContext().log("Failed to register a student.", error);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("errorMessage", "Your registration could not be saved. Please try again after checking the database.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private void showCatalogError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("registrationError", message);
        request.getRequestDispatcher("/workshops").forward(request, response);
    }

    private Set<Long> longSet(HttpSession session, String attribute) {
        Object value = session.getAttribute(attribute);
        Set<Long> ids = new HashSet<>();
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
        List<RegistrationView> registrations = new ArrayList<>();
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
