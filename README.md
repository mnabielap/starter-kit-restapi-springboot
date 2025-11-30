# 🔥 Starter Kit: REST API with Spring Boot 🔥

![Java](https://img.shields.io/badge/Java-21-blue.svg) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg) ![Maven](https://img.shields.io/badge/Maven-4.0.0-red.svg) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg) ![SQLite](https://img.shields.io/badge/SQLite-07405E?style=flat&logo=sqlite&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)

Welcome to the Spring Boot REST API Starter Kit! 🚀 This project provides a solid, ready-to-use foundation for rapidly building modern RESTful APIs. It comes packed with essential features like JWT authentication, user management, API documentation, and full Docker support.

## ✨ Key Features

-   🔐 **Complete Security**: **JWT**-based authentication (Access & Refresh Tokens) and role-based authorization (USER & ADMIN) using Spring Security.
-   👤 **User Management (CRUD)**: Full operations to manage user data.
-   🔑 **Password Management**: "Forgot Password" and "Reset Password" features via email.
-   📧 **Email Verification**: A verification flow for new users.
-   🗄️ **Database Integration**: Utilizes Spring Data JPA with **PostgreSQL** or **SQLite**.
-   📚 **Automatic API Documentation**: Generated automatically with **Swagger UI** (OpenAPI).
-   ⚙️ **Centralized Error Handling**: Global Exception Handling for consistent error responses.
-   📄 **Pagination & Filtering**: Easy-to-use pagination, sorting, and data filtering for user lists.
-   🐳 **Docker Support**: Ready to be containerized with a provided `Dockerfile`.
-   🧪 **Built-in API Testing**: Includes API testing scripts with **Python** as a Postman alternative.

## 🛠️ Tech Stack

-   **Backend**: Java 21, Spring Boot 3.3.4, Spring Security, Spring Data JPA
-   **Database**: PostgreSQL / SQLite
-   **Build Tool**: Maven
-   **API Security**: JSON Web Tokens (JWT)
-   **Documentation**: Springdoc OpenAPI
-   **Deployment**: Docker
-   **Utilities**: Lombok

---

## 🚀 Getting Started

We recommend running this project in your local development environment first to understand its workflow. If you encounter difficulties with the local setup, the Docker method is an excellent alternative.

### 🏠 Method 1: Running in a Local Environment (Recommended)

#### ✔️ Prerequisites

1.  **Java Development Kit (JDK) 21** or newer.
2.  **Apache Maven**.
3.  **PostgreSQL** installed (required only if using PostgreSQL mode).
4.  Your preferred **IDE** (e.g., IntelliJ IDEA, VS Code, Eclipse).
5.  A **Mailtrap.io** account or another SMTP server for sending emails.

#### ⚙️ Configuration Steps

1.  **Clone the Repository**
    ```sh
    git clone https://github.com/mnabielap/starter-kit-restapi-springboot.git
    cd starter-kit-restapi-springboot
    ```

2.  **Set Up the Database**
    *   **Option A: SQLite (Easiest)**
        *   No external setup required. The application will create a local `.db` file.
    *   **Option B: PostgreSQL**
        *   Create a new database named `starter_kit_restapi_springboot_db`.
        *   Default credentials: Username: `postgres`, Password: `postgres`.

3.  **Configure the Application**
    -   Open the `src/main/resources/application.properties` file.
    -   **Select Database Profile**: Set `spring.profiles.active` to either `sqlite` or `postgres`.
      ```properties
      # Change to 'postgres' for PostgreSQL or 'sqlite' for SQLite
      spring.profiles.active=sqlite
      ```
    -   **IMPORTANT**: Update the SMTP configuration for email delivery. Replace `your_mailtrap_username` and `your_mailtrap_password` with your credentials.
      ```properties
      # Email (SMTP) Configuration
      spring.mail.host=smtp.mailtrap.io
      spring.mail.port=2525
      spring.mail.username=your_mailtrap_username
      spring.mail.password=your_mailtrap_password
      ```

4.  **Run the Application**
    -   You can run it using Maven:
      ```sh
      mvn spring-boot:run
      ```
    -   Alternatively, run it directly from your IDE by opening the `StarterKitRestapiSpringbootApplication.java` file and executing it.

5.  **Access the Application**
    -   🎉 Your application is now running at `http://localhost:3000`.
    -   The API documentation (Swagger UI) is available at: `http://localhost:3000/v1/docs`.

---

### 🐍 Using the Built-in API Tests (Postman Alternative)

This project comes with a set of Python scripts located in the `api_tests` directory to automatically test all API endpoints.

#### ▶️ How to Run the Tests

1.  Ensure your Spring Boot application is running (either locally or in Docker).
2.  Open a new terminal and navigate to the tests directory:
    ```sh
    cd api_tests
    ```
3.  Execute the script.
    ```sh
    python A1.auth_register.py
    ```
4.  You will see the output in the terminal for each test, including its success or failure status.

---

### 🐳 Method 2: Running with Docker

This is an alternative way to run the application without needing to manually install Java or PostgreSQL on your machine.

#### ✔️ Prerequisites

-   **Docker** installed and running.

#### ⚙️ Setup and Execution Steps

1.  **Create the `.env.docker` File**
    -   In your project's root directory, create a new file named `.env.docker`.
    -   Copy and paste the following content into it.
      ```env
      # Server Configuration
      SERVER_PORT=3000
      
      # Select Profile: 'postgres' or 'sqlite'
      SPRING_PROFILES_ACTIVE=postgres

      # Database Configuration (PostgreSQL in Docker)
      SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-db:5432/starter_kit_restapi_springboot_db
      SPRING_DATASOURCE_USERNAME=postgres
      SPRING_DATASOURCE_PASSWORD=postgres
      SPRING_JPA_HIBERNATE_DDL_AUTO=update

      # ... (copy the rest of the configuration from application.properties) ...
      # JWT, SpringDoc, and Email Config
      ```
      **Important Note**: `SPRING_DATASOURCE_URL` uses `postgres-db` as the hostname, which is the name of the database container we will create.

2.  **Build the Application Image**
    Open a terminal in the project root and run the Docker build command.
    ```sh
    docker build -t restapi-springboot-app .
    ```

3.  **Create Docker Network & Volumes**
    These commands only need to be run once. The network allows inter-container communication, and the volumes ensure your data persists.
    ```sh
    # Create the network
    docker network create restapi_springboot_network

    # Create the volume for the database (Persistent)
    docker volume create restapi_springboot_db_volume

    # Create the volume for media/uploads (if applicable)
    docker volume create restapi_springboot_media_volume
    ```

4.  **Run the PostgreSQL Container**
    ```sh
    docker run -d \
      --name postgres-db \
      --network restapi_springboot_network \
      -e POSTGRES_USER=postgres \
      -e POSTGRES_PASSWORD=postgres \
      -e POSTGRES_DB=starter_kit_restapi_springboot_db \
      -v restapi_springboot_db_volume:/var/lib/postgresql/data \
      postgres:16-alpine
    ```

5.  **Run the Spring Boot Application Container**
    This command will start your application and connect it to the database container.
    ```sh
    docker run -d -p 5005:3000 \
      --name restapi-springboot-container \
      --network restapi_springboot_network \
      --env-file .env.docker \
      -v restapi_springboot_media_volume:/app/uploads \
      restapi-springboot-app
    ```
    **Flag Explanation**:
    -   `-p 5005:3000`: Maps port 5005 on your host machine to port 3000 inside the container.
    -   `--env-file .env.docker`: Loads all environment variables from the file you created. **This is how you inject configuration into Docker.**
    -   `-v ...`: Maps a volume for media data persistence.

6.  **Access the Application (Docker Version)**
    -   🎉 Your application is now running at `http://localhost:5005`.
    -   The API documentation (Swagger UI) is available at: `http://localhost:5005/v1/docs`.

---

## 📋 Useful Docker Commands

Here are some commands to manage your containers.

#### Viewing logs from a running container
```sh
# To view logs in real-time
docker logs -f restapi-springboot-container
```

#### Stopping containers
```sh
docker stop restapi-springboot-container
docker stop postgres-db
```

#### Restarting existing containers
```sh
docker start restapi-springboot-container
docker start postgres-db
```

#### Removing containers (after stopping)
```sh
docker rm restapi-springboot-container
docker rm postgres-db
```

#### Listing existing volumes
```sh
docker volume ls
```

#### Removing a volume
> ⚠️ **WARNING**: This command will permanently delete your data! Use with extreme caution.
```sh
docker volume rm restapi_springboot_db_volume
```