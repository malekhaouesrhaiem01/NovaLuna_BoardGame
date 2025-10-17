Nova Luna Online

This project is a digital version of the board game Nova Luna by Uwe Rosenberg. It was developed as part of a software engineering project and focuses on implementing the full game logic, network communication, and bot integration for online play.

The goal was to reproduce the game’s mechanics accurately while allowing players to connect and play together over the network. The project also includes AI-controlled players that can act as opponents in single- or multi-player games.

Features

Online multiplayer support through a custom network client (NovaLunaNetworkClient)

Local and AI-controlled players

Core game logic implemented according to the official Nova Luna rules

Clear separation between layers (entity, service, GUI)

Fully tested with JUnit 5 and documented using KDoc

Technical Details

Language: Kotlin

Frameworks: BGW GUI and BGW Net Common

Architecture: Layered structure (Entity / Service / GUI) following SoPra conventions

Main class for networking: NovaLunaNetworkClient

Each class was designed based on activity and use case diagrams

How to Run

Clone the repository and open it in IntelliJ IDEA (or another IDE with Kotlin support):

git clone https://github.com/malekhaouesrhaiem01/NovaLuna_BoardGame.git


Then build and run the project:

./gradlew run

Notes

The main focus during development was the ai bots  and game logic and the network layer  implementation  and making sure the communication between clients worked reliably. Each method and class was tested for possible edge cases to make sure the game runs smoothly in different scenarios.
