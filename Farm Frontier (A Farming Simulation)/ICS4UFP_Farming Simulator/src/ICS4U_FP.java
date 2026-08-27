/*
 * FARM FRONTIER - A farming simulation game
 * STUDENT NAME: Henry
 * COURSE CODE: ICS4U
 * DATE: June 2026
 *
 * This program models a small farm economy where the player manages crops,
 * animals, buildings, inventory, market trading, tasks, and random events.
 * It includes a full game loop with day advancement, save/load persistence,
 * and a command-driven menu for farming decisions.
 *
 * Core concepts demonstrated:
 * - Object-oriented design with classes, inheritance, and polymorphism
 * - Linear data structures (arrays, stacks, queues, linked lists)
 * - Algorithms for searching, sorting, and map traversal
 * - File I/O for saving/loading game state
 */

import java.io.*;
import java.util.*;

/**
 * Tile represents a single cell on the farm map using a character symbol.
 *
 * PRE:  symbol must be a valid map tile indicator such as E, B, P, G, W, or S.
 * POST: The tile can be queried and updated as buildings are placed.
 *
 * COURSE CONCEPT: Encapsulation of map state in a simple object.
 */
class Tile {
	private char symbol;

	public Tile(char symbol) {
		this.symbol = symbol; // assign the tile symbol on creation
	}
	
	public char getSymbol() {
		return symbol; // return the stored symbol for display or logic
	}
	
	public void setSymbol(char symbol) {
		this.symbol = symbol; // update the tile type when building or clearing
	}
}

/**
 * EventNode stores one entry in the event log linked list.
 *
 * PRE:  event string must describe a game event.
 * POST: The node becomes part of a sequential log representing event history.
 *
 * COURSE CONCEPT: Linked list node with a next pointer.
 */
class EventNode {
	String event;
	EventNode next;

	public EventNode(String event) {
		this.event = event; // store the event description
		next = null; // initialize the next pointer to null
	}
}

/**
 * EventLogLinkedList maintains a chronological record of game events.
 *
 * PRE:  list may be empty or already contain event nodes.
 * POST: New events can be appended and full history printed.
 *
 * COURSE CONCEPT: Singly linked list traversal and append operation.
 */
class EventLogLinkedList {
	private EventNode head;

	public EventLogLinkedList() {
		head = null; // start with an empty log
	}
	
	/**
	 * Add a game event to the end of the event log.
	 */
	public void addEvent(String event) {
		// Create a new node for this event.
		EventNode newNode = new EventNode(event);
		if(head == null) {
			head = newNode; // first event becomes the head
			return;
		}
		EventNode current = head;
		while(current.next != null) {
			current = current.next; // traverse to the last node
		}
		current.next = newNode; // append event to the list
	}
	
	/**
	 * Print all stored events in order.
	 */
	public void displayEvents() {
		System.out.println("\n===== Event Log =====");
		EventNode current = head;
		if (current == null) {
			System.out.println("No Events.");
			return;
		}
		while(current != null) {
			System.out.println(current.event); // output each event string
			current = current.next; // continue through list
		}
	}
}

/**
 * ActionNode stores an entry in the action stack for recent game operations.
 *
 * PRE:  action string should describe the performed action.
 * POST: Node links to the next action and supports LIFO traversal.
 *
 * COURSE CONCEPT: Stack node used for last-in, first-out behavior.
 */
class ActionNode {
	String action;
	ActionNode next;

	public ActionNode(String action) {
		this.action = action; // store action description
		next = null; // next pointer starts empty
	}
}

/**
 * ActionStack records recent player actions using a LIFO data structure.
 *
 * PRE:  stack may begin empty and grows as actions occur.
 * POST: New actions are pushed and can be displayed from newest to oldest.
 *
 * COURSE CONCEPT: Stack operations implemented with linked nodes.
 */
class ActionStack {
	private ActionNode top;

	public ActionStack() {
		top = null; // empty stack at start
	}
	
	/**
	 * Push a new action onto the stack.
	 */
	public void push(String action) {
		ActionNode newNode = new ActionNode(action);
		newNode.next = top; // the old top becomes the next node
		top = newNode; // update top to the new action
	}
	
	/**
	 * Print recent actions from newest to oldest.
	 */
	public void displayActions() {
		System.out.println("\n===== Recent Actions =====");
		ActionNode current = top;
		if (current == null) {
			System.out.println("No Actions.");
			return; // nothing on the stack
		}
		while(current != null) {
			System.out.println(current.action); // output the action at this node
			current = current.next; // move down the stack
		}
	}
}

/**
 * Task represents an objective players can complete.
 */
class Task {
	private String description;
	private String type;
	private String target;
	private int amount;
	private double reward;

	public Task(String description, String type, String target, int amount, double reward) {
		this.description = description; // text shown to the player
		this.type = type; // internal task type identifier
		this.target = target; // target resource or building name
		this.amount = amount; // numeric completion threshold
		this.reward = reward; // cash reward when completed
	}
	
	public String getDescription() {
		return description; // return the visible task description
	}
	
	public String getType() {
		return type; // return the task category
	}
	
	public String getTarget() {
		return target; // return the task target name
	}
	
	public int getAmount() {
		return amount; // return the task goal amount
	}
	
	public double getReward() {
		return reward; // return the reward amount
	}
	
	/**
	 * Determine whether this task is complete in the current farm.
	 */
	public boolean isCompleted(Farm farm) {
		if(type.equals("SELL_RESOURCE")) {
			Resource r = farm.binarySearchResource(target);
			if(r != null) {
				// completed when the target resource quantity is at or below the goal
				return r.getQuantity() <= amount;
			}
		}
		else if(type.equals("BUILD")) {
			// completed when enough buildings of the requested type exist on the farm
			return farm.countBuildings(target) >= amount;
		}
		else if(type.equals("BUY_ANIMALS")) {
			// completed when the farm has enough animals of the requested species
			return farm.countAnimalsByName(target) >= amount;
		}
		else if(type.equals("EARN_MONEY")) {
			// completed when farm cash reaches or exceeds the reward threshold
			return farm.getMoney() >= amount;
		}
		else if(type.equals("SURVIVE_DAYS")) {
			// completed when the current game day reaches the target day
			return farm.getDay() >= amount;
		}
		return false; // default if task type is unknown
	}
}

/**
 * Node for a task queue linked list.
 */
class TaskNode {
	Task task;
	TaskNode next;

	public TaskNode(Task task) {
		this.task = task;
		next = null;
	}
}

/**
 * Ordered queue of active tasks.
 */
class TaskQueue {
	private TaskNode front;
	private TaskNode rear;

	public TaskQueue() {
		front = null; // start with empty queue state
		rear = null;
	}
	
	public boolean isEmpty() {
		return front == null; // queue is empty when there is no front node
	}
	
	/**
	 * PRE:  description is a unique task description
	 * POST: Returns true if task with same description exists
	 * TIME: O(n) linear search through queue
	 */
	public boolean taskExists(String description) {
		TaskNode current = front;
		while(current != null) {
			if(current.task.getDescription().equals(description)) {
				return true; // duplicate task found
			}
			current = current.next; // move to next task
		}
		return false; // no duplicate found
	}
	
	/**
	 * PRE:  task is a valid Task object
	 * POST: Task is added to back of queue if not duplicate
	 * TIME: O(n) due to taskExists() check
	 */
	public void enqueue(Task task) {
		if(taskExists(task.getDescription())) {
			return; // do not add duplicate tasks
		}
		TaskNode newNode = new TaskNode(task);
		if(isEmpty()) {
			front = newNode; // queue was empty, new node becomes first element
			rear = newNode; // and also the last element
		} else {
			rear.next = newNode; // attach new node at the end
			rear = newNode; // update the rear pointer
		}
	}
	
	/**
	 * PRE:  Queue may or may not be empty
	 * POST: Front task removed and returned; queue adjusted; rear set to null if empty
	 * TIME: O(1) constant time
	 */
	public Task dequeue() {
		if(isEmpty()) {
			return null; // nothing to remove from an empty queue
		}
		Task removed = front.task;
		front = front.next; // advance the front pointer
		if(front == null) {
			rear = null; // queue has become empty
		}
		return removed; // return the removed task
	}
	
	public Task peek() {
		if(isEmpty()) {
			return null; // no task available
		}
		return front.task; // inspect the first task without removing it
	}
	
	public void displayTasks() {
		System.out.println("\n===== Task Queue =====");
		if(isEmpty()) {
			System.out.println("No active tasks.");
			return; // nothing to show
		}
		TaskNode current = front;
		int count = 1;
		while(current != null) {
			System.out.println(count + ". " + current.task.getDescription());
			current = current.next; // move to next queued task
			count++;
		}
		System.out.println("\nOnly the first task must be completed first because this is a queue.");
	}
	
	/**
	 * Checks if front task is completed and awards reward
	 * PRE:  farm is valid; queue may be empty
	 * POST: All front completed tasks removed and money awarded
	 * TIME: O(n) where n is number of completed tasks at front
	 */
	public void checkFrontTask(Farm farm) {
		while(!isEmpty() && peek().isCompleted(farm)) {
			Task currentTask = peek();
			System.out.println("\nTask Completed: " + currentTask.getDescription());
			System.out.printf("Reward: $%.2f%n", currentTask.getReward());
			farm.addMoney(currentTask.getReward()); // reward for completing task
			dequeue(); // remove it from queue
		}
	}

	public void removeCompletedTasks(Farm farm) {
		TaskQueue temp = new TaskQueue(); // temporary queue for incomplete tasks
		TaskNode current = front;
		while(current != null) {
			if(!current.task.isCompleted(farm)) {
				temp.enqueue(current.task); // keep unfinished tasks only
			}
			current = current.next; // continue scanning full queue
		}
		this.front = temp.front; // restore queue with only incomplete tasks
		this.rear = temp.rear;
	}
}

/**
 * PositionNode Class - Node for position queue implementation
 * 
 * PURPOSE: Stores row/column coordinates in BFS pathfinding
 * 
 * COURSE CONCEPT: Linked List Data Structure (used for Queue in BFS)
 */
class PositionNode {
	int row;
	int col;
	PositionNode next;

	public PositionNode(int row, int col) {
		this.row = row; // store row coordinate
		this.col = col; // store column coordinate
		next = null; // initialize next pointer
	}
}

/**
 * Simple FIFO queue used by BFS pathfinding.
 */
class PositionQueue {
	private PositionNode front;
	private PositionNode rear;

	public PositionQueue() {
		front = null; // queue starts empty
		rear = null;
	}
	
	public boolean isEmpty() {
		return front == null; // queue is empty if no front node
	}
	
	public void enqueue(int row, int col) {
		PositionNode newNode = new PositionNode(row, col);
		if(isEmpty()) {
			front = newNode; // first node becomes both front and rear
			rear = newNode;
		}
		else {
			rear.next = newNode; // attach new node to the end
			rear = newNode; // move rear pointer forward
		}
	}
	
	public PositionNode dequeue() {
		if(isEmpty()) {
			return null; // no position to remove if empty
		}
		PositionNode removed = front; // remove the front node
		front = front.next; // advance front pointer
		if(front == null) {
			rear = null; // queue is now empty after removal
		}
		return removed; // return the removed position
	}
}

/**
 * FarmMap stores a 2D tile grid and supports map placement and search.
 *
 * PRE:  rows and cols are positive integers representing the farm grid.
 * POST: A farm layout is created and can be used for building placement,
 *       map display, and region-finding operations.
 *
 * COURSE CONCEPT: 2D arrays and traversal algorithms for grid-based maps.
 */
class FarmMap {
	private Tile[][] map;  // 2D array storing farm tiles

