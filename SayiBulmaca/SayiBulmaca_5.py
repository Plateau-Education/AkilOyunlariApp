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
        for i in range(5):
            self.answer.append(rd.choice(self.possible_nums))
            self.possible_nums.remove(self.answer[-1])
        while self.answer[0] == 0:
            rd.shuffle(self.answer)

    def PerfectGrid(self):
        flag = 0
        cluessum = 0
        for _ in range(5):
            num_list = self.possible_nums.copy()
            answer = self.answer.copy()
            row = []
            clue = rd.randint(1, 3)
            flag += 1
            if flag > 3:
                if cluessum < 5:
                    clue += 1
            cluessum += clue
            for j in range(clue):
                choice = rd.choice(answer)
                row.append(choice)
                self.set.add(choice)
                answer.remove(row[-1])
            for i in range(5 - clue):
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
        rd.shuffle(self.grid)
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
                    if x == 4 or y == -4:
                        rd.shuffle(self.grid[grid.index(i)])
                        self.ClueGuide()
                        continue
                self.grid[grid.index(i)].append((x, y))

    def PrintGrid(self):
        self.grid[-1].append((5, 0))
        for i in self.grid:
            print(i)

    def Solver(self):
        nums = list(self.set)
        grid = [i[:-1] for i in self.grid[:-1]]
        guide = [i[-1] for i in self.grid[:-1]]
        solve = 0
        for a, b, c, d, e in itertools.permutations(nums, 5):
            self.ClueGuide(grid, [a, b, c, d, e])
            if [i[-1] for i in grid] == guide:
                solve += 1
                if solve > 1:
                    return False
        if solve == 1:
            return True


def main():
    game = SayiBulmaca()
    game.SetAnswer()
    game.PerfectGrid()
    game.ClueGuide()
    for u in game.grid:
        game.grid[game.grid.index(u)] = u[:6]
    if game.Solver():
        game.PrintGrid()
    else:
        return main()


start1 = timeit.default_timer()
main()
end1 = timeit.default_timer()
print(f"Toplam süre: {end1 - start1} seconds.")
