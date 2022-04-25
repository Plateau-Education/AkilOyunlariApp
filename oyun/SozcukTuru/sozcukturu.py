import random
import re
from itertools import combinations

iter = 0

class SozcukTuru:
    def __init__(self):
        self.zorluk = "Easy"
        self.harf_list = []
        self.try_lets = None
        self.try_list = []
        self.tried = []
        self.kelimeler = []
        self.grid = []
        self.cozum = 0

    def create_grid(self):
        if self.zorluk == "Easy":
            self.boyut = (3, 4)
        elif self.zorluk == "Medium":
            self.boyut = (3, 5)
        elif self.zorluk == "Hard":
            self.boyut = (4, 5)
        elif self.zorluk == "Hardest":
            self.boyut = (5, 5)
        else:
            raise ValueError("zorluk sadece Easy, Medium, Hard veya Hardest olabilir")

    def kelime_secici(self):
        if self.zorluk == "Easy":
            self.harf_range = (3, 5)
        elif self.zorluk == "Medium":
            self.harf_range = (4, 6)
        elif self.zorluk == "Hard":
            self.harf_range = (2, 6)
        elif self.zorluk == "Hardest":
            self.harf_range = (3, 7)

        kelimeler = []
        for harf_sayisi in range(self.harf_range[0], self.harf_range[1] + 1):
            f = open(
                f"C:/Users/efdal/OneDrive/Masaüstü/Akil_Oyunlari/oyun/kelimeler/{harf_sayisi}.txt",
                encoding="utf-8",
            )
            lst = f.read().split()
            kelime = random.choice(lst)
            kelimeler.append(kelime)
            for i in kelime:
                self.harf_list.append(i)
            self.kelimeler.append(kelime)
            f.close()
        return kelimeler

    def kelime_dagitici(self, kelimeler):
        dolu_kareler = []
        ilk_index = (
            random.randint(0, self.boyut[0] - 1),
            random.randint(0, self.boyut[1] - 1),
        )
        dolu_kareler.append(ilk_index)
        suanki = ilk_index
        harf_koordinat_list = [[suanki[0], suanki[1], ord(kelimeler[0][0])]]
        for h in range(self.harf_range[0], self.harf_range[1] + 1):
            if h == self.harf_range[0]:
                h -= 1
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
                            or komsu[0] >= self.boyut[0]
                            or komsu[1] < 0
                            or komsu[1] >= self.boyut[1]
                        ):
                            komsular.remove(komsu)
                if komsular == []:
                    return []
                suanki = random.choice(komsular)
                dolu_kareler.append(suanki)
                if h == self.harf_range[0] - 1:
                    harf_koordinat_list.append(
                        [suanki[0], suanki[1], ord(kelimeler[0][i + 1])]
                    )
                else:
                    harf_koordinat_list.append(
                        [suanki[0], suanki[1], ord(kelimeler[h - self.harf_range[0]][i])]
                    )
        return harf_koordinat_list

    def printProgress(self):
        grid = [[0 for _ in range(self.boyut[1])] for _ in range(self.boyut[0])]
        for i in self.grid:
            grid[i[0]][i[1]] = chr(i[2])
        for row in grid:
            print(row)

    def getFiltered(self, words, com):
        re_ = f"\n([{''.join(com)}]" + "{" + str(len(com)) + "})\n"
        strAr = sorted(com)
        filter_ = list(filter(lambda x: "".join(sorted(x)) == "".join(sorted(strAr)), re.findall(re_, words)))
        return filter_

    def cozucu(self, suanki=None, harf_list=None, grid=None):
        if harf_list == []:
            self.cozum += 1
            return
        if not harf_list:
            suanki = (self.grid[0][0] - 1, self.grid[0][1])
            harf_list = [ord(i) for i in self.harf_list]
            grid = {(i, j): 1 for i in range(self.boyut[0]) for j in range(self.boyut[1])}
        n = harf_list[0]
        harf_list2 = harf_list.copy()
        harf_list2.remove(n)
        possible_list = filter(lambda x: x[2] == n, self.grid)
        komsular = [(suanki[0] + 1, suanki[1], n), (suanki[0] - 1, suanki[1], n),
                    (suanki[0], suanki[1] + 1, n), (suanki[0], suanki[1] - 1, n),
                    (suanki[0] + 1, suanki[1] + 1, n), (suanki[0] + 1, suanki[1] - 1, n),
                    (suanki[0] - 1, suanki[1] + 1, n), (suanki[0] - 1, suanki[1] - 1, n)]
        for i in possible_list:
            if tuple(i) in komsular:
                if grid[(i[0], i[1])]:
                    grid[(i[0], i[1])] = 0
                    self.cozucu(i, harf_list2, grid)
                    grid[(i[0], i[1])] = 1
        return

    def kelime_kontrol(self, lap=0, harflist=None, kelime_list=None):
        if lap > self.harf_range[1]:
            self.cozucu()
            return
        if not harflist:
            harflist = self.harf_list.copy()
            kelime_list = []
            lap = self.harf_range[0]
        f = open(f"C:/Users/efdal/OneDrive/Masaüstü/Akil_Oyunlari/oyun/kelimeler/{lap}.txt", encoding="utf-8").read()
        for com in combinations(harflist, lap):
            if com in self.tried:
                continue
            self.tried.append(com)
            kelimeler = self.getFiltered(words=f, com=com)
            for kelime in kelimeler:
                harflist_next = harflist.copy()
                for harf in kelime:
                    harflist_next.remove(harf)
                kelimeler_next = kelime_list
                kelimeler_next.append(kelime)
                self.kelime_kontrol(lap + 1, harflist_next, kelimeler_next)
                kelimeler_next.remove(kelime)
        return

    def class_main(self):
        self.create_grid()
        kelimeler = self.kelime_secici()
        for _ in range(1000):
            grid = self.kelime_dagitici(kelimeler)
            if grid:
                self.grid = grid
                self.kelime_kontrol()
                global iter
                iter += 1
                if self.cozum == 1:
                    self.create_grid()
                    # for kelime in self.kelimeler:
                        # print(kelime)
                    return grid
        return False


