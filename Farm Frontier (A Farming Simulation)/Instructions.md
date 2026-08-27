# 🌾 Farm Frontier

### A Text-Based Farm Management & Strategy Simulation

**Author:** Haiyang Guo
**Project:** ICS4U Final Project

---

## 📖 Description

**Farm Frontier** is a text-based farm management simulation game where the player takes the role of a **farm owner** responsible for managing a growing agricultural business.

The main objective is to:

* 💰 Keep the farm profitable
* 🏦 Avoid bankruptcy
* 🌱 Produce and sell resources
* 🏗️ Expand the farm
* 👷 Manage employees and animals
* 📋 Complete tasks
* 📈 Increase the farm's overall value

The game focuses more on **business, accounting, planning, and strategy** than on traditional arcade-style gameplay. Players must carefully read information, compare options, and plan ahead to keep their farm financially stable.

---

# 🎮 How to Play

Farm Frontier is controlled entirely through the **console**.

When the program starts, the player enters the main menu and selects options by typing the corresponding number and pressing **Enter**.

The game is organized into several sections, each handling a different aspect of farm management.

---

## 1. 📅 Start / Continue Day

This option displays a summary of the current day, including information such as:

* Current money
* Current day
* Storage usage
* Active tasks
* General advice

The player can then choose whether to advance to the next day.

### Advancing the Day

Advancing the day is one of the most important actions in the game.

When a new day begins, the game automatically:

* 🌱 Grows planted crops
* 🌾 Harvests ready crops and adds them to inventory
* 🐟 Produces fish if fish ponds exist
* 🐄 Produces animal products such as milk and eggs
* 💵 Deducts employee salaries
* 🏪 Adds supermarket income
* 📊 Updates market prices
* 🎲 Triggers a random event
* 📋 Checks task completion

Because advancing the day can both help and hurt the farm, check your **money, inventory, storage, and tasks** before continuing.

---

# 2. 🚜 Farm Management

The **Farm Management** section contains the main farming operations.

The player can view:

* Farm status
* Inventory
* Buildings
* Employees
* Animals

### 🌱 Planting Crops

The player can plant crops by choosing:

1. A crop type
2. The number of acres to plant

Planting costs money, but crops can later be harvested and added to the inventory.

Different crops have different:

* Seed costs
* Selling prices
* Growth times
* Yields

### 🐄 Feeding Animals

Animals require crops as feed.

Before feeding animals, check the available crop supply.

Properly feeding animals helps maintain production of resources such as:

* Milk
* Eggs

If animals are not properly fed, production may be reduced.

### 💰 Trading Animals

Animals can also be traded for money.

This can provide quick cash when the farm needs it, but selling too many animals can reduce future production.

The player therefore needs to balance **short-term income** with **long-term production**.

---

# 3. 🛒 Market / Trading

The **Market / Trading** section allows the player to:

* View current market prices
* Sell resources
* Manage the farm's income

Market prices change as days pass, so it is important to check prices before selling.

### 💡 Trading Strategy

Selling resources is the primary way to earn money.

However, do not automatically sell everything.

Some resources may be needed for:

* 🐄 Animal feed
* 📋 Tasks
* Future production

Before selling, consider:

* Current market prices
* Storage capacity
* Task requirements
* Future resource needs

### 🏪 Supermarket

If the player owns a **Supermarket**, resource sales become more profitable because the building increases sale revenue.

This makes building investments an important part of long-term strategy.

---

# 4. 🏗️ Buildings / Expansion

The **Buildings / Expansion** section allows the player to purchase buildings and place them on the farm map.

Each building provides a different benefit:

| Building           | Effect                       |
| ------------------ | ---------------------------- |
| 🐄 **Barn**        | Improves animal production   |
| 🐟 **Fish Pond**   | Improves fish production     |
| 🌱 **Greenhouse**  | Improves crop harvests       |
| 📦 **Warehouse**   | Increases storage capacity   |
| 🏪 **Supermarket** | Increases revenue from sales |

Buildings must be placed on **empty tiles** on the farm map.

## 🗺️ Farm Map

The map uses the following symbols:

| Symbol | Meaning     |
| ------ | ----------- |
| `E`    | Empty       |
| `B`    | Barn        |
| `P`    | Fish Pond   |
| `G`    | Greenhouse  |
| `W`    | Warehouse   |
| `S`    | Supermarket |

The map contains **7 × 7 tiles**.

Players enter coordinates using the row and column numbers displayed on the screen.

> **Note:** The player sees coordinates from **1–7**, while the program internally uses Java array indexes from **0–6**.

---

## 🔍 Map Search Tools

The farm map is not simply decorative. It also supports map-based planning.

### Breadth-First Search (BFS)

**BFS** is used to suggest the **nearest available building location**.

This can help players decide where to place a new building.

### Depth-First Search (DFS)

**DFS** is used to analyze connected empty areas of land.

This helps the player identify areas that may be useful for future expansion.

---

# 5. 📊 Reports

The **Reports** section provides detailed information about the current state of the farm.

