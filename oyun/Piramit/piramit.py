# An Algorithm to generate Pyramid Mind Puzzle
import copy
import random


class Piramit:
    def __init__(self):
        self.boyut = 3
        self.solutions = []

    def orta_nokta(self, row, column, genislik):
        ortanokta_x = (row + 1 / 2) * genislik * (8 / 5) + (
            ((self.boyut - column + 1) / 2) * genislik * (8 / 5)
        )
        ortanokta_y = (column + 1 / 2) * genislik + 10
        return ortanokta_x, ortanokta_y

    def tum_ihtimaller(self, sayi, en_alt_satirin_ustu):
        if sayi <= 1 or sayi > 9:
            return []
        ihtimaller = []
        if not en_alt_satirin_ustu:
            baslangic = 2
        else:
            baslangic = 1
        for i in range(baslangic, 9 - sayi + 1):
            ihtimaller.append((i, i + sayi))
            ihtimaller.append((i + sayi, i))
        for i in range(baslangic, sayi // 2):
            ihtimaller.append((i, sayi - i))
            ihtimaller.append((sayi - i, i))
        return ihtimaller

    def grid_olusturucu(self):
        grid = []
        for column in range(self.boyut):
            c = []
            for _ in range(column + 1):
                c.append(0)
            grid.append(c)
        if self.boyut == 3:
            grid[0][0] = random.randint(2, 9)
            grid[2][0] = random.randint(1, 9)
            grid[2][2] = random.randint(1, 9)
        elif self.boyut == 4:
            grid[0][0] = random.randint(2, 9)
            grid[2][1] = random.randint(2, 9)
            grid[3][0] = random.randint(1, 9)
            grid[3][3] = random.randint(1, 9)
        elif self.boyut == 5:
            grid[0][0] = random.randint(2, 9)
            grid[2][0] = random.randint(2, 9)
            grid[2][2] = random.randint(2, 9)
            grid[4][0] = random.randint(1, 9)
            grid[4][2] = random.randint(1, 9)
            grid[4][4] = random.randint(1, 9)
        elif self.boyut == 6:
            grid[0][0] = random.randint(2, 9)
            grid[2][0] = random.randint(2, 9)
            grid[2][2] = random.randint(2, 9)
            grid[4][1] = random.randint(2, 9)
            grid[4][3] = random.randint(2, 9)
            grid[5][0] = random.randint(1, 9)
            grid[5][5] = random.randint(1, 9)
        return grid

    def possible(self, grid, row, column, n):
        if not (row == (self.boyut - 1)):
            if n == 1:
                return False
        if column != row and column != 0:  # son karede ve ilk karede değilse
            if grid[row][column + 1] != 0 and grid[row - 1][column] != 0:
                if not (
                    (abs(n - grid[row][column + 1]) == grid[row - 1][column])
                    or (n + grid[row][column + 1] == grid[row - 1][column])
                ):
                    return False
            if grid[row][column + 1] != 0:
                if (
                    n in grid[row]
                    or n == grid[row][column + 1] + 1
                    or n == grid[row][column + 1] - 1
                ):
                    return False
            if grid[row - 1][column] != 0:
                if n not in [
                    i[0]
                    for i in self.tum_ihtimaller(
                        grid[row - 1][column], (row == (self.boyut - 1))
                    )
                ]:
                    return False
            if grid[row][column - 1] != 0 and grid[row - 1][column - 1] != 0:
                if not (
                    (abs(n - grid[row][column - 1]) == grid[row - 1][column - 1])
                    or (n + grid[row][column - 1] == grid[row - 1][column - 1])
                ):
                    return False
            if grid[row][column - 1] != 0:
                if (
                    n in grid[row]
                    or n == grid[row][column - 1] + 1
                    or n == grid[row][column - 1] - 1
                ):
                    return False
            if grid[row - 1][column - 1] != 0:
                if n not in [
                    i[0]
                    for i in self.tum_ihtimaller(
                        grid[row - 1][column - 1], (row == (self.boyut - 1))
                    )
                ]:
                    return False
            return True
        elif column == 0:  # ilk karede ise
            if grid[row][column + 1] != 0 and grid[row - 1][column] != 0:
                if not (
                    (abs(n - grid[row][column + 1]) == grid[row - 1][column])
                    or (n + grid[row][column + 1] == grid[row - 1][column])
                ):
                    return False
            if grid[row][column + 1] != 0:
                if (
                    n in grid[row]
                    or n == grid[row][column + 1] + 1
                    or n == grid[row][column + 1] - 1
                ):
                    return False
            if grid[row - 1][column] != 0:
                if n not in [
                    i[0]
                    for i in self.tum_ihtimaller(
                        grid[row - 1][column], (row == (self.boyut - 1))
                    )
                ]:
                    return False
            return True
        elif column == row:
            if grid[row][column - 1] != 0 and grid[row - 1][column - 1] != 0:
                if not (
                    (abs(n - grid[row][column - 1]) == grid[row - 1][column - 1])
                    or (n + grid[row][column - 1] == grid[row - 1][column - 1])
                ):
                    return False
            if grid[row][column - 1] != 0:
                if (
                    n in grid[row]
                    or n == grid[row][column - 1] + 1
                    or n == grid[row][column - 1] - 1
                ):
                    return False
            if grid[row - 1][column - 1] != 0:
                if n not in [
                    i[0]
                    for i in self.tum_ihtimaller(
                        grid[row - 1][column - 1], (row == (self.boyut - 1))
                    )
                ]:
                    return False
            return True

    def solver(self, grid):
        for row in range(1, self.boyut):
            for column in range(row + 1):
                if grid[row][column] == 0:
                    for n in range(1, 10):
                        if self.possible(grid, row, column, n):
                            grid[row][column] = n
                            self.solver(grid)
                            grid[row][column] = 0
                    return
        self.solutions.append(copy.deepcopy(grid))

    def class_main(self):
        while True:
            grid = self.grid_olusturucu()
            self.solutions = []
            self.solver(grid)
            if len(self.solutions) == 1:
                break
        return self.solutions[0]


def main(size, count):
    datacontrol = set()
    data = []
    database = []
    for _ in range(count):
        soru = Piramit()
        soru.boyut = size
        veri = soru.class_main()
        data.append(veri)
        datacontrol.add(str(veri))
    for i in data:
        if str(i) in datacontrol:
            datacontrol.discard(str(i))
            database.append(i)
    return database
