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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import games.stendhal.common.Direction;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.BehaviourAction;
import games.stendhal.server.entity.npc.behaviour.impl.Behaviour;
import games.stendhal.server.entity.player.Player;

public class Blackjack extends AbstractQuest {
	// spades ♠
	 private static final String SPADES = "\u2660";

	// hearts ♥
	private static final String HEARTS = "\u2665";

	// diamonds ♦
	private static final String DIAMONDS = "\u2666";

	// clubs ♣
	private static final String CLUBS = "\u2663";

	private static final int CHAT_TIMEOUT = 60;

	private static final int MIN_STAKE = 10;

	private static final int MAX_STAKE = 400;

	private int stake;

	private boolean bankStands;

	private boolean playerStands;

	private Map<String, Integer> cardValues;

	private Stack<String> deck;

	private final List<String> playerCards = new LinkedList<String>();

	private final List<String> bankCards = new LinkedList<String>();

	private StackableItem playerCardsItem;

	private StackableItem bankCardsItem;

	private SpeakerNPC ramon;

	private final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(
			"-1_athor_ship_w2");

	private void startNewGame(final Player player) {
		cleanUpTable();
		playerCards.clear();
		bankCards.clear();
		playerCardsItem = (StackableItem) SingletonRepository.getEntityManager().getItem("扑克");
		zone.add(playerCardsItem);
		playerCardsItem.setPosition(25, 38);
		bankCardsItem = (StackableItem) SingletonRepository.getEntityManager().getItem("扑克");
		bankCardsItem.setPosition(27, 38);
		zone.add(bankCardsItem);

		playerStands = false;
		bankStands = false;
		// Before each game, we put all cards back on the deck and
		// shuffle it.
		// We could change that later so that the player can
		// try to remember what's still on the deck
		deck = new Stack<String>();
		for (final String card : cardValues.keySet()) {
			deck.add(card);
		}
		Collections.shuffle(deck);

		dealCards(player, 2);
	}

	private void cleanUpTable() {
		if (playerCardsItem != null) {
			zone.remove(playerCardsItem);
			playerCardsItem = null;
		}
		if (bankCardsItem != null) {
			zone.remove(bankCardsItem);
			bankCardsItem = null;
		}
	}

	private int countAces(final List<String> cards) {
		int count = 0;
		for (final String card : cards) {
			if (card.startsWith("A")) {
				count++;
			}
		}
		return count;
	}

	private int sumValues(final List<String> cards) {
		int sum = 0;
		for (final String card : cards) {
			sum += cardValues.get(card).intValue();
		}
		int numberOfAces = countAces(cards);
		while ((sum > 21) && (numberOfAces > 0)) {
			sum -= 10;
			numberOfAces--;
		}
		return sum;
	}

	private boolean isBlackjack(final List<String> cards) {
		return (sumValues(cards) == 21) && (cards.size() == 2);
	}

	/**
	 * Deals <i>number</i> cards to the player, if the player is not standing,
	 * and to the bank, if the bank is not standing.
	 * @param rpEntity
	 *
	 * @param number
	 *            The number of cards that each player should draw.
	 */
	private void dealCards(final RPEntity rpEntity, final int number) {
		StringBuilder messagebuf = new StringBuilder();
		messagebuf.append('\n');
		int playerSum = sumValues(playerCards);
		int bankSum = sumValues(bankCards);
		for (int i = 0; i < number; i++) {
			if (!playerStands) {
				final String playerCard = deck.pop();
				playerCards.add(playerCard);
				messagebuf.append("你得到了 " + playerCard + ".\n");
			}

			if (playerStands && (playerSum < bankSum)) {
				messagebuf.append("庄家 胜.\n");
				bankStands = true;
			}
			if (!bankStands) {
				final String bankCard = deck.pop();
				bankCards.add(bankCard);
				messagebuf.append("庄家得到一张 " + bankCard + ".\n");
			}
			playerSum = sumValues(playerCards);
			bankSum = sumValues(bankCards);
		}
		playerCardsItem.setQuantity(playerSum);
		playerCardsItem.setDescription("你看了玩家的牌: "
				+ playerCards);
		playerCardsItem.notifyWorldAboutChanges();
		bankCardsItem.setQuantity(bankSum);
		bankCardsItem.setDescription("你看了庄家的牌: "
				+ bankCards);
		bankCardsItem.notifyWorldAboutChanges();
		if (!playerStands) {
			messagebuf.append("你有 " + playerSum + "点. \n");
			if (playerSum == 21) {
				playerStands = true;
			}
		}
		if (!bankStands) {
			messagebuf.append("庄家有 " + bankSum + "点. \n");
			if ((bankSum >= 17) && (bankSum <= 21) && (bankSum >= playerSum)) {
				bankStands = true;
				messagebuf.append("庄家胜. \n");
			}
		}
		final String message2 = analyze(rpEntity);
		if (message2 != null) {
			messagebuf.append(message2);
		}
		ramon.say(messagebuf.toString());
	}

