/***************************************************************************
 *                   (C) Copyright 2011-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.ados.outside;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds a NPC outside Magician house in Ados  (name:温妮尔) who is the pupil of Magician 海震
 *
 * @author geomac
 */
public class MagicianHouseGreeterNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		createMagicianHouseGreeterNPC(zone);
	}

	private void createMagicianHouseGreeterNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("温妮尔") {

			@Override
			protected void createDialog() {
				addGreeting("喂, 我对魔法 #迷宫 很感兴趣!");
				addHelp("如果你走累了, 可以退出游戏然后再进入就能回到 #海震 房间.");
				addReply("迷宫", "我估计你会迷路, 但有 #卷轴 可帮你.");
				addReply("卷轴", "进入 #迷宫 十分钟内可以捡起地上的卷轴.");
				addQuest("如果我让别人进入并完成 #迷宫. 海震就会让我成为他的 #助手.");
				addReply("助手", "希望某天, 我也学会运用魔法.");
				addReply("海震", "他教我使用魔法.");
				addOffer("我可以给你一些 #建议.");
				addReply("建议", "常看小地图会很有帮助.");
				addJob("希望我可以马上成为海震的 #助手 .");
				addGoodbye("谢谢你, 祝你开心.");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setDescription("你遇见了 温妮尔. 她想学习魔法.");
		npc.setEntityClass("magicianhousegreeternpc");
		npc.setPosition(70, 52);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		zone.add(npc);
	}
}