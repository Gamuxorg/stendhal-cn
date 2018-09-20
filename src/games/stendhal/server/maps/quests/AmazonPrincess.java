/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: The Amazon Princess
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Princess Esclara, the Amazon Princess in a Hut on 亚马孙岛</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>The princess asks you for an exotic drink</li>
 * <li>Find someone who serves exotic drinks</li>
 * <li>Take exotic drink back to princess</li>
 * <li>Princess gives you a reward</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>Karma +25 in all</li>
 * <li>Some 鱼肉派, random between 2 and 7.</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>You can repeat it once an hour.</li>
 * </ul>
 */
public class AmazonPrincess extends AbstractQuest {

	private static final String QUEST_SLOT = "amazon_princess";

	// The delay between repeating quests is 60 minutes
	private static final int REQUIRED_MINUTES = 60;
	private static final List<String> triggers = Arrays.asList("drink", "椰林飘香", "cocktail", "cheers", "pina");


	private void offerQuestStep() {
		final SpeakerNPC npc = npcs.get("Princess Esclara");
npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"我在寻找一种饮品, 最好是外来品, 你能帮我找来一支吗？",
				null);
npc.add(ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		new QuestCompletedCondition(QUEST_SLOT),
		ConversationStates.ATTENDING,
		"我喝过了, 谢谢你！",
		null);

npc.add(ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		new AndCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES), new QuestStateStartsWithCondition(QUEST_SLOT, "drinking;")),
		ConversationStates.QUEST_OFFERED,
		"上次你带来的鸡尾酒真的好喝, 你还能带给我另一种饮品吗?",
		null);

npc.add(ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)), new QuestStateStartsWithCondition(QUEST_SLOT, "drinking;")),
		ConversationStates.ATTENDING,
		null,
		new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "我相信我喝多后也会喝另一种 "));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, null,
				ConversationStates.ATTENDING,
				"我喜欢这些外来饮料, 但我不记得我喜欢的饮料的名字了. ",
				null);

// Player agrees to get the drink
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"谢谢你！如果你找到一些, 对我说 #drink ,我保证不会亏待你. ",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 3));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"Oh, 没你法, 再见了. ",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	/**
	 * Get Drink Step :
	 * src/games/stendhal/server/maps/athor/cocktail_bar/BarmanNPC.java he
	 * serves drinks to all, not just those with the quest
	 */
	private void bringCocktailStep() {
		final SpeakerNPC npc = npcs.get("Princess Esclara");
		npc.add(
			ConversationStates.ATTENDING, triggers,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new PlayerHasItemWithHimCondition("椰林飘香")),
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(
						new DropItemAction("椰林飘香"),
						new ChatAction() {
							@Override
							public void fire(final Player player,
									final Sentence sentence,
									final EventRaiser npc) {
								int pieAmount = Rand.roll1D6() + 1;
								new EquipItemAction("鱼肉派", pieAmount, true).fire(player, sentence, npc);
								npc.say("谢谢你！拿着厨师做的这 " +
										pieAmount + " 个 " +
										 "鱼肉派" +
										" , 还有我的～吻. ");
								new SetQuestAndModifyKarmaAction(getSlotName(), "drinking;"
																 + System.currentTimeMillis(), 15.0).fire(player, sentence, npc);
							}
						},
						new InflictStatusOnNPCAction("椰林飘香")
						));

		npc.add(
			ConversationStates.ATTENDING, triggers,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new NotCondition(new PlayerHasItemWithHimCondition("椰林飘香"))),
			ConversationStates.ATTENDING,
			"你没有带来我喜欢的饮品, 去弄一些外来的品种！",
			null);

		npc.add(
			ConversationStates.ATTENDING, triggers,
			new QuestNotInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"你可以抽空弄点我喜欢的 #favour ...", null);

	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Amazon 公主",
				"一个想喝饮料的公主. ",
				true);
		offerQuestStep();
		bringCocktailStep();
	}


	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Princess Esclara 欢迎我到来到她在 Amazon 岛上的家. ");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("她让我找一些饮品, 但我觉得她应该没喝过.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start") || isCompleted(player)) {
			res.add("公主很渴, 我答应给她找一个异界饮品, 找到后对她说 'drink' . ");
		}
		if ("start".equals(questState) && player.isEquipped("椰林飘香") || isCompleted(player)) {
			res.add("我为公主找到一种 椰林飘香 的饮料, 我觉得她会喜欢喝. ");
		}
        if (isCompleted(player)) {
            if (isRepeatable(player)) {
                res.add("我把 椰林飘香 带给公主, 但我打赌她肯定会再要, 也许我应该弄到更多的 鱼肉派s.");
            } else {
                res.add("公主 Esclara 很爱我带给她的 椰林飘香 , 她不再感到口渴了. 她把 鱼肉派s 送给了我, 还有一个香吻!");
            }
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "AmazonPrincess";
	}

	// Amazon is dangerous below this level - don't hint to go there
	@Override
	public int getMinLevel() {
		return 70;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"drinking;"),
				 new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)).fire(player,null, null);
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"drinking;").fire(player, null, null);
	}

	@Override
	public String getRegion() {
		return Region.AMAZON_ISLAND;
	}

	@Override
	public String getNPCName() {
		return "Princess Esclara";
	}
}
