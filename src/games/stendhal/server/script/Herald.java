/***************************************************************************
 *                   (C) Copyright 2003-2013 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.script;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.Direction;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.core.scripting.ScriptingSandbox;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.AdminCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.player.Player;


/**
 * A herald which will tell news to citizens.
 *
 * @author yoriy
 */
public class Herald extends ScriptImpl {

	// TODO: there is ability of using list of herald names,
	// it will add to game more fun.
    public final String HeraldName = "Patrick";

    // after some thinking, i decided to not implement here
	// news records to file.
	private final Logger logger = Logger.getLogger(Herald.class);
    private final int REQUIRED_ADMINLEVEL_INFO = 100;
    private final int REQUIRED_ADMINLEVEL_SET = 1000;
    private final TurnNotifier turnNotifier = TurnNotifier.get();

    //private final String HaveNoTime = "Hi, I have to do my job, so I have no time to speak with you, sorry.";
    private final String HiOldFriend = "Oh, 你来啦! Hi, 老朋友, 见到你很高兴.";
    private final String TooScared = "Oh, 你一定疯了吧, 我帮不了你, 皇帝会为此杀掉我们两个.";
    private final String BadJoke = "是笑话? 我喜欢开玩笑, 但不要过份.";
    private final String FeelBad = "Oh, 我不清楚哪里不对, 总觉得不太好...抱歉, 我帮不了你...";
    private final String DontUnderstand = "对不起, 没明白你的意思";
    private final String InfoOnly = "Oh, 我还不能完全相信你, 所以不能把最新的通知表告诉你. ";
    private final String WillHelp = "当然, 你想要的我都可以做."+
									" 告诉我 '#speech <时间间隔 (秒)> <时间总长 (秒)> <讲话内容>'. " +
									"如果你想在我当前通告中删除一条, "+
									"请说 '#remove <number of speech>'. "+
									"你也可以向我要当前公告, 说 '#info' 就行.";

    private final LinkedList<HeraldNews> heraldNews = new LinkedList<HeraldNews>();

    /**
     * class for herald announcements.
     */
    private final static class HeraldNews {

		private final String news;
		private final int interval;
		private final int limit;
		private int counter;
		private final int id;
		private final HeraldListener tnl;
		public String getNews(){
			return(news);
		}
		public int getInterval(){
			return(interval);
		}
		public int getLimit(){
			return(limit);
		}
		public int getCounter(){
			return(counter);
		}
		public int getid(){
			return(id);
		}
		public HeraldListener getTNL(){
			return(tnl);
		}
		public void setCounter(int count){
			this.counter=count;
		}

		/**
		 * constructor for news
		 * @param news - text to speech
		 * @param interval - interval between speeches in seconds.
		 * @param limit - time limit in seconds.
		 * @param counter - counter of speeches
		 * @param tnl - listener object
		 * @param id - unique number to internal works with news.
		 */
		public HeraldNews(String news, int interval, int limit, int counter,
				HeraldListener tnl, int id){
			this.news=news;
			this.interval=interval;
			this.limit=limit;
			this.counter=counter;
			this.tnl=tnl;
			this.id=id;
		}
	}

	/**
	 * herald turn listener object.
	 */
	class HeraldListener implements TurnListener{
		private final int id;
		/**
		 * function invokes by TurnNotifier each time when herald have to speech.
		 */
		@Override
		public void onTurnReached(int currentTurn) {
			workWithCounters(id);
			}
			/**
			 * tnl constructor.
			 * @param i - id of news
			 */
		public HeraldListener(int i) {
			id=i;
		}
	}

	/**
	 * invokes by /script -load Herald.class,
	 * placing herald near calling admin.
	 */
	@Override
	public void load(final Player admin, final List<String> args, final ScriptingSandbox sandbox) {
		if (admin==null) {
			logger.error("herald called by null admin", new Throwable());
		} else {
			if (sandbox.getZone(admin).collides(admin.getX()+1, admin.getY())) {
				logger.info("Spot for placing herald is occupied.");
				admin.sendPrivateText("Spot (right) near you is occupied, can't place herald here.");
				return;
			}
			sandbox.setZone(admin.getZone());
			sandbox.add(getHerald(sandbox.getZone(admin), admin.getX() + 1, admin.getY()));
		}
	}

