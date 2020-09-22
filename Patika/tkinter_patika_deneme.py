# from tkinter import Tk, Canvas, Frame, BOTH

# class Example(Frame):

#     def __init__(self):
#         super().__init__()

#         self.initUI()


#     def initUI(self):

#         self.master.title("Lines")
#         self.pack(fill=BOTH, expand=1)

#         canvas = Canvas(self)
#         # canvas.create_line(15, 25, 200, 25)
#         # canvas.create_line(300, 35, 300, 200, dash=(4, 2))
#         # canvas.create_line(55, 85, 155, 85, 105, 180, 55, 85)
#         canvas.create_rectangle(50, 25, 150, 75, fill="blue", width=0)

#         canvas.pack(fill=BOTH, expand=1)


# def main():

#     root = Tk()
#     ex = Example()
#     root.geometry("400x250+300+300")
#     root.mainloop()


# if __name__ == '__main__':
#     main()

import random, time, timeit
from tkinter import Tk, Canvas, BOTH
import pandas as pd
import numpy as np

def blacklist(patika_boyutu=6, siyah_kare_sayisi = 0):
    # siyah_kare_sayisi = patika_boyutu #- 2
    if siyah_kare_sayisi == 0: siyah_kare_sayisi = patika_boyutu
    while True:
        rows = [random.randint(0,patika_boyutu-1) for i in range(siyah_kare_sayisi)]
        columns = [random.randint(0,patika_boyutu-1) for i in range(siyah_kare_sayisi)]
        zipped = zip(rows,columns)
        blackList = set()
        for coor in zipped: 
            if coor not in [(0,1), (0,patika_boyutu-2), (1,0), (1,patika_boyutu-1), (patika_boyutu-2,0), (patika_boyutu-1,patika_boyutu-2), (patika_boyutu-1,1), (patika_boyutu-1,patika_boyutu-2)]:
                blackList.add(coor)
        if len(blackList) >= siyah_kare_sayisi:
            blackList = list(blackList)[:siyah_kare_sayisi]
            break
    print(blackList)
    return blackList
#0,1  0,4  1,0  1,5  4,0  4,5  5,1  5,4

def orta_nokta(row, column, genislik):
    ortanokta_x = (row + 1/2)*genislik+10
    ortanokta_y = (column + 1/2)*genislik+10
    return (ortanokta_x, ortanokta_y)

def create_grid(c,genislik, patika_boyutu=6, siyah_kare_sayisi=0):
    blackList = blacklist(patika_boyutu, siyah_kare_sayisi)

    # blackList = [(4, 4), (4, 2), (6, 0), (6, 3), (0, 4), (2,5), (2,1)]

    #[(0,5), (1,3), (2,0), (5,2)]   /   [(5,0), (5,3), (3,3), (0,3)]   /   [(3,5), (3,2), (1,1), (5,2)]
    for row in range(patika_boyutu):
        for column in range(patika_boyutu):
            if (row,column) in blackList:
                c.create_rectangle(row*genislik+10, column*genislik+10, (row+1)*genislik+10, (column+1)*genislik+10, fill="black")
            else: c.create_rectangle(row*genislik+10, column*genislik+10, (row+1)*genislik+10, (column+1)*genislik+10)

    return blackList

def tracking_check(kose_sag_asagi, kose_sag_yukari, kose_sol_asagi, kose_sol_yukari, kenar_sag_sol, kenar_yukari_asagi, patika_boyutu=6, siyah_kare_sayisi=0):
    
    current = (0,1)
    gelis = "yukari"
    # print(current)
    current_degisti = True
    counter = 0
    while (current != (0,0)) and (current in kose_sol_asagi or current in kose_sol_yukari or current in kose_sag_asagi or current in kose_sag_yukari or current in kenar_sag_sol or current in kenar_yukari_asagi):
        # time.sleep(0.2)
        counter+=1
        if counter > patika_boyutu**2-siyah_kare_sayisi: # boş kare sayısı
            print("Boş kare sayısından büyük")
            return False
        current_degisti = False
        if gelis == "sag": #cizgi sağından gelmişse

            if current in kose_sag_asagi:  
                current_degisti = True
                gelis = "yukari"
                current = (current[0], current[1]+1) #asagiya gidecek
            elif current in kose_sag_yukari: 
                current_degisti = True
                gelis = "asagi"
                current = (current[0], current[1]-1) #yukariya gidecek
            elif current in kenar_sag_sol:
                current_degisti = True
                gelis = "sag"
                current = (current[0]-1, current[1]) #sola gidecek
        elif gelis == "sol": #cizgi solundan gelmişse

            if current in kose_sol_asagi:  
                current_degisti = True
                gelis = "yukari"
                current = (current[0], current[1]+1) #asagiya gidecek
            elif current in kose_sol_yukari: 
                current_degisti = True
                gelis = "asagi"
                current = (current[0], current[1]-1) #yukariya gidecek
            elif current in kenar_sag_sol:
                current_degisti = True
                gelis = "sol" 
                current = (current[0]+1, current[1]) #saga gidecek
        elif gelis == "asagi": #asagidan gelmişse

            if current in kose_sol_asagi:  
                current_degisti = True
                gelis = "sag"
                current = (current[0]-1, current[1]) #sola gidecek
            elif current in kose_sag_asagi: 
                current_degisti = True
                gelis = "sol" 
                current = (current[0]+1, current[1]) #saga gidecek
            elif current in kenar_yukari_asagi:
                current_degisti = True
                gelis = "asagi"
                current = (current[0], current[1]-1) #yukariya gidecek
        elif gelis == "yukari": #yukaridan gelmişse

            if current in kose_sol_yukari:  
                current_degisti = True
                gelis = "sag"
                current = (current[0]-1, current[1]) #sola gidecek
            elif current in kose_sag_yukari: 
                current_degisti = True
                gelis = "sol" 
                current = (current[0]+1, current[1]) #saga gidecek
            elif current in kenar_yukari_asagi:
                current_degisti = True
                gelis = "yukari"
                current = (current[0], current[1]+1) #asagiya gidecek
        # print(current)
        if current_degisti == False:
            print("stuck")
            return False
    if counter < patika_boyutu**2-siyah_kare_sayisi-1:
        print("boş kutu sayısından küçük")
        return False
    elif counter > patika_boyutu**2-siyah_kare_sayisi:
        print("boş kutu sayısından büyük")
        return False
    elif counter >= patika_boyutu**2-siyah_kare_sayisi-1: 
        print("boş kutu sayısı civarında")
        return True
                                
