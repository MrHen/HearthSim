package com.hearthsim.player.playercontroller;

import com.hearthsim.card.Card;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.spellcard.SpellDamage;
import com.hearthsim.exception.HSException;
import com.hearthsim.exception.HSInvalidParamFileException;
import com.hearthsim.exception.HSParamNotFoundException;
import com.hearthsim.io.ParamFile;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.util.HearthActionBoardPair;
import com.hearthsim.util.IdentityLinkedList;
import com.hearthsim.util.factory.BoardStateFactoryBase;
import com.hearthsim.util.factory.DepthBoardStateFactory;
import com.hearthsim.util.factory.SparseBoardStateFactory;
import com.hearthsim.util.tree.HearthTreeNode;
import com.hearthsim.util.tree.StopNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BruteForceSearchAI implements ArtificialPlayer, BoardScorer {

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
    public final static int MAX_THINK_TIME = 20000;
	
	double myAttackWeight; //weight for the attack score
	double myHealthWeight;
	double enemyAttackWeight; //weight for the attack score
	double enemyHealthWeight;
	double myHeroHealthWeight;
	double enemyHeroHealthWeight;
	double tauntWeight;
	double manaWeight;
	double myNumMinionsWeight;
	double enemyNumMinionsWeight;
	double spellDamageAddWeight;
	double spellDamageMultiplierWeight;
	double myDivineShieldWeight;
	double enemyDivineShieldWeight;
	double myWeaponWeight;
	double enemyWeaponWeight;
	double myChargeWeight;
	boolean useSparseBoardStateFactory_ = true;

	protected BruteForceSearchAI() {}
	
    //TODO: come up with more meaningful names for these different AI 'styles'
    public static BruteForceSearchAI buildStandardAI2() {
        BruteForceSearchAI artificialPlayer = buildStandardAI1();
        artificialPlayer.setTauntWeight(0);
        artificialPlayer.setSpellDamageAddWeight(0.9);
        artificialPlayer.setSpellDamageMultiplierWeight(1);
        artificialPlayer.setMyDivineShieldWeight(1);
        artificialPlayer.setEnemyDivineShieldWeight(1);

        return artificialPlayer;
    }

    public static BruteForceSearchAI buildStandardAI1() {
        BruteForceSearchAI artificialPlayer = new BruteForceSearchAI();

        artificialPlayer.setMyAttackWeight(0.9);
        artificialPlayer.setMyHealthWeight(0.9);
        artificialPlayer.setEnemyAttackWeight(1.0);
        artificialPlayer.setEnemyHealthWeight(1.0);
        artificialPlayer.setTauntWeight(1.0);
        artificialPlayer.setMyHeroHealthWeight(0.1);
        artificialPlayer.setEnemyHeroHealthWeight(0.1);
        artificialPlayer.setManaWeight(0.1);
        artificialPlayer.setMyNumMinionsWeight(0.5);
        artificialPlayer.setEnemyNumMinionsWeight(0.5);
        artificialPlayer.setSpellDamageAddWeight(0.0);
        artificialPlayer.setSpellDamageMultiplierWeight(0.5);
        artificialPlayer.setMyDivineShieldWeight(0.0);
        artificialPlayer.setEnemyDivineShieldWeight(0.0);

        artificialPlayer.setMyWeaponWeight(0.5);
        artificialPlayer.setEnemyWeaponWeight(0.5);

        return artificialPlayer;
    }


	/**
	 * Constructor
	 * 
	 * This is the preferred (non-deprecated) way to instantiate this class
	 * 
	 * @param aiParamFile The path to the input parameter file
	 * @throws IOException
	 * @throws HSInvalidParamFileException
	 */
	public BruteForceSearchAI(Path aiParamFile) throws IOException, HSInvalidParamFileException {
		ParamFile pFile = new ParamFile(aiParamFile);
		try {
			myAttackWeight = pFile.getDouble("w_a");
			myHealthWeight = pFile.getDouble("w_h");
			enemyAttackWeight = pFile.getDouble("wt_a");
			enemyHealthWeight = pFile.getDouble("wt_h");
			tauntWeight = pFile.getDouble("w_taunt");
			myHeroHealthWeight = pFile.getDouble("w_health");
			enemyHeroHealthWeight = pFile.getDouble("wt_health");

			myNumMinionsWeight = pFile.getDouble("w_num_minions");
			enemyNumMinionsWeight = pFile.getDouble("wt_num_minions");

			//The following two have default values for now... 
			//These are rather arcane parameters, so please understand 
			//them before attempting to change them. 
			spellDamageMultiplierWeight = pFile.getDouble("w_sd_mult", 1.0);
			spellDamageAddWeight = pFile.getDouble("w_sd_add", 0.9);

			//Divine Shield defualts to 0 for now
			myDivineShieldWeight = pFile.getDouble("w_divine_shield", 0.0);
			enemyDivineShieldWeight = pFile.getDouble("wt_divine_shield", 0.0);
			
			//weapon score for the hero
			myWeaponWeight = pFile.getDouble("w_weapon", 0.5);
			enemyWeaponWeight = pFile.getDouble("wt_weapon", 0.5);
			
			//charge model score
			myChargeWeight = pFile.getDouble("w_charge", 0.0);

			manaWeight = pFile.getDouble("w_mana", 0.1);
			
			useSparseBoardStateFactory_ = pFile.getBoolean("use_sparse_board_state_factory", true);
			
		} catch (HSParamNotFoundException e) {
			log.error(e.getMessage());
			System.exit(1);
		}
	}
	
	public boolean getUseSparseBoardStateFactory() {
		return useSparseBoardStateFactory_;
	}

	public  void setUseSparseBoardStateFactory(boolean value) {
		useSparseBoardStateFactory_ = value;
	}
	
	/**
	 * Returns the card score for a particular card assuming that it is in the hand
	 * 
	 * @param card
	 * @return
	 */
	public double cardInHandScore(Card card) {
		double theScore = 0.001; // need non-zero so the AI values TheCoin and Innervate
		if (card instanceof SpellDamage) {
			theScore += ((SpellDamage)card).getAttack() * spellDamageMultiplierWeight + spellDamageAddWeight;
		} else if (card instanceof Minion) {
			//Charge modeling.  Charge's value primarily comes from the fact that it can be used immediately upon placing it.
			//After the card is placed, it's really just like any other minion, except maybe for small value in bouncing it.
			//So, the additional score for charge minions should really only apply when it is still in the hand.
			Minion minion = (Minion)card;
			theScore += card.getMana() * manaWeight + (minion.getCharge() ? myChargeWeight : 0.0);
		} else
			theScore += card.getMana() * manaWeight;
		return theScore;
	}
	
	public double heroHealthScore_p0(double heroHealth, double heroArmor) {
		double toRet = myHeroHealthWeight * (heroHealth + heroArmor);
		if (heroHealth <= 0) {
			//dead enemy hero is a very good thing
			toRet -= 100000000.0;
		}
		return toRet;
	}
	
	public double heroHealthScore_p1(double heroHealth, double heroArmor) {
		double toRet = -enemyHeroHealthWeight * (heroHealth + heroArmor);
		if (heroHealth <= 0) {
			//dead enemy hero is a very good thing
			toRet += 100000.0;
		}
		return toRet;
	}
	
	public double boardScore(BoardModel board) {

		IdentityLinkedList<Minion> myBoardCards;
		IdentityLinkedList<Minion> opBoardCards;
		IdentityLinkedList<Card> myHandCards;
		myBoardCards = board.getCurrentPlayer().getMinions();
		opBoardCards = board.getWaitingPlayer().getMinions();
		myHandCards = board.getCurrentPlayerHand();
		
		//my board score
		double myScore = 0.0;
		for (final Minion minion: myBoardCards) {
			myScore += minion.getAttack() * myAttackWeight;
			myScore += minion.getTotalHealth() * myHealthWeight;
			myScore += (minion.getTaunt() ? 1.0 : 0.0) * tauntWeight;
			if (minion.getDivineShield())
                myScore += (minion.getAttack() + minion.getTotalHealth()) * myDivineShieldWeight;
		}
				
		//opponent board score
		double opScore = 0.0;
		for (final Minion minion: opBoardCards) {
			opScore += minion.getAttack() * enemyAttackWeight;
			opScore += minion.getTotalHealth() * enemyHealthWeight;
			opScore += (minion.getTaunt() ? 1.0 : 0.0) * tauntWeight;
			if (minion.getDivineShield()) opScore += (minion.getAttack() + minion.getTotalHealth()) * enemyDivineShieldWeight;
		}
		
		//weapons
		double weaponScore = 0.0;
		weaponScore += board.getCurrentPlayerHero().getAttack() * board.getCurrentPlayerHero().getWeaponCharge() * myWeaponWeight;
		weaponScore -= board.getWaitingPlayerHero().getAttack() * board.getWaitingPlayerHero().getWeaponCharge() * enemyWeaponWeight;
		
		//my cards.  The more cards that I have, the better
		double handScore = 0.0;
		for (final Card card: myHandCards) {
			handScore += this.cardInHandScore(card);
		}
		
		//the more we beat on the opponent hero, the better
		double heroScore = 0;
		heroScore += heroHealthScore_p0(board.getCurrentPlayerHero().getHealth(), board.getCurrentPlayerHero().getArmor());
		heroScore += heroHealthScore_p1(board.getWaitingPlayerHero().getHealth(), board.getWaitingPlayerHero().getArmor());
		
		//the more minions you have, the better.  The less minions the enemy has, the better
		double minionScore = 0.0;
		minionScore += myNumMinionsWeight * (board.getCurrentPlayer().getNumMinions());
		minionScore -= enemyNumMinionsWeight * (board.getWaitingPlayer().getNumMinions());
		
		double score = myScore - opScore + handScore + heroScore + minionScore + weaponScore;
		
		return score;
	}

	public List<HearthActionBoardPair> playTurn(int turn, BoardModel board) throws HSException {
		PlayerModel playerModel0 = board.getCurrentPlayer();
		PlayerModel playerModel1 = board.getWaitingPlayer();
		
		BoardStateFactoryBase factory = null;
		if (useSparseBoardStateFactory_) {
			factory = new SparseBoardStateFactory(playerModel0.getDeck(), playerModel1.getDeck(), MAX_THINK_TIME);
		} else {
			factory = new DepthBoardStateFactory(playerModel0.getDeck(), playerModel1.getDeck(), MAX_THINK_TIME);
		}
		return this.playTurn(turn, board, factory);
	}
	
	public List<HearthActionBoardPair> playTurn(int turn, BoardModel board, BoardStateFactoryBase factory) throws HSException {
		PlayerModel playerModel0 = board.getCurrentPlayer();
		PlayerModel playerModel1 = board.getWaitingPlayer();

		log.debug("playing turn for " + playerModel0.getName());
        //The goal of this ai is to maximize his board score
        log.debug("start turn board state is {}", board);
		HearthTreeNode toRet = new HearthTreeNode(board);		
		
		HearthTreeNode allMoves = factory.doMoves(toRet, this);
		ArrayList<HearthActionBoardPair> retList = new ArrayList<HearthActionBoardPair>();
		HearthTreeNode curMove = allMoves;
		
		while (curMove.getChildren() != null) {
			curMove = curMove.getChildren().get(0);
			if (curMove instanceof StopNode) {
				HearthTreeNode allEffectsDone = ((StopNode)curMove).finishAllEffects(playerModel0.getDeck(), playerModel1.getDeck());
				List<HearthActionBoardPair> nextMoves = this.playTurn(turn, allEffectsDone.data_);
				if(nextMoves.size() == 0) {
					// if no further actions can be taken, save the result after the stop node has resolved
					retList.add(new HearthActionBoardPair(null, allEffectsDone.data_));
				} else {
					for( HearthActionBoardPair actionBoard : nextMoves) {
						retList.add(actionBoard);
					}
				}
				break;
			} else {
				retList.add(new HearthActionBoardPair(curMove.getAction(), curMove.data_));
			}
		}
		return retList;
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
	public ArtificialPlayer deepCopy() {
		// TODO Auto-generated method stub
		BruteForceSearchAI copied = new BruteForceSearchAI();
		copied.myAttackWeight = myAttackWeight; //weight for the attack score
		copied.myHealthWeight = myHealthWeight;
		copied.enemyAttackWeight = enemyAttackWeight; //weight for the attack score
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
		copied.useSparseBoardStateFactory_ = useSparseBoardStateFactory_;
		return copied;
	}

	@Override
	public int getMaxThinkTime() {
		// TODO Auto-generated method stub
		return MAX_THINK_TIME;
	}
}
