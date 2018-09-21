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
package games.stendhal.server.maps.semos.kanmararn;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class CowardSoldierNPC implements ZoneConfigurator {


   /**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildHideoutArea(zone);
	}

	private void buildHideoutArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Henry") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(57, 113));
				nodes.add(new Node(59, 113));
				nodes.add(new Node(59, 115));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Ssshh! 小声点, 否则你会引起更多矮人 #dwarves 的注意.");
				addJob("我是军队的一名士兵.");
				addGoodbye("要小心周围的矮人, 再见!");
				addHelp("我需要自救, 我与队友分散了 #group , 现在只剩我一个人.");
				addReply(Arrays.asList("dwarf", "dwarves"),
					"到处都有矮人！他们的王国 #kingdom 一定在附近.");
				addReply(Arrays.asList("kingdom", "卡梅伦"),
					"卡梅伦, 传说是矮人们 #dwarves 居住的城市.");
				addReply("group",
					"一般会派一组5个人到这个地区探寻财宝 #treasure.");
				addReply("treasure",
					"传闻在这个地牢下某处 #somewhere , 有一个大宝藏. .");
				addReply("somewhere", "如果你能帮 #help 我, 我可以给你一点线索.");
			}
			// remaining behaviour is defined in maps.quests.KanmararnSoldiers.
		};

		npc.setEntityClass("youngsoldiernpc");
		npc.setDescription("你遇见了 Henry. 他是 Smemos 镇的一个失散的士兵, 现在藏在黑暗的洞窟中...");
		npc.setPosition(57, 113);
		npc.setBaseHP(100);
		npc.initHP(20);
		npc.setLevel(5);
		zone.add(npc);
	}
}
