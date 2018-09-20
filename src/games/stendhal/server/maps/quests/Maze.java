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
import java.util.List;

import games.stendhal.common.Direction;
import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.maze.MazeGenerator;
import games.stendhal.server.maps.quests.maze.MazeSign;

public class Maze extends AbstractQuest {
	/** Minimum time between repeats. */
	private static final int COOLING_TIME = MathHelper.MINUTES_IN_ONE_HOUR * 24;
	private MazeSign sign;
	private MazeGenerator maze = null;

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"迷宫",
				"海震的迷宫对于寻路者是个很大的挑战.",
				false);
		addMazeSign();
		setupConversation();
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(getSlotName())) {
				return res;
			}
			res.add("海震建立了一个巨型迷宫让我去解开.");

			if (player.getZone().getName().endsWith("_maze")) {
				res.add("我落入的迷宫中的圈套.");
			} else {
				if (!isCompleted(player)) {
					res.add("最终我没有解开迷宫.");
				} else {
					res.add("我解开了迷宫!");
				}
				if (isRepeatable(player)) {
					res.add("现在我可以再一次尝试试解开迷宫.");
				} else {
					res.add("海震还没有为我做出新迷宫.");
				}
			}
			final int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
			if (repetitions > 1) {
				res.add("我解开迷宫已经用了 " + repetitions + " ,这么久的时间!");
			}

			return res;
	}

	@Override
	public String getName() {
		return "迷宫";
	}

	@Override
	public String getSlotName() {
		return "迷宫";
	}

	@Override
	public boolean isRepeatable(Player player) {
		return new TimePassedCondition(getSlotName(), 1, COOLING_TIME).fire(player, null, null);
	}

	private SpeakerNPC getNPC() {
		return npcs.get("海震");
	}

	private void addMazeSign() {
		setSign(new MazeSign());
		getSign().setPosition(10, 7);
		getNPC().getZone().add(getSign());
	}

	private void setupConversation() {
		SpeakerNPC npc = getNPC();

		npc.addQuest("我可以把你送入 #迷宫 ,你需要自已找到出口. 我在桌子上那本 蓝色的书 上记下了用时最快和解题最多的解题人.");

		npc.add(ConversationStates.ATTENDING,
				"迷宫",
				new TimePassedCondition(getSlotName(), 1, COOLING_TIME),
				ConversationStates.QUEST_OFFERED,
				"迷宫中会在对角有一个传送口可以出来. 我也在另外两个角落放上卷轴, 如果你够快就能得到. 你想试试吗?",
				null);

		npc.add(ConversationStates.ATTENDING,
				"迷宫",
				new NotCondition(new TimePassedCondition(getSlotName(), 1, COOLING_TIME)),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(getSlotName(), 1,
						COOLING_TIME, "一天我只能提供一次解迷机会. 你可以改天再试"));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				null,
				new SendToMazeChatAction());

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"OK. 看来你只能选择放弃了.",
				null);
	}

	private void setSign(MazeSign sign) {
		this.sign = sign;
	}

	public MazeSign getSign() {
		return sign;
	}

	private class SendToMazeChatAction implements ChatAction {
		public SendToMazeChatAction() {
			// empty constructor to prevent warning
		}

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
			maze = new MazeGenerator(player.getName() + "_maze", 128, 128);
			maze.setReturnLocation(player.getZone().getName(), player.getX(), player.getY());
			maze.setSign(getSign());
			StendhalRPZone zone = maze.getZone();
			SingletonRepository.getRPWorld().addRPZone(zone);
			new SetQuestAction(getSlotName(), 0, "start").fire(player, sentence, raiser);
			new SetQuestToTimeStampAction(getSlotName(), 1).fire(player, sentence, raiser);
			maze.startTiming();
			player.teleport(zone, maze.getStartPosition().x, maze.getStartPosition().y, Direction.DOWN, player);
		}
	}

	/**
	 * Access the portal from MazeTest.
	 *
	 * @return return portal from the maze
	 */
	protected Portal getPortal() {
		return maze.getPortal();
	}

	@Override
	public String getNPCName() {
		return "海震";
	}

	@Override
	public String getRegion() {
		return Region.ADOS_SURROUNDS;
	}
}
