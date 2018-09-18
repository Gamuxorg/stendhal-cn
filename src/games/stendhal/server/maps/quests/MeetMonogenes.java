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

import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Speak with 梦金斯 PARTICIPANTS: - 梦金斯
 *
 * STEPS: - Talk to 梦金斯 to activate the quest and keep speaking with
 * 梦金斯. - Be polite and say "bye" at the end of the conversation to get a
 * small reward.
 *
 * REWARD: broken (- 10 XP (check that user's level is lesser than 2) - No money)
 *
 * REPETITIONS: - None
 *
 */
public class MeetMonogenes extends AbstractQuest {
	@Override
	public String getSlotName() {
		return "Monogenes";
	}
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"会见梦金斯",
				"塞门镇一们年老的智者有一张地图, 可以指引新玩家熟悉此镇. ",
				false);
		final SpeakerNPC npc = npcs.get("梦金斯");

		npc.addGreeting(null, new SayTextAction("又见面了, [name]. 这次要 #帮助 你做点什么？"));

		// A little trick to make NPC remember if it has met
        // player before and react accordingly
        // NPC_name quest doesn't exist anywhere else neither is
        // used for any other purpose
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotCompletedCondition("Monogenes")),
				ConversationStates.INFORMATION_1,
				"你好, 过来这外地人！不要因为人们的陌生和冷漠觉得害怕... " +
				"Blordrough 令人恐惧的影响力已四处弥散, 也影响着这个城镇和我们每个人. " +
				"尽管不太熟, 我能给你一些提示, 你原意听听吗？",
				new SetQuestAction("Monogenes", "done"));

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.HELP_MESSAGES,
			null,
			ConversationStates.INFORMATION_1,
			"我能给你一些有关塞门镇居民情况的小提示,  是否你想听?",
			null);

		npc.add(
			ConversationStates.INFORMATION_1,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"你可通通说说 \"#hi\" \"#你好\" \"嗨\" \"喂\" 等方式吸引别人的注意.  " +
			"然后看看对方原意谈论的内容, 一些高亮的内容 比如 #'像这样' . " +
			"还有一些常用的对话词语可以对话, 比如问 #工作 , 寻求 #帮助 , " +
			"还有交易有关的 #买卖 或 #交易 等常用对话. 你可以继续问 #问题 . " +
			"r如果你想快速的了解下 塞门镇 的常用 #地标 , 问我就对了.",
			null);

		npc.add(
			ConversationStates.INFORMATION_1,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"想了解发生了什么事？想听听塞门镇讲坛？哈!且听下回分解. ",
			null);

		final List<String> yesnotriggers = new ArrayList<String>();
		yesnotriggers.addAll(ConversationPhrases.YES_MESSAGES);
		yesnotriggers.addAll(ConversationPhrases.NO_MESSAGES);

		npc.add(
				ConversationStates.INFORMATION_1,
				"",
				new NotCondition(new TriggerInListCondition(yesnotriggers)),
				ConversationStates.INFORMATION_1,
				"问你一个 ' #是 与 #不 ' 的问题：我可以提供一些关于社区的小提示, 你原意听听吗？",
				null);

		// he puts 'like this' into blue and so many people try that first
		npc.addReply(
				"像这样",
				"对啦, 像这样！现在, 你可以查看这张 #地图 或指引你去 #银库 ,  #图书馆 ,  #酒店 , #教堂 ,  #铁匠铺 ,  #面包房 , 或者旧 #村庄 .");

		npc.addReply(
			"地标",
			"给你一张地图, 或为你指出 #银库 ,  #图书馆 ,  #酒店 , #教堂 ,  #铁匠铺 ,  #面包房 , #公共箱 或者旧 #村庄 等地圵.");

		npc.add(
			ConversationStates.ATTENDING,
			"地图", null, ConversationStates.ATTENDING,
			"我已在这张地图上作了地标标记:\n"
			+ "1 镇政厅, 城主住在里面,   2 图书馆,   3 银库,   4 面包房,\n"
			+ "5 银库,   6 铁匠铺的卡蔓,   7 酒店的玛格丽特 \n"
        	+ "8 教堂的伊丽莎,   9 地牢 Dungeon,\n"
        	+ "10 公共箱, \n"
        	+ "A 塞门镇 村庄,   B 北部平源和矿山, \n"
        	+ "C 去 Ados 城的大路, \n"
        	+ "D 南部平原和 Nalwor 森林, \n"
        	+ "E 塞门镇 村庄 开放地带",
        	new ExamineChatAction("map-semos-city.png", "塞门镇", "塞门镇地图"));

		npc.addReply(
			"银库",
			"我前面的一所大房子, 门前放着一个很大的假宝箱. 很明显, 就是那. ");

		npc.addReply(
			"图书馆",
			"顺着路往西走, 有一个两个门的大房子, 门上面标记着书和羽毛笔");

		npc.addReply(
			"酒店",
			"顺着路东南方向就不会走错, 门前标志是INN.");

		npc.addReply(
			"教堂",
			"教堂在这的东南方向, 挨着 #酒店 . 屋顶上有个十字架, 很好找");

		npc.addReply(
			"面包房",
			"直往东走是本地的面包房；门前标志是个面包");

		npc.addReply(
			"铁匠铺",
			"西南方向走是铁匠铺. 门前标志是铁锤, 很容易找到. ");

		npc.addReply(Arrays.asList("公共", "公共箱", "箱子"),
			"顺着地图橙色的路往下走能看到 公共箱. Faiumoni的居民和勇士会把一些自已无用的东西扔进去, 你可以去找点适合你用的拿走. 但要记住：把不需要的东西分享出来总是很好的");

		npc.addReply(
			"村庄",
			"一直往西南方向走, 路过 #铁匠铺 , 然后你就进入了塞门镇 旧村庄. 尼世亚 在那里卖羊. ");


		/** Give the reward to the polite newcomer user */
		// npc.add(ConversationStates.ATTENDING,
		// SpeakerNPC.GOODBYE_MESSAGES,
		// null,
		// ConversationStates.IDLE,
		// null,
		// new SpeakerNPC.ChatAction() {
		// @Override
		// public void fire(Player player, Sentence sentence, SpeakerNPC engine) {
		// if (player.getLevel() < 2) {
		// engine.say("Goodbye! I hope I was of some use to you.");
		// player.addXP(10);
		// player.notifyWorldAboutChanges();
		// } else {
		// engine.say("I hope to see you again sometime.");
		// }
		// }
		// });
		npc.addGoodbye();
	}

	@Override
	public String getName() {
		return "MeetMonogenes";
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest("Monogenes")) {
				return res;
			}
			if (isCompleted(player)) {
				res.add("我找到了梦金斯, 他给了我一份地图. 以后我就可以随时找他看这份地图. ");
			}
			return res;
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