	/**
	 * Constructor initializes 7x7 farm grid with default buildings
	 * PRE:  rows > 0, cols > 0
	 * POST: Map initialized with empty tiles; 3 default buildings placed
	 */
	public FarmMap(int rows, int cols) {
		map = new Tile[rows][cols];
		// Fill map with empty tiles
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				map[r][c] = new Tile('E'); // initialize each cell as empty
			}
		}
		// Sample structures for testing and initial gameplay layout
		if(rows > 2 && cols > 2) map[1][1].setSymbol('B'); // Barn
		if(rows > 3 && cols > 3) map[2][3].setSymbol('P'); // Fish Pond
		if(rows > 5 && cols > 5) map[4][4].setSymbol('G'); // Greenhouse
	}
	
	public void displayMap() {
		System.out.println("\nFarm Map");
		System.out.println("Legend: E = Empty   B = Barn   P = Pond   G = Greenhouse   W = Warehouse   S = Supermarket");
		System.out.print("  ");
		for (int c = 0; c < map[0].length; c++) {
			System.out.printf(" %3d ", c + 1); // label columns starting at 1
		}
		System.out.println();
		System.out.print("   +");
		for (int c = 0; c < map[0].length; c++) {
			System.out.print("-----"); // top border
		}
		System.out.println();
		for (int r = 0; r < map.length; r++) {
			System.out.printf("%2d |", r + 1); // show row number
			for (int c = 0; c < map[r].length; c++) {
				System.out.printf(" %c  |", map[r][c].getSymbol()); // print current tile
			}
			System.out.println();
			System.out.print("   +");
			for (int c = 0; c < map[0].length; c++) {
				System.out.print("-----"); // separator line after each row
			}
			System.out.println();
		}
	}
	
	public boolean canPlaceBuilding(int row, int col) {
		if(row < 0 || row >= map.length || col < 0 || col >= map[0].length) {
			return false; // location outside the farm map
		}
		if(map[row][col].getSymbol() != 'E') {
			return false; // location already occupied
		}
		return true; // valid empty tile for building
	}
	
	public boolean placeBuilding(int row, int col, char symbol) {
		if(!canPlaceBuilding(row, col)) {
			System.out.println("Invalid or occupied location.");
			return false;
		}
		map[row][col].setSymbol(symbol); // mark the tile with building symbol
		return true;
	}
	
	public int getRows() {
		return map.length; // number of rows in the farm grid
	}
	
	public int getCols() {
		return map[0].length; // number of columns in the farm grid
	}
	
	public char getSymbol(int row, int col) {
		return map[row][col].getSymbol(); // return symbol from a specific tile
	}
	
	public void writeMap(PrintWriter output) {
		for (int r = 0; r < map.length; r++) {
			for (int c = 0; c < map[r].length; c++) {
				output.print(map[r][c].getSymbol()); // write each tile character
			}
			output.println(); // newline after each map row
		}
	}
	
	public boolean loadMap(Scanner input) {
		for (int r = 0; r < map.length; r++) {
			if(!input.hasNextLine()) {
				return false; // file ended early
			}
			String line = input.nextLine();
			if(line.length() < map[r].length) {
				return false; // invalid line length
			}
			for (int c = 0; c < map[r].length; c++) {
				map[r][c].setSymbol(line.charAt(c)); // restore symbol from file
			}
		}
		return true; // map loaded successfully
	}
	
	/**
	 * Count connected empty tiles via DFS.
	 */
	public int analyzeEmptyLandDFS(int row, int col) {
		boolean[][] visited = new boolean[map.length][map[0].length];
		return dfsCountEmpty(row, col, visited); // start recursive search
	}
	
	private int dfsCountEmpty(int row, int col, boolean[][] visited) {
		// Stop if the coordinates are outside the farm grid.
		if(row < 0 || row >= map.length || col < 0 || col >= map[0].length) {
			return 0;
		}
		// Skip tiles we already counted.
		if(visited[row][col]) {
			return 0;
		}
		// Only count empty tiles.
		if(map[row][col].getSymbol() != 'E') {
			return 0;
		}
		visited[row][col] = true; // mark this tile so recursion does not loop back
		
		// Add current tile then search each neighbor.
		return 1
			+ dfsCountEmpty(row - 1, col, visited) // up
			+ dfsCountEmpty(row + 1, col, visited) // down
			+ dfsCountEmpty(row, col - 1, visited) // left
			+ dfsCountEmpty(row, col + 1, visited); // right
	}
	
	/**
	 * Find the nearest empty tile using BFS.
	 *
	 * 1. Initialize queue with starting position
	 * 2. While queue not empty:
	 *    a. Dequeue front position
	 *    b. If empty tile found, return it
	 *    c. Otherwise, enqueue unvisited neighbors
	 * 3. Return null if no empty tile found
	 */
	public int[] findNearestEmptyTileBFS(int startRow, int startCol) {
		if(startRow < 0 || startRow >= map.length || startCol < 0 || startCol >= map[0].length) {
			return null; // invalid starting cell
		}
		boolean[][] visited = new boolean[map.length][map[0].length];
		PositionQueue queue = new PositionQueue();
		queue.enqueue(startRow, startCol);
		visited[startRow][startCol] = true; // mark starting cell visited
		
		// 4 directions: up, down, left, right
		int[] rowChange = {-1, 1, 0, 0};
		int[] colChange = {0, 0, -1, 1};

		while(!queue.isEmpty()) {
			PositionNode current = queue.dequeue(); // explore next position
			if(map[current.row][current.col].getSymbol() == 'E') {
				return new int[] {current.row, current.col}; // nearest empty found
			}
			for(int i = 0; i < 4; i++) {
				int newRow = current.row + rowChange[i];
				int newCol = current.col + colChange[i];
				if(newRow >= 0 && newRow < map.length && newCol >= 0 && newCol < map[0].length && !visited[newRow][newCol]) {
					visited[newRow][newCol] = true; // do not revisit positions
					queue.enqueue(newRow, newCol); // add neighbor for later inspection
				}
			}
		}
		return null; // no empty tile reachable from start
	}

	/**
	 * Finds the largest connected empty region
	 * 
	 * PRE:  Map is initialized
	 * POST: Returns [row, col, size] of largest empty region, or null
	 * TIME: O(rows * cols) - explores entire map using DFS
	 */
	public int[] findBestExpansionArea() {
		boolean[][] visited = new boolean[map.length][map[0].length];
		int bestSize = 0;
		int bestRow = -1;
		int bestCol = -1;
		
		for(int r = 0; r < map.length; r++) {
			for(int c = 0; c < map[r].length; c++) {
				if(map[r][c].getSymbol() == 'E' && !visited[r][c]) {
					int size = dfsCountEmpty(r, c, visited); // count connected empty tiles
					if(size > bestSize) {
						bestSize = size; // update best region size
						bestRow = r; // store starting coordinates
						bestCol = c;
					}
				}
			}
		}
		if(bestRow >= 0) {
			return new int[] {bestRow, bestCol, bestSize};
		}
		return null; // no empty region found
	}
}

/**
 * Abstract shared base for farm resources.
 *
 * PRE:  name, quantity, and value are initialized for a valid resource.
 * POST: Shared inventory behavior is defined for crops, animal products,
 *       and fish products.
 *
 * COURSE CONCEPT: Inheritance and polymorphism with an abstract superclass.
 */
abstract class Resource {
	protected String name;      // Name of resource (Wheat, Milk, Salmon, etc)
	protected int quantity;     // Amount currently held
	protected double value;     // Price per unit

	public Resource(String name, int quantity, double value) {
		this.name = name; // save the resource name
		this.quantity = quantity; // initial quantity available
		this.value = value; // unit price for market operations
	}
	
	public String getName() {
		return name; // return resource name for display or search
	}
	
	public int getQuantity() {
		return quantity; // return current inventory quantity
	}
	
	public double getValue() {
		return value; // return unit market price
	}
	
	public double getTotalValue() {
		return quantity * value; // calculate total inventory value
	}
	
	/**
	 * Increases quantity by specified amount
	 * PRE:  amount > 0
	 * POST: quantity increased
	 */
	public void addQuantity(int amount) {
		quantity += amount; // adjust inventory count upward
	}
	
	/**
	 * Decreases quantity by specified amount
	 * PRE:  amount > 0 and amount <= quantity
	 * POST: quantity decreased
	 */
	public void removeQuantity(int amount) {
		quantity -= amount; // reduce inventory count
	}
	
	/**
	 * Sets quantity to specific value
	 * PRE:  quantity >= 0
	 * POST: quantity set (or unchanged if negative provided)
	 */
	public void setQuantity(int quantity) {
		if(quantity >= 0) {
			this.quantity = quantity; // change quantity only when valid
		}
	}
	
	/**
	 * Abstract method - must be implemented by each subclass
	 * POLYMORPHISM: Different types return different values
	 * - Crop returns "Crop"
	 * - AnimalProduct returns "Animal Product"
	 * - FishProduct returns "Fish Product"
	 */
	public abstract String getType();
}

/**
 * Crop resource with growth and harvest state.
 *
 * PRE:  crop must have a name, quantity, sell value, and growth profile.
 * POST: Crop objects can track planted acres, days remaining, and harvests.
 *
 * COURSE CONCEPT: State tracking and object behavior for a simulation model.
 */
class Crop extends Resource {
	private int growthDays;       // Base days for growth cycle
	private int daysRemaining;    // Days until ready to harvest
	private int plantedAcres;     // Amount of crop currently growing
	private int yieldPerAcre;     // Harvest amount per acre planted
	private double seedCost;      // Cost to plant per acre

	public Crop(String name, int quantity, double value, int growthDays, int yieldPerAcre, double seedCost) {
		super(name, quantity, value); // call parent constructor
		
		this.growthDays = growthDays; // set growth length
		this.yieldPerAcre = yieldPerAcre; // set harvest yield
		this.seedCost = seedCost; // set cost to plant per acre
		daysRemaining = growthDays; // crop needs full growth cycle
		plantedAcres = 0; // nothing is planted yet
	}
	
	public int getGrowthDays() {
		return growthDays; // return how many days this crop takes to grow
	}
	
	/**
	 * Advances crop growth by 1 day
	 * PRE:  plantedAcres > 0 (crop must be planted)
	 * POST: daysRemaining decremented by 1
	 */
	public void grow() {
		if(plantedAcres > 0) {
			daysRemaining--; // reduce the remaining growth days
		}
	}
	
	/**
	 * Checks if crop is ready for harvest
	 * PRE:  None
	 * POST: Returns true if plantedAcres > 0 AND daysRemaining <= 0
	 */
	public boolean readyToHarvest() {
		return plantedAcres > 0 && daysRemaining <= 0; // ready only when planted and growth complete
	}
	
	@Override
	public String getType() {
		return "Crop"; // resource type identifier for crop items
	}
	
	/**
	 * Resets crop growth cycle after harvest
	 * 
	 * CRITICAL: Must reset BOTH daysRemaining and plantedAcres
	 * Previously only reset daysRemaining, causing infinite crops
	 * 
	 * PRE:  None
	 * POST: daysRemaining = growthDays, plantedAcres = 0
	 * IMPACT: Prevents infinite crop production
	 */
	public void resetGrowth() {
		daysRemaining = growthDays; // reset growth timer
		plantedAcres = 0;  // CRITICAL: Reset planted acres so must replant
	}
	
	public int getPlantedAcres() {
		return plantedAcres; // return acres currently planted
	}
	
	public void addPlantedAcres(int acres) {
		if(acres > 0) {
			plantedAcres += acres; // increase planted area
			if(daysRemaining <= 0) {
				daysRemaining = growthDays; // restart growth if previous cycle completed
			}
		}
	}
	
	public int getYieldPerAcre() {
		return yieldPerAcre; // return harvest amount per acre
	}
	
	public double getSeedCost() {
		return seedCost; // return seed cost used for planting calculations
	}
	
	public int getDaysRemaining() {
		return daysRemaining; // return how many days until harvest
	}
	
	public int harvest(int greenhouseBonus, int availableSpace) {
		int totalYield = plantedAcres * yieldPerAcre + greenhouseBonus; // calculate gross harvest
		int actualHarvest = Math.min(totalYield, availableSpace); // limited by storage space
		if(actualHarvest > 0) {
			addQuantity(actualHarvest); // add actual harvest to inventory
		}
		resetGrowth(); // restart the planting cycle
		return actualHarvest; // report harvested amount
	}
}

/**
 * Animal-based product resource.
 */
class AnimalProduct extends Resource {
	public AnimalProduct(String name, int quantity, double value) {
		super(name, quantity, value); // call parent constructor for resource fields
	}
	
	/**
	 * Returns "Animal Product" to identify this product type
	 * Implements abstract getType() from Resource
	 */
	@Override
	public String getType() {
		return "Animal Product"; // type label used for display and logic
	}
}

/**
 * Fish-based product resource.
 */
class FishProduct extends Resource {
	public FishProduct(String name, int quantity, double value) {
		super(name, quantity, value); // call parent constructor for resource fields
	}
	
	/**
	 * Returns "Fish Product" to identify this product type
	 * Implements abstract getType() from Resource
	 */
	@Override
	public String getType() {
		return "Fish Product"; // type label used for display and logic
	}
}

/**
 * Livestock with production and feeding state.
 *
 * PRE:  animal requires a species, product type, count, and yield values.
 * POST: Animal objects can produce goods and respond to feeding mechanics.
 *
 * COURSE CONCEPT: Object state and behavior modeling for real-world systems.
 */
class Animal {
	private String name;              // Species name (Cow, Chicken)
	private String productName;       // Product generated (Milk, Eggs)
	private int count;                // Population of this animal type
	private int feedLevel;            // 0=unfed, 1=normal (affects production)
	private int dailyYield;           // Amount of product produced per animal
	private double tradeValue;        // Sale price per animal

	public Animal(String name, String productName, int count, int dailyYield, double tradeValue) {
		this.name = name; // animal species name
		this.productName = productName; // associated product
		this.count = count; // starting animal count
		this.dailyYield = dailyYield; // base per-animal yield
		this.tradeValue = tradeValue; // price when sold
		this.feedLevel = 1;  // Start fed so first day production is normal
	}

	public String getName() {
		return name; // return species name
	}
	
	public String getProductName() {
		return productName; // return product name produced by animal
	}
	
	public int getCount() {
		return count; // return how many animals of this type exist
	}
	
	public int getFeedLevel() {
		return feedLevel; // return current feed status
	}
	
	public int getDailyYield() {
		return dailyYield; // return base yield per animal
	}
	
	public double getTradeValue() {
		return tradeValue; // return sale value per animal
	}
	
