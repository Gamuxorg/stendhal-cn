package games.stendhal.server.maps.quests.piedpiper;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/**
 *  NPC's actions when player asks for his reward.
 *
 */
public class RewardPlayerAction implements ChatAction, ITPPQuestConstants {

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser mayor) {
		    	final int quantity = TPPQuestHelperFunctions.calculateReward(player);
		    	// to avoid giving karma without job
		    	if(quantity==0) {
		    		mayor.say("鼠灾发生期间你没有杀死任何老鼠, 所以你没有奖励.");
		    		return;
		    	}
		    	player.addKarma(5);
		    	final StackableItem moneys = (StackableItem) SingletonRepository.getEntityManager()
		    				.getItem("money");
		    	moneys.setQuantity(quantity);
		    	player.equipOrPutOnGround(moneys);
		    	mayor.say("请拿着 "+quantity+" 奖励, 十分感谢你的帮助.");
		    	player.setQuest(QUEST_SLOT, "done");
			}
}

