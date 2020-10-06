# An algorithm to generate "Sayı Bulmaca"
import random as rd


class SayiBulmaca:

    def __init__(self):
        self.possible_nums = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
        self.answer = []
        self.grid = []
        self.set = set()

    def SetAnswer(self):
        for i in range(4):
            self.answer.append(rd.choice(self.possible_nums))
            self.possible_nums.remove(self.answer[-1])
        while self.answer[0] == 0:
            rd.shuffle(self.answer)

    def Grid(self):
        for _ in range(4):
            num_list = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
            answer = self.answer.copy()
            row = []
            clue = rd.randint(1, 3)
            for j in range(clue):
                choice = rd.choice(answer)
                row.append(choice)
                self.set.add(choice)
                num_list.remove(row[-1])
                answer.remove(row[-1])
            for i in range(4-clue):
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
            self.Grid()
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
                elif j in i:
                    y -= 1
            self.grid[grid.index(i)].append((x, y))

    def PrintGrid(self):
        for i in self.grid:
            print(i)


def main():
    game = SayiBulmaca()
    game.SetAnswer()
    game.Grid()
    game.ClueGuide()
    game.PrintGrid()


main()