	/**
	 * Increases animal population
	 * PRE:  amount > 0
	 * POST: count increased by amount
	 */
	public void addCount(int amount) {
		if(amount > 0) {
			count += amount; // add new animals to the herd
		}
	}
	
	public void removeCount(int amount) {
		if(amount > 0) {
			count -= amount; // remove sold or lost animals
			if(count < 0) {
				count = 0; // prevent negative animal count
			}
		}
	}
	
	public void setFeedLevel(int feedLevel) {
		this.feedLevel = Math.max(feedLevel, 0); // keep feed level non-negative
	}
	
	public void addFeed(int feedPoints) {
		if(feedPoints > 0) {
			feedLevel += feedPoints; // increase feed level with feed units
		}
	}
	
	public int produce(int barnBonus, int farmerBonus) {
		if(count <= 0) {
			return 0; // no production if no animals
		}
		int product = count * dailyYield + barnBonus + farmerBonus; // calculate base production
		if(feedLevel <= 0) {
			product = Math.max(product / 2, 0); // reduce production when unfed
		} else {
			feedLevel = Math.max(feedLevel - 1, 0); // consume a feed unit
		}
		return product; // return produced product amount
	}
}

/**
 * CropType stores planting info.
 */
class CropType {
	private String name;
	private double seedCost;
	private double sellValue;
	private int growthDays;
	private int yieldPerAcre;

	public CropType(String name, double seedCost, double sellValue, int growthDays, int yieldPerAcre) {
		this.name = name; // crop name for planting menus
		this.seedCost = seedCost; // cost to plant one acre
		this.sellValue = sellValue; // market price after harvest
		this.growthDays = growthDays; // required days until harvest
		this.yieldPerAcre = yieldPerAcre; // harvest amount per acre
	}

	public String getName() {
		return name; // return crop name
	}
	
	public double getSeedCost() {
		return seedCost; // return planting cost
	}
	
	public double getSellValue() {
		return sellValue; // return sell price for harvested crop
	}
	
	public int getGrowthDays() {
		return growthDays; // return days required for growth
	}
	
	public int getYieldPerAcre() {
		return yieldPerAcre; // return yield per acre
	}
}

/**
 * Abstract building base class.
 */
abstract class Building {
	protected String name;      // Building name
	protected double cost;      // Purchase cost

	public Building(String name, double cost) {
		this.name = name; // set building name
		this.cost = cost; // set purchase cost
	}
	
	public String getName() {
		return name; // return building name
	}
	
	public double getCost() {
		return cost; // return purchase cost
	}
	
	/**
	 * Abstract method - must be implemented by each building subclass
	 * POLYMORPHISM: Different building types return different names
	 */
	public abstract String getType();
}

/**
 * Barn building.
 */
class Barn extends Building {
	public Barn() {
		super("Barn", 5000); // Barn costs $5000
	}
	
	@Override
	public String getType() {
		return "Barn"; // identifier used when placing buildings
	}
}

/**
 * Fish pond building.
 */
class FishPond extends Building {
	public FishPond() {
		super("Fish Pond", 7000); // Fish Pond costs $7000
	}
	
	@Override
	public String getType() {
		return "Fish Pond";
	}
}

/**
 * Greenhouse building.
 */
class Greenhouse extends Building {
	public Greenhouse() {
		super("Greenhouse", 8000); // Greenhouse costs $8000
	}
	
	@Override
	public String getType() {
		return "Greenhouse";
	}
}

/**
 * Warehouse building.
 */
class Warehouse extends Building {
	public Warehouse() {
		super("Warehouse", 12000); // Warehouse costs $12000
	}

	@Override
	public String getType() {
		return "Warehouse";
	}
}

/**
 * Supermarket building.
 */
class Supermarket extends Building {
	public Supermarket() {
		super("Supermarket", 20000); // Supermarket costs $20000
	}

	@Override
	public String getType() {
		return "Supermarket";
	}
}

/**
 * Market pricing and price updates.
 */
class Market {
	private double wheatPrice;
	private double milkPrice;
	private double salmonPrice;
	private double cornPrice;
	private double tomatoPrice;
	private double eggsPrice;

	public Market() {
		wheatPrice = 5.0; // initial price for wheat
		milkPrice = 8.0; // initial price for milk
		salmonPrice = 12.0; // initial price for salmon
		cornPrice = 10.0; // initial price for corn
		tomatoPrice = 14.0; // initial price for tomato
		eggsPrice = 4.0; // initial price for eggs
	}
	
	public void displayPrices() {
		System.out.println("\n===== Market Prices =====");
		System.out.printf("Wheat: $%.2f%n", wheatPrice);
		System.out.printf("Corn: $%.2f%n", cornPrice);
		System.out.printf("Tomato: $%.2f%n", tomatoPrice);
		System.out.printf("Milk: $%.2f%n", milkPrice);
		System.out.printf("Salmon: $%.2f%n", salmonPrice);
		System.out.printf("Eggs: $%.2f%n", eggsPrice);
	}
	
	public double getPrice(String resourceName) {
		if(resourceName.equalsIgnoreCase("Wheat")) {
			return wheatPrice;
		}
		if(resourceName.equalsIgnoreCase("Corn")) {
			return cornPrice;
		}
		if(resourceName.equalsIgnoreCase("Tomato")) {
			return tomatoPrice;
		}
		if(resourceName.equalsIgnoreCase("Milk")) {
			return milkPrice;
		}
		if(resourceName.equalsIgnoreCase("Salmon")) {
			return salmonPrice;
		}
		if(resourceName.equalsIgnoreCase("Eggs")) {
			return eggsPrice;
		}
		return 0; // unknown resource returns zero price
	}
	
	public void updatePrices() {
		wheatPrice += Math.random() * 2 - 1; // random fluctuation around current price
		cornPrice += Math.random() * 2 - 1.5; // larger swing for corn
		tomatoPrice += Math.random() * 2 - 1.5; // tomato price adjusts daily
		milkPrice += Math.random() * 2 - 1; // milk price changes moderately
		salmonPrice += Math.random() * 2 - 1; // fish price can go up or down
		eggsPrice += Math.random() * 1.5 - 0.75; // eggs fluctuate less dramatically
		
		if(wheatPrice < 1) wheatPrice = 1; // ensure a minimum realistic price
		if(cornPrice < 2) cornPrice = 2;
		if(tomatoPrice < 3) tomatoPrice = 3;
		if(milkPrice < 1) milkPrice = 1;
		if(salmonPrice < 1) salmonPrice = 1;
		if(eggsPrice < 1) eggsPrice = 1;
	}
}

class EventManager {
	public static void randomEvent(Farm farm, EventLogLinkedList eventLog) {
		int event = (int)(Math.random() * 5); // choose one of five random events

		switch(event) {
		case 0:
			farm.setMoney(farm.getMoney() + 500); // add market bonus cash
			System.out.println("\nMarket Boom! (+$500)");
			eventLog.addEvent("Day " + farm.getDay() + ": Market Boom");
			break;
			
		case 1:
			farm.setMoney(farm.getMoney() - 500); // subtract tax cost
			System.out.println("\nTax Increase! (-$500)");
			eventLog.addEvent("Day " + farm.getDay() + ": Tax Increase");
			break;
			
		case 2:
			System.out.println("\nNormal Day."); // no cash change event
			eventLog.addEvent("Day " + farm.getDay() + ": Normal Day");
			break;
			
		case 3:
			farm.setMoney(farm.getMoney() - 1000); // flood damage cost
			eventLog.addEvent("Day " + farm.getDay() + ": Flood Damage");
			System.out.println("\nFlood Damage! (-$1000)");
			break;
			
		case 4:
			farm.setMoney(farm.getMoney() + 2000); // good weather bonus
			eventLog.addEvent("Day " + farm.getDay() + ": Good Weather");
			System.out.println("\nGood Weather! (+$2000)");
			break;
		}
	}
}

class Employee {
	private String name;
	private String role;
	private double salary;

	public Employee(String name, String role, double salary) {
		this.name = name; // store the employee name
		this.role = role; // store the employee role
		this.salary = salary; // store monthly salary cost
	}
	
	public String getName() {
		return name; // return the employee's name
	}
	
	public String getRole() {
		return role; // return the employee's role
	}
	
	public double getSalary() {
		return salary; // return salary for payroll calculations
	}
}

/**
 * Farm manages the full game state.
 */
class Farm {
	private double money;         // Cash on hand
	private int day;              // Current game day
	private int landSize;         // Available farming space
	private FarmMap farmMap;      // 2D map of farm layout
	
	// Polymorphic collections - can store different subclass types
	private Resource[] inventory;   // Can store Crop, AnimalProduct, FishProduct
	private Building[] buildings;   // Can store Barn, Greenhouse, Warehouse, etc
	private Employee[] employees;   // Hired staff
	private Animal[] animals;       // Livestock population

	/**
	 * Constructor - Initialize new game
	 * 
	 * PRE:  None
	 * POST: Farm created with starting resources and buildings
	 * 
	 * INITIAL STATE:
	 * - Money: $16,000
	 * - Day: 1
	 * - Inventory (60% capacity): Wheat 20, Corn 12, Tomato 10, Milk 20, Salmon 12, Eggs 22
	 * - Employees: John (Farmer $1500/mo), Sarah (Fisher $1800/mo)
	 * - Animals: 3 Cows, 6 Chickens
	 * - Buildings on map: Barn (1,1), Pond (2,3), Greenhouse (4,4), Warehouse (3,1)
	 */
	public Farm() {
		money = 16000; // starting cash for the farm
		day = 1; // day one of the game
		landSize = 18; // beginning usable land acres
		farmMap = new FarmMap(7, 7); // initialize the farm layout
		
		// Create inventory - reduced to ~60% capacity for better gameplay
		inventory = new Resource[15];
		inventory[0] = new Crop("Wheat", 20, 5.0, 3, 15, 4.0);
		inventory[1] = new Crop("Corn", 12, 10.0, 4, 20, 6.0);
		inventory[2] = new Crop("Tomato", 10, 14.0, 5, 18, 8.0);
		inventory[3] = new AnimalProduct("Milk", 20, 8.0);
		inventory[4] = new FishProduct("Salmon", 12, 12.0);
		inventory[5] = new AnimalProduct("Eggs", 22, 4.0);
		
		// Create buildings - do NOT add default map buildings to array
		// Only player-purchased buildings go in the array
		buildings = new Building[15];
		// No initial buildings in array - they're only on the map from FarmMap constructor
		farmMap.placeBuilding(3, 1, 'W');
		
		// Create employees - reduced salaries for better balance
		employees = new Employee[10];
		employees[0] = new Employee("John", "Farmer", 1500);
		employees[1] = new Employee("Sarah", "Fisher", 1800);
		
		// Create animals - balanced to have similar value
		animals = new Animal[10];
		animals[0] = new Animal("Cow", "Milk", 3, 2, 500.0);
		animals[1] = new Animal("Chicken", "Eggs", 6, 3, 120.0);
	}
	
	public void displayStatus() {
		System.out.println("\n==============================");
		System.out.println("        Farm Status");
		System.out.println("==============================");
		System.out.printf("%-15s : %12s%n", "Money", String.format("$%.2f", money)); // current cash on hand
		System.out.printf("%-15s : %12d%n", "Day", day); // game day number
		System.out.printf("%-15s : %12s%n", "Land Size", landSize + " acres"); // total farm acreage
		System.out.printf("%-15s : %12s%n", "Net Worth", String.format("$%.2f", money + calculateInventoryValue())); // cash + inventory value
		System.out.printf("%-15s : %12s%n", "Storage", String.format("%d / %d", getCurrentInventorySize(), getInventoryCapacity())); // storage usage
		System.out.printf("%-15s : %12d%n", "Employees", getTotalEmployees()); // current employees
		System.out.printf("%-15s : %12d%n", "Animals", getTotalAnimals()); // total animals on farm
	}
	
	/**
	 * Displays all inventory items with type, quantity, and value information
	 * PRE:  None
	 * POST: Formatted inventory printed to console
	 */
	public void displayInventory(Market market) {
		System.out.println("\nInventory");
		System.out.println("--------------------------------------------------------------------------------");
		System.out.printf("%-15s | %-14s | %-5s | %-8s | %-28s%n", "Type", "Name", "Qty", "Unit $", "Details");
		System.out.println("--------------------------------------------------------------------------------");
		for (Resource r : inventory) {
			if (r != null) {
				if (r instanceof Crop) {
					Crop crop = (Crop) r;
					String details = String.format("P:%dac H:%dd", crop.getPlantedAcres(), crop.getDaysRemaining());
					System.out.printf("%-15s | %-14s | %5d | %8.2f | %-28s%n", r.getType(), r.getName(), r.getQuantity(), market.getPrice(r.name), details);
				} else {
					System.out.printf("%-15s | %-14s | %5d | %8.2f | %-28s%n", r.getType(), r.getName(), r.getQuantity(), market.getPrice(r.name), "");
				}
			}
		}
	}
	
