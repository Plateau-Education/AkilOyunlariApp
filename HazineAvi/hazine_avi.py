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

    def full_for_solver1(self,y,x,n,grid):
        komsular = [(y+1,x), (y-1,x), (y,x+1), (y,x-1),
                    (y+1,x+1), (y+1,x-1), (y-1,x+1), (y-1,x-1)]
        for _ in range(5):
            for komsu in komsular:
                if komsu[0] < 0 or komsu[0] > self.boyut-1 or komsu[1] < 0 or komsu[1] > self.boyut-1:
                    komsular.remove(komsu)
        count1 = 0
        count0 = 0
        for komsu in komsular:
            if grid[komsu[0]][komsu[1]] == -1: count1 += 1
            elif grid[komsu[0]][komsu[1]] == 0: count0 += 1
        # print(self.grid[y][x],count)
        if n == -1: return grid[y][x] == count1
        else: return count0,count1


    def possible_for_solver1(self,y,x,n,grid):
        komsular = [(y+1,x), (y-1,x), (y,x+1), (y,x-1),
                    (y+1,x+1), (y+1,x-1), (y-1,x+1), (y-1,x-1)]
        for _ in range(5):
            for komsu in komsular:
                if komsu[0] < 0 or komsu[0] > self.boyut-1 or komsu[1] < 0 or komsu[1] > self.boyut-1:
                    komsular.remove(komsu)
        
        if n == -1: return (all([self.full_for_solver1(y2,x2,-1,grid) == False for y2,x2 in komsular if grid[y2][x2] > 0]) and any([grid[y][x] > 0 for y,x in komsular]))
        else: return all([(grid[y2][x2] - self.full_for_solver1(y2,x2,-2,grid)[1] < self.full_for_solver1(y2,x2,-2,grid)[0]) for y2,x2 in komsular if grid[y2][x2] > 0])
        


    def solver1(self,grid):
        for y in range(self.boyut):
            for x in range(self.boyut):
                # print(self.grid)
                if grid[y][x] == 0:
                    for n in range(-1,-3,-1):
                        if self.possible_for_solver1(y,x,n,grid):
                            grid[y][x] = n
                            if self.solver1(grid) == 1:
                                return 1
                            grid[y][x] = 0
                    return
                    
        print(np.matrix(grid))
        if len(self.solutions) > 1: return 1
        self.solutions.append(copy.deepcopy(grid))
        

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
                    
    def elmas_yerlestirme(self,alt_sinir,ust_sinir):
        self.elmas_sayisi = random.randint(alt_sinir,ust_sinir)
        rows = [random.randint(0, self.boyut - 1) for i in range(self.elmas_sayisi)]
        columns = [random.randint(0, self.boyut - 1) for i in range(self.elmas_sayisi)]
        zipped = zip(rows, columns)
        self.grid = np.zeros((self.boyut,self.boyut),dtype=int)
        for row,column in zipped:
            self.grid[row][column] = -1
        # self.solutions.append(self.grid)
        # print(self.grid)

    def sayi_belirleme(self):
        count0 = 0
        for r in range(self.boyut):
            for c in range(self.boyut):
                if self.grid[r][c] == 0:
                    count0+=1
                    self.grid[r][c] = self.full_for_solver1(r,c,0,self.grid)[1]
        while count0 > (self.boyut**2 - self.elmas_sayisi) * (14/24):
            while True:
                y = random.randint(0,self.boyut-1)
                x = random.randint(0,self.boyut-1)
                if self.grid[y][x] > 0:
                    self.grid[y][x] = 0
                    count0-=1
                    break
        
        # self.solutions.append(self.grid)

    def sayi_azaltma(self,backtracking=False):
        self.elmas_yerlestirme(20,30)
        self.sayi_belirleme()
        grid = self.grid.copy()
        print(grid)
        self.solutions = []
        self.solver1(grid)
        if len(self.solutions) != 1: self.sayi_azaltma()

        # count0 = 0
        # while True:
        #     for _ in range(self.boyut**2//2):
        #         y = random.randint(0,self.boyut-1)
        #         x = random.randint(0,self.boyut-1)
        #         if grid[y][x] > 0:
        #             grid[y][x] = 0
        #             count0-=1
        #             break
        #     self.solutions = []
        #     self.solver1(grid)
        #     if len(self.solutions) == 1:
        #         self.grid = grid
        #         if backtracking:
        #             return
        #     else:
        #         self.sayi_azaltma(True)
        #         return



    def main(self):
        root = Tk()
        canvas = Canvas(root)

        # start = timeit.default_timer()
        self.create_grid(canvas,40)
        # self.elmas_yerlestirme(20,30)
        # self.sayi_belirleme()
        # self.sayi_azaltma()
        self.solver1(self.grid)
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
