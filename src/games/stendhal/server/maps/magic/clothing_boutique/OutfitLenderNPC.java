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
package games.stendhal.server.maps.magic.clothing_boutique;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.behaviour.adder.OutfitChangerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.OutfitChangerBehaviour;
import games.stendhal.server.entity.player.Player;
import marauroa.common.Pair;

public class OutfitLenderNPC implements ZoneConfigurator {

	// outfits to last for 10 hours normally
	public static final int endurance = 10 * 60;

	// this constant is to vary the price. N=1 normally but could be a lot smaller on special occasions
	private static final double N = 1;

	private static HashMap<String, Pair<Outfit, Boolean>> outfitTypes = new HashMap<String, Pair<Outfit, Boolean>>();
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		initOutfits();
		buildBoutiqueArea(zone);
	}

	private void initOutfits() {
		// these outfits must be put on over existing outfit
		// (what's null doesn't change that part of the outfit)
		// so true means we put on over
		// FIXME: Use new outfit system
		  final Pair<Outfit, Boolean> JUMPSUIT = new Pair<Outfit, Boolean>(new Outfit(null, null, null, Integer.valueOf(83), null), true);
		  final Pair<Outfit, Boolean> DUNGAREES = new Pair<Outfit, Boolean>(new Outfit(null, null, null, Integer.valueOf(84), null), true);
		  final Pair<Outfit, Boolean> GREEN_DRESS = new Pair<Outfit, Boolean>(new Outfit(null, null, null,	Integer.valueOf(78), null), true);

		  final Pair<Outfit, Boolean> GOWN = new Pair<Outfit, Boolean>(new Outfit(null, null, null, Integer.valueOf(82), null), true);
		  final Pair<Outfit, Boolean> NOOB = new Pair<Outfit, Boolean>(new Outfit(null, null, null, Integer.valueOf(80), null), true);
		  final Pair<Outfit, Boolean> GLASSES = new Pair<Outfit, Boolean>(new Outfit(null, null, Integer.valueOf(99), null, null), true);
		  final Pair<Outfit, Boolean> GLASSES_2 = new Pair<Outfit, Boolean>(new Outfit(null, null, Integer.valueOf(79), null, null), true);
		  final Pair<Outfit, Boolean> HAT = new Pair<Outfit, Boolean>(new Outfit(null, Integer.valueOf(99), null, null, null), true);

		// these outfits must replace the current outfit (what's null simply isn't there)
		  final Pair<Outfit, Boolean> BUNNY = new Pair<Outfit, Boolean>(new Outfit(null, Integer.valueOf(00), Integer.valueOf(98), Integer.valueOf(81), Integer.valueOf(98)), false);
		  final Pair<Outfit, Boolean> HORSE = new Pair<Outfit, Boolean>(new Outfit(0, Integer.valueOf(00), Integer.valueOf(98), Integer.valueOf(00), Integer.valueOf(97)), false);
		  final Pair<Outfit, Boolean> GIRL_HORSE = new Pair<Outfit, Boolean>(new Outfit(0, Integer.valueOf(00), Integer.valueOf(98), Integer.valueOf(00), Integer.valueOf(96)), false);
		  final Pair<Outfit, Boolean> ALIEN = new Pair<Outfit, Boolean>(new Outfit(null, Integer.valueOf(00), Integer.valueOf(98), Integer.valueOf(00), Integer.valueOf(95)), false);



			outfitTypes.put("欢快套装", JUMPSUIT);
			outfitTypes.put("粗而衣服", DUNGAREES);
			outfitTypes.put("绿色套装", GREEN_DRESS);
			outfitTypes.put("长外衣", GOWN);
			outfitTypes.put("橙色外衣", NOOB);
			outfitTypes.put("兔子装", BUNNY);
			outfitTypes.put("玻璃装", GLASSES);
			outfitTypes.put("半透玻璃", GLASSES_2);
			outfitTypes.put("帽子", HAT);
			outfitTypes.put("马尾", HORSE);
			outfitTypes.put("女士双马尾", GIRL_HORSE);
			outfitTypes.put("异国风情", ALIEN);
	}



	private void buildBoutiqueArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("莉莉娅娜") {
			@Override
			protected void createPath() {
			    final List<Node> nodes = new LinkedList<Node>();
			    nodes.add(new Node(16, 5));
			    nodes.add(new Node(16, 16));
			    nodes.add(new Node(26, 16));
			    nodes.add(new Node(26, 5));
			    setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				class SpecialOutfitChangerBehaviour extends OutfitChangerBehaviour {
					SpecialOutfitChangerBehaviour(final Map<String, Integer> priceList, final int endurance, final String wearOffMessage) {
						super(priceList, endurance, wearOffMessage);
					}

					@Override
					public void putOnOutfit(final Player player, final String outfitType) {

						final Pair<Outfit, Boolean> outfitPair = outfitTypes.get(outfitType);
						final Outfit outfit = outfitPair.first();
						final boolean type = outfitPair.second();
						if (type) {
							player.setOutfit(outfit.putOver(player.getOutfit()), true);
						} else {
							player.setOutfit(outfit, true);
						}
						player.registerOutfitExpireTime(endurance);
					}
					// override transact agreed deal to only make the player rest to a normal outfit if they want a put on over type.
					@Override
					public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser seller, final Player player) {
						final String outfitType = res.getChosenItemName();
						final Pair<Outfit, Boolean> outfitPair = outfitTypes.get(outfitType);
						final boolean type = outfitPair.second();

						if (type) {
							if (player.getOutfit().getBody() > 80
									&& player.getOutfit().getBody() < 99) {
								seller.say("你已经穿上了魔法外套, 但不如换一种风格 - 请你先换上传统一点的衣服再问我吗? 谢谢!");
								return false;
							}
						}

						int charge = getCharge(res, player);

						if (player.isEquipped("money", charge)) {
							player.drop("money", charge);
							putOnOutfit(player, outfitType);
							return true;
						} else {
							seller.say("抱歉, 你的钱不够!");
							return false;
						}
					}

					// These outfits are not on the usual OutfitChangerBehaviour's
					// list, so they need special care when looking for them
					@Override
					public boolean wearsOutfitFromHere(final Player player) {
						final Outfit currentOutfit = player.getOutfit();

						for (final Pair<Outfit, Boolean> possiblePair : outfitTypes.values()) {
							if (possiblePair.first().isPartOf(currentOutfit)) {
								return true;
							}
						}
						return false;
					}
				}
				final Map<String, Integer> priceList = new HashMap<String, Integer>();
				priceList.put("欢快套装", (int) (N * 500));
				priceList.put("粗而衣服", (int) (N * 500));
				priceList.put("绿色套装", (int) (N * 500));
				priceList.put("长外衣", (int) (N * 750));
				priceList.put("橙色外衣", (int) (N * 500));
				priceList.put("兔子装", (int) (N * 800));
				priceList.put("玻璃装", (int) (N * 400));
				priceList.put("半透玻璃", (int) (N * 400));
				priceList.put("帽子", (int) (N * 400));
				priceList.put("马尾", (int) (N * 1200));
				priceList.put("女士双马尾", (int) (N * 1200));
				priceList.put("异国风情", (int) (N * 1200));
			       	addGreeting("您好！有什么我可以帮助您的吗？");
				//addQuest("I can't think of anything for you, sorry.");
				addQuest("我不能代替您考虑，对不起。");
				add(
					ConversationStates.ATTENDING,
					ConversationPhrases.OFFER_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"如果你想 #租借 套装就告诉我， 可以 #租借 的套装有 #长外衣, #绿色套装, #玻璃装, #半透玻璃, #帽子, #异国风情 , #马尾, #女士双马尾 , #欢快套装,  #粗布衣服,  #兔子装 或者 #橙色外衣 .",
					new ExamineChatAction("outfits.png", "外衣", "价目表"));
				//addJob("I work in this clothes boutique. It's no ordinary shop, we use magic to put our clients into fantastic outfits. Ask about the #offer.");
				addJob("我在魔法装备店工作。 它不是一家普通的店， 我们使用魔法为我们的顾客穿上梦幻般的装备。 询问关于 #买卖");
				// addJob("I normally work in a clothes boutique, we use magic to put our clients into fantastic outfits. I'm here for 矿镇复兴展会周, where we #offer our outfits at greatly reduced prices, but they last for less time!");
				addJob("我通常在魔法装备店工作， 我们使用魔法为我们的顾客穿上梦幻般的装备。 我在这里为了 矿镇复兴展会周, 在那里我们用折扣价 #买卖 我们的装备, 但是它们持续时间更短！");
				//addHelp("Our hired outfits wear off after some time, but you can always come back for more!");
				addHelp("我们卖的装备会在一段时间后磨损, 但你总是可以回来买更多的。");
				addGoodbye("再见！");
				final OutfitChangerBehaviour behaviour = new SpecialOutfitChangerBehaviour(priceList, endurance, "你的魔法外衣已脱下.");
				new OutfitChangerAdder().addOutfitChanger(this, behaviour, "租借", false, false);
			}
		};

		npc.setEntityClass("slim_woman_npc");
		npc.setPosition(16, 5);
		// npc.setPosition(101, 102);
		npc.initHP(100);
		npc.setDescription("你看见 莉莉娅娜. 她在 Magic 主城魔法装备店工作。");
		zone.add(npc);
	}
}

