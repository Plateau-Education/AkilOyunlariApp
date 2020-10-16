# An algorithm to generate "Pentomino"
import random as rd


class Pentomino:
    def __init__(self):
        self.grid = []
        self.patterns = []
        self.possible = set()

    def CreateGrid(self):
        for x in range(6):
            row = []
            for y in range(6):
                row.append((x, y, "[]"))
            self.grid.append(row)

    def SetPossible(self):
        for a in self.grid[0]:
            self.possible.add(a)
        for b in self.grid[-1]:
            self.possible.add(b)
        for i in self.grid[1:5]:
            self.possible.add(i[0])
            self.possible.add(i[-1])

    def CreateCells(self):
        for i in range(16):
            choice = rd.choice(list(self.possible))
            self.possible.remove(choice)
            x = choice[0]
            y = choice[1]
            self.grid[x][y] = (x, y, "  ")
            if x == 0 and 0 < y < 5:
                self.possible.add((x+1, y, "[]"))
            elif x == 5 and 0 < y < 5:
                self.possible.add((x-1, y, "[]"))
            elif y == 0 and 0 < x < 5:
                self.possible.add((x, y+1, "[]"))
            elif y == 5 and 0 < x < 5:
                self.possible.add((x, y-1, "[]"))

    def PrintGrid(self):
        grid = self.grid.copy()
        for i in grid:
            print()
            for j in i:
                print(j[2], end="")


def main():
    game = Pentomino()
    game.CreateGrid()
    game.SetPossible()
    game.CreateCells()
    game.PrintGrid()

main()
