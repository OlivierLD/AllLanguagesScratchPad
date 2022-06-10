# Haskell
- Install Haskell on Raspberry Pi: <https://wiki.haskell.org/Raspberry_Pi>
- On Mac, <https://medium.com/analytics-vidhya/install-haskell-on-macos-e5677ab620b5>
    ```
    $ brew install haskell-stack
    $ brew install ghc cabal-install
    ```
- Use haskell tools <https://downloads.haskell.org/~ghc/latest/docs/html/users_guide/using.html>

## Get Started
Good samples to get started from scratch: <https://www.schoolofhaskell.com/user/eriks/Simple%20examples>

### Execute the haskell files (interpreter)
```
$ stack runhaskell 01.hs
Hello, world! 
```

### Compile and run
```
$ stack ghc cellularautomaton[.hs]
$ # or just
$ ghc cellularautomaton[.hs]
$ ./cellularautomaton 
                                X                                
                               X X                               
                              X   X                              
                             X X X X                             
                            X       X                            
                           X X     X X                           
                          X   X   X   X                          
                         X X X X X X X X                         
                        X               X                        
                       X X             X X                       
                      X   X           X   X                      
                     X X X X         X X X X                     
                    X       X       X       X                    
                   X X     X X     X X     X X                   
                  X   X   X   X   X   X   X   X                  
                 X X X X X X X X X X X X X X X X                 
                X                               X                
               X X                             X X               
              X   X                           X   X              
             X X X X                         X X X X             
            X       X                       X       X            
           X X     X X                     X X     X X           
          X   X   X   X                   X   X   X   X          
         X X X X X X X X                 X X X X X X X X         
        X               X               X               X        
       X X             X X             X X             X X       
      X   X           X   X           X   X           X   X      
     X X X X         X X X X         X X X X         X X X X     
    X       X       X       X       X       X       X       X    
   X X     X X     X X     X X     X X     X X     X X     X X   
  X   X   X   X   X   X   X   X   X   X   X   X   X   X   X   X  
 X X X X X X X X X X X X X X X X X X X X X X X X X X X X X X X X 
$ 
```

### With modules (like `Data.Matrix`)
```
$ cabal update
$ cabal install --lib matrix
Resolving dependencies...
Build profile: -w ghc-8.10.7 -O1
In order, the following will be built (use -v for more details):
 - loop-0.3.0 (lib) (requires download & build)
 - primitive-0.7.4.0 (lib) (requires download & build)
 - semigroups-0.20 (lib) (requires download & build)
 - vector-0.12.3.1 (lib) (requires download & build)
 - matrix-0.3.6.1 (lib) (requires download & build)
Downloading  loop-0.3.0
Downloaded   loop-0.3.0
. . .
$
```
And then
```
$ ghc matrix[.hs] 
Loaded package environment from /Users/olediour/.ghc/x86_64-darwin-8.10.7/environments/default
[1 of 1] Compiling Main             ( matrix.hs, matrix.o )
Linking matrix ...
```
Now you can run
```
$ ./matrix
```
### ghci (like a REPL)
```
$ ghci cellularautomaton.hs 
GHCi, version 8.10.7: https://www.haskell.org/ghc/  :? for help
Loaded package environment from /Users/olediour/.ghc/x86_64-darwin-8.10.7/environments/default
[1 of 1] Compiling Main             ( cellularautomaton.hs, interpreted )
Ok, one module loaded.
*Main> main
                                X                                
                               X X                               
                              X   X                              
                             X X X X                             
                            X       X                            
                           X X     X X                           
                          X   X   X   X                          
                         X X X X X X X X                         
                        X               X                        
                       X X             X X                       
                      X   X           X   X                      
                     X X X X         X X X X                     
                    X       X       X       X                    
                   X X     X X     X X     X X                   
                  X   X   X   X   X   X   X   X                  
                 X X X X X X X X X X X X X X X X                 
                X                               X                
               X X                             X X               
              X   X                           X   X              
             X X X X                         X X X X             
            X       X                       X       X            
           X X     X X                     X X     X X           
          X   X   X   X                   X   X   X   X          
         X X X X X X X X                 X X X X X X X X         
        X               X               X               X        
       X X             X X             X X             X X       
      X   X           X   X           X   X           X   X      
     X X X X         X X X X         X X X X         X X X X     
    X       X       X       X       X       X       X       X    
   X X     X X     X X     X X     X X     X X     X X     X X   
  X   X   X   X   X   X   X   X   X   X   X   X   X   X   X   X  
 X X X X X X X X X X X X X X X X X X X X X X X X X X X X X X X X 
*Main> 
*Main> :load matrix
[1 of 1] Compiling Main             ( matrix.hs, interpreted )
Ok, one module loaded.
*Main> main
┌             ┐
│  1  2  3  4 │
│  5  6  7  8 │
│  9 10 11 12 │
└             ┘
. . .
*Main>  :quit
Leaving GHCi.
$
```

---
