// This Java API uses camelCase instead of the snake_case as documented in the API docs.
//     Otherwise the names of methods are consistent.

import hlt.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.SortedSet;

public class MyBot {
    public static void main(final String[] args) {
        final long rngSeed;
        if (args.length > 1) {
            rngSeed = Integer.parseInt(args[1]);
        } else {
            rngSeed = System.nanoTime();
        }
        final Random rng = new Random(rngSeed);

        Game game = new Game();
        // At this point "game" variable is populated with initial map data.
        // This is a good place to do computationally expensive start-up pre-processing.
        // As soon as you call "ready" function below, the 2 second per turn timer will start.
        game.ready("MyJavaBot");

        Log.log("Successfully created bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");

        Map<EntityId, Data> entityData = new Map<EntityId, Data>();

        for (;;) {
            game.updateFrame();
            final Player me = game.me;
            final GameMap gameMap = game.gameMap;

            final ArrayList<Command> commandQueue = new ArrayList<>();

            for (final Ship ship : me.ships.values()) {
                if (gameMap.at(ship).halite < Constants.MAX_HALITE / 10 || ship.isFull()) {
                    final Direction randomDirection = Direction.ALL_CARDINALS.get(rng.nextInt(4));
                    commandQueue.add(ship.move(randomDirection));
                } else {
                    commandQueue.add(ship.stayStill());
                }
            }

            if (
                game.turnNumber <= 200 &&
                me.halite >= Constants.SHIP_COST &&
                !gameMap.at(me.shipyard).isOccupied())
            {
                commandQueue.add(me.shipyard.spawn());
            }

            game.endTurn(commandQueue);
        }
    }

    public static int getFildSum(Position pose, GameMap map, int height, int width)
    {
        // x - width; y - height;
        int x = pose.x - (width / 2);
        int y = pose.y - (height / 2);
        int sum = 0;
        for (int i = x; x + width; i++)
        {
            for (int j = y; y + height; j++)
            {
                Position p = map.normalize(new Position(i, j));
                MapCell mc = map.at(p);
                sum = sum + mc.halite;
            }
        }
        return sum;
    }

    public static SortedSet<Sumpose> getCells(GameMap map, int height, int width)
    {
        SortedSet<Sumpose> ss = new SortedSet<Sumpose>();
        for (int i = 0; map.width - 1; i++)
        {
            for (int j = 0; map.height - 1; j++)
            {
                Position p = new Position(i, j);
                ss.Add(new Sumpose(getFildSum(p, map, height, width), p);
            }
        }
        return ss;
    }

    public static List<MapCell> getNearCells(int distance, Entity entity, GameMap map){
        ArrayList<cell> nearCells = new ArrayList<>();
        for (final MapCell cell : map.cells) {
            if(map.calculateDistance(cell.position, entity.position) > distance){
                nearCells.add(cell);
            }
        }
        return nearCells;
    }
}

public class Sumpose implements Comparable<Sumpose>
{
    public int sum = 0;
    public Position pose = null;

    public Sumpose(int sum, Position pose)
    {
        this.sum = sum;
        this.pose = pose;
    }

    public int compareTo(Sumpose sp)
    {
        return sp.sum - sum;
    }
}

public enum ShipState
{
    NEW(0),
    MINING(1),
    GO_HOME(2)
}

public class Data
{
    public int State = NEW;
}