	/**
	 * function invokes by HeraldListener.onTurnReached each time when herald have to speech.
	 * @param id - ID for news in news list
	 */
	public void workWithCounters(int id) {
		int index=-1;
		for (int i=0; i<heraldNews.size(); i++){
			if(heraldNews.get(i).getid()==id){
				index=i;
			}
		}
		if (index==-1) {
			logger.info("workWithCounters: id not found. ");
		}
		try {
			final int interval = heraldNews.get(index).getInterval();
			final int limit = heraldNews.get(index).getLimit();
			final String text = heraldNews.get(index).getNews();
			int counter = heraldNews.get(index).getCounter();
			HeraldListener tnl = heraldNews.get(index).getTNL();
			final SpeakerNPC npc = SingletonRepository.getNPCList().get(HeraldName);
			npc.say(text);
			counter++;
			turnNotifier.dontNotify(tnl);
			if(interval*counter<limit){
				heraldNews.get(index).setCounter(counter);
				turnNotifier.notifyInSeconds(interval, tnl);
			} else {
				// it was last announce.
				heraldNews.remove(index);
			}
		} catch (IndexOutOfBoundsException ioobe) {
			logger.error("workWithCounters: index is out of bounds: "+Integer.toString(index)+
					     ", size "+Integer.toString(heraldNews.size())+
					     ", id "+Integer.toString(id),ioobe);
		}
	}

