package games.stendhal.server.maps.quests.piedpiper;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

public class DetailsKillingsAction implements ChatAction, ITPPQuestConstants {
	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser mayor) {
		if (TPPQuestHelperFunctions.calculateReward(player)==0) {
			mayor.say("你 #鼠灾 发生时, 没有杀死老鼠. "+
					  "要得到 #奖励 你必须要在指定时间内最少要杀死 "+
					  "一只老鼠.");
			return;
		}
		final StringBuilder sb = new StringBuilder("好的, 最终奖励, 你杀了 ");
		long moneys = 0;
		int kills = 0;
		for(int i=0; i<RAT_TYPES.size(); i++) {
			try {
				kills=Integer.parseInt(player.getQuest(QUEST_SLOT,i+1));
			} catch (NumberFormatException nfe) {
				// Have no records about this creature in player's slot.
				// Treat it as he never killed this creature.
				kills=0;
			}
			// must add 'and' word before last creature in list
			if(i==(RAT_TYPES.size()-1)) {
				sb.append("和 ");
			}

			sb.append(kills + RAT_TYPES.get(i));
			sb.append(", ");
			moneys = moneys + kills*RAT_REWARDS.get(i);
		}
		sb.append("所以我要给你 ");
		sb.append(moneys);
		sb.append(" 钱作为 #奖励 ");
		mayor.say(sb.toString());
	}


}
