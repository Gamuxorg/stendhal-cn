/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.Rand;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OutfitCompatibleWithClothesCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: 比萨 Delivery
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li> 蓝德 (the baker in 塞门镇)
 * <li> NPC's all over the world (as customers)
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> 蓝德 gives you a 比萨 and tells you who ordered it, and how much
 * time you have to deliver.
 * <li> As a gimmick, you get a 比萨 delivery uniform.
 * <li> You walk to the customer and say "比萨".
 * <li> The customer takes the 比萨. If you were fast enough, you get a tip.
 * <li> You put on your original clothes automatically.
 * </ul>
 * REWARD:
 * <ul>
 * <li> XP (Amount varies depending on customer. You only get half of the XP if
 * the 比萨 has become cold.)
 * <li> some karma if delivered on time (5)
 * <li> gold coins (As a tip, if you were fast enough; amount varies depending
 * on customer.)
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> As many as wanted, but you can't get a new task while you still have the
 * chance to do the current delivery on time.
 * </ul>
 */
public class PizzaDelivery extends AbstractQuest {
	private static final Logger logger = Logger.getLogger(PizzaDelivery.class);
	// FIXME: return to "final" after outfit testing is finished
	private static Outfit UNIFORM;

	public PizzaDelivery() {
		UNIFORM = new Outfit(null, null, null, Integer.valueOf(90), null);
	}

	/**
	 * A customer data object.
	 */
	static class CustomerData {
		/** A hint where to find the customer. */
		private final String npcDescription;

		/** The 比萨 style the customer likes. */
		private final String flavor;

		/** The time until the 比萨 should be delivered. */
		private final int expectedMinutes;

		/** The money the player should get on fast delivery. */
		private final int tip;

		/**
		 * The experience the player should gain for delivery. When the 比萨
		 * has already become cold, the player will gain half of this amount.
		 */
		private final int xp;

		/**
		 * The text that the customer should say upon quick delivery. It should
		 * contain %d as a placeholder for the tip, and can optionally contain
		 * %s as a placeholder for the 比萨 flavor.
		 */
		private final String messageOnHotPizza;

		/**
		 * The text that the customer should say upon quick delivery. It can
		 * optionally contain %s as a placeholder for the 比萨 flavor.
		 */
		private final String messageOnColdPizza;

		/**
		 * The min level player who can get to this NPC
		 */
		private final int level ;

		/**
		 * Creates a CustomerData object.
		 *
		 * @param npcDescription
		 * @param flavor
		 * @param expectedTime
		 * @param tip
		 * @param xp
		 * @param messageHot
		 * @param messageCold
		 * @param level
		 */
		CustomerData(final String npcDescription, final String flavor,
				final int expectedTime, final int tip, final int xp, final String messageHot,
				final String messageCold, final int level) {
			this.npcDescription = npcDescription;
			this.flavor = flavor;
			this.expectedMinutes = expectedTime;
			this.tip = tip;
			this.xp = xp;
			this.messageOnHotPizza = messageHot;
			this.messageOnColdPizza = messageCold;
			this.level = level;
		}

		/**
		 * Get the minimum level needed for the NPC
		 *
		 * @return minimum level
		 */
		public int getLevel() {
			return level;
		}
	}

	private static final String QUEST_SLOT = "pizza_delivery";

