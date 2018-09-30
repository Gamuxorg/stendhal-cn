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
package games.stendhal.server.script;

import java.util.List;

import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.MineTownRevivalWeeks;

/**
 * Starts or stops the 矿镇复兴展会周.
 *
 * information about semos.xml changes at the bottom of the script
 * @author hendrik
 */
public class MineTown extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {
		if (args.size() != 1) {
			admin.sendPrivateText("/script MineTown.class {true|false}");
			return;
		}

		boolean enable = Boolean.parseBoolean(args.get(0));
		if (enable) {
			startSemosMineTowns(admin);
		} else {
			stopSemosMineTowns(admin);
		}
	}

	/**
	 * Starts the 矿镇复兴展会周.
	 *
	 * @param admin adminstrator running the script
	 */
	private void startSemosMineTowns(Player admin) {
		if (StendhalQuestSystem.get().getQuest(MineTownRevivalWeeks.QUEST_NAME) != null) {
			admin.sendPrivateText("矿镇复兴展会周 活动已开始.");
			return;
		}
		StendhalQuestSystem.get().loadQuest(new MineTownRevivalWeeks());
	}

	/**
	 * Ends the 矿镇复兴展会周.
	 *
	 * @param admin adminstrator running the script
	 */
	private void stopSemosMineTowns(Player admin) {
		if (StendhalQuestSystem.get().getQuest(MineTownRevivalWeeks.QUEST_NAME) == null) {
			admin.sendPrivateText("矿镇复兴展会周 还没开始.");
			return;
		}
		StendhalQuestSystem.get().unloadQuest(MineTownRevivalWeeks.QUEST_NAME);
	}

}
// TODO: these should not be done manually but added and removed as part of the script load and unload
// Mine Town Weeks information
// Loading mountain_n2_mine_town_weeks.tmx and 塞门镇 halloween.city.tmx . Both contain several walkblockers for tables, 卡若琳s shop and signs.

// Mine Town map:
// Walkblockers for tables at:
// x="55" y="110">, <entity x="61" y="110">, <entity x="67" y="110">
//<attribute name="description">这是一张优雅且干净的餐桌</attribute>
//<attribute name="width">4</attribute>
//<attribute name="height">2</attribute>

// 卡若琳s shop signs at:
// x="60" y="105", x="55" y="105"
// <parameter name="shop">sellrevivalweeks</parameter>, <parameter name="title">卡若琳s snacks and drinks shop (sells)</parameter>

// Wooden arch at:
// x="94" y="118", x="95" y="119", x="96" y="119"
// <attribute name="text">Welcome to the 矿镇复兴展会周 xxxx!</attribute>

// The 塞门镇_halloween.tmx map
// Banners at:
// x="53" y="3", x="14" y="4", x="16" y="48", x="58" y="49"
// <attribute name="text">#Mine #Town #Revival #Weeks #xxxx! Enjoy the #x #festival and meet #苏茜 and her father while celebrating with snacks and drinks! Just take the path up to the #North #from #塞门 镇 #City to reach the #Mine #Town!</attribute>
// <attribute name="width">2</attribute>
// <attribute name="height">2</attribute>
