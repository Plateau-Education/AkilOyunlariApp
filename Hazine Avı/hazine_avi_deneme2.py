import random, time, timeit, copy
from tkinter import Tk, Canvas, BOTH
import tkinter.font as tkFont
import numpy as np

class HazineAvi:

    def __init__(self):
        self.boyut = 8
        self.grid = []
        self.solutions = []

    def orta_nokta(self, row, column, genislik):
        ortanokta_x = (row + 1 / 2) * genislik + 10
        ortanokta_y = (column + 1 / 2) * genislik + 10
        return (ortanokta_x, ortanokta_y)

    def daire_koordinat(self,row,column,genislik):
        x1 = row*genislik+15
        y1 = column*genislik+15
        x2 = (row+1)*genislik+5
        y2 = (column+1)*genislik+5
        return x1,y1,x2,y2

    def create_grid(self,canvas,genislik):
        for row in range(self.boyut):
            for column in range(self.boyut):
                canvas.create_rectangle(row*genislik+10, column*genislik+10, (row+1)*genislik+10, (column+1)*genislik+10)

    def gorsellestir(self,canvas,genislik):
        grid = self.solutions[0]
        for row in range(self.boyut):
            for column in range(self.boyut):
                if grid[column][row] > 0:
                    canvas.create_text(
                        self.orta_nokta(row, column, genislik), 
                        text=grid[column][row],
                        font=tkFont.Font(family="Poppins", size=25),
                    )
                elif grid[column][row] == -1:
                    x1,y1,x2,y2 = self.daire_koordinat(row,column,genislik)
                    canvas.create_oval(x1,y1,x2,y2,fill="green")
                elif grid[column][row] == -2:
                    canvas.create_line(row*genislik+10,column*genislik+10,(row+1)*genislik+10,(column+1)*genislik+10)
                    canvas.create_line(row*genislik+10,(column+1)*genislik+10,(row+1)*genislik+10,column*genislik+10)
     

    def count01(self,y,x,grid):
        komsular = [(y+1,x), (y-1,x), (y,x+1), (y,x-1),
                    (y+1,x+1), (y+1,x-1), (y-1,x+1), (y-1,x-1)]
        for _ in range(5):
            for komsu in komsular:
                if komsu[0] < 0 or komsu[0] > self.boyut-1 or komsu[1] < 0 or komsu[1] > self.boyut-1:
                    komsular.remove(komsu)
        list1 = []
        list0 = []
        for komsu in komsular:
            if grid[komsu[0]][komsu[1]] == -1: list1.append((komsu[0],komsu[1]))
            elif grid[komsu[0]][komsu[1]] == 0: list0.append((komsu[0],komsu[1]))
        return len(list0), len(list1), list0, list1

    def solver2(self,grid):
        for _ in range(10):
            for y in range(self.boyut):
                for x in range(self.boyut):
                    n = grid[y][x]
                    if n > 0:
                        count0, count1, list0, list1 = self.count01(y,x,grid)
                        if (n-count1) > count0: return "Wrong Question"
                        elif (n-count1) == 0:
                            for y2,x2 in list0:
                                grid[y2][x2] = -2
                        elif (n-count1) == count0:
                            for y3,x3 in list0:
                                grid[y3][x3] = -1
        self.solutions.append(copy.deepcopy(grid))                    

    def main(self):

        root = Tk()
        canvas = Canvas(root)

        # start = timeit.default_timer()
        self.create_grid(canvas,40)
        # self.elmas_yerlestirme(20,30)
        # self.sayi_belirleme()
        # self.sayi_azaltma()
        self.solver2(self.grid)
        print("Solutions: ",self.solutions)
        # end = timeit.default_timer()
        # print(f"It took {end-start} seconds.")
        self.gorsellestir(canvas,40)
        
        canvas.pack(fill=BOTH,expand=1)
        root.geometry(f"{self.boyut*45}x{self.boyut*45}+300+300")
        root.mainloop()

soru = HazineAvi()
soru.grid = [[0,0,0,0,0,4,0,0],
             [1,0,2,4,0,0,3,0],
             [0,4,0,0,5,0,0,1],
             [0,0,4,4,0,0,3,0],
             [0,4,0,0,3,2,0,0],
             [2,0,0,4,0,0,2,0],
             [0,3,0,0,3,1,0,2],
             [0,0,4,0,0,0,0,0]]
# soru.boyut = len(soru.grid)
soru.boyut = 8
soru.main()
