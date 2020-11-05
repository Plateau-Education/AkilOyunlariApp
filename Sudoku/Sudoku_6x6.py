# Working code to Generate a puzzle
# -*- coding: utf-8 -*-
import timeit
import time
import copy
import random
import numpy as np


answer = []


class Cell:
    # Initializes cell object. A cell is a single box of a sudoku puzzle. 81 cells make up the body of a sudoku puzzle.
    # Initializes puzzle with all possible answers available, solved to false, and position of cell within the
    # sudoku puzzle

    def __init__(self, position):
        self.possibleAnswers = [1, 2, 3, 4, 5, 6]
        self.answer = None
        self.position = position
        self.solved = False

    def remove(self, num):
        # Removes num from list of possible answers in cell object.
        if num in self.possibleAnswers and self.solved == False:
            self.possibleAnswers.remove(num)
            if len(self.possibleAnswers) == 1:
                self.answer = self.possibleAnswers[0]
                self.solved = True
        if num in self.possibleAnswers and self.solved == True:
            self.answer = 0

    def solvedMethod(self):
        # Returns whether or not a cell has been solved
        return self.solved

    def checkPosition(self):
        # Returns the position of a cell within a sudoku puzzle. x = row; y = col; z = box number
        return self.position

    def returnPossible(self):
        # Returns a list of possible answers that a cell can still use
        return self.possibleAnswers

    def lenOfPossible(self):
        # Returns an integer of the length of the possible answers list
        return len(self.possibleAnswers)

    def returnSolved(self):
        # Returns whether or not a cell has been solved
        if self.solved:
            return self.possibleAnswers[0]
        else:
            return 0

    def setAnswer(self, num):
        # Sets an answer of a puzzle and sets a cell's solved method to true. This method also eliminates all other
        # possible numbers
        if num in [1, 2, 3, 4, 5, 6]:
            self.solved = True
            self.answer = num
            self.possibleAnswers = [num]
        else:
            raise ValueError

    def reset(self):
        # Resets all attributes of a cell to the original conditions
        self.possibleAnswers = [1, 2, 3, 4, 5, 6]
        self.answer = None
        self.solved = False


def emptySudoku():
    # Creates an empty sudoku in row major form. Sets up all of the x, y, and z
    # coordinates for the sudoku cells
    ans = []
    for y in range(1, 7):
        if y in [5, 6]:
            intz = 3
        if y in [3, 4]:
            intz = 2
        if y in [1, 2]:
            intz = 1
        for x in range(1, 7):
            z = intz
            if x in [4, 5, 6]:
                z += 3
            if x in [1, 2, 3]:
                z += 0
            c = Cell((x, y, z))
            ans.append(c)
    return ans


def printSudoku(sudoku):
    # Prints out a sudoku in a format that is easy for a human to read
    row1 = []
    row2 = []
    row3 = []
    row4 = []
    row5 = []
    row6 = []
    for i in range(36):
        if i in range(0, 6):
            row1.append(sudoku[i].returnSolved())
        if i in range(6, 12):
            row2.append(sudoku[i].returnSolved())
        if i in range(12, 18):
            row3.append(sudoku[i].returnSolved())
        if i in range(18, 24):
            row4.append(sudoku[i].returnSolved())
        if i in range(24, 30):
            row5.append(sudoku[i].returnSolved())
        if i in range(30, 36):
            row6.append(sudoku[i].returnSolved())

    # print(row1[0:3], row1[3:6])
    # print(row2[0:3], row2[3:6])
    # print('')
    # print(row3[0:3], row3[3:6])
    # print(row4[0:3], row4[3:6])
    # print('')
    # print(row5[0:3], row5[3:6])
    # print(row6[0:3], row6[3:6])

    return [row1, row2, row3, row4, row5, row6]


