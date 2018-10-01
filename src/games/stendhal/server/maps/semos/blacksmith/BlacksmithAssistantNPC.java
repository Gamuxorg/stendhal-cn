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
package games.stendhal.server.maps.semos.blacksmith;

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
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.CollisionAction;

/**
 * The blacksmith's young assistant (original name: 黑姆伊索).
 * He smuggles out weapons.
 *
 * @see games.stendhal.server.maps.quests.MeetHackim
 */
public class BlacksmithAssistantNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("黑姆伊索") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
                nodes.add(new Node(5,2));
                nodes.add(new Node(5,5));
                nodes.add(new Node(10,5));
                nodes.add(new Node(10,9));
                nodes.add(new Node(7,9));
                nodes.add(new Node(7,12));
                nodes.add(new Node(3,12));
                nodes.add(new Node(3,8));
                nodes.add(new Node(9,8));
                nodes.add(new Node(9,5));
                nodes.add(new Node(12,5));
                nodes.add(new Node(12,2));
                nodes.add(new Node(15,2));
                nodes.add(new Node(15,5));
                nodes.add(new Node(5,5));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {

				// A little trick to make NPC remember if it has met
		        // player before and react accordingly
		        // NPC_name quest doesn't exist anywhere else neither is
		        // used for any other purpose
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestNotStartedCondition("meet_hackim")),
				        ConversationStates.ATTENDING,
				        "Hi 陌生人, 我是黑姆伊索, 铁匠铺的助理. 你来这是想买些武器吗？",
				        new SetQuestAction("meet_hackim","start"));

				addGreeting(null, new SayTextAction("Hi 又见面了, [name]. 你现在能 #帮助 我吗?"));

				addHelp("我是铁匠铺的助理, 告诉我...你来这是要买武器吗？");
				addJob("我帮助 艾克德罗斯 在铁匠铺为 Deniran 的军队制作武器. 我大部分工作是给火添煤, 或者把武器放到架子上. 偶尔当 艾克德罗斯 不注意时, 我喜欢用其中一把剑, 假装是一个著名的冒险家!");
				addOffer("你可以问问 艾克德罗斯. 他销售一些自制武器.");
				addGoodbye();
			}

		};
		npc.setPosition(5, 2);
		npc.setCollisionAction(CollisionAction.REVERSE); // prevent trapping players
		npc.setEntityClass("naughtyteennpc");
		npc.setDescription("你见到了黑姆伊索, 塞门铁匠铺的助手");
		zone.add(npc);
	}
}


