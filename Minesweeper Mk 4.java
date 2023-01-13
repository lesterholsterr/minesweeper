/*
Minesweeper.java
Author: Matthew Yang
Last Modified: 05/22/22
This program allows the user to play minesweeper through the standard output
and permanently stores user information using text files.

Current Version: Mark IV
   Uploaded to Google Drive
   Updated error message outputs
   Finalized method header comments
   No errors found

Features List
   - After each action, the program will output the number of flags the user still has
   - If the user tries to do an action on a flagged square, the program will ask them if they would
      like to dig anyways, keep flagged, or unflag the square.
   - The first square that a user digs will never be a mine. If it is, a new board will be secretly
      generated that does not have a mine in that location
   - If the user digs a square with value 0, the adjacent squares are automatically revealed, and the
      0 is replaced with a space (' ') for asthetic purposes

Change Log
   - Implemented text file storage system
   - Able to now random generate a minesweeper char array with specified rows, columns, and 
      mines that is filled with 'X' for mines and numbers for non-mined squares
   - Able to now output the empty minesweeper grid and replace each grid as the user requests
      to dig or flag that grid through a 4 method recursive structure
   - Able to check if the user has won or lost and send an appropriate message
   - Base game fully programmed but still needs more testing
   - Added the ability to unflag a square that was previously flagged
   - Added a counter for mines remaining based on the number of squares flagged by the player
   - Added the "first guess security" feature so that if the user's first guess is a mine,
      the program continuously generates new boards until one where that first guess is not
      a mine is generated
   - After much scrutiny, successfully implemented the auto reveal feature if the user digs
      a tile with value 0.
   - Updated some of the error messages which were vaguely written
*/

import java.util.*;
import java.io.*;

public class Minesweeper
{
   
   //Static Variable Declaration
   //Static integers
   static int numberOfRows = -1;
   static int numberOfCols = -1;
   static int numberOfGrids = -1;
   static int numberOfMines = -1;
   
   static int numberOfWins = 0;
   static int numberOfLosses = 0;
   static int lastWinRows = 0;
   static int lastWinCols = 0;
   static int lastWinMines = 0;
   static double winRate = 0;
   
   static int rowChoice = -1;
   static int colChoice = -1;
   static int digOrFlag = -1;
   
   static int squaresDug;
   static int flagsRemaining;
   static int winCon = 0;                          //winCon = 0 if neither won nor lost, winCon = 1 if won, winCon = 2 if lost
   
   //Static chars
   static char[][] privateBoard;
   static char[][] publicBoard;
   
   //Static booleans
   static boolean inputValid = false;              //Generic variable used to take user input through do-while loops
   static boolean firstDig = true;
   static boolean firstDigMine = true;             //Is true if the user's first dig is a mine. Is set to true by default and becomes 
                                                   //false when it is confirmed that the first dig is not a mine.
   //Static Strings
   static String fileName = "";
   
   /*
   outputStats()
   
   This method outputs 5 lines to the standard output telling the user the number of wins and losses
   they've had, their win percentage, and the board dimensions and number of mines in their most recent
   win. It has no parameters because it users static variables which are initialized in the main method
   using a file scanner once the user provides their username, and they are updated in updateStats().
   */
   public static void outputStats ()
   {
      System.out.println("Wins:  " + numberOfWins);
      System.out.println("Losses:  " + numberOfLosses);
      System.out.printf("Win rate:  %.2f%s\n", winRate, "%");
      System.out.println("Size of most recent win:  " + lastWinRows + "x" + lastWinCols);
      System.out.println("Mines in most recent win:  " + lastWinMines);
   }
   
