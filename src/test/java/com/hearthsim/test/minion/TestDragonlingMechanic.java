package com.hearthsim.test.minion;

import com.hearthsim.card.Card;
import com.hearthsim.card.basic.minion.BoulderfistOgre;
import com.hearthsim.card.basic.minion.DragonlingMechanic;
import com.hearthsim.card.basic.minion.RaidLeader;
import com.hearthsim.exception.HSException;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.tree.HearthTreeNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestDragonlingMechanic {

    private HearthTreeNode board;
    private PlayerModel currentPlayer;
    private PlayerModel waitingPlayer;

    @Before
    public void setup() throws HSException {
        board = new HearthTreeNode(new BoardModel());
        currentPlayer = board.data_.getCurrentPlayer();
        waitingPlayer = board.data_.getWaitingPlayer();

        board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, new RaidLeader());
        board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, new BoulderfistOgre());

        board.data_.placeMinion(PlayerSide.WAITING_PLAYER, new RaidLeader());
        board.data_.placeMinion(PlayerSide.WAITING_PLAYER, new BoulderfistOgre());

        Card fb = new DragonlingMechanic();
        currentPlayer.placeCardHand(fb);

        currentPlayer.setMana((byte) 8);
        waitingPlayer.setMana((byte) 8);
    }

    @Test
    public void test0() throws HSException {
        Card theCard = currentPlayer.getHand().get(0);
        HearthTreeNode ret = theCard.useOn(PlayerSide.WAITING_PLAYER, 0, board);

        assertNull(ret);

        assertEquals(currentPlayer.getHand().size(), 1);
        assertEquals(currentPlayer.getNumMinions(), 2);
        assertEquals(waitingPlayer.getNumMinions(), 2);
        assertEquals(currentPlayer.getMana(), 8);
        assertEquals(waitingPlayer.getMana(), 8);
        assertEquals(currentPlayer.getHero().getHealth(), 30);
        assertEquals(waitingPlayer.getHero().getHealth(), 30);
        assertEquals(currentPlayer.getCharacter(1).getHealth(), 2);
        assertEquals(currentPlayer.getCharacter(2).getHealth(), 7);
        assertEquals(waitingPlayer.getCharacter(1).getHealth(), 2);
        assertEquals(waitingPlayer.getCharacter(2).getHealth(), 7);

        assertEquals(currentPlayer.getCharacter(1).getTotalAttack(), 2);
        assertEquals(currentPlayer.getCharacter(2).getTotalAttack(), 7);
        assertEquals(waitingPlayer.getCharacter(1).getTotalAttack(), 2);
        assertEquals(waitingPlayer.getCharacter(2).getTotalAttack(), 7);
    }

    @Test
    public void test2() throws HSException {
        Card theCard = currentPlayer.getHand().get(0);
        HearthTreeNode ret = theCard.useOn(PlayerSide.CURRENT_PLAYER, 1, board);

        assertNotNull(ret);
        currentPlayer = ret.data_.getCurrentPlayer();
        waitingPlayer = ret.data_.getWaitingPlayer();

        assertEquals(currentPlayer.getHand().size(), 0);
        assertEquals(currentPlayer.getNumMinions(), 4);
        assertEquals(waitingPlayer.getNumMinions(), 2);
        assertEquals(currentPlayer.getMana(), 4);
        assertEquals(waitingPlayer.getMana(), 8);
        assertEquals(currentPlayer.getHero().getHealth(), 30);
        assertEquals(waitingPlayer.getHero().getHealth(), 30);
        assertEquals(currentPlayer.getCharacter(1).getHealth(), 2);
        assertEquals(currentPlayer.getCharacter(2).getHealth(), 4);
        assertEquals(currentPlayer.getCharacter(3).getHealth(), 1);
        assertEquals(currentPlayer.getCharacter(4).getHealth(), 7);
        assertEquals(waitingPlayer.getCharacter(1).getHealth(), 2);
        assertEquals(waitingPlayer.getCharacter(2).getHealth(), 7);

        assertEquals(currentPlayer.getCharacter(1).getTotalAttack(), 2);
        assertEquals(currentPlayer.getCharacter(2).getTotalAttack(), 3);
        assertEquals(currentPlayer.getCharacter(3).getTotalAttack(), 3);
        assertEquals(currentPlayer.getCharacter(4).getTotalAttack(), 7);
        assertEquals(waitingPlayer.getCharacter(1).getTotalAttack(), 2);
        assertEquals(waitingPlayer.getCharacter(2).getTotalAttack(), 7);
    }

    @Test
    public void testBattlecryFizzlesOnFullBoard() throws HSException {
        board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, new BoulderfistOgre());
        board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, new BoulderfistOgre());
        board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, new BoulderfistOgre());
        board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, new BoulderfistOgre());

        Card theCard = currentPlayer.getHand().get(0);
        HearthTreeNode ret = theCard.useOn(PlayerSide.CURRENT_PLAYER, 3, board);

        assertNotNull(ret);
        currentPlayer = ret.data_.getCurrentPlayer();
        waitingPlayer = ret.data_.getWaitingPlayer();
        assertEquals(currentPlayer.getHand().size(), 0);
        assertEquals(currentPlayer.getNumMinions(), 7);

        assertEquals(currentPlayer.getMana(), 4);
        assertEquals(currentPlayer.getCharacter(4).getTotalAttack(), 3);
        assertEquals(currentPlayer.getCharacter(4).getTotalHealth(), 4);

        assertEquals(currentPlayer.getCharacter(5).getTotalAttack(), 7);
        assertEquals(currentPlayer.getCharacter(5).getTotalHealth(), 7);
    }
}
