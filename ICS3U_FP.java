/*
 * ICS3U_FP.java
 * Author: Haiyang
 * Date: June 15, 2025
 * Project Title: Elemental Destiny (Yahtzee Redefined)
 *
 * Description:
 * "Elemental Destiny" is an enchanting dice game where each roll summons one of six elemental forces.
 * Players roll 5 dice (numbered 1 to 5 for ordering purposes) with 6 faces:
 *   Face 1: Fire   - Reflects aggressiveness and power.
 *   Face 2: Water  - Reflects adaptability and revival.
 *   Face 3: Earth  - Reflects shelter and security.
 *   Face 4: Air    - Reflects speed and quickness.
 *   Face 5: Thunder- Reflects immediate effect.
 *   Face 6: Wild   - May be used as any element for scoring (only one conversion allowed per roll).
 *
 * Each turn consists of an initial roll, up to 2 re-rolls (choosing which dice to hold), and then
 * the player chooses one scoring category (from 11 available) that has not yet been filled.
 *
 * Scoring categories include:
 *   1. Fire Fury          (Single Element: Fire; 10 points each; wild converts one additional die)
 *   2. Water Flow         (Single Element: Water; 5 points each; wild converts one additional die)
 *   3. Earth Guardian     (Single Element: Earth; 3 points each; bonus of 5 points if 3 or more Earth faces)
 *   4. Air Gust           (Single Element: Air; 4 points each; wild converts one additional die)
 *   5. Thunder Strike     (Single Element: Thunder; 8 points each; wild converts one additional die)
 *   6. Elemental Storm    (Five of a kind; bonus 50 points)
 *   7. Full Spectrum      (At least one of each non-wild element (Fire, Water, Earth, Air, Thunder); bonus 30 points;
 *                          one wild can substitute for one missing element)
 *   8. Fusion Combo       (Full House: 3 of one element and 2 of another; bonus 25 points; wild may substitute 1 die)
 *   9. Elemental Chain    (Exact order: Die1: Fire, Die2: Thunder, Die3: Water, Die4: Earth, Die5: Air;
 *                          bonus 40 points; wild not allowed)
 *   10. Wild Frenzy       (If 3 or more Wilds appear; bonus 20 points)
 *   11. Chance            (Sum of individual dice values: Fire=10, Water=5, Earth=3, Air=4, Thunder=8; Wild=0)
 *
 * Extra Bonuses (not chosen as a category):
 *   - Glory Element Card: +30 points if all categories (1-10) have been given a score greater than 0.
 *   - Supreme Life Extension Card: If the total points from categories 1-5 exceed 100, the player gets an extra turn.
 *
 * Additional Features:
 *   - Players may choose single-player or two-player mode.
 *   - After each round, a score sheet is displayed.
 *   - Enchanting messages and thematic language are used throughout the game.
 *
 * Concepts used: arrays, methods, strings, loops, ASCII output, basic data types, arithmetic operations, and user input.
 */

import java.util.Scanner;
import java.util.Random;

public class ICS3U_FP {

	// Elemental constants for dice faces
	public static final int FIRE = 1;
	public static final int WATER = 2;
	public static final int EARTH = 3;
	public static final int AIR = 4;
	public static final int THUNDER = 5;
	public static final int WILD = 6;

	// Array mapping dice values to element names (index 0 is unused)
	public static final String[] ELEMENT_NAMES = {"", "Fire", "Water", "Earth", "Air", "Thunder", "Wild"};

	// Points for single element scoring
	public static final int POINT_FIRE = 10;
	public static final int POINT_WATER = 5;
	public static final int POINT_EARTH = 3;
	public static final int POINT_AIR = 4;
	public static final int POINT_THUNDER = 8;
	// Wild has no inherent point value

	// Game configuration constants
	public static final int NUM_DICE = 5;
	public static final int MAX_REROLLS = 2;
	// 11 scoring categories mean 11 rounds for each player.
	public static final int TOTAL_ROUNDS = 11;

	// Scoring category names (order corresponds to score sheet indices 0 to 10)
	public static final String[] CATEGORY_NAMES = {
			"Fire Fury", 
			"Water Flow", 
			"Earth Guardian", 
			"Air Gust", 
			"Thunder Strike",
			"Elemental Storm", 
			"Full Spectrum", 
			"Fusion Combo", 
			"Elemental Chain", 
			"Wild Frenzy",
			"Chance"
	};

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		Random random = new Random();

		// Enchanting title and instructions.
		displayTitle();
		displayInstructions();
		// Display scoring categories and rules for player reference.
		displayCategoriesAndRules();