   /*
   createBoard()
   
   returns char[][] - The returned 2D char array is stored as a static array called privateBoard, and
   represents the Minesweeper board that the player has requested if it was fully unrevealed.
   
   This method creates and returns a 2D char array with the number rows and columns equal to the user's
   input from the main method. It then generates random locations and replaces those locations in the 
   char array with 'X' signifying mines until the number of mines is equal to the user's input from the
   main method. Finally, the method fills the remaining empty squares based on how many mines are 
   adjacent to each tile.
   */
   public static char[][] createBoard()
   {
      //Variable and Constant Declaration
      final int RADIX = 10;
      
      int minesPlaced = 0;
      int mineCount = 0;
      int x, y;                        //Random numbers used to plant mines
      char[][] privateBoard = new char[numberOfRows][numberOfCols];
      
      while (minesPlaced < numberOfMines)
      {
         //Generating random numbers for x and y
         x = (int)(Math.random() * numberOfRows);
         y = (int)(Math.random() * numberOfCols);
         
         //Randomly filling in privateBoard with mines (indicated as 'X')
         if (privateBoard[x][y] != 'X')
         {
            privateBoard[x][y] = 'X';
            minesPlaced++;
         }
      }
      
      /*The use of the if statement prevents this method from outputting this line each time a new board is generated.
      Otherwise, if the user's first dig is a mine, a new board is generated and this message outputs again. Instead,
      the process of generating the new board should be done in secret.*/
      if (!firstDigMine)
      {
         System.out.println("The mines have been planted!");
      }
      
      //Filling in the remaining squares of privateBoard with numbers based on how many adjacent mines there are
      for (int i = 0; i < numberOfRows; i++)
      {
         for (int j = 0; j < numberOfCols; j++)
         {
            if (privateBoard[i][j] != 'X')
            {
               for (int k = i-1; k <= i+1; k++)
               {
                  for (int l = j-1; l <= j+1; l++)
                  {
                     if (k >= 0 && l >= 0 && k < numberOfRows && l < numberOfCols)
                     {
                        if (privateBoard[k][l] == 'X')
                        {
                           mineCount++;
                        }
                     }
                  }
               }
               
               //The Character.forDigit(int digit, int RADIX) method converts an integer (digit) of base RADIX
               //into a char value. It is used to make the mineCount integer compatible with the char array.
               privateBoard[i][j] = Character.forDigit(mineCount, RADIX);
               mineCount = 0;
            }
         }
      }
      
      return privateBoard;
   }
   
   /*
   printBoard()
   
   This method outputs the elements of the static 2D char array publicBoard, which represents
   the current state of the user's minesweeper game along with the board's formatting. This method
   is part of a 4 method recursive loop which begins with the main method calling on printBoard().
   */
   public static void printBoard ()
   {
      
      //Column Numbers
      System.out.print("   ");
      for (int i = 1; i <= numberOfCols; i++)
      {
         System.out.printf("  %-2d", i);
      }
      System.out.println("");
      
      //Top Row (---)
      System.out.print("   ");
      for (int i = 1; i <= numberOfCols; i++)
      {
         System.out.print("----");
      }
      System.out.print("-\n");
      
      //Actual Board
      for (int i = 0; i < numberOfRows; i++)
      {
         System.out.printf("%-3d| ", i+1);
         for (int j = 0; j < numberOfCols; j++)
         {
            System.out.print(publicBoard[i][j] + " | ");
         }
         System.out.println("");
      }
      
      //Bottom Row (---)
      System.out.print("   ");
      for (int i = 1; i <= numberOfCols; i++)
      {
         System.out.print("----");
      }
      System.out.print("-\n");
      
      areYouWinningSon();
   }
   
   /*
   areYouWinningSon()
   
   This method checks the 2D array publicBoard to see if the user has won, lost, or neither each
   time the user digs or flags a square and it is revelaed on publicBoard. The program then changes
   the value of the static int variable winCon and calls on the methods winMessage() or loseMessage()
   if winCon = 1 or 2 respectively. This method is part of a 4 method recursive loop, and this method
   gets called by printBoard().
   */
   public static void areYouWinningSon()
   {
      //Resets the variable squaresDug each time this method is executed.
      squaresDug = 0;
      
      //Scanning every element of publicBoard
      for (int i = 0; i < numberOfRows; i++)
      {
         for (int j = 0; j < numberOfCols; j++)
         {
            //Setting winCon = 2 if an X appears on publicBoard (meaning the user has dug a mine)
            if (publicBoard[i][j] == 'X')
            {
               winCon = 2;
            }
            //Incrementing squaresDug if publicBoard at that element is a number
            else if (publicBoard[i][j] != '_' && publicBoard[i][j] != 'M')
            {
               squaresDug++;
            }
         }
      }
      
      //Calling on loseMessage() if winCon = 2
      if (winCon == 2)
      {
         loseMessage();
      }
      //Calling on winMessage() if squaresDug = all of the non-mined squares
      else if (squaresDug == (numberOfGrids - numberOfMines))
      {
         winCon = 1;
         winMessage();
      }
      //Outputting an appropriate message based on the user's previous action if the user has neither won nor lost
      else
      {
         if (rowChoice != -1 && colChoice != -1 && digOrFlag != 2 && digOrFlag != 3)
         {
            System.out.println(rowChoice + "-" + colChoice + " is surrounded by " + privateBoard[rowChoice-1][colChoice-1] + " mines!");
         }
         else if (digOrFlag == 2)
         {
            System.out.println("Flagged " + rowChoice + "-" + colChoice + ".");
         }
         else if (digOrFlag == 3)
         {
            System.out.println("Unflagged " + rowChoice + "-" + colChoice + ".");
         }
         
         //Outputting the number of flags remaining that the user has
         System.out.println("Flags Remaining: " + flagsRemaining + "\n");
         
         digOrFlag();
      }
      
   }
   
