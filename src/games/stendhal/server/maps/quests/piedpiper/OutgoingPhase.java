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
package games.stendhal.server.maps.quests.piedpiper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.MultiZonesFixedPathsList;
import games.stendhal.server.core.pathfinder.RPZonePath;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.npc.ActorNPC;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class OutgoingPhase extends TPPQuest {
	private final SpeakerNPC piedpiper = new SpeakerNPC("彼德彼伯");
	private final SpeakerNPC mainNPC = TPPQuestHelperFunctions.getMainNPC();
	private final int minPhaseChangeTime;
	private int maxPhaseChangeTime;
	private List<List<RPZonePath>> fullpath =
		new LinkedList<List<RPZonePath>>();
	private LinkedList<Creature> rats;

	/**
	 * adding quest related npc's fsm states
	 */
	private void addConversations() {
		TPP_Phase myphase = OUTGOING;

		// Player asking about rats
		mainNPC.add(
				ConversationStates.ATTENDING,
				Arrays.asList("老鼠", "鼠灾"),
				new TPPQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				"鼠灾消除. "+
	    		"你们可以为自已的付出得到 #奖励 , 想了解详情请查看 #详情 ",
				null);

		// Player asking about details.
		mainNPC.add(
				ConversationStates.ATTENDING,
				"详情",
				new TPPQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				null,
				new DetailsKillingsAction());

		// Player asking about reward
		mainNPC.add(
				ConversationStates.ATTENDING,
				"奖励",
				new TPPQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				null,
				new RewardPlayerAction());
	}


	/**
	 * constructor
	 * @param timings
	 * - a pair of time parameters for phase timeout
	 */
	public OutgoingPhase(final Map<String, Integer> timings) {
		super(timings);
		minPhaseChangeTime = timings.get(OUTGOING_TIME_MIN);
		maxPhaseChangeTime = timings.get(OUTGOING_TIME_MAX);
		addConversations();
		rats=TPPQuestHelperFunctions.getRats();
	}

	@Override
	public void prepare() {
		rats.clear();
		createPiedPiper();
	}

	/**
	 * summon new rat, attracted by piper
	 */
	public void SummonRat() {
		final ActorNPC newCreature = new ActorNPC(false);

		// playing role of creature
		final Creature model = TPPQuestHelperFunctions.getRandomRat();
		newCreature.setRPClass("creature");
		newCreature.put("type", "creature");
		newCreature.put("title_type", "enemy");
		newCreature.setEntityClass(model.get("class"));
		newCreature.setEntitySubclass(model.get("subclass"));
		newCreature.setName("attracted "+model.getName());
		newCreature.setDescription(model.getDescription());

		// make actor follower of piper
		newCreature.setResistance(0);
		newCreature.setPosition(piedpiper.getX(), piedpiper.getY());
		piedpiper.getZone().add(newCreature);

		logger.debug("rat summoned");
	}

	/**
	 * class for adding a random rat to a chain
	 * when piper staying near house's door
	 */
	class AttractRat implements Observer {

		@Override
		public void update(Observable arg0, Object arg1) {
			SummonRat();
		}

	}

	/**
	 * class for processing rats' outgoing to catacombs
	 */
	class RoadsEnd implements Observer {

		final Observer o;

		@Override
		public void update(Observable arg0, Object arg1) {
			logger.debug("road's end.");
			o.update(null, null);

		}

		public RoadsEnd(Observer o) {
			this.o = o;
		}

	}

	/**
	 * prepare NPC to walk through his multizone path.
	 */
	private void leadNPC() {
		final StendhalRPZone zone = fullpath.get(0).get(0).get().first();
		final int x=fullpath.get(0).get(0).getPath().get(0).getX();
		final int y=fullpath.get(0).get(0).getPath().get(0).getY();
		piedpiper.setPosition(x, y);
		zone.add(piedpiper);
		Observer o = new MultiZonesFixedPathsList(
						piedpiper,
						fullpath,
						new AttractRat(),
						new RoadsEnd(
								new PhaseSwitcher(this)));
		o.update(null, null);
	}

	@Override
	public int getMinTimeOut() {
		return minPhaseChangeTime;
	}


	@Override
	public int getMaxTimeOut() {
		return maxPhaseChangeTime;
	}


	@Override
	public void phaseToDefaultPhase(List<String> comments) {
		destroyPiedPiper();
		super.phaseToDefaultPhase(comments);
	}


	@Override
	public void phaseToNextPhase(ITPPQuest nextPhase, List<String> comments) {
		destroyPiedPiper();
		super.phaseToNextPhase(nextPhase, comments);
	}


	/*
	 *  彼德彼伯 sent rats away:-)
	 */
	@Override
	public String getSwitchingToNextPhaseMessage() {
		final String text =
			"查玛斯市长 高喊: 感谢, 现在所有的 #老鼠 都被清除, " +
			"彼德彼伯 催眠了它们, 并把它们带出了地牢. "+
			"帮助过阿多斯城解决鼠灾的人们, "+
			"现在都能拿到 #奖励.";
		return text;
	}

	@Override
	public TPP_Phase getPhase() {
		return TPP_Phase.TPP_OUTGOING;
	}

	/**
	 * function for creating 彼德彼伯 npc
	 */
	private void createPiedPiper() {
		TPPQuestHelperFunctions.setupPiper(piedpiper);
		fullpath = PathsBuildHelper.getAdosCollectingRatsPaths();
		leadNPC();
	}

	/**
	 * function will remove piped piper npc object
	 */
	private void destroyPiedPiper() {
		piedpiper.getZone().remove(piedpiper);
	}

}

