from itertools import permutations
import random
import numpy as np

# Utils
def coordinates_to_index(size, coordinates):
    return coordinates[0] * size + coordinates[1]
def index_to_coordinates(size,index): 
    return ((index-1)//size, (index-1) % size)

def same_col_row_list(size, coordinates):
    r = coordinates[0]
    c = coordinates[1]
    scr_list = []
    # same row
    for i1 in range(c):
        scr_list.append((r,i1))
    for j1 in range(c+1,size):
        scr_list.append((r,j1))
    # same column
    for i2 in range(r):
        scr_list.append((i2,c))
    for j2 in range(r+1,size):
        scr_list.append((j2,c))
    return scr_list

def skyscraper_in_rowcol(rowcol, reverse):
    if reverse: rowcol.reverse()
    count = 0
    maxn = 0
    for i in rowcol:
        if(i > maxn):
            maxn=i
            count+=1
    return count



# Main Funcs
class Cell:
    def __init__(self, size):
        self.size = size
        self.possible_nums = list(range(1,size+1))
        self.coordinates = (0,0)
        self.written_number = 0
    def get_all_info(self):
        return self.size, self.possible_nums, self.coordinates, self.written_number

def create_empty_grid(size):
    grid = []
    for _ in range(size):
        grid.append(list())
    cell_dict = dict()
    for row in range(size):
        for column in range(size):
            c = Cell(size)
            c.coordinates = (row,column)
            cell_dict[(row,column)] = c
    return grid, cell_dict

def random_fill(size):
    grid, cell_dict = create_empty_grid(size)
    for cell in cell_dict.values():
        # print("cell: ",cell.get_all_info())
        if cell.possible_nums: 
            cell.written_number = random.choice(cell.possible_nums)
            cell.possible_nums = list()
            for scr in same_col_row_list(size,cell.coordinates):
                c2 = cell_dict[scr]
                try: c2.possible_nums.remove(cell.written_number)
                except: pass
        else:
            print("There is no possible number for this cell: ", cell.coordinates)
            return random_fill(size)
    r = 0
    for cell in range(len(cell_dict)):
        n = list(cell_dict.values())[cell].written_number
        grid[r].append(n)
        if(cell % 5 == 4):
            r+=1
             
    return grid

def find_clues(size, grid):
    clue_dict = dict()
    mgrid = np.matrix(grid)
    #up
    for i1 in range(size):
        clue_dict[(-1,i1)] = skyscraper_in_rowcol(mgrid.T[i1,:].tolist()[0], False)
    #left
    for i2 in range(size):
        clue_dict[(i2,-1)] = skyscraper_in_rowcol(mgrid[i2,:].tolist()[0], False)
    #down
    for i3 in range(size):
        clue_dict[(size,i3)] = skyscraper_in_rowcol(mgrid.T[i3,:].tolist()[0], True)
    #right
    for i4 in range(size):
        clue_dict[(i4,size)] = skyscraper_in_rowcol(mgrid[i4,:].tolist()[0], True)
    
    print(mgrid)
    return clue_dict


def solve(size, clue_dict):
    grid, cell_dict = create_empty_grid(size)
    mgrid = np.matrix(grid)

    for cl in range(len(clue_dict)):
        if cl == 5:
            pass

    

            


# g = random_fill(5)
# print(find_clues(5,g))

# grid = [[5,4,2],[7,1,3],[6,9,8]]
# mgrid = np.matrix(grid)
# print(mgrid.T[0,:].tolist())
# print(mgrid[:,0].flatten().tolist())
# print(np.matrix(random_fill(5)))