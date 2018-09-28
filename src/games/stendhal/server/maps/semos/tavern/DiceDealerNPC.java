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
package games.stendhal.server.maps.semos.tavern;

import java.awt.Rectangle;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.CroupierNPC;
import games.stendhal.server.util.Area;

/*
 * Inside Semos Tavern - Level 0 (ground floor)
 */
public class DiceDealerNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		build里卡多(zone);
	}

	final CroupierNPC ricardo = new CroupierNPC("里卡多") {
		@Override
		protected void createPath() {
			// 里卡多 doesn't move
			setPath(null);
		}

		@Override
		protected void createDialog() {
			addGreeting("欢迎来到牌桌 #gambling ,这里是梦想成真的地方.");
			addJob("本店是 塞门镇唯一官方授权的赌场.");
			addReply(
			        "gambling",
			        "规则很简单：如果你要加入只要对我说 #play, 然后下注, 把dice扔到桌上, 正面数值总值越大, 你的赢得的奖金越多. 奖金计算请看后面的黑板!");
			addHelp("如果你在找 奥斯特: 他在楼上.");
			addGoodbye();
		}

		@Override
		protected void onGoodbye(RPEntity player) {
			setDirection(Direction.DOWN);
		}
	};

	private void build里卡多(final StendhalRPZone zone) {
		ricardo.setEntityClass("naughtyteen2npc");
		ricardo.setPosition(26, 2);
		ricardo.setDirection(Direction.DOWN);
		ricardo.setDescription("里卡多 的口袋总是叮叮铛铛的响, 作为酒保来说他有点太年轻. ");
		ricardo.initHP(100);
		final Rectangle tableArea = new Rectangle(25, 4, 2, 3);

		zone.add(ricardo);
		ricardo.setTableArea(tableArea);
	}

	/**
	 * Access the playing area for JUnit tests.
	 * @return playing area
	 */
	public Area getPlayingArea() {
		return ricardo.getPlayingArea();
	}
}
