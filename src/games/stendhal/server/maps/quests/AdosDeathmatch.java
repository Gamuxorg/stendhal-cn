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

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.Direction;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.Spot;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.TeleportAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasPetOrSheepCondition;
import games.stendhal.server.entity.npc.condition.PlayerInAreaCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.deathmatch.BailAction;
import games.stendhal.server.maps.deathmatch.DeathmatchInfo;
import games.stendhal.server.maps.deathmatch.DoneAction;
import games.stendhal.server.maps.deathmatch.LeaveAction;
import games.stendhal.server.maps.deathmatch.StartAction;
import games.stendhal.server.util.Area;

/**
 * Creates the Ados Deathmatch Game.
 */
public class AdosDeathmatch extends AbstractQuest {

		/** the logger instance. */
	private static final Logger logger = Logger.getLogger(AdosDeathmatch.class);

	private StendhalRPZone zone;

	private static Area arena;

	private DeathmatchInfo deathmatchInfo;

	public AdosDeathmatch() {
	    // constructor for quest system
	    logger.debug("little constructor for quest system", new Throwable());
	}

	@Override
	public String getSlotName() {
		return "adosdeathmatch";
	}

	public AdosDeathmatch(final StendhalRPZone zone, final Area area) {
		this.zone = zone;
		arena = area;
		logger.debug("big constructor for zone", new Throwable());
		final Spot entrance = new Spot(zone, 96, 75);
		deathmatchInfo = new DeathmatchInfo(arena, zone, entrance);
		// do not let players scroll out of deathmatch
		Rectangle r = area.getShape().getBounds();
		zone.disallowOut(r.x, r.y, r.width, r.height);
	}

	/**
	 * Shows the player the potential trophy.
	 *
	 * @param x
	 *            x-position of helmet
	 * @param y
	 *            y-position of helmet
	 */
	public void createHelmet(final int x, final int y) {
		final Item helmet = SingletonRepository.getEntityManager()
				.getItem("trophy helmet");
		helmet.setDescription("这是给本次死亡大赛优胜者郑重的奖励, 对通过死亡大赛的每位选手增加一点防御！");
		helmet.setPosition(x, y);
		zone.add(helmet, false);
	}

	/**
	 * Create the Deathmatch assistant.
	 *
	 * @param name name of the assistant
	 * @param x x coordinate of the assistant
	 * @param y y coordinate of the assistant
	 */
	public void createNPC(final String name, final int x, final int y) {

		// We create an NPC
		final SpeakerNPC npc = new SpeakerNPC(name) {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {

				// player is outside the fence. after 'hi' use ConversationStates.INFORMATION_1 only.
				add(
						ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(name),
								new NotCondition(new PlayerInAreaCondition(arena))),
						ConversationStates.INFORMATION_1,
						"欢迎来到 Ados Deathmatch! 如果你想加入, 请和 #Thonatus 谈.",
						null);
				add(
						ConversationStates.INFORMATION_1,
						"Thonatus",
						null,
						ConversationStates.INFORMATION_1,
						"Thonatus 作为死亡比赛 #Deathmatch 官方招募人. 他在 Ados 西南的 #沼泽 里 #swamp ",
						null);

                add(
					ConversationStates.INFORMATION_1,
					Arrays.asList("沼泽", "swamp"),
					null,
					ConversationStates.INFORMATION_1,
					"是的,这里的西南方向, 就像我说的, 要小心, 沼泽地聚集了一些邪恶的生物.",
					null);


				add(
					ConversationStates.INFORMATION_1,
					"死记录大赛",
					null,
					ConversationStates.INFORMATION_1,
					"如果你接受了 #Thonatus 所讲的比赛规则 #challenge , 你会到达那里, 强太的敌人会包围你, 你必须全部杀死他们才能宣布胜利 #victory.",
					null);

                add( 
                    ConversationStates.INFORMATION_1,
                    Arrays.asList("比赛", "挑战","challenge"),
                    null,
                    ConversationStates.INFORMATION_1,
                    "记住在 #死亡大赛 中的名字. 除非你觉得你有很高的防御才能不接受挑战. 并且一定要检查里面没有任何精英战士, 与强大的野兽搏斗！ And be sure to check that there is not any elite warrior already inside, battling strong beasts!",
                    null);

				add(
                    ConversationStates.INFORMATION_1,
                    Arrays.asList("胜利", "取胜", "victory"),
                    null,
                    ConversationStates.INFORMATION_1,
                    "奖品就是你所见的这种头盔, 它的防御会随着你在死亡大赛中撑过的回合提升最大值. ",
                    null);

				// player is inside
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(name),
								new PlayerInAreaCondition(arena)),
						ConversationStates.ATTENDING,
						"欢迎来到 Ados 死亡大赛！你需要 #帮助吗 #help ?", null);
				addJob("我是本次 ＃死亡大赛 的助理, 如果想参赛并需要 #帮助 的话就告诉我. ");
				addHelp("当你准备好时对我说 #开始 '#start' ！要杀死 #所有 #出现 的对手. 如果你坚持到最后, 高喊 '#胜利 #victory' 取得最终 #胜利 .");
				addGoodbye("祝你在死亡大赛中玩的开心！");

				add(
						ConversationStates.ATTENDING,
						Arrays.asList("所有", "出现", "死亡大赛"),
						ConversationStates.ATTENDING,
						"每一轮你会面对更强大的敌人. 做好防御, 杀死他们, 或者想放弃比赛就对我说 #认输 #投降 #bail!",
						null);
				add(
						ConversationStates.ATTENDING,
						Arrays.asList("头盔", "helm", "helmet"),
						ConversationStates.ATTENDING,
						"如果你在死亡大赛中取胜, 我们会奖励给你一件战利品头盔 helmet. 每次 #胜利 都会给这个头盔增加强度. ",
						null);

