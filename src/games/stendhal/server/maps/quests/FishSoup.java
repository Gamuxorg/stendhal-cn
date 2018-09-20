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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.PoisonStatus;
import games.stendhal.server.maps.Region;
import marauroa.common.game.IRPZone;

/**
 * QUEST: Special 鱼汤.
 * <p>
 * PARTICIPANTS: <ul><li> Florence Boullabaisse on Ados market</ul>
 *
 * STEPS: <ul><li> Florence Boullabaisse tells you the ingredients of a special 鱼汤 <li> You
 * collect the ingredients <li> You bring the ingredients to Florence <li> The soup
 * is served at a market table<li> Eating the soup heals you fully over time <li> Making it adds karma
 * </ul>
 *
 * REWARD: <ul><li>healing soup <li> Karma bonus of 10 (if ingredients given individually)<li>50 XP</ul>
 *
 * REPETITIONS: <ul><li> as many as desired <li> Only possible to repeat once every twenty
 * minutes</ul>
 *
 * @author Vanessa Julius and Krupi (idea)
 */
public class FishSoup extends AbstractQuest {

	private static final List<String> NEEDED_FOOD = Arrays.asList("surgeonfish",
			"cod", "char", "roach", "clownfish", "洋葱", "mackerel",
			"大蒜", "韭菜", "perch", "西红柿");

	private static final String QUEST_SLOT = "fishsoup_maker";

	private static final int REQUIRED_MINUTES = 20;

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	/**
	 * Returns a list of the names of all food that the given player still has
	 * to bring to fulfil the quest.
	 *
	 * @param player
	 *            The player doing the quest
	 * @param hash
	 *            If true, sets a # character in front of every name
	 * @return A list of food item names
	 */
	private List<String> missingFood(final Player player, final boolean hash) {
		final List<String> result = new LinkedList<String>();

		String doneText = player.getQuest(QUEST_SLOT);
		if (doneText == null) {
			doneText = "";
		}
		final List<String> done = Arrays.asList(doneText.split(";"));
		for (String ingredient : NEEDED_FOOD) {
			if (!done.contains(ingredient)) {
				if (hash) {
					ingredient = "#" + ingredient;
				}
				result.add(ingredient);
			}
		}
		return result;
	}

	/**
	 * Serves the soup as a reward for the given player.
	 * @param player to be rewarded
	 */
	private void placeSoupFor(final Player player) {
		final Item soup = SingletonRepository.getEntityManager()
				.getItem("鱼汤");
		final IRPZone zone = SingletonRepository.getRPWorld().getZone("0_阿多斯_城_n2");
		// place on market table
		soup.setPosition(64, 15);
		// only allow player who made soup to eat the soup
		soup.setBoundTo(player.getName());
		// here the soup is altered to have the same heal value as the player's
		// base HP. soup is already persistent so it will last.
		soup.put("amount", player.getBaseHP());
		zone.add(soup);
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Florence Boullabaisse");

		// player says hi before starting the quest
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.INFORMATION_1,
			"你好, 欢迎来到 Ados 市场! 我有一些好吃的, 你要要点什么 #revive . ",
			null);

