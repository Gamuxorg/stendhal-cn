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
package games.stendhal.server.maps.quests.houses;

import java.util.LinkedList;

import games.stendhal.common.MathHelper;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.portal.HousePortal;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

/**
 * Controls house buying.
 *
 * @author kymara
 */
public class HouseBuyingMain {
	private HouseTax houseTax = null;

	/** Kalavan house seller Zone name. */
	private static final String KALAVAN_CITY = "0_kalavan_city";
	/** Athor house seller Zone name. */
	private static final String ATHOR_ISLAND = "0_athor_island";
	/** Ados house seller Zone name. */
	private static final String ADOS_TOWNHALL = "int_阿多斯_市_政厅_3";
	/** Kirdneh house seller Zone name. */
	private static final String KIRDNEH_TOWNHALL = "int_kirdneh_townhall";

	/**
	 * The NPC for Kalavan Houses.
	 *
	 * @param zone target zone
	 */
	public void createKalavanNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new KalavanHouseseller("巴瑞特霍木斯", "kalavan", houseTax);
		zone.add(npc);
	}

	/**
	 * The NPC for Ados Houses.
	 *
	 * @param zone target zone
	 */
	public void createAdosNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new AdosHouseSeller("瑞格丹森", "ados", houseTax);
		zone.add(npc);
	}

	/**
	 * The NPC for Kirdneh Houses.
	 *
	 * @param zone target zone
	 */
	public void createKirdnehNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new KirdnehHouseSeller("罗格费雷普顿", "kirdneh", houseTax);
		zone.add(npc);
	}

	/**
	 * The NPC for 阿托尔公寓s.
	 *
	 * @param zone target zone
	 */
	public void createAthorNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new AthorHouseSeller("Cyk", "athor", houseTax);
		zone.add(npc);
	}

	public LinkedList<String> getHistory(final Player player) {
		LinkedList<String> hist = new LinkedList<String>();
		if(!player.hasQuest("house")) {
			hist.add("我还没买过房子. ");
			return(hist);
		}
		hist.add("我买 " +  HouseUtilities.getHousePortal(MathHelper.parseInt(player.getQuest("house"))).getDoorId() + ".");
		HousePortal playerHousePortal = HouseUtilities.getPlayersHouse(player);
		if(playerHousePortal!=null) {
			int unpaidPeriods = houseTax.getUnpaidTaxPeriods(player);
			if (unpaidPeriods>0) {
				hist.add("我拥有 " + unpaidPeriods + " 月的税款. ");
			} else {
				hist.add("我交的房产税到期日期为.");
			}
		} else {
			hist.add("我不再拥有那个房屋了. ");
		}
		return(hist);
	}

	public void addToWorld() {
		// Start collecting taxes as well
		houseTax = new HouseTax();

		StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(KALAVAN_CITY);
		createKalavanNPC(zone);

		zone = SingletonRepository.getRPWorld().getZone(ADOS_TOWNHALL);
		createAdosNPC(zone);

		zone = SingletonRepository.getRPWorld().getZone(KIRDNEH_TOWNHALL);
		createKirdnehNPC(zone);

		zone = SingletonRepository.getRPWorld().getZone(ATHOR_ISLAND);
		createAthorNPC(zone);

	}

	public boolean isCompleted(final Player player) {
		return HouseUtilities.getPlayersHouse(player)!=null;
	}
}