def koseleri_bul(canvas, blackList, genislik, patika_boyutu=6):

    kose_sol_asagi = []
    kose_sag_asagi = []
    kose_sol_yukari = []
    kose_sag_yukari = []
    kenar_yukari_asagi = []
    kenar_sag_sol = []

    sol_cizgi = []
    sag_cizgi = []
    yukari_cizgi = []
    asagi_cizgi = []

    degismeme_count = "degisti"    

    for tur in range(30):
        
        degismeme_count="degismedi"
        for row in range(patika_boyutu):
            for column in range(patika_boyutu):
                if (row,column) not in blackList and (row,column) not in kose_sag_asagi and (row,column) not in kose_sag_yukari and (row,column) not in kose_sol_asagi and (row,column) not in kose_sol_yukari and (row,column) not in kenar_yukari_asagi and (row,column) not in kenar_sag_sol:
                    sol = (row-1, column)
                    sag = (row+1, column)
                    yukari = (row, column-1)
                    asagi = (row, column+1)
                    
                    sol_dolu = False
                    sag_dolu = False
                    yukari_dolu = False
                    asagi_dolu = False                
                    
                    if sol in blackList or sol[0] < 0 or sol in kose_sol_yukari or sol in kose_sol_asagi or sol in kenar_yukari_asagi: sol_dolu = True
                    if sag in blackList or sag[0] > patika_boyutu-1 or sag in kose_sag_yukari or sag in kose_sag_asagi or sag in kenar_yukari_asagi: sag_dolu = True
                    if yukari in blackList or yukari[1] < 0 or yukari in kose_sag_yukari or yukari in kose_sol_yukari or yukari in kenar_sag_sol: yukari_dolu = True
                    if asagi in blackList or asagi[1] > patika_boyutu-1 or asagi in kose_sag_asagi or asagi in kose_sol_asagi or asagi in kenar_sag_sol: asagi_dolu = True
                    dolu_komsu_sayisi = sol_dolu + sag_dolu + yukari_dolu + asagi_dolu
                    # print(kose_sag_yukari,kose_sag_asagi,kose_sol_yukari,kose_sol_asagi,kenar_sag_sol,kenar_yukari_asagi)
                    # print((row,column),": ",dolu_komsu_sayisi, "sol:",sol_dolu, "sag:",sag_dolu, "yukari:",yukari_dolu, "asagi:",asagi_dolu)
                    if dolu_komsu_sayisi >= 3:
                        print("Wrong Question")
                        return "Wrong Question"
                    elif dolu_komsu_sayisi == 2:
                        # blackList.append((row,column))
                        degismeme_count = "degisti"
                        if not sol_dolu and not sag_dolu: #sol-sag kenar
                            kenar_sag_sol.append((row,column))
                            sag_cizgi.append(sol)
                            sol_cizgi.append(sag)
                            bas = orta_nokta(sol[0],sol[1], genislik)
                            son = orta_nokta(sag[0], sag[1], genislik)
                            canvas.create_line(bas[0], bas[1], son[0], son[1])
                        elif not yukari_dolu and not asagi_dolu: #yukari-asagi kenar
                            kenar_yukari_asagi.append((row,column))
                            yukari_cizgi.append(asagi)
                            asagi_cizgi.append(yukari)
                            bas = orta_nokta(yukari[0],yukari[1], genislik)
                            son = orta_nokta(asagi[0], asagi[1], genislik)
                            canvas.create_line(bas[0], bas[1], son[0], son[1])
                        elif not sol_dolu and not asagi_dolu: #sol ve asagi köşe
                            kose_sol_asagi.append((row,column))
                            sag_cizgi.append(sol)
                            yukari_cizgi.append(asagi)
                            bas = orta_nokta(sol[0], sol[1], genislik)
                            orta = orta_nokta(row, column, genislik)
                            son = orta_nokta(asagi[0], asagi[1], genislik)
                            canvas.create_line(bas[0], bas[1], orta[0], orta[1])
                            canvas.create_line(orta[0], orta[1], son[0],son[1])
                        elif not sag_dolu and not asagi_dolu: #sag ve asagi köşe
                            kose_sag_asagi.append((row,column))
                            sol_cizgi.append(sag)
                            yukari_cizgi.append(asagi)
                            bas = orta_nokta(sag[0], sag[1], genislik)
                            orta = orta_nokta(row, column, genislik)
                            son = orta_nokta(asagi[0], asagi[1], genislik)
                            canvas.create_line(bas[0], bas[1], orta[0], orta[1])
                            canvas.create_line(orta[0], orta[1], son[0],son[1])
                        elif not sol_dolu and not yukari_dolu: #sol ve yukarı köşe
                            kose_sol_yukari.append((row,column))
                            sag_cizgi.append(sol)
                            asagi_cizgi.append(yukari)
                            bas = orta_nokta(sol[0], sol[1], genislik)
                            orta = orta_nokta(row, column, genislik)
                            son = orta_nokta(yukari[0], yukari[1], genislik)
                            canvas.create_line(bas[0], bas[1], orta[0], orta[1])
                            canvas.create_line(orta[0], orta[1], son[0],son[1])
                        elif not sag_dolu and not yukari_dolu: #sağ ve yukarı köşe
                            kose_sag_yukari.append((row,column))
                            sol_cizgi.append(sag)
                            asagi_cizgi.append(yukari)
                            bas = orta_nokta(sag[0], sag[1], genislik)
                            orta = orta_nokta(row, column, genislik)
                            son = orta_nokta(yukari[0], yukari[1], genislik)
                            canvas.create_line(bas[0], bas[1], orta[0], orta[1])
                            canvas.create_line(orta[0], orta[1], son[0],son[1])

                    if (row, column) in sag_cizgi and (row,column) in sol_cizgi and (row,column)not in kenar_sag_sol:
                        degismeme_count = "degisti"
                        kenar_sag_sol.append((row, column))
                    elif (row, column) in yukari_cizgi and (row, column) in asagi_cizgi and (row,column) not in kenar_yukari_asagi:
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append((row,column))
                    elif (row, column) in sol_cizgi and (row, column) in asagi_cizgi and (row,column) not in kose_sol_asagi:
                        degismeme_count = "degisti"
                        kose_sol_asagi.append((row,column))
                    elif (row, column) in sol_cizgi and (row, column) in yukari_cizgi and (row,column) not in kose_sol_yukari:
                        degismeme_count = "degisti"
                        kose_sol_yukari.append((row,column))
                    elif (row, column) in sag_cizgi and (row, column) in asagi_cizgi and (row,column) not in kose_sag_asagi:
                        degismeme_count = "degisti"
                        kose_sag_asagi.append((row,column))
                    elif (row, column) in sag_cizgi and (row, column) in yukari_cizgi and (row,column) not in kose_sag_yukari:
                        degismeme_count = "degisti"
                        kose_sag_yukari.append((row,column))
                    
                    if sag in sag_cizgi and sag in sol_cizgi and sag not in kenar_sag_sol:
                        degismeme_count = "degisti"
                        kenar_sag_sol.append(sag)
                    elif sag in yukari_cizgi and sag in asagi_cizgi and sag not in kenar_yukari_asagi:
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append(sag)
                    elif sag in sol_cizgi and sag in asagi_cizgi and sag not in kose_sol_asagi:
                        degismeme_count = "degisti"
                        kose_sol_asagi.append(sag)
                    elif sag in sol_cizgi and sag in yukari_cizgi and sag not in kose_sol_yukari:
                        degismeme_count = "degisti"
                        kose_sol_yukari.append(sag)
                    elif sag in sag_cizgi and sag in asagi_cizgi and sag not in kose_sag_asagi:
                        degismeme_count = "degisti"
                        kose_sag_asagi.append(sag)
                    elif sag in sag_cizgi and sag in yukari_cizgi and sag not in kose_sag_yukari:
                        degismeme_count = "degisti"
                        kose_sag_yukari.append(sag)
        
                    if sol in sag_cizgi and sol in sol_cizgi and sol not in kenar_sag_sol:
                        degismeme_count = "degisti"
                        kenar_sag_sol.append(sol)
                    elif sol in yukari_cizgi and sol in asagi_cizgi and sol not in kenar_yukari_asagi:
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append(sol)
                    elif sol in sol_cizgi and sol in asagi_cizgi and sol not in kose_sol_asagi:
                        degismeme_count = "degisti"
                        kose_sol_asagi.append(sol)
                    elif sol in sol_cizgi and sol in yukari_cizgi and sol not in kose_sol_yukari:
                        degismeme_count = "degisti"
                        kose_sol_yukari.append(sol)
                    elif sol in sag_cizgi and sol in asagi_cizgi and sol not in kose_sag_asagi:
                        degismeme_count = "degisti"
                        kose_sag_asagi.append(sol)
                    elif sol in sag_cizgi and sol in yukari_cizgi and sol not in kose_sag_yukari:
                        degismeme_count = "degisti"
                        kose_sag_yukari.append(sol)
        
                    if yukari in sag_cizgi and yukari in sol_cizgi and yukari not in kenar_sag_sol:
                        degismeme_count = "degisti"
                        kenar_sag_sol.append(yukari)
                    elif yukari in yukari_cizgi and yukari in asagi_cizgi and yukari not in kenar_yukari_asagi:
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append(yukari)
                    elif yukari in sol_cizgi and yukari in asagi_cizgi and yukari not in kose_sol_asagi:
                        degismeme_count = "degisti"
                        kose_sol_asagi.append(yukari)
                    elif yukari in sol_cizgi and yukari in yukari_cizgi and yukari not in kose_sol_yukari:
                        degismeme_count = "degisti"
                        kose_sol_yukari.append(yukari)
                    elif yukari in sag_cizgi and yukari in asagi_cizgi and yukari not in kose_sag_asagi:
                        degismeme_count = "degisti"
                        kose_sag_asagi.append(yukari)
                    elif yukari in sag_cizgi and yukari in yukari_cizgi and yukari not in kose_sag_yukari:
                        degismeme_count = "degisti"
                        kose_sag_yukari.append(yukari)
                    
                    if asagi in sag_cizgi and asagi in sol_cizgi and asagi not in kenar_sag_sol:
                        degismeme_count = "degisti"
                        kenar_sag_sol.append(asagi)
                    elif asagi in yukari_cizgi and asagi in asagi_cizgi and asagi not in kenar_yukari_asagi:
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append(asagi)
                    elif asagi in sol_cizgi and asagi in asagi_cizgi and asagi not in kose_sol_asagi:
                        degismeme_count = "degisti"
                        kose_sol_asagi.append(asagi)
                    elif asagi in sol_cizgi and asagi in yukari_cizgi and asagi not in kose_sol_yukari:
                        degismeme_count = "degisti"
                        kose_sol_yukari.append(asagi)
                    elif asagi in sag_cizgi and asagi in asagi_cizgi and asagi not in kose_sag_asagi:
                        degismeme_count = "degisti"
                        kose_sag_asagi.append(asagi)
                    elif asagi in sag_cizgi and asagi in yukari_cizgi and asagi not in kose_sag_yukari:
                        degismeme_count = "degisti"
                        kose_sag_yukari.append(asagi)
                    
        if degismeme_count == "degismedi":
            print(f"{tur} tur gecti, bulunabilen koseler bulundu.")
            
            for row in range(patika_boyutu):
                for column in range(patika_boyutu):
                    # print(row,column)
                    if (row,column) not in blackList and (row,column) not in kose_sag_asagi and (row,column) not in kose_sag_yukari and (row,column) not in kose_sol_asagi and (row,column) not in kose_sol_yukari and (row,column) not in kenar_yukari_asagi and (row,column) not in kenar_sag_sol:
                        sol = (row-1, column)
                        sag = (row+1, column)
                        yukari = (row, column-1)
                        asagi = (row, column+1)
                        
                        sol_dolu = False
                        sag_dolu = False
                        yukari_dolu = False
                        asagi_dolu = False                
                        
                        if sol in blackList or sol[0] < 0 or sol in kose_sol_yukari or sol in kose_sol_asagi or sol in kenar_yukari_asagi: sol_dolu = True
                        if sag in blackList or sag[0] > patika_boyutu-1 or sag in kose_sag_yukari or sag in kose_sag_asagi or sag in kenar_yukari_asagi: sag_dolu = True
                        if yukari in blackList or yukari[1] < 0 or yukari in kose_sag_yukari or yukari in kose_sol_yukari or yukari in kenar_sag_sol: yukari_dolu = True
                        if asagi in blackList or asagi[1] > patika_boyutu-1 or asagi in kose_sag_asagi or asagi in kose_sol_asagi or asagi in kenar_sag_sol: asagi_dolu = True
                        
                        rc = (row,column)

                        if sol_dolu: #sol tarafı dolu
                            if rc in sag_cizgi: 
                                current = sag
                                gelis = "sol"
                            elif rc in yukari_cizgi:
                                current = yukari
                                gelis = "asagi"
                            elif rc in asagi_cizgi:
                                current = asagi
                                gelis = "yukari"
                        elif sag_dolu:
                            if rc in sol_cizgi: 
                                current = sol
                                gelis = "sag"
                            elif rc in yukari_cizgi:
                                current = yukari
                                gelis = "asagi"
                            elif rc in asagi_cizgi: 
                                current = asagi
                                gelis = "yukari"                      
                        elif yukari_dolu:
                            if rc in sol_cizgi: 
                                current = sol
                                gelis = "sag"
                            elif rc in sag_cizgi: 
                                current = sag
                                gelis = "sol"
                            elif rc in asagi_cizgi: 
                                current = asagi
                                gelis = "yukari"
                        elif asagi_dolu:
                            if rc in sol_cizgi: 
                                current = sol
                                gelis = "sag"
                            elif rc in sag_cizgi: 
                                current = sag
                                gelis = "sol"
                            elif rc in yukari_cizgi:
                                current = yukari
                                gelis = "asagi"
                                
                        # print(current)
                        current_degisti = True
                        while (current in kose_sol_asagi or current in kose_sol_yukari or current in kose_sag_asagi or current in kose_sag_yukari or current in kenar_sag_sol or current in kenar_yukari_asagi):
                            # time.sleep(0.2)
                            current_degisti = False
                            if gelis == "sag": #cizgi sağından gelmişse
                                if current in kose_sag_asagi:  
                                    current_degisti = True
                                    gelis = "yukari"
                                    current = (current[0], current[1]+1) #asagiya gidecek
                                elif current in kose_sag_yukari: 
                                    current_degisti = True
                                    gelis = "asagi"
                                    current = (current[0], current[1]-1) #yukariya gidecek
                                elif current in kenar_sag_sol:
                                    current_degisti = True
                                    gelis = "sag"
                                    current = (current[0]-1, current[1]) #sola gidecek
                            elif gelis == "sol": #cizgi solundan gelmişse

                                if current in kose_sol_asagi:  
                                    current_degisti = True
                                    gelis = "yukari"
                                    current = (current[0], current[1]+1) #asagiya gidecek
                                elif current in kose_sol_yukari: 
                                    current_degisti = True
                                    gelis = "asagi"
                                    current = (current[0], current[1]-1) #yukariya gidecek
                                elif current in kenar_sag_sol:
                                    current_degisti = True
                                    gelis = "sol" 
                                    current = (current[0]+1, current[1]) #saga gidecek
                            elif gelis == "asagi": #asagidan gelmişse

                                if current in kose_sol_asagi:  
                                    current_degisti = True
                                    gelis = "sag"
                                    current = (current[0]-1, current[1]) #sola gidecek
                                elif current in kose_sag_asagi: 
                                    current_degisti = True
                                    gelis = "sol" 
                                    current = (current[0]+1, current[1]) #saga gidecek
                                elif current in kenar_yukari_asagi:
                                    current_degisti = True
                                    gelis = "asagi"
                                    current = (current[0], current[1]-1) #yukariya gidecek
                            elif gelis == "yukari": #yukaridan gelmişse

                                if current in kose_sol_yukari:  
                                    current_degisti = True
                                    gelis = "sag"
                                    current = (current[0]-1, current[1]) #sola gidecek
                                elif current in kose_sag_yukari: 
                                    current_degisti = True
                                    gelis = "sol" 
                                    current = (current[0]+1, current[1]) #saga gidecek
                                elif current in kenar_yukari_asagi:
                                    current_degisti = True
                                    gelis = "yukari"
                                    current = (current[0], current[1]+1) #asagiya gidecek
                            # print(current)
                            if current_degisti == False:
                                print("stuck")
                                return "Wrong Question"
   
                        if sol_dolu:
                            if rc in sag_cizgi:
                                if current == yukari: #yukarısıyla birleşirse kapalı alan oluşturucak
                                    asagi_cizgi.append(rc) #şuanki bloğa asagi_cizgi eklendi
                                    yukari_cizgi.append(asagi) #altindaki bloğa yukari_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(asagi[0],asagi[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                                elif current == asagi: #asagisiyla birleşirse kapalı alan oluşturacak
                                    yukari_cizgi.append(rc) #şuanki bloğa yukari_cizgi eklendi
                                    asagi_cizgi.append(yukari) #üstündeki bloğa asagi_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(yukari[0],yukari[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                            elif rc in yukari_cizgi:
                                if current == sag: #sagdakiyle birleşirse kapalı alan oluşturucak
                                    asagi_cizgi.append(rc) #şuanki bloğa asagi_cizgi eklendi
                                    yukari_cizgi.append(asagi) #altindaki bloğa yukari_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(asagi[0],asagi[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                                if current == asagi: #asagisiyla birleşirse kapalı alan oluşturacak
                                    if sag[0] > patika_boyutu-1: continue 
                                    sag_cizgi.append(rc) #şuanki bloğa sag_cizgi eklendi
                                    sol_cizgi.append(sag) #sagindaki bloğa sol_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(sag[0],sag[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                            elif rc in asagi_cizgi:
                                if current == sag: #sagdakiyle birleşirse kapalı alan oluşturucak
                                    yukari_cizgi.append(rc) #şuanki bloğa yukari_cizgi eklendi
                                    asagi_cizgi.append(yukari) #altindaki bloğa asagi_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(yukari[0],yukari[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                                elif current == yukari: #yukarisiyla birleşirse kapalı alan oluşturacak
                                    sag_cizgi.append(rc) #şuanki bloğa sag_cizgi eklendi
                                    sol_cizgi.append(sag) #sagindaki bloğa sol_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(sag[0],sag[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                                else:
                                    print((row,column),"kapalı alan oluşturmuyor", current)
                        elif sag_dolu:
                            if rc in sol_cizgi:
                                if current == yukari: #yukarısıyla birleşirse kapalı alan oluşturucak
                                    asagi_cizgi.append(rc) #şuanki bloğa asagi_cizgi eklendi
                                    yukari_cizgi.append(asagi) #altindaki bloğa yukari_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(asagi[0],asagi[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                                elif current == asagi: #asagisiyla birleşirse kapalı alan oluşturacak
                                    yukari_cizgi.append(rc) #şuanki bloğa yukari_cizgi eklendi
                                    asagi_cizgi.append(yukari) #üstündeki bloğa asagi_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(yukari[0],yukari[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                            elif rc in yukari_cizgi:
                                if current == sol: #soldakiyle birleşirse kapalı alan oluşturucak
                                    asagi_cizgi.append(rc) #şuanki bloğa asagi_cizgi eklendi
                                    yukari_cizgi.append(asagi) #altindaki bloğa yukari_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(asagi[0],asagi[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                                elif current == asagi: #asagisiyla birleşirse kapalı alan oluşturacak
                                    sol_cizgi.append(rc) #şuanki bloğa sol_cizgi eklendi
                                    sag_cizgi.append(sol) #solundaki bloğa sag_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(sol[0],sol[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                            elif rc in asagi_cizgi:
                                if current == sol: #soldakiyle birleşirse kapalı alan oluşturucak
                                    yukari_cizgi.append(rc) #şuanki bloğa yukari_cizgi eklendi
                                    asagi_cizgi.append(yukari) #altindaki bloğa asagi_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(yukari[0],yukari[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break

                                elif current == yukari: #yukarisiyla birleşirse kapalı alan oluşturacak
                                    sol_cizgi.append(rc) #şuanki bloğa sol_cizgi eklendi
                                    sag_cizgi.append(sol) #solundaki bloğa sag_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(sol[0],sol[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                                else:
                                    print((row,column),"kapalı alan oluşturmuyor",current)
                        elif yukari_dolu:
                            if rc in sol_cizgi:
                                if current == sag: #sagla birleşirse kapalı alan oluşturucak
                                    asagi_cizgi.append(rc) #şuanki bloğa asagi_cizgi eklendi
                                    yukari_cizgi.append(asagi) #altindaki bloğa yukari_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(asagi[0],asagi[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                                elif current == asagi: #asagisiyla birleşirse kapalı alan oluşturacak
                                    sag_cizgi.append(rc) #şuanki bloğa sag_cizgi eklendi
                                    sol_cizgi.append(sag) #üstündeki bloğa sag_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(sag[0],sag[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                            elif rc in sag_cizgi:
                                if current == sol: #yukarısıyla birleşirse kapalı alan oluşturucak
                                    asagi_cizgi.append(rc) #şuanki bloğa asagi_cizgi eklendi
                                    yukari_cizgi.append(asagi) #altindaki bloğa yukari_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(asagi[0],asagi[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                                elif current == asagi: #asagisiyla birleşirse kapalı alan oluşturacak
                                    sol_cizgi.append(rc) #şuanki bloğa yukari_cizgi eklendi
                                    sag_cizgi.append(sol) #üstündeki bloğa asagi_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(sol[0],sol[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                            elif rc in asagi_cizgi:
                                if current == sol: #soldakiyle birleşirse kapalı alan oluşturucak
                                    sag_cizgi.append(rc) #şuanki bloğa sag_cizgi eklendi
                                    sol_cizgi.append(sag) #sagindaki bloğa sol_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(sag[0],sag[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break

                                elif current == sag: #sagiyla birleşirse kapalı alan oluşturacak
                                    sol_cizgi.append(rc) #şuanki bloğa sol_cizgi eklendi
                                    sag_cizgi.append(sol) #solundaki bloğa sag_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(sol[0],sol[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                                else:
                                    print((row,column),"kapalı alan oluşturmuyor",current)
                        elif asagi_dolu:
                            if rc in sol_cizgi:
                                if current == sag: #sagla birleşirse kapalı alan oluşturucak
                                    yukari_cizgi.append(rc) #şuanki bloğa yukari_cizgi eklendi
                                    asagi_cizgi.append(yukari) #üstündeki bloğa asagii_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(yukari[0],yukari[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                                elif current == yukari: #yukarısıyla birleşirse kapalı alan oluşturacak
                                    sag_cizgi.append(rc) #şuanki bloğa sag_cizgi eklendi
                                    sol_cizgi.append(sag) #üstündeki bloğa sag_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(sag[0],sag[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                            elif rc in sag_cizgi:
                                if current == sol: #yukarısıyla birleşirse kapalı alan oluşturucak
                                    yukari_cizgi.append(rc) #şuanki bloğa yukari_cizgi eklendi
                                    asagi_cizgi.append(yukari) #üstündeki bloğa asagi_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(yukari[0],yukari[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                                elif current == yukari: #yukarisiyla birleşirse kapalı alan oluşturacak
                                    sol_cizgi.append(rc) #şuanki bloğa yukari_cizgi eklendi
                                    sag_cizgi.append(sol) #üstündeki bloğa asagi_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(sol[0],sol[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                            elif rc in yukari_cizgi:
                                if current == sol: #soldakiyle birleşirse kapalı alan oluşturucak
                                    sag_cizgi.append(rc) #şuanki bloğa sag_cizgi eklendi
                                    sol_cizgi.append(sag) #sağındaki bloğa sol_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(sag[0],sag[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break
                                elif current == sag: #sağıyla birleşirse kapalı alan oluşturacak
                                    sol_cizgi.append(rc) #şuanki bloğa sol_cizgi eklendi
                                    sag_cizgi.append(sol) #solundaki bloğa sag_cizgi eklendi
                                    bas = orta_nokta(rc[0],rc[1],genislik)
                                    son = orta_nokta(sol[0],sol[1],genislik)
                                    canvas.create_line(bas[0],bas[1],son[0],son[1])
                                    degismeme_count = "degisti"
                                    break

                    if (row, column) in sag_cizgi and (row,column) in sol_cizgi and (row,column)not in kenar_sag_sol:
                        degismeme_count = "degisti"
                        kenar_sag_sol.append((row, column))
                    elif (row, column) in yukari_cizgi and (row, column) in asagi_cizgi and (row,column) not in kenar_yukari_asagi:
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append((row,column))
                    elif (row, column) in sol_cizgi and (row, column) in asagi_cizgi and (row,column) not in kose_sol_asagi:
                        degismeme_count = "degisti"
                        kose_sol_asagi.append((row,column))
                    elif (row, column) in sol_cizgi and (row, column) in yukari_cizgi and (row,column) not in kose_sol_yukari:
                        degismeme_count = "degisti"
                        kose_sol_yukari.append((row,column))
                    elif (row, column) in sag_cizgi and (row, column) in asagi_cizgi and (row,column) not in kose_sag_asagi:
                        degismeme_count = "degisti"
                        kose_sag_asagi.append((row,column))
                    elif (row, column) in sag_cizgi and (row, column) in yukari_cizgi and (row,column) not in kose_sag_yukari:
                        degismeme_count = "degisti"
                        kose_sag_yukari.append((row,column))
                    
                    if sag in sag_cizgi and sag in sol_cizgi and sag not in kenar_sag_sol:
                        degismeme_count = "degisti"
                        kenar_sag_sol.append(sag)
                    elif sag in yukari_cizgi and sag in asagi_cizgi and sag not in kenar_yukari_asagi:
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append(sag)
                    elif sag in sol_cizgi and sag in asagi_cizgi and sag not in kose_sol_asagi:
                        degismeme_count = "degisti"
                        kose_sol_asagi.append(sag)
                    elif sag in sol_cizgi and sag in yukari_cizgi and sag not in kose_sol_yukari:
                        degismeme_count = "degisti"
                        kose_sol_yukari.append(sag)
                    elif sag in sag_cizgi and sag in asagi_cizgi and sag not in kose_sag_asagi:
                        degismeme_count = "degisti"
                        kose_sag_asagi.append(sag)
                    elif sag in sag_cizgi and sag in yukari_cizgi and sag not in kose_sag_yukari:
                        degismeme_count = "degisti"
                        kose_sag_yukari.append(sag)
        
                    if sol in sag_cizgi and sol in sol_cizgi and sol not in kenar_sag_sol:
                        degismeme_count = "degisti"
                        kenar_sag_sol.append(sol)
                    elif sol in yukari_cizgi and sol in asagi_cizgi and sol not in kenar_yukari_asagi:
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append(sol)
                    elif sol in sol_cizgi and sol in asagi_cizgi and sol not in kose_sol_asagi:
                        degismeme_count = "degisti"
                        kose_sol_asagi.append(sol)
                    elif sol in sol_cizgi and sol in yukari_cizgi and sol not in kose_sol_yukari:
                        degismeme_count = "degisti"
                        kose_sol_yukari.append(sol)
                    elif sol in sag_cizgi and sol in asagi_cizgi and sol not in kose_sag_asagi:
                        degismeme_count = "degisti"
                        kose_sag_asagi.append(sol)
                    elif sol in sag_cizgi and sol in yukari_cizgi and sol not in kose_sag_yukari:
                        degismeme_count = "degisti"
                        kose_sag_yukari.append(sol)
        
                    if yukari in sag_cizgi and yukari in sol_cizgi and yukari not in kenar_sag_sol:
                        degismeme_count = "degisti"
                        kenar_sag_sol.append(yukari)
                    elif yukari in yukari_cizgi and yukari in asagi_cizgi and yukari not in kenar_yukari_asagi:
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append(yukari)
                    elif yukari in sol_cizgi and yukari in asagi_cizgi and yukari not in kose_sol_asagi:
                        degismeme_count = "degisti"
                        kose_sol_asagi.append(yukari)
                    elif yukari in sol_cizgi and yukari in yukari_cizgi and yukari not in kose_sol_yukari:
                        degismeme_count = "degisti"
                        kose_sol_yukari.append(yukari)
                    elif yukari in sag_cizgi and yukari in asagi_cizgi and yukari not in kose_sag_asagi:
                        degismeme_count = "degisti"
                        kose_sag_asagi.append(yukari)
                    elif yukari in sag_cizgi and yukari in yukari_cizgi and yukari not in kose_sag_yukari:
                        degismeme_count = "degisti"
                        kose_sag_yukari.append(yukari)
                    
                    if asagi in sag_cizgi and asagi in sol_cizgi and asagi not in kenar_sag_sol:
                        degismeme_count = "degisti"
                        kenar_sag_sol.append(asagi)
                    elif asagi in yukari_cizgi and asagi in asagi_cizgi and asagi not in kenar_yukari_asagi:
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append(asagi)
                    elif asagi in sol_cizgi and asagi in asagi_cizgi and asagi not in kose_sol_asagi:
                        degismeme_count = "degisti"
                        kose_sol_asagi.append(asagi)
                    elif asagi in sol_cizgi and asagi in yukari_cizgi and asagi not in kose_sol_yukari:
                        degismeme_count = "degisti"
                        kose_sol_yukari.append(asagi)
                    elif asagi in sag_cizgi and asagi in asagi_cizgi and asagi not in kose_sag_asagi:
                        degismeme_count = "degisti"
                        kose_sag_asagi.append(asagi)
                    elif asagi in sag_cizgi and asagi in yukari_cizgi and asagi not in kose_sag_yukari:
                        degismeme_count = "degisti"
                        kose_sag_yukari.append(asagi)
                    
            


    for row in range(patika_boyutu):
        for column in range(patika_boyutu):
            if (row, column) in sag_cizgi and (row,column) in sol_cizgi and (row,column)not in kenar_sag_sol:
            
                kenar_sag_sol.append((row, column))
            elif (row, column) in yukari_cizgi and (row, column) in asagi_cizgi and (row,column) not in kenar_yukari_asagi:
                
                kenar_yukari_asagi.append((row,column))
            elif (row, column) in sol_cizgi and (row, column) in asagi_cizgi and (row,column) not in kose_sol_asagi:
                
                kose_sol_asagi.append((row,column))
            elif (row, column) in sol_cizgi and (row, column) in yukari_cizgi and (row,column) not in kose_sol_yukari:
                
                kose_sol_yukari.append((row,column))
            elif (row, column) in sag_cizgi and (row, column) in asagi_cizgi and (row,column) not in kose_sag_asagi:
                
                kose_sag_asagi.append((row,column))
            elif (row, column) in sag_cizgi and (row, column) in yukari_cizgi and (row,column) not in kose_sag_yukari:
                
                kose_sag_yukari.append((row,column))
                        
       
    return kose_sag_asagi, kose_sag_yukari, kose_sol_asagi, kose_sol_yukari, kenar_sag_sol, kenar_yukari_asagi   
        


