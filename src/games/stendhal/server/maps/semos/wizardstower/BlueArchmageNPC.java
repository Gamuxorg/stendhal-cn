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
 * 伊拉斯塔斯, the archmage of the Wizards Tower
 *
 * see games.stendhal.server.maps.quests.ArchmageErastusQuest
 */
public class BlueArchmageNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildErastus(zone);
	}

	private void buildErastus(final StendhalRPZone zone) {
		final SpeakerNPC erastus = new SpeakerNPC("伊拉斯塔斯") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(21, 37));
				nodes.add(new Node(13, 37));
				nodes.add(new Node(13, 32));
				nodes.add(new Node(22, 32));
				nodes.add(new Node(22, 25));
				nodes.add(new Node(24, 25));
				nodes.add(new Node(22, 25));
				nodes.add(new Node(22, 32));
				nodes.add(new Node(33, 32));
				nodes.add(new Node(32, 32));
				nodes.add(new Node(32, 33));
				nodes.add(new Node(32, 32));
				nodes.add(new Node(22, 32));
				nodes.add(new Node(22, 25));
				nodes.add(new Node(24, 25));
				nodes.add(new Node(20, 25));
				nodes.add(new Node(20, 32));
				nodes.add(new Node(8, 32));
				nodes.add(new Node(11, 32));
				nodes.add(new Node(11, 35));
				nodes.add(new Node(13, 35));
				nodes.add(new Node(13, 37));
				nodes.add(new Node(22, 37));
				nodes.add(new Node(22, 40));
				nodes.add(new Node(26, 40));
				nodes.add(new Node(26, 36));
				nodes.add(new Node(26, 37));
				nodes.add(new Node(28, 37));
				nodes.add(new Node(25, 37));
				nodes.add(new Node(25, 40));
				nodes.add(new Node(22, 40));
				nodes.add(new Node(22, 37));
				nodes.add(new Node(21, 37));
				nodes.add(new Node(21, 36));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("你好, 冒险者！");
				addHelp("所有的魔法类型都有一个 #相克 的魔法以维持平衡. 牢记这条很重要. ");
				addJob("我是 Erastus. 我的职责是联合并领导圈子中的巫师. ");
				addOffer("我什么都不卖. 不过 Zekiel 和 巫师们应该可以帮到你！");
				addReply("相克", "想打败火系当然用水系. 但相克不只是针锋相对. " +
						"我见过 大法师 能够融合它们, 并创建出更强大的魔法. ");
				addQuest("是的我有个请求, 但你需要首先从巫师界里学会点魔法知识. 当你完成我的请求时, 我会把这个给你. ");
				addGoodbye("再见, 冒险者！");

			} //remaining behaviour defined in maps.quests.ArchmageErastusQuest
		};

		erastus.setDescription("你遇见 Erastus, 他是全能级法术大师. ");
		erastus.setEntityClass("blueoldwizardnpc");
		erastus.setPosition(21, 36);
		erastus.initHP(100);
		zone.add(erastus);
	}
}
