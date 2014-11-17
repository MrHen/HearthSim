package com.hearthsim.player.playercontroller;

import com.hearthsim.card.Card;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.spellcard.SpellDamage;
import com.hearthsim.model.BoardModel;
import com.hearthsim.util.DeepCopyable;
import com.hearthsim.util.IdentityLinkedList;

public class WeightedScorer implements BoardScorer, DeepCopyable<WeightedScorer> {

	private double myAttackWeight; // weight for the attack score
	private double myHealthWeight;
	private double enemyAttackWeight; // weight for the attack score
	private double enemyHealthWeight;
	private double myHeroHealthWeight;
	private double enemyHeroHealthWeight;
	private double tauntWeight;
	private double manaWeight;
	private double myNumMinionsWeight;
	private double enemyNumMinionsWeight;
	private double spellDamageAddWeight;
	private double spellDamageMultiplierWeight;
	private double myDivineShieldWeight;
	private double enemyDivineShieldWeight;
	private double myWeaponWeight;
	private double enemyWeaponWeight;
	private double myChargeWeight;

	public WeightedScorer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double boardScore(BoardModel board) {
		IdentityLinkedList<Minion> myBoardCards;
		IdentityLinkedList<Minion> opBoardCards;
		IdentityLinkedList<Card> myHandCards;
		myBoardCards = board.getCurrentPlayer().getMinions();
		opBoardCards = board.getWaitingPlayer().getMinions();
		myHandCards = board.getCurrentPlayerHand();

		// my board score
		double myScore = 0.0;
		for(final Minion minion : myBoardCards) {
			myScore += minion.getAttack() * myAttackWeight;
			myScore += minion.getTotalHealth() * myHealthWeight;
			myScore += (minion.getTaunt() ? 1.0 : 0.0) * tauntWeight;
			if(minion.getDivineShield())
				myScore += (minion.getAttack() + minion.getTotalHealth()) * myDivineShieldWeight;
		}

		// opponent board score
		double opScore = 0.0;
		for(final Minion minion : opBoardCards) {
			opScore += minion.getAttack() * enemyAttackWeight;
			opScore += minion.getTotalHealth() * enemyHealthWeight;
			opScore += (minion.getTaunt() ? 1.0 : 0.0) * tauntWeight;
			if(minion.getDivineShield())
				opScore += (minion.getAttack() + minion.getTotalHealth()) * enemyDivineShieldWeight;
		}

		// weapons
		double weaponScore = 0.0;
		weaponScore += board.getCurrentPlayerHero().getAttack() * board.getCurrentPlayerHero().getWeaponCharge()
				* myWeaponWeight;
		weaponScore -= board.getWaitingPlayerHero().getAttack() * board.getWaitingPlayerHero().getWeaponCharge()
				* enemyWeaponWeight;

		// my cards. The more cards that I have, the better
		double handScore = 0.0;
		for(final Card card : myHandCards) {
			handScore += this.cardInHandScore(card);
		}

		// the more we beat on the opponent hero, the better
		double heroScore = 0;
		heroScore += heroHealthScore_p0(board.getCurrentPlayerHero().getHealth(), board.getCurrentPlayerHero()
				.getArmor());
		heroScore += heroHealthScore_p1(board.getWaitingPlayerHero().getHealth(), board.getWaitingPlayerHero()
				.getArmor());

		// the more minions you have, the better. The less minions the enemy has, the better
		double minionScore = 0.0;
		minionScore += myNumMinionsWeight * (board.getCurrentPlayer().getNumMinions());
		minionScore -= enemyNumMinionsWeight * (board.getWaitingPlayer().getNumMinions());

		double score = myScore - opScore + handScore + heroScore + minionScore + weaponScore;

		return score;
	}

	/**
	 * Returns the card score for a particular card assuming that it is in the hand
	 * 
	 * @param card
	 * @return
	 */
	@Override
	public double cardInHandScore(Card card) {
		double theScore = 0.001; // need non-zero so the AI values TheCoin and Innervate
		if(card instanceof SpellDamage) {
			theScore += ((SpellDamage)card).getAttack() * spellDamageMultiplierWeight + spellDamageAddWeight;
		} else if(card instanceof Minion) {
			// Charge modeling. Charge's value primarily comes from the fact that it can be used immediately upon placing it.
			// After the card is placed, it's really just like any other minion, except maybe for small value in bouncing it.
			// So, the additional score for charge minions should really only apply when it is still in the hand.
			Minion minion = (Minion)card;
			theScore += card.getMana() * manaWeight + (minion.getCharge() ? myChargeWeight : 0.0);
		} else
			theScore += card.getMana() * manaWeight;
		return theScore;
	}

	@Override
	public double heroHealthScore_p0(double heroHealth, double heroArmor) {
		double toRet = myHeroHealthWeight * (heroHealth + heroArmor);
		if(heroHealth <= 0) {
			// dead enemy hero is a very good thing
			toRet -= 100000000.0;
		}
		return toRet;
	}

	@Override
	public double heroHealthScore_p1(double heroHealth, double heroArmor) {
		double toRet = -enemyHeroHealthWeight * (heroHealth + heroArmor);
		if(heroHealth <= 0) {
			// dead enemy hero is a very good thing
			toRet += 100000.0;
		}
		return toRet;
	}

