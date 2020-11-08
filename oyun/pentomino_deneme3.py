from time import time
from datetime import timedelta


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

    def print_list(self, separator=' '):
        for row in self.traverse_node_line(self.head, "down"):
            for col in self.traverse_node_line(row, "right"):
                print(col.value, end=separator)
            print()

    def traverse_node_line(self, start_node, direction):
        cur_node = start_node
        while cur_node:
            yield cur_node
            cur_node = getattr(cur_node, direction)
            if cur_node is start_node:
                break

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
    def __init__(self, board=None, rows=None, cols=None, blocks=None):
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
        self.figures = {
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
        }
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
        self.starttime = time()
        self.prevtime = self.starttime
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

    def print_progress(self, message, interval):
        new_time = time()
        if (new_time - self.prevtime) >= interval:
            print(message)
            print(f"Time has elapsed: {timedelta(seconds=new_time - self.starttime)}")
            self.prevtime = new_time

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
            self.print_progress(f"{self.tried_variants_num} variants have tried", 5.0)
            self.tried_variants_num += 1
            # If no columns left - all cells are filled, the solution is found.
            if llist.head.right is llist.head:
                solution = tuple(tuple(row) for row in board)
                if self.check_solution_uniqueness(solution):
                    print(f"The solution № {len(self.solutions) + 1}")
                    self.print_board(solution)
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
            tmp = sum(1 for item in llist.col_nonzero_nodes(col))
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
        print("#" * 50)

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


rows = 4
cols = 6
blocks = [(2, 2), (3, 2), (2, 3), (3, 3)]
solver = Solver(rows=rows, cols=cols, blocks=blocks)
solver.find_solutions()
