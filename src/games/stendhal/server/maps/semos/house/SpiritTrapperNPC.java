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
package games.stendhal.server.maps.semos.house;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.MultiProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.MultiProducerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.TeleporterBehaviour;
import games.stendhal.server.entity.player.Player;



/**
 * Builds a shady Spirit Trapper NPC for the Empty Bottle quest.
 *
 * @author soniccuz based on FlowerSellerNPC by kymara and fishermanNPC by dine.
 */
public class SpiritTrapperNPC implements ZoneConfigurator {



	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {

			final List<String> setZones = new ArrayList<String>();
			setZones.add("0_阿多斯_沼泽");
			setZones.add("0_阿多斯_郊外_w");
			setZones.add("0_阿多斯_城墙_n2");
			setZones.add("0_阿多斯_城墙_s");
			setZones.add("0_阿多斯_城_s");
        	new TeleporterBehaviour(buildSemosHouseArea(), setZones, "0_ados", "..., ....");
	}

	private SpeakerNPC buildSemosHouseArea() {

	    final SpeakerNPC mizuno = new SpeakerNPC("迷之诺") {
	                @Override
			protected void createPath() {
				// npc does not move
				setPath(null);
			}
	        @Override
			protected void createDialog() {
	        	addGreeting("你想要做什么?");
			    addJob("我宁愿自已留着.");
			    addHelp("快点看, 我必须马上走, 如果你手上有 #黑珍珠 , 我会用魔法 #箭 和你交换. 介意 #买 些 #箭 吗?");
			    addOffer("快点看, 我必须马上走, 如果你手上有 #黑珍珠 , 我可以魔法 #箭 和你交换, 介意 #买 些 #箭 吗?");
			    addGoodbye("快走... 你这垃圾在这乱转我都不能干活了.");

			    addReply("箭","我把魔法能量注入到箭里, 魔法能量有 #冰 , #火 和 #电 .");
			    addReply(Arrays.asList("冰", "冰箭", "火", "火箭"),
	                    "一颗黑珍珠, 可以换一只箭.");
			    addReply(Arrays.asList("电", "电箭w"),
	                    "电箭最快, 每两颗黑珍珠只能换一只箭.");
			    addReply(Arrays.asList("黑珍珠", "珍珠"),
	                    "对我来说, 他们制作了不错的法宝. 我常可以在那些垃圾刺客中找到他们.");
			    // the rest is in the MessageInABottle quest




			 // Mizuno exchanges elemental arrows for 黑珍珠.
				// (uses sorted TreeMap instead of HashMap)
			    final HashSet<String> productsNames = new HashSet<String>();
                productsNames.add("冰箭");
                productsNames.add("火箭");
                productsNames.add("电箭");

                final Map<String, Integer> reqRes_iceArrow = new TreeMap<String, Integer>();
                reqRes_iceArrow.put("黑珍珠", 1);

                final Map<String, Integer> reqRes_fireArrow = new TreeMap<String, Integer>();
                reqRes_fireArrow.put("黑珍珠", 1);

                final Map<String, Integer> reqRes_lightArrow = new TreeMap<String, Integer>();
                reqRes_lightArrow.put("黑珍珠", 2);


                final HashMap<String, Map<String, Integer>> requiredResourcesPerProduct = new HashMap<String, Map<String, Integer>>();
                requiredResourcesPerProduct.put("冰箭", reqRes_iceArrow);
                requiredResourcesPerProduct.put("火箭", reqRes_fireArrow);
                requiredResourcesPerProduct.put("电箭", reqRes_lightArrow);

                final HashMap<String, Integer> productionTimesPerProduct = new HashMap<String, Integer>();
                productionTimesPerProduct.put("冰箭", 0 * 60);
                productionTimesPerProduct.put("火箭", 0 * 60);
                productionTimesPerProduct.put("电箭", 0 * 60);

                final HashMap<String, Boolean> productsBound = new HashMap<String, Boolean>();
                productsBound.put("冰箭", false);
                productsBound.put("火箭", false);
                productsBound.put("电箭", false);

                class SpecialTraderBehaviour extends MultiProducerBehaviour {

					public SpecialTraderBehaviour(String questSlot, String productionActivity,
							HashSet<String> productsNames,
							HashMap<String, Map<String, Integer>> requiredResourcesPerProduct,
							HashMap<String, Integer> productionTimesPerProduct,
							HashMap<String, Boolean> productsBound) {
						super(questSlot, productionActivity, productsNames, requiredResourcesPerProduct, productionTimesPerProduct,
								productsBound);
						// TODO Auto-generated constructor stub
					}

					@Override
					public boolean askForResources(final ItemParserResult res, final EventRaiser npc, final Player player) {
						int amount = res.getAmount();
				        String productName = res.getChosenItemName();

				        if (getMaximalAmount(productName, player) < amount) {
				            npc.say(" 如果你带给我 "
				                    + getRequiredResourceNamesWithHashes(productName, amount)
									+ " ,我只能 " + getProductionActivity() + " "
				                    + productName);
				                    
				            return false;
				        } else {
							res.setAmount(amount);
							npc.say( productName
									+ " for "
									+ getRequiredResourceNamesWithHashes(productName, amount) + ". "
									+ " 对吧?");


				            return true;
				        }
				    }


					@Override
					public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser npc, final Player player) {
				    	int amount = res.getAmount();
				        String productName = res.getChosenItemName();


				        if (getMaximalAmount(productName, player) < amount) {
				            // The player tried to cheat us by placing the resource
				            // onto the ground after saying "yes"
				            npc.say("喂! 在这儿! 你最好不要骗我...");
				            return false;
				        } else {
				            for (final Map.Entry<String, Integer> entry : getRequiredResourcesPerProduct(productName).entrySet()) {
				                final int amountToDrop = amount * entry.getValue();
				                player.drop(entry.getKey(), amountToDrop);
				            }
				            final long timeNow = new Date().getTime();
				            player.setQuest(getQuestSlot(), amount + ";" + productName + ";" + timeNow);

				            if (getProductionTime(productName, amount) == 0) {

				            	//If production time is 0 just give player the product
				            	final int numberOfProductItems = amount;
				            	final StackableItem products = (StackableItem) SingletonRepository.getEntityManager().getItem(productName);
				    			products.setQuantity(numberOfProductItems);

				    			if (isProductBound(productName)) {
				    				products.setBoundTo(player.getName());
				    			}

				    			if (player.equipToInventoryOnly(products)) {
				    				npc.say("这是你的 " 
									+ productName + ".");

				    				player.setQuest(getQuestSlot(), "done");
				    				player.notifyWorldAboutChanges();
				    				player.incProducedCountForItem(productName, products.getQuantity());
				    				SingletonRepository.getAchievementNotifier().onProduction(player);
				    			} else {
				    				npc.say("欢迎回来！你的事我完成了. 但现在你还不能拿走 "
				    						+  productName
				    						+ ". 等你背包有空间时再回来拿.");
				    			}

				            	return true;
				            } else {
				            		npc.say("OK, 我会为你"
				                    + getProductionActivity()
				                    + " "
				                    +  productName
				                    + " , 但要花些时间, 请在 "
				                    + getApproximateRemainingTime(player) + " 时间后回来.");
				            		return true;
				            	}
				        }
				    }


                }

                final MultiProducerBehaviour behaviour = new SpecialTraderBehaviour(
                        "arrow_trader",
                        "buy",
                        productsNames,
                        requiredResourcesPerProduct,
                        productionTimesPerProduct,
                        productsBound);

                    new MultiProducerAdder().addMultiProducer(this, behaviour,
                        "你想做什么?");
			}
		};

		mizuno.setEntityClass("man_001_npc");
		mizuno.initHP(100);
		mizuno.setHP(80);
		mizuno.setCollisionAction(CollisionAction.REVERSE);
		mizuno.setDescription("你遇见 迷之诺. 他像一个灵魂游荡在 Ados 的无人地带, 不知道他做些什么.");

		// start in int_semos_house
		final StendhalRPZone	zone = SingletonRepository.getRPWorld().getZone("int_semos_house");
		mizuno.setPosition(5, 6);
		zone.add(mizuno);

		return mizuno;
	}
}
