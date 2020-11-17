import re, random, timeit


def getAr(chosen, word_listStr):
    re_ = f"\n([{chosen}]" + "{" + str(len(chosen)) + "})\n"
    return re.findall(re_, word_listStr)


def func(chosen, word_list):
    strAr = sorted(chosen)
    filter_ = list(
        filter(lambda x: "".join(sorted(x)) == "".join(sorted(strAr)), word_list)
    )
    return filter_


f = open(
    "C:\\Users\\ezrea\\Documents\\GitHub\\AkilOyunlariApp\\KelimeOyunlari\\5.txt",
    encoding="utf-8",
)
words = f.read()
word_list = words.split("\n")


start = timeit.default_timer()
for s in ["kelim", "kalem", "zaman", "melek", "nedim"]:
    print(func(s, getAr(s, words)))
    # print(func(s, word_list))

end = timeit.default_timer()
print(f"It took {end-start} seconds.")
