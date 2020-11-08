import random, time, timeit
import pandas as pd
import numpy as np


def blacklist(patika_boyutu=6, siyah_kare_sayisi=0):
    if siyah_kare_sayisi == 0:
        siyah_kare_sayisi = patika_boyutu
    blackList = set()
    while True:
        rows = [random.randint(0, patika_boyutu - 1) for i in range(siyah_kare_sayisi)]
        columns = [
            random.randint(0, patika_boyutu - 1) for i in range(siyah_kare_sayisi)
        ]
        zipped = zip(rows, columns)
        for coor in zipped:
            if coor not in [
                (0, 1),
                (0, patika_boyutu - 2),
                (1, 0),
                (1, patika_boyutu - 1),
                (patika_boyutu - 2, 0),
                (patika_boyutu - 1, patika_boyutu - 2),
                (patika_boyutu - 1, 1),
                (patika_boyutu - 1, patika_boyutu - 2),
            ]:
                blackList.add(coor)
        if len(blackList) >= siyah_kare_sayisi:
            blackList = list(blackList)[:siyah_kare_sayisi]
            break
    return blackList


def orta_nokta(row, column, genislik):
    ortanokta_x = (row + 1 / 2) * genislik + 10
    ortanokta_y = (column + 1 / 2) * genislik + 10
    return ortanokta_x, ortanokta_y


def create_grid(c, genislik, patika_boyutu=6, siyah_kare_sayisi=0):
    blackList = blacklist(patika_boyutu, siyah_kare_sayisi)
    return blackList


