# Smart Inventory Management System

A single-shop inventory platform for administrators and staff. The system combines product management, purchasing, point-of-sale billing, stock auditing, returns, reporting, staff management, and JWT authentication.

## Roles and Billing

The application has two roles:

- `ADMIN` can manage products, purchasing, categories, inventory, reports, returns, staff, and dashboard data. Administrators cannot access sales or billing.
- `STAFF` can view and search products and operate billing. Staff select products by ID, name, or SKU, enter quantities, and complete a sale. Selling prices are read from the database, stock is reduced transactionally, and the saved bill includes the cashier, customer, payment method, item subtotals, and total.

Registration accepts either `ADMIN` or `STAFF`. Existing legacy `USER` records are treated as `STAFF` when they authenticate. After checkout, staff can print the receipt or download a PDF copy from the billing screen. The PDF uses the jsPDF browser library loaded by the dashboard template.

## Technology

- Java 17
- Spring Boot 3.3.3
- Spring Web, Spring Data JPA, Hibernate
- MySQL
- Spring Security with JWT and BCrypt
- Maven
- Bootstrap dashboard UI

## Prerequisites

Install:

- JDK 17
- Maven
- MySQL Server

For Docker deployment, install Docker Desktop or Docker Engine with Compose.

## Database Setup

The application expects MySQL on `localhost:3306` and uses the database name `inventory_management_db`. The database is created automatically when the configured MySQL user has permission to create databases.

For local development, copy `.env.example` to `.env` and update the values, or set the same environment variables in your shell. The application keeps local `root`/`root` defaults for backward compatibility, but deployments should always use dedicated credentials.

```properties
SPRING_DATASOURCE_USERNAME=your_mysql_username
SPRING_DATASOURCE_PASSWORD=your_mysql_password
```

Hibernate is configured with `ddl-auto=update`, so the schema is updated from the JPA entities during development.

## Run the Application

From the project root:

```bash
mvn spring-boot:run
```

Open the dashboard at:

```text
http://localhost:8080/
```

To build and run the verification lifecycle:

```bash
mvn test
```

## Docker Deployment

1. Copy `.env.example` to `.env` and replace every `change-me` value.
2. Start the application and MySQL:

```bash
docker compose up --build -d
```

3. Open `http://localhost:8080/`.

Stop the deployment with:

```bash
docker compose down
```

The MySQL data is stored in the named `inventory-db` volume. For a production host, put a reverse proxy and HTTPS in front of the application, use a managed database where possible, and set strong secret values through the hosting provider's environment settings.

## Bulk Product Import

An administrator can import many products from the Products page using a CSV file. Download the template, fill one product per row, and upload it. The columns are:

```text
productName,sku,description,categoryId,brand,purchasePrice,sellingPrice,currentStock,minimumStock,maximumStock,expiryDate
```

Use database IDs for `categoryId`. Leave optional fields blank, and use `YYYY-MM-DD` for `expiryDate`. The import reports saved rows and row-level errors. Product creation, update, deletion, and bulk import are restricted to `ADMIN` accounts.

## Main API Groups

- `/api/auth` - login and authentication
- `/api/products` - product catalog and stock limits
- `/api/categories` - product categories
- `/api/customers` - customer records
- `/api/purchases` - purchase orders and stock-in
- `/api/sales` - POS sales and stock-out
- `/api/inventory` - stock movement history and low-stock records
- `/api/returns` - customer returns and stock adjustments
- `/api/dashboard` - operational summary cards
- `/api/reports` - sales, purchase, inventory, and customer reports

Most application APIs require a JWT obtained from the login endpoint.

## Project Structure

```text
src/main/java/com/inventory/
  controller/     REST endpoints
  service/        business rules and transactions
  repository/     Spring Data JPA repositories
  entity/         database entities
  dto/            API request and response contracts
  security/       JWT authentication
  config/         application configuration
```

## Completed Project Phases

1. Spring Boot and Maven foundation
2. Product and category management
3. Customer and staff management
4. POS sales and stock-out
5. Inventory transaction audit trail
6. Customer returns
7. Authentication and JWT authorization
8. Dashboard shell and summary API
9. Operational reporting APIs

## Verification Status

The current project builds successfully with `mvn test`. The CI workflow runs the same command on pushes and pull requests targeting `main`.
