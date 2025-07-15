# Robot Simulator – Multithreaded Sensor System

## Overview

This project is a Java-based simulator for a robot equipped with multiple virtual sensors: **Camera**, **LiDAR**, and **GPS**. Each sensor operates as a separate microservice, running on its own thread, and communicates through a custom-built **Message Bus** system. The simulator was developed as part of a **System Programming** course and was awarded a grade of **91**.

## Key Features

- **Sensor Simulation**: Camera, LiDAR, and GPS sensors implemented as concurrent services.
- **Concurrent Communication**: Thread-safe Message Bus for inter-sensor messaging.
- **Time Synchronization**: A centralized time service ensures data from all sensors is aligned.
- **Fusion-SLAM Module**: Integrates LiDAR and Camera data to build a coherent map.
- **Multithreading**: Focus on synchronization and deadlock-free design using Java concurrency primitives.

## Architecture

- **Message Bus**: Custom concurrent publish-subscribe system for sensor communication.
- **Sensor Threads**: Each sensor runs independently and pushes data to the bus.
- **Fusion Service**: Consumes sensor data and performs SLAM-like logic.
- **Time Thread**: Coordinates synchronization across all components.

## Technical Highlights

- Language: **Java**
- Emphasis on thread safety and lock design
- Modular and scalable component-based system
- Debugging multi-threaded flows with logging and controlled timing

## Skills Demonstrated

- Multithreaded architecture
- Synchronization and shared-memory programming
- Modular design patterns and timing control
- Debugging concurrency issues in large systems

## Course Information

- **Course**: System Programming
- **Institution**: Ben-Gurion University of the Negev
- **Final Grade**: 91