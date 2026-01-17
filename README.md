# Panta Store App

An inventory management desktop application for "Panta store" — built in Java with JavaFX. This project began as a tool to track inventory in a retail store and is planned to grow into a customer-facing system where customers can view inventory and later place pre-orders before visiting the store.

This README summarizes what is already implemented, how the code is organized, how to run the application in development, and recommended next steps to reach the planned features.

---

Table of contents
- About
- Primary features (what the code implements today)
- Project structure & key classes
- Development setup and run instructions
- Database configuration & migrations
- Recommended next steps / roadmap
- Tips, testing and troubleshooting
- Contributing & license

---

About

Panta Store App is a lightweight Java desktop app using JavaFX for the UI and a MySQL-backed database for persistence. It focuses on inventory tracking (products, quantities, cost/price, categories and timestamps) with an admin dashboard for adding/editing items and basic user/login scaffolding. The application creates its required tables automatically on first run.

This repository is intentionally set up to evolve:
1. Stage 1 (current) — inventory management for store staff.
2. Stage 2 — user login features so customers can view inventory.
3. Stage 3 — customer pre-orders (place orders online before visiting).

---

Primary features (implemented)

- JavaFX application entry point
  - HelloApplication launches the JavaFX app and loads the Login.fxml view.
  - At startup it calls DbMigrations.ensureTables(...) to create DB tables automatically.

- User login scaffolding
  - LoginController handles the login UI and delegates authentication to a DAO.
  - CurrentUser utility class holds the authenticated user in memory.

- Dashboard and product CRUD UI
  - DashboardController contains UI wiring for a product table, add/edit inline pane, refresh and logout.
  - Product table columns include id, name, category, expiry, price, cost, quantity and created timestamp.
  - Add / edit flows with validation helpers and handlers (onSaveAddItem, onCancelAddItem, openEditInline, showAddPane, etc.)

- Data access layer
  - DBUtil configures a HikariCP DataSource (connection pooling) with environment-variable-driven defaults.
  - DbMigrations contains SQL to create required tables (users, products). It's executed automatically on startup.
  - ProductDAO and UserDAO map ResultSet rows to model objects and provide queries to load/save items.

- Models
  - Product model (id, name, cat_name, exp_date, price, cost, quantity, created_at).
  - User model (username, first/last name, password hash fields) — present as a scaffold in the code.

- Utilities & developer conveniences
  - DBtest to validate DB connectivity from the classpath.
  - module-info.java configured for Java module system including JavaFX, HikariCP and jBCrypt (for password hashing).

---

Project structure (high level)

- src/main/java/
  - com.store.pantastoreapp.HelloApplication — JavaFX application entry.
  - com.store.pantastoreapp.controllers.* — LoginController, DashboardController, and related controllers.
  - com.store.pantastoreapp.db.* — DBUtil (Hikari pool), DbMigrations and DBtest.
  - com.store.pantastoreapp.Models.* — Product, User models.
  - com.store.pantastoreapp.Utils.* — ProductDAO, UserDAO, CurrentUser, SceneManager (scene switching helpers).
- src/main/resources/
  - FXML files (Login.fxml, Dashboard.fxml, etc.) and UI resources.
- module-info.java — module declarations and required modules.

---

Development setup

Requirements
- Java 17+ (or compatible JDK used for compilation / JavaFX runtime).
- JavaFX SDK matching your JDK version (if running from command line).
- MySQL server (or another compatible MySQL-compatible DB).
- An IDE that supports JavaFX (IntelliJ IDEA, Eclipse, or VS Code with Java extensions).

Database defaults (used by DBUtil)
The app reads DB configuration from environment variables with the following defaults:

- DB_HOST: 127.0.0.1
- DB_PORT: 3306
- DB_NAME: pantastore
- DB_USER: pantastoreuser
- DB_PASS: pantastorepass