def sudokuGen():
    # Generates a completed sudoku. Sudoku is completely random
    cells = [i for i in range(36)]  # our cells is the positions of cells not currently set
    sudoku = emptySudoku()
    while len(cells) != 0:
        lowestNum = []
        Lowest = []
        for i in cells:
            # finds all the lengths of of possible answers for each remaining cell
            lowestNum.append(sudoku[i].lenOfPossible())
        m = min(lowestNum)  # finds the minimum of those
        # Puts all of the cells with the lowest number of possible answers in a list titled Lowest
        for i in cells:
            if sudoku[i].lenOfPossible() == m:
                Lowest.append(sudoku[i])
        # Now we randomly choose a possible answer and set it to the cell
        choiceElement = random.choice(Lowest)
        choiceIndex = sudoku.index(choiceElement)
        cells.remove(choiceIndex)
        position1 = choiceElement.checkPosition()
        if not choiceElement.solvedMethod():  # the actual setting of the cell
            possibleValues = choiceElement.returnPossible()
            finalValue = random.choice(possibleValues)
            choiceElement.setAnswer(finalValue)
            # now we iterate through the remaining unset cells and remove the input if it's in the same row, col, or box
            for i in cells:
                position2 = sudoku[i].checkPosition()
                if position1[0] == position2[0]:
                    sudoku[i].remove(finalValue)
                if position1[1] == position2[1]:
                    sudoku[i].remove(finalValue)
                if position1[2] == position2[2]:
                    sudoku[i].remove(finalValue)

        else:
            finalValue = choiceElement.returnSolved()
            # now we iterate through the remaining unset cells and remove the input if it's in the same row, col, or box
            for i in cells:
                position2 = sudoku[i].checkPosition()
                if position1[0] == position2[0]:
                    sudoku[i].remove(finalValue)
                if position1[1] == position2[1]:
                    sudoku[i].remove(finalValue)
                if position1[2] == position2[2]:
                    sudoku[i].remove(finalValue)
    return sudoku


def sudokuChecker(sudoku):
    # Checks to see if an input a completed sudoku puzzle is of the correct format and abides by all
    # of the rules of a sudoku puzzle. Returns True if the puzzle is correct. False if otherwise
    for i in range(len(sudoku)):
        for n in range(len(sudoku)):
            if i != n:
                position1 = sudoku[i].checkPosition()
                position2 = sudoku[n].checkPosition()
                if position1[0] == position2[0] or position1[1] == position2[1] or position1[2] == position2[2]:
                    num1 = sudoku[i].returnSolved()
                    num2 = sudoku[n].returnSolved()
                    if num1 == num2:
                        return False
    return True


def perfectSudoku():
    # Generates a completed sudoku. Sudoku is in the correct format and is completly random
    result = False
    while not result:
        s = sudokuGen()
        result = sudokuChecker(s)
    global answer
    answer = printSudoku(s)
    return s


def solver(sudoku, f=0):
    # Input an incomplete Sudoku puzzle and solver method will return the solution to the puzzle. First checks to see if
    # any obvious answers can be set then checks the rows columns and boxes for obvious solutions. Lastly the solver
    # 'guesses' a random possible answer from a random cell and checks to see if that is a possible answer. If the
    # 'guessed' answer is incorrect, then it removes the guess and tries a different answer in a different cell and
    # checks for a solution. It does this until all of the cells have been solved. Returns a printed solution
    # to the puzzle and the number of guesses that it took to complete the puzzle. The number of guesses is a measure
    # of the difficulty of the puzzle. The more guesses that it takes to solve a given puzzle the more challenging it
    # is to solve the puzzle
    if f > 900:
        return False
    guesses = 0
    copy_s = copy.deepcopy(sudoku)
    cells = [i for i in range(36)]  # our cells is the positions of cells not currently set
    solvedCells = []
    for i in cells:
        if copy_s[i].lenOfPossible() == 1:
            solvedCells.append(i)
    while solvedCells:
        for n in solvedCells:
            cell = copy_s[n]
            position1 = cell.checkPosition()
            finalValue = copy_s[n].returnSolved()
            # now we itterate through the remaing unset cells and remove the input if it's in the same row, col, or box
            for i in cells:
                position2 = copy_s[i].checkPosition()
                if position1[0] == position2[0]:
                    copy_s[i].remove(finalValue)
                if position1[1] == position2[1]:
                    copy_s[i].remove(finalValue)
                if position1[2] == position2[2]:
                    copy_s[i].remove(finalValue)
                if copy_s[i].lenOfPossible() == 1 and i not in solvedCells and i in cells:
                    solvedCells.append(i)
                # print(n)
            solvedCells.remove(n)
            cells.remove(n)
        if cells != [] and solvedCells == []:
            lowestNum = []
            lowest = []
            for i in cells:
                lowestNum.append(copy_s[i].lenOfPossible())
            m = min(lowestNum)
            for i in cells:
                if copy_s[i].lenOfPossible() == m:
                    lowest.append(copy_s[i])
            randomChoice = random.choice(lowest)
            randCell = copy_s.index(randomChoice)
            randGuess = random.choice(copy_s[randCell].returnPossible())
            copy_s[randCell].setAnswer(randGuess)
            solvedCells.append(randCell)
            guesses += 1
    if sudokuChecker(copy_s):
        if guesses == 0:
            xlevel = 'Easy'
        elif guesses == 1:
            xlevel = 'Medium'
        elif guesses >= 2:
            xlevel = 'Hard'
        return copy_s, guesses, xlevel
    else:
        return solver(sudoku, f + 1)


