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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.events.TeleportListener;
import games.stendhal.server.core.events.TeleportNotifier;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.LoadSignFromHallOfFameAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetHallOfFameToAgeDiffAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToPlayerAgeAction;
import games.stendhal.server.entity.npc.action.SetQuestToYearAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.SystemPropertyCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * A kind of paper chase.
 *
 * @author hendrik
 */
public class PaperChase extends AbstractQuest implements TeleportListener {
	private static final String QUEST_SLOT = "paper_chase_20[year]";
	private static final String FAME_TYPE = QUEST_SLOT.substring(QUEST_SLOT.length() - 1);

	private static final int TELEPORT_PENALTY_IN_MINUTES = 10;

	private static final List<String> NPC_IDLE = Arrays.asList("泰德", "哈文米特奇", "Pdiddi", "Ketteh Wehoh");

	private List<String> points = Arrays.asList("尼世亚", "马鲁斯", "荷奴姆尼瑞恩", "巴尔顿", "Rachel", "Fritz",
												"农妇爱丽丝", "伊丽莎白", "Sue", "老妇荷茉娜", "哈泽尔",
												"布朗伯德船长", "Jane", "赛位莫拉", "Phalk", "费多拉");

	private Map<String, String> texts = new HashMap<String, String>();

	private Map<String, String> greetings = new HashMap<String, String>();

	private LoadSignFromHallOfFameAction loadSignFromHallOfFame;


	private void setupGreetings() {
		// Each greeting is said by the previous NPC to point to the NPC in the key.
		greetings.put("马鲁斯", "我的羊知道你正朝我这儿来.");
		greetings.put("荷奴姆尼瑞恩", "上次有人来这是很早的事了. 你能来真好. ");
		greetings.put("巴尔顿", "啊, 当我把镰刀割下的小麦准备集中放到独轮车时你找到了我. 太好了! ");
		greetings.put("Rachel", "这儿风沙天气严重吧? 希望这次找到我的提示不要太简单.");
		greetings.put("Fritz", "我爱阿多斯银库的顾客! 他们太可爱了! ");
		greetings.put("农妇爱丽丝", "这里都是鱼腥味对吧? 那是大海的灵魂! ");
		greetings.put("伊丽莎白", "如此远的奇妙旅程, 有太多的事情要探索! ");
		greetings.put("Sue", "我喜爱巧克力! 你遇到了我, 也许下次你就会带一块给我. ");
		greetings.put("老妇荷茉娜", "周围的所有花让我感到温暖. 希望你也一样, 谢谢你的来访! ");
		greetings.put("哈泽尔", "Oh 你好, 在这找到我真好. 快来加入我的煲汤工作, 我给你做点好吃的汤. ");
		greetings.put("布朗伯德船长", "博物馆真是个工作的好地方, 在这找到我真是不错. ");
		greetings.put("Jane", "呀! 我的船会带你到海上航行, 航行! *sing* ");
		greetings.put("赛位莫拉", "海边很热, 建议你用用 防晒油. ");
		greetings.put("Phalk", "这里的花太美了! 不幸的是这此精灵们不太懂欣赏. ");
		greetings.put("费多拉", "小伙子, 你在旅途中有太多事要做! 现在去完成它们. 你一定很着急! ");
	}