The JDBC URL constructed in DBUtil:
jdbc:mysql://<DB_HOST>:<DB_PORT>/<DB_NAME>?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC

The application will attempt to create the necessary tables at startup (DbMigrations.ensureTables).

Run locally (recommended developer flow)

1. Make sure MySQL is running and a database (or user) matching the above defaults exists, or set DB_* env vars to match your local DB.

2. Import the project into your IDE as a Java project (Maven/Gradle support was not detected in the repository — see notes). If you use a build tool, add JavaFX and dependencies; otherwise run from your IDE after adding JavaFX to the runtime.

3. Build & run
   - From IDE: run com.store.pantastoreapp.HelloApplication.main()
   - From command line (example using `java` with module path):
     - Ensure JavaFX SDK is available and add it to module/path; example commands will vary by environment. The repository currently assumes a modular JVM setup (module-info.java present).

4. On first run the app will create `users` and `products` tables automatically.

Suggested Maven dependencies (conceptual)
- org.openjfx:javafx-controls / javafx-fxml
- com.zaxxer:HikariCP
- org.mindrot:jbcrypt (or similar)
- mysql:mysql-connector-java

---

Database migrations & schema

DbMigrations.ensureTables creates:
- users: id, username (unique), password_hash, first_name, last_name, created_at
- products: id, name, exp_date, price, cost, quantity, cat_name, created_at

Because the migration runs at application startup, a fresh database will be prepared automatically. For production, consider versioned migrations (Flyway or Liquibase) and avoid automatic schema changes at startup without administrator oversight.

---

Security & production notes

- Password hashing: module-info lists jbcrypt — confirm UserDAO uses bcrypt to store/verify passwords (this is best practice).
- Use prepared statements and parameterized queries (DAO code appears to use ResultSet mapping; confirm queries are using PreparedStatement to prevent SQL injection).
- Do not commit production credentials. Use environment variables in production (DB_*).
- Enable TLS between app and DB or run DB on a private network when deploying.
- Add logging configuration and exception monitoring in production.

---

Roadmap

Short-term (to complete Stage 1 reliably)
- Add a build file (Maven or Gradle) with dependencies and a run task.
- Add a docker-compose file to run MySQL for local development.
- Add a SQL schema export and optional seed data (a few products and an admin user).
- Harden validation on add/edit product forms and sanitize inputs.

Medium-term (Stage 2: customer login & inventory view)
- Implement role-based login/registration (customer vs staff).
- Build a read-only customer UI (web or desktop) to view product availability, price, and expected restock.
- Add search, filtering and pagination.

Long-term (Stage 3: pre-order / checkout)
- Add order model, order_items and a checkout flow for customers to place pre-orders.
- Integrate notifications (email/SMS) and order status updates.
- Add CSV/XLSX import/export for bulk product updates from supplier lists.
- Add audit logs for inventory adjustments and user actions.

Operational / business features
- Cost tracking: add landed-cost support (unit cost + import/shipping/taxes) to guide pricing.
- Reports: low-stock report, best-sellers, profit margins.
- Backup and recovery: scheduled DB backups and file storage.

---

Testing & troubleshooting

Common dev checks
- DB connection: you can run `DBtest` to confirm DB connectivity.
- If the UI fails to load FXML, confirm the resource path in HelloApplication and that FXML files are present under src/main/resources/FXML.
- If you see column-related exceptions in ProductDAO (e.g., missing exp_date), ensure the products table matches the migration SQL.

Manual test list before production use
- Add / edit product flows (including quantities, costs and expiry dates).
- Login and session handling.
- Database failure behavior (connection retries, app not crashing).
- Input validation (no negative prices/quantities).
- Password storage and verification (ensure bcrypt is used).

---

Contributing

- Open issues for new features (customer UI, orders, build tool, docker-compose).
- Create branches for features and submit pull requests with clear descriptions and testing steps.
- Add unit tests for DAO classes and integration tests for DB migrations.