	private static Map<String, CustomerData> customerDB;



	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("我见到 蓝德, 并同意帮他送出比萨快递");
		if (!"done".equals(questState)) {
			final String[] questData = questState.split(";");
			final String customerName = questData[0];
			final CustomerData customerData = customerDB.get(customerName);
			res.add("蓝德 给了我一份 " + customerData.flavor + " 要送给 " + customerName + ".");
			res.add("蓝德 告诉我: \"" + customerData.npcDescription + "\"");
			if (!isDeliveryTooLate(player)) {
				res.add("如果我快点, 我尽可能保证比萨还是热的. ");
			} else {
				res.add("比萨已经凉了. ");
			}
		} else {
			res.add("我送出了蓝德给我的最后一份比萨. ");
		}
		return res;
	}

	// Don't add 莎丽 here, as it would conflict with 蓝德 telling
	// about his daughter.
	private static void buildCustomerDatabase() {
		customerDB = new HashMap<String, CustomerData>();

		customerDB.put("巴尔顿",
			new CustomerData(
				"巴尔顿 是一个隐士, 他住在Semons 和 Ados 之间的一坐大山中. 他被称为 Ados Rock. 要从这里往东走. ",
				"比萨Prosciutto",
				// minutes to deliver. Tested by mort: 6:30
				// min, with killing some orcs.
				7,
				// tip when delivered on time. Quite
				// high because you can't do much
				// senseful on top of the hill and must
				// walk down again.
				200,
				// experience gain for delivery
				300,
				"谢谢！我想知道你如何这么快的送出去的. 请收下这 %d 片金子, 我在这没办法花出去!",
				"Brrr.  %s 已不热了, 好吧, 还是谢谢你的努力. ",
				10));

		customerDB.put("Cyk",
			new CustomerData(
				"Cyk 目前在 Anthor 岛度假. 他有一头蓝色头发, 你很容易认出他, 要先去东南方找Athor渡口. ",
				"比萨 Hawaii",
				// minutes to deliver. You need about 6 min
				// to Eliza, up to 12 min to wait for the
				// ferry, 5 min for the crossing, and 0.5
				// min from the docks to the beach, so you
				// need a bit of luck for this one.
				20,
				// tip when delivered on time
				300,
				// experience gain for delivery
				500,
				"Wow, 我不相信你能跑半个世界送过来！给, 拿着这 %s bucks!",
				"已经凉了, 虽然我在如此远的面包店订了比萨的期望.....还是谢谢你. ",
				20));

		customerDB.put("Eliza",
			new CustomerData(
				"Eliza 在Athor岛做渡口服务工作. 你可以在 Ados 沼泽南方的码头找到她. ",
				"比萨 del Mare",
				// minutes to deliver. Tested by mort: 6
				// min, ignoring slow animals and not
				// walking through the swamps.
				7,
				// tip when delivered on time.
				170,
				// experience gain for delivery
				300,
				"不敢相信! 真的还是热的, 很好的一次购物, 收下这 %d 片金子!",
				"可惜. 已经凉了. 尽管如此还是谢谢你!",
				20));

		customerDB.put("费多拉",
			new CustomerData(
				"费多拉 生活在Ados 城. 她是一位化装艺术家. 从这里往东可以找到她. ",
				"比萨 Napoli",
				// minutes to deliver. Tested by mort: about
				// 6 min, outrunning all enemies.
				7,
				// tip when delivered on time
				150,
				// experience gain for delivery
				200,
				"非常感谢！你就是为送比萨而生. 这 %d 片金币作为小费送给你!",
				"Bummer. 凉凉.",
				15));

		customerDB.put("海震",
			new CustomerData(
				"海震 是一位魔法师, 他住在到达Ados路边的小屋, 从这里往东然后再往北走就到了. ",
				"比萨 Diavolo",
				// minutes to deliver. Tested by kymara:
				// exactly 3 min.
				4,
				// tip when delivered on time
				80,
				// experience gain for delivery
				150,
				"啊, 我的 %s 居然还是新鲜的！这是你的小费 %d 枚钱币!",
				"我希望下次拿到比萨时, 它还是热的. ",
				10));

		customerDB.put("詹妮",
			new CustomerData(
				"詹妮 拥有一座磨坊, 地点在 Semons 镇的北方稍偏东的平原中",
				"比萨 Margherita",
				// minutes to deliver. Tested by mort: can
				// be done in 1:15 min, with no real danger.
				2,
				// tip when delivered on time
				20,
				// experience gain for delivery
				50,
				"啊, 你带来了我的 %s! 你太棒了！来, 这是给你的小费 %d 金币!",
				"不好意思. 你的比萨 怎么不是热的？面包房不就在离这不远的城里？",
				2));

		customerDB.put("Jynath",
			new CustomerData(
				"Jynath 是个女巫, 她住在 Or'ril 城堡南部的小房子里. 你需要往西南方向走, 穿过森林, 然后延路一直向西, 直到看到她的房子",
				"比萨 Funghi",
				// minutes to deliver. Tested by mort: 5:30
				// min, leaving the slow monsters on the way
				// behind.
				6,
				// tip when delivered on time
				140,
				// experience gain for delivery
				200,
				"Oh, 我没期望你能这么快来, 太好了, 通常我不给小费, 但这次你的杰出表现, 这是你的 %d 片金子.",
				"太差了... 我必须用一个超强魔法给比萨重新加热. ",
				5));

		customerDB.put("Katinka",
			new CustomerData(
				"Katinka 在Ados野生动物避难所, 照料着很多小动物. 在这的东北方, Ados大路的旁边. ",
				"比萨 Vegetale",
				// minutes to deliver. Tested by kymara in
				// 3:25 min, leaving behind the orcs.
				4,
				// tip when delivered on time
				100,
				// experience gain for delivery
				200,
				"呀! 我的 %s! 到了, 你得到 %d 片金子作为小费!",
				"恶. 我恨冷比萨. 我会把它喂动物们了. ",
				10));

		customerDB.put("马鲁斯",
			new CustomerData(
				"马鲁斯 是 Semon 牢房的守卫. 从这往西走到头, 在Semon 村子的远方. ", "比萨 Tonno",
				// minutes to deliver. Tested by kymara: takes longer than before due to fence in village
				3,
				// tip when delivered on time. A bit higher than 詹妮
				// because you can't do anything else in the jail and need
				// to walk out again.
				25,
				// experience gain for delivery
				100,
				"啊, 我的 %s! 这是你的小费: %d 片黄金.",
				"这就是结果! 为什么花这么长时间?",
				2));

		customerDB.put("尼世亚",
			new CustomerData(
				"尼世亚 卖羊人. 要吧在西边找到他, 就是村子中. ",
				"比萨 Pasta",
				// minutes to deliver. Tested by mort: easy
				// to do in less than 1 min.
				1,
				// tip when delivered on time
				10,
				// experience gain for delivery
				25,
				"谢谢你! 太快了, 快拿着这些钱 %d !",
				"很不好. 都凉了, 谢了.",
				0));

		customerDB.put("奥斯特",
			new CustomerData(
				"奥斯特 是个军火商, 他现在在塞门镇酒馆的二楼出租房中.",
				"比萨 Quattro Stagioni",
				// minutes to deliver. Tested by mort: can
				// be done in 45 sec with no danger.
				1,
				// tip when delivered on time
				10,
				// experience gain for delivery
				25,
				"谢谢你! 你的比萨服务真好, 这是你的小费 %d 金币!",
				"我应该自已去面包房取, 或许还快点. ",
				0));

		customerDB.put("Ramon",
			new CustomerData(
				"Ramon 在到Athor岛的渡口作赌场发牌手, 从这里往东南方向走到港口末端, 路很长!",
				"比萨 Bolognese",
				// minutes to deliver. You need about 6 mins
				// to Eliza, and once you board the ferry,
				// about 15 sec to deliver. If you have bad
				// luck, you need to wait up to 12 mins for
				// the ferry to arrive at the mainland, so
				// you need a bit of luck for this one.
				14,
				// tip when delivered on time
				250,
				// experience gain for delivery
				400,
				"非常感谢！我能吃到比Laura做的可怕食物好的多的食品. 这是 %d 金子小费, 拿着！",
				"太差. 都凉子. 这希望可以吃到比战舰食品好的东西. ",
				20));

		customerDB.put("Tor'Koom",
			new CustomerData(
				"Tor'Koom 是一个兽人, 住在塞门镇城地下的地牢中. 羊是他最爱的食物. 他住在地下四层, 小心!",
				// "比萨 sheep" in Italian ;)
				"比萨 Pecora",
				// minutes to deliver. Tested by kymara:
				// done in about 8 min, with lots of monsters getting in your way.
				9,
				// tip when delivered on time
				170,
				// experience gain for delivery
				300,
				"Yummy %s! 过来, 拿走这些 %d 钱!",
				"Grrr. 比萨 凉了, 你走的像羊一样慢.",
				15));

		customerDB.put("Martin Farmer",
				new CustomerData(
					"Martin 农夫正在Ados城度假. 你需要从这里往东走",
					"比萨 Fiorentina",
					// minutes to deliver. Time for 费多拉 was 7, so 8 should be ok for martin
					8,
					// tip when delivered on time
					160,
					// experience gain for delivery
					220,
					"Ooooh, 我爱刚出炉的比萨, 谢谢, 拿上这些 %d money...!",
					"Hmpf.. 凉比萨.. ok.. 我收下了, 不过下次快点.",
					10));
	}

	private void startDelivery(final Player player, final EventRaiser npc) {

		final String name = Rand.rand(getAllowedCustomers(player));
		final CustomerData data = customerDB.get(name);

		final Item pizza = SingletonRepository.getEntityManager().getItem("比萨");
		pizza.setInfoString(data.flavor);
		pizza.setDescription("你看了 " + data.flavor + ".");
		pizza.setBoundTo(name);

		if (player.equipToInventoryOnly(pizza)) {
    		npc.say("你必须带它 "
    			+ data.flavor
    			+ " 到 "
    			+ "#" + name
    			+ " 在 "
    			+ data.expectedMinutes+ "分钟"
    			+ " 内. 然后说 \"比萨\" 完成运送 "
    			+ name
    			+ " 知道是这发给你, 噢, 请穿上这个快递装, 并且别把它丢到地上 " + data.flavor + " 在地上! 我们的顾客都喜欢新鲜的. ");
    		player.setOutfit(UNIFORM, true);
    		player.setQuest(QUEST_SLOT, name + ";" + System.currentTimeMillis());
		} else {
			npc.say("当你背包有空间了再来拿比萨!");
		}
	}

	/**
	 * Get a list of customers appropriate for a player
	 *
	 * @param player the player doing the quest
	 * @return list of customer data
	 */
	private List<String> getAllowedCustomers(Player player) {
		List<String> allowed = new LinkedList<String>();
		int level = player.getLevel();
		for (Map.Entry<String, CustomerData> entry : customerDB.entrySet()) {
			if (level >= entry.getValue().getLevel()) {
				allowed.add(entry.getKey());
			}
		}
		return allowed;
	}

	/**
	 * Checks whether the player has failed to fulfil his current delivery job
	 * in time.
	 *
	 * @param player
	 *            The player.
	 * @return true if the player is too late. false if the player still has
	 *         time, or if he doesn't have a delivery to do currently.
	 */
	private boolean isDeliveryTooLate(final Player player) {
		if (player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT)) {
			final String[] questData = player.getQuest(QUEST_SLOT).split(";");
			final String customerName = questData[0];
			final CustomerData customerData = customerDB.get(customerName);
			final long bakeTime = Long.parseLong(questData[1]);
			final long expectedTimeOfDelivery = bakeTime
				+ (long) 60 * 1000 * customerData.expectedMinutes;
			if (System.currentTimeMillis() > expectedTimeOfDelivery) {
				return true;
			}
		}
		return false;

	}

	private void handOverPizza(final Player player, final EventRaiser npc) {
		if (player.isEquipped("比萨")) {
			final CustomerData data = customerDB.get(npc.getName());
			for (final Item pizza : player.getAllEquipped("比萨")) {
				final String flavor = pizza.getInfoString();
				if (data.flavor.equals(flavor)) {
					player.drop(pizza);
					// Check whether the player was supposed to deliver the
					// 比萨.
					if (player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT)) {
						if (isDeliveryTooLate(player)) {
							if (data.messageOnColdPizza.contains("%s")) {
								npc.say(String.format(data.messageOnColdPizza, data.flavor));
							} else {
								npc.say(data.messageOnColdPizza);
							}
							player.addXP(data.xp / 2);
						} else {
							if (data.messageOnHotPizza.contains("%s")) {
								npc.say(String.format(data.messageOnHotPizza,
										data.flavor, data.tip));
							} else {
								npc.say(String.format(data.messageOnHotPizza,
										data.tip));
							}
							final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
							money.setQuantity(data.tip);
							player.equipOrPutOnGround(money);
							player.addXP(data.xp);
							player.addKarma(5);
						}
						new InflictStatusOnNPCAction("比萨").fire(player, null, npc);
						player.setQuest(QUEST_SLOT, "done");
						putOffUniform(player);
					} else {
						// This should not happen: a player cannot pick up a 比萨 from the ground
						// that did have a flavor, those are bound. If a 比萨 has flavor the player
						// should only have got it from the quest.
						npc.say("Eek! 比萨都脏了！你是在地上找的吗？");
					}
					return;
				}
			}
			// The player has brought the 比萨 to the wrong NPC, or it's a plain 比萨.
			npc.say("不, 谢谢, 我喜欢 " + data.flavor + " 更好.");
		} else {
			npc.say("有比萨? 在哪?");
		}
	}

	/** Takes away the player's uniform, if the he is wearing it.
	 * @param player to remove uniform from*/
	private void putOffUniform(final Player player) {
		if (UNIFORM.isPartOf(player.getOutfit())) {
			player.returnToOriginalOutfit();
		}
	}

	private void prepareBaker() {
		final SpeakerNPC leander = npcs.get("蓝德");

		// haven't done the 比萨 quest before or already delivered the last one, ok to wear 比萨 outfit
		leander.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new OutfitCompatibleWithClothesCondition(), new QuestNotActiveCondition(QUEST_SLOT)),
				ConversationStates.QUEST_OFFERED,
				"我需要你以最快速度送出热的比萨. 如果你足够快, 你可以得到小费, 这个单你接吗？",
				null);

		// haven't done the 比萨 quest before or already delivered the last one, outfit would be incompatible with 比萨 outfit
		leander.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new OutfitCompatibleWithClothesCondition()), new QuestNotActiveCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"抱歉, 你不能穿上我们的比萨快递. 如果你改变主意. 你可以再向我问 #task 接单.",
				null);

		// 比萨 quest is active: check if the delivery is too late already or not
		leander.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final String[] questData = player.getQuest(QUEST_SLOT)
								.split(";");
						final String customerName = questData[0];
						if (isDeliveryTooLate(player)) {
							// If the player still carries any 比萨 due for an NPC,
							// take it away because the baker is angry,
							// and because the player probably won't
							// deliver it anymore anyway.
							for (final Item pizza : player.getAllEquipped("比萨")) {
								if (pizza.getInfoString()!=null) {
									player.drop(pizza);
								}
							}
							npc.say("我知道你没有按时送给 "
								+ customerName
								+ " . 上次任务失败了, 你确定这次可以按时送货吗?");
						} else {
							npc.say("你还要送比萨给 "
									+ customerName + ", 赶快!");
							npc.setCurrentState(ConversationStates.ATTENDING);
						}
				}
			});

		leander.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					startDelivery(player, npc);
				}
			});

		leander.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"糟糕. 我希望我女儿 #莎丽 能从露营中赶回来, 可以帮我送货. ",
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					putOffUniform(player);
				}
			});

		for (final String name : customerDB.keySet()) {
			final CustomerData data = customerDB.get(name);
			leander.addReply(name, data.npcDescription);
		}
	}

	private void prepareCustomers() {
		for (final String name : customerDB.keySet()) {
			final SpeakerNPC npc = npcs.get(name);
			if (npc == null) {
				logger.error("NPC " + name + " is used in the 比萨 Delivery quest but does not exist in game.", new Throwable());
				continue;
			}

			npc.add(ConversationStates.ATTENDING, "比萨", null,
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						handOverPizza(player, npc);
					}
				});
		}
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"比萨 Delivery",
				"蓝德 的 比萨 店招收新手男女快递员.",
				false);
		buildCustomerDatabase();
		prepareBaker();
		prepareCustomers();
	}

	@Override
	public String getName() {
		return "PizzaDelivery";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "蓝德";
	}
}
