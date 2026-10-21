package com.seattrack.web;

import com.seattrack.dao.BookingDao;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            long count = new BookingDao().countRegistrations();
            event.getServletContext().setAttribute("registrationCount", count);
        } catch (Exception error) {
            event.getServletContext().log("SeatTrack could not connect to MySQL during startup.", error);
        }
    }
}