   /*
   digOrFlag()
   
   This method takes user inputs for a row and column number that the user would like to make an action on.
   It then provides the user with an appropriate list of options (Ex. Dig, Flag, Unflag, Keep flagged) depending
   on if the square the user chose has already been revelaed, is currently empty, or is currently flagged. This 
   method is part of a 4 method recursive loop, and this method gets called by areYouWinningSon().
   */
   public static void digOrFlag()
   {
      //Variable Declaration
      Scanner sc = new Scanner(System.in);
      boolean alreadyDug = false;
      boolean alreadyFlagged = false;
      
      digOrFlag = 0;
      
      do
      {
         //Taking input for rowChoice
         inputValid = false;
         do
         {
            alreadyDug = false;
            alreadyFlagged = false;
            try
            {
               System.out.print("Choose a row:  ");
               rowChoice = sc.nextInt();
               
               if (rowChoice > 0 && rowChoice <= numberOfRows)
               {
                  inputValid = true;
               }
               else
               {
                  System.out.println("Invalid input. Please enter an integer between 1 and " + numberOfRows + " (inclusive)");
               }
            }
            catch (InputMismatchException e)
            {
               System.out.println("Invalid input. Please enter an integer between 1 and " + numberOfRows + " (inclusive)");
               sc.nextLine();
            }
         } while (!inputValid);
         
         //Taking input for colChoice
         inputValid = false;
         do
         {
            try
            {
               System.out.print("Choose a column:  ");
               colChoice = sc.nextInt();
               
               if (colChoice > 0 && colChoice <= numberOfCols)
               {
                  inputValid = true;
               }
               else
               {
                  System.out.println("Invalid input. Please enter an integer between 1 and " + numberOfCols + " (inclusive)");
               }
            }
            catch (InputMismatchException e)
            {
               System.out.println("Invalid input. Please enter an integer between 1 and " + numberOfCols + " (inclusive)");
               sc.nextLine();
            }
         } while (!inputValid);
         
         //Outputting an appropriate message if the square is currently flagged and taking input for digOrFlag
         if (publicBoard[rowChoice-1][colChoice-1] == 'M')
         {
            inputValid = false;
            do
            {
               try
               {
                  System.out.print("You have already flagged this square. Would you like to [1] DIG ANYWAYS, [2] KEEP FLAGGED, or [3] UNFLAG?  ");
                  digOrFlag = sc.nextInt();
                  
                  if (digOrFlag == 1 || digOrFlag == 2 || digOrFlag == 3)
                  {
                     inputValid = true;
                  }
                  else
                  {
                     System.out.println("Invalid input. Please enter either 1, 2, or 3");
                  }
               }
               catch (InputMismatchException e)
               {
                  System.out.println("Invalid input. Please enter either 1, 2, or 3");
                  sc.nextLine();
               }
            } while (!inputValid);
            
            if (digOrFlag == 3)
            {
               flagsRemaining++;
               System.out.println("Alright, unflagged " + rowChoice + "-" + colChoice + ". Here is the updated board:");
            }
            else if (digOrFlag == 2)
            {
               System.out.println("Got it, then you will have to select a different square.\n");
               alreadyFlagged = true;
            }
            else
            {
               flagsRemaining++;
            }
         }
         //Outputting an appropriate message if the square has already been dug
         else if (publicBoard[rowChoice-1][colChoice-1] != '_')
         {
            System.out.println("You have already dug this square. Please try again.\n");
            alreadyDug = true;
         }
         
      } while (alreadyDug || alreadyFlagged);
      
      //Taking input for digOrFlag when the user enters a valid input
      inputValid = false;
      if (publicBoard[rowChoice-1][colChoice-1] != 'M')
      {
         do
         {
            try
            {
               System.out.print("Would you like to [1] DIG or [2] FLAG?  ");
               digOrFlag = sc.nextInt();
               
               if (digOrFlag == 1)
               {
                  inputValid = true;
               }
               else if (digOrFlag == 2)
               {
                  inputValid = true;
                  flagsRemaining--;
               }
               else
               {
                  System.out.println("Invalid input. Please enter either 1 or 2.");
               }
            }
            catch (InputMismatchException e)
            {
               System.out.println("Invalid input. Please enter either 1 or 2.");
               sc.nextLine();
            }
         } while (!inputValid);
      }
      
      updateBoard();
      
   }
   
