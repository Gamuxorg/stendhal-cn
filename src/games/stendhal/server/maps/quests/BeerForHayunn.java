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
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: 啤酒 For 海云那冉
 *
 * PARTICIPANTS:
 * <ul>
 * <li>海云那冉 (the veteran warrior in 塞门镇)</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>海云那冉 asks you to buy a 啤酒 from 玛格丽特.</li>
 * <li>玛格丽特 sells you a 啤酒.</li>
 * <li>海云那冉 sees your 啤酒, asks for it and then thanks you.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>50 XP</li>
 * <li>20 gold coins</li>
 * <li>Karma: 10</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class BeerForHayunn extends AbstractQuest {
	public static final String QUEST_SLOT = "beer_hayunn";
	private static final String OTHER_QUEST_SLOT = "meet_hayunn";



	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("我和 海云那冉 谈了些话. ");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("我不想让 海云那冉 喝醉.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("我答应海云那冉, 从塞门镇 酒店的 玛格丽特 处买瓶啤酒给他.");
		}
		if ("start".equals(questState) && player.isEquipped("啤酒")
				|| "done".equals(questState)) {
			res.add("我有一瓶啤酒. ");
		}
		if ("done".equals(questState)) {
			res.add("我把啤酒给了 海云那冉. 他付给我20金币, 我还增加了一些经验. ");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("海云那冉");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			// Don't give the task until the previous is completed to avoid
			// confusing 海云那冉 in a lot of places later.
			new AndCondition(new QuestNotCompletedCondition(QUEST_SLOT),
					new QuestCompletedCondition(OTHER_QUEST_SLOT)),
			ConversationStates.QUEST_OFFERED,
			"我口快渴死了, 但我不能斿离开这间教育半步！你能去 #酒店 带点 #啤酒 给我吗?",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"还是谢谢你, 但我不想喝太多；你要明白, 我还是有责任感的！如果有学生出现, 我还要保持师德...",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"谢谢！我保证在这等着, 绝对. ",
			new SetQuestAction(QUEST_SLOT, "start"));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Oh, 就当我没说, 我现在只希望下场雨, 然后我张大嘴接点. ",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			"酒店",
			null,
			ConversationStates.QUEST_OFFERED,
			"如果你不知酒店在哪儿, 如可以问问老 梦金斯; 他是个不错的向导. 你要去问问吗？",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			"啤酒",
			null,
			ConversationStates.QUEST_OFFERED,
			"#玛格丽特 卖的冰镇啤酒就够了, 怎么样, 你愿意去吗？",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			"玛格丽特",
			null,
			ConversationStates.QUEST_OFFERED,
			"玛格丽特 是酒店的可爱妹纸, 当然也非常漂亮！哈哈, 你是不是要去看看？顺便买点啤酒. ",
			null);
	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("海云那冉");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestActiveCondition(QUEST_SLOT),
					new PlayerHasItemWithHimCondition("啤酒")),
			ConversationStates.QUEST_ITEM_BROUGHT,
			"嗨！这是给我的酒吗？", null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestActiveCondition(QUEST_SLOT),
					new NotCondition(new PlayerHasItemWithHimCondition("啤酒"))),
			ConversationStates.ATTENDING,
			"嗨, 我还在等着啤酒呢, 没忘吧？还有, 有什么需要我帮助的？",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("啤酒"));
		reward.add(new EquipItemAction("money", 20));
		reward.add(new IncreaseXPAction(50));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(10));
		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("啤酒"),
			ConversationStates.ATTENDING,
			"*glug glug* 咕嘟, 咕嘟, 啊！正合我意. 如果你需要什么尽管说, ok?",
			new MultipleActions(reward));

		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"该死！要记着你的保证, 对吧？现在我真的急需!",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"海云那冉 的啤酒",
				"海云那冉,守卫室的很棒的勇士, 他想喝点啤酒",
				false);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "BeerForHayunn";
	}

	public String getTitle() {

		return "海云那冉的啤酒";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "海云那冉";
	}
}
