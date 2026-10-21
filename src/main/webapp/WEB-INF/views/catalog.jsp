<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Workshops | SeatTrack</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css">
</head>
<body>
<%@ include file="includes/header.jspf" %>
<main class="container">
    <section class="hero">
        <p class="eyebrow">COLLEGE EVENTS</p>
        <h1>Find your next workshop</h1>
        <p>Browse guest lectures and workshops, reserve a seat, and keep your confirmation ticket.</p>
    </section>

    <c:if test="${not empty registrationError}">
        <div class="notice error" role="alert"><c:out value="${registrationError}"/></div>
    </c:if>
    <c:if test="${param.error eq 'sold-out'}">
        <div class="notice error" role="alert">That workshop just sold out. Choose another event.</div>
    </c:if>
    <c:if test="${param.cancelled eq 'true'}">
        <div class="notice success" role="status">Registration cancelled and the seat is available again.</div>
    </c:if>
    <c:if test="${param.error eq 'registration-not-found'}">
        <div class="notice error" role="alert">That registration was already cancelled or no longer exists.</div>
    </c:if>

    <section class="panel">
        <div class="section-heading">
            <div>
                <p class="eyebrow">AVAILABLE EVENTS</p>
                <h2>Workshop catalog</h2>
            </div>
            <form method="get" action="${pageContext.request.contextPath}/workshops" class="filter">
                <label for="department">Department</label>
                <select id="department" name="department">
                    <option value="">All departments</option>
                    <c:forEach var="department" items="${departments}">
                        <option value="<c:out value='${department}'/>"
                                <c:if test="${param.department eq department}">selected</c:if>>
                            <c:out value="${department}"/>
                        </option>
                    </c:forEach>
                </select>
                <button class="button secondary" type="submit">Filter</button>
            </form>
        </div>

        <c:choose>
            <c:when test="${empty workshops}">
                <p class="empty-state">No workshops match this department yet.</p>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table>
                        <thead>
                        <tr>
                            <th>Workshop</th>
                            <th>Department</th>
                            <th>Seats left</th>
                            <th>Reserve</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="workshop" items="${workshops}">
                            <tr>
                                <td>
                                    <strong><c:out value="${workshop.title}"/></strong>
                                    <c:if test="${not empty workshop.description}">
                                        <span class="subtext"><c:out value="${workshop.description}"/></span>
                                    </c:if>
                                </td>
                                <td><c:out value="${workshop.department}"/></td>
                                <td>
                                    <span class="seat-count ${workshop.availableSeats eq 0 ? 'sold-out' : ''}">
                                        <c:out value="${workshop.availableSeats}"/> / <c:out value="${workshop.totalSeats}"/>
                                    </span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${workshop.availableSeats gt 0}">
                                            <form method="post" action="${pageContext.request.contextPath}/register" class="booking-form">
                                                <input type="hidden" name="workshopId" value="${workshop.workshopId}">
                                                <label class="visually-hidden" for="name-${workshop.workshopId}">Student name</label>
                                                <input id="name-${workshop.workshopId}" name="studentName" required maxlength="150"
                                                       placeholder="Your name" autocomplete="name">
                                                <label class="visually-hidden" for="email-${workshop.workshopId}">Student email</label>
                                                <input id="email-${workshop.workshopId}" name="studentEmail" type="email" required maxlength="254"
                                                       placeholder="you@college.edu" autocomplete="email">
                                                <button class="button" type="submit">Book seat</button>
                                            </form>
                                        </c:when>
                                        <c:otherwise><span class="sold-out-label">Sold out</span></c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <section class="panel">
        <div class="section-heading">
            <div>
                <p class="eyebrow">YOUR LIVE REGISTRATION DATA</p>
                <h2>Your registrations</h2>
            </div>
            <span class="muted">Bookings in this browser session</span>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                <tr><th>Student</th><th>Email</th><th>Workshop</th><th>Registered</th><th>Action</th></tr>
                </thead>
                <tbody>
                <c:forEach var="registration" items="${registrations}">
                    <tr>
                        <td><c:out value="${registration.studentName}"/></td>
                        <td><c:out value="${registration.studentEmail}"/></td>
                        <td><c:out value="${registration.workshopTitle}"/></td>
                        <td><c:out value="${registration.registeredAt}"/></td>
                        <td>
                            <c:if test="${sessionRegistrationIds.contains(registration.registrationId)}">
                                <form method="post" action="${pageContext.request.contextPath}/cancel">
                                    <input type="hidden" name="registrationId" value="${registration.registrationId}">
                                    <button class="button danger" type="submit">Cancel</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty registrations}">
                    <tr><td colspan="5" class="empty-state">No one has registered yet.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</main>
<%@ include file="includes/footer.jspf" %>
</body>
</html>
