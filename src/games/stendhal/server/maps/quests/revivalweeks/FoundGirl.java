/***************************************************************************
 *                   (C) Copyright 2003-2016 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.revivalweeks;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestToYearAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestSmallerThanCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.TriggerExactlyInListCondition;
import games.stendhal.server.maps.ados.rosshouse.LittleGirlNPC;

public class FoundGirl implements LoadableContent {
	private SpeakerNPC npc;
	private ChatCondition noFriends;
	private ChatCondition anyFriends;
	private ChatCondition oldFriends;
	private ChatCondition currentFriends;

	private void buildConditions() {
		noFriends = new QuestNotStartedCondition("susi");
		anyFriends = new QuestStartedCondition("susi");
		oldFriends = new OrCondition(
				new QuestInStateCondition("susi", "friends"),
				new QuestSmallerThanCondition("susi", Calendar.getInstance().get(Calendar.YEAR)));
		currentFriends = new QuestInStateCondition("susi", Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
	}

	final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_semos_frank_house");

	private void createGirlNPC() {

		npc = new SpeakerNPC("苏茜") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 17));
				nodes.add(new Node(4, 27));
				nodes.add(new Node(7, 27));
				nodes.add(new Node(7, 17));
				setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				// done outside
			}
		};

		//	npcs.add(npc);
		npc.setOutfit(new Outfit(0, 4, 7, 32, 13));
		npc.setPosition(4, 17);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		npc.setSpeed(1.0);
		zone.add(npc);
	}

	private void addDialog() {

		// greeting
		addGreetingDependingOnQuestState();

		npc.addJob("我只是个等着爸爸带我出去的小女孩. 我们会在 #矿镇复兴展会周 找到很多乐趣!");
		npc.addGoodbye("玩的开心!");
		npc.addHelp("好好玩.");
		npc.addOffer("你这个 #朋友 我交了.");

		// Revival Weeks
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("矿镇复兴展会", "矿镇复兴展会周", "矿镇", "复兴展会"),
			ConversationStates.ATTENDING,
			"矿镇复兴展会周期间, 我们会在塞门镇现在几乎废弃的旧矿区举办 #庆祝 #宴会."
			+ "宴会于几天后结束, 因为在我消失后阿多斯的人们就会寻找我."
			+ "现在我发现我们又能办宴会了!",
			null);
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("庆祝", "庆祝宴会", "宴会"),
			ConversationStates.ATTENDING,
			"你可以找屋外的费多拉要一个面具, 或者还可以在其它房间解决迷题. 或者与你的朋友玩一把 井字棋 , 或者向 马尔托斯 问这个游戏的玩法.",
			null);

		// friends
		npc.add(
			ConversationStates.ATTENDING, Arrays.asList("朋友", "友谊"),
			new QuestInStateCondition("苏茜", Integer.toString(Calendar.getInstance().get(Calendar.YEAR))),
			ConversationStates.ATTENDING,
			"感谢能成为朋友.", null);

		addFirstQuest();
		addSecondQuest();

		// quest
		addQuest();
	}


	private void addGreetingDependingOnQuestState() {
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						noFriends),
				ConversationStates.ATTENDING,
				"猜猜看, 我们正在办另一场 #矿镇复兴展会周 .", null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						anyFriends),
				ConversationStates.ATTENDING,
				null, new SayTextAction("你好 [name], 又见面了. "
						+ "猜猜看, 我们正在办另一场 #矿镇复兴展会周 ."));
		// TODO: Tell old friends about renewal
	}


	private void addQuest() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				noFriends,
				ConversationStates.ATTENDING, "我需要一个 #朋友.", null);
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				oldFriends,
				ConversationStates.ATTENDING, "我们应重新确定我们的 #友谊.", null);
		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				currentFriends,
				ConversationStates.ATTENDING,
				"我在 #矿镇复兴展会周 交了很多朋友.",
				null);
	}

	private void addFirstQuest() {
		// initial friends quest
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("朋友", "友谊"),
			noFriends,
			ConversationStates.INFORMATION_1,
			"请跟着念:\r\n                        \"环是圆的,\"",
			null);
		npc.add(ConversationStates.INFORMATION_1,
			"",
			new TriggerExactlyInListCondition("环是圆的,", "环是圆的"),
			ConversationStates.INFORMATION_2, "\"没有结尾.\"",
			null);
		npc.add(ConversationStates.INFORMATION_2,
			"",
			new TriggerExactlyInListCondition("没有结尾.", "没有结尾"),
			ConversationStates.INFORMATION_3,
			"\"到底多长,\"", null);
		npc.add(ConversationStates.INFORMATION_3,
			"",
			new TriggerExactlyInListCondition(
				"到底多长,", "到底多长",
				"到底多长。"),
			ConversationStates.INFORMATION_4,
			"\"我们是朋友.\"", null);

		ChatAction reward = new MultipleActions(new IncreaseKarmaAction(10), new IncreaseXPAction(25), new SetQuestToYearAction("susi"));
		npc.add(ConversationStates.INFORMATION_4,
			"",
			new TriggerExactlyInListCondition("我们是朋友.", "我们是朋友"),
			ConversationStates.ATTENDING,
			"耶! 现在我们是朋友了.",
			reward);
	}

	private void addSecondQuest() {
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("朋友", "友谊"),
				oldFriends,
				ConversationStates.INFORMATION_5,
				"请跟着念:\r\n                        \"交个新朋友,\"",
				null);
		npc.add(ConversationStates.INFORMATION_5,
				"",
				new TriggerExactlyInListCondition("交个新朋友,", "交个新朋友"),
				ConversationStates.INFORMATION_6, "\"不忘旧朋友.\"",
				null);
		npc.add(ConversationStates.INFORMATION_6, "",
				new TriggerExactlyInListCondition("不忘旧朋友.", "不忘旧朋友"),
				ConversationStates.INFORMATION_7, "\"一面是银,\"",
				null);
		npc.add(ConversationStates.INFORMATION_7, "",
				new TriggerExactlyInListCondition("一面是银,", "一面是银"),
				ConversationStates.INFORMATION_8, "\"反面是金.\"",
				null);

		// lowercase "and" is ignored, even in full match mode
		ChatAction reward = new MultipleActions(new IncreaseKarmaAction(15), new IncreaseXPAction(50), new SetQuestToYearAction("susi"));
		npc.add(ConversationStates.INFORMATION_8, "",
				new TriggerExactlyInListCondition("反面是金.", "反面是金"),
				ConversationStates.ATTENDING,
				"耶! 现在我们是更好的朋友了.",
				reward);
	}

	/**
	 * removes an NPC from the world and NPC list
	 *
	 * @param name name of NPC
	 */
	private void removeNPC(String name) {
		SpeakerNPC npc = NPCList.get().get(name);
		if (npc == null) {
			return;
		}
		npc.getZone().remove(npc);
	}


	/**
	 * removes 苏茜 from her home in Ados and adds her to the Mine Towns.
	 */
	@Override
	public void addToWorld() {
		removeNPC("苏茜");

		buildConditions();
		createGirlNPC();
		addDialog();
	}


	/**
	 * removes 苏茜 from the Mine Town and places her back into her home in Ados.
	 *
	 * @return <code>true</code>, if the content was removed, <code>false</code> otherwise
	 */
	@Override
	public boolean removeFromWorld() {
		removeNPC("苏茜");

		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_阿多斯_罗斯_小屋");
		new LittleGirlNPC().createGirlNPC(zone);

		return true;
	}
}
