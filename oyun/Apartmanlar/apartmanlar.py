from itertools import permutations
import random
import numpy as np


def create_empty_grid(size):
    grid = []
    for _ in range(size):
        grid.append(list())

    return grid


def randomly_fill_grid(size):
    grid = create_empty_grid(size)
    ps = []
    for p in permutations(range(1, size + 1), size):
        ps.append(p)
    possibility_list = [ps for _ in range(size - 1)]
    firstRow = list(range(1, size + 1))
    random.shuffle(firstRow)
    possibility_list.insert(0, firstRow)

    for i in range(1, size):
        rowPos = possibility_list[i]
        previous_rows
    return grid


def checkRowPos(row_possibility_list, previous_rows, size):
    n_previous_rows = np.array(previous_rows)
    n_previous_rows = list(n_previous_rows.T)
    for index in range(size):
        row_possibility_list = list(
            filter(
                lambda rp: rp[index] not in n_previous_rows[index], row_possibility_list
            )
        )

    return row_possibility_list


# ps = []
# for p in permutations(range(1, 6), 5):
#     ps.append(p)
# print(checkRowPos(ps, [[1, 2, 3, 4, 5], [2, 1, 4, 5, 3]], 5))