def solver(canvas, blackList, genislik, patika_boyutu=6, siyah_kare_sayisi=0):
    
    sonuc = koseleri_bul(canvas, blackList, genislik, patika_boyutu)
    if sonuc == "Wrong Question":
        is_solvable = False
    else: 
        kose_sag_asagi, kose_sag_yukari, kose_sol_asagi, kose_sol_yukari, kenar_sag_sol, kenar_yukari_asagi = sonuc
        tum_kenar_koseler = blackList + kose_sag_asagi + kose_sag_yukari + kose_sol_asagi + kose_sol_yukari + kenar_sag_sol + kenar_yukari_asagi
    
        if any([tum_kenar_koseler.count((r,c)) > 1 for r in range(patika_boyutu) for c in range(patika_boyutu)]):
            is_solvable = False
        else: 
            is_solvable = all([(r,c) in tum_kenar_koseler for r in range(patika_boyutu) for c in range(patika_boyutu)]) 

        if is_solvable:
            is_solvable = tracking_check(kose_sag_asagi, kose_sag_yukari, kose_sol_asagi, kose_sol_yukari, kenar_sag_sol, kenar_yukari_asagi, patika_boyutu, siyah_kare_sayisi)

    # print("Tüm kenar ve köşeler: ",sorted(tum_kenar_koseler))
    print("Soru çözülebilir: ", is_solvable)

    return is_solvable

