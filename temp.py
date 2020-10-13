grid = [
    [-2, 1, -1, -2, -2, 4, -1, 3],
    [1, 3, 3, -2, -1, -1, -1, -1],
    [-1, 4, -1, -1, -2, 3, -2, -1],
    [-1, 5, -1, -2, -2, -2, -2, 1],
    [3, -1, -2, 2, 2, -2, 1, -2],
    [2, -1, -2, -1, -2, -1, 2, -2],
    [-2, -2, -1, 2, 4, -1, -2, 1],
    [-2, -1, 2, 1, 2, -1, -1, 1],
]


def komsular(y, x):
    komsular = [
        (y + 1, x),
        (y - 1, x),
        (y, x + 1),
        (y, x - 1),
        (y + 1, x + 1),
        (y + 1, x - 1),
        (y - 1, x + 1),
        (y - 1, x - 1),
    ]
    for _ in range(5):
        for komsu in komsular:
            if komsu[0] < 0 or komsu[0] > 8 - 1 or komsu[1] < 0 or komsu[1] > 8 - 1:
                komsular.remove(komsu)
    return komsular


def isAllEmptyHasNumNeighbor(grid):
    for y in range(len(grid)):
        for x in range(len(grid)):
            result = False
            if grid[y][x] <= 0:
                komsulr = komsular(y, x)
                for k in komsulr:
                    if grid[k[0]][k[1]] > 0:
                        result = True
                        break
                if not result:
                    return False
    return True


print(isAllEmptyHasNumNeighbor(grid))