	public double getMyChargeWeight() {
		return myChargeWeight;
	}

	public void setMyChargeWeight(double myChargeWeight) {
		this.myChargeWeight = myChargeWeight;
	}

	public double getMyAttackWeight() {
		return myAttackWeight;
	}

	public void setMyAttackWeight(double myAttackWeight) {
		this.myAttackWeight = myAttackWeight;
	}

	public double getMyHealthWeight() {
		return myHealthWeight;
	}

	public void setMyHealthWeight(double myHealthWeight) {
		this.myHealthWeight = myHealthWeight;
	}

	public double getEnemyAttackWeight() {
		return enemyAttackWeight;
	}

	public void setEnemyAttackWeight(double enemyAttackWeight) {
		this.enemyAttackWeight = enemyAttackWeight;
	}

	public double getEnemyHealthWeight() {
		return enemyHealthWeight;
	}

	public void setEnemyHealthWeight(double enemyHealthWeight) {
		this.enemyHealthWeight = enemyHealthWeight;
	}

	public double getMyHeroHealthWeight() {
		return myHeroHealthWeight;
	}

	public void setMyHeroHealthWeight(double myHeroHealthWeight) {
		this.myHeroHealthWeight = myHeroHealthWeight;
	}

	public double getEnemyHeroHealthWeight() {
		return enemyHeroHealthWeight;
	}

	public void setEnemyHeroHealthWeight(double enemyHeroHealthWeight) {
		this.enemyHeroHealthWeight = enemyHeroHealthWeight;
	}

	public double getTauntWeight() {
		return tauntWeight;
	}

	public void setTauntWeight(double tauntWeight) {
		this.tauntWeight = tauntWeight;
	}

	public double getManaWeight() {
		return manaWeight;
	}

	public void setManaWeight(double manaWeight) {
		this.manaWeight = manaWeight;
	}

	public double getMyNumMinionsWeight() {
		return myNumMinionsWeight;
	}

	public void setMyNumMinionsWeight(double myNumMinionsWeight) {
		this.myNumMinionsWeight = myNumMinionsWeight;
	}

	public double getEnemyNumMinionsWeight() {
		return enemyNumMinionsWeight;
	}

	public void setEnemyNumMinionsWeight(double enemyNumMinionsWeight) {
		this.enemyNumMinionsWeight = enemyNumMinionsWeight;
	}

	public double getSpellDamageAddWeight() {
		return spellDamageAddWeight;
	}

	public void setSpellDamageAddWeight(double spellDamageAddWeight) {
		this.spellDamageAddWeight = spellDamageAddWeight;
	}

	public double getSpellDamageMultiplierWeight() {
		return spellDamageMultiplierWeight;
	}

	public void setSpellDamageMultiplierWeight(double spellDamageMultiplierWeight) {
		this.spellDamageMultiplierWeight = spellDamageMultiplierWeight;
	}

	public double getMyDivineShieldWeight() {
		return myDivineShieldWeight;
	}

	public void setMyDivineShieldWeight(double myDivineShieldWeight) {
		this.myDivineShieldWeight = myDivineShieldWeight;
	}

	public double getEnemyDivineShieldWeight() {
		return enemyDivineShieldWeight;
	}

	public void setEnemyDivineShieldWeight(double enemyDivineShieldWeight) {
		this.enemyDivineShieldWeight = enemyDivineShieldWeight;
	}

	public double getMyWeaponWeight() {
		return myWeaponWeight;
	}

	public void setMyWeaponWeight(double myWeaponWeight) {
		this.myWeaponWeight = myWeaponWeight;
	}

	public double getEnemyWeaponWeight() {
		return enemyWeaponWeight;
	}

	public void setEnemyWeaponWeight(double enemyWeaponWeight) {
		this.enemyWeaponWeight = enemyWeaponWeight;
	}

	@Override
	public WeightedScorer deepCopy() {
		WeightedScorer copied = new WeightedScorer();
		copied.myAttackWeight = myAttackWeight; // weight for the attack score
		copied.myHealthWeight = myHealthWeight;
		copied.enemyAttackWeight = enemyAttackWeight; // weight for the attack score
		copied.enemyHealthWeight = enemyHealthWeight;
		copied.myHeroHealthWeight = myHeroHealthWeight;
		copied.enemyHeroHealthWeight = enemyHeroHealthWeight;
		copied.tauntWeight = tauntWeight;
		copied.manaWeight = manaWeight;
		copied.myNumMinionsWeight = myNumMinionsWeight;
		copied.enemyNumMinionsWeight = enemyNumMinionsWeight;
		copied.spellDamageAddWeight = spellDamageAddWeight;
		copied.spellDamageMultiplierWeight = spellDamageMultiplierWeight;
		copied.myDivineShieldWeight = myDivineShieldWeight;
		copied.enemyDivineShieldWeight = enemyDivineShieldWeight;
		copied.myWeaponWeight = myWeaponWeight;
		copied.enemyWeaponWeight = enemyWeaponWeight;
		copied.myChargeWeight = myChargeWeight;
		return copied;
	}
}
