<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Admin tables | SeatTrack</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css">
</head>
<body>
<%@ include file="includes/header.jspf" %>
<main class="container">
    <section class="hero compact">
        <p class="eyebrow">JDBC + RESULTSETMETADATA</p>
        <h1>MySQL table viewer</h1>
        <p>Select a table to inspect its live MySQL rows and metadata-driven columns.</p>
    </section>
    <section class="panel">
        <form method="get" action="${pageContext.request.contextPath}/admin" class="filter">
            <label for="table">Database table</label>
            <select id="table" name="table">
                <option value="WORKSHOPS" ${selectedTable eq 'WORKSHOPS' ? 'selected' : ''}>WORKSHOPS</option>
                <option value="REGISTRATIONS" ${selectedTable eq 'REGISTRATIONS' ? 'selected' : ''}>REGISTRATIONS</option>
            </select>
            <button class="button" type="submit">Show table</button>
        </form>
        <h2><c:out value="${selectedTable}"/></h2>
        <div class="table-wrap">
            <table>
                <thead><tr><c:forEach var="column" items="${columns}"><th><c:out value="${column}"/></th></c:forEach></tr></thead>
                <tbody>
                <c:forEach var="row" items="${rows}">
                    <tr>
                        <c:forEach var="column" items="${columns}" varStatus="loop">
                            <td><c:out value="${row[loop.index]}"/></td>
                        </c:forEach>
                    </tr>
                </c:forEach>
                <c:if test="${empty rows}">
                    <tr><td class="empty-state" colspan="${columns.size()}">This table has no rows yet.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</main>
<%@ include file="includes/footer.jspf" %>
</body>
</html>
