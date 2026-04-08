<div align="center">

# 🧠⚫ Othello AI Engine ⚪

### High-performance game-tree search, bitboards, and exact endgame solving in Oracle Java 26

<p>
  <img alt="Oracle JDK 26" src="https://img.shields.io/badge/Oracle%20JDK-26-F80000?style=for-the-badge&logo=oracle&logoColor=white" />
  <img alt="CLI Game" src="https://img.shields.io/badge/Interface-CLI-1F6FEB?style=for-the-badge&logo=windows-terminal&logoColor=white" />
  <img alt="Bitboards" src="https://img.shields.io/badge/Board-Bitboards-6F42C1?style=for-the-badge" />
  <img alt="Search" src="https://img.shields.io/badge/AI-Negamax%20%2B%20Alpha--Beta-0A7E8C?style=for-the-badge" />
  <img alt="Endgame Solving" src="https://img.shields.io/badge/Endgame-Exact%20Solving-D73A49?style=for-the-badge" />
</p>

<p>
  <strong>Playable from the terminal. Engineered like a serious board-game AI.</strong>
</p>

</div>

> [!IMPORTANT]
> This is not just a simple CLI board game. Under the hood, it is a compact adversarial AI system built with immutable state, fast bitwise board logic, pruning-heavy search, cached positions, and phase-aware evaluation.

## 👀 In One Minute

| Question | Answer |
| --- | --- |
| **What is it?** | A playable command-line Othello engine written in Oracle Java 26. |
| **Why does it feel high-tech?** | It uses bitboards, negamax search, alpha-beta pruning, move ordering, a transposition table, and exact late-game solving. |
| **Why is that impressive?** | Those are the same kinds of ideas used in serious turn-based game engines to search deeper and make stronger decisions faster. |
| **What can it do?** | `Human vs Human`, `Human vs AI`, `hint`, `moves`, `undo`, `help`, and `quit`. |
| **What makes the AI interesting?** | Four difficulty tiers, phase-aware evaluation, and a brutal `Godlike` mode that becomes much more exact near the endgame. |

## ✨ Why This Stands Out

- 🧠 **It is real AI engineering, not just menu logic.** Every move comes from adversarial search, pruning, heuristics, and cached analysis.
- ⚡ **It is performance-minded.** The board is stored as two 64-bit bitboards, which makes move generation compact and fast.
- 🎯 **It is both playable and explainable.** You can demo it live, then talk through the exact algorithms behind the decisions.
- 🧱 **It shows clean software design.** The code is separated into engine, AI, CLI, app entrypoint, and shared model layers.
- 🔥 **It scales from casual to scary.** `Easy` is approachable, while `Godlike` is designed to be genuinely hard to beat.

## 💼 Why This Repo Looks Strong on GitHub

- `Algorithms you can talk about:` negamax, alpha-beta pruning, transposition tables, killer moves, history heuristics, and heuristic evaluation.
- `Performance choices that sound serious:` bitboards, directional bit operations, iterative deepening, and exact endgame solving.
- `Software engineering depth:` immutable board state, clear package boundaries, and scriptable build/run/test workflows.
- `Demo value:` this is not just theory on a page; people can launch it and immediately feel the difference between difficulty levels.

## ⚡ Quick Start

You need Oracle JDK 26 installed.

