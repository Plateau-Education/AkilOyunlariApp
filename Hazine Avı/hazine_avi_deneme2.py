import random, time, timeit, copy
from tkinter import Tk, Canvas, BOTH
import tkinter.font as tkFont
import numpy as np
from itertools import combinations

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
     
    def komsular(self,y,x):
        komsular = [(y+1,x), (y-1,x), (y,x+1), (y,x-1),
                    (y+1,x+1), (y+1,x-1), (y-1,x+1), (y-1,x-1)]
        for _ in range(5):
            for komsu in komsular:
                if komsu[0] < 0 or komsu[0] > self.boyut-1 or komsu[1] < 0 or komsu[1] > self.boyut-1:
                    komsular.remove(komsu)
        return komsular

    def count01(self,y,x,grid):
        komsular = self.komsular(y,x)
        list1 = []
        list0 = []
        for komsu in komsular:
            if grid[komsu[0]][komsu[1]] == -1: list1.append((komsu[0],komsu[1]))
            elif grid[komsu[0]][komsu[1]] == 0: list0.append((komsu[0],komsu[1]))
        return len(list0), len(list1), list0, list1, grid[y][x] == len(list1)

    def minmax(self,c):
        if c < 2: 
            minc = 0
            maxc = c+2
        elif c > self.boyut -3:
            minc = c-2
            maxc = self.boyut-1
        else:
            minc = c-2
            maxc = c+2
        return minc,maxc

    def intercept_setList(self,setList):
        s = setList[0]
        for i in setList[1:]:
            s = s & i
        return s

    def possible_combinations(self,y,x,n,list0,list1,grid):
        intercept_list = []
        for c1 in combinations(list0,n-len(list1)):
            comb_set = set()
            copy_g = copy.deepcopy(grid)
            impflag = False
            minx,maxx,miny,maxy = self.boyut,0,self.boyut,0
            for y1,x1 in c1:
                copy_g[y1][x1] = -1
                comb_set.add((y1,x1,-1))
                if y1 < miny: miny = self.minmax(y1)[0]
                if y1 > maxy: maxy = self.minmax(y1)[1]
                if x1 < minx: minx = self.minmax(x1)[0]
                if x1 > maxx: maxx = self.minmax(x1)[1]
        
            for c2 in combinations(list0,len(list0)-n+len(list1)):
                for y2,x2 in c2:
                    copy_g[y2][x2] = -2
                    comb_set.add((y2,x2,-2))
                    if y2 < miny: miny = self.minmax(y2)[0]
                    if y2 > maxy: maxy = self.minmax(y2)[1]
                    if x2 < minx: minx = self.minmax(x2)[0]
                    if x2 > maxx: maxx = self.minmax(x2)[1]
            
            for y3 in range(miny,maxy+1):
                if impflag: break
                for x3 in range(minx,maxx+1):
                    if impflag: break
                    n1 = copy_g[y3][x3]
                    if n1 > 0:
                        count_0, count_1, list_0 = self.count01(y3,x3,copy_g)[:3]
                        if (n-count_1) > count_0: impflag = True
                        elif (n-count_1) == 0:
                            for y4,x4 in list_0:
                                copy_g[y4][x4] = -2
                                comb_set.add((y4,x4,-2))
                        elif (n-count_1) == count_0:
                            for y5,x5 in list_0:
                                copy_g[y5][x5] = -1
                                comb_set.add((y5,x5,-1))
            # print(comb_set)
            if impflag: break
            else:
                intercept_list.append(comb_set)
        print(intercept_list)
        if intercept_list:
            return self.intercept_setList(intercept_list)
        else:
            return set() 
        
            # komsular = self.komsular(y1,x1)
            # for k in komsular:
            #     if grid[k[0]][k[1]] > 0:
            #         komsular2 = self.komsular(k[0],k[1])
            #         if (all([self.count01(y2,x2,grid)[-1] == False for y2,x2 in komsular2 if grid[y2][x2] > 0]) and any([grid[y3][x3] > 0 for y3,x3 in komsular2])):
            #             impflag = False
            #             c0,c1,l0 = self.count01(k[0],k[1],grid)[:3]
            #             # bi dict yapılabilir. içinde her kombinasyonun her komşu sayı için olabilecek kombinasyonları olur. en son bunlar set intercept yapılır.
            #         else:
            #             impflag = True
            #             break

                        
        # comb2 = set()
        # for combination2 in combinations(list0,len(list0)-n+len(list1)):
        #     comb2.add(combination2)

    def first_base(self,grid):
        for y in range(self.boyut):
            for x in range(self.boyut):
                n = grid[y][x]
                if n > 0:
                    count0, count1, list0, list1 = self.count01(y,x,grid)[:4]
                    if (n-count1) > count0: return "Wrong Question"
                    elif (n-count1) == 0:
                        for y2,x2 in list0:
                            grid[y2][x2] = -2
                    elif (n-count1) == count0:
                        for y3,x3 in list0:
                            grid[y3][x3] = -1
                    elif (n-count1) < count0:
                        for pc in self.possible_combinations(y,x,n,list0,list1,grid):
                            if pc == set(): continue
                            y4,x4,n4 = pc
                            grid[y4][x4] = n4

    def solver2(self,grid):
        for _ in range(10):
            self.first_base(grid)

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
