import random, time, timeit, copy
from tkinter import Tk, Canvas, BOTH
import tkinter.font as tkFont
import numpy as np
from itertools import combinations


class HazineAvi:
    def __init__(self):
        self.boyut = 8
        self.grid = []
        self.solutions = []

    def orta_nokta(self, row, column, genislik):
        ortanokta_x = (row + 1 / 2) * genislik + 10
        ortanokta_y = (column + 1 / 2) * genislik + 10
        return (ortanokta_x, ortanokta_y)

    def daire_koordinat(self, row, column, genislik):
        x1 = row * genislik + 15
        y1 = column * genislik + 15
        x2 = (row + 1) * genislik + 5
        y2 = (column + 1) * genislik + 5
        return x1, y1, x2, y2

    def create_grid(self, canvas, genislik):
        for row in range(self.boyut):
            for column in range(self.boyut):
                canvas.create_rectangle(
                    row * genislik + 10,
                    column * genislik + 10,
                    (row + 1) * genislik + 10,
                    (column + 1) * genislik + 10,
                )

    def gorsellestir(self, canvas, genislik, grid):
        for row in range(self.boyut):
            for column in range(self.boyut):
                if grid[column][row] > 0:
                    canvas.create_text(
                        self.orta_nokta(row, column, genislik),
                        text=grid[column][row],
                        font=tkFont.Font(family="Poppins", size=25),
                    )
                elif grid[column][row] == -1:
                    x1, y1, x2, y2 = self.daire_koordinat(row, column, genislik)
                    canvas.create_oval(x1, y1, x2, y2, fill="green")
                elif grid[column][row] == -2:
                    canvas.create_line(
                        row * genislik + 10,
                        column * genislik + 10,
                        (row + 1) * genislik + 10,
                        (column + 1) * genislik + 10,
                    )
                    canvas.create_line(
                        row * genislik + 10,
                        (column + 1) * genislik + 10,
                        (row + 1) * genislik + 10,
                        column * genislik + 10,
                    )

    def komsular(self, y, x):
        komsular = [
            (y + 1, x),
            (y - 1, x),
            (y, x + 1),
            (y, x - 1),
            (y + 1, x + 1),
            (y + 1, x - 1),
            (y - 1, x + 1),
            (y - 1, x - 1),
        ]
        for _ in range(5):
            for komsu in komsular:
                if (
                    komsu[0] < 0
                    or komsu[0] > self.boyut - 1
                    or komsu[1] < 0
                    or komsu[1] > self.boyut - 1
                ):
                    komsular.remove(komsu)
        return komsular

    def count01(self, y, x, grid):
        komsular = self.komsular(y, x)
        list1 = []
        list0 = []
        for komsu in komsular:
            if grid[komsu[0]][komsu[1]] == -1:
                list1.append((komsu[0], komsu[1]))
            elif grid[komsu[0]][komsu[1]] == 0:
                list0.append((komsu[0], komsu[1]))
            # if y == 0 and x == 1:
            #     print(grid[komsu[0]][komsu[1]])
        return len(list0), len(list1), list0, list1, grid[y][x] == len(list1)

    def minmax(self, c):
        if c < 2:
            minc = 0
            maxc = c + 2
        elif c > self.boyut - 3:
            minc = c - 2
            maxc = self.boyut - 1
        else:
            minc = c - 2
            maxc = c + 2
        return minc, maxc

    def intercept_setList(self, setList):
        s = setList[0]
        for i in setList[1:]:
            s = s & i
        return s

    def possible_combinations(self, y, x, n, list0, list1, grid):
        intercept_list = []
        for c1 in combinations(list0, n - len(list1)):
            comb_set = set()
            copy_g = []
            copy_g = copy.deepcopy(grid)
            impflag = False
            minx, maxx, miny, maxy = self.boyut, 0, self.boyut, 0
            for y1, x1 in c1:
                copy_g[y1][x1] = -1
                if y1 < miny:
                    miny = self.minmax(y1)[0]
                if y1 > maxy:
                    maxy = self.minmax(y1)[1]
                if x1 < minx:
                    minx = self.minmax(x1)[0]
                if x1 > maxx:
                    maxx = self.minmax(x1)[1]

            # for c2 in combinations(set(list0) - set(c1), len(list0) - n + len(list1)):
            c2 = tuple(set(list0) - set(c1))
            for y2, x2 in c2:
                copy_g[y2][x2] = -2
                comb_set.add((y2, x2, -2))
                if y2 < miny:
                    miny = self.minmax(y2)[0]
                if y2 > maxy:
                    maxy = self.minmax(y2)[1]
                if x2 < minx:
                    minx = self.minmax(x2)[0]
                if x2 > maxx:
                    maxx = self.minmax(x2)[1]

            for y3 in range(miny, maxy + 1):
                if impflag:
                    break
                for x3 in range(minx, maxx + 1):
                    if impflag:
                        break
                    n1 = copy_g[y3][x3]
                    if n1 > 0:

                        count_0, count_1, list_0 = self.count01(y3, x3, copy_g)[:3]
                        # print(
                        #     f"y:{y3} x:{x3} n:{n1} count_1:{count_1} count_0:{count_0}"
                        # )
                        if (n1 - count_1) > count_0 or n1 < count_1:
                            impflag = True
                            comb_set = set()
                            # print("problem")
                            # f"PROBLEM!!! y:{y3} x:{x3} n:{n1} count_1:{count_1} count_0:{count_0}"
                            # )
                            break
                        elif (n1 - count_1) == 0:
                            for y4, x4 in list_0:
                                copy_g[y4][x4] = -2
                                comb_set.add((y4, x4, -2))
                        elif (n1 - count_1) == count_0:
                            for y5, x5 in list_0:
                                copy_g[y5][x5] = -1
                                comb_set.add((y5, x5, -1))

            # print(f"---{y,x,n,c1,c2}---")
            # print(np.matrix(copy_g))
            # print(comb_set)
            if impflag:
                continue
            else:
                if comb_set:
                    intercept_list.append(comb_set)

        if intercept_list:
            # print("INTERCEPT LIST: ", self.intercept_setList(intercept_list))
            return self.intercept_setList(intercept_list)
        else:
            return set()

            # komsular = self.komsular(y1,x1)
            # for k in komsular:
            #     if grid[k[0]][k[1]] > 0:
            #         komsular2 = self.komsular(k[0],k[1])
            #         if (all([self.count01(y2,x2,grid)[-1] == False for y2,x2 in komsular2 if grid[y2][x2] > 0]) and any([grid[y3][x3] > 0 for y3,x3 in komsular2])):
            #             impflag = False
            #             c0,c1,l0 = self.count01(k[0],k[1],grid)[:3]
            #             # bi dict yapılabilir. içinde her kombinasyonun her komşu sayı için olabilecek kombinasyonları olur. en son bunlar set intercept yapılır.
            #         else:
            #             impflag = True
            #             break

        # comb2 = set()
        # for combination2 in combinations(list0,len(list0)-n+len(list1)):
        #     comb2.add(combination2)

    def first_base(self, grid):
        for y in range(self.boyut):
            for x in range(self.boyut):
                n = grid[y][x]
                if n > 0:
                    count0, count1, list0, list1 = self.count01(y, x, grid)[:4]
                    if (n - count1) > count0:
                        return "Wrong Question"
                    elif (n - count1) == 0:
                        for y2, x2 in list0:
                            grid[y2][x2] = -2
                    elif (n - count1) == count0:
                        for y3, x3 in list0:
                            grid[y3][x3] = -1
                    elif (n - count1) < count0:
                        pass

    def second_base(self, grid):
        for y in range(self.boyut):
            for x in range(self.boyut):
                n = grid[y][x]
                if n > 0:
                    count0, count1, list0, list1 = self.count01(y, x, grid)[:4]
                    if (n - count1) < count0:
                        for pc in self.possible_combinations(
                            y, x, n, list0, list1, grid
                        ):
                            if pc == set():
                                continue
                            y4, x4, n4 = pc
                            grid[y4][x4] = n4

    def isFullCheck(self, grid):
        return all(
            [grid[y][x] != 0 for y in range(self.boyut) for x in range(self.boyut)]
        )

    def isAllEmptyHasNumNeighbor(self, grid):
        for y in range(self.boyut):
            for x in range(self.boyut):
                if grid[y][x] <= 0:
                    result = False
                    komsulr = self.komsular(y, x)
                    for k in komsulr:
                        if grid[k[0]][k[1]] > 0:
                            result = True
                            break
                    if not result:
                        return False
                # else:
                #     result = False
                #     komsulr = self.komsular(y, x)
                #     for k1 in komsulr:
                #         countb0 = 0
                #         komsulr2 = self.komsular(k1[0], k1[1])
                #         for k2 in komsulr2:
                #             if grid[k[0]][k[1]] > 0:
                #                 countb0 += 1
                #         if countb0 > 1:
                #             return True
                #     return False
        return True

    def solver2(self, grid):
        for __ in range(10):
            for _ in range(3):
                cgrid = copy.deepcopy(grid)
                if self.first_base(grid) == "Wrong Question":
                    return "Wrong Question"
                if np.array_equal(cgrid, grid):
                    break
            cgrid = copy.deepcopy(grid)
            self.second_base(grid)
            if np.array_equal(cgrid, grid):
                break
        if self.isFullCheck(grid) and self.isAllEmptyHasNumNeighbor:
            self.solutions.append(copy.deepcopy(grid))
            return
        else:
            return "Wrong Question"

    def elmas_yerlestirme(self, alt_sinir, ust_sinir, grid):
        self.elmas_sayisi = random.randint(alt_sinir, ust_sinir)
        rows = [random.randint(0, self.boyut - 1) for i in range(self.elmas_sayisi)]
        columns = [random.randint(0, self.boyut - 1) for i in range(self.elmas_sayisi)]
        zipped = zip(rows, columns)
        grid = np.zeros((self.boyut, self.boyut), dtype=int)
        for row, column in zipped:
            grid[row][column] = -1
        return grid
        # self.solutions.append(self.grid)
        # print(self.grid)

    def sayi_belirleme(self, grid):
        count0 = 0
        for r in range(self.boyut):
            for c in range(self.boyut):
                if grid[r][c] == 0:
                    count0 += 1
                    grid[r][c] = self.count01(r, c, grid)[1]
        # while count0 > (self.boyut ** 2 - self.elmas_sayisi) * (8 * 8) - 1:
        #     while True:
        #         y = random.randint(0, self.boyut - 1)
        #         x = random.randint(0, self.boyut - 1)
        #         if grid[y][x] > 0:
        #             grid[y][x] = 0
        #             count0 -= 1
        #             break
        return grid
        # self.solutions.append(self.grid)

    def sayi_azaltma(self):
        cozulmus = self.sayi_belirleme(
            self.elmas_yerlestirme(
                self.boyut + 1, self.boyut * 3, np.zeros((self.boyut, self.boyut))
            )
        )
        cozulmemis = []
        for y in range(self.boyut):
            cozulmemis.append([])
            for x in range(self.boyut):
                if cozulmus[y][x] >= 0:
                    cozulmemis[y].append(cozulmus[y][x])
                else:
                    cozulmemis[y].append(0)

        grid = copy.deepcopy(cozulmemis)
        previous_grid = copy.deepcopy(grid)
        cells = [
            (y, x)
            for y in range(self.boyut)
            for x in range(self.boyut)
            if grid[y][x] > 0
        ]
        for tur in range(self.boyut ** 2):
            rndIndex = random.choice(cells)
            cells.remove(rndIndex)
            grid[rndIndex[0]][rndIndex[1]] = 0
            # print(grid)
            self.solutions = []
            if self.solver2(copy.deepcopy(grid)) == "Wrong Question":
                if tur > 4 and self.isAllEmptyHasNumNeighbor(previous_grid):
                    # cozulmemis = []
                    # for y in range(self.boyut):
                    #     cozulmemis.append([])
                    #     for x in range(self.boyut):
                    #         if previous_grid[y][x] >= 0:
                    #             cozulmemis[y].append(previous_grid[y][x])
                    #         elif previous_grid[y][x] == -2 or previous_grid[y][x] == -1:
                    #             cozulmemis[y].append(0)
                    #         else:
                    #             cozulmemis[y].append(0)
                    print("Prev: ", np.matrix(previous_grid))
                    print("Grid: ", np.matrix(grid))
                    cozulmemis = copy.deepcopy(previous_grid)
                    self.solver2(previous_grid)
                    cozulmus = previous_grid
                    return cozulmus, cozulmemis
                break
            previous_grid = copy.deepcopy(grid)
        self.sayi_azaltma()

    def main(self):

        root = Tk()
        canvas = Canvas(root)

        start = timeit.default_timer()
        self.create_grid(canvas, 40)
        # self.elmas_yerlestirme(20, 30)
        # self.sayi_belirleme()
        while True:
            try:
                cozulmus, cozulmemis = self.sayi_azaltma()
                break
            except:
                pass
        print(np.matrix(cozulmemis))
        print(np.matrix(cozulmus))
        # self.solver2(self.grid)
        print("Solutions: ", self.solutions)
        end = timeit.default_timer()
        print(f"It took {end-start} seconds.")
        self.gorsellestir(canvas, 40, cozulmus)

        canvas.pack(fill=BOTH, expand=1)
        root.geometry(f"{self.boyut*45}x{self.boyut*45}+300+300")
        if input() == "q":
            root.destroy()
            root = Tk()
            canvas = Canvas(root)
            self.create_grid(canvas, 40)
            self.gorsellestir(canvas, 40, cozulmemis)
            canvas.pack(fill=BOTH, expand=1)
            root.geometry(f"{self.boyut*45}x{self.boyut*45}+300+300")
            root.mainloop()
        root.mainloop()


