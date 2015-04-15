package com.hearthsim.card.minion.concrete;

import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.minion.MinionBattlecryInterface;
import com.hearthsim.event.effect.EffectCharacter;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.tree.HearthTreeNode;

/**
 * Created by oyachai on 3/21/15.
 */
public class DefiasRingleader extends Minion implements MinionBattlecryInterface {

    public DefiasRingleader() {
        super();
    }

    @Override
    public EffectCharacter<Minion> getBattlecryEffect() {
        return (PlayerSide originSide, Minion origin, PlayerSide targetSide, int minionPlacementIndex, HearthTreeNode boardState) -> {
            HearthTreeNode toRet = boardState;

            if (toRet.data_.getCurrentPlayer().isComboEnabled()) {
                Minion newMinion = new DefiasBandit();
                toRet = newMinion.summonMinion(PlayerSide.CURRENT_PLAYER, this, toRet, false, true);
            }
            return toRet;
        };
    }
}