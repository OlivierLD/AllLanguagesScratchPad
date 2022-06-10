fib 0 a b = a
fib n a b = fib (n - 1) b (a + b)

fibonacci n = fib n 0 1

main = print [fibonacci n | n <- [0..19]]
