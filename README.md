# Tictactoe AI with clojure

`lein run` runs a default AI vs AI simulation with an empty board.  
In repl (`lein repl`), you can play against the AI with the function `player`. The game expects you to input the index (0-8) of the square you want to play.  
You can simulate AI vs AI with different board states with the `play-game` -function. The function takes an initial state and starting turn as parameters. State is given with a string, for example: `"???xxo?o?"` Starting turn is an boolean, true on x's turn.


