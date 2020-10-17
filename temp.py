# import copy
# from itertools import combinations

# grid = [
#     [0, 0, 1, 0, 0],
#     [3, 3, 3, 1, 0],
#     [0, 0, 2, 0, 0],
#     [1, 0, 3, 0, 1],
#     [0, 1, 0, 0, 0],
# ]

# cells = [(y, x) for y in range(len(grid)) for x in range(len(grid)) if grid[y][x] > 0]
# # time.sleep(0.1)
# # print(grid)
# for silinen_ipucu_sayisi in range(len(cells) - len(grid) + 1, len(grid) - 1, -1):

#     for sis in combinations(cells, silinen_ipucu_sayisi):
#         # print(sis)
#         copy_g = copy.deepcopy(grid)
#         for si in sis:
#             copy_g[si[0]][si[1]] = 0
#         print("Copy_g2:", copy_g)

from itertools import combinations

cells = [
    (0, 1),
    (0, 2),
    (1, 0),
    (1, 1),
    (1, 2),
    (1, 4),
    (2, 0),
    (2, 1),
    (2, 4),
    (3, 0),
    (3, 2),
    (3, 3),
    (3, 4),
    (4, 0),
    (4, 3),
]

for i in combinations(cells, 14):
    print(i)