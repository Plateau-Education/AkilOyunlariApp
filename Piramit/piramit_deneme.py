import random, time, timeit
from tkinter import Tk, Canvas, BOTH
import tkinter.font as tkFont

class Piramit:

    def __init__(self):
        pass

    def create_grid(self,canvas,genislik):
        pass

    def main(self):
        root = Tk()
        canvas = Canvas(root)

        self.create_grid(canvas,40)

        canvas.pack(fill=BOTH,expand=1)
        root.geometry("400x250+300+300")
        root.mainloop()
