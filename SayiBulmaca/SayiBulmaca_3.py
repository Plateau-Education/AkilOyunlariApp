# An algorithm to generate "Sayı Bulmaca"
import random as rd
import timeit
import itertools


class SayiBulmaca:
    def __init__(self):
        self.possible_nums = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
        self.answer = []
        self.grid = []
        self.set = set()

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
            for j in range(clue):
                choice = rd.choice(answer)
                row.append(choice)
                self.set.add(choice)
                answer.remove(row[-1])
            for i in range(3 - clue):
                choice = rd.choice(num_list)
                row.append(choice)
                self.set.add(choice)
                num_list.remove(row[-1])
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
        self.grid.append(self.answer)

    def ClueGuide(self, trygrid=None, tryanswer=None):
        if trygrid:
            for i in trygrid:
                x = 0
                y = 0
                for j in tryanswer:
                    if j in i and j == i[tryanswer.index(j)]:
                        x += 1
                    elif j in i:
                        y -= 1
                i.append((x, y))
        else:
            grid = self.grid[:-1].copy()
            answer = self.answer.copy()
            for i in grid:
                x = 0
                y = 0
                for j in answer:
                    if j in i and j == i[answer.index(j)]:
                        x += 1
                    elif j in i:
                        y -= 1
                if x == 2:
                    rd.shuffle(self.grid[grid.index(i)])
                    self.ClueGuide()
                    continue
                self.grid[grid.index(i)].append((x, y))

    def PrintGrid(self):
        self.grid[-1].append((3, 0))
        for p in self.grid:
            print(p)
        # for i in self.grid:
        # print(i)

    def Solver(self):
        nums = self.set.copy()
        grid = [i[:-1] for i in self.grid[:-1]]
        guide = [i[-1] for i in self.grid[:-1]]
        solve = 0
        for a, b, c in itertools.permutations(nums, 3):
            self.ClueGuide(grid, [a, b, c])
            if [i[-1] for i in grid] == guide:
                solve += 1
                if solve > 1:
                    return solve
        if solve == 1:
            return True


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


def main():
    game = SayiBulmaca()
    game.SetAnswer()
    game.PerfectGrid()
    game.ClueGuide()
    for u in game.grid:
        game.grid[game.grid.index(u)] = u[:4]
    if game.Solver():
        game.grid[-1].append((3, 0))
        return game.grid
    else:
        return main()


startbase = timeit.default_timer()
for _ in range(100):
    # start1 = timeit.default_timer()
    a = main()
    # end1 = timeit.default_timer()
    # print(f"Süre: {end1-start1}")
    # for i in a:
    #     print(i)
endbase = timeit.default_timer()
print(f"Toplam süre: {endbase - startbase} seconds.")
