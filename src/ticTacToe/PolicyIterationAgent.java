package ticTacToe;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A policy iteration agent. You should implement the following methods:
 * (1) {@link PolicyIterationAgent#evaluatePolicy}: this is the policy evaluation step from your lectures
 * (2) {@link PolicyIterationAgent#improvePolicy}: this is the policy improvement step from your lectures
 * (3) {@link PolicyIterationAgent#train}: this is a method that should runs/alternate (1) and (2) until convergence. 
 * @author ae187
 *
 */
public class PolicyIterationAgent extends Agent {

	/**
	 * This map is used to store the values of states according to the current policy (policy evaluation). 
	 */
	HashMap<Game, Double> policyValues=new HashMap<Game, Double>();
	
	/**
	 * This stores the current policy as a map from {@link Game}s to {@link Move}. 
	 */
	HashMap<Game, Move> curPolicy=new HashMap<Game, Move>();
	
	double discount=0.9;
	
	/**
	 * The mdp model used
	 */
	TTTMDP mdp;
	
	/**
	 * loads the policy from file if one exists. Policies should be stored in .pol files directly under the project folder.
	 */
	public PolicyIterationAgent() {
		super();
		this.mdp=new TTTMDP();
		initValues();
		initRandomPolicy();
		train();
		
		
	}
	
	
	/**
	 * Use this constructor to initialise your agent with an existing policy
	 * @param p
	 */
	public PolicyIterationAgent(Policy p) {
		super(p);
		
	}

	/**
	 * Use this constructor to initialise a learning agent with default MDP paramters (rewards, transitions, etc) as specified in 
	 * {@link TTTMDP}
	 * @param discountFactor
	 */
	public PolicyIterationAgent(double discountFactor) {
		
		this.discount=discountFactor;
		this.mdp=new TTTMDP();
		initValues();
		initRandomPolicy();
		train();
	}
	/**
	 * Use this constructor to set the various parameters of the Tic-Tac-Toe MDP
	 * @param discountFactor
	 * @param winningReward
	 * @param losingReward
	 * @param livingReward
	 * @param drawReward
	 */
	public PolicyIterationAgent(double discountFactor, double winningReward, double losingReward, double livingReward, double drawReward)
	{
		this.discount=discountFactor;
		this.mdp=new TTTMDP(winningReward, losingReward, livingReward, drawReward);
		initValues();
		initRandomPolicy();
		train();
	}
	/**
	 * Initialises the {@link #policyValues} map, and sets the initial value of all states to 0 
	 * (V0 under some policy pi ({@link #curPolicy} from the lectures). Uses {@link Game#inverseHash} and {@link Game#generateAllValidGames(char)} to do this. 
	 * 
	 */
	public void initValues()
	{
		List<Game> allGames=Game.generateAllValidGames('X');//all valid games where it is X's turn, or it's terminal.
		for(Game g: allGames)
			this.policyValues.put(g, 0.0);
		
	}
	
	/**
	 *  You should implement this method to initially generate a random policy, i.e. fill the {@link #curPolicy} for every state. Take care that the moves you choose
	 *  for each state ARE VALID. You can use the {@link Game#getPossibleMoves()} method to get a list of valid moves and choose 
	 *  randomly between them. 
	 */
	public void initRandomPolicy()
	{
		for (Map.Entry<Game, Double> pair: this.policyValues.entrySet())
		{
			Game game = pair.getKey();
			Double value = pair.getValue();

			List<Move> possibleMoves = game.getPossibleMoves();
			if (!possibleMoves.isEmpty())
			{
				Move firstMove = possibleMoves.get(0);
				this.curPolicy.put(game,firstMove);
			}
		}
	}
	
	
	/**
	 * Performs policy evaluation steps until the maximum change in values is less than {@param delta}, in other words
	 * until the values under the currrent policy converge. After running this method, 
	 * the {@link PolicyIterationAgent#policyValues} map should contain the values of each reachable state under the current policy. 
	 * You should use the {@link TTTMDP} {@link PolicyIterationAgent#mdp} provided to do this.
	 *
	 * @param delta
	 */
	protected void evaluatePolicy(double delta)
	{
		int maxIter = 200;

		for (int i =0; i<maxIter;i++)
		{
			//break on some value change
			HashMap<Game,Double> changeValuesMap = new HashMap<Game,Double>();
			for (Map.Entry<Game, Double> pair: this.policyValues.entrySet())
			{
				Game game = pair.getKey();
				Double value = pair.getValue();

				//apply rewards
				switch (game.state)
				{
					case Game.DRAW:
						this.policyValues.put(game, this.mdp.winReward);
						continue;
					case Game.O_WON:
						this.policyValues.put(game,this.mdp.loseReward);
						continue;
					case Game.X_WON:
						this.policyValues.put(game,this.mdp.drawReward);
						continue;
				}
				Move moveUnderCurrentPolicy = this.curPolicy.get(game);

				double currentValue = 0D;
				for(TransitionProb transitionProb: mdp.generateTransitions(game,moveUnderCurrentPolicy)){
					double prob = transitionProb.prob;
					double localReward = transitionProb.outcome.localReward;
					double gamma = this.discount;
					double valuePrim = policyValues.get(transitionProb.outcome.sPrime);
					currentValue += prob * (localReward + gamma*valuePrim);
				}
				double previousValue = this.policyValues.get(game);
				this.policyValues.put(game,currentValue);
				double difference = Math.abs(previousValue - currentValue);
				changeValuesMap.put(game,difference);

			}
			if (!changeValuesMap.isEmpty()) {
				double maxChange = Collections.max(changeValuesMap.entrySet(), Map.Entry.comparingByValue()).getValue();
				if ( maxChange<delta)
				{
					break;
				}
			}
		}
		
	}
	
	/**This method should be run AFTER the {@link PolicyIterationAgent#evaluatePolicy} train method to extract a policy according to {@link PolicyIterationAgent#valueFuncion}
	 * You will need to do a single step of expectimax from each game (state) key in {@link PolicyIterationAgent#valueFuncion} 
	 * to extract a policy.
	 * 
	 * @return the policy according to {@link PolicyIterationAgent#valueFunction}
	 */
	protected void improvePolicy()
	{

		for (Map.Entry<Game, Double> pair: this.policyValues.entrySet()) {
			Game game = pair.getKey();

			Map<Move, Double> moveRankingMap = new HashMap<Move, Double>();
			for (Move possibleMove : game.getPossibleMoves())
			{
				double summation = 0D;
				for ( TransitionProb transitionProb : this.mdp.generateTransitions(game, possibleMove)){
					double prob = transitionProb.prob;
					double localReward = transitionProb.outcome.localReward;
					double gamma = this.discount;
					double valuePrim = policyValues.get(transitionProb.outcome.sPrime);
					summation += prob * (localReward + gamma*valuePrim);
				}
				moveRankingMap.put(possibleMove,summation);
			}
			if (!moveRankingMap.isEmpty()) {
				Map.Entry<Move, Double> bestPair = Collections.max(moveRankingMap.entrySet(), Map.Entry.comparingByValue());
				this.curPolicy.put(game, bestPair.getKey());
			}
		}
	}
	
	/**
	 * This method should perform policy evaluation and policy improvement steps until convergence (i.e. until the policy
	 * no longer changes), and so uses what your 
	 * {@link PolicyIterationAgent#evaluatePolicy} and {@link PolicyIterationAgent#improvePolicy} methods.
	 */
	public void train()
	{
		this.initValues();
		initRandomPolicy();
		int maxIter = 200;
		for (int i = 0; i < maxIter; i++)
		{
			this.evaluatePolicy(0.01);
			HashMap<Game, Move>  oldPolicy = new HashMap<Game, Move>(this.curPolicy);
			improvePolicy();
			if (curPolicy.equals(oldPolicy))
			{
				System.out.println("Policy converged at iteration: " + i);
				break;
			}
		}

		this.policy = new Policy();
		this.policy.policy = this.curPolicy;
		
	}
	

}
