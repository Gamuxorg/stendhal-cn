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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import games.stendhal.common.Rand;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

public class InvasionPhase extends TPPQuest {

	private final int minPhaseChangeTime;
	private final int maxPhaseChangeTime;
	protected LinkedList<Creature> rats = new LinkedList<Creature>();

	private void addConversations(final SpeakerNPC mainNPC) {
		TPP_Phase myphase = INVASION;

		// Player asking about rats at invasion time.
		mainNPC.add(
				ConversationStates.ATTENDING,
				Arrays.asList("老鼠", "鼠灾"),
				new TPPQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
						npc.say("那里 " + TPPQuestHelperFunctions.getRats().size() +
								" 仍然有 "+Integer.toString(TPPQuestHelperFunctions.getRats().size())+
								" 老鼠存活.");
					}
				});

		//Player asked about details at invasion time.
		mainNPC.add(
				ConversationStates.ATTENDING,
				"详情",
				new TPPQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				"阿多斯 被老鼠要被占领了! "+
				  "在所有老鼠消灭之前,"+
				  "我现在不想给你回报或解释什么" ,
				null);

		// Player asked about reward at invasion time.
		mainNPC.add(
				ConversationStates.ATTENDING,
				"奖励",
				new TPPQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				"阿多斯 被老鼠要被占领了! "+
				  "在所有老鼠消灭之前,"+
				  "我现在不想给你回报或解释什么" ,
				null);
	}

	/**
	 * Create InvasionPhase.
	 *
	 * @param timings
	 */
	public InvasionPhase(Map<String, Integer> timings) {
		super(timings);
		minPhaseChangeTime = timings.get(INVASION_TIME_MIN);
		maxPhaseChangeTime = timings.get(INVASION_TIME_MAX);
		this.rats=TPPQuestHelperFunctions.getRats();
		addConversations(TPPQuestHelperFunctions.getMainNPC());
	}


	@Override
	public int getMinTimeOut() {
		return minPhaseChangeTime;
	}


	@Override
	public int getMaxTimeOut() {
		return maxPhaseChangeTime;
	}



	/**
	 * rats invasion starts :-)
	 * Iterate through each zone and select the min and max rat count based on zone size
	 * Places rat if possible, if not skip this rat (so if 6 rats chosen perhaps only 3 are placed)
	 */
	private void summonRats() {

		final RatsObserver ratsObserver = new RatsObserver();

		// generating rats in zones
		for(int j=0; j<(RAT_ZONES.size()); j++) {
			final StendhalRPZone zone = (StendhalRPZone) SingletonRepository.getRPWorld().getRPZone(
					RAT_ZONES.get(j));
			final int maxRats = (int) Math.round(Math.sqrt(zone.getWidth()*zone.getHeight())/4);
			final int minRats = (int) Math.round(Math.sqrt(zone.getWidth()*zone.getHeight())/12);
			final int ratsCount = Rand.rand(maxRats-minRats)+minRats;
			logger.debug(ratsCount+ " rats selected at " + zone.getName());
			for(int i=0 ; i<ratsCount; i++) {
				final int x=Rand.rand(zone.getWidth());
				final int y=Rand.rand(zone.getHeight());
				final Creature tempCreature = TPPQuestHelperFunctions.getRandomRat();
				final Creature rat = new Creature(tempCreature.getNewInstance());

				// chosen place is occupied
				if (zone.collides(rat,x,y)) {
					// Could not place the creature here.
					// Treat it like it was never exists.
					logger.debug("RATS " + zone.getName() + " " + x + " " + y + " collided.");
					continue;
				} else if (zone.getName().startsWith("0")) {
					// If we can't make it here, we can't make it anywhere ...
					// just checking the 0 level zones atm
					// the rat is not in the zone yet so we can't call the smaller version of the searchPath method
					final List<Node> path = Path.searchPath(zone, x, y, zone.getWidth()/2,
							zone.getHeight()/2, (64+64)*2);
					if (path == null || path.size() == 0){
						logger.debug("RATS " + zone.getName() + " " + x + " " + y + " no path to " + zone.getWidth()/2 + " " + zone.getHeight()/2);
						continue;
					}
				}
				// spawn creature
				rat.registerObjectsForNotification(ratsObserver);
				/* -- commented because of these noises reflects on all archrats in game -- */
				// add unique noises to humanoids
				if (tempCreature.getName().equals("鼠人弓箭手")) {
					final LinkedList<String> ll = new LinkedList<String>(
							Arrays.asList("我们会占领阿多斯!",
							"我们的报复会很可怕!"));
					LinkedHashMap<String, LinkedList<String>> lhm =
						new LinkedHashMap<String, LinkedList<String>>();
					// add to all states except death.
					lhm.put("idle", ll);
					lhm.put("fight", ll);
					lhm.put("follow", ll);
					rat.setNoises(lhm);
				}

				StendhalRPAction.placeat(zone, rat, x, y);
				rats.add(rat);
			}
		}
	}

	/**
	 * function to control amount of alive rats.
	 * @param dead
	 * 			- creature that was just died.
	 */
	private void notifyDead(final RPEntity dead) {
		if (!rats.remove(dead)) {
			logger.warn("杀死的生物不在控制列表中 ("+dead.toString()+").");
		}
		if (rats.size()==0) {
			phaseToDefaultPhase(
					new LinkedList<String>(Arrays.asList("彼德彼伯")));
		}
    }

	/**
	 *  Rats are dead :-)
	 */

	@Override
	public String getSwitchingToDefPhaseMessage() {
		final String text = "查玛斯市长宣布:在阿多斯没有老鼠 #rats 存活, "+
				            "它们只能住在鬼屋"+
				            "欢迎捕鼠猎人领取 #奖励.";
		return(text);
	}

	/**
	 *  Rats now living under all buildings. Need to call 彼德彼伯 :-)
	 */
	@Override
	public String getSwitchingToNextPhaseMessage() {
		final String text =
			"查玛斯市长播报: 紧急, #老鼠 已占领城市, "+
		  //"查玛斯市长 shouts: The #rats left as suddenly as they arrived. "+
		  //"Perhaps they have returned to the sewers. "+
			"我现在南要呼叫彼德彼伯, 一个灭鼠人."+
			"另外, 感谢所有尽力清理 阿多斯的市民, "+
			"欢迎你们领取 #奖励.";
		return(text);
	}

	/**
	 * removing rats from the world
	 */
	private void removeAllRats() {
		final int sz=rats.size();
		int i=0;
		while(rats.size()!=0) {
			try {
			final Creature rat = rats.get(0);
			rat.stopAttack();
			rat.clearDropItemList();
			rat.getZone().remove(rat);
			rats.remove(0);
			i++;
			} catch (IndexOutOfBoundsException ioobe) {
				// index is greater then size???
				logger.error("removeAllRats IndexOutOfBoundException at "+
						Integer.toString(i)+" position. Total "+
						Integer.toString(sz)+" elements.", ioobe);
			}
		}
	}

	/**
	 * Red alert! Rats in the 阿多斯城!
	 *
	 * @return Ados mayor's call for help message
	 */
	protected String ratsProblem() {
		final String text = "查玛斯市长 急呼: 阿多斯城发生 #鼠灾 !"+
			              " 任何帮助阿多斯渡过难关的人, 都有奖励!";
		return(text);
	}

	@Override
	public void prepare() {
		summonRats();
		super.startShouts(timings.get(SHOUT_TIME), ratsProblem());
	}

	@Override
	public void phaseToDefaultPhase(List<String> comments) {
		comments.add("老鼠已全部清除");
		super.phaseToDefaultPhase(comments);
	}


	@Override
	public void phaseToNextPhase(ITPPQuest nextPhase, List<String> comments) {
		comments.add("现行状况, 还有 "+rats.size()+" 只老鼠存活.");
		removeAllRats();
		super.phaseToNextPhase(nextPhase, comments);
	}

    /**
     *  Implementation of Observer interface.
     *  Update function will record the fact of rat's killing
     *  in player's quest slot.
     */
	class RatsObserver implements Observer {
		@Override
		public void update (Observable obj, Object arg) {
	        if (arg instanceof CircumstancesOfDeath) {
	    		final CircumstancesOfDeath circs=(CircumstancesOfDeath)arg;
	        	if(RAT_ZONES.contains(circs.getZone().getName())) {
	        	if(circs.getKiller() instanceof Player) {
	        		final Player player = (Player) circs.getKiller();
	        		killsRecorder(player, circs.getVictim());
	        	}
	        	notifyDead(circs.getVictim());
	        	}
	        }
	    }
	}

	/**
	 *  method for making records about killing rats
	 *  in player's quest slot.
	 *
	 *  @param player
	 *  			- player which killed rat.
	 *  @param victim
	 *  			- rat object
	 */
	private void killsRecorder(Player player, final RPEntity victim) {

		final String str = victim.getName();
		final int i = RAT_TYPES.indexOf(str);
		if(i==-1) {
			//no such creature in reward table, will not count it
			logger.warn("Unknown creature killed: "+
					    victim.getName());
			return;
		}

		if((player.getQuest(QUEST_SLOT)==null)||
		   (player.getQuest(QUEST_SLOT).equals("done")||
		   (player.getQuest(QUEST_SLOT).equals("")))){
			// player just killed his first creature.
		    player.setQuest(QUEST_SLOT, "rats;0;0;0;0;0;0");
		}

		// we using here and after "i+1" because player's quest index 0
		// is occupied by quest stage description.
		if ("".equals(player.getQuest(QUEST_SLOT,i+1))){
			// something really wrong, will correct this...
			player.setQuest(QUEST_SLOT,"rats;0;0;0;0;0;0");
		}
		int kills;
		try {
			kills = Integer.parseInt(player.getQuest(QUEST_SLOT, i+1))+1;
		} catch (NumberFormatException nfe) {
			// have no records about this creature in player's slot.
			// treat it as he never killed this creature before.
			kills=1;
		}
		player.setQuest(QUEST_SLOT, i+1, Integer.toString(kills));
	}


	@Override
	public TPP_Phase getPhase() {
		return TPP_Phase.TPP_INVASION;
	}



}
