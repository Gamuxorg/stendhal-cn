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
package games.stendhal.server.maps.semos.bakery;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasRecordedItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;

/**
 * A woman who bakes 面包 for players.
 *
 * Erna will lend tools.
 *
 * @author daniel / kymara
 */
public class ShopAssistantNPC implements ZoneConfigurator  {

	private static final int COST = 3000;
	private static final String QUEST_SLOT = "borrow_kitchen_equipment";

	private static final List<String> ITEMS = Arrays.asList("制糖机", "杵和臼");


	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Erna") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
                nodes.add(new Node(26,9));
                nodes.add(new Node(26,6));
                nodes.add(new Node(28,6));
                nodes.add(new Node(28,2));
                nodes.add(new Node(28,5));
                nodes.add(new Node(22,5));
                nodes.add(new Node(22,4));
                nodes.add(new Node(22,7));
                nodes.add(new Node(26,7));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("我是本面包店的助理.");
				addReply("面粉",
				"我们用的 #面粉 是镇东北方的磨坊生产的, 用本地出产的麦子研磨. 但狼群咬伤了他们的送货员! 如果你能帮我们带一些面粉过来, 我们能 #烘焙 出好吃的面包给你.");
				addHelp("面包对人很好, 尤其在你外出冒险时, 不能总吞下红色的生肉. 我的老板 蓝德 是这个岛上做的三明治最好的面包师!");
				addGoodbye();

				// Erna bakes 面包 if you bring her 面粉.
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("面粉", 2);

				final ProducerBehaviour behaviour = new ProducerBehaviour("erna_bake_bread",
						"烘焙", "面包", requiredResources, 10 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				"欢迎来到 塞门镇面包房! 我们为能把磨坊的面粉送到这里的人烤 #烘焙 出好吃的面包. ");

				addOffer("我们的 比萨 外卖团队可以 #借 一些厨师制服给你.");

				add(ConversationStates.ATTENDING, "借",
				    new LevelLessThanCondition(6),
				    ConversationStates.ATTENDING,
				    "Oh 抱歉, 我不能借给像你这样只有这么一点经验的新人.",
				    null);

				add(ConversationStates.ATTENDING, "借",
				    new AndCondition(new LevelGreaterThanCondition(5), new QuestNotCompletedCondition("pizza_delivery")),
				    ConversationStates.ATTENDING,
				    "你需要和 蓝德 讲, 问他如果你能帮送 比萨 外卖, 然后我才可能会借给你.",
				    null);

				add(ConversationStates.ATTENDING, "借",
				    new AndCondition(
				        new LevelGreaterThanCondition(5),
				        new QuestCompletedCondition("pizza_delivery"),
				        new QuestNotActiveCondition(QUEST_SLOT)),
				    ConversationStates.ATTENDING,
				    "我把 " + ITEMS + " 借给你. 如果你有兴趣, 请对我说.",
				    null);

