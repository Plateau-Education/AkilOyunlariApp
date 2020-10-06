import random, time, timeit, copy
from tkinter import Tk, Canvas, BOTH
import tkinter.font as tkFont
import numpy as np

class HazineAvi:

    def __init__(self):
        self.boyut = 8
        self.grid = []

    def create_grid(self,canvas,genislik):
        for row in range(self.boyut):
            for column in range(self.boyut):
                canvas.create_rectangle(row*genislik+10, column*genislik+10, (row+1)*genislik+10, (column+1)*genislik+10)

    def full_for_solver1(self,y,x,n):
        komsular = [(y+1,x), (y-1,x), (y,x+1), (y,x-1),
                    (y+1,x+1), (y+1,x-1), (y-1,x+1), (y-1,x-1)]
        for _ in range(5):
            for komsu in komsular:
                if komsu[0] < 0 or komsu[0] > self.boyut-1 or komsu[1] < 0 or komsu[1] > self.boyut-1:
                    komsular.remove(komsu)
        count1 = 0
        count0 = 0
        for komsu in komsular:
            if self.grid[komsu[0]][komsu[1]] == -1: count1 += 1
            elif self.grid[komsu[0]][komsu[1]] == 0: count0 += 1
        # print(self.grid[y][x],count)
        if n == -1: return self.grid[y][x] == count1
        else: return count0,count1


    def possible_for_solver1(self,y,x,n):
        komsular = [(y+1,x), (y-1,x), (y,x+1), (y,x-1),
                    (y+1,x+1), (y+1,x-1), (y-1,x+1), (y-1,x-1)]
        for _ in range(5):
            for komsu in komsular:
                if komsu[0] < 0 or komsu[0] > self.boyut-1 or komsu[1] < 0 or komsu[1] > self.boyut-1:
                    komsular.remove(komsu)
        if n == -1: return all([self.full_for_solver1(y2,x2,-1) == False for y2,x2 in komsular if self.grid[y2][x2] > 0])
        else: return all([(self.grid[y2][x2] - self.full_for_solver1(y2,x2,-2)[1] < self.full_for_solver1(y2,x2,-2)[0]) for y2,x2 in komsular if self.grid[y2][x2] > 0])
        


    def solver1(self):
        for y in range(self.boyut):
            for x in range(self.boyut):
                # print(self.grid)
                if self.grid[y][x] == 0:
                    for n in range(-1,-3,-1):
                        if self.possible_for_solver1(y,x,n):
                            self.grid[y][x] = n
                            self.solver1()
                            self.grid[y][x] = 0
                    return
                    
                    

        print(np.matrix(self.grid))


    def main(self):
        root = Tk()
        canvas = Canvas(root)

        self.create_grid(canvas,40)
        self.solver1()
        # print(self.possible_for_solver1(0,7))

        canvas.pack(fill=BOTH,expand=1)
        root.geometry("400x300+300+300")
        root.mainloop()

soru = HazineAvi()
soru.boyut = 8
soru.grid = [[0,0,0,0,0,4,0,0],
             [1,0,2,4,0,0,3,0],
             [0,4,0,0,5,0,0,1],
             [0,0,4,4,0,0,3,0],
             [0,4,0,0,3,2,0,0],
             [2,0,0,4,0,0,2,0],
             [0,3,0,0,3,1,0,2],
             [0,0,4,0,0,0,0,0]]
soru.main()