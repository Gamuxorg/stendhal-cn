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
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Hat For 梦金斯
 *
 * PARTICIPANTS:
 * <ul>
 * <li>梦金斯, an old man in 塞门镇.</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> 梦金斯 asks you to buy a hat for him.</li>
 * <li> 辛布兰卡 sells you a leather helmet.</li>
 * <li> 梦金斯 sees your leather helmet and asks for it and then thanks you.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>50 XP</li>
 * <li>Karma: 10</li>
 * </ul>
 *
 * REPETITIONS: - None.
 */
public class HatForMonogenes extends AbstractQuest {
	private static final String QUEST_SLOT = "hat_monogenes";


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (player.hasQuest(QUEST_SLOT)) {
			res.add("我在塞门镇的喷泉处遇到了 梦金斯. ");
		}
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("我需要找一顶帽子, 里面的毛皮可以给我的头保暖. ");
		if (player.isQuestInState(QUEST_SLOT, "start")
				&& player.isEquipped("leather helmet")
				|| player.isQuestCompleted(QUEST_SLOT)) {
			res.add("我发现了一顶帽子.");
		}
		if (player.isQuestCompleted(QUEST_SLOT)) {
			res.add("我把帽子给了 梦金斯 , 让他的光头给暖和一些.");
		}
		return res;
	}

	private void createRequestingStep() {
		final SpeakerNPC monogenes = npcs.get("梦金斯");

		monogenes.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"你能带给我一顶 #帽子 吗？我的头冻得不行了, 啊！啊！塞门镇在这个时节真是冷...",
			null);

		monogenes.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"谢谢你的帮助, 好兄弟, 这顶帽子好的可以让我至少撑过5个冬天, 对于我来说有点浪费了.",
			null);

		monogenes.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"谢谢我的好兄弟, 我在这等你回来！",
			new SetQuestAction(QUEST_SLOT, "start"));

		monogenes.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"你确定还有重要的事, 连这点小事都不愿帮我？我猜我快不行了.... *sniff*",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		monogenes.add(
			ConversationStates.QUEST_OFFERED,
			"帽子",
			null,
			ConversationStates.QUEST_OFFERED,
			"你不知道帽子是什么？任何一个可以盖住我头的东西！像皮毛, 你可以帮我吗?",
			null);
	}

	private void createBringingStep() {
		final SpeakerNPC monogenes = npcs.get("梦金斯");

		monogenes.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(monogenes.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new PlayerHasItemWithHimCondition("leather helmet")),
			ConversationStates.QUEST_ITEM_BROUGHT,
			"嗨！这是给我的皮帽吗？", null);

		monogenes.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(monogenes.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new NotCondition(new PlayerHasItemWithHimCondition("leather helmet"))),
			ConversationStates.ATTENDING,
			"嗨, 我的朋友, 还记得之前答应我的皮帽吗？这里真是太冷了..",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("leather helmet"));
		reward.add(new IncreaseXPAction(50));
		reward.add(new IncreaseKarmaAction(10));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		// make sure the player isn't cheating by putting the
		// helmet away and then saying "yes"
		monogenes.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("leather helmet"),
			ConversationStates.ATTENDING,
			"祝福你我的朋友！现在我的头不再冷了",
			new MultipleActions(reward));

		monogenes.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"我猜某个今天得到帽子的人一定更幸福... *sneeze*",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Hat for 梦金斯",
				"梦金斯 想要一顶帽子取暖以渡过这个寒冬.",
				false);
		createRequestingStep();
		createBringingStep();
	}

	@Override
	public String getName() {
		return "HatForMonogenes";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}
	@Override
	public String getNPCName() {
		return "梦金斯";
	}
}
