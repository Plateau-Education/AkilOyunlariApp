"""
Kelime düzenleme
"""
# for _ in range(10):
#     f = open("C:\\Users\\ezrea\\Documents\\GitHub\\AkilOyunlariApp\\Kelime Oyunları\\words.txt", encoding="utf-8")
#     words = f.read()
#     word_list = words.split("\n")
#     for word in word_list:
#         word = word.strip()
#         if any([x in word for x in " âîûABCÇDEFGĞHIİJKLMNOÖPRSŞTUÜVYZ"]):
#             word_list.remove(word)
        
#     words = "\n".join(word_list)
#     f.close()
#     f = open("C:\\Users\\ezrea\\Documents\\GitHub\\AkilOyunlariApp\\Kelime Oyunları\\words.txt", encoding="utf-8", mode="w")
#     f.write(words)
#     f.close()

# f = open("C:\\Users\\ezrea\\Documents\\GitHub\\AkilOyunlariApp\\Kelime Oyunları\\words.txt", encoding="utf-8")
# print(f.read())
# f.close()


"""
Harf Sayısına göre ayırma
"""
# with open("C:\\Users\\ezrea\\Documents\\GitHub\\AkilOyunlariApp\\Kelime Oyunları\\words.txt", encoding="utf-8") as all_words:
#     words = all_words.read()
#     word_list = words.split("\n")
#     for n in range(2,11):
#         harf_sayisina_gore = []
#         for word in word_list:
#             if len(word) == n:
#                 harf_sayisina_gore.append(word)
#         f = open(f"{n}.txt", encoding="utf-8",mode="w+")
#         f.write("\n".join(harf_sayisina_gore))
#         f.close()
