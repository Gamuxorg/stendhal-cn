// $Id$
package games.stendhal.server.script;

import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.npc.NPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.scripting.ScriptImpl;

import java.util.List;


/**
 * end of the world
 */
public class Doomsday extends ScriptImpl {

	@Override
    public void execute(Player admin, List<String> args) {
	    super.execute(admin, args);
	    
	    reduceNPCsHP();
    }

	private void reduceNPCsHP() {
	    StendhalRPZone semos = (StendhalRPZone) StendhalRPWorld.get().getRPZone("0_semos_city");
	    
	    for (NPC npc : semos.getNPCList()) {
	    	npc.setHP(10);
	    }
    }
	
}
