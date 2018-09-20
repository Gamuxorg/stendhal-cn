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
package games.stendhal.server.maps.kalavan.castle;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.spawner.PassiveEntityRespawnPoint;

/**
 * Creates the items on the table in the castle basement.
 *
 * @author kymara
 */
public class ItemsOnTable implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildBasementArea(zone);
	}

	private void buildBasementArea(final StendhalRPZone zone) {

		// Plant grower for poison
		final PassiveEntityRespawnPoint plantGrower = new PassiveEntityRespawnPoint("痍毒", 2000);
		plantGrower.setPosition(109, 103);
		plantGrower.setDescription("Scientists often put bottles down here.");
		zone.add(plantGrower);

		plantGrower.setToFullGrowth();

		// Plant grower for 抗毒药济
		final PassiveEntityRespawnPoint plantGrower2 = new PassiveEntityRespawnPoint("大瓶抗毒药济", 4500);
		plantGrower2.setPosition(83, 111);
		plantGrower2.setDescription("Scientists often put bottles down here.");
		zone.add(plantGrower2);

		plantGrower2.setToFullGrowth();

		// Plant grower for mega poison
		final PassiveEntityRespawnPoint plantGrower3 = new PassiveEntityRespawnPoint("剧毒", 4000);
		plantGrower3.setPosition(100, 116);
		plantGrower3.setDescription("Scientists often put bottles down here.");
		zone.add(plantGrower3);

		plantGrower3.setToFullGrowth();

		// Plant grower for a shield (3 hours)
		final PassiveEntityRespawnPoint plantGrower4 = new PassiveEntityRespawnPoint("王冠之盾", 36000);
		plantGrower4.setPosition(40, 22);
		plantGrower4.setDescription("Imperial soliders leave their things here.");
		zone.add(plantGrower4);

		plantGrower4.setToFullGrowth();

		// Plant grower for a 双刃剑 (24 hours)
		final PassiveEntityRespawnPoint plantGrower5 = new PassiveEntityRespawnPoint("双刃剑", 288000);
		plantGrower5.setPosition(27, 21);
		plantGrower5.setDescription("Imperial soliders leave their things here.");
		zone.add(plantGrower5);

		plantGrower5.setToFullGrowth();

		// grower for an empty 耳瓶 (30min)
		final PassiveEntityRespawnPoint bottleGrower1 = new PassiveEntityRespawnPoint("耳瓶", 6000);
		bottleGrower1.setPosition(91, 90);
		bottleGrower1.setDescription("Scientists often put bottles down here.");
		zone.add(bottleGrower1);

		bottleGrower1.setToFullGrowth();

		// grower for an empty 细瓶子 (30min)
		final PassiveEntityRespawnPoint bottleGrower2 = new PassiveEntityRespawnPoint("细瓶子", 6000);
		bottleGrower2.setPosition(102, 89);
		bottleGrower2.setDescription("Scientists often put bottles down here.");
		zone.add(bottleGrower2);

		bottleGrower2.setToFullGrowth();

		// grower for an empty 大瓶子
		final PassiveEntityRespawnPoint bottleGrower3 = new PassiveEntityRespawnPoint("大瓶子", 3000);
		bottleGrower3.setPosition(104, 105);
		bottleGrower3.setDescription("Scientists often put bottles down here.");
		zone.add(bottleGrower3);

		bottleGrower3.setToFullGrowth();


	}

}
