# multiAgents.py
# --------------
# Licensing Information:  You are free to use or extend these projects for
# educational purposes provided that (1) you do not distribute or publish
# solutions, (2) you retain this notice, and (3) you provide clear
# attribution to UC Berkeley, including a link to http://ai.berkeley.edu.
#
# Attribution Information: The Pacman AI projects were developed at UC Berkeley.
# The core projects and autograders were primarily created by John DeNero
# (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# Student side autograding was added by Brad Miller, Nick Hay, and
# Pieter Abbeel (pabbeel@cs.berkeley.edu).


from util import manhattanDistance
from game import Directions
import random, util

from game import Agent

class ReflexAgent(Agent):
    """
    A reflex agent chooses an action at each choice point by examining
    its alternatives via a state evaluation function.

    The code below is provided as a guide.  You are welcome to change
    it in any way you see fit, so long as you don't touch our method
    headers.
    """


    def getAction(self, gameState):
        """
        You do not need to change this method, but you're welcome to.

        getAction chooses among the best options according to the evaluation function.

        Just like in the previous project, getAction takes a GameState and returns
        some Directions.X for some X in the set {NORTH, SOUTH, WEST, EAST, STOP}
        """
        # Collect legal moves and successor states
        legalMoves = gameState.getLegalActions()

        # Choose one of the best actions
        scores = [self.evaluationFunction(gameState, action) for action in legalMoves]
        bestScore = max(scores)
        bestIndices = [index for index in range(len(scores)) if scores[index] == bestScore]
        chosenIndex = random.choice(bestIndices) # Pick randomly among the best

        "Add more of your code here if you want to"

        return legalMoves[chosenIndex]

    def evaluationFunction(self, currentGameState, action):
        """
        Design a better evaluation function here.

        The evaluation function takes in the current and proposed successor
        GameStates (pacman.py) and returns a number, where higher numbers are better.

        The code below extracts some useful information from the state, like the
        remaining food (newFood) and Pacman position after moving (newPos).
        newScaredTimes holds the number of moves that each ghost will remain
        scared because of Pacman having eaten a power pellet.

        Print out these variables to see what you're getting, then combine them
        to create a masterful evaluation function.
        """
        # Useful information you can extract from a GameState (pacman.py)
        successorGameState = currentGameState.generatePacmanSuccessor(action)
        newPos = successorGameState.getPacmanPosition()
        newFood = successorGameState.getFood()
        newGhostStates = successorGameState.getGhostStates()
        newScaredTimes = [ghostState.scaredTimer for ghostState in newGhostStates]

        "*** YOUR CODE HERE ***"
        evaluated = 0
        food_list = newFood.asList()
        closest_food_length = 30
        closest_food_coords = []
        for i in food_list:
            if util.manhattanDistance(i, newPos) <= closest_food_length:
                closest_food_coords = i
                closest_food_length = util.manhattanDistance(i, newPos)
        evaluated -= closest_food_length
        if currentGameState.getNumFood() > successorGameState.getNumFood():
            evaluated += 100
        capsules = successorGameState.getCapsules()
        for i in newGhostStates:
            if util.manhattanDistance(newPos, i.getPosition()) <= 3:
                evaluated -= 200
        if newPos in capsules:
            evaluated += 200
        return evaluated

def scoreEvaluationFunction(currentGameState):
    """
    This default evaluation function just returns the score of the state.
    The score is the same one displayed in the Pacman GUI.

    This evaluation function is meant for use with adversarial search agents
    (not reflex agents).
    """
    return currentGameState.getScore()

class MultiAgentSearchAgent(Agent):
    """
    This class provides some common elements to all of your
    multi-agent searchers.  Any methods defined here will be available
    to the MinimaxPacmanAgent, AlphaBetaPacmanAgent & ExpectimaxPacmanAgent.

    You *do not* need to make any changes here, but you can if you want to
    add functionality to all your adversarial search agents.  Please do not
    remove anything, however.

    Note: this is an abstract class: one that should not be instantiated.  It's
    only partially specified, and designed to be extended.  Agent (game.py)
    is another abstract class.
    """

    def __init__(self, evalFn = 'scoreEvaluationFunction', depth = '2'):
        self.index = 0 # Pacman is always agent index 0
        self.evaluationFunction = util.lookup(evalFn, globals())
        self.depth = int(depth)

class MinimaxAgent(MultiAgentSearchAgent):
    """
    Your minimax agent (question 2)
    """

    def getAction(self, gameState):
        """
        Returns the minimax action from the current gameState using self.depth
        and self.evaluationFunction.

        Here are some method calls that might be useful when implementing minimax.

        gameState.getLegalActions(agentIndex):
        Returns a list of legal actions for an agent
        agentIndex=0 means Pacman, ghosts are >= 1

        gameState.generateSuccessor(agentIndex, action):
        Returns the successor game state after an agent takes an action

        gameState.getNumAgents():
        Returns the total number of agents in the game

        gameState.isWin():
        Returns whether or not the game state is a winning state

        gameState.isLose():
        Returns whether or not the game state is a losing state
        """
        "*** YOUR CODE HERE ***"
        numghosts = gameState.getNumAgents() - 1

        def min_function(state, agentid, depth):
            if depth == 0 or state.isWin() or state.isLose():
                return self.evaluationFunction(state)
            legal_moves = state.getLegalActions(agentid)
            result = 999
            for i in legal_moves:
                if agentid == numghosts:
                    move = state.generateSuccessor(agentid, i)
                    result = min(result, max_function(move, depth-1))
                else:
                    move = state.generateSuccessor(agentid, i)
                    result = min(result, min_function(move, agentid+1, depth))
            return result

        def max_function(state, depth):
            if state.isWin() or state.isLose() or depth == 0:
                return self.evaluationFunction(state)
            legal_moves = state.getLegalActions(0)
            result = -999
            for i in legal_moves:
                move = state.generateSuccessor(0, i)
                result = max(result, min_function(move, 1 ,depth))
            return result

        legal_moves = gameState.getLegalActions(0)
        result = -999
        final_action = Directions.STOP
        for i in legal_moves:
            move = gameState.generateSuccessor(0, i)
            newresult = max(result, min_function(move, 1, self.depth))
            if result < newresult:
                final_action = i
                result = newresult
        return final_action

