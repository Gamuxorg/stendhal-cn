/***************************************************************************
 *                   (C) Copyright 2003-2016 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.temple;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

public class TelepathNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSemosTempleArea(zone);
	}

	private void buildSemosTempleArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("艾欧弗鲁托") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(8, 19));
				nodes.add(new Node(8, 20));
				nodes.add(new Node(15, 20));
				nodes.add(new Node(15, 19));
				nodes.add(new Node(16, 19));
				nodes.add(new Node(16, 14));
				nodes.add(new Node(15, 14));
				nodes.add(new Node(15, 13));
				nodes.add(new Node(12, 13));
				nodes.add(new Node(8, 13));
				nodes.add(new Node(8, 14));
				nodes.add(new Node(7, 14));
				nodes.add(new Node(7, 19));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {

				// player has met io before and has a pk skull
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestStartedCondition("meet_io"),
								new ChatCondition() {
									@Override
									public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
										return player.isBadBoy() ;
									}
								}),
				        ConversationStates.QUESTION_1,
				        null,
				        new SayTextAction("又见面了, [name]. 我感觉你身上印着杀人犯的标记, 希望洗掉这恶名吗?"));

				// player has met io before and has not got a pk skull
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestStartedCondition("meet_io"),
								new ChatCondition() {
									@Override
									public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
										return !player.isBadBoy() ;
									}
								}),
				        ConversationStates.ATTENDING,
				        null,
				        new SayTextAction("又见面了, [name]. 这次需要我 #帮忙 #help 做些什么? 不是我不知道..."));

				// first meeting with player
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestNotStartedCondition("meet_io")),
						ConversationStates.ATTENDING,
				        null,
				        new MultipleActions(
				        		new SayTextAction("我等着你, [name]. 我怎么知道你的名字？这简单, 我是 艾欧弗鲁托, 感知者. 你还要我展示六种基本元素的通灵术吗？"),
				        		new SetQuestAction("meet_io", "start")));

				add(ConversationStates.QUESTION_1, ConversationPhrases.YES_MESSAGES, null, ConversationStates.ATTENDING,
				        null, new ChatAction() {

					        @Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						       	if ((player.getLastPVPActionTime() > System.currentTimeMillis()
											- 2 * MathHelper.MILLISECONDS_IN_ONE_WEEK)) {
									// player attacked another within the last two weeks
									long timeRemaining = player.getLastPVPActionTime() - System.currentTimeMillis()
										+ 2 * MathHelper.MILLISECONDS_IN_ONE_WEEK;
									raiser.say("你要克制自已, 两周内不能攻击他人. 所以在 " + TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + " 后回到这里, 并记住, 我知道你的一切歪脑筋!");
								} else if (player.getKarma() < 5) {
									// player does not have much good karma
									raiser.say("话说, 有因必有果, 你积的功德越多, 运气也越好. 对你来说, 此生行善越多, 你的命运才能好转.");
								} else {
									// player has fulfilled all requirements to be rehabilitated
									raiser.say("你真的为你的所做所为忏悔了吗?");
									raiser.setCurrentState(ConversationStates.QUESTION_2);
								}
							}
					    }
				);
				// player didn't want pk icon removed, offer other help
				add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES, null, ConversationStates.ATTENDING, "不错! 如果你需要, 我可以用其他方法 #帮助 #help 你", null);
				// player satisfied the pk removal requirements and said yes they were sorry
				add(ConversationStates.QUESTION_2, ConversationPhrases.YES_MESSAGES, null, ConversationStates.ATTENDING,
				        "好, 我知道你是.", new ChatAction() {

					        @Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
								player.rehabilitate();
							} });
				// player said no they are not really sorry
				add(ConversationStates.QUESTION_2, ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE, "I thought not! Good bye!", null);
				addJob("我被赋予驾驭人类思想的全部能力. 我在传心术方面有很大的提升；尽管如此, 我还不能预测未来. 所以还不知道是否我们有能力打败 Blordrough 的黑暗军团...");
				addQuest("好吧, 现在需要为我做的事真的不太多. 而且我... 嗨! 你刚才在试着读我的思想吗？做这事之前, 你应该问问我吧！");
				addGoodbye();
				// further behaviour is defined in the MeetIo quest.
			}
		};

		npc.setEntityClass("floattingladynpc");
		npc.setDescription("你遇见了 Flotto. 她很了解你");
		npc.setPosition(8, 19);
		npc.initHP(100);
		zone.add(npc);
	}
}
