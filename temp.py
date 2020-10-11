# # import numpy as np

# # a = np.zeros((8,8),dtype=int)

import itertools

# # for i in itertools.combinations([(3,2),(4,3),(5,3),(5,2)],3):
# #     print(i)


# def intercept_setList(setList):
#     s = setList[0]
#     for i in setList[1:]:
#         s = s & i
#     return s

# def common_coordinates(tuple12):
#     one = intercept_setList(tuple12[0])
#     two = intercept_setList(tuple12[1])
#     return one,two

# d = {(1,1) : ([{(1,2),(2,3)},{(1,2),(3,4),(5,3)},{(1,2)}], [{(3,2),(5,3)},{(3,2),(3,4),(5,3)},{(3,2),(5,3)}])}
# # tuple12 = ([{(1,2),(2,3)},{(1,2),(3,4),(5,3)},{(1,2)}], [{(3,2),(5,3)},{(3,2),(3,4),(5,3)},{(3,2),(5,3)}])
# d[(1,1)] = common_coordinates(d[(1,1)])
# print(d)

# a = [{(1,2),(2,3)},{(1,2),(3,4),(5,3)},{(1,2)}]
# print(intercept_setList(a))
# list0 = [(2, 0), (0, 0), (1, 1), (0, 1)]
# for y1 in itertools.combinations(list0,1):
#     print(y1)
