package games.stendhal.server.maps.ados.tavern;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.Log4J;
import marauroa.server.game.db.DatabaseFactory;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;


public class BarmanNPCTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		new DatabaseFactory().initializeDatabase();
		MockStendlRPWorld.get();
		PlayerTestHelper.generatePlayerRPClasses();
		ItemTestHelper.generateRPClasses();
		PlayerTestHelper.generateNPCRPClasses();

		MockStendhalRPRuleProcessor.get();
		// load item configurations to handle money and other items
		SingletonRepository.getEntityManager();

		SingletonRepository.getNPCList().clear();
	}


	@Test
	public void testChoclateBar() throws Exception {
		BarmanNPC zoneconfig = new BarmanNPC();
		StendhalRPZone testzone = new StendhalRPZone("dalesTestZone");
		zoneconfig.configureZone(testzone , null);
		SpeakerNPC dale = SingletonRepository.getNPCList().get("dale");
		Engine engine = dale.getEngine();
		Player player = PlayerTestHelper.createPlayer("monsterdhal");

		engine.step(player,"hi");
		assertThat(getReply(dale),is("Hey, good looking ..."));

		engine.step(player,"buy 500 巧克力棒");
		assertThat(getReply(dale),is("500 巧克力棒s will cost 50000. Do you want to buy them?"));
		final Item item = ItemTestHelper.createItem("money", 50000);
		player.getSlot("背包").add(item);

		engine.step(player,"yes");
		assertThat(getReply(dale),is("Congratulations! Here are your 巧克力棒s!"));

		engine.step(player,"bye");
		assertThat(getReply(dale),is("See you around, sweetcheeks."));



		engine.step(player,"hi");
		assertThat(getReply(dale),is("Hey, good looking ..."));

		engine.step(player,"offer");
		assertThat(getReply(dale), anyOf(equalTo("I sell 红酒, 椰林飘香, and 巧克力棒."),
				equalTo("I sell 红酒, 巧克力棒, and 椰林飘香."),
				equalTo("I sell 巧克力棒, 红酒, and 椰林飘香."),
				equalTo("I sell 巧克力棒, 椰林飘香, and 红酒."),
				equalTo("I sell 椰林飘香, 巧克力棒, and 红酒."),
				equalTo("I sell 椰林飘香, 红酒, and 巧克力棒.")));

		engine.step(player,"buy 100 chocolate");
		assertThat(getReply(dale),is("Please specify which sort of chocolate you want to buy."));

		engine.step(player,"buy 100 chocolates");
		assertThat(getReply(dale),is("Please specify which sort of chocolate you want to buy."));
	}

}
