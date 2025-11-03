# Java Group Chat & Distributed System

This project is a robust, multi-threaded, group-based client-server communication system developed in Java. It features a distributed architecture where clients can join a central server, communicate in a group, and dynamically manage leadership through an automated 'Coordinator' role.

The system is built from the ground up, handling raw socket-level communication, concurrency, and client state management. It includes a user-friendly GUI built with Java Swing.



---

## 🚀 Key Features

* **Multi-Client Architecture:** The server is multi-threaded and can handle multiple client connections concurrently without blocking.
* **Dynamic Coordinator Role:** The first client to connect is automatically assigned the **Coordinator** role.
* **Automated Role Reassignment:** If the Coordinator disconnects, the server automatically promotes another client to the Coordinator role, ensuring the system's management functions are always available.
* **Unique ID Assignment:** The server ensures every client is assigned a distinct, unique ID upon connection.
* **Member List (Coordinator Only):** The Coordinator has an exclusive option to request and view a detailed list of all currently online users, including their ID, IP address, and port number.
* **Group Broadcasting:** All clients can send messages that are broadcast to every other connected client.
* **Direct/Private Messaging:** The architecture supports routing messages to specific clients (implementation of commands like "direct messaging" is facilitated).
* **User-Friendly GUI:** The client application features a clean and intuitive graphical user interface built with Java Swing.

---

## 🏗️ System Architecture

The system is divided into four primary modules, demonstrating a clear separation of concerns.

1.  **Server Module (`Server.java`)**
    * Acts as the central backbone of the system.
    * Listens on a specified port for new client connections.
    * Upon a new connection, it spins off a dedicated `ServerHandler` thread to manage that client, allowing the main server loop to remain non-blocking and scalable.

2.  **ServerHandler Module (`ServerHandler.java`)**
    * A dedicated thread instance is created for each connected client.
    * Manages all communication *for* that specific client (reading inputs, sending responses).
    * Processes incoming commands (e.g., broadcast, request details) and manages the client's lifecycle.
    * Handles the logic for the Coordinator mechanism, including assignment and reassignment.

3.  **Client Module (`Client.java`)**
    * The core client-side logic.
    * Manages the network connection to the server.
    * Runs a separate thread to continuously listen for incoming messages from the server, ensuring the GUI remains responsive.
    * Works in tandem with the `GUI` to process user input and display messages.

4.  **GUI Module (`GUI.java`)**
    * The complete user interface built using **Java Swing**.
    * Provides text fields for server details (IP, Port) and messages, a main text area for the chat history, and buttons for actions.
    * It is designed to be user-friendly and intuitive.

---

## 🛠️ Technologies & Design Principles

### Core Technologies
* **Language:** Java (JDK 11)
* **Networking:** Java Sockets
* **Concurrency:** Java Threads & `ThreadPoolExecutor`
* **User Interface:** Java Swing
* **Build:** Apache Maven
* **Testing:** JUnit & Mockito

### Implemented Design Patterns
This project leverages several fundamental design patterns for a clean, maintainable, and extensible codebase:

* **Singleton Pattern:** Used in the `Server` class to ensure only one instance of the server can be created, centralising connection and state management.
* **Observer Pattern:** The GUI acts as an observer to the `Client`. When the client's listener thread receives a new message (an event), it notifies the GUI, which then updates the chat display.
* **Strategy Pattern:** The `ServerHandler` uses a strategy-like approach (e.g., via `switch` or `if-else` blocks) to parse incoming messages and select the correct processing behaviour (e.g., handle broadcast, handle private message, handle coordinator request).
* **Factory Method Pattern:** Can be seen in the creation of message objects, encapsulating the logic for creating different message types.
* **Decorator Pattern:** Used implicitly in the Swing GUI, where components like `JTextArea` are wrapped by a `JScrollPane` to add scrolling functionality without altering the core component.

---

## ⚙️ Environment Setup & How to Run

### Prerequisites
* Java Development Kit (JDK) 11 or higher
* Apache Maven

### Running the System

1.  **Clone the Repository:**
    ```sh
    git clone [https://github.com/your-username/your-repository-name.git](https://github.com/your-username/your-repository-name.git)
    cd your-repository-name
    ```

2.  **Build the Project:**
    Use Maven to compile the project and handle dependencies. This will create a `.jar` file in the `target/` directory.
    ```sh
    mvn clean install
    ```

3.  **Step 1: Run the Server**
    You must start the server first. Run the `Server` main class from your IDE or using the compiled JAR.
    ```sh
    # Replace 'your-project.jar' with the name of the JAR in your /target folder
    # Replace 'com.example.Server' with your actual package and class name
    java -cp target/your-project.jar com.example.Server
    ```
    The server will start and log that it is waiting for connections.

4.  **Step 2: Run the Client(s)**
    With the server running, you can now launch one or more client instances.
    ```sh
    # Run the Client main class
    java -cp target/your-project.jar com.example.Client
    ```
    * The GUI will appear.
    * Enter the server's IP address (e.g., `127.0.0.1` for localhost) and the port the server is listening on.
    * The **first client** to connect will become the Coordinator.

---

## 🔬 Testing

The system's reliability is validated through a suite of unit tests using **JUnit** and **Mockito**.

* **`ClientTest`:** Mocks network and GUI interactions to test client-side logic, such as message formatting and connection attempts.
* **`ServerHandlerTest`:** Tests the logic for managing individual client connections, command processing, and broadcasting.
* **`ServerTest`:** Verifies the server's lifecycle (start, stop) and its ability to accept new connections.

---

## 📈 Future Enhancements

This project provides a strong foundation for several potential enhancements:

* **Advanced Security:** Integrate SSL/TLS to encrypt communication between clients and the server.
* **Heartbeat Mechanism:** Implement a client-side "ping" to the server to more gracefully detect and handle dead connections.
* **Persistent Storage:** Connect the server to a database to save chat history and user accounts.
* **Room-Based Chat:** Evolve the system to support multiple chat rooms or channels.
