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
import java.util.Observer;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.GoToPosition;
import games.stendhal.server.core.pathfinder.MultiZonesFixedPath;
import games.stendhal.server.core.pathfinder.RPZonePath;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.interaction.NPCChatting;
import games.stendhal.server.entity.npc.interaction.NPCFollowing;

/**
 * Implementation of 彼德彼伯's initial actions (coming, chatting, going to work place)
 * @author yoriy
 */
public class AwaitingPhase extends TPPQuest {

	private SpeakerNPC piedpiper;
	private final SpeakerNPC mainNPC = TPPQuestHelperFunctions.getMainNPC();
	private final int minPhaseChangeTime;
	private int maxPhaseChangeTime;
	private List<RPZonePath> fullpathin =
		new LinkedList<RPZonePath>();
	private List<RPZonePath> fullpathout =
			new LinkedList<RPZonePath>();
	private final List<String> conversations = new LinkedList<String>();
	private final String explainations =
			"我在这里见到了我们市的救星. 我必须马上和他谈谈. "+
			"请在我讲完后再问我.";


	/**
	 * adds quest's related conversations to mayor
	 */
	private void addConversations() {
		TPP_Phase myphase = AWAITING;

		// Player asking about rats.
		mainNPC.add(
				ConversationStates.ATTENDING,
				Arrays.asList("鼠灾", "老鼠"),
				new TPPQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				"好的, 我们尽力打扫城市. "+
	    		"你的帮助会得到 #奖励, 如果想了解 #详情 只管问.",
				null);

		// Player asking about details.
		mainNPC.add(
				ConversationStates.ATTENDING,
				"详情",
				new TPPQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				null,
				new DetailsKillingsAction());

		// Player asked about reward
		mainNPC.add(
				ConversationStates.ATTENDING,
				"奖励",
				new TPPQuestInPhaseCondition(myphase),
				ConversationStates.ATTENDING,
				null,
				new RewardPlayerAction());
	}


	/**
	 * fills conversations list between mayor and piper
	 */
	private void fillConversations() {
		//piper
		conversations.add("你好, 查玛斯市长. 你叫我是什么事?");
		//mayor
		conversations.add("Hello, 在这里能见到我们尊敬的英雄我非常开心. 还没听说过你的人, 那就太...");
		//piper
		conversations.add("请谈谈你的来意, 我的时间不多.");
		//mayor
		conversations.add("... 好, 我说什么了? 啊对, 我们的城市又出现 #鼠灾 问题.");
		//piper
		conversations.add("又?");
		//mayor
		conversations.add("是的, 这些动物太蠢, 前不久刚教训过它们就忘了.");
		//piper
		conversations.add("我能帮忙, 如果你付钱的话.");
		//mayor
		conversations.add("阿多斯城没有别的方法除掉这些害虫. 我们会付钱.");
		//piper
		conversations.add("你了解我的收费吗?");
		//mayor
		conversations.add("是的, 我已记在纸上了.");
		//piper
		conversations.add("好. 我要尽快收到报酬, 请备好钱.");
		//mayor
		conversations.add("不用担心, 我在我的城内说到做到.");
	}


	/**
	 * constructor
	 * @param timings - a pair of time parameters for phase timeouts
	 */
	public AwaitingPhase(final Map<String, Integer> timings) {
		super(timings);
		minPhaseChangeTime = timings.get(AWAITING_TIME_MIN);
		maxPhaseChangeTime = timings.get(AWAITING_TIME_MAX);
		addConversations();
		fillConversations();
	}


	/**
	 * prepare actions
	 */
	@Override
	public void prepare() {
		createPiedPiper();
	}


	/**
	 * function for creating 彼德彼伯 npc
	 */
	private void createPiedPiper() {
		piedpiper = new SpeakerNPC("彼德彼伯");
		TPPQuestHelperFunctions.setupPiper(piedpiper);
		fullpathin = PathsBuildHelper.getAdosIncomingPath();
		fullpathout = PathsBuildHelper.getAdosTownHallBackwardPath();
		leadNPC();
	}


	/**
	 * function will remove piped piper npc object
	 */
	private void destroyPiedPiper() {
		piedpiper.getZone().remove(piedpiper);
		piedpiper = null;
	}

	/**
	 * prepare NPC to walk through his multizone pathes and do some actions during that.
	 */
	private void leadNPC() {
		final StendhalRPZone zone = fullpathin.get(0).get().first();
		final int x=fullpathin.get(0).get().second().get(0).getX();
		final int y=fullpathin.get(0).get().second().get(0).getY();
		piedpiper.setPosition(x, y);
		zone.add(piedpiper);
		Observer o = new MultiZonesFixedPath(piedpiper, fullpathin,
						new NPCFollowing(mainNPC, piedpiper,
							new NPCChatting(piedpiper, mainNPC, conversations, explainations,
								new GoToPosition(piedpiper, PathsBuildHelper.getAdosTownHallMiddlePoint(),
									new MultiZonesFixedPath(piedpiper, fullpathout,
										new PhaseSwitcher(this))))));
		o.update(null, null);
	}


	/**
	 * @return - min quest phase timeout
	 */
	@Override
	public int getMinTimeOut() {
		return minPhaseChangeTime;
	}


	/**
	 * @return - max quest phase timeout
	 */
	@Override
	public int getMaxTimeOut() {
		return maxPhaseChangeTime;
	}


	/**
	 * @param comments - comments for switching event
	 */
	@Override
	public void phaseToDefaultPhase(List<String> comments) {
		destroyPiedPiper();
		super.phaseToDefaultPhase(comments);
	}


	/**
	 * @param nextPhase - next phase
	 * @param comments - comments for switching event
	 */
	@Override
	public void phaseToNextPhase(ITPPQuest nextPhase, List<String> comments) {
		destroyPiedPiper();
		super.phaseToNextPhase(nextPhase, comments);
	}


	/**
	 *  彼德彼伯 will now start to collect rats :-)
	 *  @return - npc shouts at switching quest phase.
	 */
	@Override
	public String getSwitchingToNextPhaseMessage() {
		final String text =
			/*
			"彼德彼伯 shouts: Ados citizens, now i will clean up your city from rats. I will play " +
			"magical melody, and rats will attracts to me. Please do not try to block or kill them, " +
			"because melody will also protect MY rats. " +
			"Just enjoy the show.";
			 */
			"查玛斯市长 高喊: 感谢, 现在所有的 #老鼠 都被清除, " +
			"彼德彼伯 催眠了它们, 并把它们带出了地牢. "+
			"帮助过阿多斯城解决鼠灾的人们, "+
			"现在都能拿到 #奖励.";

		return text;
	}


	/**
	 * @return - current phase
	 */
	@Override
	public TPP_Phase getPhase() {
		return TPP_Phase.TPP_AWAITING;
	}

}

