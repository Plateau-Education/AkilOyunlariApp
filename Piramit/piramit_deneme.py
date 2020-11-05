import random, time, timeit, copy
from tkinter import Tk, Canvas, BOTH
import tkinter.font as tkFont


class Piramit:
    def __init__(self):
        self.boyut = 3
        self.solutions = []
        self.tkinterOn = False

    def orta_nokta(self, row, column, genislik):
        ortanokta_x = (row + 1 / 2) * genislik * (8 / 5) + (
            ((self.boyut - column + 1) / 2) * genislik * (8 / 5)
        )
        ortanokta_y = (column + 1 / 2) * genislik + 10
        return (ortanokta_x, ortanokta_y)

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
            # if n not in set([j[0] for i in grid[row-1][column] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column+1]]) & set([j[1] for i in grid[row-1][column-1] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[0] in grid[row][column-1]]) and n not in set([j[0] for i in grid[row] for j in i if len(j) == 1]):
            #     pass
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
            # return n in set([j[0] for i in grid[row-1][column] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column+1]])
        elif column == row:  # son karede ise
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
            # return n in set([j[0] for i in grid[row-1][column-1] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column-1]])

    def solver(self, grid):
        # print(self.tum_ihtimaller(4,False))
        for row in range(1, self.boyut):
            for column in range(row + 1):
                if grid[row][column] == 0:
                    for n in range(1, 10):
                        if self.possible(grid, row, column, n):
                            grid[row][column] = n
                            self.solver(grid)
                            grid[row][column] = 0
                    return
        # print(grid)
        self.solutions.append(copy.deepcopy(grid))

    def cizici(self, canvas, genislik, grid, solution_or_question="solution"):
        for column in range(self.boyut):
            for row in range(column + 1):
                canvas.create_rectangle(
                    row * genislik * (8 / 5)
                    + (((self.boyut - column + 1) / 2) * genislik * (8 / 5)),
                    column * genislik + 10,
                    (row + 1) * genislik * (8 / 5)
                    + (((self.boyut - column + 1) / 2) * genislik * (8 / 5)),
                    (column + 1) * genislik + 10,
                )
                if solution_or_question == "question":
                    if (
                        (self.boyut == 3 and (column, row) in [(0, 0), (2, 0), (2, 2)])
                        or (
                            self.boyut == 4
                            and (column, row) in [(0, 0), (2, 1), (3, 0), (3, 3)]
                        )
                        or (
                            self.boyut == 5
                            and (column, row)
                            in [(0, 0), (2, 0), (2, 2), (4, 0), (4, 2), (4, 4)]
                        )
                        or (
                            self.boyut == 6
                            and (column, row)
                            in [(0, 0), (2, 0), (2, 2), (4, 1), (4, 3), (5, 0), (5, 5)]
                        )
                    ):
                        canvas.create_text(
                            self.orta_nokta(row, column, genislik),
                            text=grid[column][row],
                            font=tkFont.Font(family="Poppins", size=25),
                        )
                else:
                    canvas.create_text(
                        self.orta_nokta(row, column, genislik),
                        text=grid[column][row],
                        font=tkFont.Font(family="Poppins", size=25),
                    )

    def class_main(self):

        # start = timeit.default_timer()

        while True:
            grid = self.grid_olusturucu()
            self.solutions = []
            self.solver(grid)

            if len(self.solutions) == 1:
                break
        # end = timeit.default_timer()
        # print(f"It took {end-start} seconds.")

        if self.tkinterOn:
            root = Tk()
            canvas = Canvas(root)

            self.cizici(canvas, 40, self.solutions[0], "solution")

            canvas.pack(fill=BOTH, expand=1)
            root.geometry("500x350+300+300")
            if input() == "q":
                root.destroy()
                root = Tk()
                canvas = Canvas(root)
                self.cizici(canvas, 40, self.solutions[0], "question")
                canvas.pack(fill=BOTH, expand=1)
                root.geometry("500x350+300+300")
                root.mainloop()

            root.mainloop()
        return self.solutions[0]


def main(size):
    soru = Piramit()
    soru.boyut = size
    soru.tkinterOn = False
    return soru.class_main()


start = timeit.default_timer()
for _ in range(10):
    print(main(6))
end = timeit.default_timer()
print(end - start)
