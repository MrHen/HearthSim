package com.hearthsim.card.minion;

import com.hearthsim.card.Deck;
import com.hearthsim.card.ImplementedCardList;
import com.hearthsim.card.weapon.WeaponCard;
import com.hearthsim.exception.HSException;
import com.hearthsim.exception.HSInvalidPlayerIndexException;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.DeepCopyable;
import com.hearthsim.util.HearthAction;
import com.hearthsim.util.HearthAction.Verb;
import com.hearthsim.util.factory.BoardStateFactoryBase;
import com.hearthsim.util.tree.HearthTreeNode;

import org.json.JSONObject;

public abstract class Hero extends Minion {

	protected static final byte HERO_ABILITY_COST = 2; // Assumed to be 2 for all heroes

	protected WeaponCard weapon;
	protected byte armor_;

	public Hero() {
		ImplementedCardList cardList = ImplementedCardList.getInstance();
		ImplementedCardList.ImplementedCard implementedCard = cardList.getCardForClass(this.getClass());
		if(implementedCard != null) {
			this.name_ = implementedCard.name_;
			this.health_ = (byte)implementedCard.health_;
			this.attack_ = 0;
			this.baseHealth_ = health_;
			this.maxHealth_ = health_;
			this.heroTargetable_ = true;
		}
	}

	public byte getWeaponCharge() {
		if(weapon == null) {
			return 0;
		} else {
			return weapon.getWeaponCharge_();
		}
	}

	public void setWeaponCharge(byte weaponCharge) {
		if(weaponCharge == 0) {
			weapon = null;
		} else {
			weapon.setWeaponCharge_(weaponCharge);
		}
	}

	public void addArmor(byte armor) {
		armor_ += armor;
	}

	public byte getArmor() {
		return armor_;
	}

	public void setArmor(byte armor) {
		armor_ = armor;
	}

	@Override
	public Hero deepCopy() {
		Hero copy = (Hero)super.deepCopy();
		if(weapon != null) {
			copy.weapon = (WeaponCard)weapon.deepCopy();
		}
		copy.armor_ = armor_;

		return copy;
	}

	/**
	 * 
	 * Attack with the hero
	 * 
	 * A hero can only attack if it has a temporary buff, such as weapons
	 * 
	 *
	 *
	 * @param targetMinionPlayerSide
	 * @param targetMinion
	 *            The target minion
	 * @param boardState
	 *            The BoardState before this card has performed its action. It will be manipulated and returned.
	 * @param deckPlayer0
	 *            The deck of player0
	 * @return The boardState is manipulated and returned
	 */
	@Override
	public HearthTreeNode attack(PlayerSide targetMinionPlayerSide, Minion targetMinion, HearthTreeNode boardState,
			Deck deckPlayer0, Deck deckPlayer1) throws HSException {

		if(attack_ + extraAttackUntilTurnEnd_ == 0) {
			return null;
		}

		if(this.getWeapon() != null && this.getWeapon().getWeaponCharge_() == 0 && this.extraAttackUntilTurnEnd_ == 0) {
			return null;
		}

		// this is somewhat redundant, but it must be done here...
		if(frozen_) {
			this.hasAttacked_ = true;
			this.frozen_ = false;
			return boardState;
		}
		HearthTreeNode toRet = super.attack(targetMinionPlayerSide, targetMinion, boardState, deckPlayer0, deckPlayer1);
		if(toRet != null && this.getWeapon() != null) {
			this.weapon.onAttack(targetMinionPlayerSide, targetMinion, boardState, deckPlayer0, deckPlayer1);

			if(getWeaponCharge() > 0) {
				this.getWeapon().setWeaponCharge_((byte)(getWeaponCharge() - 1));
				if(getWeaponCharge() == 0) {
					destroyWeapon();
				}
			}

		}

		return toRet;
	}

	@Override
	public boolean canBeUsedOn(PlayerSide playerSide, Minion minion, BoardModel boardModel) {
		if(hasBeenUsed)
			return false;
		if(!minion.isHeroTargetable())
			return false;
		return true;
	}

	public final HearthTreeNode useHeroAbility(PlayerSide targetPlayerSide, Minion targetMinion,
			HearthTreeNode boardState, Deck deckPlayer0, Deck deckPlayer1) throws HSException {
		return this.useHeroAbility(targetPlayerSide, targetMinion, boardState, deckPlayer0, deckPlayer1, false);
	}

