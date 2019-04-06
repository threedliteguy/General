import TaflGame
import GameVariants
from random import randint



g = TaflGame.GameState()
#g = TaflGame.GameState(GameVariants.ArdRi())
#g = TaflGame.GameState(GameVariants.Tablut())
#g = TaflGame.GameState(GameVariants.Tawlbwrdd())
#g = TaflGame.GameState(GameVariants.Hnefatafl())
#g = TaflGame.GameState(GameVariants.AleaEvangelii())

print(g.board)
print(g.pieces)

g.render()

#exit()


# Random game
for i in range(0,1000000):
  pieceno = randint(0,len(g.pieces)-1)
  p = g.pieces[pieceno]
  r = -99
  lastplayer = g.time%2*2-1
  if p[2]*lastplayer < 0:
     if randint(0,2) == 0:
        r = g.moveByPieceNo(pieceno,p[0],randint(0,g.height-1))
     else:
        r = g.moveByPieceNo(pieceno,randint(0,g.width-1),p[1])

  if r >= 0: 
      g.render()
      print("=====")
      print(g.getValidMoves((g.time+1)%2*2-1))
      print("=====")

  if g.done: break



exit()



# Sample game:

print("Result:",g.move(3,2,4,2))
g.render()

print("Result:",g.move(3,1,2,1))
g.render()

print("Result:",g.move(3,3,3,2))
g.render()

print("Result:",g.move(3,0,4,0))
g.render()

print("Result:",g.move(3,2,3,0))
g.render()



#black win
#print("Result:",g.move(2,1,2,0))
#g.render()

#white win
print("Result:",g.move(2,1,2,2))
g.render()

print("Result:",g.move(3,0,0,0))
g.render()







