# An algorithm to generate "Pentomino"
import random as rd


def Sort(a):
    return a[1]


class Pentomino:
    def __init__(self):
        self.grid = []
        self.patterns = []

    def CreateCells(self):
        xs = []
        ys = []
        x = 0
        y = 0
        for _ in range(20):
            xs.append(x)
            ys.append(y)
            self.grid.append((x, y))
            possibles = [(x + 1, y), (x, y + 1), (x - 1, y), (x, y - 1)]
            dict1 = {(x + 1, y): (x - 1, y), (x - 1, y): (x + 1, y), (x, y + 1): (x, y - 1), (x, y - 1): (x, y + 1)}
            choice = rd.choice(possibles)
            if len(possibles) == 4:
                possibles.remove(dict1[choice])
            else:
                possibles.remove(dict1[choice])
                possibles.append(dict1[dict1[choice]])
            x = choice[0]
            y = choice[1]

    def PrintGrid(self):
        grid = self.grid.copy()
        grid.sort(key=Sort)
        print(grid)
