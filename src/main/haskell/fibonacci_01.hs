fibonacci = 0 : 1 : zipWith (+) fibonacci (tail fibonacci)

main = print $ take 20 fibonacci
