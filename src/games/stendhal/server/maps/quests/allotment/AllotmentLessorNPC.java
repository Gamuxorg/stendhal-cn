package games.stendhal.server.maps.quests.allotment;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.GateKey;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestStateGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.TextHasNumberCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

/**
 * Builds an allotment lessor NPC for 塞门镇.
 *
 * @author kymara, filipe
 */
public class AllotmentLessorNPC implements ZoneConfigurator {
	private static String QUEST_SLOT = AllotmentUtilities.QUEST_SLOT;
	private AllotmentUtilities rentHelper;

	/**
	 * Configure a zone.
	 *
	 * @param zone The zone to be configured.
	 * @param attributes Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		rentHelper = AllotmentUtilities.get();
		buildNPC(zone);
	}

	/**
	 * Creates the NPC and sets the quest dialog
	 *
	 * @param zone The zone to be configured.
	 * @param attributes Configuration attributes.
	 */
	private void buildNPC(final StendhalRPZone zone) {
		// condition to check if there are any allotments available
		final ChatCondition hasAllotments = new ChatCondition() {
			@Override
			public boolean fire(Player player, Sentence sentence, Entity npc) {
				return rentHelper.getAvailableAllotments(zone.getName()).size() > 0;
			}
		};

		/**
		 * condition to check if the player already has an allotment rented
		 * note: this is used instead QuestActiveCondition because it relies on
		 * the time that the player speaks to the NPC
		 */
		final ChatCondition questActive = new ChatCondition() {
			@Override
			public boolean fire(Player player, Sentence sentence, Entity npc) {
				return new QuestStateGreaterThanCondition(QUEST_SLOT, 1, (int) System.currentTimeMillis()).fire(player,  sentence, npc);
			}
		};

		// create the new NPC
		final SpeakerNPC npc = new SpeakerNPC("Jef's_twin") {

			@Override
			protected void createPath() {
				/*final List<Node> nodes = new LinkedList<Node>();

				nodes.add(new Node(70, 19));
                nodes.add(new Node(86,19));
                nodes.add(new Node(86,3));
                nodes.add(new Node(87,3));
                nodes.add(new Node(87,19));
                nodes.add(new Node(106,19));
                nodes.add(new Node(106,3));
                nodes.add(new Node(107,3));
                nodes.add(new Node(107,19));
                nodes.add(new Node(69,19));

				setPath(new FixedPath(nodes, true));*/
				setPath(null);
			}

			@Override
			protected void createDialog() {
				// TODO: this was copy pasted change as needed
				addGreeting("唉呀!");
				addJob("嗯, 我不确信你的意思, 现在我在等我妈妈从 #商店 买东西回来. ");
				addHelp("我有一些关于市场那边的 #消息. ");
				addOffer("我不卖东西, 我只等妈妈回来. 如果你想听, 我可以告诉你一些 #消息 . ");
				// quest: FindJefsMom , quest sentence given there
				addReply("消息", "有一些商人马上要来市场开店了！这太棒了, 目前太多商铺空着, 太可惜了.");
				addReply("商店", "是的, 她必须去镇上. 我们种的花都在销往那里. 还有, 那边的市场有些 #消息...");
				addGoodbye("再会.");

				// if player already has one rented ask how may help
				add(ConversationStates.ATTENDING,
					Arrays.asList("rent", "租用","租房"),
					questActive,
					ConversationStates.QUEST_STARTED,
					"需要什么服务吗？把钥匙弄丢了？再租个新房？或是咨询房租到期的时间？",
					null);

				// if allotment not rented and there are available then ask if player wants to rent
				add(ConversationStates.ATTENDING,
					Arrays.asList("rent", "租用","租房"),
					new AndCondition(
							new NotCondition(questActive),
							hasAllotments),
					ConversationStates.QUEST_OFFERED,
					"你想租房吗?",
					null);

				// if allotment not rented and there are none available then tell player
				add(ConversationStates.ATTENDING,
					Arrays.asList("rent", "租用","租房"),
					new AndCondition(
							new NotCondition(questActive),
							new NotCondition(hasAllotments)),
					ConversationStates.ATTENDING,
					null,
					new ChatAction() {
						@Override
						public void fire(Player player, Sentence sentence, EventRaiser npc) {
							long diff = rentHelper.getNextExpiryTime(zone.getName()) - System.currentTimeMillis();

							npc.say("抱歉, 现在没有空房了, 请在 " + TimeUtil.approxTimeUntil((int) (diff / 1000L)) + " 时间后再来咨询.");
						}
				});

				// if offer rejected
				add(ConversationStates.QUEST_OFFERED,
					ConversationPhrases.NO_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"Ok, 还没别的需要服务的事吗?",
					new SetQuestAction(QUEST_SLOT, 1, "0"));

				// if accepts to rent allotment
				add(ConversationStates.QUEST_OFFERED,
					ConversationPhrases.YES_MESSAGES,
					null,
					ConversationStates.QUESTION_1,
					null,
					new ChatAction() {
						//say which ones are available
						@Override
						public void fire(Player player, Sentence sentence, EventRaiser npc) {
							List<String> allotments = rentHelper.getAvailableAllotments(zone.getName());
							String reply = allotments.toString();

							npc.say("你喜欢哪一间? 我看看... " +  "还有" + " "
									+ reply + " 是可以租的空房, 你再犹豫可能就 没了. ");
						}
					});

				// to exit renting/choosing an allotment
				add(ConversationStates.ANY,
					"none",
					null,
					ConversationStates.ATTENDING,
					"Ok.",
					null);

				// do business
				add(ConversationStates.QUESTION_1,
					"",
					new TextHasNumberCondition(),
					ConversationStates.ATTENDING,
					null,
					new ChatAction() {
						// does the transaction if possible
						@Override
						public void fire(Player player, Sentence sentence, EventRaiser npc) {
							final int number = sentence.getNumeral().getAmount();
							final String allotmentNumber = Integer.toString(number);

							//TODO: get payment
							if (!rentHelper.isValidAllotment(zone.getName(), allotmentNumber)) {
								npc.say("恐怕你选的房间不存在. ");
							} else {
								if (rentHelper.getAvailableAllotments(zone.getName()).contains(allotmentNumber)) {
									if(rentHelper.setExpirationTime(zone.getName(), allotmentNumber, player.getName())) {
										npc.say("这是你的 " + allotmentNumber + " 号房间的钥匙. 以后就使用房间至 "
												+ TimeUtil.approxTimeUntil((int) (AllotmentUtilities.RENTAL_TIME / 1000L)) + " 的时间.");

										if (!player.equipToInventoryOnly(rentHelper.getKey(zone.getName(), player.getName()))) {
											npc.say("Oh, 你好像放的东西太多了, 在你回来前, 我会保证东西的安全. 只用问下你 #租用 的房间.");
										}

										new SetQuestAction(QUEST_SLOT, 1, Long.toString(AllotmentUtilities.RENTAL_TIME + System.currentTimeMillis())).fire(player, sentence, npc);
									} else {
										// error? shouldn't happen
										npc.say("Uh oh! 有些你的记录有些问题, 请再等一会. ");
									}
								} else {
									npc.say("抱歉, 你想租用的房子的已被占用.");
								}
							}
						}
					});

				// if player asked about key
				add(ConversationStates.QUEST_STARTED,
					"key",
					null,
					ConversationStates.ATTENDING,
					"",
					// gives player a new key to give to friends to use
					new ChatAction() {
						@Override
						public void fire(Player player, Sentence sentence, EventRaiser npc) {
							GateKey key = rentHelper.getKey(zone.getName(), player.getName());

							if (key != null) {
								if (player.equipToInventoryOnly(key)) {
									npc.say("这是你的钥匙, 欢迎入住.");
								} else {
									npc.say("在你能带更多钥匙多前, 你不能再拿走这把了. ");
								}
							} else {
								npc.say("文件太多不好整理, 它提示你没有租用过房子.");
							}
						}
					});

				// if player asked about remaining time
				add(ConversationStates.QUEST_STARTED,
					"time",
					null,
					ConversationStates.ATTENDING,
					null,
					// gets a new key
					new ChatAction() {
						@Override
						public void fire(Player player, Sentence sentence, EventRaiser npc) {
							npc.say("你租房剩余时间还有 " + rentHelper.getTimeLeftPlayer(zone.getName(), player.getName()) + ".");
						}
					});

			}
		};

		//TODO: also copy-pasted change as needed
		npc.setEntityClass("kid6npc");
		npc.setPosition(85, 11);
		npc.initHP(100);
		npc.setDescription("你遇见了 jefs 的双胞胎兄弟. 他好像在等某个人.");
		zone.add(npc);
	}
}