def main(level, count):
    datacontrol = set()
    data = []
    database = []
    for i in range(count):
        for _ in range(10000):
            obj = SozcukTuru()
            obj.zorluk = level
            a = obj.class_main()
            if a:
                data.append(a)
                datacontrol.add(str(a))
                # obj.printProgress()
                break
    for i in data:
        if str(i) in datacontrol:
            datacontrol.discard(str(i))
            # print(i)
            # print()
            database.append(i)
    return database


sorular = main("Easy", 1000)
print(len(sorular), iter)
iter = 0
sorular = main("Medium", 100)
print(len(sorular), iter)
iter = 0
sorular = main("Hard", 20)
print(len(sorular), iter)


# deneme = SozcukTuru()
# deneme.boyut = (3, 4)
# deneme.harf_range = (3, 5)
# for _ in range(1000):
#     grid = deneme.kelime_dagitici(["aşı", "skeç", "damak"])
#     if grid:
#         deneme.harf_list = []
#         deneme.kelimeler = ["aşı", "skeç", "damak"]
#         for kelim in deneme.kelimeler:
#             for harf in kelim:
#                 deneme.harf_list.append(harf)
#         deneme.grid = grid
#         deneme.kelime_kontrol()
#         print(deneme.cozum)
#         for kelime in deneme.kelimeler:
#             print(kelime)
# for _ in range(3):
#     for _ in range(5):
#         print("0 ", end="")
#     print()
# start1 = timeit.default_timer()
# for _ in range(10):
# start = timeit.default_timer()
# for i in main("Easy", 10):
#     print(i)
# end = timeit.default_timer()
# print(end - start)
# end1 = timeit.default_timer()
# print(end1 - start1)
# main("Easy", 20)