	public void displayBuildings() {
		System.out.println("\nBuildings");
		System.out.println("-------------------------------------");
		System.out.printf("%-18s | %10s%n", "Building", "Cost");
		System.out.println("-------------------------------------");
		for (Building b : buildings) {
			if (b != null) {
				System.out.printf("%-18s | $%9.2f%n", b.getType(), b.getCost());
			}
		}
	}
	
	public void displayEmployees() {
		System.out.println("\nEmployees");
		System.out.println("----------------------------------------------");
		System.out.printf("%-18s | %-12s | %10s%n", "Name", "Role", "Salary");
		System.out.println("----------------------------------------------");
		for(Employee e : employees) {
			if(e != null) {
				System.out.printf("%-18s | %-12s | $%9.2f%n", e.getName(), e.getRole(), e.getSalary());
				// print each hired employee and payroll cost
			}
		}
		System.out.println("\nBonuses:");
		System.out.println("  Farmer bonus: +" + countEmployeesByRole("Farmer") * 5 + " crop yield when harvesting.");
		System.out.println("  Fisher bonus: +" + countEmployeesByRole("Fisher") * 4 + " fish production each day.");
	}
	
	public double calculateSalaryExpenses() {
		double total = 0;
		for(Employee e : employees) {
			if(e != null) {
				total += e.getSalary(); // sum salary for all hired staff
			}
		}
		return total; // return monthly payroll total
	}
	
	public void addMoney(double amount) {
		money += amount; // change farm cash balance
	}
	
	public int countEmployeesByRole(String role) {
		int count = 0;
		for(Employee e : employees) {
			if(e != null && e.getRole().equalsIgnoreCase(role)) {
				count++; // count staff with matching role
			}
		}
		return count;
	}
	
	public int getTotalEmployees() {
		int count = 0;
		for(Employee e : employees) {
			if(e != null) {
				count++; // count each non-null employee slot
			}
		}
		return count;
	}
	
	public boolean addEmployee(Employee employee) {
		for(int i = 0; i < employees.length; i++) {
			if(employees[i] == null) {
				employees[i] = employee; // store new employee in first free slot
				return true;
			}
		}
		return false; // no space for more employees
	}
	
	public boolean addResource(Resource resource) {
		for(int i = 0; i < inventory.length; i++) {
			if(inventory[i] == null) {
				inventory[i] = resource; // place new resource in inventory
				return true;
			}
		}
		return false; // inventory full
	}
	
	public Resource[] getInventory() {
		return inventory; // return current inventory array for save/load and display
	}
	
	public Employee[] getEmployees() {
		return employees; // return list of hired employees
	}
	
	public void resetInventory() {
		for(int i = 0; i < inventory.length; i++) {
			inventory[i] = null;
		}
	}
	
	public void resetEmployees() {
		for(int i = 0; i < employees.length; i++) {
			employees[i] = null;
		}
	}

	public void displayAnimals() {
		System.out.println("\nAnimals");
		System.out.println("--------------------------------------------------------------------------------");
		System.out.printf("%-10s | %5s | %-10s | %11s | %10s | %11s%n", "Type", "Count", "Product", "Daily Yield", "Feed", "Trade $");
		System.out.println("--------------------------------------------------------------------------------");
		for(Animal a : animals) {
			if(a != null && a.getCount() > 0) {
				System.out.printf("%-10s | %5d | %-10s | %11d | %10d | $%10.2f%n",
					a.getName(), a.getCount(), a.getProductName(), a.getDailyYield(), a.getFeedLevel(), a.getTradeValue());
				// print summary for each animal type on farm
			}
		}
	}

	public int getTotalAnimals() {
		int total = 0;
		for(Animal a : animals) {
			if(a != null) {
				total += a.getCount(); // count each individual animal
			}
		}
		return total;
	}

	public Animal searchAnimal(String animalName) {
		for(Animal a : animals) {
			if(a != null && a.getName().equalsIgnoreCase(animalName)) {
				return a; // return the matching animal type
			}
		}
		return null; // no match found
	}

	public int countAnimalsByName(String name) {
	    Animal animal = searchAnimal(name);
	    if (animal == null) {
	        return 0; // no such animal type exists
	    }
	    return animal.getCount(); // quantity of that animal type
	}

	public Animal[] getAnimals() {
		return animals; // provide direct access to animal array
	}

	public boolean addAnimal(Animal animal) {
		if(animal == null || animal.getCount() <= 0) {
			return false; // invalid animal data
		}
		
		Animal existing = searchAnimal(animal.getName());
		if(existing != null) {
			existing.addCount(animal.getCount());
			existing.addFeed(animal.getFeedLevel());
			return true; // merge with existing animal batch
		}
		
		for(int i = 0; i < animals.length; i++) {
			if(animals[i] == null) {
				animals[i] = animal; // add new animal type to list
				return true;
			}
		}
		return false; // no room for new animal
	}

	public void resetAnimals() {
		for(int i = 0; i < animals.length; i++) {
			animals[i] = null; // clear animal inventory
		}
	}

	public boolean feedAnimals(String animalName, String feedResourceName, int feedUnits) {
		if(feedUnits <= 0) {
			System.out.println("Feed amount must be greater than 0.");
			return false;
		}
		
		Animal animal = searchAnimal(animalName);
		if(animal == null) {
			System.out.println("Animal type not found.");
			return false;
		}
		
		Resource feed = binarySearchResource(feedResourceName);
		if(feed == null || !(feed instanceof Crop)) {
			System.out.println("You can only feed animals with crops.");
			return false;
		}
		if(feed.getQuantity() < feedUnits) {
			System.out.println("Not enough " + feedResourceName + " to feed the animals.");
			return false;
		}
		
		feed.removeQuantity(feedUnits); // reduce crop inventory for feeding
		animal.addFeed(feedUnits * 2); // convert feed units into animal nutrition
		System.out.println("Fed " + animal.getName() + " with " + feedUnits + " " + feedResourceName + ".");
		return true;
	}

	public boolean tradeAnimals(String animalName, int sellCount, ActionStack stack) {
		if(sellCount <= 0) {
			System.out.println("Sell count must be greater than 0.");
			return false;
		}
		
		Animal animal = searchAnimal(animalName);
		if(animal == null) {
			System.out.println("Animal type not found.");
			return false;
		}
		if(sellCount > animal.getCount()) {
			System.out.println("Not enough " + animalName + " to trade.");
			return false;
		}
		
		double revenue = animal.getTradeValue() * sellCount; // calculate sale revenue
		animal.removeCount(sellCount); // remove traded animals
		if(animal.getCount() == 0) {
			for(int i = 0; i < animals.length; i++) {
				if(animals[i] != null && animals[i].getName().equalsIgnoreCase(animalName)) {
					animals[i] = null; // clear empty animal entry
					break;
				}
			}
		}
		
		money += revenue; // add trading income to farm balance
		System.out.printf("Traded %d %s for $%.2f%n", sellCount, animalName, revenue);
		if(stack != null) {
			stack.push("Traded " + sellCount + " " + animalName); // record action for undo stack
		}
		return true;
	}

	private double getDefaultProductValue(String productName) {
		if(productName.equalsIgnoreCase("Milk")) {
			return 8.0; // default milk price
		}
		if(productName.equalsIgnoreCase("Eggs")) {
			return 4.0; // default egg price
		}
		return 5.0; // default value for any other animal product
	}

	public void produceAnimalProducts() {
		int barnBonus = countBuildings("Barn") * 3; // barn boosts animal yield per day
		int farmerBonus = countEmployeesByRole("Farmer") * 2; // farmers also improve animal output
		
		for(Animal a : animals) {
			if(a != null && a.getCount() > 0) {
				int produced = a.produce(barnBonus, farmerBonus); // calculate product count for this animal type
				if(produced <= 0) {
					continue; // skip if this animal type produced nothing today
				}
				
				Resource product = binarySearchResource(a.getProductName());
				if(product == null) {
					product = new AnimalProduct(a.getProductName(), 0, getDefaultProductValue(a.getProductName()));
					if(!addResource(product)) {
						System.out.println("Inventory is full; cannot store new " + a.getProductName() + ".");
						continue;
					}
				}
				
				int availableSpace = getInventoryCapacity() - getCurrentInventorySize();
				if(availableSpace > 0) {
					int actualAdded = Math.min(produced, availableSpace);
					product.addQuantity(actualAdded); // increase product quantity in inventory
					System.out.println(a.getName() + " produced " + actualAdded + " " + a.getProductName() + ".");
					if(actualAdded < produced) {
						System.out.println("Storage full! Some " + a.getProductName() + " could not be stored.");
					}
				} else {
					System.out.println("Storage full! " + a.getName() + " production skipped.");
				}
			}
		}
	}

	public int getResourceQuantity(String resourceName) {
		Resource r = binarySearchResource(resourceName);
		if(r != null) {
			return r.getQuantity(); // return resource quantity from inventory
		}
		return 0; // not found yields zero
	}
	
	public void setResourceQuantity(String resourceName, int quantity) {
		Resource r = binarySearchResource(resourceName);
		if(r != null) {
			r.setQuantity(quantity); // set new quantity for existing resource
		}
	}

	public boolean plantCrop(String cropName, int acres, double cost) {
		if(acres <= 0) {
			System.out.println("Please plant at least 1 acre.");
			return false;
		}
		if(money < cost) {
			System.out.println("Not enough money to plant crops.");
			return false;
		}
		
		Resource resource = binarySearchResource(cropName);
		if(resource instanceof Crop) {
			Crop crop = (Crop) resource;
			crop.addPlantedAcres(acres); // schedule acres for growth
			money -= cost; // payment for planting
			System.out.println("Planted " + acres + " acres of " + cropName + "!");
			return true;
		}
		System.out.println("Crop not found.");
		return false;
	}

	public double calculateInventoryValue() {
		double total = 0;
		for(Resource r : inventory) {
			if(r != null) {
				total += r.getTotalValue(); // add total value of each resource
			}
		}
		return total; // return the combined inventory worth
	}

	public void sortInventoryByValue() {
		// selection sort finds the least valuable resource and places it at the current index
		for(int i = 0; i < inventory.length - 1; i++) {
			int min = i;
			for(int j = i + 1; j < inventory.length; j++) {
				if(inventory[j] != null && inventory[min] != null && inventory[j].getValue() < inventory[min].getValue()) {
					min = j; // lowest value resource among the remaining entries
				}
			}
			Resource temp = inventory[i];
			inventory[i] = inventory[min];
			inventory[min] = temp;
		}
	}

	/**
	 * Sort inventory alphabetically by name.
	 */
	public void sortInventoryByName() {
		for(int i = 0; i < inventory.length - 1; i++) {
			int min = i;
			// Find the next smallest resource name after position i.
			for(int j = i + 1; j < inventory.length; j++) {
				if(inventory[j] != null && inventory[min] != null && inventory[j].getName().compareToIgnoreCase(inventory[min].getName()) < 0) {
					min = j;
				}
			}
			// Place the smallest remaining resource at position i.
			Resource temp = inventory[i];
			inventory[i] = inventory[min];
			inventory[min] = temp;
		}
		// Sort does not print results; it prepares inventory for search.
	}
	
	/**
	 * Search inventory by name using binary search.
	 */
	public Resource binarySearchResource(String resourceName) {
		sortInventoryByName(); // keep inventory ordered before searching
		
		int low = 0;
		int high = inventory.length - 1;
		while(low <= high) {
			int mid = (low + high) / 2;
			if(inventory[mid] == null) {
				high = mid - 1; // skip empty slots on the right side
			}
			else {
				int compare = inventory[mid].getName().compareToIgnoreCase(resourceName);
				if(compare == 0) {
					return inventory[mid]; // exact match found
				}
				else if(compare < 0) {
					low = mid + 1; // target is after mid
				}
				else {
					high = mid - 1; // target is before mid
				}
			}
		}
		return null; // not found
	}
	
	public int getCurrentInventorySize() {
		int total = 0;
		for(Resource r : inventory) {
			if(r != null) {
				total += r.getQuantity(); // sum all resource quantities in inventory
			}
		}
		return total;
	}

	public void growCrops() {
		int greenhouseBonus = countBuildings("Greenhouse") * 10; // greenhouses increase harvest
		int farmerBonus = countEmployeesByRole("Farmer") * 5; // farmers also boost crop yields
		
		for(Resource r : inventory) {
			if(r instanceof Crop) {
				Crop crop = (Crop) r;
				crop.grow(); // advance each crop growth day
				if(crop.readyToHarvest()) {
					int totalHarvest = crop.getPlantedAcres() * crop.getYieldPerAcre() + greenhouseBonus + farmerBonus;
					int availableSpace = getInventoryCapacity() - getCurrentInventorySize();
					if(availableSpace > 0 && totalHarvest > 0) {
						int actualHarvest = Math.min(totalHarvest, availableSpace);
						crop.addQuantity(actualHarvest); // add harvested items to inventory
						System.out.println("\n" + crop.getName() + " harvest complete! +" + actualHarvest + " " + crop.getName() + " added.");
					} else if(totalHarvest > 0) {
						System.out.println("\nStorage full! " + crop.getName() + " could not be harvested.");
					}
					crop.resetGrowth(); // reset crop status after harvest
				}
			}
		}
	}
	private char getSymbolForBuilding(String type) {
		if(type.equals("Barn")) return 'B';
		if(type.equals("Fish Pond")) return 'P';
		if(type.equals("Greenhouse")) return 'G';
		if(type.equals("Warehouse")) return 'W';
		if(type.equals("Supermarket")) return 'S';
		return ' ';
	}
	
