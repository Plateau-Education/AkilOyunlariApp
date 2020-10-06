# An algorithm to generate "Sayı Bulmaca"
import random as rd


class SayiBulmaca:
    def __init__(self):
        self.possible_nums = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
        self.answer = []
        self.grid = []
        self.set = set()
        self.clues = -5

    def SetAnswer(self):
        for i in range(5):
            self.answer.append(rd.choice(self.possible_nums))
            self.possible_nums.remove(self.answer[-1])
        while self.answer[0] == 0:
            rd.shuffle(self.answer)

    def PerfectGrid(self):
        for _ in range(5):
            num_list = self.possible_nums.copy()
            answer = self.answer.copy()
            row = []
            clue = rd.randint(1, 4)
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
        self.grid.append(self.answer)

    def ClueGuide(self):
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


def main(levelx):
    game = SayiBulmaca()
    game.SetAnswer()
    game.PerfectGrid()
    game.ClueGuide()
    if levelx == 'Easy':
        if game.clues > 11:
            game.PrintGrid()
        else:
            return main(levelx)
    if levelx == 'Medium':
        if 12 > game.clues > 8:
            game.PrintGrid()
        else:
            return main(levelx)
    if levelx == 'Hard':
        if game.clues < 9:
            game.PrintGrid()
        else:
            return main(levelx)


level = input("Easy-Medium-Hard\nChoose\n")
main(level)
