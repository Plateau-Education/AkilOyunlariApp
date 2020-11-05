# An algorithm to generate "Sayı Bulmaca"
import random as rd
from copy import deepcopy


class SayiBulmaca:
    def __init__(self):
        self.possible_nums = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
        self.answer = []
        self.grid = []
        self.set = set()
        self.solutions = 0
        self.guide = [[0, 0], [0, 0], [0, 0], [0, 0], [0, 0]]

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

    def ClueGuide(self, trygrid=None, tryanswer=None, count = 0):
        if trygrid:
            for i in trygrid:
                x = 0
                y = 0
                for j in tryanswer:
                    if j in i and j == i[tryanswer.index(j)]:
                        x += 1
                    elif j in i:
                        y -= 1
                i[-1] = [x, y]
        else:
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
                if x == 4 or y == -4:
                    rd.shuffle(self.grid[b])
                    self.ClueGuide(count=1)
                    continue
                self.guide[b] = [x, y]
            if count == 0:
                flag = 0
                arti = 0
                for n in self.guide:
                    flag += n[0] - n[1]
                    if n[1] == 0:
                        arti += 1
                if flag > 10 and arti == 0:
                    return False
                for i, row in enumerate(self.grid):
                    row.append(self.guide[i])
            return True

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
                    # print(n, grid, setx, guide)
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


def main():
    game = SayiBulmaca()
    game.SetAnswer()
    game.PerfectGrid()
    if game.ClueGuide():
        game.isUnique()
        if game.solutions == 1:
            game.answer.append([5, 0])
            game.grid.append(game.answer)
            return game.grid
        else:
            return main()
    else:
        return main()
