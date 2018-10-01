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
package games.stendhal.server.maps.semos.guardhouse;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.maps.quests.BeerForHayunn;


/**
 * An old hero (original name: 海云那冉) who players meet when they enter the semos guard house.
 *
 * @see games.stendhal.server.maps.quests.BeerForHayunn
 * @see games.stendhal.server.maps.quests.MeetHayunn
 */
public class RetiredAdventurerNPC implements ZoneConfigurator {
	private static final String QUEST_SLOT="meet_hayunn";

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("海云那冉") {

			@Override
			public void createDialog() {
				// A little trick to make NPC remember if it has met
			    // player before and react accordingly
				// NPC_name quest doesn't exist anywhere else neither is
				// used for any other purpose

				final List<ChatAction> actions = new LinkedList<ChatAction>();
				actions.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
				actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, "老鼠", 0, 1));

				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestNotStartedCondition(QUEST_SLOT)),
						ConversationStates.ATTENDING,
				        "Hi. 我敢打赌, 你以前向我学过冒险课程. 先做个测试, 你去杀了外面的老鼠, 你应该可以轻易的找到一只；走之前, 要学学如何攻击吗？",
						new MultipleActions(actions));

			   	add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestCompletedCondition(QUEST_SLOT),
								new NotCondition(new QuestActiveCondition(BeerForHayunn.QUEST_SLOT))),
						ConversationStates.ATTENDING,
						"又见面了, 现在需我的 #帮助 吗？",
						null);

				addHelp("正如我说的, 我是一个退休的冒险者, 现在教新玩家, 你想知道哪些? 我可以教你");
				addJob("我的工作是指导 塞门镇的人们怎样从地牢中的怪物口中逃脱. 我现在退休了. 不能和年轻人去南方和邪恶的 Blordrough 军团战斗了. 最近一些怪物野心膨胀, 从下面跑出了地面. 塞门镇需要像你一样善良的人帮助. 你可以向城主问问有什么任务需要去做.");
				addGoodbye();
				// further behaviour is defined in quests.
			}

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 9));
				nodes.add(new Node(6, 9));
				nodes.add(new Node(6, 14));
				nodes.add(new Node(6, 9));
				nodes.add(new Node(11, 9));
				setPath(new FixedPath(nodes, true));
			}

		};
		npc.setPosition(4, 9);
		npc.setCollisionAction(CollisionAction.STOP);
		npc.setEntityClass("oldheronpc");
		npc.setDescription("你遇见了 海云那冉. 在他灰白色的头发和破旧的盔甲下面, 有着锐利的目光和结实的肌肉");
		npc.setBaseHP(100);
		npc.setHP(85);
		zone.add(npc);
	}


}