Official docs:
- [Java 26 Documentation](https://docs.oracle.com/en/java/javase/26/)

Open a terminal in the project root and verify Java:

```powershell
java -version
javac -version
```

This project compiles with:

```powershell
javac --release 26
```

Build the project:

```powershell
.\build.ps1
```

Run the game:

```powershell
.\run.ps1
```

Run smoke tests:

```powershell
.\test.ps1
```

The smoke tests check opening legality, flip correctness, AI move legality across all difficulty modes, and forced-pass handling.

If PowerShell blocks local scripts, use:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
powershell -ExecutionPolicy Bypass -File .\run.ps1
powershell -ExecutionPolicy Bypass -File .\test.ps1
```

Batch wrappers are also included:

```bat
build.bat
run.bat
test.bat
```

## 🎮 What You Can Do

### Game Modes

- `Human vs Human` for local same-machine play
- `Human vs AI` with `Easy`, `Medium`, `Hard`, and `Godlike`
- a launcher menu with gameplay and rules options

### Controls

Enter moves with coordinates like:

- `D3`
- `C4`
- `F5`
- `H8`

Board symbols:

- `B` = black discs
- `W` = white discs
- `*` = legal moves
- `?` = current hint square
- `.` = empty squares that are not currently legal

In-game commands:

- `hint` = ask the engine for its recommended move
- `moves` = print the current legal move list
- `undo` = rewind one ply
- `help` = show rules and controls
- `quit` = return to the main menu

Black always moves first. If a player has no legal move, the engine automatically passes. The game ends when neither side can move.

## 🧠 AI Highlights

| 🔧 Component | 🚀 Why it matters |
| --- | --- |
| `Bitboard state model` | Stores the entire board in two `long` values for fast occupancy checks and move generation. |
| `Negamax search` | Reuses a clean minimax-style formulation for both players in a zero-sum game. |
| `Alpha-beta pruning` | Cuts away branches that cannot improve the current best result. |
| `Move ordering` | Searches strong candidate moves first so pruning becomes much more effective. |
| `Transposition table` | Reuses work for previously explored positions instead of recalculating them. |
| `Killer + history heuristics` | Pushes cutoff-causing or historically strong moves earlier in future searches. |
| `Phase-aware evaluator` | Changes what it values across opening, midgame, and endgame. |
| `Exact late-game solving` | Stops guessing late and pushes toward exact calculation as the board empties. |

## 📁 Project Layout

```text
src/main/java/com/othello
  app/        Application entrypoint
  ai/         Search engine, evaluation, transposition table, difficulty profiles
  cli/        Console renderer and interactive game controller
  engine/     Immutable board state and bitboard rule engine
  model/      Shared domain enums and records

src/test/java/com/othello/test
  OthelloTestRunner.java
```

## 🧪 Difficulty Profiles

| Difficulty | Search Style | Time Budget | Depth Ceiling | Personality |
| --- | --- | --- | --- | --- |
| `Easy` | iterative deepening | `125 ms` | `3` | tactical, lighter, intentionally a little less perfect |
| `Medium` | iterative deepening | `400 ms` | `5` | balanced and more positional |
| `Hard` | iterative deepening | `1250 ms` | `8` | serious search with stronger endgame transition |
| `Godlike` | iterative deepening | `3250 ms` | `12` | deepest search and the most aggressive exact late-game play |

## 🔬 Technical Deep Dive

<details>
<summary><strong>Architecture overview</strong></summary>

This project is a classical game-tree engine, not a machine-learning model. The AI strength comes from search quality, pruning efficiency, move ordering, cached positions, and evaluation design.

At a high level, the engine works like this:

1. Represent the position as two bitboards.
2. Generate all legal moves for the current side.
3. Search future positions with negamax.
4. Prune branches using alpha-beta bounds.
5. Try stronger candidate moves earlier to improve pruning.
6. Score non-terminal positions with a weighted evaluator.
7. Push toward exact solving as the endgame approaches.

</details>

<details>
<summary><strong>Bitboard representation</strong></summary>

The board is stored using two 64-bit integers:

- one `long` for black discs
- one `long` for white discs

Each bit maps to one square on the 8x8 board. That makes the representation fast, compact, and ideal for Othello because:

- occupancy checks become bit tests
- move generation becomes directional bit shifting
- flips can be resolved with low-level bitwise operations
- immutable state copies stay lightweight enough for search

The rule engine checks all eight directions:

- north
- south
- east
- west
- northeast
- northwest
- southeast
- southwest

</details>

<details>
<summary><strong>Search stack</strong></summary>

The search engine uses a negamax formulation of minimax plus several performance upgrades:

- `Alpha-beta pruning`
  Removes branches that cannot improve the current line.

- `Iterative deepening`
  Searches depth 1, then 2, then 3, and so on until the depth or time budget is reached.

- `Principal variation search behavior`
  Searches the first move with a full window, then uses narrower windows for later moves and re-searches only when needed.

- `Transposition table`
  Caches previously seen positions with depth and bound information.

- `Killer moves`
  Remembers cutoff-causing moves at a given ply and tries them earlier in similar future nodes.

- `History heuristic`
  Boosts moves that have repeatedly performed well across the search.

- `Late-game exact deepening`
  Increases search depth toward exact play when the number of empty squares gets low enough.

These optimizations are what make the stronger modes feel substantially smarter instead of just slower.

</details>

<details>
<summary><strong>Evaluation function</strong></summary>

When the search reaches a non-terminal position at its horizon, the evaluator blends multiple signals:

- `Disc differential`
- `Mobility`
- `Potential mobility`
- `Corner occupancy`
- `Frontier discs`
- `Stable edge presence`
- `Positional square weights`
- `X-square and C-square corner pressure`
- `Parity`

The weights are phase-aware. Opening and midgame positions lean more heavily on mobility, potential mobility, frontier safety, and positional control. As the board fills up, disc count and parity matter more.

</details>

<details>
<summary><strong>Why the design choices are strong</strong></summary>

- `Immutable board state`
  Makes the search logic easier to reason about and safer to extend.

- `Bitboards over arrays`
  Lets the engine use compact, low-level operations where Othello benefits most.

- `CLI over GUI`
  Keeps the focus on the engine, algorithms, and game flow instead of UI overhead.

- `Script-based workflow`
  Makes the project easy to build, run, and test with a minimal setup.

</details>

<details>
<summary><strong>Troubleshooting</strong></summary>

- `PowerShell says scripts are disabled`
  Use `powershell -ExecutionPolicy Bypass -File .\run.ps1`

- `java` or `javac` is not recognized
  Make sure Oracle JDK 26 is installed and added to `PATH`.

- `The game does not start from the right folder`
  Open the terminal inside the project root before running scripts.

- `I want to verify the installation`
  Run `.\build.ps1` and then `.\test.ps1`.

</details>

## 🏁 Summary

This project takes a familiar board game and turns it into a compact showcase of Java architecture, algorithmic thinking, and classical AI engineering. It is easy to run, easy to demo, and technically rich enough to talk about search, heuristics, optimization, and clean systems design with confidence.