def main(c,genislik,patika_boyutu=6, siyah_kare_sayisi=0):
    if siyah_kare_sayisi == 0: siyah_kare_sayisi = patika_boyutu
    # blackList = create_grid(c,genislik, patika_boyutu, siyah_kare_sayisi)
    # solver(c, blackList, genislik,patika_boyutu, siyah_kare_sayisi)
    start = timeit.default_timer()
    for _ in range(100000):
        blackList = create_grid(c,genislik, patika_boyutu, siyah_kare_sayisi)
        is_solvable = solver(c, blackList, genislik, patika_boyutu, siyah_kare_sayisi)
        if is_solvable:
            print("BlackList: ",blackList,"is solvable")
            break
        else:
            c.delete("all")
    end = timeit.default_timer()
    print(f"It took {end-start} seconds.")
    return blackList

root = Tk()
c = Canvas(root)
root.title("Patika")

genislik = 40

patika_boyutu = int(input("Patika boyutu:"))
siyah_kare_sayisi = int(input("Siyah kare sayısı:"))
if patika_boyutu <= 2:
    raise ValueError("Patika boyutu en az 3 olabilir.")
if siyah_kare_sayisi >= patika_boyutu**2//4:
    raise ValueError("Girdiğiniz siyah kare sayısı, patika boyutuna göre fazladır.")