class AlphaBetaAgent(MultiAgentSearchAgent):
    """
    Your minimax agent with alpha-beta pruning (question 3)
    """

    def getAction(self, gameState):
        """
        Returns the minimax action using self.depth and self.evaluationFunction
        """
        "*** YOUR CODE HERE ***"
        numghosts = gameState.getNumAgents() - 1

        def min_function(state, agentid, depth, maxi, mini):
            if depth == 0 or state.isWin() or state.isLose():
                return self.evaluationFunction(state)
            legal_moves = state.getLegalActions(agentid)
            result = 999
            for i in legal_moves:
                if agentid == numghosts:
                    move = state.generateSuccessor(agentid, i)
                    result = min(result, max_function(move, depth-1, maxi, mini))
                else:
                    move = state.generateSuccessor(agentid, i)
                    result = min(result, min_function(move, agentid+1, depth, maxi, mini))
                if result < maxi:
                    return result
                mini = min(mini, result)
            return result

        def max_function(state, depth, maxi, mini):
            if state.isWin() or state.isLose() or depth == 0:
                return self.evaluationFunction(state)
            legal_moves = state.getLegalActions(0)
            result = -999
            for i in legal_moves:
                move = state.generateSuccessor(0, i)
                result = max(result, min_function(move, 1 ,depth, maxi, mini))
                if result > mini:
                    return result
                maxi = (max(maxi, result))
            return result

        legal_moves = gameState.getLegalActions(0)
        result = -999
        mini = 999
        maxi = -999
        final_action = Directions.STOP
        for i in legal_moves:
            move = gameState.generateSuccessor(0, i)
            newresult = max(result, min_function(move, 1, self.depth, maxi, mini))
            if result < newresult:
                final_action = i
                result = newresult
            if result > mini:
                return final_action
            maxi = max(maxi, result)
        return final_action

class ExpectimaxAgent(MultiAgentSearchAgent):
    """
      Your expectimax agent (question 4)
    """

    def getAction(self, gameState):
        """
        Returns the expectimax action using self.depth and self.evaluationFunction

        All ghosts should be modeled as choosing uniformly at random from their
        legal moves.
        """
        "*** YOUR CODE HERE ***"
        numghosts = gameState.getNumAgents() - 1

        def expecti_function(state, agentid, depth):
            if depth == 0 or state.isWin() or state.isLose():
                return self.evaluationFunction(state)
            legal_moves = state.getLegalActions(agentid)
            result = 0
            possible_moves = [state.generateSuccessor(agentid, i) for i in legal_moves]
            for i in possible_moves:
                if agentid == numghosts:
                    result += max_function(i, depth-1)
                else:
                    result += expecti_function(i, agentid+1, depth)
            return float(result)/len(possible_moves)

        def max_function(state, depth):
            if state.isWin() or state.isLose() or depth == 0:
                return self.evaluationFunction(state)
            legal_moves = state.getLegalActions(0)
            result = -999
            for i in legal_moves:
                move = state.generateSuccessor(0, i)
                result = max(result, expecti_function(move, 1 ,depth))
            return result

        legal_moves = gameState.getLegalActions(0)
        result = -999
        final_action = Directions.STOP
        for i in legal_moves:
            move = gameState.generateSuccessor(0, i)
            newresult = expecti_function(move, 1, self.depth)
            if result < newresult:
                final_action = i
                result = newresult
        return final_action

def betterEvaluationFunction(currentGameState):
    """
    Your extreme ghost-hunting, pellet-nabbing, food-gobbling, unstoppable
    evaluation function (question 5).

    DESCRIPTION: Ghosts are bad, so avoid ghosts (if within range of ~3, drastically reduce result).
    Food is good, get food (subtract distance to closest food).
    Capsules are fantastic, prioritise capsules (subtract number of capsules left).
    If winning/losing state, add/subtract extremely large number.
    """
    "*** YOUR CODE HERE ***"
    evaluated = scoreEvaluationFunction(currentGameState)
    newFood = currentGameState.getFood()
    food_list = newFood.asList()

    closestfood = 99
    for i in food_list:
        thisdist = util.manhattanDistance(i, currentGameState.getPacmanPosition())
        if thisdist < closestfood:
            closestfood = thisdist
    ghostdist = 999
    for i in currentGameState.getGhostStates():
        nextdist = util.manhattanDistance(currentGameState.getPacmanPosition(), i.getPosition())
        if ghostdist > nextdist:
            ghostdist = nextdist
    evaluated += max(ghostdist, 3) * 2
    evaluated -= 2 * closestfood
    capsules = currentGameState.getCapsules()
    evaluated -= 4 * len(food_list)
    evaluated -= 5 * len(capsules)
    if currentGameState.isWin():
      return 999
    if currentGameState.isLose():
      return -999
    return evaluated


# Abbreviation
better = betterEvaluationFunction
