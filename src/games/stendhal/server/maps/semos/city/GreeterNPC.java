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

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * An old man (original name: 梦金斯) who stands around and gives directions
 * to newcomers. He's the brother of RetireeNPC (original name: 迪金斯).
 *
 * @see games.stendhal.server.maps.quests.Meet梦金斯
 * @see games.stendhal.server.maps.quests.HatForMonogenes
 */
public class GreeterNPC implements ZoneConfigurator {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("梦金斯") {
			@Override
			public void createDialog() {
				addJob("我是 迪金斯 的大哥, 我也不记得我以前是做什么的了...我现在退休了.");
				addOffer("我在 塞门镇做 #地标 指引员. 帮助新来的移民指路. 当我心情不好时, 也会故意指错路逗自已开心. . . 呵呵呵！当然, 有时我指错路, 他们最终还是找到正确的路, 哈哈哈!");
				// All further behaviour is defined in quest classes.
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.LEFT);
			}



		};
		npc.setPosition(27, 43);
		npc.setEntityClass("oldmannpc");
		npc.setDescription("你见到了 梦金斯. 看起来很苍老. 或许他知道一点情况...");
		npc.setDirection(Direction.LEFT);
		zone.add(npc);
	}

}
