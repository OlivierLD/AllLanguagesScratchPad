sieve (p : xs) = p : sieve [x | x <- xs, x `mod` p /= 0]

primes = sieve [2..]

main = print $ take 20 primes
