import random, time, timeit
from tkinter import Tk, Canvas, BOTH
import tkinter.font as tkFont


class SozcukTuru:
    def __init__(self):
        self.zorluk = "kolay"

    def create_grid(self, canvas, genislik):
        if self.zorluk == "kolay":
            self.boyut = (3, 4)
        elif self.zorluk == "orta":
            self.boyut = (3, 5)
        elif self.zorluk == "zor":
            self.boyut = (4, 5)
        elif self.zorluk == "en zor":
            self.boyut = (5, 5)
        else:
            raise ValueError("zorluk sadece kolay, orta, zor veya en zor olabilir")
        for row in range(self.boyut[0]):
            for column in range(self.boyut[1]):
                canvas.create_rectangle(
                    row * genislik + 10,
                    column * genislik + 10,
                    (row + 1) * genislik + 10,
                    (column + 1) * genislik + 10,
                )

    def kelime_secici(self):
        if self.zorluk == "kolay":
            self.harf_range = (3, 5)
        elif self.zorluk == "orta":
            self.harf_range = (4, 6)
        elif self.zorluk == "zor":
            self.harf_range = (2, 6)
        elif self.zorluk == "en zor":
            self.harf_range = (3, 7)

        kelimeler = []
        for harf_sayisi in range(self.harf_range[0], self.harf_range[1] + 1):
            f = open(
                f"C:\\Users\\ezrea\\Documents\\GitHub\\AkilOyunlariApp\\Kelime Oyunları\\{harf_sayisi}.txt",
                encoding="utf-8",
            )
            lst = f.read()
            lst = lst.split()
            kelime = random.choice(lst)
            kelimeler.append(kelime)
            f.close()
        print(kelimeler)
        return kelimeler

    def orta_nokta(self, row, column, genislik):
        ortanokta_x = (row + 1 / 2) * genislik + 10
        ortanokta_y = (column + 1 / 2) * genislik + 10
        return (ortanokta_x, ortanokta_y)

    def kelime_dagitici(self, canvas, kelimeler):
        dolu_kareler = []
        ilk_index = (
            random.randint(0, self.boyut[0] - 1),
            random.randint(0, self.boyut[1] - 1),
        )
        dolu_kareler.append(ilk_index)
        suanki = ilk_index
        canvas.create_text(
            self.orta_nokta(suanki[0], suanki[1], 40),
            text=kelimeler[0][0],
            font=tkFont.Font(family="Poppins", size=20),
        )
        harf_koordinat_list = [(suanki[0], suanki[1], kelimeler[0][0])]
        for h in range(self.harf_range[0], self.harf_range[1] + 1):
            if h == self.harf_range[0]:
                h = self.harf_range[0] - 1
            for i in range(h):
                komsular = [
                    (suanki[0] + 1, suanki[1]),
                    (suanki[0] - 1, suanki[1]),
                    (suanki[0], suanki[1] + 1),
                    (suanki[0], suanki[1] - 1),
                    (suanki[0] + 1, suanki[1] + 1),
                    (suanki[0] + 1, suanki[1] - 1),
                    (suanki[0] - 1, suanki[1] + 1),
                    (suanki[0] - 1, suanki[1] - 1),
                ]
                for _ in range(5):
                    for komsu in komsular:
                        if (
                            komsu in dolu_kareler
                            or komsu[0] < 0
                            or komsu[0] > self.boyut[0] - 1
                            or komsu[1] < 0
                            or komsu[1] > self.boyut[1] - 1
                        ):
                            komsular.remove(komsu)
                # print(komsular)
                if komsular == []:
                    return []
                suanki = random.choice(komsular)
                dolu_kareler.append(suanki)
                if h == self.harf_range[0] - 1:
                    canvas.create_text(
                        self.orta_nokta(suanki[0], suanki[1], 40),
                        text=kelimeler[0][i + 1],
                        font=tkFont.Font(family="Poppins", size=20),
                    )
                    harf_koordinat_list.append(
                        (suanki[0], suanki[1], kelimeler[0][i + 1])
                    )
                else:
                    canvas.create_text(
                        self.orta_nokta(suanki[0], suanki[1], 40),
                        text=kelimeler[h - self.harf_range[0]][i],
                        font=tkFont.Font(family="Poppins", size=20),
                    )
                    harf_koordinat_list.append(
                        (suanki[0], suanki[1], kelimeler[h - self.harf_range[0]][i])
                    )
        return harf_koordinat_list
        # is_solvable = all([(r,c) in dolu_kareler for r in range(self.boyut[0]) for c in range(self.boyut[1])])
        # print(is_solvable)

    def yol_cizici(self, canvas, grid):
        ilk_coor = grid[0][0], grid[0][1]
        for kutu in grid:
            son_coor = kutu[0], kutu[1]
            canvas.create_line(
                self.orta_nokta(ilk_coor[0], ilk_coor[1], 40)[0],
                self.orta_nokta(ilk_coor[0], ilk_coor[1], 40)[1],
                self.orta_nokta(son_coor[0], son_coor[1], 40)[0],
                self.orta_nokta(son_coor[0], son_coor[1], 40)[1],
                width=1,
            )
            ilk_coor = son_coor

    def main(self):
        root = Tk()
        canvas = Canvas(root)

        start = timeit.default_timer()
        self.create_grid(canvas, 40)
        kelimeler = self.kelime_secici()
        for _ in range(1000):
            grid = self.kelime_dagitici(canvas, kelimeler)
            if grid == []:
                canvas.delete("all")
            else:
                self.create_grid(canvas, 40)
                break
        end = timeit.default_timer()
        print(f"It took {end-start} seconds.")
        self.yol_cizici(canvas, grid)
        canvas.pack(fill=BOTH, expand=1)
        root.geometry("400x250+300+300")
        root.mainloop()

        return grid


obj = SozcukTuru()
obj.zorluk = "zor"
print(obj.main())
