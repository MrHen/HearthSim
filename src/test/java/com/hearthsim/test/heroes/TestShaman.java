package com.hearthsim.test.heroes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.hearthsim.card.Card;
import com.hearthsim.card.Deck;
import com.hearthsim.card.minion.Hero;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.minion.concrete.BoulderfistOgre;
import com.hearthsim.card.minion.concrete.HealingTotem;
import com.hearthsim.card.minion.concrete.RaidLeader;
import com.hearthsim.card.minion.concrete.SearingTotem;
import com.hearthsim.card.minion.concrete.SilverHandRecruit;
import com.hearthsim.card.minion.concrete.StoneclawTotem;
import com.hearthsim.card.minion.concrete.WrathOfAirTotem;
import com.hearthsim.card.minion.heroes.Shaman;
import com.hearthsim.card.minion.heroes.TestHero;
import com.hearthsim.card.spellcard.concrete.TheCoin;
import com.hearthsim.card.spellcard.concrete.WildGrowth;
import com.hearthsim.exception.HSException;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.player.playercontroller.BruteForceSearchAI;
import com.hearthsim.util.HearthActionBoardPair;
import com.hearthsim.util.tree.HearthTreeNode;
import com.hearthsim.util.tree.RandomEffectNode;

public class TestShaman {

	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

	private HearthTreeNode board;
	private Deck deck;

	@Before
	public void setup() throws HSException {
		board = new HearthTreeNode(new BoardModel(new Shaman(), new TestHero()));

		Minion minion0_0 = new BoulderfistOgre();
		Minion minion0_1 = new RaidLeader();
		Minion minion1_0 = new BoulderfistOgre();
		Minion minion1_1 = new RaidLeader();

		board.data_.placeCardHandCurrentPlayer(minion0_0);
		board.data_.placeCardHandCurrentPlayer(minion0_1);

		board.data_.placeCardHandWaitingPlayer(minion1_0);
		board.data_.placeCardHandWaitingPlayer(minion1_1);

		Card cards[] = new Card[30];
		for(int index = 0; index < 30; ++index) {
			cards[index] = new TheCoin();
		}

		deck = new Deck(cards);

		Card fb = new WildGrowth();
		board.data_.placeCardHandCurrentPlayer(fb);

		board.data_.getCurrentPlayer().setMana((byte)8);
		board.data_.getWaitingPlayer().setMana((byte)8);

		board.data_.getCurrentPlayer().setMaxMana((byte)8);
		board.data_.getWaitingPlayer().setMaxMana((byte)8);

		HearthTreeNode tmpBoard = new HearthTreeNode(board.data_.flipPlayers());
		tmpBoard.data_.getCurrentPlayerCardHand(0).useOn(PlayerSide.CURRENT_PLAYER,
				tmpBoard.data_.getCurrentPlayerHero(), tmpBoard, deck, null);
		tmpBoard.data_.getCurrentPlayerCardHand(0).useOn(PlayerSide.CURRENT_PLAYER,
				tmpBoard.data_.getCurrentPlayerHero(), tmpBoard, deck, null);

		board = new HearthTreeNode(tmpBoard.data_.flipPlayers());
		board.data_.getCurrentPlayerCardHand(0).useOn(PlayerSide.CURRENT_PLAYER, board.data_.getCurrentPlayerHero(),
				board, deck, null);
		board.data_.getCurrentPlayerCardHand(0).useOn(PlayerSide.CURRENT_PLAYER, board.data_.getCurrentPlayerHero(),
				board, deck, null);

		board.data_.resetMana();
		board.data_.resetMinions();

	}