soru = HazineAvi()

# soru.grid = [
#     [0, 2, 1, 0, 2, 0, 2, 0, 0, 0],
#     [0, 4, 0, 0, 3, 0, 0, 0, 5, 3],
#     [0, 0, 0, 4, 0, 4, 4, 0, 0, 3],
#     [4, 0, 4, 0, 0, 5, 0, 6, 0, 0],
#     [0, 0, 4, 5, 0, 0, 0, 0, 5, 4],
#     [3, 4, 0, 0, 0, 0, 5, 5, 0, 0],
#     [0, 0, 4, 0, 4, 0, 0, 5, 0, 5],
#     [2, 0, 0, 3, 3, 0, 6, 0, 0, 0],
#     [3, 6, 0, 0, 0, 3, 0, 0, 4, 0],
#     [0, 0, 0, 4, 0, 2, 0, 2, 1, 0],
# ]
# soru.grid = [
#     [2, 0, 0, 0, 2, 2, 0, 0, 0, 2],
#     [0, 5, 0, 4, 0, 0, 5, 0, 6, 0],
#     [0, 0, 4, 0, 3, 0, 0, 3, 0, 0],
#     [0, 4, 0, 0, 3, 0, 4, 0, 4, 0],
#     [3, 0, 5, 3, 2, 0, 0, 0, 0, 2],
#     [3, 0, 0, 0, 0, 3, 3, 4, 0, 2],
#     [0, 5, 0, 4, 0, 2, 0, 0, 4, 0],
#     [0, 0, 2, 0, 0, 4, 0, 2, 0, 0],
#     [0, 5, 0, 4, 0, 0, 2, 0, 5, 0],
#     [2, 0, 0, 0, 3, 2, 0, 0, 0, 2],
# ]
# soru.grid = [
#     [0, 0, 0, 0, 0, 4, 0, 0],
#     [1, 0, 2, 4, 0, 0, 3, 0],
#     [0, 4, 0, 0, 5, 0, 0, 1],
#     [0, 0, 4, 4, 0, 0, 3, 0],
#     [0, 4, 0, 0, 3, 2, 0, 0],
#     [2, 0, 0, 4, 0, 0, 2, 0],
#     [0, 3, 0, 0, 3, 1, 0, 2],
#     [0, 0, 4, 0, 0, 0, 0, 0],
# ]
# soru.grid = [
#     [0, 0, 0, 0, 0, 0, 0],
#     [0, 2, 3, 4, 3, 5, 0],
#     [0, 1, 0, 0, 0, 3, 0],
#     [0, 0, 0, 5, 0, 0, 0],
#     [0, 1, 0, 0, 0, 3, 0],
#     [0, 1, 2, 2, 3, 4, 0],
#     [0, 0, 0, 0, 0, 0, 0],
# ]
# soru.grid = [
#     [0, 0, 0, 1, 0, 0, 2],
#     [0, 2, 2, 0, 0, 4, 0],
#     [0, 3, 3, 0, 2, 0, 0],
#     [4, 0, 0, 0, 0, 0, 1],
#     [0, 0, 4, 0, 2, 2, 0],
#     [0, 4, 0, 0, 3, 3, 0],
#     [2, 0, 0, 1, 0, 0, 0],
# ]

# soru.grid = [
#     [0, 0, 0, 0, 0],
#     [0, 5, 0, 0, 2],
#     [0, 0, 0, 0, 0],
#     [0, 2, 0, 0, 1],
#     [0, 0, 0, 0, 0],
# ]

# soru.boyut = len(soru.grid)
soru.boyut = 10
# print(soru.solver2(soru.grid))
# print(np.matrix(cozulmus), np.matrix(cozulmemis))
soru.main()
