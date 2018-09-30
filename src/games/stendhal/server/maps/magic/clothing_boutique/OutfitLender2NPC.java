/***************************************************************************
 *                   (C) Copyright 2003-2015 - Stendhal                    *
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

public class OutfitLender2NPC implements ZoneConfigurator {

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
		  final Pair<Outfit, Boolean> GLASSES = new Pair<Outfit, Boolean>(new Outfit(null, null, Integer.valueOf(86), null, null), true);
		  final Pair<Outfit, Boolean> GOBLIN_FACE = new Pair<Outfit, Boolean>(new Outfit(null, null, Integer.valueOf(88), null, null), true);
		  final Pair<Outfit, Boolean> THING_FACE = new Pair<Outfit, Boolean>(new Outfit(null, null, Integer.valueOf(87), null, null), true);
		  final Pair<Outfit, Boolean> Umbrella = new Pair<Outfit, Boolean>(new Outfit(Integer.valueOf(07), null, null, null, null), true);

		// these outfits must replace the current outfit (what's null simply isn't there)
		  final Pair<Outfit, Boolean> PURPLE_SLIME = new Pair<Outfit, Boolean>(new Outfit(null, Integer.valueOf(00), Integer.valueOf(98), Integer.valueOf(00), Integer.valueOf(93)), false);
		  final Pair<Outfit, Boolean> GREEN_SLIME = new Pair<Outfit, Boolean>(new Outfit(null, Integer.valueOf(00), Integer.valueOf(98), Integer.valueOf(00), Integer.valueOf(89)), false);
		  final Pair<Outfit, Boolean> RED_SLIME = new Pair<Outfit, Boolean>(new Outfit(null, Integer.valueOf(00), Integer.valueOf(98), Integer.valueOf(00), Integer.valueOf(88)), false);
		  final Pair<Outfit, Boolean> BLUE_SLIME = new Pair<Outfit, Boolean>(new Outfit(null, Integer.valueOf(00), Integer.valueOf(98), Integer.valueOf(00), Integer.valueOf(91)), false);
		  final Pair<Outfit, Boolean> GINGERBREAD_MAN = new Pair<Outfit, Boolean>(new Outfit(null, Integer.valueOf(00), Integer.valueOf(98), Integer.valueOf(00), Integer.valueOf(92)), false);


			outfitTypes.put("玻璃", GLASSES);
			outfitTypes.put("小妖精面具", GOBLIN_FACE);
			outfitTypes.put("面具", THING_FACE);
			outfitTypes.put("雨伞", Umbrella);
			outfitTypes.put("紫色史莱姆", PURPLE_SLIME);
			outfitTypes.put("绿色史莱姆", GREEN_SLIME);
			outfitTypes.put("红色史莱姆", RED_SLIME);
			outfitTypes.put("蓝色史莱姆", BLUE_SLIME);
			outfitTypes.put("姜饼人", GINGERBREAD_MAN);
	}



	private void buildBoutiqueArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("莎士起亚") {
			@Override
			protected void createPath() {
			    final List<Node> nodes = new LinkedList<Node>();
			    nodes.add(new Node(5, 7));
			    nodes.add(new Node(5, 20));
			    nodes.add(new Node(9, 20));
			    nodes.add(new Node(9, 7));
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
								//seller.say("You already have a magic outfit on which just wouldn't look good with another - could you please put yourself in something more conventional and ask again? Thanks!");
								seller.say("你已经穿上了魔法外衣，但看起来如另一件好 - 请你传统的衣服然后再来？ 谢谢！");
								return false;
							}
						}

						int charge = getCharge(res, player);

						if (player.isEquipped("money", charge)) {
							player.drop("money", charge);
							putOnOutfit(player, outfitType);
							return true;
						} else {
							seller.say("对不起，你没有足够的钱。");
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
				priceList.put("玻璃", (int) (N * 400));
				priceList.put("小妖精面具", (int) (N * 500));
				priceList.put("面具", (int) (N * 500));
				priceList.put("紫色史莱姆", (int) (N * 3000));
				priceList.put("红色史莱姆", (int) (N * 3000));
				priceList.put("蓝色史莱姆", (int) (N * 3000));
				priceList.put("绿色史莱姆", (int) (N * 3000));
				priceList.put("姜饼人", (int) (N * 1200));
				priceList.put("雨伞", (int) (N * 300));
			    //addGreeting("Hello, I hope you are enjoying looking around our gorgeous boutique.");
				addGreeting("您好！ 我希望你你喜欢光临我们的魔法装备店！");
				addQuest("看起来简直棒极了！");
				add(
					ConversationStates.ATTENDING,
					ConversationPhrases.OFFER_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"请告诉我你喜欢哪一个装备, 是要 #租借 #玻璃, #租借 一件 #小妖精面具, #租借 一个 #面具, #租借 一把 #雨伞, #租借 一件 #紫色史莱姆 外衣, #租借 一件 #绿色史莱姆, #租借  #红色史莱姆, #租借 #蓝色史莱姆, #租借  #姜饼人 外套.",
					new ExamineChatAction("outfits2.png", "外形", "价目表"));
				//addJob("I work with magic in a fun way! Ask about the #offer.");
				addJob("我以一种有趣的方式在 魔法城堡 工作。 你要哪种 #商品 。");
				//addHelp("I can cast a spell to dress you in a magical outfit. They wear off after some time. I hope I can #offer you something you like. If not 莉莉娅娜 also rents out from a different range.");
				addHelp("我可以用法术给你穿上魔法装备。他们会在一段时间后自动消失。我希望我可以 #销售 一些你喜欢的东西。 如果不是 莉莉娅娜 也可以从不同的范围出租。");
				addGoodbye("再见！");
				final OutfitChangerBehaviour behaviour = new SpecialOutfitChangerBehaviour(priceList, endurance, "你的魔法装备已经磨损。");
				new OutfitChangerAdder().addOutfitChanger(this, behaviour, "hire", false, false);
			}
		};

		npc.setEntityClass("wizardwomannpc");
		npc.setPosition(5, 7);
		npc.initHP(100);
		npc.setDescription("你看见 莎士起亚. 她在 Magic 城堡的装备店工作！");
		zone.add(npc);
	}
}

