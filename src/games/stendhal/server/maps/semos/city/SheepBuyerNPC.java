package games.stendhal.server.maps.semos.city;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.semos.village.SheepSellerNPC;
import games.stendhal.server.util.Area;
import marauroa.common.game.RPObject;

/**
 * A merchant (original name: 赛特) who buys sheep from players.
 */
public class SheepBuyerNPC implements ZoneConfigurator {

	// The sheep pen where 赛特 moves what he buys
	/** Left X coordinate of the sheep pen */
	private static final int SHEEP_PEN_X = 39;
	/** Top Y coordinate of the sheep pen */
	private static final int SHEEP_PEN_Y = 24;
	/** Width of the sheep pen */
	private static final int SHEEP_PEN_WIDTH = 16;
	/** Height of the sheep pen */
	private static final int SHEEP_PEN_HEIGHT = 6;
	/**
	 * The maximum number of sheep 赛特 keeps in his sheep pen.
	 */
	private static final int MAX_SHEEP_IN_PEN = 8;
	/** The area covering the sheep pen in 塞门镇 */
	private Area pen;

	public class SheepBuyerSpeakerNPC extends SpeakerNPC {

		public SheepBuyerSpeakerNPC(String name) {
			super(name);
			// HP needs to be > 0 for 赛特 to appear in the killer list for
			// the 大灰狼
			setBaseHP(100);
			setHP(100);
		}

		/**
		 * Get the area of the sheep pen where bought sheep
		 * should be moved.
		 *
		 * @param zone the zone of the sheep pen
		 * @return area of the sheep pen
		 */
		private Area getPen(StendhalRPZone zone) {
			if (pen == null) {
				Rectangle2D rect = new Rectangle2D.Double();
				rect.setRect(SHEEP_PEN_X, SHEEP_PEN_Y, SHEEP_PEN_WIDTH, SHEEP_PEN_HEIGHT);
				pen = new Area(zone, rect);
			}

			return pen;
		}

		/**
		 * Try to get rid of the 大灰狼 that could have spawned in the pen,
		 * but miss it sometimes.
		 *
		 * @param zone the zone to check
		 */
		private void killWolves(StendhalRPZone zone) {
			if (Rand.throwCoin() == 1) {
				for (RPObject obj : zone) {
					if (obj instanceof Creature) {
						Creature wolf = (Creature) obj;

						if (wolf.equals(wolf.get("subclass")) && getPen(zone).contains(wolf)) {
							wolf.onDamaged(this, wolf.getHP());
							return;
						}
					}
				}
			}
		}

		/**
		 * Get a list of the sheep in the pen.
		 *
		 * @param zone the zone to check
		 * @return list of sheep in the pen
		 */
		private List<Sheep> sheepInPen(StendhalRPZone zone) {
			List<Sheep> sheep = new LinkedList<Sheep>();
			Area pen = getPen(zone);

			for (RPEntity entity : zone.getPlayerAndFriends()) {
				if (entity instanceof Sheep) {
					if (pen.contains(entity)) {
						sheep.add((Sheep) entity);
					}
				}
			}

			return sheep;
		}