def tracking_check(
        kose_sag_asagi,
        kose_sag_yukari,
        kose_sol_asagi,
        kose_sol_yukari,
        kenar_sag_sol,
        kenar_yukari_asagi,
        patika_boyutu=6,
        siyah_kare_sayisi=0,
):
    current = (0, 1)
    gelis = "yukari"
    # print(current)
    current_degisti = True
    counter = 0
    while (current != (0, 0)) and (
            current in kose_sol_asagi
            or current in kose_sol_yukari
            or current in kose_sag_asagi
            or current in kose_sag_yukari
            or current in kenar_sag_sol
            or current in kenar_yukari_asagi
    ):
        counter += 1
        if counter > patika_boyutu ** 2 - siyah_kare_sayisi:
            return False
        current_degisti = False
        if gelis == "sag":
            if current in kose_sag_asagi:
                current_degisti = True
                gelis = "yukari"
                current = (current[0], current[1] + 1)
            elif current in kose_sag_yukari:
                current_degisti = True
                gelis = "asagi"
                current = (current[0], current[1] - 1)
            elif current in kenar_sag_sol:
                current_degisti = True
                gelis = "sag"
                current = (current[0] - 1, current[1])
        elif gelis == "sol":
            if current in kose_sol_asagi:
                current_degisti = True
                gelis = "yukari"
                current = (current[0], current[1] + 1)
            elif current in kose_sol_yukari:
                current_degisti = True
                gelis = "asagi"
                current = (current[0], current[1] - 1)
            elif current in kenar_sag_sol:
                current_degisti = True
                gelis = "sol"
                current = (current[0] + 1, current[1])
        elif gelis == "asagi":
            if current in kose_sol_asagi:
                current_degisti = True
                gelis = "sag"
                current = (current[0] - 1, current[1])
            elif current in kose_sag_asagi:
                current_degisti = True
                gelis = "sol"
                current = (current[0] + 1, current[1])
            elif current in kenar_yukari_asagi:
                current_degisti = True
                gelis = "asagi"
                current = (current[0], current[1] - 1)
        elif gelis == "yukari":
            if current in kose_sol_yukari:
                current_degisti = True
                gelis = "sag"
                current = (current[0] - 1, current[1])
            elif current in kose_sag_yukari:
                current_degisti = True
                gelis = "sol"
                current = (current[0] + 1, current[1])
            elif current in kenar_yukari_asagi:
                current_degisti = True
                gelis = "yukari"
                current = (current[0], current[1] + 1)
        if current_degisti == False:
            return False
    if counter < patika_boyutu ** 2 - siyah_kare_sayisi - 1:
        return False
    elif counter > patika_boyutu ** 2 - siyah_kare_sayisi:
        return False
    elif counter >= patika_boyutu ** 2 - siyah_kare_sayisi - 1:
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
    for tur in range(patika_boyutu ** 2 + 1):
        degismeme_count = "degismedi"
        for row in range(patika_boyutu):
            for column in range(patika_boyutu):
                if (
                        (row, column) not in blackList
                        and (row, column) not in kose_sag_asagi
                        and (row, column) not in kose_sag_yukari
                        and (row, column) not in kose_sol_asagi
                        and (row, column) not in kose_sol_yukari
                        and (row, column) not in kenar_yukari_asagi
                        and (row, column) not in kenar_sag_sol
                ):
                    sol = (row - 1, column)
                    sag = (row + 1, column)
                    yukari = (row, column - 1)
                    asagi = (row, column + 1)

                    sol_dolu = False
                    sag_dolu = False
                    yukari_dolu = False
                    asagi_dolu = False

                    if (
                            sol in blackList
                            or sol[0] < 0
                            or sol in kose_sol_yukari
                            or sol in kose_sol_asagi
                            or sol in kenar_yukari_asagi
                    ):
                        sol_dolu = True
                    if (
                            sag in blackList
                            or sag[0] > patika_boyutu - 1
                            or sag in kose_sag_yukari
                            or sag in kose_sag_asagi
                            or sag in kenar_yukari_asagi
                    ):
                        sag_dolu = True
                    if (
                            yukari in blackList
                            or yukari[1] < 0
                            or yukari in kose_sag_yukari
                            or yukari in kose_sol_yukari
                            or yukari in kenar_sag_sol
                    ):
                        yukari_dolu = True
                    if (
                            asagi in blackList
                            or asagi[1] > patika_boyutu - 1
                            or asagi in kose_sag_asagi
                            or asagi in kose_sol_asagi
                            or asagi in kenar_sag_sol
                    ):
                        asagi_dolu = True
                    dolu_komsu_sayisi = sol_dolu + sag_dolu + yukari_dolu + asagi_dolu
                    if dolu_komsu_sayisi >= 3:
                        return "Wrong Question"
                    elif dolu_komsu_sayisi == 2:
                        degismeme_count = "degisti"
                        if not sol_dolu and not sag_dolu:
                            kenar_sag_sol.append((row, column))
                            sag_cizgi.append(sol)
                            sol_cizgi.append(sag)
                            bas = orta_nokta(sol[0], sol[1], genislik)
                            son = orta_nokta(sag[0], sag[1], genislik)
                        elif not yukari_dolu and not asagi_dolu:
                            kenar_yukari_asagi.append((row, column))
                            yukari_cizgi.append(asagi)
                            asagi_cizgi.append(yukari)
                            bas = orta_nokta(yukari[0], yukari[1], genislik)
                            son = orta_nokta(asagi[0], asagi[1], genislik)
                        elif not sol_dolu and not asagi_dolu:
                            kose_sol_asagi.append((row, column))
                            sag_cizgi.append(sol)
                            yukari_cizgi.append(asagi)
                            bas = orta_nokta(sol[0], sol[1], genislik)
                            orta = orta_nokta(row, column, genislik)
                            son = orta_nokta(asagi[0], asagi[1], genislik)
                        elif not sag_dolu and not asagi_dolu:  # sag ve asagi köşe
                            kose_sag_asagi.append((row, column))
                            sol_cizgi.append(sag)
                            yukari_cizgi.append(asagi)
                            bas = orta_nokta(sag[0], sag[1], genislik)
                            orta = orta_nokta(row, column, genislik)
                            son = orta_nokta(asagi[0], asagi[1], genislik)
                        elif not sol_dolu and not yukari_dolu:
                            kose_sol_yukari.append((row, column))
                            sag_cizgi.append(sol)
                            asagi_cizgi.append(yukari)
                            bas = orta_nokta(sol[0], sol[1], genislik)
                            orta = orta_nokta(row, column, genislik)
                            son = orta_nokta(yukari[0], yukari[1], genislik)
                        elif not sag_dolu and not yukari_dolu:
                            kose_sag_yukari.append((row, column))
                            sol_cizgi.append(sag)
                            asagi_cizgi.append(yukari)
                            bas = orta_nokta(sag[0], sag[1], genislik)
                            orta = orta_nokta(row, column, genislik)
                            son = orta_nokta(yukari[0], yukari[1], genislik)
                    if (
                            (row, column) in sag_cizgi
                            and (row, column) in sol_cizgi
                            and (row, column) not in kenar_sag_sol
                    ):
                        degismeme_count = "degisti"
                        kenar_sag_sol.append((row, column))
                    elif (
                            (row, column) in yukari_cizgi
                            and (row, column) in asagi_cizgi
                            and (row, column) not in kenar_yukari_asagi
                    ):
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append((row, column))
                    elif (
                            (row, column) in sol_cizgi
                            and (row, column) in asagi_cizgi
                            and (row, column) not in kose_sol_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sol_asagi.append((row, column))
                    elif (
                            (row, column) in sol_cizgi
                            and (row, column) in yukari_cizgi
                            and (row, column) not in kose_sol_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sol_yukari.append((row, column))
                    elif (
                            (row, column) in sag_cizgi
                            and (row, column) in asagi_cizgi
                            and (row, column) not in kose_sag_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sag_asagi.append((row, column))
                    elif (
                            (row, column) in sag_cizgi
                            and (row, column) in yukari_cizgi
                            and (row, column) not in kose_sag_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sag_yukari.append((row, column))
                    if (
                            sag in sag_cizgi
                            and sag in sol_cizgi
                            and sag not in kenar_sag_sol
                    ):
                        degismeme_count = "degisti"
                        kenar_sag_sol.append(sag)
                    elif (
                            sag in yukari_cizgi
                            and sag in asagi_cizgi
                            and sag not in kenar_yukari_asagi
                    ):
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append(sag)
                    elif (
                            sag in sol_cizgi
                            and sag in asagi_cizgi
                            and sag not in kose_sol_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sol_asagi.append(sag)
                    elif (
                            sag in sol_cizgi
                            and sag in yukari_cizgi
                            and sag not in kose_sol_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sol_yukari.append(sag)
                    elif (
                            sag in sag_cizgi
                            and sag in asagi_cizgi
                            and sag not in kose_sag_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sag_asagi.append(sag)
                    elif (
                            sag in sag_cizgi
                            and sag in yukari_cizgi
                            and sag not in kose_sag_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sag_yukari.append(sag)
                    if (
                            sol in sag_cizgi
                            and sol in sol_cizgi
                            and sol not in kenar_sag_sol
                    ):
                        degismeme_count = "degisti"
                        kenar_sag_sol.append(sol)
                    elif (
                            sol in yukari_cizgi
                            and sol in asagi_cizgi
                            and sol not in kenar_yukari_asagi
                    ):
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append(sol)
                    elif (
                            sol in sol_cizgi
                            and sol in asagi_cizgi
                            and sol not in kose_sol_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sol_asagi.append(sol)
                    elif (
                            sol in sol_cizgi
                            and sol in yukari_cizgi
                            and sol not in kose_sol_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sol_yukari.append(sol)
                    elif (
                            sol in sag_cizgi
                            and sol in asagi_cizgi
                            and sol not in kose_sag_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sag_asagi.append(sol)
                    elif (
                            sol in sag_cizgi
                            and sol in yukari_cizgi
                            and sol not in kose_sag_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sag_yukari.append(sol)
                    if (
                            yukari in sag_cizgi
                            and yukari in sol_cizgi
                            and yukari not in kenar_sag_sol
                    ):
                        degismeme_count = "degisti"
                        kenar_sag_sol.append(yukari)
                    elif (
                            yukari in yukari_cizgi
                            and yukari in asagi_cizgi
                            and yukari not in kenar_yukari_asagi
                    ):
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append(yukari)
                    elif (
                            yukari in sol_cizgi
                            and yukari in asagi_cizgi
                            and yukari not in kose_sol_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sol_asagi.append(yukari)
                    elif (
                            yukari in sol_cizgi
                            and yukari in yukari_cizgi
                            and yukari not in kose_sol_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sol_yukari.append(yukari)
                    elif (
                            yukari in sag_cizgi
                            and yukari in asagi_cizgi
                            and yukari not in kose_sag_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sag_asagi.append(yukari)
                    elif (
                            yukari in sag_cizgi
                            and yukari in yukari_cizgi
                            and yukari not in kose_sag_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sag_yukari.append(yukari)
                    if (
                            asagi in sag_cizgi
                            and asagi in sol_cizgi
                            and asagi not in kenar_sag_sol
                    ):
                        degismeme_count = "degisti"
                        kenar_sag_sol.append(asagi)
                    elif (
                            asagi in yukari_cizgi
                            and asagi in asagi_cizgi
                            and asagi not in kenar_yukari_asagi
                    ):
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append(asagi)
                    elif (
                            asagi in sol_cizgi
                            and asagi in asagi_cizgi
                            and asagi not in kose_sol_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sol_asagi.append(asagi)
                    elif (
                            asagi in sol_cizgi
                            and asagi in yukari_cizgi
                            and asagi not in kose_sol_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sol_yukari.append(asagi)
                    elif (
                            asagi in sag_cizgi
                            and asagi in asagi_cizgi
                            and asagi not in kose_sag_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sag_asagi.append(asagi)
                    elif (
                            asagi in sag_cizgi
                            and asagi in yukari_cizgi
                            and asagi not in kose_sag_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sag_yukari.append(asagi)
        if degismeme_count == "degismedi":
            # print(f"{tur} tur gecti, bulunabilen koseler bulundu.")

            for row in range(patika_boyutu):
                for column in range(patika_boyutu):
                    # print(row,column)
                    if (
                            (row, column) not in blackList
                            and (row, column) not in kose_sag_asagi
                            and (row, column) not in kose_sag_yukari
                            and (row, column) not in kose_sol_asagi
                            and (row, column) not in kose_sol_yukari
                            and (row, column) not in kenar_yukari_asagi
                            and (row, column) not in kenar_sag_sol
                    ):
                        sol = (row - 1, column)
                        sag = (row + 1, column)
                        yukari = (row, column - 1)
                        asagi = (row, column + 1)

                        sol_dolu = False
                        sag_dolu = False
                        yukari_dolu = False
                        asagi_dolu = False

                        if (
                                sol in blackList
                                or sol[0] < 0
                                or sol in kose_sol_yukari
                                or sol in kose_sol_asagi
                                or sol in kenar_yukari_asagi
                        ):
                            sol_dolu = True
                        if (
                                sag in blackList
                                or sag[0] > patika_boyutu - 1
                                or sag in kose_sag_yukari
                                or sag in kose_sag_asagi
                                or sag in kenar_yukari_asagi
                        ):
                            sag_dolu = True
                        if (
                                yukari in blackList
                                or yukari[1] < 0
                                or yukari in kose_sag_yukari
                                or yukari in kose_sol_yukari
                                or yukari in kenar_sag_sol
                        ):
                            yukari_dolu = True
                        if (
                                asagi in blackList
                                or asagi[1] > patika_boyutu - 1
                                or asagi in kose_sag_asagi
                                or asagi in kose_sol_asagi
                                or asagi in kenar_sag_sol
                        ):
                            asagi_dolu = True

                        rc = (row, column)

                        if sol_dolu:
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
                        while (
                                current in kose_sol_asagi
                                or current in kose_sol_yukari
                                or current in kose_sag_asagi
                                or current in kose_sag_yukari
                                or current in kenar_sag_sol
                                or current in kenar_yukari_asagi
                        ):
                            current_degisti = False
                            if gelis == "sag":
                                if current in kose_sag_asagi:
                                    current_degisti = True
                                    gelis = "yukari"
                                    current = (
                                        current[0],
                                        current[1] + 1,
                                    )
                                elif current in kose_sag_yukari:
                                    current_degisti = True
                                    gelis = "asagi"
                                    current = (
                                        current[0],
                                        current[1] - 1,
                                    )
                                elif current in kenar_sag_sol:
                                    current_degisti = True
                                    gelis = "sag"
                                    current = (
                                        current[0] - 1,
                                        current[1],
                                    )
                            elif gelis == "sol":

                                if current in kose_sol_asagi:
                                    current_degisti = True
                                    gelis = "yukari"
                                    current = (
                                        current[0],
                                        current[1] + 1,
                                    )
                                elif current in kose_sol_yukari:
                                    current_degisti = True
                                    gelis = "asagi"
                                    current = (
                                        current[0],
                                        current[1] - 1,
                                    )
                                elif current in kenar_sag_sol:
                                    current_degisti = True
                                    gelis = "sol"
                                    current = (
                                        current[0] + 1,
                                        current[1],
                                    )
                            elif gelis == "asagi":

                                if current in kose_sol_asagi:
                                    current_degisti = True
                                    gelis = "sag"
                                    current = (
                                        current[0] - 1,
                                        current[1],
                                    )
                                elif current in kose_sag_asagi:
                                    current_degisti = True
                                    gelis = "sol"
                                    current = (
                                        current[0] + 1,
                                        current[1],
                                    )
                                elif current in kenar_yukari_asagi:
                                    current_degisti = True
                                    gelis = "asagi"
                                    current = (
                                        current[0],
                                        current[1] - 1,
                                    )
                            elif gelis == "yukari":

                                if current in kose_sol_yukari:
                                    current_degisti = True
                                    gelis = "sag"
                                    current = (
                                        current[0] - 1,
                                        current[1],
                                    )
                                elif current in kose_sag_yukari:
                                    current_degisti = True
                                    gelis = "sol"
                                    current = (
                                        current[0] + 1,
                                        current[1],
                                    )
                                elif current in kenar_yukari_asagi:
                                    current_degisti = True
                                    gelis = "yukari"
                                    current = (
                                        current[0],
                                        current[1] + 1,
                                    )
                            if current_degisti == False:
                                return "Wrong Question"
                        if sol_dolu:
                            if rc in sag_cizgi:
                                if (
                                        current == yukari
                                ):
                                    asagi_cizgi.append(
                                        rc
                                    )
                                    yukari_cizgi.append(
                                        asagi
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(asagi[0], asagi[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                                elif (
                                        current == asagi
                                ):
                                    yukari_cizgi.append(
                                        rc
                                    )
                                    asagi_cizgi.append(
                                        yukari
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(yukari[0], yukari[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                            elif rc in yukari_cizgi:
                                if (
                                        current == sag
                                ):
                                    asagi_cizgi.append(
                                        rc
                                    )
                                    yukari_cizgi.append(
                                        asagi
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(asagi[0], asagi[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                                if (
                                        current == asagi
                                ):
                                    if sag[0] > patika_boyutu - 1:
                                        continue
                                    sag_cizgi.append(
                                        rc
                                    )
                                    sol_cizgi.append(
                                        sag
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(sag[0], sag[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                            elif rc in asagi_cizgi:
                                if (
                                        current == sag
                                ):
                                    yukari_cizgi.append(
                                        rc
                                    )
                                    asagi_cizgi.append(
                                        yukari
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(yukari[0], yukari[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                                elif (
                                        current == yukari
                                ):
                                    sag_cizgi.append(
                                        rc
                                    )
                                    sol_cizgi.append(
                                        sag
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(sag[0], sag[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                        elif sag_dolu:
                            if rc in sol_cizgi:
                                if (
                                        current == yukari
                                ):
                                    asagi_cizgi.append(
                                        rc
                                    )
                                    yukari_cizgi.append(
                                        asagi
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(asagi[0], asagi[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                                elif (
                                        current == asagi
                                ):
                                    yukari_cizgi.append(
                                        rc
                                    )
                                    asagi_cizgi.append(
                                        yukari
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(yukari[0], yukari[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                            elif rc in yukari_cizgi:
                                if (
                                        current == sol
                                ):
                                    asagi_cizgi.append(
                                        rc
                                    )
                                    yukari_cizgi.append(
                                        asagi
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(asagi[0], asagi[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                                elif (
                                        current == asagi
                                ):
                                    sol_cizgi.append(
                                        rc
                                    )
                                    sag_cizgi.append(
                                        sol
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(sol[0], sol[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                            elif rc in asagi_cizgi:
                                if (
                                        current == sol
                                ):
                                    yukari_cizgi.append(
                                        rc
                                    )
                                    asagi_cizgi.append(
                                        yukari
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(yukari[0], yukari[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                                elif (
                                        current == yukari
                                ):
                                    sol_cizgi.append(
                                        rc
                                    )
                                    sag_cizgi.append(
                                        sol
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(sol[0], sol[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                        elif yukari_dolu:
                            if rc in sol_cizgi:
                                if (
                                        current == sag
                                ):
                                    asagi_cizgi.append(
                                        rc
                                    )
                                    yukari_cizgi.append(
                                        asagi
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(asagi[0], asagi[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                                elif (
                                        current == asagi
                                ):
                                    sag_cizgi.append(
                                        rc
                                    )
                                    sol_cizgi.append(
                                        sag
                                    )  # üstündeki bloğa sag_cizgi eklendi
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(sag[0], sag[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                            elif rc in sag_cizgi:
                                if (
                                        current == sol
                                ):
                                    asagi_cizgi.append(
                                        rc
                                    )
                                    yukari_cizgi.append(
                                        asagi
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(asagi[0], asagi[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                                elif (
                                        current == asagi
                                ):
                                    sol_cizgi.append(
                                        rc
                                    )
                                    sag_cizgi.append(
                                        sol
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(sol[0], sol[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                            elif rc in asagi_cizgi:
                                if (
                                        current == sol
                                ):
                                    sag_cizgi.append(
                                        rc
                                    )
                                    sol_cizgi.append(
                                        sag
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(sag[0], sag[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                                elif (
                                        current == sag
                                ):
                                    sol_cizgi.append(
                                        rc
                                    )
                                    sag_cizgi.append(
                                        sol
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(sol[0], sol[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                        elif asagi_dolu:
                            if rc in sol_cizgi:
                                if (
                                        current == sag
                                ):
                                    yukari_cizgi.append(
                                        rc
                                    )
                                    asagi_cizgi.append(
                                        yukari
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(yukari[0], yukari[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                                elif (
                                        current == yukari
                                ):
                                    sag_cizgi.append(
                                        rc
                                    )
                                    sol_cizgi.append(
                                        sag
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(sag[0], sag[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                            elif rc in sag_cizgi:
                                if (
                                        current == sol
                                ):
                                    yukari_cizgi.append(
                                        rc
                                    )
                                    asagi_cizgi.append(
                                        yukari
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(yukari[0], yukari[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                                elif (
                                        current == yukari
                                ):
                                    sol_cizgi.append(
                                        rc
                                    )
                                    sag_cizgi.append(
                                        sol
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(sol[0], sol[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                            elif rc in yukari_cizgi:
                                if (
                                        current == sol
                                ):
                                    sag_cizgi.append(
                                        rc
                                    )
                                    sol_cizgi.append(
                                        sag
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(sag[0], sag[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                                elif (
                                        current == sag
                                ):
                                    sol_cizgi.append(
                                        rc
                                    )
                                    sag_cizgi.append(
                                        sol
                                    )
                                    bas = orta_nokta(rc[0], rc[1], genislik)
                                    son = orta_nokta(sol[0], sol[1], genislik)
                                    degismeme_count = "degisti"
                                    break
                    if (
                            (row, column) in sag_cizgi
                            and (row, column) in sol_cizgi
                            and (row, column) not in kenar_sag_sol
                    ):
                        degismeme_count = "degisti"
                        kenar_sag_sol.append((row, column))
                    elif (
                            (row, column) in yukari_cizgi
                            and (row, column) in asagi_cizgi
                            and (row, column) not in kenar_yukari_asagi
                    ):
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append((row, column))
                    elif (
                            (row, column) in sol_cizgi
                            and (row, column) in asagi_cizgi
                            and (row, column) not in kose_sol_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sol_asagi.append((row, column))
                    elif (
                            (row, column) in sol_cizgi
                            and (row, column) in yukari_cizgi
                            and (row, column) not in kose_sol_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sol_yukari.append((row, column))
                    elif (
                            (row, column) in sag_cizgi
                            and (row, column) in asagi_cizgi
                            and (row, column) not in kose_sag_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sag_asagi.append((row, column))
                    elif (
                            (row, column) in sag_cizgi
                            and (row, column) in yukari_cizgi
                            and (row, column) not in kose_sag_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sag_yukari.append((row, column))
                    if (
                            sag in sag_cizgi
                            and sag in sol_cizgi
                            and sag not in kenar_sag_sol
                    ):
                        degismeme_count = "degisti"
                        kenar_sag_sol.append(sag)
                    elif (
                            sag in yukari_cizgi
                            and sag in asagi_cizgi
                            and sag not in kenar_yukari_asagi
                    ):
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append(sag)
                    elif (
                            sag in sol_cizgi
                            and sag in asagi_cizgi
                            and sag not in kose_sol_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sol_asagi.append(sag)
                    elif (
                            sag in sol_cizgi
                            and sag in yukari_cizgi
                            and sag not in kose_sol_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sol_yukari.append(sag)
                    elif (
                            sag in sag_cizgi
                            and sag in asagi_cizgi
                            and sag not in kose_sag_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sag_asagi.append(sag)
                    elif (
                            sag in sag_cizgi
                            and sag in yukari_cizgi
                            and sag not in kose_sag_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sag_yukari.append(sag)
                    if (
                            sol in sag_cizgi
                            and sol in sol_cizgi
                            and sol not in kenar_sag_sol
                    ):
                        degismeme_count = "degisti"
                        kenar_sag_sol.append(sol)
                    elif (
                            sol in yukari_cizgi
                            and sol in asagi_cizgi
                            and sol not in kenar_yukari_asagi
                    ):
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append(sol)
                    elif (
                            sol in sol_cizgi
                            and sol in asagi_cizgi
                            and sol not in kose_sol_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sol_asagi.append(sol)
                    elif (
                            sol in sol_cizgi
                            and sol in yukari_cizgi
                            and sol not in kose_sol_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sol_yukari.append(sol)
                    elif (
                            sol in sag_cizgi
                            and sol in asagi_cizgi
                            and sol not in kose_sag_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sag_asagi.append(sol)
                    elif (
                            sol in sag_cizgi
                            and sol in yukari_cizgi
                            and sol not in kose_sag_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sag_yukari.append(sol)
                    if (
                            yukari in sag_cizgi
                            and yukari in sol_cizgi
                            and yukari not in kenar_sag_sol
                    ):
                        degismeme_count = "degisti"
                        kenar_sag_sol.append(yukari)
                    elif (
                            yukari in yukari_cizgi
                            and yukari in asagi_cizgi
                            and yukari not in kenar_yukari_asagi
                    ):
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append(yukari)
                    elif (
                            yukari in sol_cizgi
                            and yukari in asagi_cizgi
                            and yukari not in kose_sol_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sol_asagi.append(yukari)
                    elif (
                            yukari in sol_cizgi
                            and yukari in yukari_cizgi
                            and yukari not in kose_sol_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sol_yukari.append(yukari)
                    elif (
                            yukari in sag_cizgi
                            and yukari in asagi_cizgi
                            and yukari not in kose_sag_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sag_asagi.append(yukari)
                    elif (
                            yukari in sag_cizgi
                            and yukari in yukari_cizgi
                            and yukari not in kose_sag_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sag_yukari.append(yukari)
                    if (
                            asagi in sag_cizgi
                            and asagi in sol_cizgi
                            and asagi not in kenar_sag_sol
                    ):
                        degismeme_count = "degisti"
                        kenar_sag_sol.append(asagi)
                    elif (
                            asagi in yukari_cizgi
                            and asagi in asagi_cizgi
                            and asagi not in kenar_yukari_asagi
                    ):
                        degismeme_count = "degisti"
                        kenar_yukari_asagi.append(asagi)
                    elif (
                            asagi in sol_cizgi
                            and asagi in asagi_cizgi
                            and asagi not in kose_sol_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sol_asagi.append(asagi)
                    elif (
                            asagi in sol_cizgi
                            and asagi in yukari_cizgi
                            and asagi not in kose_sol_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sol_yukari.append(asagi)
                    elif (
                            asagi in sag_cizgi
                            and asagi in asagi_cizgi
                            and asagi not in kose_sag_asagi
                    ):
                        degismeme_count = "degisti"
                        kose_sag_asagi.append(asagi)
                    elif (
                            asagi in sag_cizgi
                            and asagi in yukari_cizgi
                            and asagi not in kose_sag_yukari
                    ):
                        degismeme_count = "degisti"
                        kose_sag_yukari.append(asagi)
            tum_kenar_koseler = (
                    blackList
                    + kose_sag_asagi
                    + kose_sag_yukari
                    + kose_sol_asagi
                    + kose_sol_yukari
                    + kenar_sag_sol
                    + kenar_yukari_asagi
            )

            if (
                    not all(
                        [
                            (r, c) in tum_kenar_koseler
                            for r in range(patika_boyutu)
                            for c in range(patika_boyutu)
                        ]
                    )
                    and degismeme_count == "degismedi"
            ):
                return "Wrong Question"
    for row in range(patika_boyutu):
        for column in range(patika_boyutu):
            if (
                    (row, column) in sag_cizgi
                    and (row, column) in sol_cizgi
                    and (row, column) not in kenar_sag_sol
            ):

                kenar_sag_sol.append((row, column))
            elif (
                    (row, column) in yukari_cizgi
                    and (row, column) in asagi_cizgi
                    and (row, column) not in kenar_yukari_asagi
            ):

                kenar_yukari_asagi.append((row, column))
            elif (
                    (row, column) in sol_cizgi
                    and (row, column) in asagi_cizgi
                    and (row, column) not in kose_sol_asagi
            ):

                kose_sol_asagi.append((row, column))
            elif (
                    (row, column) in sol_cizgi
                    and (row, column) in yukari_cizgi
                    and (row, column) not in kose_sol_yukari
            ):

                kose_sol_yukari.append((row, column))
            elif (
                    (row, column) in sag_cizgi
                    and (row, column) in asagi_cizgi
                    and (row, column) not in kose_sag_asagi
            ):

                kose_sag_asagi.append((row, column))
            elif (
                    (row, column) in sag_cizgi
                    and (row, column) in yukari_cizgi
                    and (row, column) not in kose_sag_yukari
            ):

                kose_sag_yukari.append((row, column))
    return (
        kose_sag_asagi,
        kose_sag_yukari,
        kose_sol_asagi,
        kose_sol_yukari,
        kenar_sag_sol,
        kenar_yukari_asagi,
    )


def solver(canvas, blackList, genislik, patika_boyutu=6, siyah_kare_sayisi=0):
    sonuc = koseleri_bul(canvas, blackList, genislik, patika_boyutu)
    if sonuc == "Wrong Question":
        is_solvable = False
    else:
        (
            kose_sag_asagi,
            kose_sag_yukari,
            kose_sol_asagi,
            kose_sol_yukari,
            kenar_sag_sol,
            kenar_yukari_asagi,
        ) = sonuc
        tum_kenar_koseler = (
                blackList
                + kose_sag_asagi
                + kose_sag_yukari
                + kose_sol_asagi
                + kose_sol_yukari
                + kenar_sag_sol
                + kenar_yukari_asagi
        )

        if any(
                [
                    tum_kenar_koseler.count((r, c)) > 1
                    for r in range(patika_boyutu)
                    for c in range(patika_boyutu)
                ]
        ):
            is_solvable = False
        else:
            is_solvable = all(
                [
                    (r, c) in tum_kenar_koseler
                    for r in range(patika_boyutu)
                    for c in range(patika_boyutu)
                ]
            )
        if is_solvable:
            is_solvable = tracking_check(
                kose_sag_asagi,
                kose_sag_yukari,
                kose_sol_asagi,
                kose_sol_yukari,
                kenar_sag_sol,
                kenar_yukari_asagi,
                patika_boyutu,
                siyah_kare_sayisi,
            )

    if is_solvable:
        return is_solvable, [
            blackList,
            kose_sag_asagi,
            kose_sag_yukari,
            kose_sol_asagi,
            kose_sol_yukari,
            kenar_sag_sol,
            kenar_yukari_asagi,
        ]

    return is_solvable, []


def class_main(c, genislik, patika_boyutu=6, siyah_kare_sayisi=0):
    if siyah_kare_sayisi == 0:
        siyah_kare_sayisi = patika_boyutu
    # blackList = create_grid(c,genislik, patika_boyutu, siyah_kare_sayisi)
    # solver(c, blackList, genislik,patika_boyutu, siyah_kare_sayisi)
    # start = timeit.default_timer()
    for _ in range(1000000):
        blackList = create_grid(c, genislik, patika_boyutu, siyah_kare_sayisi)
        is_solvable = solver(c, blackList, genislik, patika_boyutu, siyah_kare_sayisi)
        if is_solvable[0]:
            # print("BlackList: ", blackList, "is solvable")
            break
    return is_solvable[1]


def gen(size):
    c = None
    genislik = 40
    patika_boyutu = size
    if patika_boyutu == 5:
        siyah_kare_sayisi = random.choice([3, 3, 3, 5])
    elif patika_boyutu == 6:
        siyah_kare_sayisi = random.choice([4, 6])
    elif patika_boyutu == 7:
        siyah_kare_sayisi = random.choice([7, 7, 9])
    elif patika_boyutu == 8:
        siyah_kare_sayisi = random.choice([10, 10, 10, 8, 2])
    elif patika_boyutu == 9:
        siyah_kare_sayisi = random.choice([11, 11, 11, 9, 9, 13])
    else:
        raise ValueError("Size should be in between 5 and 9")
    result = class_main(
        c,
        genislik,
        patika_boyutu=patika_boyutu,
        siyah_kare_sayisi=siyah_kare_sayisi
    )
    return result


def main(size, count):
    datacontrol = set()
    data = []
    database = []
    for i in range(count):
        a = gen(size)
        data.append(a)
        datacontrol.add(str(a))
    for i in data:
        if str(i) in datacontrol:
            datacontrol.discard(str(i))
            database.append(i)
    return database