   /*
   updateBoard()
   
   This method updates publicBoard based on the user's request. If the user wants to dig, publicBoard
   at that element becomes equal to privateBoard, (which is the full board created in the method 
   createBoard()). If the user wants to flag or unflag, publicBoard at that element becomes 'M' or 
   '_' respectively. This method is also responsible for making sure the user's first guess is never
   a mine by instantly returning to the main method if the first guess is a mine. It is also responsible
   for automatically revealing the surrounding tiles if the user digs a tile with the value of 0 by
   calling on revealZero(int i, int j). This method is part of a 4 method recursive loop, and this method 
   gets called by digOrFlag().
   */
   public static void updateBoard()
   {
      //Variable Declaration
      boolean noZero;
      
      //Checking if the user's first dig is a mine by assigning new values for firstDigMine and firstDig appropriately
      if (firstDigMine && digOrFlag == 1 && privateBoard[rowChoice-1][colChoice-1] == 'X')
      {
         firstDigMine = true;
         firstDig = false;
         return;
      }
      else
      {
         firstDigMine = false;
         firstDig = false;
      }
      
      //If user has dug this square, reveal it
      if (digOrFlag == 1)
      {
         publicBoard[rowChoice-1][colChoice-1] = privateBoard[rowChoice-1][colChoice-1];
      }
      //If user has flagged this square, replace it with 'M'
      else if (digOrFlag == 2)
      {
         publicBoard[rowChoice-1][colChoice-1] = 'M';
      }
      //If user wants to unflage the square, replace it with '_'
      else
      {
         publicBoard[rowChoice-1][colChoice-1] = '_';
      }
      
      //Scanning every element of publicBoard
      do
      {
         noZero = true;
         //If a 0 was dug, reveal the surrounding squares automatically by calling on revealZero(i, j)
         for (int i = 0; i < numberOfRows; i++)
         {
            for (int j = 0; j < numberOfCols; j++)
            {
               if (publicBoard[i][j] == '0')
               {
                  revealZero(i, j);
                  noZero = false;
               }
            }
         }
      } while (!noZero);
      
      printBoard();
   }
   
   /*
   revealZero()
   
   int rowIndex - The row index number of an element in publicBoard equal to '0'
   int colIndex - The column index number of an element in publicBoard equal to '0'
   
   This method is an extension of the method updateBoard. It gets called when the user digs a tile
   with the value 0. This method updates the value of publicBoard for those surrounding squares and
   changes the value of 0 to become ' '. This is both for asthetic purposes, and also so the program
   knows that this tile has been checked already and does not repeatedly call on this method, since it
   is called whenever publicBoard contains an element with the value '0'.
   */
   public static void revealZero(int rowIndex, int colIndex)
   {
      //Reveal all surrounding squares by udpating publicBoard
      for (int i = rowIndex-1; i <= rowIndex+1; i++)
      {
         for (int j = colIndex-1; j <= colIndex+1; j++)
         {
            if (i >= 0 && j >= 0 && i < numberOfRows && j < numberOfCols)
            {
               if (i == rowIndex && j == colIndex)
               {
                  publicBoard[i][j] = ' ';
               }
               else if (publicBoard[i][j] == '_')
               {
                  publicBoard[i][j] = privateBoard[i][j];
               }
               
            }
         }
      }
   }
   
   /*
   winMessage()
   
   This method outputs a message for the user if they have dug all the squares that
   are not mines. It then calls on updateStats() to update the user's stats by overwriting
   the user's text file.
   */
   public static void winMessage()
   {
      System.out.println("You cleared the field! YOU WIN!! \\o/");
      System.out.println("Your stats are now...");
      updateStats();
   }
   
   /*
   loseMessage()
   
   This method outputs a message for the user if they have dug a mine. It then calls on 
   updateStats() to update the user's stats by overwriting the user's text file.
   */
   public static void loseMessage()
   {
      System.out.println(" *** BOOOOOOOOOM! ***  X_x");
      System.out.println("You hit a mine and lost. Better luck next time!");
      System.out.println("\nYour stats are now...");
      updateStats();
   }
   
