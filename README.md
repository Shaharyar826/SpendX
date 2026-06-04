# SpendX

A JavaFX desktop application for tracking shared expenses, managing friend groups, and settling balances.

## Features

- User registration and login with encrypted passwords
- Add and split expenses equally among friends
- Friend requests — send, accept, and reject
- Group management — create groups and add members
- Settlement tracking between friends
- Dashboard with balance overview and notifications
- Expense reports by category and month

## Prerequisites

Make sure the following are installed on your machine:

| Tool | Version | Download |
|---|---|---|
| JDK | 17 or higher | https://adoptium.net |
| Maven | 3.8 or higher | https://maven.apache.org/download.cgi |
| MySQL | 8.0 or higher | https://dev.mysql.com/downloads/installer |

> After installing Maven and JDK, make sure both are added to your system `PATH` and `JAVA_HOME` is set.

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/SpendX.git
cd SpendX
```

### 2. Set up the database

Open MySQL and run the schema file:

```bash
mysql -u root -p < sql/schema.sql
```

This creates the `spendx` database and all required tables.

### 3. Set environment variables

SpendX reads database credentials from environment variables. Run the following in Command Prompt (the values below are defaults — change them to match your MySQL setup):

```cmd
setx SPENDX_DB_PASS "your_mysql_password"
```

Optionally, if your MySQL URL or username differs from the defaults:

```cmd
setx SPENDX_DB_URL "jdbc:mysql://localhost:3306/spendx?useSSL=false&serverTimezone=UTC"
setx SPENDX_DB_USER "root"
```

> After running `setx`, restart your terminal or IDE for the variables to take effect.

### 4. Build and run

```bash
mvn clean javafx:run
```

## Project Structure

```
SpendX/
├── sql/
│   └── schema.sql              # Database schema
├── src/main/
│   ├── java/com/spendx/
│   │   ├── app/                # Entry point (MainApp.java)
│   │   ├── controller/         # JavaFX controllers
│   │   ├── dao/                # Database access layer
│   │   ├── model/              # Data models
│   │   └── util/               # DB connection, session, scene manager
│   └── resources/com/spendx/
│       ├── view/               # FXML UI files
│       └── css/                # Stylesheets
└── pom.xml
```

## Environment Variables Reference

| Variable | Default | Description |
|---|---|---|
| `SPENDX_DB_URL` | `jdbc:mysql://localhost:3306/spendx?useSSL=false&serverTimezone=UTC` | MySQL connection URL |
| `SPENDX_DB_USER` | `root` | MySQL username |
| `SPENDX_DB_PASS` | *(required)* | MySQL password |