	/**
	 * kind of herald constructor
	 * @param zone - zone to place herald
	 * @param x - x coord in zone
	 * @param y - y coord in zone
	 * @return herald NPC :-)
	 */
	private SpeakerNPC getHerald(StendhalRPZone zone, int x, int y) {
		final SpeakerNPC npc = new SpeakerNPC(HeraldName) {

			/**
			 * npc says his job list
			 */
			class ReadJobsAction implements ChatAction {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc){
					int newssize = heraldNews.size();
					if(newssize==0){
						npc.say("通告列表为空.");
						return;
					}
					StringBuilder sb=new StringBuilder();
					sb.append("以下是当前通告: ");


					for(int i=0; i<newssize;i++){
						// will add 1 to position numbers to show position 0 as 1.
						logger.info("info: index "+Integer.toString(i));
						try {
						final int left = heraldNews.get(i).getLimit()/heraldNews.get(i).getInterval()-
										 heraldNews.get(i).getCounter();
						sb.append(" #"+Integer.toString(i+1)+". (left "+
								  Integer.toString(left)+" times): "+
								"#Every #"+Integer.toString(heraldNews.get(i).getInterval())+
								" #seconds #to #"+Integer.toString(heraldNews.get(i).getLimit())+
								" #seconds: \""+heraldNews.get(i).getNews()+"\"");
						} catch (IndexOutOfBoundsException ioobe) {
							logger.error("ReadNewsAction: size of heraldNews = "+
									Integer.toString(newssize), ioobe);
						}
						if(i!=(newssize-1)){
							sb.append("; ");
						}
					}
					npc.say(sb.toString());
				}
			}


			/**
			 * npc says his job list
			 */
			class ReadNewsAction implements ChatAction {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc){
					int newssize = heraldNews.size();
					if(newssize==0){
						npc.say("通告列表为空.");
						return;
					}

					StringBuilder sb=new StringBuilder();
					sb.append("当前公告为: ");

					for(int i=0; i<newssize;i++){
						// will add 1 to position numbers to show position 0 as 1.
						logger.info("info: index "+Integer.toString(i));
						try {
						sb.append("\""+heraldNews.get(i).getNews()+"\"");
						} catch (IndexOutOfBoundsException ioobe) {
							logger.error("ReadNewsAction: size of heraldNews = "+
									Integer.toString(newssize), ioobe);
						}
						if(i!=(newssize-1)){
							sb.append("; ");
						}
					}
					npc.say(sb.toString());
				}
			}
			/**
			 * NPC adds new job to his job list.
			 */
			class WriteNewsAction implements ChatAction {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc){
					String text = sentence.getOriginalText();
					logger.info("Original sentence: " + text);
					final String[] starr = text.split(" ");
					if(starr.length < 2){
						npc.say("你忘了加时间限制. 你也知道我也是人而且年龄大了.");
						return;
					}
					try {
						final int interval = Integer.parseInt(starr[1].trim());
						final int limit = Integer.parseInt(starr[2].trim());
						if(limit < interval){
							npc.say("我计算出 "+Integer.toString(interval)+
									", 和 "+Integer.toString(limit)+" 小于 " + Integer.toString(interval)+
									". 请再试一次.");
							return;
						}
						try {
							text = text.substring(starr[0].length()).trim().
										substring(starr[1].length()).trim().
								        substring(starr[2].length()).trim();
							final String out="时间间隔: "+Integer.toString(interval)+", 时间总长: "+
							Integer.toString(limit)+", text: \""+text+"\"";
							npc.say("Ok,我已记下. "+out);
							logger.info("Admin "+player.getName()+
										" added announcement: " +out);
							final HeraldListener tnl = new HeraldListener(heraldNews.size());
							heraldNews.add(new HeraldNews(text, interval, limit, 0, tnl, heraldNews.size()));
							turnNotifier.notifyInSeconds(interval,tnl);
						} catch (IndexOutOfBoundsException ioobe) {
						    	npc.say(FeelBad);
						    	logger.error("WriteNewsAction: Error while parsing sentence "+sentence.toString(), ioobe);
						}
					} catch (NumberFormatException nfe) {
						 npc.say(DontUnderstand);
						 logger.info("Error while parsing numbers. Interval and limit is: "+"("+starr[0]+"), ("+starr[1]+")");
					}
				}
			}

			/**
			 * NPC removes one job from his job list.
			 */
			class RemoveNewsAction implements ChatAction {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc){
					String text = sentence.getOriginalText();
					final String[] starr = text.split(" ");
					if(starr.length < 2){
						npc.say("告诉我要删除通告的编号.");
						return;
					}
					final String number = starr[1];
					try {
						final int i = Integer.parseInt(number)-1;
						if (i < 0){
							npc.say(BadJoke);
							return;
						}
						if (heraldNews.size()==0) {
							npc.say("目前没有通告.");
							return;
						}
						if(i>=(heraldNews.size())){
							npc.say("现在只有 "+ Integer.toString(heraldNews.size())+
									" 条通告.");
							return;
						}
						logger.warn("Admin "+player.getName()+" 正删除公告 #"+
									Integer.toString(i)+": interval "+
									Integer.toString(heraldNews.get(i).getInterval())+", limit "+
									Integer.toString(heraldNews.get(i).getLimit())+", text \""+
									heraldNews.get(i).getNews()+"\"");
						turnNotifier.dontNotify(heraldNews.get(i).getTNL());
						heraldNews.remove(i);
						npc.say("Ok, 已删除.");
					} catch (NumberFormatException nfe) {
						logger.error("RemoveNewsAction: cant remove "+number+" speech.", nfe);
						npc.say(DontUnderstand);
					}
				}
			}

			/**
			 * npc removes all jobs from his job list
			 */
			class ClearNewsAction implements ChatAction {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc){
						logger.info("ClearAllAction: Admin "+player.getName()+
									" cleared announcement list.");
						for (int i=0; i<heraldNews.size(); i++) {
							turnNotifier.dontNotify(heraldNews.get(i).getTNL());
						}
						if(heraldNews.size()!=0){
							npc.say("Ufff, 总算有点闲时间. 听说, 塞门镇 可以赌博?");
							heraldNews.clear();
						} else {
							npc.say("Oh, 谢谢你的关心, 但我还好.");
						}
					}
			}

			/**
			 *  Finite states machine logic for herald.
			 */
			@Override
			public void createDialog() {
				add(ConversationStates.IDLE,
					Arrays.asList("hi", "喂", "hello", "你好"),
					new NotCondition(new AdminCondition(REQUIRED_ADMINLEVEL_INFO)),
					ConversationStates.IDLE,
					null,	new ReadNewsAction());
				add(ConversationStates.IDLE,
					Arrays.asList("hi", "喂", "hello", "你好"),
					new AdminCondition(REQUIRED_ADMINLEVEL_INFO),
					ConversationStates.ATTENDING,
					HiOldFriend, null);
				add(ConversationStates.ATTENDING,
					Arrays.asList("help", "帮助", "帮忙"),
					new AdminCondition(REQUIRED_ADMINLEVEL_SET),
					ConversationStates.ATTENDING,
					WillHelp, null);
				add(ConversationStates.ATTENDING,
					Arrays.asList("speech", "remove"),
					new NotCondition(new AdminCondition(REQUIRED_ADMINLEVEL_SET)),
					ConversationStates.ATTENDING,
					TooScared, null);
				add(ConversationStates.ATTENDING,
					Arrays.asList("help", "帮助", "帮忙"),
					new NotCondition(new AdminCondition(REQUIRED_ADMINLEVEL_SET)),
					ConversationStates.ATTENDING,
					InfoOnly, new ReadJobsAction());
				add(ConversationStates.ATTENDING,
					Arrays.asList("info", "list", "tasks", "news"),
					new AdminCondition(REQUIRED_ADMINLEVEL_INFO),
					ConversationStates.ATTENDING,
					null, new ReadJobsAction());
				add(ConversationStates.ATTENDING,
					Arrays.asList("speech"),
					new AdminCondition(REQUIRED_ADMINLEVEL_SET),
					ConversationStates.ATTENDING,
					null, new WriteNewsAction());
				add(ConversationStates.ATTENDING,
					Arrays.asList("remove", "删除"),
					new AdminCondition(REQUIRED_ADMINLEVEL_SET),
					ConversationStates.ATTENDING,
					null, new RemoveNewsAction());
				add(ConversationStates.ATTENDING,
						Arrays.asList("clear"),
						new AdminCondition(REQUIRED_ADMINLEVEL_SET),
						ConversationStates.ATTENDING,
						null, new ClearNewsAction());
				addGoodbye();
			}
		};
		zone.assignRPObjectID(npc);
		npc.setEntityClass("heraldnpc");
		npc.setPosition(x, y);
		npc.initHP(100);
		npc.setDirection(Direction.LEFT);
		return(npc);
	}
}
