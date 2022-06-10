fib n m a b
    | n == m = a
    | otherwise = fib n (m + 1) b (a + b)

fibonacci n = fib n 0 0 1

main = print [fibonacci n | n <- [0..19]]
