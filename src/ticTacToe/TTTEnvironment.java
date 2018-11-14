package ticTacToe;

import java.util.ArrayList;
import java.util.List;

public class TTTEnvironment {
	
	/**
	 * A Tic Tac Toe Reinforcement Learning (RL) environment
	 */
	
	Game game;
	
	
	double winReward=10.0;
	double loseReward=-10.0;
	double livingReward=-1.00;
	double drawReward=0.0;
	
	public TTTEnvironment(Agent opponent)
	{
		game=new Game(new Agent(), opponent);
	}
	
	
	public Game getCurrentGameState()
	{
		return game;
	}
	
	public List<Move> getPossibleMoves()
	{
		List<Move> moves=new ArrayList<Move>();
		if (game.whoseTurn.getName()!='X')
			return moves;
		
		return game.getPossibleMoves();
		
	}
	
	/**
	 * performs action/move {@code m} and returns an environment outcome {@code o}.
	 * @param m
	 * @return
	 */
	public Outcome executeMove(Move m)
	{
		if (!game.isLegal(m))
			return null;
		
		try {
		//our move
			game.executeMove(m);
		}
		catch(IllegalMoveException e)
		{
			e.printStackTrace();
			
		}
		
		/*
		 * TODO
		 */
		//opponent's move
		try {
			game.executeMove(game.o.getMove(game));
		}
		catch(IllegalMoveException e)
		{
			e.printStackTrace();
		}
		/*
		 * TODO
		 */
		
		
		return null;
		
	}

}