	/**
	 * @param rpEntity
	 * @return The text that the dealer should say, or null if he shouldn't say
	 *         anything.
	 */
	private String analyze(final RPEntity rpEntity) {
		final int playerSum = sumValues(playerCards);
		final int bankSum = sumValues(bankCards);
		String message = null;
		if (isBlackjack(bankCards) && isBlackjack(playerCards)) {
			message = "你有一张 二十一点, 但庄家也有一个. It's a push. ";
			message += payOff(rpEntity, 1);
		} else if (isBlackjack(bankCards)) {
			message = "庄家有一张 二十一点. 祝下次好运!";
		} else if (isBlackjack(playerCards)) {
			message = "你有一张 二十一点! 恭喜! ";
			message += payOff(rpEntity, 3);
		} else if (playerSum > 21) {
			if (bankSum > 21) {
				message = "双方出局Both have busted!平局 This is a draw. ";
				message += payOff(rpEntity, 1);
			} else {
				message = "你已破产! 下次好运!";
			}
		} else if (bankSum > 21) {
			message = "庄家破产！恭喜！";
			message += payOff(rpEntity, 2);
		} else {
			if (!playerStands) {
				message = "你想再发一张牌？";
				ramon.setCurrentState(ConversationStates.QUESTION_1);
			} else if (!bankStands) {
				letBankDrawAfterPause(ramon.getAttending().getName());
			} else if (bankSum > playerSum) {
				message = "庄家胜, 祝下次好运!";
			} else if (bankSum == playerSum) {
				message = "平局";
				message += payOff(rpEntity, 1);
			} else {
				message = "你赢了. 恭喜！";
				message += payOff(rpEntity, 2);
			}
		}
		return message;
	}

	private void letBankDrawAfterPause(final String playerName) {
		SingletonRepository.getTurnNotifier().notifyInSeconds(1, new TurnListener() {
			private final String name = playerName;

			@Override
			public void onTurnReached(final int currentTurn) {
				if (name.equals(ramon.getAttending().getName())) {
					dealCards(ramon.getAttending(), 1);
				}

			}

		});
	}

	/**
	 * Gives the player <i>factor</i> times his stake.
	 *
	 * @param rpEntity
	 *            The player.
	 * @param factor
	 *            The multiplier. 1 for draw, 2 for win, 3 for win with
	 *            二十一点.
	 * @return A message that the NPC should say to inform the player.
	 */
	private String payOff(final RPEntity rpEntity, final int factor) {
		final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		money.setQuantity(factor * stake);
		rpEntity.equipOrPutOnGround(money);
		if (factor == 1) {
			return "取回你的筹码. ";
		} else {
			return "你的筹码, 增加 " + (factor - 1) * stake
					+ " 筹码. ";
		}
	}