	@Test
	public void testHeropowerNode() throws HSException {
		Minion target = board.data_.getCharacter(PlayerSide.CURRENT_PLAYER, 0);
		Hero hero = board.data_.getCurrentPlayerHero();
		HearthTreeNode ret = hero.useHeroAbility(PlayerSide.CURRENT_PLAYER, target, board, deck, null);
		assertNotEquals(board, ret);
		assertTrue(ret instanceof RandomEffectNode);
		assertEquals(ret.getChildren().size(), 4);

		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getNumMinions(), 2);
		assertEquals(board.data_.getCurrentPlayer().getMana(), 8);
	}

	@Test
	public void testTotemSearing() throws HSException {
		Minion target = board.data_.getCharacter(PlayerSide.CURRENT_PLAYER, 0);
		Hero hero = board.data_.getCurrentPlayerHero();
		HearthTreeNode ret = hero.useHeroAbility(PlayerSide.CURRENT_PLAYER, target, board, deck, null);

		HearthTreeNode searingNode = ret.getChildren().get(0);
		assertEquals(searingNode.data_.getCurrentPlayer().getNumMinions(), 3);
		assertEquals(searingNode.data_.getCurrentPlayer().getMana(), 6);
		assertTrue(searingNode.data_.getCurrentPlayer().getMinions().get(2) instanceof SearingTotem);

		assertEquals(searingNode.data_.getCurrentPlayer().getMinions().get(0).getTotalHealth(), 2);
		assertEquals(searingNode.data_.getCurrentPlayer().getMinions().get(1).getTotalHealth(), 7);
		assertEquals(searingNode.data_.getCurrentPlayer().getMinions().get(2).getTotalHealth(), 1);

		// Raid Leader is in effect
		assertEquals(searingNode.data_.getCurrentPlayer().getMinions().get(0).getTotalAttack(), 2);
		assertEquals(searingNode.data_.getCurrentPlayer().getMinions().get(1).getTotalAttack(), 7);
		assertEquals(searingNode.data_.getCurrentPlayer().getMinions().get(2).getTotalAttack(), 2);
	}

	@Test
	public void testTotemStoneclaw() throws HSException {
		Minion target = board.data_.getCharacter(PlayerSide.CURRENT_PLAYER, 0);
		Hero hero = board.data_.getCurrentPlayerHero();
		HearthTreeNode ret = hero.useHeroAbility(PlayerSide.CURRENT_PLAYER, target, board, deck, null);

		HearthTreeNode searingNode = ret.getChildren().get(1);
		assertEquals(searingNode.data_.getCurrentPlayer().getNumMinions(), 3);
		assertEquals(searingNode.data_.getCurrentPlayer().getMana(), 6);
		assertTrue(searingNode.data_.getCurrentPlayer().getMinions().get(2) instanceof StoneclawTotem);

		assertEquals(searingNode.data_.getCurrentPlayer().getMinions().get(0).getTotalHealth(), 2);
		assertEquals(searingNode.data_.getCurrentPlayer().getMinions().get(1).getTotalHealth(), 7);
		assertEquals(searingNode.data_.getCurrentPlayer().getMinions().get(2).getTotalHealth(), 2);

		// Raid Leader is in effect
		assertEquals(searingNode.data_.getCurrentPlayer().getMinions().get(0).getTotalAttack(), 2);
		assertEquals(searingNode.data_.getCurrentPlayer().getMinions().get(1).getTotalAttack(), 7);
		assertEquals(searingNode.data_.getCurrentPlayer().getMinions().get(2).getTotalAttack(), 1);
	}

	@Test
	public void testTotemHealing() throws HSException {
		Minion target = board.data_.getCharacter(PlayerSide.CURRENT_PLAYER, 0);
		Hero hero = board.data_.getCurrentPlayerHero();
		HearthTreeNode ret = hero.useHeroAbility(PlayerSide.CURRENT_PLAYER, target, board, deck, null);

		HearthTreeNode healingNode = ret.getChildren().get(2);
		assertEquals(healingNode.data_.getCurrentPlayer().getNumMinions(), 3);
		assertEquals(healingNode.data_.getCurrentPlayer().getMana(), 6);
		assertTrue(healingNode.data_.getCurrentPlayer().getMinions().get(2) instanceof HealingTotem);

		assertEquals(healingNode.data_.getCurrentPlayer().getMinions().get(0).getTotalHealth(), 2);
		assertEquals(healingNode.data_.getCurrentPlayer().getMinions().get(1).getTotalHealth(), 7);
		assertEquals(healingNode.data_.getCurrentPlayer().getMinions().get(2).getTotalHealth(), 2);

		// Raid Leader is in effect
		assertEquals(healingNode.data_.getCurrentPlayer().getMinions().get(0).getTotalAttack(), 2);
		assertEquals(healingNode.data_.getCurrentPlayer().getMinions().get(1).getTotalAttack(), 7);
		assertEquals(healingNode.data_.getCurrentPlayer().getMinions().get(2).getTotalAttack(), 1);
	}

	@Test
	public void testTotemWrathOfAir() throws HSException {
		Minion target = board.data_.getCharacter(PlayerSide.CURRENT_PLAYER, 0);
		Hero hero = board.data_.getCurrentPlayerHero();
		HearthTreeNode ret = hero.useHeroAbility(PlayerSide.CURRENT_PLAYER, target, board, deck, null);

		HearthTreeNode wrathOfAirNode = ret.getChildren().get(3);
		assertEquals(wrathOfAirNode.data_.getCurrentPlayer().getNumMinions(), 3);
		assertEquals(wrathOfAirNode.data_.getCurrentPlayer().getMana(), 6);
		assertTrue(wrathOfAirNode.data_.getCurrentPlayer().getMinions().get(2) instanceof WrathOfAirTotem);

		assertEquals(wrathOfAirNode.data_.getCurrentPlayer().getMinions().get(0).getTotalHealth(), 2);
		assertEquals(wrathOfAirNode.data_.getCurrentPlayer().getMinions().get(1).getTotalHealth(), 7);
		assertEquals(wrathOfAirNode.data_.getCurrentPlayer().getMinions().get(2).getTotalHealth(), 2);

		// Raid Leader is in effect
		assertEquals(wrathOfAirNode.data_.getCurrentPlayer().getMinions().get(0).getTotalAttack(), 2);
		assertEquals(wrathOfAirNode.data_.getCurrentPlayer().getMinions().get(1).getTotalAttack(), 7);
		assertEquals(wrathOfAirNode.data_.getCurrentPlayer().getMinions().get(2).getTotalAttack(), 1);
	}

	@Test
	public void testTotemPosition() throws HSException {
		BruteForceSearchAI ai0 = BruteForceSearchAI.buildStandardAI1();
		List<HearthActionBoardPair> ab = ai0.playTurn(0, board.data_);
		BoardModel resBoard = ab.get(ab.size() - 1).board;

		assertEquals(resBoard.getCurrentPlayer().getMana(), 6);
		assertEquals(resBoard.getWaitingPlayer().getMana(), 8);
		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(resBoard).getNumMinions(), 3);
		assertEquals(PlayerSide.WAITING_PLAYER.getPlayer(resBoard).getNumMinions(), 1);

		boolean isRightTotem = false;
		isRightTotem = isRightTotem || resBoard.getCurrentPlayer().getMinions().getLast() instanceof HealingTotem;
		isRightTotem = isRightTotem || resBoard.getCurrentPlayer().getMinions().getLast() instanceof SearingTotem;
		isRightTotem = isRightTotem || resBoard.getCurrentPlayer().getMinions().getLast() instanceof StoneclawTotem;
		isRightTotem = isRightTotem || resBoard.getCurrentPlayer().getMinions().getLast() instanceof WrathOfAirTotem;
		assertTrue(isRightTotem);

		log.info("{}", PlayerSide.CURRENT_PLAYER.getPlayer(resBoard).getMinions().get(2).getClass());
	}

	@Test
	public void testHeropowerWithFullBoard() throws HSException {
		Minion target = board.data_.getCharacter(PlayerSide.CURRENT_PLAYER, 0);
		Hero shaman = board.data_.getCurrentPlayerHero();
		board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, new SilverHandRecruit());
		board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, new SilverHandRecruit());
		board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, new SilverHandRecruit());
		board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, new SilverHandRecruit());
		board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, new SilverHandRecruit());
		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getNumMinions(), 7);

		HearthTreeNode ret = shaman.useHeroAbility(PlayerSide.CURRENT_PLAYER, target, board, deck, null);
		assertNull(ret);

		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getNumMinions(), 7);
		assertEquals(board.data_.getCurrentPlayer().getMana(), 8);
	}

	@Test
	public void testHeropowerWithExistingTotem() throws HSException {
		Minion target = board.data_.getCharacter(PlayerSide.CURRENT_PLAYER, 0);
		Hero shaman = board.data_.getCurrentPlayerHero();
		board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, new SearingTotem());
		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getNumMinions(), 3);

		HearthTreeNode ret = shaman.useHeroAbility(PlayerSide.CURRENT_PLAYER, target, board, deck, null);
		assertNotEquals(board, ret);
		assertTrue(ret instanceof RandomEffectNode);
		assertEquals(ret.getChildren().size(), 3);

		HearthTreeNode childNode = ret.getChildren().get(0);
		assertFalse(childNode.data_.getCurrentPlayer().getMinions().get(3) instanceof SearingTotem);

		childNode = ret.getChildren().get(1);
		assertFalse(childNode.data_.getCurrentPlayer().getMinions().get(3) instanceof SearingTotem);

		childNode = ret.getChildren().get(2);
		assertFalse(childNode.data_.getCurrentPlayer().getMinions().get(3) instanceof SearingTotem);

		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getNumMinions(), 3);
		assertEquals(board.data_.getCurrentPlayer().getMana(), 8);
	}

	@Test
	public void testHeropowerWithAllTotems() throws HSException {
		Minion target = board.data_.getCharacter(PlayerSide.CURRENT_PLAYER, 0);
		Hero shaman = board.data_.getCurrentPlayerHero();
		board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, new SearingTotem());
		board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, new StoneclawTotem());
		board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, new HealingTotem());
		board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, new WrathOfAirTotem());
		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getNumMinions(), 6);

		HearthTreeNode ret = shaman.useHeroAbility(PlayerSide.CURRENT_PLAYER, target, board, deck, null);
		assertNull(ret);

		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getNumMinions(), 6);
		assertEquals(board.data_.getCurrentPlayer().getMana(), 8);
	}
}
