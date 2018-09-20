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
package games.stendhal.server.maps.semos.library;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;

public class HistorianGeographerNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSemosLibraryArea(zone);
	}

	private void buildSemosLibraryArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Zynn Iwuhos") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(15, 3));
				nodes.add(new Node(12, 3));
				nodes.add(new Node(12, 6));
				nodes.add(new Node(13, 6));
				nodes.add(new Node(13, 7));
				nodes.add(new Node(13, 6));
				nodes.add(new Node(15, 6));
				nodes.add(new Node(15, 7));
				nodes.add(new Node(15, 6));
				nodes.add(new Node(17, 6));
				nodes.add(new Node(17, 7));
				nodes.add(new Node(17, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting(null, new SayTextAction("又见面了, [name]. 这次要我帮 #help 你点什么？"));
				addGoodbye();

				// A little trick to make NPC remember if it has met
		        // player before and react accordingly
		        // NPC_name quest doesn't exist anywhere else neither is
		        // used for any other purpose
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestNotCompletedCondition("Zynn")),
						ConversationStates.ATTENDING,
						"Hi, 潜在读者！这里记录了塞门镇的历史, 还有很多关于 Faiumoni 岛的有趣的真相. 如果你喜欢, 我能为你提供此地的 #地理 和 #历史 的简短的介绍.  #geography and #history! 我还关注着本地的 #新闻 #news, 你可以随时问我有关此方面的知识.",
						new SetQuestAction("Zynn", "done"));

				addHelp("我能为你提供最好的帮助, 把我对于 Faiumoni 的 #地理 和 #历史 #geography and #history 的知识分享给你, 还有最新的 #消息 #news . ");
				addJob("我是一个历史和地理学家, 我记录并保留了关于 Faiumoni 的每件事的真相, 你知道在这个图书管写最多书的人是我吗？ 好吧, 对于 \"明白如何杀死怪物\"  这一书, 当然...这是由 海云那冉 写的.");

				addQuest("我不认为目前您还不能帮我做一些具体的事, 但还是谢谢你的关心!");

				add(ConversationStates.ATTENDING, Arrays.asList("offer", "buy", "trade", "deal", "scroll", "scrolls", "home", "empty",
				        "marked", "summon", "magic", "wizard", "sorcerer"), null, ConversationStates.ATTENDING,
				        "我不再销售卷轴了... 我的供应商 #海震. 惹出了个大麻烦.", null);

				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("海震", "海震."),
				        null,
				        ConversationStates.ATTENDING,
				        "海震? 他是一个住在 塞门镇 和 Ados之间的小房子的巫师. 我以前常去那里卖卷轴给他, 但他有个条件, 恐怕你不得不一个人去找他.",
				        null);
			}
		};

		npc.setEntityClass("wisemannpc");
		npc.setDescription("你遇见了 Zynn Iwuhos. 他看起来比他家四周挂着的破旧地图还苍老");
		npc.setPosition(15, 3);
		npc.initHP(100);
		zone.add(npc);
	}
}
