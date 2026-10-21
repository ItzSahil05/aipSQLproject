<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Something went wrong | SeatTrack</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css">
</head>
<body>
<%@ include file="includes/header.jspf" %>
<main class="container narrow">
    <section class="panel">
        <p class="eyebrow">REQUEST ERROR</p>
        <h1>We could not complete that request</h1>
        <p role="alert"><c:out value="${errorMessage}"/></p>
        <a class="button" href="${pageContext.request.contextPath}/workshops">Return to catalog</a>
    </section>
</main>
<%@ include file="includes/footer.jspf" %>
</body>
</html>
