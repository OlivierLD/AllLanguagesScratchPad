import Data.Matrix

m1 = matrix 3 4 $ \(r, c) -> 4 * (r - 1) + c
m2 = fromList 3 4 [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
m3 = fromLists [[1, 2, 3, 4], [5, 6, 7, 8], [9, 10, 11, 12]]

main = do
    print m1
    print m2
    print m3

    print $ zero 3 4
    print $ identity 3
    print $ permMatrix 3 1 2

    print $ nrows m1
    print $ ncols m1

    print $ getElem 2 3 m1
    print $ m1 ! (2, 3)
    print $ getRow 2 m1
    print $ getCol 3 m1
    print $ getDiag m1

    print $ setElem 13 (2, 3) m1
    print $ transpose m1
    print $ extendTo 0 4 8 m1
    print $ mapRow (\c x -> 2 * x) 3 m1

    print $ submatrix 2 3 1 2 m1
    print $ minorMatrix 1 2 m1
    print $ splitBlocks 2 3 m1

    print $ m1 <|> zero 3 2
    print $ m1 <-> zero 2 4

    print $ multStd m1 (identity 4)

    print $ scaleMatrix 2 m1
    print $ scaleRow 2 3 m1
    print $ combineRows 3 2 1 m1
    print $ switchRows 1 2 m1

    print $ luDecomp $ fromLists [[1.0, 2.0], [3.0, 4.0]]
    print $ trace m1
    print $ diagProd m1

    print $ detLaplace $ identity 3
    print $ detLU $ fromLists [[1.0, 2.0], [3.0, 4.0]]