	private void setupTexts() {
		texts.put("马鲁斯", "你要找的下个人是, 注意着小偷和其他罪犯的人. "
				  + "他在塞门镇附近工作.");
		texts.put("荷奴姆尼瑞恩", "我下个要找的是, 阿多斯农场找拿着半月形东西的精灵, 还有. 他总是忙着收割小麦.");
		texts.put("巴尔顿", "你要找的下个人是, 坐在一个多风的山顶上的某个人.");
		texts.put("Rachel", "一个要找的是一位在银行工作的女士, 她对她的工作知无不言.");
		texts.put("Fritz", "请去阿多斯找到一位老渔民, 他会讲很多有关鱼的故事, 还有个女儿叫卡若琳.");
		texts.put("农妇爱丽丝", "你要找到的下个人是在阿多斯休假的人, 正和她全家一起. 她也了解各种食物和饮料.");
		texts.put("伊丽莎白", "现在你要找一位女孩, 她在科徳内的广场玩, 而且喜欢巧克力.");
		texts.put("Sue", "请找一位好园丁, 他在卡拉文附近拥有种着西红柿的绿房子.");
		texts.put("老妇荷茉娜", "现在请去找一位好心老妇, 她做的汤很有名, 可以温暖你的心. 她可能会先问你汤如何, 不用太在意她就好 :)");
		texts.put("哈泽尔", "我知道一位好心女士, 她可以帮你进行到下一环节. 她在某个博物馆并喜爱着这项工作.");
		texts.put("布朗伯德船长", "现在你要去一个渡口, 并且与这个老水手谈谈, 他会提示你下个要找的人.");
		texts.put("Jane", "哈呀下个要找的是一位女士, 她正与她丈夫一起在阿托尔海滨度假.");
		texts.put("赛位莫拉", "你要找的下个人不久前开了一家花店. 我看到很多长耳动物在她周围, 藏在一个森林边的城市里.");
		texts.put("Phalk", "你要找的下个人是一位老战士, 他守卫着塞门镇北方的矿山.");
		texts.put("费多拉", "最后要找的人, 现在就站在这.");
	}

	/**
	 * Handles all normal points in this paper chase (without the first and last.
	 * one)
	 */
	private class PaperChasePoint implements ChatAction {
		private final int idx;

		PaperChasePoint(final int idx) {
			this.idx = idx;
		}

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
			final String state = points.get(idx);
			final String next = points.get(idx + 1);
			final String questState = player.getQuest(QUEST_SLOT, 0);

			// player does not have this quest or finished it
			if (questState == null) {
				raiser.say("请与塞门镇北方矿镇的费多拉说, 开始 纸片人生.");
				return;
			}

			final String nextNPC = questState;

			// is the player supposed to speak to another NPC?
			if (!nextNPC.equals(state)) {
				raiser.say("你说的什么? \"" + texts.get(nextNPC) + "\" 明显不是我.");
				return;
			}

