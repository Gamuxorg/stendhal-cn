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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.MathHelper;
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
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingRandomItemCollectionAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasRecordedItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Weekly Item Fetch Quest.
 * <p>
 * PARTICIPANTS:
 * <ul><li> 哈泽尔, Museum Curator of Kirdneh
 * <li> some items
 * </ul>
 * STEPS:<ul>
 * <li> talk to Museum Curator to get a quest to fetch a rare item
 * <li> bring the item to the Museum Curator
 * <li> if you cannot bring it in 6 weeks she offers you the chance to fetch
 *
 * another instead </ul>
 *
 * REWARD:
 * <ul><li> xp
 * <li> between 100 and 600 money
 * <li> can buy kirdneh house if other eligibilities met
 * <li> 10 Karma
 * </ul>
 * REPETITIONS:
 * <ul><li> once a week</ul>
 */
public class WeeklyItemQuest extends AbstractQuest {

	private static final String QUEST_SLOT = "weekly_item";

	/** How long until the player can give up and start another quest */
	private static final int expireDelay = MathHelper.MINUTES_IN_ONE_WEEK * 6;

	/** How often the quest may be repeated */
	private static final int delay = MathHelper.MINUTES_IN_ONE_WEEK;

	/**
	 * All items which are hard enough to find but not tooo hard and not in Daily quest. If you want to do
	 * it better, go ahead. *
	 */
	private static Map<String,Integer> items;

	private static void buildItemsMap() {
		items = new HashMap<String, Integer>();

		// armor
		items.put("野蛮人护甲",1);
		items.put("混沌护甲",1);
		items.put("矮人护甲",1);
		items.put("黄金铠甲",1);
		items.put("冰护甲",1);
		items.put("附魔甲",1);
		items.put("奇妙甲",1);
		items.put("影子铠甲",1);
		items.put("石甲",1);
		items.put("异界甲",1);

		// axe
		items.put("黄金双刃斧",1);
		items.put("魔法双刃斧",1);

		// boots
		items.put("混沌靴",1);
		items.put("金靴子",1);
		items.put("奇妙靴子",1);
		items.put("影子靴子",1);
		items.put("铁靴",1);
		items.put("石靴",1);
		items.put("异界靴子",1);


		// cloak
		items.put("蓝龙斗篷",1);
		items.put("蓝色条纹斗篷",1);
		items.put("混沌斗篷",1);
		items.put("金斗篷",1);
		items.put("奇妙斗篷",1);
		items.put("红龙斗篷",1);
		items.put("影子斗篷",1);
		items.put("异界斗篷",1);

		// club
		items.put("骨玉法杖",1);

		// drinks
		items.put("强治疗剂",5);
		items.put("鱼汤",3);

		// helmet
		items.put("混沌头盔",1);
		items.put("黄金头盔",1);
		items.put("黄金角盔",1);
		items.put("奇妙头盔",1);
		items.put("影子头盔",1);

		// jewellery
		items.put("钻石",1);
		items.put("黑曜石",1);

		// legs
		items.put("混沌护腿",1);
		items.put("矮人护腿",1);
		items.put("黄金护腿",1);
		items.put("奇妙裤子",1);
		items.put("影子护腿",1);
		items.put("异界护腿",1);

		// misc
		items.put("巨人心脏",5);
		items.put("毒腺",1);

		// resource
		items.put("密银锭",1);
		items.put("密银矿石",1);
		items.put("蜘蛛丝腺",7);

		// special
		items.put("幸运石",1);
		items.put("传说之卵",1);

		// shield
		items.put("混乱之盾",1);
		items.put("金盾",1);
		items.put("绿龙盾",1);
		items.put("附魔钢盾",1);
		items.put("奇妙之盾",1);
		items.put("影盾",1);
		items.put("异界盾",1);

		// sword
		items.put("刺客匕首",1);
		items.put("屠刀",1);
		items.put("混沌之剑",1);
		items.put("暗之匕首",1);
		items.put("恶魔剑",1);
		items.put("卓尔之剑",1);
		items.put("烈火剑",1);
		items.put("巨剑",1);
		items.put("地狱匕首",1);
		items.put("冰剑",1);
		items.put("永恒之剑",1);
		items.put("暗之匕首",1);
		items.put("异界之剑",1);

	}

