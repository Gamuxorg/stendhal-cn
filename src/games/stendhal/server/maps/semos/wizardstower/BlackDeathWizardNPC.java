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
package games.stendhal.server.maps.semos.wizardstower;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * 拉瓦夏克, the death wizard of the Wizards Tower
 *
 * see games.stendhal.server.maps.quests.WizardRavashackPlainQuest
 */
public class BlackDeathWizardNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildRavashack(zone);
	}

	private void buildRavashack(final StendhalRPZone zone) {
		final SpeakerNPC ravashack = new SpeakerNPC("拉瓦夏克") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(5, 18));
				nodes.add(new Node(7, 18));
				nodes.add(new Node(7, 15));
				nodes.add(new Node(7, 20));
				nodes.add(new Node(12, 20));
				nodes.add(new Node(12, 21));
				nodes.add(new Node(9, 21));
				nodes.add(new Node(9, 25));
				nodes.add(new Node(11, 25));
				nodes.add(new Node(9, 25));
				nodes.add(new Node(9, 26));
				nodes.add(new Node(9, 21));
				nodes.add(new Node(2, 21));
				nodes.add(new Node(2, 25));
				nodes.add(new Node(4, 25));
				nodes.add(new Node(4, 27));
				nodes.add(new Node(6, 27));
				nodes.add(new Node(4, 27));
				nodes.add(new Node(4, 28));
				nodes.add(new Node(2, 28));
				nodes.add(new Node(2, 18));
				nodes.add(new Node(7, 18));
				nodes.add(new Node(7, 15));
				nodes.add(new Node(7, 18));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("人类！欢迎你, 现在什么时间了？");
				addHelp("抱歉人类, 我正忙着在巫师界内建立死灵法师部门. ");
				addJob("我叫 拉瓦侠客. 是巫师界内 #灵魂熔炉 的死灵法师. ");
				addOffer("抱歉人类, 我正忙着在巫师界内建立死灵法师部门. ");
				addQuest("这个世界上的魔法技术刚刚起步, 我忙着在巫师界中组建 #灵魂熔炉 的死灵法师. 当需要你出力的时候, 我会及时通知你. ");
				addReply("灵魂熔炉", "在 glory 的中心地带安置着灵魂熔炉, 是一所黑暗魔法学校.");
				addGoodbye("太久了！人类！");

			} //remaining behaviour defined in maps.quests.WizardRavashackPlainQuest
		};

		ravashack.setDescription("你遇见了 拉瓦侠客, 他是个具有神秘强大魔力的死灵法师. ");
		ravashack.setEntityClass("largeblackwizardnpc");
		ravashack.setPosition(5, 17);
		ravashack.initHP(100);
		zone.add(ravashack);
	}
}
