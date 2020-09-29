import random, time, timeit
from tkinter import Tk, Canvas, BOTH
import tkinter.font as tkFont

class Piramit:

    def __init__(self):
        self.boyut = 3

    def orta_nokta(self,row, column, genislik):
        ortanokta_x = (row + 1/2)*genislik*(8/5)+(((self.boyut-column+1)/2)*genislik*(8/5))
        ortanokta_y = (column + 1/2)*genislik+10
        return (ortanokta_x, ortanokta_y)

    def tum_ihtimaller(self, sayi, en_alt_satirin_ustu):
        if sayi <= 1 or sayi > 9: return []
        ihtimaller = []
        if not en_alt_satirin_ustu:
            baslangic = 2
        else:
            baslangic = 1

        for i in range(baslangic,9-sayi+1):
            ihtimaller.append((i,i+sayi))
            ihtimaller.append((i+sayi,i))
        for i in range(baslangic,sayi//2):
            ihtimaller.append((i,sayi-i))
            ihtimaller.append((sayi-i,i))
        return ihtimaller

    def possible(self, grid, row, column, n):
        if (row==(self.boyut-1)):
            if n == 1: return False
        
        if column != row and column != 0:#son karede ve ilk karede değilse
            if n not in set([j[0] for i in grid[row-1][column] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column+1]]) & set([j[1] for i in grid[row-1][column-1] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[0] in grid[row][column-1]]) and n not in set([j[0] for i in grid[row] for j in i if len(j) == 1]):
                pass
        elif column == 0:#ilk karede ise 
            return n in set([j[0] for i in grid[row-1][column] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column+1]])
        elif column == row:#son karede ise
            return n in set([j[0] for i in grid[row-1][column-1] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column-1]])                
        

    def en_dusuk_sayida_ihtimal(self,grid):
        en_dusuk_ihtimal = (9,0,0)
        for row in range(1,self.boyut):
            for column in range(row+1):
                if len(grid[row][column]) == 1: continue
                elif len(grid[row][column]) < en_dusuk_ihtimal[0]:
                    en_dusuk_ihtimal = (len(grid[row][column]), row, column)
        return en_dusuk_ihtimal

    def grid_olusturucu(self):

        # for column in range(self.boyut):
        #     for row in range(column+1):
        #         canvas.create_rectangle(row*genislik*(8/5)+(((self.boyut-column+1)/2)*genislik*(8/5)), column*genislik+10, (row+1)*genislik*(8/5)+(((self.boyut-column+1)/2)*genislik*(8/5)), (column+1)*genislik+10)

        grid = []
        for column in range(self.boyut):
            c = []
            for row in range(column+1):
                c.append(set([1,2,3,4,5,6,7,8,9]))
            grid.append(c)
        grid[0][0] = set([4])
        grid[2][0] = set([1])
        grid[2][2] = set([7])
        # canvas.create_text(self.orta_nokta(0,0,40), text=list(grid[0][0])[0], font=tkFont.Font(family="Poppins",size=25))
        # canvas.create_text(self.orta_nokta(0,2,40), text=list(grid[2][0])[0], font=tkFont.Font(family="Poppins",size=25))
        # canvas.create_text(self.orta_nokta(2,2,40), text=list(grid[2][2])[0], font=tkFont.Font(family="Poppins",size=25))
        
        return grid

    def solver(self,grid):
        
        for row in range(1,self.boyut):
            for column in range(row+1):
                if len(grid[row][column]) > 1:
                    if column != row and column != 0:#son karede ve ilk karede değilse
                        # print(set([j[0] for i in grid[row-1][column] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column+1]]) | set([j[1] for i in grid[row-1][column-1] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[0] in grid[row][column-1]]))
                        for n in range(1,10):#set([j[0] for i in grid[row-1][column] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column+1]]) & set([j[1] for i in grid[row-1][column-1] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[0] in grid[row][column-1]]):    
                            if n not in set([list(j)[0] for j in [i for i in grid[row]] if len(j) == 1]) and n not in set([list(j)[0]+1 for j in [i for i in grid[row]] if len(j) == 1]) and n not in set([list(j)[0]-1 for j in [i for i in grid[row]] if len(j) == 1]):
                                if len(grid[row][column+1]) == 1 and len(grid[row-1][column]) == 1 and not ((abs(n-list(grid[row][column+1])[0]) == list(grid[row-1][column])[0]) or (n+list(grid[row][column+1])[0] == list(grid[row-1][column])[0])): continue
                                if len(grid[row][column+1]) == 1 and len(grid[row-1][column]) == 1 and not ((abs(n-list(grid[row][column+1])[0]) == list(grid[row-1][column])[0]) or (n+list(grid[row][column+1])[0] == list(grid[row-1][column])[0])): continue
                                grid[row][column] = set([n])
                                self.solver(grid)
                                grid[row][column] = set([j[0] for i in grid[row-1][column] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column+1]]) & set([j[1] for i in grid[row-1][column-1] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[0] in grid[row][column-1]])
                        return
                    elif column == 0:#ilk karede ise 
                        for n in range(1,10):#set([j[0] for i in grid[row-1][column] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column+1]]):
                            if n not in set([list(j)[0] for j in [i for i in grid[row]] if len(j) == 1]) and n not in set([list(j)[0]+1 for j in [i for i in grid[row]] if len(j) == 1]) and n not in set([list(j)[0]-1 for j in [i for i in grid[row]] if len(j) == 1]):
                                if len(grid[row][column+1]) == 1 and len(grid[row-1][column]) == 1 and not ((abs(n-list(grid[row][column+1])[0]) == list(grid[row-1][column])[0]) or (n+list(grid[row][column+1])[0] == list(grid[row-1][column])[0])): continue
                                grid[row][column] = set([n])
                                self.solver(grid)
                                grid[row][column] = set([j[0] for i in grid[row-1][column] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column+1]])
                        return
                    elif column == row:#son karede ise
                        for n in range(1,10):#set([j[0] for i in grid[row-1][column-1] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column-1]]):             
                            if n not in set([list(j)[0] for j in [i for i in grid[row]] if len(j) == 1]) and n not in set([list(j)[0]+1 for j in [i for i in grid[row]] if len(j) == 1]) and n not in set([list(j)[0]-1 for j in [i for i in grid[row]] if len(j) == 1]):
                                if len(grid[row][column-1]) == 1 and len(grid[row-1][column-1]) == 1 and not ((abs(n-list(grid[row][column-1])[0]) == list(grid[row-1][column-1])[0]) or (n+list(grid[row][column-1])[0] == list(grid[row-1][column-1])[0])): continue
                                grid[row][column] = set([n])
                                self.solver(grid)
                                grid[row][column] = set([j[0] for i in grid[row-1][column-1] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column-1]])
                        return
        

        print(grid)
                    
                # if len(grid[row][column]) == 0: return "Wrong Question"
                # if len(grid[row][column]) == 1: continue


                # # print(grid[row][column])
                # if column != row and column != 0:#son karede ve ilk karede değilse
                #     grid[row][column] = grid[row][column] & set([j[0] for i in grid[row-1][column] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column+1]])
                #     grid[row][column] = grid[row][column] & set([j[1] for i in grid[row-1][column-1] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[0] in grid[row][column-1]])
                
                # elif column == 0:#ilk karede ise 
                #     grid[row][column] = grid[row][column] & set([j[0] for i in grid[row-1][column] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column+1]])
                # elif column == row:#son karede ise
                #     grid[row][column] = grid[row][column] & set([j[0] for i in grid[row-1][column-1] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column-1]])                
                                

                # print(grid) 

                # if len(grid[row][column]) == 1: 
                #     canvas.create_text(self.orta_nokta(column,row,40), text=list(grid[row][column])[0], font=tkFont.Font(family="Poppins",size=25))
                #     continue
        

                
                    


                
                



        # print(grid)

    def main(self):
        # root = Tk()
        # canvas = Canvas(root)

        grid = self.grid_olusturucu()
        self.solver(grid)

        # canvas.pack(fill=BOTH,expand=1)
        # root.geometry("400x250+300+300")
        # root.mainloop()
soru = Piramit()
soru.boyut = 3
soru.main()