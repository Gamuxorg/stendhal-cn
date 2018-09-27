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
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
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
 * QUEST: Coal for Haunchy
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Haunchy Meatoch, the BBQ grillmaster on the Ados market</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Haunchy Meatoch asks you to fetch coal for his BBQ</li>
 * <li>Find some coal in 塞门镇 Mine or buy some from other players</li>
 * <li>Take the coal to Haunchy</li>
 * <li>Haunchy gives you a tasty reward</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>Karma +25 in all</li>
 * <li>XP +200 in all</li>
 * <li>Some 烤排s, random between 1 and 4.</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>You can repeat it each 2 days.</li>
 * </ul>
 *
 * @author Vanessa Julius and storyteller
 */
public class CoalForHaunchy extends AbstractQuest {

	private static final String QUEST_SLOT = "coal_for_haunchy";

	// The delay between repeating quests is 48 hours or 2880 minutes
	private static final int REQUIRED_MINUTES = 2880;

	private void offerQuestStep() {
		final SpeakerNPC npc = npcs.get("Haunchy Meatoch");

		// player says quest when he has not ever done the quest before (rejected or just new)
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"我不能使用木头来做烘烤大餐, 我需要一些真正的石煤炭保持高温, 但剩下不多了, 问题是我不能自已取, 因为我的牛排会烤焦, 所以我只能呆在这.你能带过来 25 块煤炭 #coal 吗?",
				null);

		npc.add(
				ConversationStates.QUEST_OFFERED,
				Arrays.asList("煤炭"),
				null,
				ConversationStates.QUEST_OFFERED,
				"煤炭 煤炭不易找到, 通常可以在地下的某处找到, 但也有可能在 塞门镇 的旧矿区通道内找到...",
				null);

        // player has completed the quest (doesn't happen here)
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"我能继续制作可口的烤牛排了, 谢谢你!",
				null);

		// player asks about quest which he has done already and he is allowed to repeat it
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES), new QuestStateStartsWithCondition(QUEST_SLOT, "waiting;")),
				ConversationStates.QUEST_OFFERED,
				"上次你带来的煤炭快要用过错了, 你能再带一些来吗？",
				null);

		// player asks about quest which he has done already but it is not time to repeat it
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)), new QuestStateStartsWithCondition(QUEST_SLOT, "waiting;")),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "这些煤炭起远远超过我的计划, 我不需要更多了"));

		// Player agrees to get the coal, increase 5 karma
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"谢谢你！如果你找到 25 块煤炭, 请对我说 #煤炭 我就收到了, 我会给你相当不错的奖励.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"Oh, 没办法, 我想你失去了一次吃烘烤大餐的机会. 再见了.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	/*
	 * Get Coal Step :
	 * Players will get some coal in 塞门镇 Mine and with buying some from other players.
	 *
	 */
	private void bringCoalStep() {
		final SpeakerNPC npc = npcs.get("Haunchy Meatoch");

		final List<String> triggers = new ArrayList<String>();
		triggers.add("煤炭");
		triggers.add("煤");
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);

		// player asks about quest or says 煤炭 when they are supposed to bring some 煤炭 and they have it
		npc.add(
				ConversationStates.ATTENDING, triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new PlayerHasItemWithHimCondition("煤炭",25)),
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
						new DropItemAction("煤炭",25),
						new IncreaseXPAction(200),
						new IncreaseKarmaAction(20),
						new ChatAction() {
							@Override
							public void fire(final Player player,
									final Sentence sentence,
									final EventRaiser npc) {
								int grilledsteakAmount = Rand.rand(4) + 1;
								new EquipItemAction("烤排", grilledsteakAmount, true).fire(player, sentence, npc);
								npc.say("谢谢你！从我的烧烤架上拿走 " + grilledsteakAmount + " 个 " +
										 "烤排" + " !");
								new SetQuestAndModifyKarmaAction(getSlotName(), "waiting;"
										+ System.currentTimeMillis(), 10.0).fire(player, sentence, npc);
							}
						}));

		// player asks about quest or says coal when they are supposed to bring some coal and they don't have it
		npc.add(
				ConversationStates.ATTENDING, triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new NotCondition(new PlayerHasItemWithHimCondition("煤炭",25))),
				ConversationStates.ATTENDING,
				"你给的煤不够我用的. 还请再去捡点回来！",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("煤炭","煤"),
				new QuestNotInStateCondition(QUEST_SLOT,"start"),
				ConversationStates.ATTENDING,
				"有时你真是我的 #最爱 ...", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Haunchy 的煤炭",
				"Haunchy Meatoch 担心他的烧烤大餐. 他准备的煤炭能够持续到他的烧烤结束吗？或许他准备的不够?",
				true);
		offerQuestStep();
		bringCoalStep();
	}


	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Haunchy Meatoch 欢迎我来到 Ados 集市");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("他让我去弄些煤炭, 但我没时间做这些. ");
		}
		if (player.isQuestInState(QUEST_SLOT, "start") || isCompleted(player)) {
			res.add("烧烤大餐的铁架温度不够, 我答应为 Haunchy 去找 25 块煤炭. ");
		}
		if ("start".equals(questState) && player.isEquipped("煤炭",25) || isCompleted(player)) {
			res.add("我找到了 25 块煤炭, 我想 Haunchy 一定会很高兴");
		}
		if (isCompleted(player)) {
			if (isRepeatable(player)) {
				res.add("我带着 25 块煤炭给了 Haunchy, 但我but I'd bet his amount is low again and needs more. Maybe I'll get more grilled tasty steaks.");
			} else {
				res.add("Haunchy Meatoch was really happy when I gave him the 煤炭, he has enough for now. He gave me some of the best steaks which I ever ate!");
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
		return "CoalForHaunchy";
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"waiting;"),
				new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)).fire(player,null, null);
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"waiting;").fire(player, null, null);
	}

	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Haunchy Meatoch";
	}
}
