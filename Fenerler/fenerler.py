import random, time, timeit
from tkinter import Tk, Canvas, BOTH
import tkinter.font as tkFont


class Fenerler:

    def __init___(self):
        self.boyut = 5
    
    def orta_nokta(self,row, column, genislik):
        ortanokta_x = (row + 1/2)*genislik+10
        ortanokta_y = (column + 1/2)*genislik+10
        return (ortanokta_x, ortanokta_y)

    def daire_koordinat(self,row,column,genislik):
        x1 = row*genislik+10
        y1 = column*genislik+10
        x2 = (row+1)*genislik+10
        y2 = (column+1)*genislik+10
        return x1,y1,x2,y2

    def create_grid(self,canvas,genislik):
        self.fener_sayisi = self.boyut - 2
        for row in range(self.boyut):
            for column in range(self.boyut):
                canvas.create_rectangle(row*genislik+10, column*genislik+10, (row+1)*genislik+10, (column+1)*genislik+10)
        
    def fener_yerlestirme(self,canvas):
        carpilar = set()
        fener_koordinatlari = []
        for _ in range(self.fener_sayisi):
            uygun_koordinatlar = set([(r,c) for r in range(self.boyut) for c in range(self.boyut)])-carpilar
            uygun_koordinatlar = uygun_koordinatlar-set(fener_koordinatlari)
            fener = random.choice(list(uygun_koordinatlar))
            fener_koordinatlari.append(fener)
            komsular = [(fener[0]+1,fener[1]), (fener[0]-1,fener[1]), 
                            (fener[0],fener[1]+1), (fener[0],fener[1]-1),
                            (fener[0]+1,fener[1]+1), (fener[0]+1,fener[1]-1),
                            (fener[0]-1,fener[1]+1), (fener[0]-1,fener[1]-1)]
            for _ in range(5):
                for komsu in komsular:
                    if komsu[0] < 0 or komsu[0] > self.boyut-1 or komsu[1] < 0 or komsu[1] > self.boyut-1:
                        komsular.remove(komsu)
                    else:
                        if komsu not in carpilar:
                            carpilar.add(komsu)
        uygun_koordinatlar = set([(r,c) for r in range(self.boyut) for c in range(self.boyut)])-carpilar
        uygun_koordinatlar = uygun_koordinatlar-set(fener_koordinatlari)
        return fener_koordinatlari,uygun_koordinatlar
            
    def fener_cizici(self,canvas,genislik,fener_koordinatlari):
        for koor in fener_koordinatlari:
            x1,y1,x2,y2 = self.daire_koordinat(koor[0],koor[1],genislik)
            print(x1,y1,x2,y2)
            canvas.create_oval(x1,y1,x2,y2,fill="red")

    def fener_gorme_sayilari(self,canvas,fener_koordinatlari,uygun_koordinatlar):
        for k in uygun_koordinatlar:
            gorme_sayisi = 0
            for fener in fener_koordinatlari:
                if k[0] == fener[0] or k[1] == fener[1] or any([fener==(k[0]+i,k[1]+i) for i in range(-self.boyut,self.boyut)]) or any([fener==(k[0]+i,k[1]-i) for i in range(-self.boyut,self.boyut)]):
                    gorme_sayisi+=1
            canvas.create_text(self.orta_nokta(k[0],k[1],40), text=str(gorme_sayisi), font=tkFont.Font(family="Poppins",size=20))
        

    def rakam_yerlestirme(self,canvas,uygun_koordinatlar):
        pass        


    def main(self):
        root = Tk()
        canvas = Canvas(root)

        self.create_grid(canvas,40)

        koordinatlar = self.fener_yerlestirme(canvas)
        fener_koordinatlari = koordinatlar[0]
        uygun_koordinatlar = koordinatlar[1]
        print(fener_koordinatlari)
        self.fener_cizici(canvas,40,fener_koordinatlari)

        self.fener_gorme_sayilari(canvas,fener_koordinatlari,uygun_koordinatlar)


        canvas.pack(fill=BOTH,expand=1)
        root.geometry("400x250+300+300")
        root.mainloop()


soru = Fenerler()
soru.boyut = 5
soru.main()