	private ChatAction startQuestAction() {
		// common place to get the start quest actions as we can both starts it and abort and start again

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new StartRecordingRandomItemCollectionAction(QUEST_SLOT,0,items,"I want Kirdneh's museum to be the greatest in the land! Please fetch [item]"
				+ " and say #complete, once you've brought it."));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));

		return new MultipleActions(actions);
	}

	private void getQuest() {
		final SpeakerNPC npc = npcs.get("哈泽尔");
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"You're already on a quest to bring the museum [item]"
						+ ". Please say #complete if you have it with you."));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new TimePassedCondition(QUEST_SLOT,1,expireDelay)),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"You're already on a quest to bring the museum [item]"
						+ ". Please say #complete if you have it with you. But, perhaps that is now too rare an item. I can give you #another task, or you can return with what I first asked you."));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,delay))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT,1, delay, "The museum can only afford to send you to fetch an item once a week. Please check back in"));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new OrCondition(new QuestNotStartedCondition(QUEST_SLOT),
								new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
												 new TimePassedCondition(QUEST_SLOT,1,delay))),
				ConversationStates.ATTENDING,
				null,
				startQuestAction());
	}

	private void completeQuest() {
		final SpeakerNPC npc = npcs.get("哈泽尔");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"I don't remember giving you any #task yet.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"You already completed the last quest I had given to you.",
				null);

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropRecordedItemAction(QUEST_SLOT,0));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new IncrementQuestAction(QUEST_SLOT,2,1));
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
		actions.add(new IncreaseXPDependentOnLevelAction(5.0/3.0, 290.0));
		actions.add(new IncreaseKarmaAction(10.0));
		actions.add(new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				int goldamount;
				final StackableItem money = (StackableItem) SingletonRepository.getEntityManager()
								.getItem("money");
				goldamount = 100 * Rand.roll1D6();
				money.setQuantity(goldamount);
				player.equipOrPutOnGround(money);
				raiser.say("Wonderful! Here is " + Integer.toString(goldamount) + " money to cover your expenses.");
			}
		});

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0)),
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0))),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"You don't seem to have [item]"
						+ " with you. Please get it and say #complete only then."));

	}

	private void abortQuest() {
		final SpeakerNPC npc = npcs.get("哈泽尔");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						 		 new TimePassedCondition(QUEST_SLOT,1,expireDelay)),
				ConversationStates.ATTENDING,
				null,
				startQuestAction());

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						 		 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))),
				ConversationStates.ATTENDING,
				"It hasn't been long since you've started your quest, you shouldn't give up so soon.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new QuestNotActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"I'm afraid I didn't send you on a #quest yet.",
				null);

	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("I have met 哈泽尔, the curator of Kirdneh museum.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("I do not want to help Kirdneh museum become the greatest in the land.");
			return res;
		}
		res.add("I want to help Kirdneh museum become the greatest in the land.");
		if (player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT)) {
			String questItem = player.getRequiredItemName(QUEST_SLOT,0);
			int amount = player.getRequiredItemQuantity(QUEST_SLOT,0);
			if (!player.isEquipped(questItem, amount)) {
				res.add(String.format("I have been asked to find " +amount + questItem + " for Kirdneh museum."));
			} else {
				res.add(String.format("I have " + amount + questItem + " for Kirdneh museum and need to take it."));
			}
		}
		if (isRepeatable(player)) {
			res.add("I took the valuable item to 哈泽尔 and the museum can now afford to send me to find another.");
		} else if (isCompleted(player)) {
			res.add("I took the valuable item to 哈泽尔 within the last 7 days.");
		}
		// add to history how often player helped 哈泽尔 so far
		final int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add("I've brought exhibits for the museum on "
					+ repetitions + "occasion" + " so far.");
		}

		return res;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Kirdneh museum needs help!",
				"哈泽尔, the curator of the Kirdneh Museum, wants as many rare exhibits as she can afford.",
				true);
		buildItemsMap();

		getQuest();
		completeQuest();
		abortQuest();
	}

	@Override
	public String getName() {
		return "WeeklyItemQuest";
	}

	// the items requested are pretty hard to get, so it's not worth prompting player to go till they are higher level.
	@Override
	public int getMinLevel() {
		return 60;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}

	@Override
	public String getRegion() {
		return Region.KIRDNEH;
	}

	@Override
	public String getNPCName() {
		return "哈泽尔";
	}
}