	/**
	 * Count buildings of a given type on the map.
	 */
	public int countBuildings(String type) {
		int count = 0;
		char symbol = getSymbolForBuilding(type); // map building type to map symbol
		
		for(int r = 0; r < farmMap.getRows(); r++) {
			for(int c = 0; c < farmMap.getCols(); c++) {
				if(farmMap.getSymbol(r, c) == symbol) {
					count++; // count matching tiles
				}
			}
		}
		return count;
	}
	
	public void sellResource(String resourceName, int quantity, Market market, ActionStack stack) {
		if(quantity <= 0) {
			System.out.println("Quantity must be greater than 0.");
			return;
		}
		Resource resource = binarySearchResource(resourceName);
		if(resource == null) {
			System.out.println("Resource not found.");
			return;
		}
		if(quantity > resource.getQuantity()) {
			System.out.println("Not enough inventory.");
			return;
		}
		
		double salePrice = market.getPrice(resourceName); // get current market price for resource
		double multiplier = 1 + (countBuildings("Supermarket") * 0.10); // supermarket bonus increases revenue
		double revenue = salePrice * quantity * multiplier; // total sale income
		
		resource.removeQuantity(quantity); // reduce inventory after sale
		money += revenue; // add earned money to farm balance
		
		System.out.print("\nSold " + quantity + " " + resourceName + " for $");
		System.out.printf("%.2f%n", revenue);
		stack.push("Sold " + quantity + " " + resourceName); // log sale action for review
	}
	
	/**
	 * Advance the game one day.
	 */
	public void advanceDay() {
		day++; // move to next game day
		growCrops(); // process crop growth and harvest if ready
		
		int fishBonus = countBuildings("Fish Pond") * 5 + countEmployeesByRole("Fisher") * 4;
		if(fishBonus > 0) {
			Resource fishResource = binarySearchResource("Salmon");
			if(fishResource instanceof FishProduct) {
				int availableSpace = getInventoryCapacity() - getCurrentInventorySize();
				if(availableSpace > 0) {
					int actualFish = Math.min(fishBonus, availableSpace);
					fishResource.addQuantity(actualFish); // store fish production
					System.out.println("Fish production: +" + actualFish);
				} else {
					System.out.println("\nStorage full! Fish production skipped.");
				}
			}
		}
		
		produceAnimalProducts(); // produce milk and eggs from animals
		
		int supermarketBonus = countBuildings("Supermarket") * 100;
		if(supermarketBonus > 0) {
			money += supermarketBonus; // daily sales bonus from supermarkets
			System.out.println("Supermarket revenue: +$" + supermarketBonus);
		}
		
		double dailySalary = calculateSalaryExpenses() / 30; // pay wages
		money -= dailySalary;
		System.out.println("\nDay advanced to " + day);
		System.out.printf("Daily salaries paid: $%.2f%n", dailySalary);
	}
	
	public int getInventoryCapacity() {
		return 200 + countBuildings("Warehouse") * 100; // base capacity plus warehouse expansion
	}
	
	public boolean addBuilding(Building building) {
		for(int i = 0; i < buildings.length; i++) {
			if(buildings[i] == null) {
				buildings[i] = building; // store purchased building in array
				return true;
			}
		}
		return false; // building list is full
	}
	
	public void resetBuildings() {
		for(int i = 0; i < buildings.length; i++) {
			buildings[i] = null;
		}
	}
	
	private Building createBuildingFromSymbol(char symbol) {
		switch(symbol) {
		case 'B':
			return new Barn();
		case 'P':
			return new FishPond();
		case 'G':
			return new Greenhouse();
		case 'W':
			return new Warehouse();
		case 'S':
			return new Supermarket();
		default:
			return null; // no building for unknown symbol
		}
	}
	
	public void loadBuildingsFromMap() {
		resetBuildings();
		int index = 0;
		for(int r = 0; r < farmMap.getRows(); r++) {
			for(int c = 0; c < farmMap.getCols(); c++) {
				Building building = createBuildingFromSymbol(farmMap.getSymbol(r, c));
				if(building != null && index < buildings.length) {
					buildings[index++] = building;
				}
			}
		}
	}
	
	public boolean hasBuildingSpace() {
		for(Building b : buildings) {
			if(b == null) {
				return true; // available slot found for a new building
			}
		}
		return false; // no empty building slots remain
	}
	
	public boolean buyAndPlaceBuilding(Building building, int row, int col, char symbol) {
		if(money < building.getCost()) {
			System.out.println("Not enough money.");
			return false;
		}
		if(!hasBuildingSpace()) {
			System.out.println("No building space available.");
			return false;
		}
		if(!farmMap.canPlaceBuilding(row, col)) {
			System.out.println("Invalid or occupied building location.");
			return false;
		}
		
		money -= building.getCost(); // deduct build cost from farm funds
		addBuilding(building); // store building in the purchased list so it affects the farm state
		farmMap.placeBuilding(row, col, symbol); // place building on map grid for visualization and logic
		System.out.println(building.getType() + " purchased!");
		return true;
	}
	
	public FarmMap getFarmMap() {
		return farmMap; // return the farm's map object
	}
	
	public double getMoney() {
		return money; // current cash balance
	}
	
	public int getDay() {
		return day; // current game day
	}
	
	public int getLandSize() {
		return landSize; // amount of usable farmland
	}
	
	public void setMoney(double money) {
		this.money = money; // update cash balance
	}
	
	public void setDay(int day) {
		this.day = day; // update game day counter
	}
	
	public void setLandSize(int landSize) {
		this.landSize = landSize; // adjust farm acreage
	}
	
	public void displayFinancialReport() {
		System.out.println("\n========== Financial Report ==========");
		System.out.printf("Current Money: $%.2f%n", money); // show liquidity
		System.out.printf("Inventory Value: $%.2f%n", calculateInventoryValue()); // show asset value
		System.out.printf("Storage Used: %d/%d%n", getCurrentInventorySize(), getInventoryCapacity()); // capacity usage
		System.out.printf("Employee Expenses: $%.2f per month%n", calculateSalaryExpenses()); // payroll burden
		System.out.println("Land Size: " + landSize);
		System.out.println("Buildings:");
		System.out.println("\tBarns: " + countBuildings("Barn"));
		System.out.println("\tFish Ponds: " + countBuildings("Fish Pond"));
		System.out.println("\tGreenhouses: " + countBuildings("Greenhouse"));
		System.out.println("\tWarehouses: " + countBuildings("Warehouse"));
		System.out.println("\tSupermarkets: " + countBuildings("Supermarket"));
		System.out.printf("Estimated Net Worth: $%.2f%n", (money + calculateInventoryValue()));
		if(money < 2000) {
			System.out.println("Warning: Low cash reserves. Sell resources or earn money soon to avoid bankruptcy.");
		}
	}
	
	public void displayInventoryReport() {
		System.out.println("\n========== Inventory Report ==========");
		System.out.printf("Storage Used: %d/%d%n", getCurrentInventorySize(), getInventoryCapacity()); // storage utilization
		
		double totalValue = calculateInventoryValue();
		System.out.printf("Total Inventory Value: $%.2f%n", totalValue); // total asset value in stock
		System.out.println("\nResource Details:");
		System.out.println("Type            | Name           | Qty | Unit $  |  Total $");
		System.out.println("-----------------------------------------------------------");
		
		int lowStockCount = 0;
		for(Resource r : inventory) {
			if(r != null) {
				System.out.printf("%-15s | %-14s | %3d | $%6.2f | $%7.2f%n", r.getType(), r.getName(), r.getQuantity(), r.getValue(), r.getTotalValue());
				if(r.getQuantity() > 0 && r.getQuantity() < 10) {
					lowStockCount++; // count low-stock resources
				}
			}
		}
		if(lowStockCount > 0) {
			System.out.println("\nWarning: Some resources are low on stock. Consider producing or buying more soon.");
		}
		if(getCurrentInventorySize() >= getInventoryCapacity() * 0.8) {
			System.out.println("\nWarning: Storage is near capacity. Sell or expand storage soon.");
		}
	}
	
	public boolean bankrupt() {
		return money <= 0; // true when farm has run out of cash
	}
}

/**
 * Handles file save/load operations.
 */
class SaveManager {
	private static File locateFile(String filename) {
		// Try multiple locations in order of preference
		
		// 1. Current working directory
		File file = new File(filename);
		if(file.exists()) {
			System.out.println("[Save system: Found file in current directory]");
			return file;
		}
		
		// 2. Bin directory (Eclipse output folder)
		file = new File("bin" + File.separator + filename);
		if(file.exists()) {
			System.out.println("[Save system: Found file in bin directory]");
			return file;
		}
		
		// 3. Src directory (source folder)
		file = new File("src" + File.separator + filename);
		if(file.exists()) {
			System.out.println("[Save system: Found file in src directory]");
			return file;
		}
		
		// 4. Parent directory
		file = new File(".." + File.separator + filename);
		if(file.exists()) {
			System.out.println("[Save system: Found file in parent directory]");
			return file;
		}
		
		// 5. If no file exists, default to current directory
		System.out.println("[Save system: Creating new file in current directory]");
		return new File(filename);
	}

	/**
	 * Write the current farm state to save.txt.
	 */
	public static boolean saveFarm(Farm farm) {
		try {
			File saveFile = locateFile("save.txt");
			PrintWriter output = new PrintWriter(new FileWriter(saveFile));
			
			output.println("VERSION:3"); // version tag allows future save format changes
			output.println(farm.getMoney());
			output.println(farm.getDay());
			output.println(farm.getLandSize());
			
			Resource[] inventory = farm.getInventory();
			int resourceCount = 0;
			for(Resource r : inventory) {
				if(r != null) resourceCount++; // count resources that will be saved
			}
			output.println(resourceCount); // number of saved resources
			
			for(Resource r : inventory) {
				if(r == null) continue;
				if(r instanceof Crop) {
					Crop crop = (Crop) r;
					output.printf("Crop,%s,%d,%.2f,%d,%d,%.2f,%d%n", crop.getName(), crop.getQuantity(), crop.getValue(), crop.getGrowthDays(), crop.getYieldPerAcre(), crop.getSeedCost(), crop.getPlantedAcres());
				}
				else if(r instanceof FishProduct) {
					output.printf("FishProduct,%s,%d,%.2f%n", r.getName(), r.getQuantity(), r.getValue());
				}
				else if(r instanceof AnimalProduct) {
					output.printf("AnimalProduct,%s,%d,%.2f%n", r.getName(), r.getQuantity(), r.getValue());
				}
			}
			
			Employee[] employees = farm.getEmployees();
			
			int employeeCount = 0;
			for(Employee e : employees) {
				if(e != null) employeeCount++;
			}
			output.println(employeeCount);
			
			for(Employee e : employees) {
				if(e != null) {
					output.printf("%s,%s,%.2f%n", e.getName(), e.getRole(), e.getSalary());
				}
			}
			
			Animal[] animals = farm.getAnimals();
			
			int animalCount = 0;
			for(Animal a : animals) {
				if(a != null && a.getCount() > 0) {
					animalCount++;
				}
			}
			output.println(animalCount);
			
			for(Animal a : animals) {
				if(a != null && a.getCount() > 0) {
					output.printf("Animal,%s,%s,%d,%d,%d,%.2f%n", a.getName(), a.getProductName(), a.getCount(), a.getFeedLevel(), a.getDailyYield(), a.getTradeValue());
				}
			}
			
			farm.getFarmMap().writeMap(output);
			output.close();
			System.out.println("\nGame Saved!");
			return true;
		} 
		catch (IOException e) {
			System.out.println("Save Error.");
			return false;
		}
	}

