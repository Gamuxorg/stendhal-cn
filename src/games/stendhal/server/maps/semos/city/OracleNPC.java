/***************************************************************************
 *                   (C) Copyright 2003-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.city;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayNPCNamesForUnstartedQuestsAction;
import games.stendhal.server.entity.npc.action.SayUnstartedQuestDescriptionFromNPCNameAction;
import games.stendhal.server.entity.npc.condition.TriggerIsNPCNameForUnstartedQuestCondition;
import games.stendhal.server.maps.Region;

/**
 * An oracle who lets players know how they can help others.
 *
 */
public class OracleNPC implements ZoneConfigurator {

	/**
	 * region that this NPC can give information about
	 */
	private final List<String> regions = Arrays.asList(Region.SEMOS_CITY, Region.SEMOS_SURROUNDS);

	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("帕丽温克") {

			@Override
			public void createDialog() {
				addGreeting("玫瑰为红, 紫萝为蓝, 塞门镇 需要帮助, 你能做些什么？");

				// use a standard action to list the names of NPCs for quests which haven't been started in this region
				addReply(ConversationPhrases.HELP_MESSAGES, null, new SayNPCNamesForUnstartedQuestsAction(regions));

				// if the player says an NPC name, describe the quest (same description as in the travel log)
				add(ConversationStates.ATTENDING,
						"",
						new TriggerIsNPCNameForUnstartedQuestCondition(regions),
						ConversationStates.ATTENDING,
						null,
						new SayUnstartedQuestDescriptionFromNPCNameAction(regions));

				addQuest("哦, 在 " + regions + " 有其他很多需要帮助 #help 的人, 我不会再问你.");
				addJob("我没有确切的职业. 我的技能是指引你如何帮助 #help 他们, 特别是在 " + regions + " 镇里.");
				addOffer("*giggles* 我什么也不卖. 如果你喜欢, 我可以把我的 ＃name 或者我的姐妹们 #sisters 的名字告诉你.");
				addReply("sisters", "我的姐妹们住在其他的城镇, 找到他们学习如何帮助 #help 那些离她们最近的人.");
				addReply("name", "他和我的姐妹们 #sisters 都有一个花名. " +
						"我妈妈喜爱 forget-me-not 勿忘我的一种花. 好在她没有给我取名勿忘我, 所以她给我取名长春花 帕丽温克 , 因为它和勿忘我的花有几分相似. 因此, 请不要忘记我....");

				// just to be nice :)
				addEmotionReply("hugs", "hugs");
				addGoodbye("感谢你停了下来");
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(2, 29));
				nodes.add(new Node(2, 31));
				nodes.add(new Node(9, 31));
				nodes.add(new Node(9, 32));
				nodes.add(new Node(5, 32));
				nodes.add(new Node(5, 33));
				nodes.add(new Node(3, 33));
				nodes.add(new Node(3, 32));
				nodes.add(new Node(2, 32));
				setPath(new FixedPath(nodes, true));
			}
		};
		npc.setPosition(2, 29);
		npc.setDescription("你见到 帕丽温克. 她看起来心烦意乱.");
		npc.setEntityClass("oracle1npc");
		zone.add(npc);
	}

}