				// player already has 借ed something it didn't return and will pay for it
				add(ConversationStates.ATTENDING, "借",
				    new AndCondition(new QuestActiveCondition(QUEST_SLOT), new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT))),
				    ConversationStates.QUESTION_1,
				    "你没归还我上次借给你的东西！你想为此付 " + COST + " 钱给我吗?",
				    null);

				// player already has 借ed something it didn't return and will return it
				add(ConversationStates.ATTENDING, "借",
				    new AndCondition(new QuestActiveCondition(QUEST_SLOT), new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT)),
				    ConversationStates.QUESTION_2,
				    "你没有归还我上次借给你的东西！现在要归还吗？?",
				    null);

				// player wants to pay for previous item
				final List<ChatAction> payment = new LinkedList<ChatAction>();
				payment.add(new DropItemAction("money", COST));
				payment.add(new SetQuestAction(QUEST_SLOT, "done"));
				payment.add(new DecreaseKarmaAction(10));
				add(ConversationStates.QUESTION_1,
				    ConversationPhrases.YES_MESSAGES,
				    new PlayerHasItemWithHimCondition("money", COST),
				    ConversationStates.ATTENDING,
				    "谢谢, 如果你想 #借 任何工具, 再跟我一声说就行",
				    new MultipleActions(payment));

				// player already has 借ed something and wants to return it
				final List<ChatAction> returnitem = new LinkedList<ChatAction>();
				returnitem.add(new DropRecordedItemAction(QUEST_SLOT));
				returnitem.add(new SetQuestAction(QUEST_SLOT, "done"));
				add(ConversationStates.QUESTION_2,
				    ConversationPhrases.YES_MESSAGES,
				    new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT),
				    ConversationStates.ATTENDING,
				    "谢谢, 如果你想 #借 任何工具, 再跟我一声说就行",
				    new MultipleActions(returnitem));

				// don't want to pay for it now
				add(ConversationStates.QUESTION_1,
				    ConversationPhrases.NO_MESSAGES,
				    null,
				    ConversationStates.ATTENDING,
				    "没问题, 你借用多久都可以, 但一次只能借一件, 再借要把上次的还回来. 否则要赔钱.",
				    null);
				// does want to pay for it now
				add(ConversationStates.QUESTION_1,
				    ConversationPhrases.YES_MESSAGES,
				    new NotCondition(new PlayerHasItemWithHimCondition("money", COST)),
				    ConversationStates.ATTENDING,
				    "抱歉, 但是好像你的钱不够.",
				    null);

				// don't want to return it now
				add(ConversationStates.QUESTION_2,
				    ConversationPhrases.NO_MESSAGES,
				    null,
				    ConversationStates.ATTENDING,
				    "没问题, 你借用多久都可以, 但一次只能借一件, 再借要把上次的还回来. 否则要付钱.",
				    null);


				// saying the item name and storing that item name into the quest slot, and giving the item
				for(final String itemName : ITEMS) {
					add(ConversationStates.ATTENDING,
					    itemName,
					    new AndCondition(
					        new LevelGreaterThanCondition(5),
					        new QuestCompletedCondition("pizza_delivery"),
					        new QuestNotActiveCondition(QUEST_SLOT)),
					    ConversationStates.ATTENDING,
					    null,
					    new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
								final Item item =  SingletonRepository.getEntityManager().getItem(itemName);
								if (item == null) {
									npc.say("抱歉, 好像错了, 请你再说一次正确的物品名称. ");
								} else {
									player.equipOrPutOnGround(item);
									player.setQuest(QUEST_SLOT, itemName);
									npc.say("给你！不要忘记 #归还 , 否则你将为此付钱!");
								}
							}
						});
				}

				// additionally add "糖块" as trigger word
				add(ConversationStates.ATTENDING,
					    "糖块",
					    new AndCondition(
					        new LevelGreaterThanCondition(5),
					        new QuestCompletedCondition("pizza_delivery"),
					        new QuestNotActiveCondition(QUEST_SLOT)),
					    ConversationStates.ATTENDING,
					    "抱歉, 我不能借给你糖块, 只有 #制糖机 .",
					    null);

				// too low level
				add(ConversationStates.ATTENDING,
					    ITEMS,
					    new LevelLessThanCondition(6),
					    ConversationStates.ATTENDING,
					    "抱歉, 你在这里的经验太少, 我还不能信任你.",
					    null);

				// currently has 借ed an item
				add(ConversationStates.ATTENDING,
					    ITEMS,
					    new QuestActiveCondition(QUEST_SLOT),
					    ConversationStates.ATTENDING,
					    "你把上次借的东西还给我, 才能再借新的.",
					    null);

				// haven't done 比萨
				add(ConversationStates.ATTENDING,
					    ITEMS,
					    new QuestNotCompletedCondition("pizza_delivery"),
					    ConversationStates.ATTENDING,
					    "只有 比萨 外卖人员才能借工具. 请为 蓝德 送一次外卖, 再说借的事.",
					    null);

				// player asks about pay from attending state
				add(ConversationStates.ATTENDING, Arrays.asList("赔钱", "赔付", "支付"),
				    new QuestActiveCondition(QUEST_SLOT),
				    ConversationStates.QUESTION_1,
				    "如果你弄丢了借我的东西, 你可以赔付 " + COST + " 金币. 你现在想付钱吗?",
				    null);

				// player asks about return from attending state
				add(ConversationStates.ATTENDING, "归还",
				    new AndCondition(new QuestActiveCondition(QUEST_SLOT), new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT)),
				    ConversationStates.QUESTION_2,
				    "你现在要把借的工具归还吗？",
				    null);

				// player asks about return from attending state
				add(ConversationStates.ATTENDING, "归还",
				    new AndCondition(new QuestActiveCondition(QUEST_SLOT), new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT))),
				    ConversationStates.QUESTION_1,
				    "你没带着借我的工具呀！要为此赔付 " + COST + " 金币我吗?",
				    null);

			}};
			npc.setPosition(26, 9);
			npc.setEntityClass("housewifenpc");
			npc.setDescription("你看到了 Erna. 她为 蓝德 工作了很长时间, 现在是他忠实的助理.");
			zone.add(npc);
	}
}

