from random import choice
from itertools import combinations


figures = [
            (
                (1, 1, 1, 1, 1),
            ),
            (
                (1, 1, 1, 1),
                (1, 0, 0, 0),
            ),
            (
                (1, 1, 1, 1),
                (0, 1, 0, 0),
            ),
            (
                (1, 1, 1, 0),
                (0, 0, 1, 1),
            ),
            (
                (1, 1, 1),
                (1, 0, 1),
            ),
            (
                (1, 1, 1),
                (0, 1, 1),
            ),
            (
                (1, 1, 1),
                (1, 0, 0),
                (1, 0, 0)
            ),
            (
                (1, 1, 0),
                (0, 1, 1),
                (0, 0, 1)
            ),
            (
                (1, 0, 0),
                (1, 1, 1),
                (0, 0, 1)
            ),
            (
                (0, 1, 0),
                (1, 1, 1),
                (1, 0, 0)
            ),
            (
                (0, 1, 0),
                (1, 1, 1),
                (0, 1, 0)
            ),
            (
                (1, 1, 1),
                (0, 1, 0),
                (0, 1, 0)
            )
        ]


class Node:
    def __init__(self, value):
        self.value = value
        self.up = None
        self.down = None
        self.left = None
        self.right = None
        self.row_head = None
        self.col_head = None
        __slots__ = ('value', 'up', 'down', 'left', 'right', 'row_head', 'col_head')


class Linked_list_2D:
    def __init__(self, width):
        self.width = width
        self.head = None
        self.size = 0

    def append(self, value):
        new_node = Node(value)

        if self.head is None:
            self.head = left_neigh = right_neigh = up_neigh = down_neigh = new_node
        elif self.size % self.width == 0:
            up_neigh = self.head.up
            down_neigh = self.head
            left_neigh = right_neigh = new_node
        else:
            left_neigh = self.head.up.left
            right_neigh = left_neigh.right
            if left_neigh is left_neigh.up:
                up_neigh = down_neigh = new_node
            else:
                up_neigh = left_neigh.up.right
                down_neigh = up_neigh.down

        new_node.up = up_neigh
        new_node.down = down_neigh
        new_node.left = left_neigh
        new_node.right = right_neigh
        # Every node has links to the first node of row and column
        # These nodes are used as the starting point to deletion and insertion
        new_node.row_head = right_neigh
        new_node.col_head = down_neigh

        up_neigh.down = new_node
        down_neigh.up = new_node
        right_neigh.left = new_node
        left_neigh.right = new_node

        self.size += 1

    ### First approach - a lot of code duplicity
    def col_nonzero_nodes(self, node):
        cur_node = node
        while cur_node:
            if cur_node.value and cur_node.row_head is not self.head:
                yield cur_node
            cur_node = cur_node.down
            if cur_node is node:
                break

    def row_nonzero_nodes(self, node):
        cur_node = node
        while cur_node:
            if cur_node.value and cur_node.col_head is not self.head:
                yield cur_node
            cur_node = cur_node.right
            if cur_node is node:
                break

    def delete_row(self, node):
        cur_node = node
        while cur_node:
            up_neigh = cur_node.up
            down_neigh = cur_node.down

            if cur_node is self.head:
                self.head = down_neigh
                if cur_node is down_neigh:
                    self.head = None

            up_neigh.down = down_neigh
            down_neigh.up = up_neigh

            cur_node = cur_node.right
            if cur_node is node:
                break

    def insert_row(self, node):
        cur_node = node
        while cur_node:
            up_neigh = cur_node.up
            down_neigh = cur_node.down

            up_neigh.down = cur_node
            down_neigh.up = cur_node

            cur_node = cur_node.right
            if cur_node is node:
                break

    def insert_col(self, node):
        cur_node = node
        while cur_node:
            left_neigh = cur_node.left
            right_neigh = cur_node.right

            left_neigh.right = cur_node
            right_neigh.left = cur_node

            cur_node = cur_node.down
            if cur_node is node:
                break

    def delete_col(self, node):
        cur_node = node
        while cur_node:
            left_neigh = cur_node.left
            right_neigh = cur_node.right

            if cur_node is self.head:
                self.head = right_neigh
                if cur_node is right_neigh:
                    self.head = None

            left_neigh.right = right_neigh
            right_neigh.left = left_neigh

            cur_node = cur_node.down
            if cur_node is node:
                break


