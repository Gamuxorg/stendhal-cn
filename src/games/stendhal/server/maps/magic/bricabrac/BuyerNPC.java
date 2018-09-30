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
package games.stendhal.server.maps.magic.bricabrac;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

/**
 * Builds an witch NPC She is a trader for bric-a-brac items.
 *
 * @author kymara
 */
public class BuyerNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("文达") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 12));
				nodes.add(new Node(12, 12));
				nodes.add(new Node(12, 8));
				nodes.add(new Node(27, 8));
				nodes.add(new Node(27, 5));
				nodes.add(new Node(27, 10));
				nodes.add(new Node(8, 10));
				nodes.add(new Node(8, 12));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				//addGreeting("你好.//Hello.");
				addGreeting("你好。");
				//addJob("I potter around collecting odds and bobs. Sometimes I sell items, but mostly I like to keep them. If you have any relics to #trade, I would be very happy indeed.");
				addJob("我闲逛着收集一些杂物. 有时候我会卖掉它们,但是通常我会收藏它们。如果你有任何文物来 #交易，我真的会很高兴");
				//addHelp("I could tell you about some of these wonderful items here. The white #pot, #coffins, #dress, #shield, #armor, #tools, #rug, #flowers, #clock and #'sewing machine' are all fascinating!");
				addHelp("我可以告诉你一些奇妙的东西。白色的 #壶, #棺材, #衣服, #盾牌, #盔甲, #工具, #地毯, #鲜花, #座钟 和 #缝纫机 都是迷人的!");
				/*addReply(
						"pot",
						"You mean the white and blue one, the oriental pot, I suppose. That is an original made by the ancient oni people. It's very rare.");
				*/
				addReply(
						"壶",
						"你是说白色的和蓝色的,我想是东方壶吧. 那是古代东方人的原作。它非常稀有。");
				/*addReply(
						"coffins",
						"Those coffins were looted from some underground catacombs, I had to pay a pretty price for that pair.");
				*/
				addReply(
						"棺材",
						"那些棺材是从地下墓穴掠夺来的, 我花费了很大代价修复它们。");
				/*addReply(
						"dress",
						"I do love that beautiful pink dress. I am told it was worn by the elven princess Tywysoga.");
				*/
				addReply(
						"衣服",
						"我很喜欢那件粉色的衣服，我听说它是精灵公主穿的。");
				/*addReply(
						"shield",
						"That is a truly fearsome shield, is it not? There is some enscription on the back about devil knights, but I am afraid I do not understand it.");
				*/
				addReply(
						"盾牌",
						"那真是一个可怕的盾牌, 不是吗? 背后有一些关于魔鬼骑士的铭文, 但是我恐怕不能理解它。");
				/*addReply(
						"rug",
						"That is a genuine rug from the far East. I have never seen one like it, only cheap copies. Please don't get muddy footprints on it!");			
				*/
				
				addReply(
						"地毯",
						"那一是一份真正的远东地毯。我从未见过这样的,除了便宜的西贝货。请不要在上面留下泥脚印！");				

				/*addReply(
						"flowers",
						"Ah ha! These are flowers grown with elf magic. I bought them myself from a wonderful florist in Nalwor.");
*/				
				addReply(
						"鲜花",
						"啊哈！这些是随着精灵魔力成长的鲜花。我自己从纳尔沃那里一个很棒的花农那买的。");
				/*addReply(
						"clock",
						"That grandfather clock is one of my more modern pieces. If you know Woody the Woodcutter, you may recognise the handiwork.");
*/				
				addReply(
						"座钟",
						"那座大座钟是我更现代的收藏品之一。 如果你认识樵夫 乌迪, 你可能会认出这件手工作品。");
				

	  			addReply(
						"工具",
						"那些后墙上的工具真是古老！他们被塞门镇的艾克德罗斯的曾祖父使用过，真不可思议！");
			    
				addReply(
						"盔甲",
						"啊哈，那个伟大的作品是 Deniran 制作的。关于它我恐怕只知道一点点");

				/*addReply(
						"sewing machine",
						"Oh you know that is my favourite. It was made by a man called Zinger, and it still works just as well as the day it was made.");
				*/
				addReply(
						"缝纫机",
						"哦你知道那是我的最爱。 它是一个叫做 Zinger 的男人制作的 , 它仍旧工作得像刚制作的那样好。");
				/*addQuest("I have no favour to ask of you.");
				*/
				addQuest("我不想问你了。");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buymagic")), false);
				//addOffer("There is a list of prices for relics and magic items I would buy, over on that large book.");
				addOffer("有一份我将购买的文物和魔法物品的价格清单,在那本大书上");
				addGoodbye("再见.");
			}
		};


		npc.setDescription("你遇见 文达 ，一个看起来乱糟糟的女巫。");
		npc.setEntityClass("witch2npc");
		npc.setPosition(4, 12);
		npc.initHP(100);
		zone.add(npc);
	}
}
