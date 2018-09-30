/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import games.stendhal.common.Rand;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerCanEquipItemCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;


/**
 * QUEST: Find Jefs mum
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Jef</li>
 * <li>安伯</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>杰夫waits for his mum in Kirdneh for a longer time now and is frightened that something happened to her</li>
 * <li> You go to find 安伯 somewhere in Fado forest</li>
 * <li> She gives you a flower which you have to bring to Jef</li>
 * <li> You return and give the flower to Jef</li>
 * <li>杰夫will reward you well</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 800 XP</li>
 * <li> Red lionfish which杰夫got by someone who made holidays on 阿多斯城 earlier (between 1-6)</li>
 * <li> Karma: 15</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> Once every 4320 minutes. (3 days)</li>
 * </ul>
 *
 * @author Vanessa Julius
 *
 */
public class FindJefsMom extends AbstractQuest {

	// 4320 minutes (3 days)
	private static final int REQUIRED_MINUTES = 4320;

	private static final String QUEST_SLOT = "find_jefs_mom";


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void offerQuestStep() {
		final SpeakerNPC npc = npcs.get("杰夫");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new OrCondition(new QuestNotStartedCondition(QUEST_SLOT), new QuestInStateCondition(QUEST_SLOT, 0, "rejected")),
			ConversationStates.QUEST_OFFERED,
			"我找不到我妈妈了！她去了市场买东西, 但很长时间没回来, 你能帮我找找她吗？",
			null);

