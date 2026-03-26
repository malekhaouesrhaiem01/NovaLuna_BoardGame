# Nova Luna Online – Kotlin Board Game

A digital version of the board game **Nova Luna** by Uwe Rosenberg, developed in Kotlin at TU Dortmund.
Features online multiplayer, AI bots (easy + hard), and a modular layered architecture.

---

## Screenshots

![Main Menu](https://github.com/user-attachments/assets/00b0e5df-6cf6-43d8-8f09-3da6418c25ea)

![Player Setup](https://github.com/user-attachments/assets/bb207743-8e97-46d5-8bfe-1dec463b0700)

![Gameplay](https://github.com/user-attachments/assets/fcc1f2bb-0e30-4660-be4c-f422f34b909c)

---

## Features

- online multiplayer via custom `NovaLunaNetworkClient`
- AI-controlled players (easy + hard difficulty)
- Full game logic based on official Nova Luna rules
- Save & load game support
- Clean layered architecture: Entity → Service → GUI
- Fully tested with JUnit 5 and documented with KDoc

---

## Tech Stack

`Kotlin` `BGW GUI` `BGW Net Common` `Gradle` `JUnit 5` `KDoc`

---

## How to Run

```bash
git clone https://github.com/malekhaouesrhaiem01/NovaLuna_BoardGame.git
cd NovaLuna_BoardGame
./gradlew run
```

Open in IntelliJ IDEA or any IDE with Kotlin support.

---

## Architecture

The project follows the SoPra layered structure:

| Layer | Responsibility |
|---|---|
| **Entity** | Data models and game state |
| **Service** | Game logic and network communication |
| **GUI** | BGW-based user interface |

Each class was designed based on activity and use case diagrams.
The main networking class is `NovaLunaNetworkClient`.

---

## Testing

```bash
./gradlew test
```

All entity and service classes are covered by JUnit 5 tests, including edge cases for game logic and network communication.