class Solver:
    def __init__(self, board=None, rows=None, cols=None, blocks=None, figure=None):
        if board is None:
            board = [[0] * cols for _ in range(rows)]
        if rows is None and cols is None:
            rows = len(board)
            cols = len(board[0])
        if blocks:
            for i in blocks:
                board[i[0]][i[1]] = "#"
        self.rows = rows
        self.cols = cols
        self.fig_name_start = 1
        self.figures = figure
        self.solutions = set()
        self.llist = None
        self.start_board = board
        self.tried_variants_num = 0

    def find_solutions(self):
        named_figures = set(enumerate(self.figures, self.fig_name_start))
        all_figure_postures = self.unique_figure_postures(named_figures)
        self.llist = Linked_list_2D(self.rows * self.cols + 1)
        pos_gen = self.generate_positions(all_figure_postures, self.rows, self.cols)
        for line in pos_gen:
            for val in line:
                self.llist.append(val)
        self.delete_filled_on_start_cells(self.llist)
        self.dlx_alg(self.llist, self.start_board)

    # Converts a one dimensional's element index to two dimensional's coordinates
    def num_to_coords(self, num):
        row = num // self.cols
        col = num - row * self.cols
        return row, col

    def delete_filled_on_start_cells(self, llist):
        for col_head_node in llist.row_nonzero_nodes(llist.head):
            row, col = self.num_to_coords(col_head_node.value - 1)
            if self.start_board[row][col]:
                llist.delete_col(col_head_node)

    def check_solution_uniqueness(self, solution):
        reflected_solution = self.reflect(solution)
        for sol in [solution, reflected_solution]:
            if sol in self.solutions:
                return
            for _ in range(3):
                sol = self.rotate(sol)
                if sol in self.solutions:
                    return
        return 1

    def dlx_alg(self, llist, board):
        # If no rows left - all figures are used
        if llist.head.down is llist.head:
            self.tried_variants_num += 1
            # If no columns left - all cells are filled, the solution is found.
            if llist.head.right is llist.head:
                solution = tuple(tuple(row) for row in board)
                if self.check_solution_uniqueness(solution):
                    self.solutions.add(solution)
                return
        # Search a column with a minimum of intersected rows
        min_col, min_col_sum = self.find_min_col(llist, llist.head)
        # The perfomance optimization - stops branch analyzing if empty columns appears
        if min_col_sum == 0:
            self.tried_variants_num += 1
            return

        intersected_rows = []
        for node in llist.col_nonzero_nodes(min_col):
            intersected_rows.append(node.row_head)
        # Pick one row (the variant of figure) and try to solve puzzle with it
        for selected_row in intersected_rows:
            rows_to_restore = []
            new_board = self.add_posture_to_board(selected_row, board)
            # If some figure is used, any other variants (postures) of this figure
            # could be discarded in this branch
            for posture_num_node in llist.col_nonzero_nodes(llist.head):
                if posture_num_node.value == selected_row.value:
                    rows_to_restore.append(posture_num_node)
                    llist.delete_row(posture_num_node)

            cols_to_restore = []
            for col_node in llist.row_nonzero_nodes(selected_row):
                for row_node in llist.col_nonzero_nodes(col_node.col_head):
                    # Delete all rows which are using the same cell as the picked one,
                    # because only one figure can fill the specific cell
                    rows_to_restore.append(row_node.row_head)
                    llist.delete_row(row_node.row_head)
                # Delete the columns the picked figure fill, they are not
                # needed in this branch anymore
                cols_to_restore.append(col_node.col_head)
                llist.delete_col(col_node.col_head)
            # Pass the shrinked llist and the board with the picked figure added
            # to the next processing
            self.dlx_alg(llist, new_board)

            for row in rows_to_restore:
                llist.insert_row(row)

            for col in cols_to_restore:
                llist.insert_col(col)

    def find_min_col(self, llist, min_col):
        min_col_sum = float("inf")
        for col in llist.row_nonzero_nodes(llist.head):
            tmp = sum(1 for _ in llist.col_nonzero_nodes(col))
            if tmp < min_col_sum:
                min_col = col
                min_col_sum = tmp
        return min_col, min_col_sum

    def add_posture_to_board(self, posture_row, prev_steps_result):
        new_board = prev_steps_result.copy()
        for node in self.llist.row_nonzero_nodes(posture_row):
            row, col = self.num_to_coords(node.col_head.value - 1)
            new_board[row][col] = node.row_head.value
        return new_board

    def print_board(self, board):
        for row in board:
            for cell in row:
                print(f"{cell: >3}", end='')
            print()
        print()
        print("#" * 30)

    def unique_figure_postures(self, named_figures):
        postures = set(named_figures)
        for name, fig in named_figures:
            postures.add((name, self.reflect(fig)))
        all_postures = set(postures)
        for name, posture in postures:
            for _ in range(3):
                posture = self.rotate(posture)
                all_postures.add((name, posture))

        return all_postures

    # Generates entries for all possible positions of every figure's posture.
    # Then the items of these entires will be linked into the 2 dimensional circular linked list
    # The entry looks like:
    # figure's name  {board cells filled by figure}  empty board's cells
    #            |       | | | | |                       | | |
    #            5 0 0 0 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 ................
    def generate_positions(self, postures, rows, cols):
        def apply_posture(name, posture, y, x, wdth, hght):
            # Flattening of 2d list
            line = [cell for row in self.start_board for cell in row]
            # Puts the figure onto the flattened start board
            for r in range(hght):
                for c in range(wdth):
                    if posture[r][c]:
                        num = (r + y) * cols + x + c
                        if line[num]:
                            return
                        line[num] = posture[r][c]
            # And adds name into the beginning
            line.insert(0, name)
            return line

        # makes columns header in a llist
        yield [i for i in range(rows * cols + 1)]

        for name, posture in postures:
            posture_height = len(posture)
            posture_width = len(posture[0])
            for row in range(rows):
                if row + posture_height > rows:
                    break
                for col in range(cols):
                    if col + posture_width > cols:
                        break
                    new_line = apply_posture(name, posture, row, col, posture_width, posture_height)
                    if new_line:
                        yield new_line

    def rotate(self, fig):
        return tuple(zip(*fig[::-1]))

    def reflect(self, fig):
        return tuple(fig[::-1])


