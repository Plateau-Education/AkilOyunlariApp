grid = [[0,0,0,6,5,3,0,0,0],
        [0,0,1,7,0,8,0,5,2],
        [0,5,4,1,0,0,3,8,0],
        [5,6,0,0,7,1,9,0,0],
        [0,9,0,5,0,6,8,0,1],
        [1,4,3,9,0,2,0,0,0],
        [0,0,5,3,9,4,0,1,6],
        [0,0,0,0,0,0,0,0,0],
        [0,1,0,0,6,0,4,7,0]]

import numpy as np

def possible(y,x,n):
    global grid
    for i in range(0,9):
        if grid[y][i] == n:
            return False
    for i in range(0,9):
        if grid[i][x] == n:
            return False
    x0 = (x//3)*3
    y0 = (y//3)*3
    for i in range(0,3):
        for j in range(0,3):
            if grid[y0+i][x0+j] == n:
                return False
    return True
solutions = []
def solve():
    global grid
    global solutions
    for y in range(9):
        for x in range(9):
            if grid[y][x] == 0:
                for n in range(1,10):
                    if possible(y,x,n):
                        grid[y][x] = n
                        solve()
                        grid[y][x] = 0
                return
    # print(np.matrix(grid))
    solutions.append(np.matrix(grid))
    # input("More?")

def tek_cozum():
    global solutions
    solve()
    if len(solutions) == 1: return True
    else: return False
print(tek_cozum())

        
