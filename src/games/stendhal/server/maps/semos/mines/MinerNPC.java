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
package games.stendhal.server.maps.semos.mines;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * Builds a NPC in Semos Mine (name:Barbarus) who is a miner and informs players about his job
 *
 * @author storyteller and Vanessa Julius
 *
 */
public class MinerNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Barbarus") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(57, 78));
				nodes.add(new Node(55, 78));
                nodes.add(new Node(55, 80));
                nodes.add(new Node(53, 80));
                nodes.add(new Node(53, 82));
                nodes.add(new Node(55, 82));
                nodes.add(new Node(55, 84));
                nodes.add(new Node(59, 84));
                nodes.add(new Node(59, 78));
                nodes.add(new Node(58, 78));
                nodes.add(new Node(57, 78));
               	setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting("Good luck!");
				addReply("good luck", "好运！希望你安全的离开这个矿山!");
				addReply("glück auf", "Glüüück Auf, Glück Auf...! *sing");
				addHelp("要记得来时的路！还有, 在这些通道里, 进入越深越会迷失方向! 我还记得以前的一些事：在矿山中 #mine 好像一些危险的东西... 我听到洞的深处不时传来奇怪的响声 #sounds ...");
				addReply("mine","这座矿山有一个复杂的通道系统, 因为这个矿已存在很长时间, 没人知道全部的通道, 除了矮人们, 或许吧, 咳!咳! ... *cough*");
				addReply("sounds","那个声音非常怪异... 有时像远处某人的呼喊, ... 就像士兵的号令, 又像是... 偶尔我也听到阴影中传来的脚步声... 真是太吓人了...");
				addOffer("我可以卖给你一些有用的挖矿工具, 是以前和我一起工作的朋友留下的, 所以只要付费, 就可以选一把鹤嘴锄 #picks 带走. 我还可以为你提供一些食品和饮料, 但剩余不多了.... 我还要工作一段时间, 所以还得给自已留一点, 抱歉...但如果你需要, 我能给你一份矿山的地图 #map . ");
				addReply("picks", "你需要一把鹤嘴锄才能在矿山的墙壁上采矿. ");
				final Map<String, Integer> offerings = new HashMap<String, Integer>();
                offerings.put("鹤嘴锄", 400);
                new SellerAdder().addSeller(this, new SellerBehaviour(offerings), false);
				addQuest("抱歉, 但如你所见, 弄了一身灰我也没有完成工作. 我没空想你的问题, 除非你能帮我弄一些煤.");
				addJob("我是个矿工. 在这个矿洞努力工作着, 如果你往下走得越深, 环境就会变越热, 并且灰尘越多, 光线条件也差,你很难看清路...");
				addReply("地图","这是 Semos 矿山的地图, 是我自已以前画的, 它或许能帮你找到路, 但要注意, 上面的路也不是完全精确!",
						new ExamineChatAction("map-semos-mine.png", "塞门镇 Mine", "Rough map of 塞门镇 Mine"));
				addGoodbye("再会, 祝好运!");

			}
		};

		npc.setDescription("You 遇见了 Barbarus. 他流了一身的汗, 混合着一身的灰尘, 脸和胳膊几乎是黑的, 看起来很脏");
		npc.setEntityClass("minernpc");
		npc.setPosition(57, 78);
		npc.setDirection(Direction.RIGHT);
		npc.initHP(100);
		zone.add(npc);
	}
}
