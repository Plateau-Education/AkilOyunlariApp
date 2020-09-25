import random, time, timeit
from tkinter import Tk, Canvas, BOTH
# import pandas as pd
# import numpy as np

class Patika:

    def __init__(self):
        self.boyut=6

    def orta_nokta(self,row, column, genislik):
        ortanokta_x = (row + 1/2)*genislik+10
        ortanokta_y = (column + 1/2)*genislik+10
        return (ortanokta_x, ortanokta_y)

    def create_grid(self,canvas,genislik):
        for row in range(self.boyut):
            for column in range(self.boyut):
                canvas.create_rectangle(row*genislik+10, column*genislik+10, (row+1)*genislik+10, (column+1)*genislik+10)

    def cozum_yolu_olusturucu(self,canvas,genislik):
        
        kose_sol_asagi = []
        kose_sag_asagi = []
        kose_sol_yukari = []
        kose_sag_yukari = []
        kenar_yukari_asagi = []
        kenar_sag_sol = []

        

    def main(self):

        root = Tk()
        canvas = Canvas(root)

        self.create_grid(canvas,40)

        canvas.pack(fill=BOTH,expand=1)
        root.geometry("400x300+300+300")
        root.mainloop()


soru = Patika()
soru.boyut=6
soru.main()
