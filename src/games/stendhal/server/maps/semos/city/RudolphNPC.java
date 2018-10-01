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
package games.stendhal.server.maps.semos.city;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;
/**
 * ZoneConfigurator configuring Rudolph the Red-Nosed 驯鹿 who clops around 塞门镇 during Christmas season
 */
public class RudolphNPC implements ZoneConfigurator {


	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Rudolph") {

			@Override
			protected void createPath() {
				final List<Node> path = new LinkedList<Node>();
				path.add(new Node(2, 3));
				path.add(new Node(2, 14));
				path.add(new Node(36, 14));
				path.add(new Node(36, 46));
				path.add(new Node(51, 46));
				path.add(new Node(51, 48));
				path.add(new Node(62, 48));
				path.add(new Node(62, 55));
				path.add(new Node(51, 55));
				path.add(new Node(51, 58));
				path.add(new Node(32, 58));
				path.add(new Node(32, 53));
				path.add(new Node(18, 53));
				path.add(new Node(18, 43));
				path.add(new Node(20, 43));
				path.add(new Node(20, 26));
				path.add(new Node(26, 26));
				path.add(new Node(26, 14));
				path.add(new Node(21, 14));
				path.add(new Node(21, 3));
				setPath(new FixedPath(path, true));
			}

			@Override
			public void createDialog() {
				addGreeting("Hi, 你好朋友. 这是一年中最美妙的时光啊！");
				addHelp("Oh, 我的, 我不能帮助你, 抱歉, 我变成 Santa 圣诞老人吧.");
				addJob("我把 Santa 的雪撬在圣诞夜装上, 点亮的我鼻子, 让我快乐, 还能让圣诞老人能看到他的路.");
				addGoodbye("很高兴见到你.");

				// remaining behaviour defined in games.stendhal.server.maps.quests.GoodiesForRudolph

			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}

		};
		npc.setPosition(2, 3);
		npc.setDirection(Direction.DOWN);
		npc.setDescription("你见到红鼻子驯鹿 驯鹿. 他的鼻子很大, 还闪着光.");
		npc.setBaseSpeed(1);
		npc.setEntityClass("rudolphnpc");
		npc.setCollisionAction(CollisionAction.REVERSE);
		zone.add(npc);
	}

}