		// Ask player for game mode: 1 for single-player, 2 for two-player.
		System.out.println("Enter '1' for single-player mode or '2' for two-player mode:");
		String modeInput = scanner.nextLine();
		int mode = 1; // default to single-player
		if (modeInput.length() > 0) {
			// Convert first character into number (basic conversion)
			char ch = modeInput.charAt(0);
			if (ch >= '1' && ch <= '9') {
				mode = ch - '0';
			}
		}
		if (mode == 2) {
			twoPlayerGame(scanner, random);
		} else {
			singlePlayerGame(scanner, random);
		}

		scanner.close();
	}

	//-------------------------------------------------------------------------
	// Method: displayCategoriesAndRules
	// Displays all scoring categories and their rules.
	//-------------------------------------------------------------------------
	public static void displayCategoriesAndRules() {
		System.out.println("--------------------------------------------------");
		System.out.println("Scoring Categories & Rules:");
		System.out.println("1) Fire Fury: Score all Fire dice. Each Fire face is worth " + POINT_FIRE + " points; a wild can act as an extra Fire.");
		System.out.println("2) Water Flow: Score all Water dice. Each Water face is worth " + POINT_WATER + " points; a wild can act as an extra Water.");
		System.out.println("3) Earth Guardian: Score all Earth dice. Each Earth face is worth " + POINT_EARTH + " points; if 3 or more Earth faces, add a bonus of 5 points.");
		System.out.println("4) Air Gust: Score all Air dice. Each Air face is worth " + POINT_AIR + " points; a wild can act as an extra Air.");
		System.out.println("5) Thunder Strike: Score all Thunder dice. Each Thunder face is worth " + POINT_THUNDER + " points; a wild can act as an extra Thunder.");
		System.out.println("6) Elemental Storm: If all 5 dice show the same element (wilds allowed as substitutes), you earn a bonus of 50 points.");
		System.out.println("7) Full Spectrum: If you have at least one of each non-Wild element (Fire, Water, Earth, Air, Thunder) in a roll, you earn 30 points. A wild can substitute one missing element.");
		System.out.println("8) Fusion Combo: Score a Full House (3 of one element and 2 of another, wild may substitute one die). Bonus: 25 points.");
		System.out.println("9) Elemental Chain: The dice must show the exact order: Die1=Fire, Die2=Thunder, Die3=Water, Die4=Earth, Die5=Air. Bonus: 40 points. (Wilds not allowed)");
		System.out.println("10) Wild Frenzy: If 3 or more Wild faces appear in the final roll, you earn a bonus of 20 points.");
		System.out.println("11) Chance: Sum the points of all dice (using Fire=" + POINT_FIRE + ", Water=" + POINT_WATER + 
				", Earth=" + POINT_EARTH + ", Air=" + POINT_AIR + ", Thunder=" + POINT_THUNDER + ", Wild=0).");
		System.out.println("Extra Bonuses:");
		System.out.println(" - Glory Element Card: +30 points if categories 1 to 10 all have scores greater than 0.");
		System.out.println(" - Supreme Life Extension Card: If total points from categories 1-5 exceed 100, you earn an extra turn.");
		System.out.println("--------------------------------------------------");
	}

	//-------------------------------------------------------------------------
	// Method: singlePlayerGame
	// Runs the game loop for a single player.
	//-------------------------------------------------------------------------
	public static void singlePlayerGame(Scanner scanner, Random random) {
		// Ask for player's name
		System.out.println("Enter your name, brave adventurer:");
		String playerName = scanner.nextLine();
		// Initialize score sheet array for 11 categories; use -1 to indicate an unused category.
		int[] scoreSheet = new int[TOTAL_ROUNDS];
		int i;
		for (i = 0; i < TOTAL_ROUNDS; i++) {
			scoreSheet[i] = -1;
		}

		// Loop through each round
		int round;
		for (round = 1; round <= TOTAL_ROUNDS; round++) {
			System.out.println("--------------------------------------------------");
			System.out.println("Round " + round + " of destiny begins, " + playerName + "!");
			// Display a brief enchanting message.
			System.out.println("When the dice fall, the balance of the elements is revealed...");

			// Roll phase: initial roll + up to MAX_REROLLS re-rolls.
			int[] dice = rollDice(random);
			System.out.println("Your initial roll:");
			displayDice(dice);

			int r;
			for (r = 1; r <= MAX_REROLLS; r++) {
				System.out.println("Do you wish to invoke fate further? Type 'yes' to re-roll, or anything else to hold:");
				String resp = scanner.nextLine();
				if (!resp.equalsIgnoreCase("yes")) {
					break;
				}
				// Ask which dice to hold. (Enter digits 1-5 to hold; entering 0 means re-roll all.)
				System.out.println("Enter the numbers (1-5) of the dice you wish to hold (or 0 to re-roll all):");
				String holdInput = scanner.nextLine();
				boolean[] hold = parseHoldInput(holdInput);
				dice = reRollDice(dice, hold, random);
				System.out.println("After re-roll " + r + ", your dice read:");
				displayDice(dice);
			}

			// End of turn: Display the final dice result.
			System.out.println("Final dice for this round:");
			displayDice(dice);
			// Display available scoring categories.
			displayScoreSheet(playerName, scoreSheet);

			// Ask the player to choose a scoring category number (1 to 11).
			int catChoice = getCategoryChoice(scanner, scoreSheet);

			// Calculate score based on chosen category.
			int roundScore = 0;
			if (catChoice == 1) { // Fire Fury
				roundScore = scoreSingleElement(dice, FIRE, POINT_FIRE);
			} else if (catChoice == 2) { // Water Flow
				roundScore = scoreSingleElement(dice, WATER, POINT_WATER);
			} else if (catChoice == 3) { // Earth Guardian
				roundScore = scoreEarthGuardian(dice);
			} else if (catChoice == 4) { // Air Gust
				roundScore = scoreSingleElement(dice, AIR, POINT_AIR);
			} else if (catChoice == 5) { // Thunder Strike
				roundScore = scoreSingleElement(dice, THUNDER, POINT_THUNDER);
			} else if (catChoice == 6) { // Elemental Storm
				roundScore = scoreElementalStorm(dice);
			} else if (catChoice == 7) { // Full Spectrum
				roundScore = scoreFullSpectrum(dice);
			} else if (catChoice == 8) { // Fusion Combo
				roundScore = scoreFusionCombo(dice);
			} else if (catChoice == 9) { // Elemental Chain
				roundScore = scoreElementalChain(dice);
			} else if (catChoice == 10) { // Wild Frenzy
				roundScore = scoreWildFrenzy(dice);
			} else if (catChoice == 11) { // Chance
				roundScore = scoreChance(dice);
			}
			// Update score sheet with the round score.
			scoreSheet[catChoice - 1] = roundScore;
			System.out.println("Your score of " + roundScore + " has been recorded in '" + CATEGORY_NAMES[catChoice - 1] + "'.");
			displayScoreSheet(playerName, scoreSheet);
		} // end FOR round

		// End-of-game bonus evaluations.
		int bonus = 0;
		if (checkGloryElement(scoreSheet)) {
			System.out.println("Glory Element Card unlocked! You gain an extra 30 points for your mastery of all elements.");
			bonus += 30;
		}
		if (checkSupremeLifeExtension(scoreSheet)) {
			System.out.println("Supreme Life Extension Card granted! Your single-element prowess exceeds mortal limits.");
			// Award an extra turn (bonus round) that lets you improve one category.
			extraTurn(scanner, random, scoreSheet);
		}

		// Calculate the total score and display the final result
		int totalScore = sumScoreSheet(scoreSheet) + bonus;
		System.out.println("--------------------------------------------------");
		System.out.println("Game Over, " + playerName + "! Your final total score is: " + totalScore);
	}

	//-------------------------------------------------------------------------
	// Method: twoPlayerGame
	// Runs the game loop for two players.
	//-------------------------------------------------------------------------
	public static void twoPlayerGame(Scanner scanner, Random random) {
		// Get each player’s name.
		System.out.println("Player One, enter your name, brave warrior:");
		String player1 = scanner.nextLine();
		System.out.println("Player Two, enter your name, fearless challenger:");
		String player2 = scanner.nextLine();

		// Initialize score sheets (11 categories each) for both players; use -1 to indicate an unused category.
		int[] scoreSheet1 = new int[TOTAL_ROUNDS];
		int[] scoreSheet2 = new int[TOTAL_ROUNDS];
		int i;
		for (i = 0; i < TOTAL_ROUNDS; i++) {
			scoreSheet1[i] = -1;
			scoreSheet2[i] = -1;
		}

		// Loop through each round
		int round;
		// Each player will take 11 rounds.
		for (round = 1; round <= TOTAL_ROUNDS; round++) {
			System.out.println("==================================================");
			// Player 1 turn
			System.out.println("Round " + round + " begins for " + player1 + "!");

			// Roll phase: initial roll + up to MAX_REROLLS re-rolls.
			int[] dice1 = rollDice(random);
			System.out.println("Your initial roll:");
			displayDice(dice1);

			int r;
			for (r = 1; r <= MAX_REROLLS; r++) {
				System.out.println("Do you wish to re-roll any dice? Type 'yes' to re-roll:");
				String resp = scanner.nextLine();
				if (!resp.equalsIgnoreCase("yes")) {
					break;
				}
				// Ask which dice to hold. (Enter digits 1-5 to hold; entering 0 means re-roll all.)
				System.out.println("Enter the numbers (1-5) of dice you want to hold (or 0 to re-roll all):");
				String holdInput = scanner.nextLine();
				boolean[] hold = parseHoldInput(holdInput);
				dice1 = reRollDice(dice1, hold, random);
				System.out.println("After re-roll " + r + ":");
				displayDice(dice1);
			}

			// End of turn: Display the final dice result.
			System.out.println("Final dice for this round:");
			displayDice(dice1);
			// Display available scoring categories.
			displayScoreSheet(player1, scoreSheet1);

			// Ask the player to choose a scoring category number (1 to 11).
			int catChoice1 = getCategoryChoice(scanner, scoreSheet1);
			// Calculate score based on chosen category.
			int roundScore1 = 0;
			if (catChoice1 == 1) { // Fire Fury
				roundScore1 = scoreSingleElement(dice1, FIRE, POINT_FIRE);
			} else if (catChoice1 == 2) { // Water Flow
				roundScore1 = scoreSingleElement(dice1, WATER, POINT_WATER);
			} else if (catChoice1 == 3) { // Earth Guardian
				roundScore1 = scoreEarthGuardian(dice1);
			} else if (catChoice1 == 4) { // Air Gust
				roundScore1 = scoreSingleElement(dice1, AIR, POINT_AIR);
			} else if (catChoice1 == 5) { // Thunder Strike
				roundScore1 = scoreSingleElement(dice1, THUNDER, POINT_THUNDER);
			} else if (catChoice1 == 6) { // Elemental Storm
				roundScore1 = scoreElementalStorm(dice1);
			} else if (catChoice1 == 7) { // Full Spectrum
				roundScore1 = scoreFullSpectrum(dice1);
			} else if (catChoice1 == 8) { // Fusion Combo
				roundScore1 = scoreFusionCombo(dice1);
			} else if (catChoice1 == 9) { // Elemental Chain
				roundScore1 = scoreElementalChain(dice1);
			} else if (catChoice1 == 10) { // Wild Frenzy
				roundScore1 = scoreWildFrenzy(dice1);
			} else if (catChoice1 == 11) { // Chance
				roundScore1 = scoreChance(dice1);
			}
			// Update score sheet with the round score.
			scoreSheet1[catChoice1 - 1] = roundScore1;
			System.out.println(player1 + ", your score of " + roundScore1 + " in '" + CATEGORY_NAMES[catChoice1 - 1] + "' has been recorded.");
			displayScoreSheet(player1, scoreSheet1);

			System.out.println("==================================================");
			// Player 2 turn
			System.out.println("Now, " + player2 + ", it is your turn in Round " + round + "!");

			// Roll phase: initial roll + up to MAX_REROLLS re-rolls.
			int[] dice2 = rollDice(random);
			System.out.println("Your initial roll:");
			displayDice(dice2);
			for (r = 1; r <= MAX_REROLLS; r++) {
				System.out.println("Do you wish to re-roll any dice? (Type 'yes' to re-roll)");
				String resp = scanner.nextLine();
				if (!resp.equalsIgnoreCase("yes")) {
					break;
				}
				// Ask which dice to hold. (Enter digits 1-5 to hold; entering 0 means re-roll all.)
				System.out.println("Enter the numbers (1-5) of dice you wish to hold (or 0 to re-roll all):");
				String holdInput = scanner.nextLine();
				boolean[] hold = parseHoldInput(holdInput);
				dice2 = reRollDice(dice2, hold, random);
				System.out.println("After re-roll " + r + ":");
				displayDice(dice2);
			}

			// End of turn: Display the final dice result.
			System.out.println("Final dice for this round:");
			displayDice(dice2);
			// Display available scoring categories.
			displayScoreSheet(player2, scoreSheet2);

			// Ask the player to choose a scoring category number (1 to 11).
			int catChoice2 = getCategoryChoice(scanner, scoreSheet2);
			// Calculate score based on chosen category.
			int roundScore2 = 0;
			if (catChoice2 == 1) { // Fire Fury
				roundScore2 = scoreSingleElement(dice2, FIRE, POINT_FIRE);
			} else if (catChoice2 == 2) { // Water Flow
				roundScore2 = scoreSingleElement(dice2, WATER, POINT_WATER);
			} else if (catChoice2 == 3) { // Earth Guardian
				roundScore2 = scoreEarthGuardian(dice2);
			} else if (catChoice2 == 4) { // Air Gust
				roundScore2 = scoreSingleElement(dice2, AIR, POINT_AIR);
			} else if (catChoice2 == 5) { // Thunder Strike
				roundScore2 = scoreSingleElement(dice2, THUNDER, POINT_THUNDER);
			} else if (catChoice2 == 6) { // Elemental Storm
				roundScore2 = scoreElementalStorm(dice2);
			} else if (catChoice2 == 7) { // Full Spectrum
				roundScore2 = scoreFullSpectrum(dice2);
			} else if (catChoice2 == 8) { // Fusion Combo
				roundScore2 = scoreFusionCombo(dice2);
			} else if (catChoice2 == 9) { // Elemental Chain
				roundScore2 = scoreElementalChain(dice2);
			} else if (catChoice2 == 10) { // Wild Frenzy
				roundScore2 = scoreWildFrenzy(dice2);
			} else if (catChoice2 == 11) { // Chance
				roundScore2 = scoreChance(dice2);
			}
			// Update score sheet with the round score.
			scoreSheet2[catChoice2 - 1] = roundScore2;
			System.out.println(player2 + ", your score of " + roundScore2 + " in '" + CATEGORY_NAMES[catChoice2 - 1] + "' has been recorded.");
			displayScoreSheet(player2, scoreSheet2);
		} // end FOR round

		// End-of-game bonus evaluations.
		int bonus1 = 0;
		if (checkGloryElement(scoreSheet1)) {
			System.out.println(player1 + " has unlocked the Glory Element Card! +30 points bonus.");
			bonus1 = 30;
		}
		int bonus2 = 0;
		if (checkGloryElement(scoreSheet2)) {
			System.out.println(player2 + " has unlocked the Glory Element Card! +30 points bonus.");
			bonus2 = 30;
		}
		if (checkSupremeLifeExtension(scoreSheet1)) {
			System.out.println(player1 + " earns the Supreme Life Extension Card and gains an extra turn!");
			// Award an extra turn (bonus round) that lets you improve one category.
			extraTurn(scanner, random, scoreSheet1);
		}
		if (checkSupremeLifeExtension(scoreSheet2)) {
			System.out.println(player2 + " earns the Supreme Life Extension Card and gains an extra turn!");
			// Award an extra turn (bonus round) that lets you improve one category.
			extraTurn(scanner, random, scoreSheet2);
		}

		// Calculate and display the total score for each player
		int totalScore1 = sumScoreSheet(scoreSheet1) + bonus1;
		int totalScore2 = sumScoreSheet(scoreSheet2) + bonus2;
		System.out.println("--------------------------------------------------");
		System.out.println(player1 + "'s final total score: " + totalScore1);
		System.out.println(player2 + "'s final total score: " + totalScore2);

		// Compare between the two final scores and decide the winner
		if (totalScore1 > totalScore2) {
			System.out.println("The cosmos crowns " + player1 + " as the Elemental Master!");
		} else if (totalScore2 > totalScore1) {
			System.out.println("The cosmos crowns " + player2 + " as the Elemental Master!");
		} else {
			System.out.println("A tie! The balance of the elements is perfectly maintained.");
		}
	}

	//-------------------------------------------------------------------------
	// Utility Methods
	//-------------------------------------------------------------------------

	// Displays the enchanting title.
	public static void displayTitle() {
		System.out.println("--------------------------------------------------");
		System.out.println("         Elemental Destiny: Yahtzee Redefined");
		System.out.println(" A game beyond this world in elemental power and strategy");
		System.out.println("--------------------------------------------------");
	}

	// Displays basic game instructions.
	public static void displayInstructions() {
		System.out.println("Instructions:");
		System.out.println("1. You will roll " + NUM_DICE + " dice with 6 faces: Fire, Water, Earth, Air, Thunder, Wild.");
		System.out.println("2. Wild may take on any element (only one conversion allowed per roll).");
		System.out.println("3. You get up to " + MAX_REROLLS + " re-rolls (3 rolls total per turn).");
		System.out.println("4. After your final roll, choose one available scoring category to record your points.");
		System.out.println("   Available categories include single element totals and combination bonuses.");
		System.out.println("5. At game end, bonus cards (Glory Element and Supreme Life Extension) may be awarded.");
		System.out.println("May fate and the elements guide your hand!");
	}

	// Rolls NUM_DICE dice (random numbers from 1 to 6) and returns them in an array.
	public static int[] rollDice(Random random) {
		int[] dice = new int[NUM_DICE];
		int i;
		for (i = 0; i < NUM_DICE; i++) {
			dice[i] = random.nextInt(6) + 1; // random number from 1 to 6
		}
		return dice;
	}

	// Displays each die's value alongside its elemental name.
	public static void displayDice(int[] dice) {
		int i;
		for (i = 0; i < dice.length; i++) {
			System.out.print("Die " + (i + 1) + ": " + ELEMENT_NAMES[dice[i]] + "\t");
		}
		System.out.println();
	}

	// Re-rolls only dice that are not held. 'hold' is a boolean array indicating which dice to keep.
	public static int[] reRollDice(int[] dice, boolean[] hold, Random random) {
		int i;
		for (i = 0; i < dice.length; i++) {
			if (!hold[i]) {
				dice[i] = random.nextInt(6) + 1;
			}
		}
		return dice;
	}

	// Parses a string input for which dice to hold.
	// Examines each character; digits 1-5 set the corresponding index to true.
	// If the character '0' is found, no dice are held.
	public static boolean[] parseHoldInput(String input) {
		boolean[] hold = new boolean[NUM_DICE];
		int i;
		// Initialize all holds to false.
		for (i = 0; i < NUM_DICE; i++) {
			hold[i] = false;
		}
		int length = input.length();
		for (i = 0; i < length; i++) {
			char c = input.charAt(i);
			if (c == '0') {
				// If '0' is entered, re-roll all dice.
				int j;
				for (j = 0; j < NUM_DICE; j++) {
					hold[j] = false;
				}
				break;
			}
			if (c >= '1' && c <= '5') {
				int index = c - '1';
				hold[index] = true;
			}
		}
		return hold;
	}

	// Displays the current score sheet with category names.
	// Unused categories appear as "----".
	public static void displayScoreSheet(String playerName, int[] scoreSheet) {
		System.out.println("Score Sheet for " + playerName + ":");
		int i;
		for (i = 0; i < TOTAL_ROUNDS; i++) {
			System.out.print((i + 1) + ") " + CATEGORY_NAMES[i] + " : ");
			if (scoreSheet[i] == -1) {
				System.out.print("----");
			} else {
				System.out.print(scoreSheet[i]);
			}
			System.out.print("    ");
			if ((i + 1) % 2 == 0)
				System.out.println();
		}
		System.out.println();
	}

	// Prompts the player to choose a scoring category.
	// Only accepts categories that have not yet been used.
	public static int getCategoryChoice(Scanner scanner, int[] scoreSheet) {
		int choice = 0;
		boolean valid = false;
		while (!valid) {
			System.out.println("Enter the number (1 to 11) of the category in which you wish to record your score:");
			String input = scanner.nextLine();
			// Basic conversion: if length is 1, convert first digit; if 2, combine two digits.
			if (input.length() == 1) {
				char c = input.charAt(0);
				if (c >= '1' && c <= '9') {
					choice = c - '0';
				}
			} else if (input.length() == 2) {
				char c1 = input.charAt(0);
				char c2 = input.charAt(1);
				if (c1 >= '1' && c1 <= '9' && c2 >= '0' && c2 <= '9') {
					choice = (c1 - '0') * 10 + (c2 - '0');
				}
			}
			// Check if choice is within valid range and category not already used.
			if (choice >= 1 && choice <= 11) {
				if (scoreSheet[choice - 1] == -1) {
					valid = true;
				} else {
					System.out.println("That category has already been used. Please choose another.");
				}
			} else {
				System.out.println("Invalid input. Please enter a number from 1 to 11.");
			}
		}
		return choice;
	}

	// Returns the score for a single element category.
	// Counts dice showing the specified element and adds one extra if any wild is present.
	public static int scoreSingleElement(int[] dice, int element, int pointValue) {
		int i;
		int count = 0;
		int wildCount = 0;
		for (i = 0; i < dice.length; i++) {
			if (dice[i] == element) {
				count++;
			} else if (dice[i] == WILD) {
				wildCount++;
			}
		}
		// Only one wild conversion is allowed.
		if (wildCount > 0) {
			count = count + 1; 
		}
		return count * pointValue;
	}

	// Special scoring for Earth Guardian.
	// Adds bonus 5 points if 3 or more Earth faces appear.
	public static int scoreEarthGuardian(int[] dice) {
		int baseScore = scoreSingleElement(dice, EARTH, POINT_EARTH);
		int i;
		int earthCount = 0;
		for (i = 0; i < dice.length; i++) {
			if (dice[i] == EARTH) {
				earthCount++;
			}
		}
		if (earthCount >= 3) {
			baseScore = baseScore + 5;
		}
		return baseScore;
	}

	// Scores Elemental Storm (five of a kind).
	// Returns 50 points if the condition is met.
	public static int scoreElementalStorm(int[] dice) {
		int i, count, wildCount = 0;
		// Count wilds
		for (i = 0; i < dice.length; i++) {
			if (dice[i] == WILD) {
				wildCount++;
			}
		}
		// For each non-wild candidate element, check if count is 4 (with one wild) or 5.
		int candidate;
		for (candidate = FIRE; candidate <= THUNDER; candidate++) {
			count = 0;
			for (i = 0; i < dice.length; i++) {
				if (dice[i] == candidate) {
					count++;
				}
			}
			if (count == 5 || (count == 4 && wildCount >= 1)) {
				return 50;
			}
		}
		return 0;
	}

	// Returns the bonus score for Full Spectrum.
	// Requires at least one of each non-wild element (Fire, Water, Earth, Air, Thunder).
	// One wild can substitute for one missing element.
	public static int scoreFullSpectrum(int[] dice) {
		int i;
		int wildCount = 0;
		int missing = 0;
		// Count wilds.
		for (i = 0; i < dice.length; i++) {
			if (dice[i] == WILD) {
				wildCount++;
			}
		}
		// For each required element from FIRE to THUNDER, check if present.
		int element;
		for (element = FIRE; element <= THUNDER; element++) {
			int count = 0;
			for (i = 0; i < dice.length; i++) {
				if (dice[i] == element) {
					count++;
				}
			}
			if (count == 0) {
				missing++;
			}
		}
		if (missing == 0) {
			return 30;
		} else if (missing == 1 && wildCount >= 1) {
			return 30;
		}
		return 0;
	}

	// Scores Fusion Combo (a Full House).
	// Returns 25 points if a valid Full House is made (wild may substitute one die).
	public static int scoreFusionCombo(int[] dice) {
		int i;
		int wildCount = 0;
		// Limit wild conversion to 1.
		for (i = 0; i < dice.length; i++) {
			if (dice[i] == WILD) {
				wildCount++;
			}
		}
		if (wildCount > 0) {
			wildCount = 1;
		}
		// Count occurrences for each element.
		int counts[] = new int[7]; // index 1-6 used
		for (i = 0; i < dice.length; i++) {
			int face = dice[i];
			if (face != WILD) {
				counts[face]++;
			}
		}
		// Check all pairs of distinct elements.
		int e1, e2;
		for (e1 = FIRE; e1 <= THUNDER; e1++) {
			for (e2 = FIRE; e2 <= THUNDER; e2++) {
				if (e1 == e2) continue;
				int needed1 = 0;
				if (counts[e1] < 3) {
					needed1 = 3 - counts[e1];
				}
				int needed2 = 0;
				if (counts[e2] < 2) {
					needed2 = 2 - counts[e2];
				}
				if (needed1 + needed2 <= wildCount) {
					return 25;
				}
			}
		}
		for (e1 = FIRE; e1 <= THUNDER; e1++) {
			if (counts[e1] == 5) {
				return 25;
			}
		}
		return 0;
	}

	// Returns the bonus score for Elemental Chain.
	// The required exact order of dice is: Die1: Fire, Die2: Thunder, Die3: Water, Die4: Earth, Die5: Air.
	// Wilds are NOT allowed.
	public static int scoreElementalChain(int[] dice) {
		if (dice[0] == FIRE && dice[1] == THUNDER && dice[2] == WATER && dice[3] == EARTH && dice[4] == AIR) {
			return 40;
		}
		return 0;
	}

	// Scores Wild Frenzy.
	// Returns 20 points if there are 3 or more wilds in the final roll.
	public static int scoreWildFrenzy(int[] dice) {
		int i, count = 0;
		for (i = 0; i < dice.length; i++) {
			if (dice[i] == WILD) {
				count++;
			}
		}
		if (count >= 3) {
			return 20;
		}
		return 0;
	}

	// Scores the Chance category by summing dice values (Wild scores 0).
	public static int scoreChance(int[] dice) {
		int i, score = 0;
		for (i = 0; i < dice.length; i++) {
			int value = dice[i];
			if (value == FIRE) {
				score += POINT_FIRE;
			} else if (value == WATER) {
				score += POINT_WATER;
			} else if (value == EARTH) {
				score += POINT_EARTH;
			} else if (value == AIR) {
				score += POINT_AIR;
			} else if (value == THUNDER) {
				score += POINT_THUNDER;
			} else if (value == WILD) {
				score += 0; // Wild scores 0 here.
			}
		}
		return score;
	}

	// Sums the values in the score sheet (ignores unused categories marked as -1).
	public static int sumScoreSheet(int[] sheet) {
		int total = 0;
		int i;
		for (i = 0; i < sheet.length; i++) {
			if (sheet[i] > 0) {
				total += sheet[i];
			}
		}
		return total;
	}

	// Checks if Glory Element Card bonus is earned.
	// That requires that all single element (indices 0-4) and combination categories (indices 5-9) have score > 0.
	public static boolean checkGloryElement(int[] sheet) {
		int i;
		for (i = 0; i < 10; i++) {
			if (sheet[i] <= 0) { // if zero or unused, bonus is not granted.
				return false;
			}
		}
		return true;
	}

	// Checks if Supreme Life Extension Card is earned.
	// That requires that the total points from single element categories (indices 0-4) exceed 100.
	public static boolean checkSupremeLifeExtension(int[] sheet) {
		int sum = 0;
		int i;
		for (i = 0; i < 5; i++) {
			if (sheet[i] > 0) {
				sum += sheet[i];
			}
		}
		if (sum > 100) {
			return true;
		}
		return false;
	}

	// Provides an extra turn to allow the player to attempt updating one category's score.
	// The player rolls the dice again (3 rolls allowed) and then chooses one category to update if the new score is greater.
	public static void extraTurn(Scanner scanner, Random random, int[] scoreSheet) {
		System.out.println("You have earned an extra turn! Fate gives you one more chance to improve your destiny.");

		// Roll phase: initial roll + up to MAX_REROLLS re-rolls.
		int[] dice = rollDice(random);
		System.out.println("Your extra turn initial roll:");
		displayDice(dice);

		int r;
		for (r = 1; r <= MAX_REROLLS; r++) {
			System.out.println("Extra turn: Do you wish to re-roll any dice? Type 'yes' to re-roll:");
			String resp = scanner.nextLine();
			if (!resp.equals("yes")) {
				break;
			}
			// Ask which dice to hold. (Enter digits 1-5 to hold; entering 0 means re-roll all.)
			System.out.println("Enter the numbers (1-5) of the dice you wish to hold (or 0 to re-roll all):");
			String holdInput = scanner.nextLine();
			boolean[] hold = parseHoldInput(holdInput);
			dice = reRollDice(dice, hold, random);
			System.out.println("After extra re-roll " + r + ":");
			displayDice(dice);
		}

		// End of turn: Display the final dice result.
		System.out.println("Final extra turn dice:");
		displayDice(dice);
		// Display available scoring categories.
		System.out.println("Here is your score sheet:");
		displayScoreSheet("Extra Turn", scoreSheet);

		// Ask the player to choose a scoring category number (1 to 11).
		System.out.println("Choose one category (1 to 11) to update with your extra turn score (if higher):");
		int catChoice = getExtraTurnCategoryChoice(scanner);
		// Calculate score based on chosen category.
		int newScore = 0;
		if (catChoice == 1) { // Fire Fury
			newScore = scoreSingleElement(dice, FIRE, POINT_FIRE);
		} else if (catChoice == 2) { // Water Flow
			newScore = scoreSingleElement(dice, WATER, POINT_WATER);
		} else if (catChoice == 3) { // Earth Guardian
			newScore = scoreEarthGuardian(dice);
		} else if (catChoice == 4) { // Air Gust
			newScore = scoreSingleElement(dice, AIR, POINT_AIR);
		} else if (catChoice == 5) { // Thunder Strike
			newScore = scoreSingleElement(dice, THUNDER, POINT_THUNDER);
		} else if (catChoice == 6) { // Elemental Storm
			newScore = scoreElementalStorm(dice);
		} else if (catChoice == 7) { // Full Spectrum
			newScore = scoreFullSpectrum(dice);
		} else if (catChoice == 8) { // Fusion Combo
			newScore = scoreFusionCombo(dice);
		} else if (catChoice == 9) { // Elemental Chain
			newScore = scoreElementalChain(dice);
		} else if (catChoice == 10) { // Wild Frenzy
			newScore = scoreWildFrenzy(dice);
		} else if (catChoice == 11) { // Chance
			newScore = scoreChance(dice);
		}

		// Check if the intended category to update has a lower score than the new score
		// If yes, update the score
		// Otherwise, not recommended to update the score
		if (newScore > scoreSheet[catChoice - 1]) {
			System.out.println("Your extra turn improved the score in '" + CATEGORY_NAMES[catChoice - 1] + "' from " + scoreSheet[catChoice - 1] + " to " + newScore + "!");
			scoreSheet[catChoice - 1] = newScore;
		} else {
			System.out.println("Alas, your extra turn did not improve that category.");
		}
		displayScoreSheet("Extra Turn", scoreSheet);
	}

	// Method for extra turn category selection (allows updating used categories)
	public static int getExtraTurnCategoryChoice(Scanner scanner) {
		int choice = 0;
		boolean valid = false;
		while (!valid) {
			System.out.println("Enter the number (1 to 11) of the category you wish to update with your extra turn score:");
			String input = scanner.nextLine();
			// Basic conversion: if length is 1, convert first digit; if 2, combine two digits.
			if (input.length() == 1) {
				char c = input.charAt(0);
				if (c >= '1' && c <= '9') {
					choice = c - '0';
				}
			}
			else if (input.length() == 2) {
				char c1 = input.charAt(0);
				char c2 = input.charAt(1);
				if (c1 >= '1' && c1 <= '9' && c2 >= '0' && c2 <= '9') {
					choice = (c1 - '0') * 10 + (c2 - '0');
				}
			}
			// Check if choice is within valid range
			if (choice >= 1 && choice <= 11) {
				valid = true;
			} else {
				System.out.println("Invalid input. Please enter a number from 1 to 11.");
			}
		}
		return choice;
	}
}