Reports are useful for making financial and strategic decisions.

## 💰 Financial Report

The financial report includes information such as:

* Current money
* Inventory value
* Storage usage and capacity
* Employee expenses
* Land size
* Building counts
* Estimated net worth
* Warnings when money is low

## 📦 Inventory Report

The inventory report displays:

* Resource quantities
* Unit prices
* Total resource value

It may also warn the player when:

* Storage is nearly full
* Resources are running low

Since Farm Frontier is heavily focused on management and accounting, reports are an important tool for planning.

---

# 6. 💾 Save / Load

Farm Frontier includes a save/load system so players can preserve their progress.

## Save

Saving writes the current farm state to:

```text
save.txt
```

The save system stores important information including:

* Money
* Day number
* Land size
* Inventory
* Crops
* Employees
* Animals
* Feed levels
* Farm map layout

## Load

Loading reads information from `save.txt` and restores the previous farm state.

The program checks several possible file locations to make saving and loading more reliable across different computers and IDE setups.

> 💡 **Tip:** Save your game before exiting if you want to continue playing later.

---

# 7. ❓ Help / Instructions

The **Help** section explains the rules and purpose of the game.

It is especially useful for new players.

The Help section provides information about:

* The main objective
* How to earn money
* What each building does
* How tasks work
* How market prices work
* How to avoid bankruptcy
* Farm map symbols

---

# 8. 🚪 Exit

The **Exit** option ends the program.

> 💾 **Remember:** Save your game before exiting if you want to preserve your progress.

---

# 📋 Tasks

Tasks provide the player with objectives to complete.

Completing tasks provides **money rewards** and gives the player short-term goals while allowing freedom to manage the farm.

Tasks are stored in a **queue**, meaning the first task must be completed before later tasks can be completed.

---

# 🎲 Random Events

Random events occur when the day advances.

These events make the farm's economy less predictable.

Possible events include:

* 📈 Market booms
* 💰 Taxes
* 🌊 Flood damage
* ☀️ Good weather

Players must adapt their strategy to unexpected changes.

---

# 👷 Employees

Employees provide **production bonuses**, but they also create additional salary expenses.

The player must therefore consider whether the benefits of hiring employees justify their ongoing costs.

---

# 🌾 Farm Systems

Farm Frontier combines several interconnected systems:

### Inventory System

Stores crops, animal products, and fish products.

### Crop System

Allows players to plant crops by acre, wait for them to grow, and harvest them.

### Animal System

Manages livestock such as cows and chickens and their production of resources such as milk and eggs.

### Market System

Provides changing resource prices, requiring players to decide when selling is most profitable.

### Building System

Allows players to purchase facilities that affect production, storage, or income.

### Task System

Provides objectives and monetary rewards.

### Report System

Summarizes the farm's financial and operational status.

### Save/Load System

Allows the player to save and restore farm progress.

---

# 💡 Strategy Tips

There is no single strategy for succeeding in Farm Frontier, but the following principles are important.

### 💰 Manage Your Money

Always maintain enough money to handle:

* Employee salaries
* Unexpected negative events
* Necessary purchases

If money becomes too low, consider selling resources or trading animals instead of making expensive investments.

### 📦 Watch Your Storage

When storage becomes full, newly produced resources may be wasted.

Consider purchasing **Warehouses** when storage becomes a problem.

### 🏗️ Think Before Buying Buildings

Buildings are expensive but provide long-term benefits:

* **Greenhouse** → Better crop harvests
* **Fish Pond** → Better fish production
* **Barn** → Better animal production
* **Warehouse** → More storage
* **Supermarket** → Higher sales revenue

### 📋 Complete Tasks

Tasks provide direction and rewards.

Since tasks are organized in a queue, focus on completing the current task before moving on to later objectives.

### 📈 Plan Ahead

The farm's success depends on balancing:

```text
Production
    ↓
Inventory
    ↓
Market Prices
    ↓
Selling
    ↓
Income
    ↓
Investment
    ↓
Expansion
```

---

# 🏆 Objective & Success

Farm Frontier does not have one fixed **"win screen."**

Instead, success is measured by your ability to:

* 💰 Maintain a profitable farm
* 📈 Increase net worth
* 🌾 Expand production
* 🏗️ Develop the farm
* 📋 Complete tasks
* 🏦 Avoid bankruptcy

The ultimate challenge is to build a **stable and successful agricultural business**.

---

# 💻 Programming Concepts

The project combines several programming concepts into one integrated simulation, including:

* Object-Oriented Programming
* Arrays
* Inheritance
* Polymorphism
* Sorting
* Searching
* Stacks
* Queues
* Linked Lists
* Recursion
* File Input/Output
* User Interaction

---

## 🌟 Final Note

**Farm Frontier** is designed to challenge the player to think like a farm owner rather than simply react to individual events.

Read the available information, plan your investments, watch your finances, and make decisions that keep your farm moving forward.

### 🌾 Build. Produce. Trade. Expand.

**Welcome to Farm Frontier.**

