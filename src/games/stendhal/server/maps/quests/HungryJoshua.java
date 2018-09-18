/***************************************************************************
 *                   (C) Copyright 2003-2014 - Stendhal                    *
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

import com.google.common.collect.ImmutableList;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CreateSlotAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EnableFeatureAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
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
 * QUEST: Hungry 约书亚
 *
 * PARTICIPANTS:
 * <ul>
 * <li> 艾克德罗斯 the blacksmith in 塞门镇</li>
 * <li> 约书亚 the blacksmith in Ados</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Talk with 艾克德罗斯 to activate the quest.</li>
 * <li> Make 5 三明治es.</li>
 * <li> Talk with 约书亚 to give him the 三明治es.</li>
 * <li> Return to 艾克德罗斯 with a message from 约书亚.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 200 XP</li>
 * <li> Karma: 10</li>
 * <li> ability to use the keyring</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> None.</li>
 * </ul>
 */
public class HungryJoshua extends AbstractQuest {
	private static final int FOOD_AMOUNT = 5;

	private static final String QUEST_SLOT = "hungry_joshua";


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
		res.add("我找到塞门镇铁匠铺的 艾克德罗斯, 如果他有任务就交给我.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("我不想帮助 艾克德罗斯 和 约书亚.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "约书亚", "done")) {
			res.add("我同意带 5 块三明治给 约书亚, 并告诉他我有这种 '食物'.");
		}
		if (questState.equals("start") && player.isEquipped("三明治",
				FOOD_AMOUNT)
				|| questState.equals("done")) {
			res.add("我找到5块 三明治 并带给了 约书亚.");
		}
		if (questState.equals("约书亚") || questState.equals("done")) {
			res.add("我带着食物找到 约书亚 ，他要我向他哥哥 艾克德罗斯 说出他的名字 约书亚' 报个平安.");
		}
		if (questState.equals("done")) {
			res.add("我把消息告诉了 艾克德罗斯, 他修好了我的钥匙.");
		}
		return res;
	}

	private void step_1() {

		final SpeakerNPC npc = npcs.get("艾克德罗斯");

		/** If quest is not started yet, start it. */
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, "我很担心住在 Ados 的兄弟. 我急需找个人给他带点 #食物 .",
			null);

		/** In case quest is completed */
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"我兄弟食物够吃了, 非常感谢.", null);

		/** In case quest is completed */
		npc.add(ConversationStates.ATTENDING, "食物",
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"我兄弟的三明治现在够吃了, 谢谢你.", null);

		/** If quest is not started yet, start it. */
		npc.add(
			ConversationStates.ATTENDING,
			"食物",
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"我觉得5块三明治就够了. 我兄弟名叫 #约书亚. 你能帮这个忙吗?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"谢谢你. 见到他时说 #食物 或 #三明治 ，让他知道你并不只是个顾客.",
			new SetQuestAction(QUEST_SLOT, "start"));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"那么你就是让他饿死! 我只能寄希望给其他好心人了.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			"约书亚",
			null,
			ConversationStates.QUEST_OFFERED,
			"他是 Ados 的金匠. 那里现在物资紧缺, 你原意帮忙吗?",
			null);

		/** Remind player about the quest */
		npc.add(ConversationStates.ATTENDING, Arrays.asList("食物", "三明治", "吃的"),
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"#约书亚 可能在挨饿! 请尽快!", null);

		npc.add(ConversationStates.ATTENDING, "约书亚",
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"我的兄弟是 Ados 的金匠.", null);

		/** remind to take the 三明治es */
		npc.add(
			ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"请别忘了带5块 #三明治 给 #约书亚!",
			null);
	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("约书亚");

		/** If player has quest and has brought the food, ask for it */
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("食物", "三明治", "吃的"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new PlayerHasItemWithHimCondition("三明治", FOOD_AMOUNT)),
			ConversationStates.QUEST_ITEM_BROUGHT,
			"Oh 太好了! 是我大哥 艾克德罗斯 托你给我送的 三明治 吗?",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("三明治", FOOD_AMOUNT));
		reward.add(new IncreaseXPAction(150));
		reward.add(new SetQuestAction(QUEST_SLOT, "约书亚"));
		reward.add(new IncreaseKarmaAction(15));
		reward.add(new InflictStatusOnNPCAction("三明治"));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("三明治", FOOD_AMOUNT),
			ConversationStates.ATTENDING,
			"谢谢你! 请告诉 艾克德罗斯 我很好, 跟大哥说我的名字 约书亚, 他就明白你已见到我了, 之后他会给你一些报酬.",
			new MultipleActions(reward));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new NotCondition(new PlayerHasItemWithHimCondition("三明治", FOOD_AMOUNT)),
			ConversationStates.ATTENDING, "嗨! 你把三明治放哪了?", null);

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.QUEST_ITEM_BROUGHT,
			"天啊, 我要饿死了, 这些吃的是给我的， #是 吗？",
			null);
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("艾克德罗斯");

		/** remind to complete the quest */
		npc.add(
			ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT, "约书亚"),
			ConversationStates.ATTENDING,
			"希望 #约书亚 没什么事...",
			null);

		/** Remind player about the quest */
		npc.add(ConversationStates.ATTENDING, Arrays.asList("食物", "三明治", "吃的"),
			new QuestInStateCondition(QUEST_SLOT, "约书亚"),
			ConversationStates.ATTENDING,
			"希望你能去确诊 #约书亚 没出什么事...", null);

		// ideally, make it so that this slot being done means
		// you get a keyring object instead what we currently
		// have - a button in the settings panel
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new IncreaseXPAction(50));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		if (System.getProperty("stendhal.container") != null) {
			reward.add(new CreateSlotAction(ImmutableList.of("belt", "back")));
			reward.add(new EquipItemAction("钥匙环", 1, true));
		} else {
			reward.add(new EnableFeatureAction("钥匙环"));
		}
		/** Complete the quest */
		npc.add(
			ConversationStates.ATTENDING, "约书亚",
			new QuestInStateCondition(QUEST_SLOT, "约书亚"),
			ConversationStates.ATTENDING,
			"听到 约书亚 没事真的太好了. 现在我能为你做点什么? 我知道了, 我会修好你的钥匙环... 好了，它现在可以用了!",
			new MultipleActions(reward));
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"饥饿的约书亚",
				"由于物资供应紧张,艾克德罗斯担心他住在 Ados 的兄弟 约书亚.",
				false);
		step_1();
		step_2();
		step_3();
	}
	@Override
	public String getName() {
		return "HungryJoshua";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "艾克德罗斯";
	}
}