				// 'start' command will start spawning creatures
				add(ConversationStates.ATTENDING, Arrays.asList("start", "go",
						"开始"), null, ConversationStates.IDLE, null,
						new StartAction(deathmatchInfo));

				// 'victory' command will scan, if all creatures are killed and
				// reward the player
				add(ConversationStates.ATTENDING, Arrays.asList("victory",
						"done", "胜利"), null, ConversationStates.ATTENDING,
						null, new DoneAction());

				// 'leave' command will send the victorious player home
				add(ConversationStates.ATTENDING, Arrays
						.asList("leave", "home"), null,
						ConversationStates.ATTENDING, null, new LeaveAction());

				// 'bail' command will teleport the player out of it
				add(ConversationStates.ANY, Arrays.asList("bail", "认输",
						"投降", "exit"), null, ConversationStates.ATTENDING,
						null, new BailAction());
			}
		};

		npc.setEntityClass("darkwizardnpc");
		npc.setPosition(x, y);
		npc.setDescription("你遇见了 Thanatos. 与这个死亡大赛参赛队员相比, 他还是比较强壮的.");
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		// The assistant is near the spikes, so give him better ears for the
		// safety of the players
		npc.setPerceptionRange(7);
		zone.add(npc);
	}


	static class DeathMatchEmptyCondition implements ChatCondition {
		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
			final List<Player> dmplayers = arena.getPlayers();
			return dmplayers.size() == 0;
		}
	}

	private void recruiterInformation() {
		final SpeakerNPC npc2 = npcs.get("Thonatus");

		npc2.add(ConversationStates.ATTENDING, Arrays.asList("heroes", "who", "hero", "status"),
				 new NotCondition(new DeathMatchEmptyCondition()), ConversationStates.ATTENDING,
				 null,
				 new ChatAction() {
					 @Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						 final List<Player> dmplayers = arena.getPlayers();
						 final List<String> dmplayernames = new LinkedList<String>();
						 for (Player dmplayer : dmplayers) {
							 dmplayernames.add(dmplayer.getName());
						 }
						 // List the players inside deathmatch
						 npc.say("本次死亡大赛中的英雄们正在战斗中, 如果你想加入 "
								 + dmplayernames + " 的队伍中, 然后才可 #challenge.");
					 }
				 });

		npc2.add(ConversationStates.ATTENDING, Arrays.asList("heroes", "who", "hero", "status") , new DeathMatchEmptyCondition(),
				 ConversationStates.ATTENDING,
				 "你是一个英雄吗？如果你想加入本次死亡比赛, 请接受 ＃挑战 #challenge . ", null);

		npc2.add(ConversationStates.ATTENDING, Arrays.asList("比赛", "挑战","challenge"),
				 new AndCondition(new LevelGreaterThanCondition(19),
						  new DeathMatchEmptyCondition(),
						  new NotCondition(new PlayerHasPetOrSheepCondition())),
				 ConversationStates.IDLE, null,
				 new TeleportAction("0_阿多斯_城墙_n", 100, 86, Direction.DOWN));


		npc2.add(ConversationStates.ATTENDING, Arrays.asList("比赛", "挑战","challenge"),
			 new AndCondition(new LevelGreaterThanCondition(19),
					  new NotCondition(new DeathMatchEmptyCondition()),
					  new NotCondition(new PlayerHasPetOrSheepCondition())),
				 ConversationStates.QUESTION_1, null,
				 new ChatAction() {
					 @Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						 final List<Player> dmplayers = arena.getPlayers();
						 final List<String> dmplayernames = new LinkedList<String>();
						 for (Player dmplayer : dmplayers) {
							 dmplayernames.add(dmplayer.getName());
						 }
						 // List the players inside deathmatch
						 npc.say("这次的死亡比赛中正有英雄在努力战斗中, 所以现在比较危险, 你要加入到 "
								 + dmplayernames + " 的战场吗?");
					 }
				 });

		npc2.add(ConversationStates.ATTENDING, Arrays.asList("比赛", "挑战","challenge"),
			 new AndCondition(new LevelGreaterThanCondition(19),
					  new PlayerHasPetOrSheepCondition()),
			 ConversationStates.ATTENDING, "抱歉, 你带的宠物可能会在战场中受惊.",
				 null);


		npc2.add(ConversationStates.QUESTION_1, ConversationPhrases.YES_MESSAGES, null,
				 ConversationStates.IDLE, null,
				 new TeleportAction("0_阿多斯_城墙_n", 100, 86, Direction.DOWN));


		npc2.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES, null,
				 ConversationStates.ATTENDING, "有点挤, 但没办法, 如果还有别的事, 尽管说.",
				 null);

		npc2.add(ConversationStates.ATTENDING, Arrays.asList("比赛", "挑战","challenge"),
				 new LevelLessThanCondition(20),
				 ConversationStates.ATTENDING, "抱歉, 我现在太弱, 还不适合参加 #死亡大赛, 等你20级后再回来吧",
				 null);
	}



	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Ados 死亡大赛",
				"Thanatos 在死亡大赛中负责查找投入战斗的英雄们. ",
				true);
		recruiterInformation();
	}
	@Override
	public String getName() {
		return "AdosDeathmatch";
	}
	@Override
	public int getMinLevel() {
		return 20;
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
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Thonatus";
	}
}