	public static boolean loadFarm(Farm farm) {
		try {
			File saveFile = locateFile("save.txt");
			Scanner input = new Scanner(saveFile); // open save file for reading
			
			if(!input.hasNextLine()) throw new IOException();
			
			String firstLine = input.nextLine();
			boolean isVersionThree = firstLine.startsWith("VERSION:3");
			boolean isVersionTwo = firstLine.startsWith("VERSION:2");
			
			if(!isVersionThree && !isVersionTwo) {
				// Old format fallback
				double money = Double.parseDouble(firstLine);
				int day = Integer.parseInt(input.nextLine());
				int landSize = Integer.parseInt(input.nextLine());
				int wheatQty = Integer.parseInt(input.nextLine());
				int milkQty = Integer.parseInt(input.nextLine());
				int salmonQty = Integer.parseInt(input.nextLine());
				
				farm.setMoney(money);
				farm.setDay(day);
				farm.setLandSize(landSize);
				farm.setResourceQuantity("Wheat", wheatQty);
				farm.setResourceQuantity("Milk", milkQty);
				farm.setResourceQuantity("Salmon", salmonQty);
				
				if(!farm.getFarmMap().loadMap(input)) {
					input.close();
					System.out.println("Load Error: save file is incomplete or invalid.");
					return false;
				}
				
				farm.loadBuildingsFromMap();
				input.close();
				System.out.println("\nGame Loaded!");
				return true;
			}
			
			// New format version 2
			double money = Double.parseDouble(input.nextLine());
			int day = Integer.parseInt(input.nextLine());
			int landSize = Integer.parseInt(input.nextLine());
			int resourceCount = Integer.parseInt(input.nextLine());
			
			// clear out the existing inventory before reading resource records from save
			farm.resetInventory();
			
			for(int i = 0; i < resourceCount; i++) {
				if(!input.hasNextLine()) throw new IOException();
				String[] parts = input.nextLine().split(",");
				if(parts[0].equals("Crop")) {
					Crop crop = new Crop(parts[1], Integer.parseInt(parts[2]), Double.parseDouble(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), Double.parseDouble(parts[6]));
					crop.addPlantedAcres(Integer.parseInt(parts[7]));
					farm.addResource(crop);
				}
				else if(parts[0].equals("FishProduct")) {
					farm.addResource(new FishProduct(parts[1], Integer.parseInt(parts[2]), Double.parseDouble(parts[3])));
				}
				else if(parts[0].equals("AnimalProduct")) {
					farm.addResource(new AnimalProduct(parts[1], Integer.parseInt(parts[2]), Double.parseDouble(parts[3])));
				}
			}
			
			int employeeCount = Integer.parseInt(input.nextLine());
			// reset staff and livestock state before restoring from save data
			farm.resetEmployees();
			farm.resetAnimals();
			
			for(int i = 0; i < employeeCount; i++) {
				if(!input.hasNextLine()) throw new IOException();
				String[] parts = input.nextLine().split(",");
				farm.addEmployee(new Employee(parts[0], parts[1], Double.parseDouble(parts[2])));
			}
			
			if(isVersionThree) {
				// version 3 save files include animal records in addition to standard state
				int animalCount = Integer.parseInt(input.nextLine());
				farm.resetAnimals();
				for(int i = 0; i < animalCount; i++) {
					if(!input.hasNextLine()) throw new IOException();
					String[] parts = input.nextLine().split(",");
					if(parts[0].equals("Animal")) {
						Animal animal = new Animal(parts[1], parts[2], Integer.parseInt(parts[3]), Integer.parseInt(parts[5]), Double.parseDouble(parts[6]));
						animal.setFeedLevel(Integer.parseInt(parts[4]));
						farm.addAnimal(animal);
					}
				}
			}
			
			if(!farm.getFarmMap().loadMap(input)) {
				input.close();
				System.out.println("Load Error: save file is incomplete or invalid.");
				return false;
			}
			
			farm.setMoney(money);
			farm.setDay(day);
			farm.setLandSize(landSize);
			farm.loadBuildingsFromMap();
			input.close();
			System.out.println("\nGame Loaded!");
			return true;
		} 
		catch (IOException | NumberFormatException e) {
			System.out.println("Load Error.");
			return false;
		}
	}
}

class ConfigLoader {
	private static ArrayList<CropType> cropTypes = new ArrayList<>();

	private static File locateFile(String filename) {
		// Try multiple locations in order of preference
		
		// 1. Current working directory
		File file = new File(filename);
		if(file.exists()) return file;
		
		// 2. Bin directory (Eclipse output folder)
		file = new File("bin" + File.separator + filename);
		if(file.exists()) return file;
		
		// 3. Src directory (source folder)
		file = new File("src" + File.separator + filename);
		if(file.exists()) return file;
		
		// 4. Parent directory
		file = new File(".." + File.separator + filename);
		if(file.exists()) return file;
		
		// 5. If no file exists, default to current directory
		return new File(filename);
	}

	public static void loadCropTypes() {
		cropTypes.clear();
		File cropFile = locateFile("crops.txt");
		
		if(cropFile.exists()) {
			try {
				Scanner input = new Scanner(cropFile);
				
				while(input.hasNextLine()) {
					String line = input.nextLine().trim();
					if(line.isEmpty()) continue; // ignore blank lines
					String[] parts = line.split(",");
					if(parts.length >= 5) {
						cropTypes.add(new CropType(parts[0], Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4])));
					} else if(parts.length >= 3) {
						cropTypes.add(new CropType(parts[0], Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), 4, 15));
					}
				}
				input.close();
				return;
			} catch (IOException | NumberFormatException e) {
				// fallback to built-in values if file parse fails
			}
		}
		
		cropTypes.add(new CropType("Wheat", 4.0, 5.0, 3, 15));
		cropTypes.add(new CropType("Corn", 6.0, 10.0, 4, 20));
		cropTypes.add(new CropType("Tomato", 8.0, 14.0, 5, 18));
		cropTypes.add(new CropType("Potato", 5.0, 8.0, 3, 18));
		cropTypes.add(new CropType("Carrot", 3.5, 6.0, 2, 12));
	}

	public static ArrayList<CropType> getCropTypes() {
		if(cropTypes.isEmpty()) {
			loadCropTypes(); // lazy-load crop data once when first needed
		}
		return cropTypes; // return cached crop type list
	}

	public static void loadCrops() {
		ArrayList<CropType> crops = getCropTypes(); // ensure crop data is loaded
		
		System.out.println("\nAvailable Crops and Seed Prices:");
		System.out.println("Name       | Seed Cost | Sell Value | Growth Days | Yield/Acre");
		System.out.println("---------------------------------------------------------------------");
		for(CropType crop : crops) {
			System.out.printf("%-10s | $%8.2f | $%9.2f | %11s | %s%n", crop.getName(), crop.getSeedCost(), crop.getSellValue(), crop.getGrowthDays() + " days", crop.getYieldPerAcre() + " units per acre");
		}
	}
}

public class ICS4U_FP {

	private static void displayMainMenu() {
		System.out.println("\n================================");
		System.out.println("Farm FRONTIER - Main Menu");
		System.out.println("================================");
		System.out.println("1. Start / Continue Day");
		System.out.println("2. Farm Management");
		System.out.println("3. Market / Trading");
		System.out.println("4. Buildings / Expansion");
		System.out.println("5. Reports");
		System.out.println("6. Save / Load");
		System.out.println("7. Help / Instructions");
		System.out.println("8. Exit");
		// prompt the player to choose the next action
		System.out.print("Choice: ");
	}

	/**
	 * Input Helper - Read an integer from the user with validation.
	 *
	 * PURPOSE: Ensures that menu selection and numeric input are safe and do not
	 * break the game when the player enters invalid text.
	 *
	 * COURSE CONCEPT: Defensive programming and input validation.
	 */
	private static int getIntInput(Scanner input, String prompt) {
		while (true) {
			// read a line from the user and guard against invalid text
			System.out.print(prompt);
			String line = input.nextLine().trim();
			try {
				return Integer.parseInt(line);
			} catch (NumberFormatException e) {
				System.out.println("Invalid input. Please enter a whole number.");
			}
		}
	}

	private static int getIntInput(Scanner input, String prompt, int min, int max) {
		while (true) {
			int value = getIntInput(input, prompt);
			// validate that the value lies inside the acceptable menu range
			if (value < min || value > max) {
				System.out.println("Please enter a number between " + min + " and " + max + ".");
			} else {
				return value;
			}
		}
	}

	/**
	 * Input Helper - Read a yes/no response from the user.
	 *
	 * PURPOSE: Converts user text into a boolean-style response and prevents
	 * invalid menu entries from causing unexpected behavior.
	 */
	private static String getYesNoInput(Scanner input, String prompt) {
		while (true) {
			// Ask the user a yes/no question and normalize the answer for comparison
			System.out.print(prompt);
			String line = input.nextLine().trim().toLowerCase();
			if (line.equals("y") || line.equals("n")) {
				return line;
			}
			// Report invalid responses and loop until the user enters a valid answer
			System.out.println("Invalid input. Please enter 'y' or 'n'.");
		}
	}

	/**
	 * Task Generation - Create daily player objectives.
	 *
	 * PURPOSE: Keeps the game engaging by generating actionable tasks based on
	 * current farm state and helps guide the player toward useful goals.
	 *
	 * COURSE CONCEPT: Logic conditions and dynamic task creation.
	 */
	private static void generateDailyTasks(Farm farm, TaskQueue taskQueue) {
		// inspect current inventory levels and create tasks to guide the player
		int milkQty = farm.getResourceQuantity("Milk");
		int wheatQty = farm.getResourceQuantity("Wheat");
		int salmonQty = farm.getResourceQuantity("Salmon");

		if(milkQty > 40) {
			// if milk is overstocked, create a sell task to avoid waste and encourage cash flow
			taskQueue.enqueue(new Task("Sell enough Milk so that Milk quantity is 40 or lower", "SELL_RESOURCE", "Milk", 40, 300));
		}
		if(wheatQty > 80) {
			// if wheat volume exceeds the safe storage threshold, recommend selling it
			taskQueue.enqueue(new Task("Sell enough Wheat so that Wheat quantity is 80 or lower", "SELL_RESOURCE", "Wheat", 80, 250));
		}
		if(salmonQty > 35) {
			// keep fish quantities manageable to prevent inventory overflow
			taskQueue.enqueue(new Task("Sell enough Salmon so that Salmon quantity is 35 or lower", "SELL_RESOURCE", "Salmon", 35, 400));
		}
		// Building tasks - suggest building more to reach useful minimum counts
		if(farm.getCurrentInventorySize() > farm.getInventoryCapacity() * 0.75 && farm.countBuildings("Warehouse") < 2) {
			// when inventory is more than 75% full, recommend extra warehouse space
			taskQueue.enqueue(new Task("Build at least 2 Warehouses to increase storage", "BUILD", "Warehouse", 2, 500));
		}
		if(farm.getMoney() > 12000 && farm.countBuildings("Greenhouse") < 2) {
			// with enough cash, suggest greenhouse investment for higher harvests
			taskQueue.enqueue(new Task("Build at least 2 Greenhouses to boost crop harvest", "BUILD", "Greenhouse", 2, 600));
		}
		if(farm.getMoney() > 20000 && farm.countBuildings("Supermarket") < 1) {
			// encourage revenue expansion once the farm has a strong cash reserve
			taskQueue.enqueue(new Task("Build a Supermarket to increase revenue from sales", "BUILD", "Supermarket", 1, 750));
		}
		if(farm.countAnimalsByName("Cow") < 6) {
			// prompt the player to grow a herd for more animal products
			taskQueue.enqueue(new Task("Raise at least 6 Cows for better production", "BUY_ANIMALS", "Cow", 6, 400));
		}
		if(farm.countAnimalsByName("Chicken") < 12) {
			// chickens are useful for egg income, so request a stable flock
			taskQueue.enqueue(new Task("Raise at least 12 Chickens for steady eggs", "BUY_ANIMALS", "Chicken", 12, 350));
		}
		if(farm.getDay() < 5) {
			// early game objective to encourage persistence through multiple days
			taskQueue.enqueue(new Task("Survive until day 5", "SURVIVE_DAYS", "", 5, 150));
		}
	}

	/**
	 * Day Flow Menu - Shows current day status and allows the player to advance.
	 *
	 * PURPOSE: Central part of the game loop where the player sees progress,
	 * reviews active tasks, and chooses to move forward by one day.
	 *
	 * COURSE CONCEPT: Game loop and state progression.
	 */
	private static void startDayMenu(Farm farm, Market market, EventLogLinkedList eventLog, ActionStack actionStack, TaskQueue taskQueue, Scanner input) {
		// create any new daily objectives before showing the current day summary
		generateDailyTasks(farm, taskQueue);
		
		System.out.println("\n===== Day Summary =====");
		farm.displayStatus();
		
		Task activeTask = taskQueue.peek();
		if(activeTask != null) {
			// show the current task and provide a small hint for the player
			System.out.println("Active Task: " + activeTask.getDescription());
			if(activeTask.getType().equals("SELL_RESOURCE")) {
				// selling tasks target excess inventory and help free storage
				System.out.println("Task Hint: Sell " + activeTask.getTarget() + " until quantity is " + activeTask.getAmount() + " or lower.");
			}
			else if(activeTask.getType().equals("BUILD")) {
				System.out.println("Task Hint: Invest in a " + activeTask.getTarget() + " soon to meet this goal.");
			}
			else if(activeTask.getType().equals("EARN_MONEY")) {
				System.out.println("Task Hint: Use market sales or faster production to reach the money goal.");
			}
			else if(activeTask.getType().equals("SURVIVE_DAYS")) {
				System.out.println("Task Hint: Keep your farm running until day " + activeTask.getAmount() + ".");
			}
		}
		else {
			System.out.println("No active task at the moment.");
		}
		
		System.out.printf("Storage: %d/%d%n", farm.getCurrentInventorySize(), farm.getInventoryCapacity());
		System.out.println("Tip: Use Market / Trading to sell, Buildings / Expansion to invest, and Reports to track progress.");
		System.out.println("\n1. Advance day");
		System.out.println("0. Return to main menu");
		int choice = getIntInput(input, "Choice: ", 0, 1);
		
		if(choice == 1) {
			// advance time by one day and process all daily updates
			farm.advanceDay();
			market.updatePrices();
			EventManager.randomEvent(farm, eventLog);
			actionStack.push("Advanced to Day " + farm.getDay());
			// check if the current task was completed by the day's actions
			taskQueue.checkFrontTask(farm);
		}
		else if(choice != 0) {
			System.out.println("Invalid choice. Returning to main menu.");
		}
	}

