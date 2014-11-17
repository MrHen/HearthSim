package com.hearthsim.player.playercontroller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.hearthsim.exception.HSException;
import com.hearthsim.exception.HSInvalidParamFileException;
import com.hearthsim.exception.HSParamNotFoundException;
import com.hearthsim.io.ParamFile;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.util.HearthActionBoardPair;
import com.hearthsim.util.factory.BoardStateFactoryBase;
import com.hearthsim.util.factory.DepthBoardStateFactory;
import com.hearthsim.util.factory.SparseBoardStateFactory;
import com.hearthsim.util.tree.HearthTreeNode;
import com.hearthsim.util.tree.StopNode;

public class BruteForceSearchAI implements ArtificialPlayer {

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
    public final static int MAX_THINK_TIME = 20000;
	
	boolean useSparseBoardStateFactory_ = true;
	
	public WeightedScorer scorer = new WeightedScorer();

	protected BruteForceSearchAI() {}
	
    //TODO: come up with more meaningful names for these different AI 'styles'
    public static BruteForceSearchAI buildStandardAI2() {
        BruteForceSearchAI artificialPlayer = buildStandardAI1();
        artificialPlayer.scorer.setTauntWeight(0);
        artificialPlayer.scorer.setSpellDamageAddWeight(0.9);
        artificialPlayer.scorer.setSpellDamageMultiplierWeight(1);
        artificialPlayer.scorer.setMyDivineShieldWeight(1);
        artificialPlayer.scorer.setEnemyDivineShieldWeight(1);

        return artificialPlayer;
    }

    public static BruteForceSearchAI buildStandardAI1() {
        BruteForceSearchAI artificialPlayer = new BruteForceSearchAI();

        artificialPlayer.scorer.setMyAttackWeight(0.9);
        artificialPlayer.scorer.setMyHealthWeight(0.9);
        artificialPlayer.scorer.setEnemyAttackWeight(1.0);
        artificialPlayer.scorer.setEnemyHealthWeight(1.0);
        artificialPlayer.scorer.setTauntWeight(1.0);
        artificialPlayer.scorer.setMyHeroHealthWeight(0.1);
        artificialPlayer.scorer.setEnemyHeroHealthWeight(0.1);
        artificialPlayer.scorer.setManaWeight(0.1);
        artificialPlayer.scorer.setMyNumMinionsWeight(0.5);
        artificialPlayer.scorer.setEnemyNumMinionsWeight(0.5);
        artificialPlayer.scorer.setSpellDamageAddWeight(0.0);
        artificialPlayer.scorer.setSpellDamageMultiplierWeight(0.5);
        artificialPlayer.scorer.setMyDivineShieldWeight(0.0);
        artificialPlayer.scorer.setEnemyDivineShieldWeight(0.0);

        artificialPlayer.scorer.setMyWeaponWeight(0.5);
        artificialPlayer.scorer.setEnemyWeaponWeight(0.5);

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
			this.scorer.myAttackWeight = pFile.getDouble("w_a");
			this.scorer.myHealthWeight = pFile.getDouble("w_h");
			this.scorer.enemyAttackWeight = pFile.getDouble("wt_a");
			this.scorer.enemyHealthWeight = pFile.getDouble("wt_h");
			this.scorer.tauntWeight = pFile.getDouble("w_taunt");
			this.scorer.myHeroHealthWeight = pFile.getDouble("w_health");
			this.scorer.enemyHeroHealthWeight = pFile.getDouble("wt_health");

			this.scorer.myNumMinionsWeight = pFile.getDouble("w_num_minions");
			this.scorer.enemyNumMinionsWeight = pFile.getDouble("wt_num_minions");

			//The following two have default values for now... 
			//These are rather arcane parameters, so please understand 
			//them before attempting to change them. 
			this.scorer.spellDamageMultiplierWeight = pFile.getDouble("w_sd_mult", 1.0);
			this.scorer.spellDamageAddWeight = pFile.getDouble("w_sd_add", 0.9);

			//Divine Shield defualts to 0 for now
			this.scorer.myDivineShieldWeight = pFile.getDouble("w_divine_shield", 0.0);
			this.scorer.enemyDivineShieldWeight = pFile.getDouble("wt_divine_shield", 0.0);
			
			//weapon score for the hero
			this.scorer.myWeaponWeight = pFile.getDouble("w_weapon", 0.5);
			this.scorer.enemyWeaponWeight = pFile.getDouble("wt_weapon", 0.5);
			
			//charge model score
			this.scorer.myChargeWeight = pFile.getDouble("w_charge", 0.0);

			this.scorer.manaWeight = pFile.getDouble("w_mana", 0.1);
			
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
		
		HearthTreeNode allMoves = factory.doMoves(toRet, this.scorer);
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



	@Override
	public ArtificialPlayer deepCopy() {
		// TODO Auto-generated method stub
		BruteForceSearchAI copied = new BruteForceSearchAI();
		copied.scorer = this.scorer.deepCopy();
		copied.useSparseBoardStateFactory_ = useSparseBoardStateFactory_;
		return copied;
	}

	@Override
	public int getMaxThinkTime() {
		// TODO Auto-generated method stub
		return MAX_THINK_TIME;
	}
}
