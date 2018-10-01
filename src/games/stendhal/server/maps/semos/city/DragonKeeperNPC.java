package games.stendhal.server.maps.semos.city;

import java.util.HashMap;
import java.util.Map;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.BabyDragon;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.player.Player;

public class DragonKeeperNPC implements ZoneConfigurator {

	public static final int BUYING_PRICE = 1;

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildHouseArea(zone);
	}

	private void buildHouseArea(final StendhalRPZone zone) {

		final SpeakerNPC npc = new SpeakerNPC("守龙人") {

			@Override
			protected void createDialog() {
				class DragonSellerBehaviour extends SellerBehaviour {
					DragonSellerBehaviour(final Map<String, Integer> items) {
						super(items);
					}

					@Override
					public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser seller, final Player player) {
						if (res.getAmount() > 1) {
							seller.say("Hmm... 我不认为你一次可以照看一只以上的龙 .");
							return false;
						} else if (player.hasPet()) {
							say("好的, 你应该首先照顾好已经有的那只.");
							return false;
						} else {
							if (!player.drop("money", getCharge(res, player))) {
								seller.say("你的钱好像不够.");
								return false;
							}
							seller.say("要给龙锻炼的机会！你应该把它带在身边并和你一起 #成长");

							final BabyDragon baby_dragon = new BabyDragon(player);

							Entity sellerEntity = seller.getEntity();
							baby_dragon.setPosition(sellerEntity.getX(), sellerEntity.getY() + 1);

							player.setPet(baby_dragon);
							player.notifyWorldAboutChanges();

							return true;
						}
					}
				}

				final Map<String, Integer> items = new HashMap<String, Integer>();
				items.put("dragon", BUYING_PRICE);

				addGreeting();
				addJob("我与这只龙并肩作战, 或许也有一只适合你.");
				addHelp("我销售龙. 想买的话只用对我说我想 #买 #龙 (buy dragon).");
				addGoodbye();
				addReply("成长","带着它战斗, 龙也会获得经验并提级等级.");
				new SellerAdder().addSeller(this, new DragonSellerBehaviour(items));
			}
		};

		npc.setEntityClass("man_005_npc");
		npc.setPosition(17, 7);
		npc.initHP(85);
		npc.setDescription("一个龙守卫乘坐强大的翼龙刚刚飞到城里.");
		zone.add(npc);

	}
}
