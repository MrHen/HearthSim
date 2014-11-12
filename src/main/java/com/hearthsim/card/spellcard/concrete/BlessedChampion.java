package com.hearthsim.card.spellcard.concrete;

import com.hearthsim.card.Deck;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.spellcard.SpellCard;
import com.hearthsim.exception.HSException;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.tree.HearthTreeNode;

public class BlessedChampion extends SpellCard {

	/**
	 * Constructor
	 * 
	 * @param hasBeenUsed Whether the card has already been used or not
	 */
	public BlessedChampion(boolean hasBeenUsed) {
		super((byte)5, hasBeenUsed);
	}

	/**
	 * Constructor
	 * 
	 * Defaults to hasBeenUsed = false
	 */
	public BlessedChampion() {
		this(false);
	}

	@Override
	public SpellCard deepCopy() {
		return new BlessedChampion(this.hasBeenUsed);
	}
	
	@Override
	public boolean canBeUsedOn(PlayerSide playerSide, Minion minion, BoardModel boardModel) {
		if(!super.canBeUsedOn(playerSide, minion, boardModel)) {
			return false;
		}

		if (isHero(minion)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 
	 * Use the card on the given target
	 * 
	 * Double a minion's Attack
	 * 
	 *
     *
     * @param side
     * @param boardState The BoardState before this card has performed its action.  It will be manipulated and returned.
     *
     * @return The boardState is manipulated and returned
	 */
	@Override
	protected HearthTreeNode use_core(
			PlayerSide side,
			Minion targetMinion,
			HearthTreeNode boardState,
			Deck deckPlayer0,
			Deck deckPlayer1,
			boolean singleRealizationOnly)
		throws HSException
	{
		HearthTreeNode toRet = super.use_core(side, targetMinion, boardState, deckPlayer0, deckPlayer1, singleRealizationOnly);
		if (toRet != null) {
			targetMinion.setAttack((byte)(2 * targetMinion.getTotalAttack()));
		}
		return toRet;
	}
}
