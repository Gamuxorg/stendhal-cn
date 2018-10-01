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
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Speak with Io PARTICIPANTS: - Io
 *
 * STEPS: - Talk to Io to activate the quest and keep speaking with Io.
 *
 * REWARD: - 10 XP - 5 gold coins
 *
 * REPETITIONS: - As much as wanted, but you only get the reward once.
 */
public class MeetIo extends AbstractQuest {

	private static final String QUEST_SLOT = "meet_io";



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
		res.add("我在塞门镇神殿遇见了艾欧弗鲁托.");
		if (isCompleted(player)) {
			res.add("艾欧告诉我六种基本元素, 并保证如果需要更新知识时会提醒我.");
		}
		return res;
	}

	private void prepareIO() {

		final SpeakerNPC npc = npcs.get("艾欧弗鲁托");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.HELP_MESSAGES,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"我是一个传心术师; 我可以把我的精神技能教给你. 你想学习这6种基本元素能力吗? 我想我已经知道答案了, 但出于礼貌我不说...",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.HELP_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"你想学习这6种元素的能力吗? 我已知道答案但我不说...",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_1,
			"输入 #/who 查明当前在这个世界中活动的玩家. 你想学习第二种基本的传心术吗?",
			null);

		npc.add(
			ConversationStates.INFORMATION_1,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_2,
			"输入 #/where #用户名 洞悉某人当前的位置; 也可以任用 #'/where sheep' 追踪你支配的羊的位置. 要高清楚这个系统的定位方法, 可以问问 #Zynn; 他比我了解的更清楚. 准备好第三课了吗?",
			null);

		npc.add(
			ConversationStates.INFORMATION_2,
			"Zynn",
			null,
			ConversationStates.INFORMATION_2,
			"他的全名叫 震爱武豪斯. 他在图书管花了大量时间绘制了地图, 还写了史书. 要学下一课吗?",
			null);

		npc.add(
			ConversationStates.INFORMATION_2,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_3,
			"输入 #'/tell 用户名 消息内容' 或者  #'/msg 用户名 消息内容' 可以与某人通话, 而不论对方在哪里.  你可能输入 #'// 回话' 与上个对话者继续通话. 准备好学习下一课了吗?",
			null);

		npc.add(
			ConversationStates.INFORMATION_3,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_4,
			"输入 #Shift+Up 可以重复最后一次输入的内容, 方便你重复发些东西. Okay, 我们开始第5课吧?",
			null);

		npc.add(
			ConversationStates.INFORMATION_4,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_5,
			"输入 #/support #<message> 向网管报告问题. 你也可以试试到 #'irc.freenode.net' 的IRC 频道 ##arianne. 它有个WEB前端可以直接用 #https://stendhalgame.org/development/chat.html \nOkay, 接下来是精神操作的最后一课!",
			null);

		npc.add(
			ConversationStates.INFORMATION_5,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_6,
			"你可以随时去精神世界旅行, 因此保存并关掉游戏只用输入 #/quit, 或者按下 #Esc 键, 或者直接关闭游戏窗口. Okay! 嗯, 我想你一定想学习如何像我一样飘在空中.",
			null);

		/** Give the reward to the patient newcomer user */
		final String answer = "*yawns* 也许我以后会教你... 我不想给你太多压力. 让你一次学的太多. 你可以随时查看刚才课程的目录, 只需输入 #/help.\n";
		npc.add(ConversationStates.INFORMATION_6,
			ConversationPhrases.YES_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.IDLE,
			answer + "嗨! 我知道你在想什么, 我并不喜欢这样!",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new EquipItemAction("money", 10));
		reward.add(new IncreaseXPAction(10));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(ConversationStates.INFORMATION_6,
			ConversationPhrases.YES_MESSAGES,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.IDLE,
			answer + "记住, 不要让其他事让你分心.",
			new MultipleActions(reward));

		npc.add(
			ConversationStates.ANY,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"如果你决定扩宽知识面, 随时说 你好 .再会!",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"会见艾欧弗鲁托",
				"艾欧弗鲁托 教会你如何交流.",
				false);
		prepareIO();
	}

	@Override
	public String getName() {
		return "MeetIo";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "艾欧弗鲁托";
	}
}
