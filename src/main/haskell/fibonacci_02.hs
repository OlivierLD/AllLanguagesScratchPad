fib a b = a : fib b (a + b)

fibonacci = fib 0 1

main = print $ take 20 fibonacci
