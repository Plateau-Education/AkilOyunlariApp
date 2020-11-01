# An algorithm to generate "Sayı Bulmaca"
import random as rd
import timeit
import itertools
from copy import deepcopy


class SayiBulmaca:
    def __init__(self):
        self.possible_nums = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
        self.answer = []
        self.grid = []
        self.set = set()
        self.solutions = 0

    def SetAnswer(self):
        for i in range(3):
            self.answer.append(rd.choice(self.possible_nums))
            self.possible_nums.remove(self.answer[-1])
        while self.answer[0] == 0:
            rd.shuffle(self.answer)

    def PerfectGrid(self):
        flag = 0
        cluessum = 0
        for _ in range(3):
            num_list = self.possible_nums.copy()
            answer = self.answer.copy()
            row = []
            clue = rd.randint(1, 2)
            flag += 1
            if flag > 2:
                if cluessum == 2:
                    clue = 2
            cluessum += clue
            for j in range(clue):
                choice = rd.choice(answer)
                row.append(choice)
                self.set.add(choice)
                answer.remove(choice)
            for i in range(3 - clue):
                choice = rd.choice(num_list)
                row.append(choice)
                self.set.add(choice)
                num_list.remove(choice)
            rd.shuffle(row)
            while row[0] == 0:
                rd.shuffle(row)
            self.grid.append(row)
        flag = True
        for key in self.answer:
            if key not in self.set:
                flag = False
        if not flag:
            self.grid = []
            self.set = set()
            self.PerfectGrid()
            return

    def ClueGuide(self):
        grid = deepcopy(self.grid)
        answer = self.answer.copy()
        for b, i in enumerate(grid):
            x = 0
            y = 0
            for c, j in enumerate(answer):
                if j == i[c]:
                    x += 1
                elif j in i:
                    y -= 1
            if x == 2:
                rd.shuffle(self.grid[b])
                self.ClueGuide()
                continue
            self.grid[b].append([x, y])
        for i, row in enumerate(self.grid):
            self.grid[i] = row[:4]
        # grid = deepcopy(self.grid)
        # answer = self.answer.copy()
        # guide = [[0, 0]] * 3
        # for i, row in enumerate(grid):
        #     for j, n in enumerate(row):
        #         if answer[j] == n:
        #             guide[i][0] += 1
        #         elif answer[j] in row:
        #             guide[i][1] -= 1
        #     if guide[i][0] == 2:
        #         rd.shuffle(self.grid[i])
        #         return self.ClueGuide()
        # for i in range(3):
        #     self.grid[i].append(guide[i])
        # if rowx:
        #     x = 0
        #     y = 0
        #     for j in range(3):
        #         if grid[rowx][j] == answer[j]:
        #             x += 1
        #         elif j in grid[rowx]:
        #             y -= 1
        #     if x == 2:
        #         rd.shuffle(self.grid[rowx])
        #         self.ClueGuide(rowx)
        #     else:
        #         self.grid[rowx].append([x, y])
        # else:
        #     for i in range(3):
        #         x = 0
        #         y = 0
        #         for j in range(3):
        #             if grid[i][j] == answer[j]:
        #                 x += 1
        #             elif answer[j] in grid[i]:
        #                 y -= 1
        #         if x == 2:
        #             rd.shuffle(self.grid[i])
        #             self.ClueGuide(i)
        #             continue
        #         else:
        #             self.grid[i].append([x, y])
        # grid = deepcopy(self.grid)
        # answer = self.answer.copy()
        # sa = 0
        # for row in self.grid:
        #     x = 0
        #     y = 0
        #     for j, n in enumerate(row):
        #         if n == answer[j]:
        #             x += 1
        #         elif n in answer:
        #             y -= 1
        #     if x == 2:
        #         rd.shuffle(self.grid[sa])
        #         self.ClueGuide()
        #         continue
        #     self.grid[sa].append([x, y])
        #     self.grid[sa] = self.grid[sa][:4]
        #     sa += 1

    def isUnique(self, bas=0, gridx=None, setq=None, answerx=None, guidex=None):
        if self.solutions > 1:
            return
        if bas == len(self.grid):
            if guidex == [[0, 0]] * len(guidex):
                self.solutions += 1
            return
        if bas == 0:
            sets = deepcopy(self.set)
            sets.discard(0)
        else:
            sets = deepcopy(setq)
        for roundx in range(len(sets)):
            if bas == 0:
                setx = deepcopy(self.set)
                grid = [i[:-1] for i in deepcopy(self.grid)]
                answer = [10] * len(grid)
                guide = [i[-1] for i in deepcopy(self.grid)]
                answer[bas] = list(sets)[roundx]
            else:
                answer = deepcopy(answerx)
                setx = deepcopy(setq)
                grid = deepcopy(gridx)
                guide = deepcopy(guidex)
                answer[bas] = list(setx)[roundx]
            for i, row in enumerate(grid):
                for j, n in enumerate(row):
                    print(n, grid, setx, guide)
                    if n in setx:
                        if n == answer[j]:
                            guide[i][0] -= 1
                        elif n in answer:
                            guide[i][1] += 1
            for i, row in enumerate(guide):
                if row == [0, 0]:
                    for n in grid[i]:
                        setx.discard(n)
            setx.discard(answer[bas])
            self.isUnique(bas=bas + 1, gridx=grid, setq=setx, answerx=answer, guidex=guide)
        return

    def PrintGrid(self):
        # self.grid[-1].append((3, 0))
        for p in self.grid:
            print(p)
        # for i in self.grid:
            # print(i)

    # def Solver(self):
    #     nums = self.set.copy()
    #     grid = [i for i in self.grid[:-1]]
    #     guide = [i[-1] for i in self.grid[:-1]]
    #     solve = 0
    #     for a, b, c in itertools.permutations(nums, 3):
    #         self.ClueGuide(grid, [a, b, c])
    #         if [i[-1] for i in grid] == guide:
    #             solve += 1
    #             if solve > 1:
    #                 return False
    #     if solve == 1:
    #         return True


# store = open("C:/Users/Proper/PycharmProjects/untitled/Storage.txt", "a+")


# def main():
#     game = SayiBulmaca()
#     game.SetAnswer()
#     game.PerfectGrid()
#     game.ClueGuide()
#     for u in game.grid:
#         game.grid[game.grid.index(u)] = u[:4]
#     if game.Solver():
#         game.PrintGrid()
#     else:
#         return main()
# game.grid[game.grid.index(u)]
def main():
    game = SayiBulmaca()
    game.SetAnswer()
    game.PerfectGrid()
    game.ClueGuide()
    game.isUnique()
    if game.solutions == 1:
        game.answer.append((3, 0))
        return game.grid, game.answer
    else:
        # print(game.solutions)
        # for i in game.grid:
        #     print(i)
        # print(game.answer)
        return main()


for _ in range(100):
    # start1 = timeit.default_timer()
    a = main()
    # end1 = timeit.default_timer()
    # print(f"Süre: {end1-start1}")
    # for i in a[0]:
    #     print(i)
    # print(a[1])