	/**
	 * Farm Management Menu - Manage farm operations and view data.
	 *
	 * PURPOSE: Allows the player to access inventory, crops, employees, and other
	 * farm-related controls without advancing the day.
	 *
	 * COURSE CONCEPTS: Menu-driven programming and object interaction.
	 */
	private static void farmManagementMenu(Market market, Farm farm, TaskQueue taskQueue, ActionStack actionStack, Scanner input) {
		boolean staying = true;
		
		while(staying) {
			System.out.println("\n===== FARM MANAGEMENT =====");
			System.out.println("View Information:");
			System.out.println("  1. Farm Status");
			System.out.println("  2. Inventory & Resources");
			System.out.println("  3. Buildings");
			System.out.println("  4. Employees");
			System.out.println("  5. Animals");
			System.out.println("\nSearch & Sort:");
			System.out.println("  6. Search Resource");
			System.out.println("  7. Sort Inventory by Name");
			System.out.println("\nFarming Actions:");
			System.out.println("  8. View Crop Types");
			System.out.println("  9. Plant Crops");
			System.out.println(" 10. Feed Animals");
			System.out.println("\nHiring:");
			System.out.println(" 11. Hire Employee");
			System.out.println("  0. Return to main menu\n");
			int choice = getIntInput(input, "Choice: ", 0, 11);
			
			switch(choice) {
			case 1:
				farm.displayStatus();
				break;
			case 2:
				// view current material holdings and produced resources
				farm.displayInventory(market);
				break;
				
			case 3:
				// view buildings currently purchased and placed on the farm map
				farm.displayBuildings();
				break;
				
			case 4:
				// display the list of hired staff and their roles
				farm.displayEmployees();
				break;
			
			case 5:
				farm.displayAnimals();
				break;

			case 6:
				System.out.print("Enter resource name: ");
				String name = input.nextLine();
				
				Resource result = farm.binarySearchResource(name);
				if(result != null) {
					System.out.println("\nResource Found:");
					System.out.println(result.getType() + " | " + result.getName() + " | Qty: " + result.getQuantity() + " | Unit $: " + String.format("%.2f", result.getValue()));
				}
				else {
					System.out.println("Resource not found.");
				}
				break;
				
			case 7:
				// sort inventory alphabetically so search is easier
				farm.sortInventoryByName();
				System.out.println("\nInventory sorted by name.");
				farm.displayInventory(market);
				break;
				
			case 8:
				ConfigLoader.loadCrops();
				break;
				
			case 9:
				plantCropMenu(farm, input);
				break;
				
			case 10:
				feedAnimalsMenu(farm, actionStack, input);
				break;
				
			case 11:
				hireEmployeeMenu(farm, actionStack, input);
				break;
				
			case 0:
				staying = false;
				break;
				
			default:
				System.out.println("Invalid choice.");
			}
		}
	}

	/**
	 * Plant Crop Menu - Select and plant crops from available types.
	 *
	 * PURPOSE: Introduces crop planting mechanics and uses data from external
	 * configuration (crops.txt) when available.
	 *
	 * COURSE CONCEPTS: File I/O config data and object creation.
	 */
	private static void plantCropMenu(Farm farm, Scanner input) {
		ArrayList<CropType> crops = ConfigLoader.getCropTypes();
		
		System.out.println("\n===== Plant Crops =====");
		for(int i = 0; i < crops.size(); i++) {
			CropType crop = crops.get(i);
			System.out.printf("%d. %s (Seed: $%.2f, Sell: $%.2f, %d days, %d yield/acre)%n", i + 1, crop.getName(), crop.getSeedCost(), crop.getSellValue(), crop.getGrowthDays(), crop.getYieldPerAcre());
		}
		int choice = getIntInput(input, "Choice: ", 1, crops.size());
		
		if(choice < 1 || choice > crops.size()) {
			System.out.println("Invalid crop selection.");
			return;
		}
		
		// capture chosen crop type and planting acreage
		CropType cropType = crops.get(choice - 1);
		int acres = getIntInput(input, "Acres to plant: ");
		
		if(acres <= 0) {
			System.out.println("You must plant at least one acre.");
			return;
		}
		
		double cost = cropType.getSeedCost() * acres; // calculate total seed expense
		String confirmPlant = getYesNoInput(input, "Confirm planting " + acres + " acres of " + cropType.getName() + " for $" + String.format("%.2f", cost) + "? (y/n): ");
		if(!confirmPlant.equals("y")) {
			System.out.println("Planting cancelled.");
			return;
		}
		
		Resource resource = farm.binarySearchResource(cropType.getName());
		if(resource == null) {
			// new crop type not currently tracked in inventory, so create a resource object first
			resource = new Crop(cropType.getName(), 0, cropType.getSellValue(), cropType.getGrowthDays(), cropType.getYieldPerAcre(), cropType.getSeedCost());
			if(!farm.addResource(resource)) {
				System.out.println("Inventory is full. Sell something before planting more crops.");
				return;
			}
		}
		// attempt to plant the crop and pay seed cost; result updates farm state
		if(farm.plantCrop(cropType.getName(), acres, cost)) {
			System.out.println("You planted " + acres + " acres of " + cropType.getName() + "!");
		}
		else {
			System.out.println("Planting failed or cancelled.");
		}
	}

	/**
	 * Hire Employee Menu - Hire a new worker for your farm.
	 *
	 * PURPOSE: Adds employees that provide ongoing bonuses and incur salary costs.
	 *
	 * COURSE CONCEPTS: Resource management and cost/benefit tradeoffs.
	 */
	private static void hireEmployeeMenu(Farm farm, ActionStack actionStack, Scanner input) {
		System.out.println("\n===== Hire Employee =====");
		System.out.println("1. Farmer ($1500/month) - +5 crop yield when harvesting");
		System.out.println("2. Fisher ($1800/month) - +4 fish production daily");
		int choice = getIntInput(input, "Choice: ", 1, 2);
		
		Employee newHire = null;
		double cost = 0;
		
		switch(choice) {
		case 1:
			newHire = new Employee("Mia", "Farmer", 1500);
			cost = 1500;
			break;
			
		case 2:
			newHire = new Employee("Liam", "Fisher", 1800);
			cost = 1800;
			break;
			
		default:
			System.out.println("Invalid choice.");
			return;
		}
		
		if(farm.getMoney() < cost) {
			System.out.println("Not enough money to hire this employee.");
			return;
		}
		// confirm before committing cash and employee slot
		String confirmHire = getYesNoInput(input, "Confirm hiring " + newHire.getName() + " (" + newHire.getRole() + ") for $" + String.format("%.2f", cost) + "? (y/n): ");
		if(!confirmHire.equals("y")) {
			System.out.println("Hiring cancelled.");
			return;
		}
		if(farm.addEmployee(newHire)) {
			farm.addMoney(-cost);
			actionStack.push("Hired " + newHire.getRole() + " " + newHire.getName());
			System.out.println("Hired " + newHire.getName() + " as a " + newHire.getRole() + "!");
		} else {
			System.out.println("No more employee slots available.");
		}
	}

	/**
	 * Feed Animals Menu - Use crops to feed livestock and improve production.
	 *
	 * PURPOSE: Demonstrates how different game systems interact: animals,
	 * crop resources, and player decisions.
	 *
	 * COURSE CONCEPTS: Object interaction and state mutation across classes.
	 */
	private static void feedAnimalsMenu(Farm farm, ActionStack actionStack, Scanner input) {
		System.out.println("\n===== Feed Animals =====");
		farm.displayAnimals(); // show current animals so player knows what can be fed
		
		System.out.println("\nAvailable Feed Resources:");
		System.out.println("  - Wheat: " + farm.getResourceQuantity("Wheat") + " units available");
		System.out.println("  - Corn:  " + farm.getResourceQuantity("Corn") + " units available");
		System.out.println("  - Tomato: " + farm.getResourceQuantity("Tomato") + " units available");
		
		System.out.print("\nAnimal to feed (Cow/Chicken): ");
		String animalName = input.nextLine();
		
		System.out.print("Feed resource (Wheat/Corn/Tomato): ");
		String feedResource = input.nextLine();
		
		// validate feed quantity input before attempting to feed animals
		int units = getIntInput(input, "Units to use as feed: ");

		String confirmFeed = getYesNoInput(input, "Confirm feed " + animalName + " with " + units + " " + feedResource + "? (y/n): ");
		if(confirmFeed.equals("y")) {
			// only push action if the feeding succeeded
			if(farm.feedAnimals(animalName, feedResource, units)) {
				actionStack.push("Fed " + animalName + " with " + units + " " + feedResource);
			}
		} else {
			System.out.println("Feeding cancelled.");
		}
	}

	/**
	 * Market Menu - Buy and sell resources, or trade animals.
	 *
	 * PURPOSE: Provides the economic mechanics of the game and lets the player
	 * convert production into money or sell livestock for cash.
	 *
	 * COURSE CONCEPTS: Economic modeling and action logging through the stack.
	 */
	private static void marketMenu(Farm farm, Market market, ActionStack actionStack, TaskQueue taskQueue, Scanner input) {
		boolean staying = true;
		
		while(staying) {
			System.out.println("\n===== MARKET / TRADING =====");
			System.out.println("1. View Market Prices");
			System.out.println("2. Sell Resource");
			System.out.println("3. Trade Animals");
			System.out.println("0. Return to main menu");
			int choice = getIntInput(input, "Choice: ", 0, 3);
			
			switch(choice) {
			case 1:
				// display the current market price list for available resources
				market.displayPrices();
				System.out.println("\n📝 PRICING EXPLANATION:");
				System.out.println("  Unit $: The price you receive per unit of resource");
				System.out.println("  Supermarket Bonus: +10% per supermarket owned");
				System.out.println("  Example: Selling 10 Wheat at $5 = $50 base");
				System.out.println("           With 1 Supermarket: $50 × 1.10 = $55");
				System.out.println("           With 2 Supermarkets: $50 × 1.20 = $60");
				break;
				
			case 2:
				// show inventory before asking the player what to sell
				farm.displayInventory(market);
				System.out.print("Resource Name: ");
				String resourceName = input.nextLine();
				int quantity = getIntInput(input, "Quantity: ");
				
				Resource res = farm.binarySearchResource(resourceName);
				if(res != null) {
					double unitPrice = market.getPrice(resourceName);
					double multiplier = 1 + (farm.countBuildings("Supermarket") * 0.10);
					double totalRevenue = unitPrice * quantity * multiplier;
					System.out.println("\nSale Calculation:");
					System.out.println("  Unit Price: $" + String.format("%.2f", unitPrice));
					System.out.println("  Quantity: " + quantity);
					System.out.println("  Supermarket Bonus: x" + String.format("%.2f", multiplier));
					System.out.println("  Total Revenue: $" + String.format("%.2f", totalRevenue));
				}
				
				String confirmSell = getYesNoInput(input, "Confirm sell " + quantity + " " + resourceName + "? (y/n): ");
				if(confirmSell.equals("y")) {
					farm.sellResource(resourceName, quantity, market, actionStack);
					taskQueue.checkFrontTask(farm);
				} else {
					System.out.println("Sale cancelled.");
				}
				break;
				
			case 3:
				// allow the player to trade livestock for cash and update game history
				farm.displayAnimals();
				System.out.print("Animal to trade (Cow/Chicken): ");
				String animalName = input.nextLine();
				int tradeQuantity = getIntInput(input, "Quantity to trade: ");
				String confirmTrade = getYesNoInput(input, "Confirm trade " + tradeQuantity + " " + animalName + "? (y/n): ");
				if(confirmTrade.equals("y")) {
					if(farm.tradeAnimals(animalName, tradeQuantity, actionStack)) {
						System.out.println("Animal trade complete.");
					}
				} else {
					System.out.println("Trade cancelled.");
				}
				break;
				
			case 0:
				staying = false;
				break;
				
			default:
				System.out.println("Invalid choice.");
			}
		}
	}

