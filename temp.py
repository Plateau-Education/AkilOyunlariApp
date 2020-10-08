# import numpy as np

# a = np.zeros((8,8),dtype=int)

import itertools

for i in itertools.permutations([1,2,3],2):
    print(i)