		/**
		 * Move a bought sheep to the den if there's space, or remove it
		 * from the zone otherwise. Remove old sheep if there'd be more
		 * than <code>MAX_SHEEP_IN_PEN</code> after the addition.
		 *
		 * @param sheep the sheep to be moved
		 */
		public void moveSheep(Sheep sheep) {
			// The area of the sheed den.
			int x = Rand.randUniform(SHEEP_PEN_X, SHEEP_PEN_X + SHEEP_PEN_WIDTH - 1);
			int y = Rand.randUniform(SHEEP_PEN_Y, SHEEP_PEN_Y + SHEEP_PEN_HEIGHT - 1);
			StendhalRPZone zone = sheep.getZone();
			List<Sheep> oldSheep = sheepInPen(zone);

			killWolves(zone);
			/*
			 * Keep the amount of sheep reasonable. 赛特's
			 * a business man and letting the sheep starve
			 * would be bad for busines.
			 */
			if (oldSheep.size() >= MAX_SHEEP_IN_PEN) {
				// 赛特 sells the oldest sheep
				zone.remove(oldSheep.get(0));
			}

			if (!StendhalRPAction.placeat(zone, sheep, x, y)) {
				// there was no room for the sheep. Simply eat it
				sheep.getZone().remove(sheep);
			}
		}
	}

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		final SpeakerNPC npc = new SheepBuyerSpeakerNPC("赛特") {
			@Override
			public void createDialog() {
				addGreeting();
				addJob("我在 塞门镇 收购羊, 然后把羊出口到 阿多斯 城. ");
				addHelp("我收购羊,但要公平合理的价格. 有羊的话, 你只需要对我说卖羊 #sell #sheep, 我会马上处理!");
				addGoodbye();
				addQuest("Hmm 我要给我朋友引见一下, 或许你问问 尼世亚 ,他可以给你出点主意...");
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(40, 45));
				nodes.add(new Node(58, 45));
				nodes.add(new Node(58, 22));
				nodes.add(new Node(39, 22));
				nodes.add(new Node(39, 15));
				nodes.add(new Node(23, 15));
				nodes.add(new Node(23, 45));
				setPath(new FixedPath(nodes, true));
			}
		};
		final Map<String, Integer> buyitems = new HashMap<String, Integer>();
		buyitems.put("sheep", 150);
		new BuyerAdder().addBuyer(npc, new SheepBuyerBehaviour(buyitems), true);
		npc.setPosition(40, 45);
		npc.setCollisionAction(CollisionAction.STOP);
		npc.setEntityClass("buyernpc");
		npc.setDescription("你看到了 赛特. 他只喜爱羊.");
		npc.setSounds(Arrays.asList("hiccup-1", "sneeze-1"));
		zone.add(npc);
	}

	private static class SheepBuyerBehaviour extends BuyerBehaviour {
		SheepBuyerBehaviour(final Map<String, Integer> items) {
			super(items);
		}

		private int getValue(ItemParserResult res, final Sheep sheep) {
			return Math.round(getUnitPrice(res.getChosenItemName()) * ((float) sheep.getWeight() / (float) Sheep.MAX_WEIGHT));
		}

		@Override
		public int getCharge(ItemParserResult res, final Player player) {
			if (player.hasSheep()) {
				final Sheep sheep = player.getSheep();
				return getValue(res, sheep);
			} else {
				// npc's answer was moved to BuyerAdder.
				return 0;
			}
		}

		@Override
		public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser seller, final Player player) {
			// res.getAmount() is currently ignored.

			final Sheep sheep = player.getSheep();

			if (sheep != null) {
				if (seller.getEntity().squaredDistance(sheep) > 5 * 5) {
					seller.say("附近没有看到你的羊！把羊带过来, 我好给你出个价.");
				} else if (getValue(res, sheep) < SheepSellerNPC.BUYING_PRICE) {
					// prevent newbies from selling their sheep too early
					seller.say("呐, 那只羊太瘦了. 多给它喂点红树莓, 把它喂肥点再过来吧.");
				} else {
					seller.say("谢谢！这是你的钱.");
					payPlayer(res, player);
					player.removeSheep(sheep);

					player.notifyWorldAboutChanges();
					if(seller.getEntity() instanceof SheepBuyerSpeakerNPC) {
						((SheepBuyerSpeakerNPC)seller.getEntity()).moveSheep(sheep);
					} else {
						// only to prevent that an error occurs and the sheep does not disappear
						sheep.getZone().remove(sheep);
					}

					return true;
				}
			} else {
				seller.say("你根本没有羊, " + player.getTitle() + "! 你还在假装拉什么过来?");
			}

			return false;
		}
	}
}