	/**
	 * Building Menu - Purchase buildings and evaluate farm layout.
	 *
	 * PURPOSE: Manages farm expansion and shows how spatial reasoning is used
	 * with the 2D farm map.
	 *
	 * COURSE CONCEPTS: Multi-dimensional arrays, BFS pathfinding, and building
	 * placement strategy.
	 */
	private static void buildingMenu(Farm farm, ActionStack actionStack, TaskQueue taskQueue, Scanner input) {
		boolean staying = true;
		
		while(staying) {
			System.out.println("\n===== BUILDINGS / EXPANSION =====");
			System.out.println("1. Buy Building");
			System.out.println("2. View Farm Map");
			System.out.println("3. Find Nearest Empty Location");
			System.out.println("4. Analyze Best Expansion Area");
			System.out.println("0. Return to main menu");
			int choice = getIntInput(input, "Choice: ", 0, 4);
			
			switch(choice) {
			case 1:
				farm.displayStatus();
				farm.displayBuildings();
				
				System.out.println("\n===== Available Buildings =====");
				System.out.println("1. Barn ($5000) - Boosts animal product yield");
				System.out.println("2. Fish Pond ($7000) - Produces fish daily");
				System.out.println("3. Greenhouse ($8000) - Boosts crop harvest");
				System.out.println("4. Warehouse ($12000) - Expands storage capacity");
				System.out.println("5. Supermarket ($20000) - Increases sale revenue");
				System.out.println("0. Exit (Return to Buildings Menu)");
				int buildingChoice = getIntInput(input, "Choose building: ", 0, 5);
				
				if(buildingChoice == 0) {
					System.out.println("Building purchase cancelled.");
					break;
				}
				
				int row = -1;
				int col = -1;
				System.out.print("Would you like a recommended building location? (y/n): ");
				String recommend = input.nextLine();
				
				if(recommend.equals("y")) {
					int centerRow = farm.getFarmMap().getRows() / 2;
					int centerCol = farm.getFarmMap().getCols() / 2;
					
					// locate the closest empty tile using breadth-first search from the map center
				int[] suggested = farm.getFarmMap().findNearestEmptyTileBFS(centerRow, centerCol);
					if(suggested != null) {
						System.out.println("Suggested location: Row " + (suggested[0] + 1) + ", Column " + (suggested[1] + 1));
						System.out.println("(These are the numbers shown on the map display)");
						String useSuggestion = getYesNoInput(input, "Use this location? (y/n): ");
						if(useSuggestion.equals("y")) {
							row = suggested[0];
							col = suggested[1];
						}
					}
					else {
						System.out.println("No empty tile is available for recommendation.");
					}
				}
                           if(row < 0 || col < 0) {
                               // if the player declined the suggestion or no suggested tile was available,
                               // ask the player to manually enter a valid building location.
                               System.out.println("\n===== Enter Building Location =====");
                               System.out.println("Note: Use the numbers shown on the Farm Map display");
                               row = getIntInput(input, "Row (1-" + farm.getFarmMap().getRows() + "): ", 1, farm.getFarmMap().getRows()) - 1;
                               col = getIntInput(input, "Column (1-" + farm.getFarmMap().getCols() + "): ", 1, farm.getFarmMap().getCols()) - 1;
                           }
                           boolean purchased = false;
                           // Confirm purchase before proceeding
					int buildingCost = 0;
				switch(buildingChoice) {
				case 1: buildingCost = 5000; break;
				case 2: buildingCost = 7000; break;
				case 3: buildingCost = 8000; break;
				case 4: buildingCost = 12000; break;
				case 5: buildingCost = 20000; break;
				default: buildingCost = 0; break;
				}
				String confirmBuy = getYesNoInput(input, "Confirm purchase of building option " + buildingChoice + " for $" + buildingCost + "? (y/n): ");
				if(confirmBuy.equals("y")) {
					switch(buildingChoice) {
				case 1:
					purchased = farm.buyAndPlaceBuilding(new Barn(), row, col, 'B');
					if(purchased) actionStack.push("Purchased Barn");
					break;
					
				case 2:
					purchased = farm.buyAndPlaceBuilding(new FishPond(), row, col, 'P');
					if(purchased) actionStack.push("Purchased Fish Pond");
					break;
					
				case 3:
					purchased = farm.buyAndPlaceBuilding(new Greenhouse(), row, col, 'G');
					if(purchased) actionStack.push("Purchased Greenhouse");
					break;
					
				case 4:
					purchased = farm.buyAndPlaceBuilding(new Warehouse(), row, col, 'W');
					if(purchased) actionStack.push("Purchased Warehouse");
					break;
					
				case 5:
					purchased = farm.buyAndPlaceBuilding(new Supermarket(), row, col, 'S');
					if(purchased) actionStack.push("Purchased Supermarket");
					break;
					
				default:
					System.out.println("Invalid building.");
				}
				} else {
					System.out.println("Purchase cancelled.");
				}

				if(purchased) {
					taskQueue.checkFrontTask(farm);
				} else {
					System.out.println("Building purchase failed. Ensure you have enough money and the tile is empty.");
				}
				break;
				
			case 2:
				farm.getFarmMap().displayMap();
				break;
				
			case 3:
				int bfsRow = getIntInput(input, "Enter starting row (1-" + farm.getFarmMap().getRows() + "): ", 1, farm.getFarmMap().getRows()) - 1;
				int bfsCol = getIntInput(input, "Enter starting column (1-" + farm.getFarmMap().getCols() + "): ", 1, farm.getFarmMap().getCols()) - 1;
				
				int[] location = farm.getFarmMap().findNearestEmptyTileBFS(bfsRow, bfsCol);
				if(location != null) {
					System.out.println("\nSuggested nearest empty tile:");
					System.out.println("Row: " + (location[0] + 1) + ", Column: " + (location[1] + 1));
				}
				else {
					System.out.println("No empty tile found.");
				}
				break;
				
			case 4:
					int[] bestZone = farm.getFarmMap().findBestExpansionArea();
					int connectedLand = 0;
					
					if(bestZone != null) {
						connectedLand = bestZone[2];
						System.out.println("\n===== BEST EXPANSION AREA =====");
						System.out.println("Starting location: Row " + (bestZone[0] + 1) + ", Column " + (bestZone[1] + 1));
						System.out.println("Connected empty tiles: " + connectedLand);
						System.out.println("\nAnalysis: This contiguous region is ideal for building clusters.");
						
						if(connectedLand >= 9) {
							System.out.println("Status: EXCELLENT - Very large expansion opportunity!");
						} else if(connectedLand >= 5) {
							System.out.println("Status: GOOD - Good space for multiple buildings");
						} else {
							System.out.println("Status: LIMITED - Room for 1-2 buildings");
						}
					} else {
						System.out.println("No empty zone found for expansion.");
					}
					break;
					
			case 0:
				staying = false;
				break;
				
			default:
				System.out.println("Invalid choice.");
			}
		}
	}

	/**
	 * Reports Menu - View detailed farm performance and game history.
	 *
	 * PURPOSE: Lets the player inspect finances, inventory, events, and tasks.
	 *
	 * COURSE CONCEPTS: Aggregation of object data and user feedback presentation.
	 */
	private static void reportsMenu(Farm farm, EventLogLinkedList eventLog, ActionStack actionStack, TaskQueue taskQueue, Scanner input) {
		boolean staying = true;
		
		while(staying) {
			System.out.println("\n===== Reports =====");
			System.out.println("1. Financial Report");
			System.out.println("2. Inventory Report");
			System.out.println("3. View Event Log");
			System.out.println("4. View Recent Actions");
			System.out.println("5. View Tasks");
			System.out.println("6. View Farm Map");
			System.out.println("0. Return to main menu");
			int choice = getIntInput(input, "Choice: ", 0, 6);
			
			switch(choice) {
			case 1:
				farm.displayFinancialReport();
				break;
				
			case 2:
				farm.displayInventoryReport();
				break;
				
			case 3:
				eventLog.displayEvents();
				break;
				
			case 4:
				actionStack.displayActions();
				break;
				
			case 5:
				taskQueue.displayTasks();
				break;
				
			case 6:
				farm.getFarmMap().displayMap();
				break;
				
			case 0:
				staying = false;
				break;
				
			default:
				System.out.println("Invalid choice.");
			}
		}
	}

	/**
	 * Save/Load Menu - Persist and restore game progress.
	 *
	 * PURPOSE: Demonstrates file input/output by storing and reloading full game
	 * state from save.txt.
	 *
	 * COURSE CONCEPTS: File persistence, serialization, and backward compatibility.
	 */
	private static void saveLoadMenu(Farm farm, ActionStack actionStack, Scanner input) {
		boolean staying = true;
		
		while(staying) {
			System.out.println("\n===== Save / Load =====");
			System.out.println("1. Save Game");
			System.out.println("2. Load Game");
			System.out.println("0. Return to main menu");
			int choice = getIntInput(input, "Choice: ", 0, 2);
			
			switch(choice) {
			case 1:
				// attempt to save the current farm state to disk
				if(SaveManager.saveFarm(farm)) {
					actionStack.push("Saved Game");
				}
				break;
			
			case 2:
				// restore a previously saved farm state from save.txt
				if(SaveManager.loadFarm(farm)) {
					actionStack.push("Loaded Game");
				}
				break;
			
			case 0:
				staying = false;
				break;
				
			default:
				System.out.println("Invalid choice.");
			}
		}
	}

	/**
	 * Help Menu - Display game instructions and strategy tips.
	 *
	 * PURPOSE: Provides players with guidance on how to use menus and how
	 * buildings and resources interact.
	 */
	private static void helpMenu() {
		System.out.println("\n===== Help / Instructions =====");
		System.out.println("Objective: Build a thriving farm by growing crops, raising animals, fishing, selling resources, and buying useful buildings.");
		System.out.println("\nUse the Start / Continue Day menu to see the current day, review your active task, then advance time.");
		System.out.println("\nFarm Management contains status, inventory, buildings, employee information, and crop data.");
		System.out.println("\nMarket / Trading is where you check prices and sell resources for money.");
		System.out.println("\nBuildings / Expansion lets you purchase buildings, view the map, and find good placement spots.");
		System.out.println("\nReports show finances, event history, actions, tasks, and map layout.");
		System.out.println("\nSave / Load keeps your progress in save.txt and reloads map layout plus building positions.");
		System.out.println("\nMap symbols: E = Empty, B = Barn, P = Pond, G = Greenhouse, W = Warehouse, S = Supermarket");
		System.out.println("\nBuildings help your farm: Barn increases animal product yields, Fish Pond increases fish, Greenhouse boosts crop harvest, Warehouse increases storage, Supermarket increases sale revenue.");
		System.out.println("\nComplete tasks in order to earn extra rewards and keep your farm moving forward.");
	}

	/**
	 * Main Method - Entry point for the Farm Frontier application.
	 *
	 * PURPOSE: Initializes game objects, displays instructions, and runs the
	 * main menu loop until the player exits or goes bankrupt.
	 *
	 * COURSE CONCEPTS: Program execution flow, object creation, and loop control.
	 */
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);

		// Intro / Game Description: give players a clear overview at startup
		System.out.println("\n===============================================");
		System.out.println("Welcome to Farm FRONTIER - A Farming Simulation");
		System.out.println("===============================================");
		System.out.println("Objective: Build a thriving farm by growing crops, raising animals, fishing, and trading.");
		System.out.println("\nGame Flow: Each day you can manage your farm, plant crops, hire employees, buy buildings, and sell resources.");
		System.out.println("\nAdvance days to trigger harvests, market updates, and random events. Complete tasks to earn rewards.");
		System.out.println("\nTips: Keep inventory under capacity, diversify income streams, and expand storage/buildings strategically.");
		System.out.println("\nControls: Use the numbered menus to navigate. Enter values when prompted and press Enter to confirm.");
		System.out.println("\nSave/Load: Use Save / Load from the main menu to preserve progress in save.txt.");
		System.out.println("\nHave fun and good luck growing your farm!\n");

		// initialize the game world and helper objects for the main loop
		Farm farm = new Farm();
		
		Market market = new Market();
		
		EventLogLinkedList eventLog = new EventLogLinkedList();
		
		ActionStack actionStack = new ActionStack();
		
		TaskQueue taskQueue = new TaskQueue();
		taskQueue.enqueue(new Task("Sell enough Milk so that Milk quantity is 15 or lower", "SELL_RESOURCE", "Milk", 15, 300));
		taskQueue.enqueue(new Task("Build at least 2 Warehouses to expand storage", "BUILD", "Warehouse", 2, 500));
		taskQueue.enqueue(new Task("Reach at least $18000 in cash", "EARN_MONEY", "", 18000, 700));

		// Remove any tasks that are already satisfied at startup (anywhere in queue)
		taskQueue.removeCompletedTasks(farm);

		boolean running = true;

		// main game loop: keep showing menu options until the player exits or loses
		while (running) {
			if(farm.bankrupt()) {
				System.out.println("\nYou have gone bankrupt!");
				running = false;
				break;
			}

			displayMainMenu();
			int choice = getIntInput(input, "", 1, 8);

			switch(choice) {
			case 1:
				startDayMenu(farm, market, eventLog, actionStack, taskQueue, input);
				break;
				
			case 2:
				farmManagementMenu(market, farm, taskQueue, actionStack, input);
				break;
				
			case 3:
				marketMenu(farm, market, actionStack, taskQueue, input);
				break;
				
			case 4:
				buildingMenu(farm, actionStack, taskQueue, input);
				break;
				
			case 5:
				reportsMenu(farm, eventLog, actionStack, taskQueue, input);
				break;
				
			case 6:
				saveLoadMenu(farm, actionStack, input);
				break;
				
			case 7:
				helpMenu();
				break;
				
			case 8:
				running = false;
				System.out.println("\n\n\nThank you for playing Farm Frontier!");
				break;
				
			default:
				System.out.println("Invalid choice.");
			}
		}
		input.close();
	}
}