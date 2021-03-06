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
package games.stendhal.server.maps.ados.magician_house;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.spawner.PassiveEntityRespawnPoint;

/**
 * Creates the items on the table in the magician house.
 *
 * @author hendrik
 */
public class ItemsOnTable implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildMagicianHouseArea(zone);
	}

	private void buildMagicianHouseArea(final StendhalRPZone zone) {
		final Item item = addPersistentItem("召唤卷轴", zone, 7, 6);
		item.setInfoString("giant_red_dragon");

		// Plant grower for poison
		final PassiveEntityRespawnPoint plantGrower = new PassiveEntityRespawnPoint("毒药", 1500);
		plantGrower.setPosition(3, 6);
		plantGrower.setDescription("海震把魔法试济放在这里.");
		zone.add(plantGrower);

		plantGrower.setToFullGrowth();

		// grower for an empty 细瓶子 (see Koboldish Torcibud quest, respawning time 1h)
		final PassiveEntityRespawnPoint bottleGrower1 = new PassiveEntityRespawnPoint("细瓶子", 12000);
		bottleGrower1.setPosition(10, 5);
		bottleGrower1.setDescription("这里像是存放瓶子的地方.");
		zone.add(bottleGrower1);

		bottleGrower1.setToFullGrowth();

	}

	private Item addPersistentItem(final String name, final StendhalRPZone zone, final int x, final int y) {
		final Item item = SingletonRepository.getEntityManager().getItem(name);
		item.setPosition(x, y);
		zone.add(item, false);

		return item;
	}
}
