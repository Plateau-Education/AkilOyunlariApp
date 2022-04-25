import random


class SozcukTuru:
    def __init__(self):
        self.zorluk = "Easy"

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
        elif self.zorluk == "VeryHard":
            self.harf_range = (3, 7)

        kelimeler = []
        for harf_sayisi in range(self.harf_range[0], self.harf_range[1] + 1):
            f = open(
                f"C:\\Users\\ezrea\\Documents\\GitHub\\AkilOyunlariApp\\oyun\\SozcukTuru\\{harf_sayisi}.txt",
                encoding="utf-8",
            )
            lst = f.read()
            lst = lst.split()
            kelime = random.choice(lst)
            kelimeler.append(kelime)
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
                if komsular == []:
                    return []
                suanki = random.choice(komsular)
                dolu_kareler.append(suanki)
                if h == self.harf_range[0] - 1:
                    harf_koordinat_list.append(
                        (suanki[0], suanki[1], kelimeler[0][i + 1])
                    )
                else:
                    harf_koordinat_list.append(
                        (suanki[0], suanki[1], kelimeler[h - self.harf_range[0]][i])
                    )
        return harf_koordinat_list

    def class_main(self):
        self.create_grid()
        kelimeler = self.kelime_secici()
        for _ in range(1000):
            grid = self.kelime_dagitici(kelimeler)
            if grid:
                self.create_grid()
                break
        return grid


def main(level, count):
    datacontrol = set()
    data = []
    database = []
    for i in range(count):
        obj = SozcukTuru()
        obj.zorluk = level
        a = obj.class_main()
        data.append(a)
        datacontrol.add(str(a))
    for i in data:
        if str(i) in datacontrol:
            datacontrol.discard(str(i))
            database.append(i)
    return database

count = 5
level = "Easy"
for i in range(count):
    obj = SozcukTuru()
    obj.zorluk = level
    a = obj.class_main()
    print(a)
