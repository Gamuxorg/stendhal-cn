package games.stendhal.server.maps.kalavan.cottage_2;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

/**
 * Provides Gertha, the 樱桃派s confectioner NPC.
 * She has a twin sister: Martha, the 苹果派s confectioner NPC.
 *
 * @author omero
 */
public class ConfectionerCherryPieNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Gertha") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(10, 7));
				nodes.add(new Node(4, 7));
				nodes.add(new Node(9, 7));
				nodes.add(new Node(9, 3));
				nodes.add(new Node(6, 3));
				nodes.add(new Node(10, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {

				addJob("I live here with my twin sister #Martha and our passion is to #bake delicious fruit pies!");

				addReply("martha",
					"She's my twin sister and we live here together... Like me, she also likes to #bake fruit pies!");
				addReply("蜂蜜",
					"Try asking #Martha where she finds it.");
				addReply("牛奶",
					"I suppose you can find that in a farm!");
				addReply("面粉",
					"I'd look for some at a mill...");
				addReply("egg",
					"Find some hens and you'll easily find some eggs too!");
				addReply("樱桃",
					"Mmm... Those are sometimes hard to get. Did you already ask in some tavern if they can offer any?");

				addHelp("If it would help, I could #bake a 樱桃派 for you!");
				addOffer("I'd be happy to #bake a 樱桃派 for you. Why don't you just ask me?!");

				addQuest("I'd love to try and bake a strawberry pie once in a while... But alas! Strawberries are nowhere to be found...");

				addGoodbye("Take care!");

				// (uses sorted TreeMap instead of HashMap)
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("面粉", Integer.valueOf(2));
				requiredResources.put("蜂蜜", Integer.valueOf(1));
				requiredResources.put("牛奶", Integer.valueOf(1));
				requiredResources.put("egg", Integer.valueOf(1));
				requiredResources.put("樱桃", Integer.valueOf(2));

				final ProducerBehaviour behaviour = new ProducerBehaviour("gertha_bake_cherrypie", "bake", "樱桃派",
				        requiredResources, 15 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				        "Hello! Did you come to taste one of my fabulous 樱桃派s? I could #bake one for you happily!");
			}
		};

		npc.setEntityClass("confectionercherrypienpc");
		npc.setDirection(Direction.DOWN);
		npc.setPosition(10, 6);
		npc.initHP(100);
		npc.setDescription("You see Gertha. She loves baking 樱桃派s for her guests.");
		zone.add(npc);
	}
}
