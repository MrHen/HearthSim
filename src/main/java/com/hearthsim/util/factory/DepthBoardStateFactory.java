package com.hearthsim.util.factory;

import java.util.ArrayList;

import com.hearthsim.card.Deck;
import com.hearthsim.exception.HSException;
import com.hearthsim.player.playercontroller.BruteForceSearchAI;
import com.hearthsim.util.tree.CardDrawNode;
import com.hearthsim.util.tree.HearthTreeNode;
import com.hearthsim.util.tree.RandomEffectNode;
import com.hearthsim.util.tree.StopNode;

public class DepthBoardStateFactory extends BoardStateFactoryBase {

	protected boolean lethal_;
	protected boolean timedOut_;
	public final long maxTime_;

	protected long startTime_;
	protected long curTime_;

	protected double curScore_;

	/**
	 * Constructor
	 * maxThinkTime defaults to 10000 milliseconds (10 seconds)
	 */
	public DepthBoardStateFactory(Deck deckPlayer0, Deck deckPlayer1) {
		this(deckPlayer0, deckPlayer1, 10000);
	}

	/**
	 * Constructor
	 * 
	 * @param deckPlayer0
	 * @param deckPlayer1
	 * @param maxThinkTime The maximum amount of time in milliseconds the factory is allowed to spend on generating the simulation tree.
	 */
	public DepthBoardStateFactory(Deck deckPlayer0, Deck deckPlayer1, long maxThinkTime) {
		super(deckPlayer0, deckPlayer1);

		lethal_ = false;
		startTime_ = System.currentTimeMillis();
		curScore_ = -1.e200;
		maxTime_ = maxThinkTime;
		timedOut_ = false;
	}

	public DepthBoardStateFactory(Deck deckPlayer0, Deck deckPlayer1, long maxThinkTime,
			SparseChildNodeCreator sparseChildNodeCreator) {
		super(deckPlayer0, deckPlayer1, sparseChildNodeCreator);

		lethal_ = false;
		startTime_ = System.currentTimeMillis();
		curScore_ = -1.e200;
		maxTime_ = maxThinkTime;
		timedOut_ = false;
	}

	public boolean didTimeOut() {
		return timedOut_;
	}

	public void resetTimeOut() {
		startTime_ = System.currentTimeMillis();
		timedOut_ = false;
	}

	/**
	 * Recursively generate all possible moves
	 * This function recursively generates all possible moves that can be done starting from a given BoardState.
	 * While generating the moves, it applies the scoring function to each BoardState generated, and it will only keep the
	 * highest scoring branch.
	 * The results are stored in a tree structure and returned as a tree of BoardState class.
	 * 
	 * @param boardStateNode The initial BoardState wrapped in a HearthTreeNode.
	 * @param scoreFunc The scoring function for AI.
	 * @return boardStateNode manipulated such that all subsequent actions are children of the original boardStateNode input.
	 */
	public HearthTreeNode doMoves(HearthTreeNode boardStateNode, BruteForceSearchAI ai) throws HSException {
		log.trace("recursively performing moves");

		if(lethal_) {
			log.debug("found lethal");
			// if it's lethal, we don't have to do anything ever. Just play the lethal.
			return null;
		}

		if(System.currentTimeMillis() - startTime_ > maxTime_) {
			log.debug("setting think time over");
			timedOut_ = true;
		}

		if(timedOut_) {
			log.debug("think time is already over");
			// Time's up! no more thinking...
			return null;
		}

		boolean lethalFound = false;
		if(boardStateNode.data_.isLethalState()) { // one of the players is dead, no reason to keep playing
			lethal_ = true;
			lethalFound = true;
		}

		if(boardStateNode instanceof RandomEffectNode) {
			// Set best child score according to the random effect score. This works here since this effect "bubbles up" the tree.
			double boardScore = ((RandomEffectNode)boardStateNode).weightedAverageBestChildScore();
			boardStateNode.setScore(boardScore);
			boardStateNode.setBestChildScore(boardScore);
		} else {
			boardStateNode.setScore(ai.boardScore(boardStateNode.data_));
		}

		// We can end up with children at this state, for example, after a battle cry. If we don't have children yet, create them.
		if(!lethalFound && boardStateNode.numChildren() <= 0) {
			ArrayList<HearthTreeNode> nodes = this.createChildren(boardStateNode);
			boardStateNode.addChildren(nodes);
		}

		if(boardStateNode.isLeaf()) {
			// If at this point the node has no children, it is a leaf node. Set its best child score to its own score.
			boardStateNode.setBestChildScore(boardStateNode.getScore());
			boardStateNode.setNumNodesTries(1);
		} else {
			// If it is not a leaf, set the score as the maximum score of its children.
			// We can also throw out any children that don't have the highest score (boy, this sounds so wrong...)
			double tmpScore;
			double bestScore = 0;
			int tmpNumNodesTried = 0;
			HearthTreeNode bestBranch = null;

			for(HearthTreeNode child : boardStateNode.getChildren()) {
				this.doMoves(child, ai); // Don't need to check lethal because lethal states shouldn't get children. Even if they do, doMoves resolves the issue.

				tmpNumNodesTried += child.getNumNodesTried();
				tmpScore = child.getBestChildScore();

				// We need to add the card score after child scoring because CardDrawNode children
				// do not inherit the value of drawn cards
				// TODO Children of CardDrawNodes should be able to track this on their own. Doing
				// it this way "breaks" the best score chain and makes it harder to isolate and test
				// scoring. It effectively "bubbles down" the tree but it isn't sent through when
				// creating children.
				if(child instanceof CardDrawNode) {
					tmpScore += ((CardDrawNode)child).cardDrawScore(deckPlayer0_, ai);
				}

				if(bestBranch == null || tmpScore > bestScore) {
					bestBranch = child;
					bestScore = tmpScore;
				}
			}

			// TODO this should be automatically handled elsewhere...
			if(bestBranch instanceof StopNode) {
				bestBranch.clearChildren(); // cannot continue past a StopNode
			}
			boardStateNode.clearChildren();
			boardStateNode.addChild(bestBranch);
			boardStateNode.setBestChildScore(bestBranch.getBestChildScore());
			boardStateNode.setNumNodesTries(tmpNumNodesTried);
		}

		return boardStateNode;
	}
}
