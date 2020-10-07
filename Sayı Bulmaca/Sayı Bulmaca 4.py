# An algorithm to generate "Sayı Bulmaca"
# Easy --> 0 3 (20sn)dk, Medium --> 1 7 (63sn)dk, Hard --> 10 45 (42dk)dk
import random as rd
import timeit


class SayiBulmaca:
    def __init__(self):
        self.possible_nums = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
        self.answer = []
        self.grid = []
        self.set = set()
        self.clues = -4

    def SetAnswer(self):
        for i in range(4):
            self.answer.append(rd.choice(self.possible_nums))
            self.possible_nums.remove(self.answer[-1])
        while self.answer[0] == 0:
            rd.shuffle(self.answer)

    def PerfectGrid(self):
        for _ in range(4):
            num_list = self.possible_nums.copy()
            answer = self.answer.copy()
            row = []
            clue = rd.randint(1, 3)
            for j in range(clue):
                choice = rd.choice(answer)
                row.append(choice)
                self.set.add(choice)
                answer.remove(row[-1])
            for i in range(4 - clue):
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
            grid = self.grid.copy()
            answer = self.answer.copy()
            for i in grid:
                x = 0
                y = 0
                for j in answer:
                    if j in i and j == i[answer.index(j)]:
                        x += 1
                        self.clues += 1
                    elif j in i:
                        y -= 1
                        self.clues += 1
                self.grid[grid.index(i)].append((x, y))

    def PrintGrid(self):
        for i in self.grid:
            print(i)

    def Solver(self):
        nums = list(self.set)
        grid = [i[:-1] for i in self.grid[:-1]]
        guide = [i[-1] for i in self.grid[:-1]]
        solve = 0
        for a in nums:
            for b in nums:
                for c in nums:
                    for d in nums:
                        self.ClueGuide(grid, [a, b, c, d])
                        if [i[-1] for i in grid] == guide:
                            solve += 1
        if solve == 1:
            return True


def main(levelx):
    game = SayiBulmaca()
    game.SetAnswer()
    game.PerfectGrid()
    game.ClueGuide()
    if game.Solver():
        if levelx == 'Easy':
            if 11 > game.clues > 8:
                game.PrintGrid()
            else:
                return main(levelx)
        if levelx == 'Medium':
            if 9 > game.clues > 6:
                game.PrintGrid()
            else:
                return main(levelx)
        if levelx == 'Hard':
            if 4 < game.clues < 7:
                game.PrintGrid()
            else:
                return main(levelx)
    else:
        main(levelx)


level = input("Easy-Medium-Hard\nChoose\n")
start1 = timeit.default_timer()
main(level)
end1 = timeit.default_timer()
print(f"Toplam süre: {end - start} seconds.")
