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
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Bows for 奥斯特
 *
 * PARTICIPANTS:
 * <ul>
 * <li> 奥斯特, ranged items seller</li>
 * <li> 卡尔, farmer</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> 奥斯特 asks for wood for his bows and arrows. </li>
 * <li> Puchit asks you to fetch 鬃毛 from 卡尔 also.</li>
 * <li> Return and you get some equipment as reward.<li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 1 XP<li>
 * <li> 鳞甲</li>
 * <li> 索链护腿</li>
 * <li> Karma: 14<li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> None.</li>
 * </ul>
 */

public class BowsForOuchit extends AbstractQuest {

	public static final String QUEST_SLOT = "bows_ouchit";

	public void prepareQuestStep() {

		/*
		 * get a reference to the 奥斯特 NPC
		 */
		SpeakerNPC npc = npcs.get("奥斯特");

		/*
		 * Add a reply on the trigger phrase "quest" to 奥斯特
		 */
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				"能过来帮个忙吗？",
				null);

		/*
		 * Player is interested in helping, so explain the quest.
		 */
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"不错！我销售弓和箭, 如果你能带来10根木头就太好了.  " +
				"能带来吗?",
				null);

		/*
		 * Player refused to help - end the conversation.
		 */
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"哦, 好吧,再见.",
				null);

		/*
		 * Player agreed to get wood, so tell them what they'll need to say
		 */
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"很好 :-) 当你找够木头时回来说 #木头.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "木头", 2.0));

		/*
		 * Player asks about wood.
		 */
		npc.add(ConversationStates.QUEST_OFFERED,
				"木头",
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"木头可以用在很多地方. 当然是在森林中可以找到它, 你可以带10根过来吗?",
				null);

		/*
		 * Player refused to help - end the conversation.
		 */
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"好吧, 当你想帮我的时候再过来, 再见了！",
				null);
	}

	public void bringWoodStep() {

		/*
		 * get a reference to the 奥斯特 NPC
		 */
		SpeakerNPC npc = npcs.get("奥斯特");

		/*
		 * Player asks about quest, remind what they're doing
		 */
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT,"木头"),
				ConversationStates.ATTENDING,
				"我还在等你带来10根 #木头",
				null);

		/*
		 * Player asks about wood, but hasn't collected any - remind them.
		 */
		npc.add(ConversationStates.ATTENDING,
				"木头",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"木头"),
								 new NotCondition (new PlayerHasItemWithHimCondition("木头",10))),
				ConversationStates.ATTENDING,
				"木头可以用在很多地方. 当然是在森林中可以找到它, 当你找够10根木头时, 请记得回来对我说 #木头",
				null);

		/*
		 * Player asks about wood, and has collected some - take it and
ask for 鬃毛.
		 */
		npc.add(ConversationStates.ATTENDING,
				"木头",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"木头"),
								new PlayerHasItemWithHimCondition("木头",10)),
				ConversationStates.ATTENDING,
				"太好了, 现在我可以制作箭了. 但制作弓还需要弓弦. 还请找到 #Karl, 他养了很多马, 如果你把我的名字告诉他, 他会从马尾上剪些 #'鬃毛' 给你. ",
				new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "hair", 2.0), new DropItemAction("木头", 10)));

		/*
		 * For simplicity, respond to '卡尔' at any time.
		 */
		npc.addReply("卡尔", "卡尔 是个农未, 在 塞门镇 的东面, 他的农场养了很多宠物. ");
	}

	public void getHairStep() {

		/*
		 * get a reference to the 卡尔 NPC
		 */
		SpeakerNPC npc = npcs.get("卡尔");

		npc.add(ConversationStates.ATTENDING,
				"奥斯特",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"hair"),
								new NotCondition (new PlayerHasItemWithHimCondition("鬃毛",1))),
				ConversationStates.ATTENDING,
				"你好, 你好！Ouchit要些 鬃毛？没问题, 拿去吧. 顺便带我向 奥斯特 问个好. ",
				new EquipItemAction("鬃毛"));

	}

	public void bringHairStep() {

		/*
		 * get a reference to the 奥斯特 NPC
		 */
		SpeakerNPC npc = npcs.get("奥斯特");

		/*
		 * Player asks about quest, remind what they're doing
		 */
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT,"hair"),
				ConversationStates.ATTENDING,
				"我正等着你带来 #'鬃毛'.",
				null);

		/*
		 * Player asks about 鬃毛, but hasn't collected any - remind them.
		 */
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("hair", "马尾", "鬃毛"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"hair"),
								new NotCondition (new PlayerHasItemWithHimCondition("鬃毛"))),
				ConversationStates.ATTENDING,
				"鬃毛可以制作弓弦. 请代我找 #卡尔 取一些过来. ",
				null);

		/*
		 * These actions are part of the reward
		 */
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("鬃毛"));
		reward.add(new EquipItemAction("鳞甲", 1, true));
		reward.add(new EquipItemAction("索链护腿", 1, true));
		reward.add(new IncreaseXPAction(100));
		reward.add(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "done", 10.0));

		/*
		 * Player asks about 鬃毛, and has collected some - take it
and ask for 鬃毛.
		 */
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("hair", "马尾", "鬃毛"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"hair"),
								new PlayerHasItemWithHimCondition("鬃毛")),
				ConversationStates.ATTENDING,
				"呀！你把 鬃毛 带来了, 非常感谢. Karl 真是好人. 这是之前有人落下的东西, 放我这没用, 送给你了, 这是你应得的. ",
				new MultipleActions(reward));

		/*
		 * Player asks about quest, and it is finished
		 */
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"感谢你的帮助. 如果需要我的帮助, 尽管说",
				null);

	}

	@Override
	public void addToWorld() {
		prepareQuestStep();
		bringWoodStep();
		getHairStep();
		bringHairStep();
		fillQuestInfo(
				"奥斯特 的弓",
				"奥斯特 忙于制造和销售弓和箭!",
				false);
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("奥斯特 让我帮他找一些弓箭的原材料. ");
		if (player.isQuestInState(QUEST_SLOT, "木头", "hair", "done")) {
			res.add("首先我必须找10根木头给 Ouchit. ");
		}
		if(player.isEquipped("木头", 10) && "木头".equals(questState)) {
			res.add("我集齐了木头, 并交给 奥斯特.");
		}
		if(player.isQuestInState(QUEST_SLOT, "hair", "done")) {
			res.add("然后我需要拿一些 鬃毛,Ouchit要用它做弓弦. 他告诉我找到农夫 卡尔 可以弄到.");
		}
		if(player.isEquipped("鬃毛") && "hair".equals(questState) || isCompleted(player)) {
			res.add("卡尔 真是好人, 直接给了我一些 鬃毛.");
		}
		if (isCompleted(player)) {
			res.add("作为谢礼, Ouchit 给了我一个新装备");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "BowsForOuchit";
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
		return "奥斯特";
	}
}