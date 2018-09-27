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
package games.stendhal.server.maps.kalavan.citygardens;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.common.MathHelper;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

/**
 * Builds the gardener in Kalavan city gardens.
 *
 * @author kymara
 */
public class GardenerNPC implements ZoneConfigurator {

	private static final String QUEST_SLOT = "sue_swap_kalavan_city_scroll";
    private static final Integer MAX_LUNCHES = 7;

	//
	// ZoneConfigurator
	//

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
		final SpeakerNPC npc = new SpeakerNPC("Sue") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(100, 123));
				nodes.add(new Node(110, 123));
				nodes.add(new Node(110, 110));
				nodes.add(new Node(119, 110));
				nodes.add(new Node(119, 122));
				nodes.add(new Node(127, 122));
				nodes.add(new Node(127, 111));
				nodes.add(new Node(118, 111));
				nodes.add(new Node(118, 123));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				class SpecialProducerBehaviour extends ProducerBehaviour {
					SpecialProducerBehaviour(final String productionActivity,
                        final String productName, final Map<String, Integer> requiredResourcesPerItem,
											 final int productionTimePerItem) {
						super(QUEST_SLOT, productionActivity, productName,
							  requiredResourcesPerItem, productionTimePerItem, false);
					}

					@Override
						public boolean askForResources(ItemParserResult res, final EventRaiser npc, final Player player) {
						int amount = res.getAmount();

						if (player.hasQuest(QUEST_SLOT) && player.getQuest(QUEST_SLOT).startsWith("done;")) {
							// she is eating. number of lunches is in tokens[1]
							final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
							// delay is number of lunches * one day - eats one lunch per day
							final long delay = (Long.parseLong(tokens[1])) * MathHelper.MILLISECONDS_IN_ONE_DAY;
							final long timeRemaining = (Long.parseLong(tokens[2]) + delay)
								- System.currentTimeMillis();
							if (timeRemaining > 0) {
								npc.say("我还在吃你上次带来的食物, 这些足够吃到 "
                                        + TimeUtil.approxTimeUntil((int) (timeRemaining / 1000))
                                        + "的时间!");
                                return false;
							}
					    }
						if (amount > MAX_LUNCHES) {
							npc.say("我不能一次拿起过一周的食物！它们会坏掉!");
							return false;
						} else if (getMaximalAmount(player) < amount) {
							npc.say("我会 " + getProductionActivity() + " 你 "
									+ amount + getProductName()
									+ " 如果你带给我 "
									+ getRequiredResourceNamesWithHashes(amount) + ".");
							return false;
						} else {
							res.setAmount(amount);
							npc.say("我就想要 "
									+ getRequiredResourceNamesWithHashes(amount)
									+ ". 你带来了吗?");
							return true;
						}
					}

					@Override
					public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser npc, final Player player) {
						int amount = res.getAmount();

						if (getMaximalAmount(player) < amount) {
							// The player tried to cheat us by placing the resource
							// onto the ground after saying "yes"
							npc.say("喂! 在这儿！你最好不要骗我...");
							return false;
						} else {
							for (final Map.Entry<String, Integer> entry : getRequiredResourcesPerItem().entrySet()) {
                                final int amountToDrop = amount * entry.getValue();
                                player.drop(entry.getKey(), amountToDrop);
							}
							final long timeNow = new Date().getTime();
							player.setQuest(QUEST_SLOT, amount + ";" + getProductName() + ";"
											+ timeNow);
							npc.say("谢谢, 在 "
									+ getApproximateRemainingTime(player) + " 后回来, 我才能为你做好 "
									+ amount + getProductName() + " .");
							return true;
						}
					}

					@Override
					public void giveProduct(final EventRaiser npc, final Player player) {
						final String orderString = player.getQuest(QUEST_SLOT);
						final String[] order = orderString.split(";");
						final int numberOfProductItems = Integer.parseInt(order[0]);
						// String productName = order[1];
						final long orderTime = Long.parseLong(order[2]);
						final long timeNow = new Date().getTime();
						if (timeNow - orderTime < getProductionTime(numberOfProductItems) * 1000) {
							npc.say("欢迎回来！Oops, 我还没做好你的卷轴, 等 "
									+ getApproximateRemainingTime(player) + " 后再回来拿.");
						} else {
                        final StackableItem products = (StackableItem) SingletonRepository.getEntityManager().getItem(
                                        getProductName());

                        products.setQuantity(numberOfProductItems);

                        if (isProductBound()) {
							products.setBoundTo(player.getName());
                        }

                        player.equipOrPutOnGround(products);
                        npc.say("欢迎回来！我已把我的食物放好, 够吃上一段时间. 作为交换, 我把 "
								+ numberOfProductItems +
                                                        getProductName() + " 给你.");
                        // store the number of lunches given and the time so we know how long she eats for
						player.setQuest(QUEST_SLOT, "done" + ";" + numberOfProductItems + ";"
										+ System.currentTimeMillis());
                        // give some XP as a little bonus for industrious workers
                        player.addXP(15 * numberOfProductItems);
                        player.notifyWorldAboutChanges();
						}
					}
				}
				addReply(ConversationPhrases.YES_MESSAGES, "很烫...");
				addReply(ConversationPhrases.NO_MESSAGES, "比下雨好多!");
				addJob("我是个园丁. 希望你喜欢这些花坛.");
				addHelp("如果你带一些 #午饭 给我, 我会拿魔法卷轴给你 #交换 .");
				addOffer("我的西红柿和大蒜长的很好, 集够了我就卖掉它.");
				final Map<String, Integer> offerings = new HashMap<String, Integer>();
                offerings.put("西红柿", 30);
                offerings.put("大蒜", 50);
                new SellerAdder().addSeller(this, new SellerBehaviour(offerings), false);
				addReply("午饭", "茶 和 三明治, 请慢用!");
				addReply("三明治", "嗯.. 我喜欢汉堡和干酪一起吃.");
				addReply(Arrays.asList("卡拉文回城卷", "scroll"), "这是一个魔法卷轴, 它能把你传送回 Kalavan. 不要问我它怎么工作的!");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("茶", 1);
				requiredResources.put("三明治", 1);

				final ProducerBehaviour behaviour = new SpecialProducerBehaviour("交换", "卡拉文回城卷", requiredResources, 1 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				        "阳光不错 [daylightphase], 是吧?");
				addQuest("我喜欢泡一杯 #茶 ，园艺是个容易口渴的工作, 如果你带把 #三明治 也带来, 我拿这个卷轴 #交换 .");
				addReply(Arrays.asList("茶", "一杯茶"), "老奶奶或许也给你倒上一杯茶, 她就住在那边的大房子里.");
				addGoodbye("再见. 欢迎来花园休息.");
			}
		};

		npc.setEntityClass("gardenernpc");
		npc.setPosition(100, 123);
		npc.initHP(100);
		npc.setDescription("你见到 Sue. 她的花散发出一种梦幻的香味, 她的手指还真的是绿色的.");
		zone.add(npc);
	}

}