main(c,genislik,patika_boyutu=patika_boyutu, siyah_kare_sayisi=siyah_kare_sayisi)
# adet = 3
# tum_dogru_blackListler =[]
# for patika_boyutu in range(4,7):
#     boyut_dogru_blackList = []
#     for tur in range(1,adet+1):
#         if patika_boyutu <= 6: siyah_kare_sayisi = patika_boyutu-2
#         else: siyah_kare_sayisi = patika_boyutu
#         dogru_blackList = main(c, genislik, patika_boyutu=patika_boyutu, siyah_kare_sayisi=siyah_kare_sayisi)
#         boyut_dogru_blackList.append(dogru_blackList)
#     tum_dogru_blackListler.append(tuple(boyut_dogru_blackList))
# tum_dogru_blackListler = np.array(tum_dogru_blackListler)
# tum_dogru_blackListler = tum_dogru_blackListler.T
# # print(tum_dogru_blackListler)
# df = pd.DataFrame(tum_dogru_blackListler,columns=["4x4","5x5","6x6"])
# print(df)


c.pack(fill=BOTH, expand=1)
root.geometry("400x400+300+300")
root.mainloop()


"""
Patika Boyutu / En İyi Siyah Kare Sayıları

3 => 1
4 => 2
5 => 3
6 => 4 ve 6
7 => 7
8 => 8 en iyi gibi, 10 ve 12 de de buldu.
9 => 9 da bulması 15 dk sürdü

"""