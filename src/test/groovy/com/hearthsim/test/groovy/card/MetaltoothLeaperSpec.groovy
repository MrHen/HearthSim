package com.hearthsim.test.groovy.card

import com.hearthsim.card.minion.concrete.AnnoyOTron
import com.hearthsim.card.minion.concrete.BloodfenRaptor
import com.hearthsim.card.minion.concrete.HarvestGolem
import com.hearthsim.card.minion.concrete.KingOfBeasts
import com.hearthsim.card.minion.concrete.MetaltoothLeaper
import com.hearthsim.card.minion.concrete.StonetuskBoar

import static com.hearthsim.model.PlayerSide.CURRENT_PLAYER
import static org.junit.Assert.*

import com.hearthsim.card.minion.concrete.AbusiveSergeant
import com.hearthsim.card.minion.concrete.BoulderfistOgre
import com.hearthsim.model.BoardModel;
import com.hearthsim.test.helpers.BoardModelBuilder
import com.hearthsim.util.tree.HearthTreeNode;

class MetaltoothLeaperSpec extends CardSpec {

    HearthTreeNode root
    BoardModel startingBoard

    def setup() {
        startingBoard = new BoardModelBuilder().make {
            currentPlayer {
                hand([MetaltoothLeaper])
                field([[minion: HarvestGolem], [minion:StonetuskBoar], [minion:AnnoyOTron]])
                mana(10)
            }
        }
        root = new HearthTreeNode(startingBoard)
    }

    def "buffs friendly minions"() {
        def copiedBoard = startingBoard.deepCopy()
        def theCard = root.data_.getCurrentPlayer().getHand().get(0)
        def ret = theCard.useOn(CURRENT_PLAYER, 0, root)

        expect:
        ret != null;

        assertBoardDelta(copiedBoard, ret.data_) {
            currentPlayer {
                playMinion(MetaltoothLeaper, 0)
                mana(7)
                numCardsUsed(1)
                updateMinion(1, [deltaAttack: +2])
                updateMinion(3, [deltaAttack: +2])
            }
        }
    }
}
