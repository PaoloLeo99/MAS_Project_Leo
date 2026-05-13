# Multi-Agent Autonomous Exploration System

![Java](https://img.shields.io/badge/Java-21+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=Gradle&logoColor=white)
![Jason](https://img.shields.io/badge/AgentSpeak-Jason-blue?style=for-the-badge)

## 📖 Overview
This project presents an implementation of a simulated 2D environment where a team of robotic agents coordinates to efficiently explore space, gather useful information, and locate hidden targets. The system simulates a research team composed of different types of robotic agents that communicate to accelerate the search process.

The agents are developed using Jason, a platform based on the BDI (Belief-Desire-Intention) architecture, which is well-suited for modeling autonomous and goal-driven behaviors.

## ✨ Key Features
* **BDI Architecture:** Agents base their reasoning on Beliefs, Desires, and Intentions using AgentSpeak/Jason.
* **Dynamic Pathfinding:** Implements the D* Lite algorithm for fast, incremental path planning in partially known environments.
* **Heterogeneous Agent Roles:**
  * **Ground Agents:** Units capable of interacting with the environment, investigating Points of Interest (POIs), and clearing obstacles.
  * **Air Agents:** Fast-moving drone units dedicated to rapid exploration and broadcasting map data to ground units.
* **MVC Pattern:** The system logic and graphical environment are structured using a robust Model-View-Controller architecture.
* **Decentralized Coordination:** Custom communication protocols minimize redundant POI interactions and optimize Task Time.

## 🛠️ Technologies
* **Java:** Core environment development, user interface, control logic, and internal actions.
* **AgentSpeak / Jason:** Open-source interpreter for implementing the BDI multi-agent system.
* **Gradle:** Build automation tool to manage dependencies and compilation.

## 🚀 Getting Started

### Prerequisites
* Java 21 or a more recent version.

### Installation & Execution
1. Clone the repository:
   ```bash
   git clone [https://github.com/PaoloLeo99/MAS_Project_Leo.git](https://github.com/PaoloLeo99/MAS_Project_Leo.git)
   cd MAS_Project_Leo
2. Run the application:
   Windows:
   ```bash
   gradlew.bat runExplorationTask

   macOS / Linux:
   ```bash
   ./gradlew runExplorationTask