			// send player to the next NPC and record it in quest state
			raiser.say(greetings.get(next) + texts.get(next) + " 好运!");
			player.setQuest(QUEST_SLOT, 0, next);
			player.addXP((idx + 1) * 10);
		}

	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	/**
	 * Adds the task to the specified NPC. Note that the start and end of this
	 * quest have to be coded specially.
	 *
	 * @param idx
	 *            index of way point
	 */
	private void addTaskToNPC(final int idx) {
		final String state = points.get(idx);
		final SpeakerNPC npc = npcs.get(state);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("纸片人生"), new SystemPropertyCondition("stendhal.minetown"),
				ConversationStates.ATTENDING, null, new PaperChasePoint(idx));
		if (NPC_IDLE.contains(state)) {
			npc.add(ConversationStates.ANY, Arrays.asList("纸片人生"), new SystemPropertyCondition("stendhal.minetown"),
					ConversationStates.ANY, null, new PaperChasePoint(idx));
		}
	}


	private void createHallOfFameSign() {
		loadSignFromHallOfFame = new LoadSignFromHallOfFameAction(null, "这些以费多拉的名义在世界旅行的人:\n", FAME_TYPE, 2000, true);
		loadSignFromHallOfFame.fire(null, null, null);
	}

	/**
	 * sets the sign to show the hall of fame
	 *
	 * @param sign a Sign or <code>null</code>.
	 */
	public void setSign(Sign sign) {
		loadSignFromHallOfFame.setSign(sign);
		loadSignFromHallOfFame.fire(null, null, null);
	}

	public void addToStarterNPCs() {
		SpeakerNPC npc = npcs.get("费多拉");

		ChatAction startAction = new MultipleActions(
			new SetQuestAction(QUEST_SLOT, 0, points.get(0)),
			new SetQuestToPlayerAgeAction(QUEST_SLOT, 1),
			new SetQuestToYearAction(QUEST_SLOT, 2));

		// Fidorea introduces the quests
		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new SystemPropertyCondition("stendhal.minetown")),
			ConversationStates.ATTENDING,
			"我还没有需要你做的事情, 但还是谢谢关心.",
			null);
		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new QuestNotStartedCondition(QUEST_SLOT), new SystemPropertyCondition("stendhal.minetown")),
			ConversationStates.QUEST_OFFERED,
			"这些因为职责而呆在家的人们, 共同制作了 #纸片人生 游戏.",
			null);
		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("纸片人生"),
			new SystemPropertyCondition("stendhal.minetown"),
			ConversationStates.ATTENDING,
			"你必须根据线索访问 #纸片人生 中的每个人. 你的旅行在 塞门镇 村庄开始, 就是你看到卖羊人的地方. "
			+ "还有个警告: 旅途中要以传送, 但每次传送要计算 " + TELEPORT_PENALTY_IN_MINUTES + " 分钟到最终计时得分.",
			startAction);


		// add normal way points (without first and last)
		for (int i = 0; i < points.size() - 1; i++) {
			addTaskToNPC(i);
		}

		// Fidorea does the post processing of this quest
		npc.add(ConversationStates.ATTENDING, Arrays.asList("纸片人生"),
				new AndCondition(new QuestNotStartedCondition(QUEST_SLOT), new SystemPropertyCondition("stendhal.minetown")),
			ConversationStates.ATTENDING, "Oh, 不错的 #任务 .", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("纸片人生"),
			new AndCondition(
					new QuestStartedCondition(QUEST_SLOT),
					new QuestNotInStateCondition(QUEST_SLOT, 0, "费多拉"),
					new QuestNotInStateCondition(QUEST_SLOT, 0, "done"),
					new SystemPropertyCondition("stendhal.minetown")),
			ConversationStates.ATTENDING, "我猜你还要与某个人谈谈.", null);

		ChatAction reward = new MultipleActions(
			new IncreaseKarmaAction(15),
			new IncreaseXPAction(400),
			new SetQuestAction(QUEST_SLOT, 0, "done"),
			new EquipItemAction("空白卷轴", 5),
			new SetHallOfFameToAgeDiffAction(QUEST_SLOT, 1, FAME_TYPE),
			loadSignFromHallOfFame);

		npc.add(ConversationStates.ATTENDING, Arrays.asList("纸片人生"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "费多拉"), new SystemPropertyCondition("stendhal.minetown")),
			ConversationStates.ATTENDING,
			"很好, 你完成了任务, 与遍布世界的所有人说了话. 我会把你的名字记下来展示给所有人. 这有张魔法卷轴作为奖励. 他们会在你今后的旅程中帮到你.",
			reward);
	}


	@Override
	public void addToWorld() {
		fillQuestInfo(
				"纸片人生",
				"在 Faiumoni 有着这样的传闻. 可能有些住在那里的人知道一些事情.",
				false);
		setupGreetings();
		setupTexts();
		createHallOfFameSign();
		TeleportNotifier.get().registerListener(this);
	}


	@Override
	public String getName() {
		return "PaperChase";
	}

	@Override
	public boolean isVisibleOnQuestStatus() {
		return false;
	}

	@Override
	public List<String> getHistory(final Player player) {
		return new ArrayList<String>();
	}


	@Override
	public void onTeleport(Player player, boolean playerAction) {
		if (!playerAction) {
			return;
		}

		if (player.hasQuest(QUEST_SLOT) && !player.getQuest(QUEST_SLOT, 0).equals("done")) {
			int startAgeWithPenalty = MathHelper.parseIntDefault(player.getQuest(QUEST_SLOT, 1), 0) - TELEPORT_PENALTY_IN_MINUTES;
			player.setQuest(QUEST_SLOT, 1, Integer.toString(startAgeWithPenalty));
		}
	}


	@Override
	public String getNPCName() {
		return "费多拉";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
}
