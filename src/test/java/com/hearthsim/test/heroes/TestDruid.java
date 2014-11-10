package com.hearthsim.test.heroes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.hearthsim.card.Card;
import com.hearthsim.card.Deck;
import com.hearthsim.card.minion.Hero;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.minion.concrete.BoulderfistOgre;
import com.hearthsim.card.minion.concrete.RaidLeader;
import com.hearthsim.card.minion.heroes.Druid;
import com.hearthsim.card.minion.heroes.TestHero;
import com.hearthsim.card.spellcard.concrete.TheCoin;
import com.hearthsim.card.spellcard.concrete.WildGrowth;
import com.hearthsim.exception.HSException;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.tree.HearthTreeNode;

public class TestDruid {

	private HearthTreeNode board;
	private Deck deck;

	@Before
	public void setup() {
		board = new HearthTreeNode(new BoardModel(new Druid(), new TestHero()));

		Minion minion0_0 = new BoulderfistOgre();
		Minion minion0_1 = new RaidLeader();
		Minion minion1_0 = new BoulderfistOgre();
		Minion minion1_1 = new RaidLeader();
		
		board.data_.placeCardHandCurrentPlayer(minion0_0);
		board.data_.placeCardHandCurrentPlayer(minion0_1);
				
		board.data_.placeCardHandWaitingPlayer(minion1_0);
		board.data_.placeCardHandWaitingPlayer(minion1_1);

		Card cards[] = new Card[10];
		for (int index = 0; index < 10; ++index) {
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
		try {
			tmpBoard.data_.getCurrentPlayerCardHand(0).useOn(PlayerSide.CURRENT_PLAYER, tmpBoard.data_.getCurrentPlayerHero(), tmpBoard, deck, null);
			tmpBoard.data_.getCurrentPlayerCardHand(0).useOn(PlayerSide.CURRENT_PLAYER, tmpBoard.data_.getCurrentPlayerHero(), tmpBoard, deck, null);
		} catch (HSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		board = new HearthTreeNode(tmpBoard.data_.flipPlayers());
		try {
			board.data_.getCurrentPlayerCardHand(0).useOn(PlayerSide.CURRENT_PLAYER, board.data_.getCurrentPlayerHero(), board, deck, null);
			board.data_.getCurrentPlayerCardHand(0).useOn(PlayerSide.CURRENT_PLAYER, board.data_.getCurrentPlayerHero(), board, deck, null);
		} catch (HSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		board.data_.resetMana();
		board.data_.resetMinions();
		
	}
	
	@Test
	public void test0() throws HSException {
		//null case
		Minion target = board.data_.getCharacter(PlayerSide.WAITING_PLAYER, 0); // Enemy hero
		Minion raidLeader = PlayerSide.CURRENT_PLAYER.getPlayer(board).getMinions().get(0);
		HearthTreeNode ret = raidLeader.attack(PlayerSide.WAITING_PLAYER, target, board, deck, null);
		
		assertNotNull(ret);
		assertEquals(board.data_.getNumCards_hand(), 1);
		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getNumMinions(), 2);
		assertEquals(PlayerSide.WAITING_PLAYER.getPlayer(board).getNumMinions(), 2);
		assertEquals(board.data_.getCurrentPlayer().getMana(), 8);
		assertEquals(board.data_.getWaitingPlayer().getMana(), 8);
		assertEquals(board.data_.getCurrentPlayerHero().getHealth(), 30);
		assertEquals(board.data_.getWaitingPlayerHero().getHealth(), 28);
		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getMinions().get(0).getHealth(), 2);
		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getMinions().get(1).getHealth(), 7);
		assertEquals(PlayerSide.WAITING_PLAYER.getPlayer(board).getMinions().get(0).getHealth(), 2);
		assertEquals(PlayerSide.WAITING_PLAYER.getPlayer(board).getMinions().get(1).getHealth(), 7);

		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getMinions().get(0).getTotalAttack(), 2);
		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getMinions().get(1).getTotalAttack(), 7);
		assertEquals(PlayerSide.WAITING_PLAYER.getPlayer(board).getMinions().get(0).getTotalAttack(), 2);
		assertEquals(PlayerSide.WAITING_PLAYER.getPlayer(board).getMinions().get(1).getTotalAttack(), 7);
		
		
		raidLeader.hasAttacked(false);
		target = board.data_.getCharacter(PlayerSide.WAITING_PLAYER, 2); // Boulderfist Ogre
		ret = raidLeader.attack(PlayerSide.WAITING_PLAYER, target, board, deck, null);
		assertEquals(board.data_.getNumCards_hand(), 1);
		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getNumMinions(), 1);
		assertEquals(PlayerSide.WAITING_PLAYER.getPlayer(board).getNumMinions(), 2);
		assertEquals(board.data_.getCurrentPlayer().getMana(), 8);
		assertEquals(board.data_.getWaitingPlayer().getMana(), 8);
		assertEquals(board.data_.getCurrentPlayerHero().getHealth(), 30);
		assertEquals(board.data_.getWaitingPlayerHero().getHealth(), 28);
		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getMinions().get(0).getHealth(), 7);
		assertEquals(PlayerSide.WAITING_PLAYER.getPlayer(board).getMinions().get(0).getHealth(), 2);
		assertEquals(PlayerSide.WAITING_PLAYER.getPlayer(board).getMinions().get(1).getHealth(), 5);

		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getMinions().get(0).getTotalAttack(), 6);
		assertEquals(PlayerSide.WAITING_PLAYER.getPlayer(board).getMinions().get(0).getTotalAttack(), 2);
		assertEquals(PlayerSide.WAITING_PLAYER.getPlayer(board).getMinions().get(1).getTotalAttack(), 7);
	}

	@Test
	public void testHeropower() throws HSException {
		Hero hero = board.data_.getCurrentPlayerHero();
		Minion target = board.data_.getCharacter(PlayerSide.CURRENT_PLAYER, 0); // Druid hero
		HearthTreeNode ret = hero.useHeroAbility(PlayerSide.CURRENT_PLAYER, target, board, deck, null);
		assertNotNull(ret);

		assertEquals(board.data_.getNumCards_hand(), 1);

		assertTrue(hero.hasBeenUsed());
		assertEquals(board.data_.getCurrentPlayer().getMana(), 6);
		assertEquals(board.data_.getWaitingPlayer().getMana(), 8);
		assertEquals(board.data_.getCurrentPlayer().getMana(), 6);
		assertEquals(board.data_.getCurrentPlayerHero().getHealth(), 30);
		assertEquals(board.data_.getWaitingPlayerHero().getHealth(), 30);
		assertEquals(board.data_.getCurrentPlayerHero().getArmor(), 1);
		assertEquals(board.data_.getWaitingPlayerHero().getArmor(), 0);
		assertEquals(board.data_.getCurrentPlayerHero().getTotalAttack(), 1);
		assertEquals(board.data_.getWaitingPlayerHero().getTotalAttack(), 0);
		assertEquals(board.data_.getCurrentPlayerHero().getExtraAttackUntilTurnEnd(), 1);
		assertEquals(board.data_.getWaitingPlayerHero().getExtraAttackUntilTurnEnd(), 0);
	}

	@Test
	@Ignore("Existing bug")
	public void testArmorIsAdditive() throws HSException {
		Hero hero = board.data_.getCurrentPlayerHero();
		hero.setArmor((byte)1);

		Minion target = board.data_.getCharacter(PlayerSide.CURRENT_PLAYER, 0); // Druid hero
		HearthTreeNode ret = hero.useHeroAbility(PlayerSide.CURRENT_PLAYER, target, board, deck, null);
		assertNotNull(ret);
		assertEquals(board.data_.getCurrentPlayerHero().getArmor(), 2);
	}

	@Test
	@Ignore("Existing bug")
	public void testAttackIsAdditive() throws HSException {
		Hero hero = board.data_.getCurrentPlayerHero();
		hero.setExtraAttackUntilTurnEnd((byte)1);

		Minion target = board.data_.getCharacter(PlayerSide.CURRENT_PLAYER, 0); // Druid hero
		HearthTreeNode ret = hero.useHeroAbility(PlayerSide.CURRENT_PLAYER, target, board, deck, null);
		assertNotNull(ret);
		assertEquals(board.data_.getCurrentPlayerHero().getExtraAttackUntilTurnEnd(), 2);
	}

	@Test
	@Ignore("Existing bug")
	public void testCannotTargetMinion() throws HSException {
		Minion raidLeader = board.data_.getCharacter(PlayerSide.CURRENT_PLAYER, 0);
		Hero hero = board.data_.getCurrentPlayerHero();
		HearthTreeNode ret = hero.useHeroAbility(PlayerSide.CURRENT_PLAYER, raidLeader, board, deck, null);
		assertNull(ret);

		assertEquals(board.data_.getNumCards_hand(), 1);

		assertEquals(board.data_.getCurrentPlayer().getMana(), 8);
		assertEquals(board.data_.getWaitingPlayer().getMana(), 8);
		assertEquals(board.data_.getCurrentPlayerHero().getHealth(), 30);
		assertEquals(board.data_.getWaitingPlayerHero().getHealth(), 30);
		assertEquals(board.data_.getCurrentPlayerHero().getArmor(), 0);
		assertEquals(board.data_.getWaitingPlayerHero().getArmor(), 0);
		assertEquals(board.data_.getCurrentPlayerHero().getTotalAttack(), 0);
		assertEquals(board.data_.getWaitingPlayerHero().getTotalAttack(), 0);
		assertEquals(board.data_.getCurrentPlayerHero().getExtraAttackUntilTurnEnd(), 0);
		assertEquals(board.data_.getWaitingPlayerHero().getExtraAttackUntilTurnEnd(), 0);

		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getMinions().get(0).getAttack(), 2);
		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getMinions().get(0).getTotalAttack(), 2);
		assertEquals(PlayerSide.CURRENT_PLAYER.getPlayer(board).getMinions().get(0).getHealth(), 2);
	}

	@Test
	public void testCannotTargetOpponent() throws HSException {
		Minion target = board.data_.getCharacter(PlayerSide.WAITING_PLAYER, 0);
		Hero hero = board.data_.getCurrentPlayerHero();
		HearthTreeNode ret = hero.useHeroAbility(PlayerSide.WAITING_PLAYER, target, board, deck, null);
		assertNull(ret);

		assertEquals(board.data_.getNumCards_hand(), 1);

		assertEquals(board.data_.getCurrentPlayer().getMana(), 8);
		assertEquals(board.data_.getWaitingPlayer().getMana(), 8);
		assertEquals(board.data_.getCurrentPlayerHero().getHealth(), 30);
		assertEquals(board.data_.getWaitingPlayerHero().getHealth(), 30);
		assertEquals(board.data_.getCurrentPlayerHero().getArmor(), 0);
		assertEquals(board.data_.getWaitingPlayerHero().getArmor(), 0);
		assertEquals(board.data_.getCurrentPlayerHero().getTotalAttack(), 0);
		assertEquals(board.data_.getWaitingPlayerHero().getTotalAttack(), 0);
		assertEquals(board.data_.getCurrentPlayerHero().getExtraAttackUntilTurnEnd(), 0);
		assertEquals(board.data_.getWaitingPlayerHero().getExtraAttackUntilTurnEnd(), 0);
	}

	@Test
	@Ignore("Existing bug")
	public void testCannotUseTwice() throws HSException {
		Hero hero = board.data_.getCurrentPlayerHero();
		Minion target = board.data_.getCharacter(PlayerSide.CURRENT_PLAYER, 0); // Druid hero
		HearthTreeNode ret = hero.useHeroAbility(PlayerSide.CURRENT_PLAYER, target, board, deck, null);
		assertNotNull(ret);

		target = board.data_.getCharacter(PlayerSide.CURRENT_PLAYER, 0); // Druid hero
		ret = hero.useHeroAbility(PlayerSide.CURRENT_PLAYER, target, board, deck, null);
		assertNull(ret);

		assertEquals(board.data_.getCurrentPlayer().getMana(), 6);
		assertEquals(board.data_.getCurrentPlayerHero().getHealth(), 30);
		assertEquals(board.data_.getCurrentPlayerHero().getArmor(), 1);
		assertEquals(board.data_.getCurrentPlayerHero().getTotalAttack(), 1);
		assertEquals(board.data_.getCurrentPlayerHero().getExtraAttackUntilTurnEnd(), 1);
	}

	@Test
	@Ignore("Existing bug")
	public void testNotEnoughMana() throws HSException {
		Hero hero = board.data_.getCurrentPlayerHero();
		hero.setMana((byte)1);

		Minion target = board.data_.getCharacter(PlayerSide.CURRENT_PLAYER, 0); // Druid hero
		HearthTreeNode ret = hero.useHeroAbility(PlayerSide.CURRENT_PLAYER, target, board, deck, null);
		assertNull(ret);

		assertFalse(hero.hasBeenUsed());
		assertEquals(board.data_.getCurrentPlayer().getMana(), 1);
		assertEquals(board.data_.getCurrentPlayerHero().getHealth(), 30);
		assertEquals(board.data_.getCurrentPlayerHero().getArmor(), 0);
		assertEquals(board.data_.getCurrentPlayerHero().getTotalAttack(), 0);
		assertEquals(board.data_.getCurrentPlayerHero().getExtraAttackUntilTurnEnd(), 0);
	}
}
