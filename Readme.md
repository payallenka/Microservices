# Microservices Learning Project

This project is a Spring Boot-based microservices application that includes an Eureka server for service discovery, along with client and frontend components. It demonstrates the workings of microservices and CRUD operations on APIs, utilizing technologies like Vaadin for the frontend.

## Overview

The application consists of multiple microservices that communicate with each other through an Eureka server for service discovery. Key components include:

- Eureka Server: For registering and discovering microservices.
- Client Service: Implements business logic and CRUD operations.
- Frontend: A user interface built with Vaadin for a seamless user experience.

## Installation

1. Clone the Repository:

   ```bash
   git clone https://github.com/yourusername/your-repo.git
   cd your-repo

2. Build the Server
   ```bash
   cd server
   mvn clean install package

3. Build the Client
   ```bash
   cd ../client
   mvn clean install package
   
4. Build the Frontend
   ```bash
   cd ../frontend
   mvn clean install package

5. Navigate to the main directory
   ```bash
   cd ..

6. Start the Application with Docker Compose
   ```bash
   docker-compose up --build

## Usage
Once the application is running, you can explore the following:

 - CRUD Operations: Interact with the client service's APIs to perform create, read, update, and delete operations.
 - Eureka Dashboard: Monitor registered services and their statuses through the Eureka server.
 - Frontend: Utilize the Vaadin interface to interact with the services.

## Contributing
We welcome contributions! Please fork the repository and create a pull request with your changes. Ensure that your code adheres to the project's coding standards and includes relevant tests.