		// player asks about quest which he has done already and he is allowed to repeat it
				npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"), new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"离我上次看到我妈妈已经过长时间了, 我可以请你再看她一次, 如果她还好的话回来告诉我.可以吗?",
				null);

		// player asks about quest but time didn't pass yet
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1,REQUIRED_MINUTES))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "现在我不想打扰妈妈, 她好像需要独自呆一会, 所以你现在不必找到她, 有事你可以问我"));


		// Player agrees to find mum
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			"非常感谢！我希望我 #mum 能马上安全回来！请把我的名字 #Jef 跟她说, 证明是我要你找她的. 如果你找到她, 请回来跟我说一声, 我会给你点东西作为答谢. ",
			new MultipleActions(new SetQuestAction(QUEST_SLOT, 0, "start")));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
			"哦, Ok. 我理解...你是一个大忙人, 所以我不再求你帮我了. ",
			new MultipleActions(new SetQuestAction(QUEST_SLOT, 0, "rejected"),
					new DecreaseKarmaAction(10.0)));

		// Player asks for quest but is already on it

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"我希望你能帮我找到妈妈, 然后回来告诉我她很好 #fine. ",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("mum", "mother", "mom"),
				null,
				ConversationStates.ATTENDING,
				"我妈妈 安伯 离开我去市场买东西了, 但她还没回来 #yet.",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				"yet",
				null,
				ConversationStates.ATTENDING,
				"我只知道这么多, 她和她的男朋友发生口角, #Roger #Frampton earlier...",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				"杰夫",
				null,
				ConversationStates.ATTENDING,
				"对, 是我 :)",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("罗格费雷普顿", "Roger", "Frampton"),
				null,
				ConversationStates.ATTENDING,
				"可能 Roger 对她的离开有些疑问. 我不确定他的具体位置, 我只知道他在 Kirdneh 的某处售房.",
				null);

	}

	private void findMomStep() {
		final SpeakerNPC amber = npcs.get("安伯");

        // give the flower if it's at least 5 days since the player activated the quest the last time, and set the time slot again
		amber.add(ConversationStates.ATTENDING, "杰夫",
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"start"),
							 new PlayerCanEquipItemCondition("马蹄莲")),

			ConversationStates.IDLE,
			"噢, 我想想: ) 我儿子杰夫让你来看望我, 她是个很棒很有爱的孩子！请把这些 马蹄莲 给他. 我很喜爱这些花！带上这个, 回去告诉他我很好 #fine.",
			new MultipleActions(new EquipItemAction("马蹄莲", 1, true),
                                new SetQuestAction(QUEST_SLOT, 0, "found_mom")));


		// don't put the flower on the ground - if player has no space, tell them
		amber.add(ConversationStates.ATTENDING, "杰夫",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
								 new NotCondition(new PlayerCanEquipItemCondition("马蹄莲"))),
				ConversationStates.IDLE,
				"哦, 我想拜托你把这些花带给我儿子, 证明我没事. 不过我看得出来, 你身上没有地方可以带着花, 请清理下你的物品, 然后回来取花吧. !",
				null);

        // don't give the flower if the quest state isn't start
	    amber.add(ConversationStates.ATTENDING, "杰夫",
		     	new AndCondition(new NotCondition(new QuestActiveCondition(QUEST_SLOT))),
		    	ConversationStates.IDLE,
		    	"我不相信你, 当你对我说我儿子名字时声音颤抖, 我希望他很好, 而且能开心安全. ",
		    	null);

	    amber.add(ConversationStates.ATTENDING, "杰夫",
	    		new AndCondition(
	    				new QuestInStateCondition(QUEST_SLOT, "found_mom"),
	    				new PlayerHasItemWithHimCondition("马蹄莲")),
	    		ConversationStates.IDLE,
	    		"请把这些花带给我儿子, 并对他说我很好 #fine.",
	    		null);

	    // replace flower if lost
	    amber.add(ConversationStates.ATTENDING, Arrays.asList("杰夫", "flower", "马蹄莲"),
	    		new AndCondition(
	    				new QuestInStateCondition(QUEST_SLOT, 0, "found_mom"),
	    				new NotCondition(new PlayerHasItemWithHimCondition("马蹄莲"))),
	    		ConversationStates.IDLE,
	    		"噢, 你把我给的花弄丢了？恐怕我也没有了. 你和风车旁的 詹妮 谈谈, 或许她可以帮到你. ",
	    		null);

	}

	private void bringFlowerToJefStep() {
		final SpeakerNPC npc = npcs.get("杰夫");

		ChatAction addRandomNumberOfItemsAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				//add random number of red lionfish
				final StackableItem red_lionfish = (StackableItem) SingletonRepository.getEntityManager()
						.getItem("red lionfish");
				int redlionfishamount;
				redlionfishamount = Rand.roll1D6();
				red_lionfish.setQuantity(redlionfishamount);
				player.equipOrPutOnGround(red_lionfish);
				npc.say("谢谢！拿着 " + redlionfishamount + "朵" +  "red lionfish! 这是我以前从一个来Amzone岛旅行的人手中得到的, 或许你得用 " + redlionfishamount + " for something.");

			}
		};
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("flower", "马蹄莲", "fine", "安伯", "done"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "found_mom"), new PlayerHasItemWithHimCondition("马蹄莲")),
				ConversationStates.ATTENDING, null,
				new MultipleActions(new DropItemAction("马蹄莲"),
                                    new IncreaseXPAction(800),
                                    new IncreaseKarmaAction(15),
									addRandomNumberOfItemsAction,
									new IncrementQuestAction(QUEST_SLOT, 2, 1),
									new SetQuestToTimeStampAction(QUEST_SLOT,1),
									new SetQuestAction(QUEST_SLOT, 0, "done")));


	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"寻找 Jefs 的妈妈",
				"Jef, 是 Kirdneh 城的一个等妈妈回家小朋友, 他的妈妈 安伯 去了超市, 但一直没回来. ",
				false);
		offerQuestStep();
		findMomStep();
		bringFlowerToJefStep();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("我在 Kirdneh 市发现了 Jefs . 他在等妈妈回来. ");
        final String questStateFull = player.getQuest(QUEST_SLOT);
        final String[] parts = questStateFull.split(";");
        final String questState = parts[0];

        if ("rejected".equals(questState)) {
			res.add("我寻找他的妈妈时花费了大多的时间, 这是为什么我拒绝帮他找妈妈的原因. ");
		}
		if ("start".equals(questState)) {
			res.add("Jef 请我看望他的去市场买东西没回来的安伯妈妈, 希望她能在我告诉她儿子Jef后能听进我的话. ");
		}
		if ("found_mom".equals(questState)) {
			res.add("我找到了 安伯, Jef's 妈妈, 当她在 Fade 森林的某处闲逛, 她给了我一朵花要带回给她儿子, 并且要我对她儿子说她很好. ");
		}
        if (isCompleted(player)) {
            if (isRepeatable(player)) {
                res.add("会见了杰夫的妈妈后, 应该回去答复杰夫了, 或许他想要我再次寻找他妈妈. ");
            } else {
                res.add("我对杰夫说他妈妈很好, 现在他想让妈妈一个人独自呆一段时间.");
            }
		}

		return res;

	}

	@Override
	public String getName() {
		return "FindJefsMom";
	}


	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
				 new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)).fire(player,null, null);
	}

	@Override
	public String getRegion() {
		return Region.KIRDNEH;
	}
	@Override
	public String getNPCName() {
		return "杰夫";
	}
}