def main(level, gridx=None, block_com=0, fig_com=0):
    # combinations 12 with --> 6 figures = 924, 5 figures = 792, 4 figures = 495
    # block combinations for each grid--> (4, 6) = 10.626, (3, 7) = 21, (5, 6) = 142.506, (4, 7) = 3.276,
    # (6, 6) = 1.947.792, (5, 7) = 324.632
    # 6 --> 4, 5, 6
    # 5 --> 5, 4, 3
    # 4 --> 4, 3
    base_dict = {"Easy": {(4, 6): 4, (3, 7): 1}, "Medium": {(5, 6): 5, (4, 7): 3}, "Hard": {(6, 6): 6, (5, 7): 5}}
    values = {}
    lap = 0
    if gridx:
        rows_cols = gridx
    else:
        rows_cols = choice(list(base_dict[level].keys()))
    grid = [(i, j) for j in range(rows_cols[1]) for i in range(rows_cols[0])]
    block_num = base_dict[level][rows_cols]
    fig_num = int((rows_cols[0] * rows_cols[1] - block_num) / 5)
    games = []
    for i in combinations(grid, block_num):
        if lap != block_com:
            lap += 1
            continue
        temp = False
        x = ""
        y = ""
        x_set = set()
        y_set = set()
        for j in i:
            x += str(j[0])
            x_set.add(j[0])
            y_set.add(j[1])
            y += str(j[1])
        for n in x_set:
            timesx = x.count(str(n))
            if fig_num == 6:
                if timesx > 3:
                    temp = True
                    break
            else:
                if timesx > 2:
                    temp = True
                    break
        for n in y_set:
            timesy = y.count(str(n))
            if len(i) == 6:
                if timesy > 3:
                    temp = True
                    break
            else:
                if timesy > 2:
                    temp = True
                    break
        if temp:
            continue
        i = list(i)
        for num in grid:
            values[num] = 0
        for j in i:
            values[j] = 1
        flag = True
        for a, b in grid:
            if a == 0:
                if b == 0:
                    if values[(a + 1, b)] and values[(a, b + 1)]:
                        flag = False
                elif b == rows_cols[1] - 1:
                    if values[(a + 1, b)] and values[(a, b - 1)]:
                        flag = False
                else:
                    if values[(a + 1, b)] and values[(a, b + 1)] and values[(a, b - 1)]:
                        flag = False
            elif a == rows_cols[0] - 1:
                if b == 0:
                    if values[(a - 1, b)] and values[(a, b + 1)]:
                        flag = False
                elif b == rows_cols[1] - 1:
                    if values[(a - 1, b)] and values[(a, b - 1)]:
                        flag = False
                else:
                    if values[(a - 1, b)] and values[(a, b + 1)] and values[(a, b - 1)]:
                        flag = False
            elif b == 0:
                if values[(a - 1, b)] and values[(a + 1, b)] and values[(a, b + 1)]:
                    flag = False
            elif b == rows_cols[1] - 1:
                if values[(a - 1, b)] and values[(a + 1, b)] and values[(a, b - 1)] == 0:
                    flag = False
            else:
                if not values[(a - 1, b)] and values[(a + 1, b)] and values[(a, b - 1)] and values[(a, b + 1)]:
                    flag = False
        if flag:
            com = 0
            for figs in combinations(figures, fig_num):
                if com != fig_com:
                    com += 1
                    continue
                solver = Solver(rows=rows_cols[0], cols=rows_cols[1], blocks=i, figure=set(figs))
                solver.find_solutions()
                if len(solver.solutions) == 1:
                    if level == "Hard":
                        temp_figs = set(figures).difference(set(figs))
                        for tur in range(len(temp_figs)):
                            try_fig = list(temp_figs)[tur]
                            new_figs = set(figs)
                            new_figs.add(try_fig)
                            solver_temp = Solver(rows=rows_cols[0], cols=rows_cols[1], blocks=i, figure=new_figs)
                            solver_temp.find_solutions()
                            if len(solver_temp.solutions) == 1:
                                print(new_figs)
                                print(i)
                                grid_new = []
                                for grids in solver.solutions:
                                    for a in grids:
                                        temp_list = []
                                        for b in a:
                                            if b == "#":
                                                temp_list.append(0)
                                            else:
                                                temp_list.append(b)
                                        grid_new.append(temp_list)
                                print(grid_new)
                                games.append(grid_new)
                                solver.print_board(grid_new)
                            else:
                                new_figs.discard(try_fig)
                    else:
                        print(figs)
                        print(i)
                        grid_new = []
                        for grids in solver.solutions:
                            for a in grids:
                                temp_list = []
                                for b in a:
                                    if b == "#":
                                        temp_list.append(0)
                                    else:
                                        temp_list.append(b)
                                grid_new.append(temp_list)
                        print(grid_new)
                        games.append(grid_new)
                        solver.print_board(grid_new)


main("Easy")
