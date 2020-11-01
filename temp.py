import copy


grid = [
    [8, 6, 4, [1, 0]],
    [2, 1, 4, [1, 0]],
    [9, 2, 7, [0, -1]],
]  # , [8, 1, 9, [3, 0]]]
# [[8, 9, 0, [1, 0]], [1, 7, 4, [1, 0]], [5, 4, 9, [1, 0]]]#, [5, 7, 0, [3, 0]]]


def checkAndReduce(grid, a, b=None):
    reduceList = []
    if b == None:
        for row in range(len(grid)):
            for n in range(len(grid)):
                if a == grid[row][n]:
                    if n == 0 and grid[row][-1][0] > 0:
                        grid[row][-1][0] -= 1
                        if grid[row][-1][0] + grid[row][-1][1] == 0:
                            for n2 in grid[row][:-1]:
                                if n2 != a:
                                    reduceList.append(n2)
                    elif n > 0 and grid[row][-1][1] > 0:
                        grid[row][-1][1] += 1
                        if grid[row][-1][0] + grid[row][-1][1] == 0:
                            for n2 in grid[row][:-1]:
                                if n2 != a:
                                    reduceList.append(n2)
                    else:
                        return False
        return reduceList
    else:
        for row in range(len(grid)):
            for n in range(len(grid)):
                if a == grid[row][n]:
                    if n == 0 and grid[row][-1][0] > 0:
                        grid[row][-1][0] -= 1
                        if grid[row][-1][0] + grid[row][-1][1] == 0:
                            for n2 in grid[row][:-1]:
                                if n2 != a:
                                    reduceList.append(n2)
                    elif n > 0 and grid[row][-1][1] > 0:
                        grid[row][-1][1] += 1
                        if grid[row][-1][0] + grid[row][-1][1] == 0:
                            for n2 in grid[row][:-1]:
                                if n2 != a:
                                    reduceList.append(n2)
                    else:
                        return False

        for row in range(len(grid)):
            for n in range(len(grid)):
                if b == grid[row][n]:
                    if n == 1 and grid[row][-1][0] > 0:
                        grid[row][-1][0] -= 1
                        if grid[row][-1][0] + grid[row][-1][1] == 0:
                            for n2 in grid[row][:-1]:
                                if n2 != b:
                                    reduceList.append(n2)
                    elif n > 0 and grid[row][-1][1] > 0:
                        grid[row][-1][1] += 1
                        if grid[row][-1][0] + grid[row][-1][1] == 0:
                            for n2 in grid[row][:-1]:
                                if n2 != b:
                                    reduceList.append(n2)
                    else:
                        return False
        return reduceList


def solver(grid):
    copy_g = copy.deepcopy(grid)
    list_a = [1, 2, 4, 6, 7, 8, 9]
    list_b = [1, 2, 4, 6, 7, 8, 9]
    list_c = [1, 2, 4, 6, 7, 8, 9]
    solutionCount = 0
    for a in list_a:
        car = checkAndReduce(copy_g, a)
        if car:
            for r in car:
                list_b.remove(r)
                list_c.remove(r)
        else:
            continue
        print(car)
        if len(list_b) == 0 or len(list_c) == 0:
            continue
        for b in list_b:
            for c in list_c:
                solutionCount += 1
    print(solutionCount)


solver(grid)