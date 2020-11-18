import re
from itertools import combinations


class Anagram:
    def __init__(self, level):
        self.puzzles = []
        self.possible_words = None
        self.try_word = None
        self.words_str = []
        self.level = level
        self.dict = {"Easy": {1: [(4, 5, 6, 7)]}, "Hard": {1: [(6, 7, 8, 9, 10)]},
                     "Medium": {10: [(3, 7), (4, 6), (5, 5)], 9: [(2, 7), (3, 6), (4, 5)], 8: [(2, 6), (4, 4), (3, 5)]}}
        self.selected_lengths = None
        self.selected_word_list = None
        self.selected_word_list2 = None

    def makeWordsStr(self):
        for i in self.dict[self.level].values():
            for j in i:
                temp_list = []
                for k in j:
                    f = open(f"C:/Users/Proper/Desktop/oyun/kelimeler/{k}.txt", encoding="utf-8").read()
                    temp_list.append(f)
                self.words_str.append(temp_list)

    def getAll(self, words, try_word=None):
        if try_word:
            re_ = f"\n([{try_word}]" + "{" + str(len(try_word)) + "})\n"
        else:
            re_ = f"\n([{self.try_word}]" + "{" + str(len(self.try_word)) + "})\n"
        self.possible_words = re.findall(re_, words)
        return

    def getFiltered(self, try_word=None):
        if try_word:
            strAr = sorted(try_word)
        else:
            strAr = sorted(self.try_word)
        filter_ = set(filter(lambda x: "".join(sorted(x)) == "".join(sorted(strAr)), self.possible_words))
        self.possible_words = filter_
        return

    def generator(self, count=1):
        self.makeWordsStr()
        if self.level == "Easy":
            for i in self.dict[self.level].keys():
                for j in self.dict[self.level][i]:
                    for k in j:
                        f1 = open(f"C:/Users/Proper/Desktop/oyun/kelimeler/{k}.txt", encoding="utf-8").read()
                        self.selected_word_list = f1.split("\n")
                        self.selected_word_list.pop(0)
                        self.selected_word_list.pop(-1)
                        for word in self.selected_word_list:
                            self.try_word = word
                            self.getAll(f1)
                            self.getFiltered()
                            self.possible_words.discard(word)
                            if len(self.possible_words) == 1:
                                self.puzzles.append([self.try_word, list(self.possible_words)[0]])
                                if len(self.puzzles) == count:
                                    return
                                # print(self.try_word, list(self.possible_words)[0])
                                try:
                                    self.selected_word_list.remove(list(self.possible_words)[0])
                                except ValueError:
                                    pass
                            self.possible_words = []
        elif self.level == "Medium":
            for i in self.dict[self.level].keys():
                for j in self.dict[self.level][i]:
                    f1 = open(f"C:/Users/Proper/Desktop/oyun/kelimeler/{j[0]}.txt", encoding="utf-8").read()
                    self.selected_word_list = f1.split("\n")
                    for word in self.selected_word_list:
                        f2 = open(f"C:/Users/Proper/Desktop/oyun/kelimeler/{j[1]}.txt", encoding="utf-8").read()
                        self.selected_word_list2 = f2.split("\n")
                        for word2 in self.selected_word_list2:
                            self.try_word = word + word2
                            words = open(f"C:/Users/Proper/Desktop/oyun/kelimeler/{i}.txt", encoding="utf-8").read()
                            self.getAll(words)
                            self.getFiltered()
                            if len(self.possible_words) == 1:
                                self.puzzles.append([[word, word2], list(self.possible_words)[0]])
                                if len(self.puzzles) == count:
                                    return
                                # print([word, word2], list(self.possible_words)[0])
                                try:
                                    self.selected_word_list2.remove(list(self.possible_words)[0])
                                except ValueError:
                                    pass
                            self.possible_words = []
        elif self.level == "Hard":
            for i in self.dict[self.level].values():
                for j in i:
                    for n, a in enumerate(j):
                        f1 = open(f"C:/Users/Proper/Desktop/oyun/kelimeler/{a}.txt", encoding="utf-8").read()
                        self.selected_word_list = f1.split("\n")
                        for b in range(5 - n):
                            f2 = open(f"C:/Users/Proper/Desktop/oyun/kelimeler/{j[b]}.txt", encoding="utf-8").read()
                            self.selected_word_list2 = f2.split("\n")
                            for word in self.selected_word_list:
                                for word2 in self.selected_word_list2:
                                    if word == word2:
                                        continue
                                    try_list = list(word + word2)
                                    tried_set = set()
                                    for try_len in j:
                                        if len(try_list) - try_len < 6:
                                            continue
                                        if (try_len, len(try_list)) in tried_set:
                                            continue
                                        tried_set.add((len(try_list), try_len))
                                        tried_set.add((try_len, len(try_list)))
                                        try_str1 = open(f"C:/Users/Proper/Desktop/oyun/kelimeler/{try_len}.txt",
                                                        encoding="utf-8").read()
                                        num = len(try_list) - try_len
                                        try_str2 = open(f"C:/Users/Proper/Desktop/oyun/kelimeler/{num}.txt",
                                                        encoding="utf-8").read()
                                        lap = 1
                                        fac = 1
                                        for com in combinations(try_list, try_len):
                                            if lap != fac * 6 * 5 * 4 * 3 * 2:
                                                lap += 1
                                                continue
                                            fac += 1
                                            try_list = list(word + word2)
                                            for let in com:
                                                try_list.remove(let)
                                            if com == try_list:
                                                continue
                                            try1 = "".join(com)
                                            self.getAll(try_str1, try1)
                                            self.getFiltered(try_word=try1)
                                            if len(self.possible_words) != 1:
                                                continue
                                            possible1 = list(self.possible_words.copy())
                                            self.possible_words = []
                                            try2 = "".join(try_list)
                                            self.getAll(try_str2, try2)
                                            self.getFiltered(try_word=try2)
                                            if len(self.possible_words) != 1:
                                                continue
                                            possible1.extend(list(self.possible_words))
                                            self.possible_words = []
                                            # print([[word, word2], possible1])
                                            self.puzzles.append([[word, word2], possible1])
                                            if len(self.puzzles) == count:
                                                return
        else:
            return False


# deneme = Anagram("Medium")
# deneme.generator(10)
# for i in deneme.puzzles:
#     print(i)