		// player returns after finishing the quest (it is repeatable) after the
		// time as finished
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestCompletedCondition(QUEST_SLOT),
					new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
			ConversationStates.QUEST_OFFERED,
			"再次欢迎, 你是因为我的鲜鱼汤而再次光临吗？",
			null);

		// player returns after finishing the quest (it is repeatable) before
		// the time as finished
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES , "Oh 抱歉. 再次做汤之前, 我需要首先洗净炊具, 请回来 在")
			);

		// player responds to word 'revive'
		npc.add(ConversationStates.INFORMATION_1,
				"revive",
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					if (player.hasQuest(QUEST_SLOT) && player.isQuestCompleted(QUEST_SLOT)) {
						npc.say("现在我已经弄清了做好鱼汤的密诀. ");
						npc.setCurrentState(ConversationStates.ATTENDING);
					} else {
						npc.say("我的密制鱼汤有魔法伤.  "
								+ "我需要你云带来一些 #ingredients.");
					}
				}
			});

		// player asks what exactly is missing
		npc.add(ConversationStates.QUEST_OFFERED, "ingredients", null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final List<String> needed = missingFood(player, true);
					npc.say("做汤之前, 我需要 "
							+ needed.size() + 
									"ingredient"
							+ " : "
							+ needed
							+ ". 你已经弄到了?");
				}
			});

		// player is willing to collect
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.QUESTION_1,
			"虽然你有其他选择, 但我求你不要让我失望. 你还有其他我需要的东西吗？",
			new SetQuestAction(QUEST_SLOT, ""));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Oh, 我希望你能改变主意. 太让人失望了！", null);

		// players asks about the ingredients individually
		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("surgeonfish","cod", "char", "roach", "clownfish", "mackerel", "perch"),
			null,
			ConversationStates.QUEST_OFFERED,
			"在Faiumoni周围, 遍布着可以钓鱼的地方. 你可以找到适合你的很多地方, 在那里你能钓到不同的鱼. " +
			" 经过Ados时可以看看图书管, 你得到了什么配方吗？",
			null);

		// players asks about the ingredients individually
		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("韭菜", "洋葱"),
			null,
			ConversationStates.QUEST_OFFERED,
			"你在Fado能找到配料的一些成份. 所以你可以去取一些配料吗？",
			null);

		// players asks about the ingredients individually
		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("西红柿", "大蒜"),
			null,
			ConversationStates.QUEST_OFFERED,
			"在 Kalavan 市的 Sue, 那里有个漂亮的花园, 花园里出售 西红柿es 和 大蒜. "
			+ "所以你可以把配料取来吗？", null);
	}

	private void step_2() {
		// Fetch the ingredients and bring them back to Florence.
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Florence Boullabaisse");

		// player returns while quest is still active
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStartedCondition(QUEST_SLOT),
					new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.QUESTION_1,
			"欢迎回来! 我希望你能为鱼汤收集一些 #ingredients 配料, 或者是一些其他的东西 #everything.",
			null);

		// player asks what exactly is missing
		npc.add(ConversationStates.QUESTION_1, "ingredients",
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.QUESTION_1, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final List<String> needed = missingFood(player, true);
					npc.say("我还需要 "
							+ needed.size() +
									"ingredient" + ": "
							+ needed
							+ ". 你有带来吗?");
				}
			});

		// player says he has a required ingredient with him
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_1, "你带回什么了?", null);

		for(final String itemName : NEEDED_FOOD) {
			npc.add(ConversationStates.QUESTION_1, itemName, null,
				ConversationStates.QUESTION_1, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						List<String> missing = missingFood(player, false);

						if (missing.contains(itemName)) {
							if (player.drop(itemName)) {
								// register ingredient as done
								final String doneText = player.getQuest(QUEST_SLOT);
								player.setQuest(QUEST_SLOT, doneText + ";" + itemName);

								// check if the player has brought all Food
								missing = missingFood(player, true);

								if (!missing.isEmpty()) {
									npc.say("十分感谢! 你还带回其他的材料吗?");
								} else {
									player.addKarma(10.0);
									player.addXP(50);
									placeSoupFor(player);
									player.getStatusList().removeAll(PoisonStatus.class);
									npc.say("鱼汤已放在市场的桌子上了, 喝了它可以让你健康. "
											+ "我的密制鱼汤, 也能带给你一点好运. ");
									player.setQuest(QUEST_SLOT, "done;"
											+ System.currentTimeMillis());
									player.notifyWorldAboutChanges();
									npc.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("Oh 快点, 我没时间开玩笑！你没有带来 "
									+ itemName
									+ " , 是吧.");
							}
						} else {
							npc.say("你已经把材料给我了.");
						}
					}
				});
		}

		// Perhaps player wants to give all the ingredients at once
		npc.add(ConversationStates.QUESTION_1, "everything",
				null,
				ConversationStates.QUESTION_1,
				null,
				new ChatAction() {
			    @Override
				public void fire(final Player player, final Sentence sentence,
					   final EventRaiser npc) {
			    	checkForAllIngredients(player, npc);
			}
		});

		// player says something which isn't in the needed food list.
		npc.add(ConversationStates.QUESTION_1, "",
			new NotCondition(new TriggerInListCondition(NEEDED_FOOD)),
			ConversationStates.QUESTION_1,
			"I won't put that in your 鱼汤.", null);

		// allow to say goodbye while Florence is listening for food names
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.GOODBYE_MESSAGES, null,
				ConversationStates.IDLE, "Bye.", null);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.NO_MESSAGES,
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.ATTENDING,
			"I'm not sure what you want from me, then.", null);

		// player says he didn't bring any Food to different question
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES,
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.ATTENDING, "Okay then. Come back later.",
			null);
	}

	private void checkForAllIngredients(final Player player, final EventRaiser npc) {
		List<String> missing = missingFood(player, false);
		for (final String food : missing) {
		if (player.drop(food)) {
			// register ingredient as done
			final String doneText = player.getQuest(QUEST_SLOT);
			player.setQuest(QUEST_SLOT, doneText + ";"
			+ food);
			}
		}
		// check if the player has brought all Food
		missing = missingFood(player, true);
		if (missing.size() > 0) {
			npc.say("You didn't have all the ingredients I need. I still need "
							+ missing.size() +
									"ingredient" + ": "
							+ missing
							+ ". You'll get bad karma if you keep making mistakes like that!");
			// to fix bug [ 2517439 ]
			player.addKarma(-5.0);
			return;
		} else {
			// you get less XP if you did it the lazy way
			// and no karma
			player.addXP(30);
			placeSoupFor(player);
			player.getStatusList().removeAll(PoisonStatus.class);
			npc.say("The soup's on the market table for you, it will heal you. Tell me if I can help you with anything else.");
			player.setQuest(QUEST_SLOT, "done;"
					+ System.currentTimeMillis());
			player.notifyWorldAboutChanges();
			npc.setCurrentState(ConversationStates.ATTENDING);
		}
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"鱼汤",
				"Florence Boullabaisse makes a healthy and tasty 鱼汤, but it needs rather a lot of ingredients.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			if (!isCompleted(player)) {
				res.add("I'm collecting ingredients to make 鱼汤. I still need " + missingFood(player, false) + ".");
			} else if(isRepeatable(player)){
				res.add("Florence is ready to make soup for me again!");
			} else {
				res.add("I made some yummy 鱼汤 and Florence is now washing the dishes.");
			}
			return res;
	}

	@Override
	public String getName() {
		return "FishSoup";
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,REQUIRED_MINUTES)).fire(player, null, null);
	}

	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Florence Boullabaisse";
	}
}