def solve(sudoku, n=0):
    # Uses the solver method to solve a puzzle. This method was built in order to avoid recursion depth errors.
    # Returns True if the puzzle is solvable and false if otherwise.
    if n < 30:
        s = solver(sudoku)
        if s:
            return s
        else:
            solve(sudoku, n + 1)
    else:
        return False


def puzzleGen(sudoku):
    # Generates a puzzle with a unique solution.
    cells = [i for i in range(36)]
    while cells:
        copy_s = copy.deepcopy(sudoku)
        randIndex = random.choice(cells)
        cells.remove(randIndex)
        copy_s[randIndex].reset()
        s = solve(copy_s)
        if not s[0]:
            f = solve(sudoku)
            # print("Guesses: " + str(f[1]))
            # print("Level: " + str(f[2]))
            return printSudoku(sudoku)
        elif equalChecker(s[0], solve(copy_s)[0]):
            if equalChecker(s[0], solve(copy_s)[0]):
                sudoku[randIndex].reset()
        else:
            f = solve(sudoku)
            # print("Guesses: " + str(f[1]))
            # print("Level: " + str(f[2]))
            return sudoku, f[1], f[2]


def equalChecker(s1, s2):
    # Checks to see if two puzzles are the same.
    for i in range(len(s1)):
        if s1[i].returnSolved() != s2[i].returnSolved():
            return False
    return True


def generator(levelx):
    # Input the level of difficulty of the sudoku puzzle. Difficulty levels include ‘Easy’ ‘Medium’ ‘Hard’ and ‘Insane’
    # Outputs a sudoku of desired difficulty.
    t1 = time.time()
    n = 0
    if levelx == 'Easy':
        p = perfectSudoku()
        s = puzzleGen(p)
        if s[2] != 'Easy':
            return generator(levelx)
        t2 = time.time()
        t3 = t2 - t1
        # print("Runtime is " + str(t3) + " seconds")
        # print("Guesses: " + str(s[1]))
        # print("Level: " + str(s[2]))
        return printSudoku(s[0])
    if levelx == 'Medium':
        p = perfectSudoku()
        s = puzzleGen(p)
        while s[2] == 'Easy':
            n += 1
            s = puzzleGen(p)
            if n > 50:
                return generator(levelx)
        if s[2] != 'Medium':
            return generator(levelx)
        t2 = time.time()
        t3 = t2 - t1
        # print("Runtime is " + str(t3) + " seconds")
        # print("Guesses: " + str(s[1]))
        # print("Level: " + str(s[2]))
        return printSudoku(s[0])
    if levelx == 'Hard':
        p = perfectSudoku()
        s = puzzleGen(p)
        while s[2] == 'Easy':
            n += 1
            s = puzzleGen(p)
            if n > 50:
                return generator(levelx)
        while s[2] == 'Medium':
            n += 1
            s = puzzleGen(p)
            if n > 50:
                return generator(levelx)
        if s[2] != 'Hard':
            return generator(levelx)
        t2 = time.time()
        t3 = t2 - t1
        # print("Runtime is " + str(t3) + " seconds")
        # print("Guesses: " + str(s[1]))
        # print("Level: " + str(s[2]))
        return printSudoku(s[0])
    else:
        raise ValueError


class OneSolution:

    def __init__(self):
        self.grid = []
        self.solutions = []

    def possible(self, y, x, n):
        for i in range(6):
            if self.grid[y][i] == n:
                return False
        for i in range(6):
            if self.grid[i][x] == n:
                return False
        x0 = (x // 3) * 3
        y0 = (y // 2) * 2
        for i in range(2):
            for j in range(3):
                if self.grid[y0 + i][x0 + j] == n:
                    return False
        return True

    def solve(self):
        for y in range(6):
            for x in range(6):
                if self.grid[y][x] == 0:
                    for n in range(1, 7):
                        if self.possible(y, x, n):
                            self.grid[y][x] = n
                            self.solve()
                            self.grid[y][x] = 0
                    return
        self.solutions.append(np.matrix(self.grid))

    def tek_cozum(self):
        self.solve()
        if len(self.solutions) == 1:
            return True
        else:
            return False


# level = input("Type a level /Easy-Medium-Hard\n")

# [Level of Difficulty] = Input the level of difficulty of the sudoku puzzle. Difficulty levels
# include ‘Easy’ ‘Medium’ ‘Hard’ and ‘Insane’. Outputs a sudoku of desired difficulty.


def main(levelx):
    for _ in range(201):
        q = OneSolution()
        q.grid = generator(levelx)
        tek_cozumlu = q.tek_cozum()
        if tek_cozumlu:
            return [q.grid, answer]

