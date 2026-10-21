# SeatTrack

SeatTrack is a Java 17, Jakarta Servlet/JSP college workshop booking application. JDBC and MySQL are used for live seat counts and registration records. Hibernate entity mappings are included for the syllabus relationship.

## 1. Create the database and app user

You can do this in DBeaver using an administrative MySQL connection (often `root`). Open a SQL editor and run the following. If `aip_user` already exists, skip `CREATE USER` and change its password if needed.

```sql
CREATE DATABASE IF NOT EXISTS seattrack_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'aip_user'@'localhost' IDENTIFIED BY 'choose-a-strong-password';
GRANT ALL PRIVILEGES ON seattrack_db.* TO 'aip_user'@'localhost';
```

Use a strong password rather than the example or any password shared in chat. If MySQL rejects the `utf8mb4_0900_ai_ci` collation, use a MySQL 8.x server or change it to a collation supported by your installed MySQL version.

Connect in DBeaver as `aip_user` to the `seattrack_db` database, then open and execute [`src/main/resources/db/schema.sql`](src/main/resources/db/schema.sql). This creates `WORKSHOPS` and `REGISTRATIONS` and inserts three sample workshops. The inserts are safe to run again; they won't add duplicate sample titles.

## 2. Configure SeatTrack

The app reads connection credentials from environment variables and does not store them in Java source:

```bash
export MYSQL_JDBC_URL='jdbc:mysql://localhost:3306/seattrack_db?allowPublicKeyRetrieval=true&useSSL=false'
export MYSQL_DB_USER='aip_user'
export MYSQL_DB_PASSWORD='your-strong-app-password'
```

Set these in the same terminal/session that starts Tomcat. The `useSSL=false` option is suitable only for a local development database; don't use it for a remote or production database.

## 3. Build and run

Build the deployable WAR:

```bash
mvn clean package
```

Deploy `target/seattrack.war` to Apache Tomcat 10.1 running on Java 17 or later, then open `http://localhost:8080/seattrack/`. Tomcat must be able to reach MySQL at `localhost:3306`.

## Features and syllabus mapping

- **GET and POST:** `GET /workshops` lists workshops and optionally filters by department. `POST /register` validates and creates a booking.
- **JDBC and transactions:** a row lock serializes seat bookings; registration insertion and seat decrement commit together. Cancel deletes a registration and restores its seat in one transaction.
- **Servlet collaboration:** a successful registration forwards to the `/ticket` confirmation servlet, which forwards to a JSP. A sold-out booking redirects to the catalog.
- **HttpSession:** records booked workshop and registration IDs for the active session; the same session cannot book the same workshop twice and can cancel its own session's booking.
- **JSP:** ticket receipt uses JSTL; shared header/footer use JSP static include directives. The footer reads `applicationScope.registrationCount`.
- **JDBC metadata:** `/admin` offers `WORKSHOPS` and `REGISTRATIONS`; column headers are read from `ResultSetMetaData`.
- **Hibernate:** [`Workshop`](src/main/java/com/seattrack/model/Workshop.java) and [`Registration`](src/main/java/com/seattrack/model/Registration.java) map a bidirectional `@OneToMany` / `@ManyToOne` relationship. Booking uses JDBC so the syllabus's transaction and metadata exercises are visible.
- **Database inspection:** use the Admin tables page or DBeaver to inspect the live rows.

The admin page is intended for a local class demonstration and is not protected by authentication. Add authentication and role-based authorization before exposing it beyond a trusted coursework environment.