   /*
   updateStats()
   
   This method updates the user's stats by assigning new values to several static variables
   depending on whether the user won or lost. It then uses a buffered writer to write these
   updated stats to the user's text file. After that, the method calls on outputStats() to
   print the updated stats for the user to see.
   */
   public static void updateStats()
   {
      //Changing the variable values that represent the stats
      if (winCon == 1)
      {
         numberOfWins++;
         lastWinRows = numberOfRows;
         lastWinCols = numberOfCols;
         lastWinMines = numberOfMines;
      }
      else
      {
         numberOfLosses++;
      }
      
      winRate = (double)numberOfWins / (numberOfWins + numberOfLosses) * 100;
      
      //Writing these updated variables to the user's file
      try
      {
         BufferedWriter out = new BufferedWriter(new FileWriter(fileName, false));
         out.write(numberOfWins + "\n");
         out.write(numberOfLosses + "\n");
         out.write(lastWinRows + "\n");
         out.write(lastWinCols + "\n");
         out.write(lastWinMines + "\n");
         
         out.close();
      }
      catch (IOException e)
      {
         System.out.println("IO Exception " + e + " at " + fileName);
      }
      
      outputStats();
   }
   
   
   public static void main(String[] args)
   {
      //Variable Declaration
      Scanner sc = new Scanner(System.in);
      
      //Declaring int and double variables
      int newOrReturning = -1;
      int maxMines = -1;
      int minMines = -1;
      
      int playAgain = 1;
      
      //Declaring String variables
      String welcomeMessage = "Welcome to ICS Minesweeper";
      String username = "";
      
      //Declaring boolean variables
      boolean firstTime = true;
      
      //Welcome message
      System.out.println("========================================");
      System.out.printf("|%32s%8s\n", welcomeMessage, "|");
      System.out.println("========================================");
      
      //Collecting player information
      do
      {
         try
         {
            //Collecting player username
            System.out.println("Hi there! What's your username?");
            username = sc.nextLine();
            
            //Collecting input of new or returning player
            System.out.println("\nAre you a [1] new player or a [2] returning player?");
            newOrReturning = sc.nextInt();
            
            //Assigning a value for fileName
            fileName = username + ".txt";
            
            //Creating a new text file for new players
            if (newOrReturning == 1)
            {
               try
               {
                  //Declaring a BufferedWriter
                  BufferedWriter out = new BufferedWriter(new FileWriter(fileName, false));
                  
                  //Writing 5 lines of 0
                  for (int i = 0; i < 5; i++)
                  {
                     out.write("0");
                     out.newLine();
                  }
                  
                  out.close();
               }
               catch (IOException e)
               {
                  System.out.println("IO exception " + e);
               }
               inputValid = true;
            }
            //Outputting stats for returning players before the game begins
            else if (newOrReturning == 2)
            {
               //Declaring a File Scanner and variables to store read values
               Scanner fs = new Scanner(new File(fileName));
               
               //Reading current stats (and calculating winRate) from <username>.txt
               numberOfWins =  fs.nextInt();
               numberOfLosses =  fs.nextInt();
               winRate = (double)numberOfWins / (numberOfWins + numberOfLosses) * 100;
               lastWinRows =  fs.nextInt();
               lastWinCols =  fs.nextInt();
               lastWinMines =  fs.nextInt();
               
               //Outputting current stats
               System.out.println("\nWelcome back " + username + "!");
               outputStats();
               inputValid = true;
            }
            //Error message if the user inputs an integer that is not 1 or 2
            else
            {
               System.out.println("Please enter only one of the integer options provided.\n");
               sc.nextLine();
            }
         }
         catch (InputMismatchException e)
         {
            System.out.println("Please enter only one of the integer options provided.\n");
            sc.nextLine();
         }
         catch (IOException e)
         {
            System.out.println("Sorry, the username you entered does not exist.");
            System.out.println("Are you sure you are a returning player? Please enter your username again.\n");
            sc.nextLine();
         }
         
      } while (!inputValid);
      
      //***Start of the actual Minesweeper Game. Keeps looping until player selects "[2] Quit" when prompted.***
      do
      {
         //Taking user input of number of rows on the board
         inputValid = false;
         do
         {
            try
            {
               System.out.print("\nHow many rows do you want?  ");
               numberOfRows = sc.nextInt();
               
               if (numberOfRows >= 5 && numberOfRows <= 15)
               {
                  inputValid = true;
               }
               else
               {
                  System.out.println("Invalid input. Please enter an integer between 5 and 15 (inclusive)");
               }
            }
            catch (InputMismatchException e)
            {
               System.out.println("Invalid input. Please enter an integer between 5 and 15 (inclusive)");
               sc.nextLine();
            }
         } while (!inputValid);
         
         //Taking user input of number of columns on the board
         inputValid = false;
         do
         {
            try
            {
               System.out.print("\nHow many columns do you want?  ");
               numberOfCols = sc.nextInt();
               
               if (numberOfCols >= 5 && numberOfCols <= 15)
               {
                  inputValid = true;
               }
               else
               {
                  System.out.println("Invalid input. Please enter an integer between 5 and 15 (inclusive)");
               }
            }
            catch (InputMismatchException e)
            {
               System.out.println("Invalid input. Please enter an integer between 5 and 15 (inclusive)");
               sc.nextLine();
            }
         } while (!inputValid);
         
         //Calculating the maximum and minimum number of mines allowed
         numberOfGrids = numberOfRows * numberOfCols;
         minMines = (int)Math.round(numberOfGrids * 0.1);
         maxMines = (int)Math.round(numberOfGrids * 0.8);
         
         //Taking user input of number of mines on the board
         inputValid = false;
         do
         {
            try
            {
               System.out.println("\nYour game board allows for a number of mines between " + 
                  minMines + " and " + maxMines + ".");
               System.out.print("How many mines do you want?  ");
               numberOfMines = sc.nextInt();
               flagsRemaining = numberOfMines;
               
               if (numberOfMines <= maxMines && numberOfMines >= minMines)
               {
                  inputValid = true;
               }
               else
               {
                  System.out.println("Invalid input. Please enter an integer in the specified range.");
               }
            }
            catch (InputMismatchException e)
            {
               System.out.println("Invalid input. Please enter an integer in the specified range.");
               sc.nextLine();
            }
         } while (!inputValid);
         
         do
         {
            //Initializing privateBoard using createBoard()
            privateBoard = new char[numberOfRows][numberOfCols];
            privateBoard = createBoard();
            
            //Printing out privateBoard (for debugging purposes) ------------------------------------------------------------------------------- REMOVE LATER
            for (int i = 0; i < numberOfRows; i++)
            {
               for (int j = 0; j < numberOfCols; j++)
               {
                  System.out.print(privateBoard[i][j] + " ");
               }
               System.out.println("");
            }
            
            //Initializing publicBoard as a 2D array of user input size filled with '_' in each element
            publicBoard = new char[numberOfRows][numberOfCols];
            for (int i = 0; i < numberOfRows; i++)
            {
               for (int j = 0; j < numberOfCols; j++)
               {
                     publicBoard[i][j] = '_';
               }
            }
            
            /*Outputs the empty board for the user to take their first action. Returns here if the user guesses a mine
            on their first guess.*/
            if (firstDig)
            {
               printBoard();
            }
            /*If the program gets here, it means the user has tried to dig a mine on their first guess. updateBoard() will
            confirm this and change the value of firstDigMine appropriately. If firstDigMine is true, this do while loop
            repeats, and a new board is secretly generated. updateBoard() will then check if the new board has a mine in 
            that same location and changes the value of firstDigMine accordingly.*/
            else
            {
               updateBoard();
            }
            
         } while (firstDigMine);
         
         /*Begins executing the recursive methods: printBoard() --> areYouWinningSon() --> digOrFlag() --> updateBoard() --> printBoard() etc.
         until areYouWinningSon() calls on winMessage() or loseMessage() --> updateStats() --> outputStats() --> main(String[] args)*/
         
         //Asks the user if they would like to play again
         inputValid = false;
         do
         {
            try
            {
               System.out.print("\nWould you like to [1] Play Again or [2] Quit?  ");
               playAgain = sc.nextInt();
               
               if (playAgain == 1 || playAgain == 2)
               {
                  inputValid = true;
               }
               else
               {
                  System.out.println("Invalid input. Please enter either 1 or 2.");
               }
            }
            catch (InputMismatchException e)
            {
               System.out.println("Invalid input. Please enter either 1 or 2.");
               sc.nextLine();
            }
         } while (!inputValid);
         
         //Resetting some of the static variables in case the user wants to play again
         winCon = 0;
         rowChoice = -1;
         colChoice = -1;
         firstDig = true;
         firstDigMine = true;
         
      } while (playAgain == 1);
      
      //Thank you message once the user quits
      System.out.print("Thanks for playing!");
      
   }
   
}