	@Override
	// @SuppressWarnings("unchecked")
	public void addToWorld() {

		// TODO: move ramon into his own NPC file.
		ramon = new SpeakerNPC("Ramon") {
			@Override
			protected void createPath() {
				// Ramon doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {

				addGreeting("欢迎来到 #二十一点 牌桌! 在船出发之前, 你可以在这里玩玩 #play 牌打发时间. ");
				addJob("我是塞门镇酒吧的发牌手, 但我没有参与打牌的权力, 但我哥哥里卡多也在这个酒吧工作. ");
				addReply(
						"二十一点",
						"二十一点 玩法简单, 不懂可以在墙上读读规则说明. .");
				addHelp("不要太专注于玩牌而赶不上船, 注意听通知");
				addGoodbye("再见!");
			}

			@Override
			protected void onGoodbye(final RPEntity player) {
				// remove the cards when the player stops playing.
				cleanUpTable();
			}
		};

		ramon.setEntityClass("naughtyteen2npc");
		ramon.setPosition(26, 36);
		ramon.setDescription("Ramon 想和你玩几局 二十一点. 你想给他个机会吗?");
		ramon.setDirection(Direction.DOWN);
		ramon.initHP(100);
		zone.add(ramon);

		cardValues = new HashMap<String, Integer>();
		final String[] colors = { CLUBS,
				DIAMONDS,
				HEARTS,
				SPADES };
		final String[] pictures = { "J", "Q", "K" };
		for (final String color : colors) {
			for (int i = 2; i <= 10; i++) {
				cardValues.put(i + color, Integer.valueOf(i));
			}
			for (final String picture : pictures) {
				cardValues.put(picture + color, Integer.valueOf(10));
			}
			// ace values can change to 1 during the game
			cardValues.put("A" + color, Integer.valueOf(11));
		}

		// increase the timeout, as otherwise the player often
		// would use their stake because of reacting too slow.

		ramon.setPlayerChatTimeout(CHAT_TIMEOUT);

		ramon.add(ConversationStates.ATTENDING, "play", null,
				ConversationStates.ATTENDING,
				"要参与游戏, 你要压至少 #'stake " + MIN_STAKE
						+ "' 最多 #'stake " + MAX_STAKE
						+ "' 的金子. 所以, 你想压多少？?", null);

		ramon.add(ConversationStates.ATTENDING, "stake", null,
				ConversationStates.ATTENDING, null,
				new BehaviourAction(new Behaviour(), "stake", "offer") {
					@Override
					public void fireSentenceError(Player player, Sentence sentence, EventRaiser npc) {
			        	npc.say(sentence.getErrorString() + " 只用告诉我你想压多少,如比 #'stake 50'.");
					}

					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						if (sentence.hasError()) {
							fireSentenceError(player, sentence, npc);
						} else {
							ItemParserResult res = behaviour.parse(sentence);

							// don't use res.wasFound() and avoid to call fireRequestError()
							fireRequestOK(res, player, sentence, npc);
						}
					}

					@Override
					public void fireRequestOK(final ItemParserResult res, Player player, Sentence sentence, EventRaiser npc) {
						stake = res.getAmount();

						if (stake < MIN_STAKE) {
							npc.say("你必须最少压 " + MIN_STAKE + " 的金子");
						} else if (stake > MAX_STAKE) {
							npc.say("你不能压多少 " + MAX_STAKE + " 的金子. ");
						} else if (player.drop("money", stake)) {
							startNewGame(player);
						} else {
							npc.say("Hey! 你钱不够了!");
						}
					}
				});

		ramon.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING, null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						dealCards(player, 1);
					}
				});

		// The player says he doesn't want to have another card.
		// Let the dealer give cards to the bank.
		ramon.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						playerStands = true;
						if (bankStands) {
							// Both stand. Let the dealer tell the final resul
							final String message = analyze(player);
							if (message != null) {
								ramon.say(message);
							}
						} else {
							letBankDrawAfterPause(player.getName());
						}
					}
				});

		fillQuestInfo(
				"二十一点",
				"不你在Athor码头等船时, 可以玩几局Blackjack（二十一点）打发时间.",
				true);
	}

	@Override
	public String getSlotName() {
		return "二十一点";
	}

	@Override
	public String getName() {
		return "二十一点";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public boolean isVisibleOnQuestStatus() {
		return false;
	}

	@Override
	public List<String> getHistory(final Player player) {
		return new ArrayList<String>();
	}

	@Override
	public String getNPCName() {
		return "Ramon";
	}
}
