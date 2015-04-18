package com.hearthsim.test.groovy.card

import com.hearthsim.card.Card
import com.hearthsim.card.Deck
import com.hearthsim.card.minion.concrete.BloodsailRaider
import com.hearthsim.card.minion.concrete.BurlyRockjawTrogg
import com.hearthsim.card.minion.concrete.ManaWyrm
import com.hearthsim.card.minion.concrete.OneEyedCheat
import com.hearthsim.card.spellcard.concrete.TheCoin
import com.hearthsim.model.BoardModel
import com.hearthsim.Game
import com.hearthsim.test.helpers.BoardModelBuilder
import com.hearthsim.util.tree.HearthTreeNode

import static com.hearthsim.model.PlayerSide.CURRENT_PLAYER
import static com.hearthsim.model.PlayerSide.WAITING_PLAYER
import static org.junit.Assert.*

class OneEyedCheatSpec extends CardSpec {
    def "playing a pirate card with a One Eyed Cheat on the field"() {
        def startingBoard = new BoardModelBuilder().make {
            currentPlayer {
                hand([BloodsailRaider, OneEyedCheat])
                field([[minion: OneEyedCheat]])
                mana(10)
            }
            waitingPlayer {
                field([[minion: OneEyedCheat]])
            }
        }

        def root = new HearthTreeNode(startingBoard)

        def copiedBoard = startingBoard.deepCopy()
        def card = root.data_.getCurrentPlayer().getHand().get(0)
        def ret = card.useOn(CURRENT_PLAYER, 0, root)

        expect:
        ret != null

        assertBoardDelta(copiedBoard, ret.data_) {
            currentPlayer {
                playMinion(BloodsailRaider, 0)
                mana(8)
                numCardsUsed(1)
                updateMinion(1, [stealthed: true])
            }
        }
    }
}
