import random, time, timeit
from tkinter import Tk, Canvas, BOTH
import tkinter.font as tkFont

class Piramit:

    def __init__(self):
        self.boyut = 3

    def create_grid(self,canvas,genislik):
        for column in range(self.boyut):
            for row in range(column+1):
                canvas.create_rectangle(row*genislik*(8/5)+(((self.boyut-column+1)/2)*genislik*(8/5)), column*genislik+10, (row+1)*genislik*(8/5)+(((self.boyut-column+1)/2)*genislik*(8/5)), (column+1)*genislik+10)

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


                
        

    def solver(self,canvas,genislik):
        grid = []
        for column in range(self.boyut):
            c = []
            for row in range(column+1):
                c.append(set([1,2,3,4,5,6,7,8,9]))
            grid.append(c)
        grid[0][0] = set([6])
        grid[2][0] = set([1])
        grid[2][2] = set([9])
        canvas.create_text(self.orta_nokta(0,0,40), text=list(grid[0][0])[0], font=tkFont.Font(family="Poppins",size=25))
        canvas.create_text(self.orta_nokta(0,2,40), text=list(grid[2][0])[0], font=tkFont.Font(family="Poppins",size=25))
        canvas.create_text(self.orta_nokta(2,2,40), text=list(grid[2][2])[0], font=tkFont.Font(family="Poppins",size=25))
        
        # print(self.tum_ihtimaller(6,False))

        for row in range(1,self.boyut):
            for column in range(row+1):
                if len(grid[row][column]) == 0: return "Wrong Question"
                if len(grid[row][column]) == 1: continue
                
                # print(grid[row][column])
                if column != row and column != 0:#son karede ve ilk karede değilse
                    grid[row][column] = grid[row][column] & set([j[0] for i in grid[row-1][column] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column+1]])
                    grid[row][column] = grid[row][column] & set([j[1] for i in grid[row-1][column-1] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[0] in grid[row][column-1]])
                
                elif column == 0:#ilk karede ise 
                    grid[row][column] = grid[row][column] & set([j[0] for i in grid[row-1][column] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column+1]])
                elif column == row:#son karede ise
                    grid[row][column] = grid[row][column] & set([j[0] for i in grid[row-1][column-1] for j in self.tum_ihtimaller(i,(row==(self.boyut-1))) if j[1] in grid[row][column-1]])                
                
                print(grid)
                    


                
                



        # print(grid)

    def main(self):
        root = Tk()
        canvas = Canvas(root)

        self.create_grid(canvas,40)
        self.solver(canvas,40)

        canvas.pack(fill=BOTH,expand=1)
        root.geometry("400x250+300+300")
        root.mainloop()
soru = Piramit()
soru.boyut = 3
soru.main()