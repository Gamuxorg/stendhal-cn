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
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.Rand;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.PoisonStatus;

/**
 * QUEST: Find the seven cherubs that are all around the world.
 *
 * PARTICIPANTS:
 * <ul>
 * <li> Cherubiel </li>
 * <li> Gabriel </li>
 * <li> Ophaniel </li>
 * <li> Raphael </li>
 * <li> Uriel </li>
 * <li> Zophiel </li>
 * <li> Azazel </li>
 *
 * STEPS:
 * <ul>
 * <li> Find them and they will reward you. </li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 2,000 XP </li>
 * <li> some karma (35) </li>
 * <li> either 烈火剑, 金靴子, 黄金铠甲, or 黄金头盔 </li>
 * </ul>
 *
 * REPETITIONS: - Just once.
 */
public class SevenCherubs extends AbstractQuest {
	private static final String QUEST_SLOT = "seven_cherubs";

	private final HashMap<String, String> cherubsHistory = new HashMap<String,String>();

	private void fillHistoryMap() {
		cherubsHistory.put("Cherubiel", "我在 塞门村庄 遇见了 Cherubiel");
		cherubsHistory.put("Ophaniel",  "我在 Orril 河边 遇见了 Ophaniel.");
		cherubsHistory.put("Gabriel",   "我在 Nalwor 遇见了 Gabriel.");
		cherubsHistory.put("Raphael",   "我在 Orril 河和去 Fado 的桥 中间遇见了 Raphael");
		cherubsHistory.put("Zophiel",   "我在 塞门山 遇见了 Zophiel");
		cherubsHistory.put("Azazel",    "我在 Ados Rock 遇见了 Azazel");
		cherubsHistory.put("Uriel",     "我在I Orril 山 遇见了 Orril");
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public boolean isCompleted(final Player player) {
		if (!player.hasQuest(QUEST_SLOT)) {
			return false;
		}
		final String npcDoneText = player.getQuest(QUEST_SLOT);
		final String[] done = npcDoneText.split(";");
		final int left = 7 - done.length;
		return left < 0;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (player.hasQuest(QUEST_SLOT)) {
			final String npcDoneText = player.getQuest(QUEST_SLOT);
			final String[] done = npcDoneText.split(";");
			boolean first = true;
			for (final String cherub : done) {
				if (!cherub.trim().equals("")) {
					if (first) {
						first = false;
						res.add("我开始寻找这七个天使");
					}
					res.add(cherubsHistory.get(cherub));
				}
			}
			if (isCompleted(player)) {
				res.add("完成, 我发现了所有的天使!");
			}
		}
		return res;
	}

	static class CherubNPC extends SpeakerNPC {
		public CherubNPC(final String name, final int x, final int y) {
			super(name);

			setEntityClass("angelnpc");
			setPosition(x, y);
			initHP(100);

			final List<Node> nodes = new LinkedList<Node>();
			nodes.add(new Node(x, y));
			nodes.add(new Node(x - 2, y));
			nodes.add(new Node(x - 2, y - 2));
			nodes.add(new Node(x, y - 2));
			setPath(new FixedPath(nodes, true));
		}

		@Override
		protected void createPath() {
			// do nothing
		}

		@Override
		protected void createDialog() {
			add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new GreetingMatchesNameCondition(getName()), true,
				ConversationStates.IDLE, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (!player.hasQuest(QUEST_SLOT)) {
							player.setQuest(QUEST_SLOT, "");
						}

						// Visited cherubs are store in the quest-name
						// QUEST_SLOT.
						// Please note that there is an additional empty
						// entry in the beginning.
						final String npcDoneText = player.getQuest(QUEST_SLOT);
						final String[] done = npcDoneText.split(";");
						final List<String> list = Arrays.asList(done);
						final int left = 7 - list.size();

						if (list.contains(raiser.getName())) {
							if (left > -1) {
								raiser.say("找出其他的天使能得到奖励!");
							} else {
								raiser.say("汝的眼睛能看到这七个天使！ 现在, 这是汝应得了奖赏.");
							}
						} else {
							player.setQuest(QUEST_SLOT, npcDoneText + ";"
									+ raiser.getName());

							player.heal();
							player.getStatusList().removeAll(PoisonStatus.class);

							if (left > 0) {
								raiser.say("不错！你再发现 "
												+ (7 - list.size())
												+ " 个. Fare thee well!");
								if (raiser.getZone().getName().equals("0_塞门_村庄_西")) {
									player.addXP(20);
								} else {
									player.addXP((7 - left + 1) * 200);
								}
							} else {
								raiser.say("这已证明了你的勇敢, 你已具体配戴这个上古神器了！");

								/*
								 * Proposal by Daniel Herding (mort): once
								 * we have enough quests, we shouldn't have
								 * this randomization anymore. There should
								 * be one hard quest for each of the golden
								 * items.
								 *
								 * I commented out the 金盾 here
								 * because you already get that from the
								 * CloaksForBario quest.
								 *
								 * 黄金护腿 was disabled because it can
								 * be won in DiceGambling.
								 *
								 * 烈火剑 was disabled because it can be
								 * earned by fighting, and because the
								 * stronger 冰剑 is available through
								 * other quest and through fighting.
								 *
								 * Once enough quests exist, this quest
								 * should always give you 金靴子
								 * (because you have to walk much to fulfil
								 * it).
								 *
								 */
								final String[] items = { "金靴子", "黄金铠甲", "黄金头盔" };
								final Item item = SingletonRepository.getEntityManager()
									.getItem(items[Rand.rand(items.length)]);
								item.setBoundTo(player.getName());
								player.equipOrPutOnGround(item);
								player.addXP(2000);
								player.addKarma(35);
							}
						}
						player.notifyWorldAboutChanges();
					}
				});
			addGoodbye();
		}
	}

	@Override
	public void addToWorld() {
		final StendhalRPWorld world = SingletonRepository.getRPWorld();
		fillHistoryMap();
		fillQuestInfo(
				"七个天使",
				"七个天使留在了这个世界上, 找到他们并赢取奖励",
				false);
		StendhalRPZone zone;
		SpeakerNPC npc;

		zone = world.getZone("0_塞门_村庄_西");
		npc = new CherubNPC("Cherubiel", 32, 60);
		zone.add(npc);

		zone = world.getZone("0_纳尔沃_城");
		npc = new CherubNPC("Gabriel", 105, 17);
		zone.add(npc);

		zone = world.getZone("0_orril_river_s");
		npc = new CherubNPC("Ophaniel", 105, 79);
		zone.add(npc);

		zone = world.getZone("0_orril_river_s_w2");
		npc = new CherubNPC("Raphael", 95, 30);
		zone.add(npc);

		zone = world.getZone("0_orril_mountain_w2");
		npc = new CherubNPC("Uriel", 47, 27);
		zone.add(npc);

		zone = world.getZone("0_塞门_山_北2_西2");
		npc = new CherubNPC("Zophiel", 16, 3);
		zone.add(npc);

		zone = world.getZone("0_阿多斯_巨石");
		npc = new CherubNPC("Azazel", 67, 24);
		zone.add(npc);
	}

	@Override
	public String getName() {
		return "七个天使";
	}
}