	/**
	 * Use the hero ability on a given target
	 * 
	 *
	 *
	 * @param targetPlayerSide
	 * @param targetMinion
	 *            The target minion
	 * @param boardState
	 * @param deckPlayer0
	 *            The deck of player0
	 * @return
	 */
	public final HearthTreeNode useHeroAbility(PlayerSide targetPlayerSide, Minion targetMinion,
			HearthTreeNode boardState, Deck deckPlayer0, Deck deckPlayer1, boolean singleRealizationOnly)
			throws HSException {

		if(boardState.data_.getCurrentPlayer().getMana() < HERO_ABILITY_COST)
			return null;

		if(this.hasBeenUsed())
			return null;

		HearthTreeNode toRet = this.useHeroAbility_core(targetPlayerSide, targetMinion, boardState, deckPlayer0,
				deckPlayer1, singleRealizationOnly);
		if(toRet != null) {
			int targetIndex = targetMinion instanceof Hero ? 0 : targetPlayerSide.getPlayer(boardState).getMinions()
					.indexOf(targetMinion);
			toRet.setAction(new HearthAction(Verb.HERO_ABILITY, PlayerSide.CURRENT_PLAYER, 0, targetPlayerSide,
					targetIndex));
			toRet = BoardStateFactoryBase.handleDeadMinions(toRet, deckPlayer0, deckPlayer1);
		}
		return toRet;
	}

	public HearthTreeNode useHeroAbility_core(PlayerSide targetPlayerSide, Minion targetMinion,
			HearthTreeNode boardState, Deck deckPlayer0, Deck deckPlayer1, boolean singleRealizationOnly)
			throws HSException {
		return null;
	}

	/**
	 * Called when this minion takes damage
	 * 
	 * Overridden from Minion. Need to handle armor.
	 * 
	 * @param damage
	 *            The amount of damage to take
	 * @param attackPlayerSide
	 *            The player index of the attacker. This is needed to do things like +spell damage.
	 * @param thisPlayerSide
	 * @param boardState
	 * @param deckPlayer0
	 *            The deck of player0
	 * @param isSpellDamage
	 * @throws HSInvalidPlayerIndexException
	 */
	@Override
	public HearthTreeNode takeDamage(byte damage, PlayerSide attackPlayerSide, PlayerSide thisPlayerSide,
			HearthTreeNode boardState, Deck deckPlayer0, Deck deckPlayer1, boolean isSpellDamage,
			boolean handleMinionDeath) throws HSException {
		HearthTreeNode toRet = boardState;
		byte damageRemaining = (byte)(damage - armor_);
		if(damageRemaining > 0) {
			armor_ = 0;
			toRet = super.takeDamage(damageRemaining, attackPlayerSide, thisPlayerSide, toRet, deckPlayer0,
					deckPlayer1, isSpellDamage, handleMinionDeath);
		} else {
			armor_ = (byte)(armor_ - damage);
		}
		return toRet;
	}

	/**
	 * End the turn and resets the card state
	 * 
	 * This function is called at the end of the turn. Any derived class must override it and remove any temporary buffs that it has.
	 */
	@Override
	public HearthTreeNode endTurn(PlayerSide thisMinionPlayerIndex, HearthTreeNode boardModel, Deck deckPlayer0,
			Deck deckPlayer1) throws HSException {
		this.extraAttackUntilTurnEnd_ = 0;
		return boardModel;
	}

	public JSONObject toJSON() {
		JSONObject json = super.toJSON();
		json.put("armor", this.armor_);
		json.put("weapon", this.weapon);
		return json;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;
		if(!super.equals(o))
			return false;

		Hero hero = (Hero)o;

		if(armor_ != hero.armor_)
			return false;
		if(weapon != null ? !weapon.equals(hero.weapon) : hero.weapon != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (weapon != null ? weapon.hashCode() : 0);
		result = 31 * result + (int)armor_;
		return result;
	}

	public void setWeapon(WeaponCard weapon) {
		if(weapon == null) {
			throw new RuntimeException("use 'destroy weapon' method if trying to remove weapon.");
		} else {
			this.weapon = weapon;
			this.attack_ = weapon.getWeaponDamage_();
		}
	}

	public WeaponCard getWeapon() {
		return weapon;
	}

	public void destroyWeapon() {
		if(weapon != null) {
			attack_ = 0;
			weapon = null;
		}
	}

	@Override
	public HearthTreeNode minionSummonEvent(PlayerSide thisMinionPlayerSide, PlayerSide summonedMinionPlayerSide,
			Minion summonedMinion, HearthTreeNode boardState, Deck deckPlayer0, Deck deckPlayer1)
			throws HSInvalidPlayerIndexException {
		HearthTreeNode hearthTreeNode = super.minionSummonEvent(thisMinionPlayerSide, summonedMinionPlayerSide,
				summonedMinion, boardState, deckPlayer0, deckPlayer1);
		if(weapon != null) {
			weapon.minionSummonedEvent(thisMinionPlayerSide, summonedMinionPlayerSide, summonedMinion, boardState,
					deckPlayer0, deckPlayer1);
		}
		return hearthTreeNode;
	}
}
