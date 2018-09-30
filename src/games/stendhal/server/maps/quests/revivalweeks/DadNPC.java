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
package games.stendhal.server.maps.quests.revivalweeks;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.ados.rosshouse.FatherNPC;

/**
 * 苏茜's father during the 矿镇复兴展会周
 */
public class DadNPC implements LoadableContent {
	private void createDadNPC() {
		final StendhalRPZone zone2 = SingletonRepository.getRPWorld().getZone("int_semos_frank_house");
		final SpeakerNPC npc2 = new SpeakerNPC("Mr Ross") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("嗨, 来这儿.");
				addJob("我在矿镇复兴展会周渡假. 但我还要在我女儿 #苏茜 的聚会开办之前完成一些工作.");
				addHelp("我女儿 苏茜 对矿镇复兴展会周很是期待. 但我真的担心女儿再出现坏状况. 所以在我完成工作之前她还要等一会.");
				addReply("苏茜", "我的女儿苏茜对矿镇复兴展会周很是期待. 但我但我真的担心女儿再出现坏状况. 所以在我完成工作之前她还要等一会.");
				addOffer("抱歉我没有要卖的东西. 等矿镇复兴展会结束后, 我要去阿多斯的 #苏茜 家.");
				addQuest("去看看我的女儿 #苏茜, 她喜欢交朋友.");
				addGoodbye("再见, 很高兴见到你.");
			}
		};

		npc2.setOutfit(new Outfit(0, 27, 7, 34, 1));
		npc2.setPosition(21, 10);
		npc2.setDirection(Direction.LEFT);
		npc2.initHP(100);
		zone2.add(npc2);
	}


	/**
	 * removes an NPC from the world and NPC list
	 *
	 * @param name name of NPC
	 */
	private void removeNPC(String name) {
		SpeakerNPC npc = NPCList.get().get(name);
		if (npc == null) {
			return;
		}
		npc.getZone().remove(npc);
	}

	@Override
	public void addToWorld() {
		removeNPC("Mr Ross");
		createDadNPC();
	}


	/**
	 * removes 苏茜's father from the Mine Town and places him back into his home in Ados.
	 *
	 * @return <code>true</code>, if the content was removed, <code>false</code> otherwise
	 */
	@Override
	public boolean removeFromWorld() {
		removeNPC("Mr Ross");

		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_阿多斯_罗斯_小屋");
		new FatherNPC().createDadNPC(zone);

		return true;
	}
}
