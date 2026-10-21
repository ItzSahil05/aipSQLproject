<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Booking confirmed | SeatTrack</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css">
</head>
<body>
<%@ include file="includes/header.jspf" %>
<main class="container narrow">
    <section class="ticket panel">
        <p class="eyebrow">BOOKING CONFIRMED</p>
        <h1>Your seat is reserved</h1>
        <p class="muted">Show this receipt at the workshop check-in desk.</p>
        <dl class="receipt">
            <dt>Ticket number</dt><dd>ST-<c:out value="${registration.registrationId}"/></dd>
            <dt>Student</dt><dd><c:out value="${registration.studentName}"/></dd>
            <dt>Email</dt><dd><c:out value="${registration.studentEmail}"/></dd>
            <dt>Workshop</dt><dd><c:out value="${registration.workshopTitle}"/></dd>
            <dt>Registered at</dt><dd><c:out value="${registration.registeredAt}"/></dd>
        </dl>
        <a class="button" href="${pageContext.request.contextPath}/workshops">Back to catalog</a>
    </section>
    <section class="panel">
        <p class="eyebrow">REGISTRATIONS TABLE</p>
        <h2>Your session registrations</h2>
        <div class="table-wrap">
            <table>
                <thead><tr><th>Registration ID</th><th>Student</th><th>Email</th><th>Workshop</th><th>Registered at</th></tr></thead>
                <tbody>
                <c:forEach var="booking" items="${registrations}">
                    <tr>
                        <td><c:out value="${booking.registrationId}"/></td>
                        <td><c:out value="${booking.studentName}"/></td>
                        <td><c:out value="${booking.studentEmail}"/></td>
                        <td><c:out value="${booking.workshopTitle}"/></td>
                        <td><c:out value="${booking.registeredAt}"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </section>
</main>
<%@ include file="includes/footer.jspf" %>
</body>
</html>
