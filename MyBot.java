// This Java API uses camelCase instead of the snake_case as documented in the API docs.
//     Otherwise the names of methods are consistent.

import hlt.*;

import java.util.*;
import java.lang.Object;

public class MyBot {
    public static void main(final String[] args) {
        final long rngSeed;
        if (args.length > 1) {
            rngSeed = Integer.parseInt(args[1]);
        } else {
            rngSeed = System.nanoTime();
        }
        final Random rng = new Random(rngSeed);

        final int goForHalite   = 1;
        final int goHome        = 2;

        Game game = new Game();
        // At this point "game" variable is populated with initial map data.
        // This is a good place to do computationally expensive start-up pre-processing.
        // As soon as you call "ready" function below, the 2 second per turn timer will start.
        game.ready("MyJavaBot");

        Log.log("Successfully created bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");

        HashMap<String, Integer> entityData1 = new HashMap<String, Integer>();
        HashMap<String, Position> targetData1 = new HashMap<String, Position>();

        final int distance = 20;
        final int minPrice = 100;
        ArrayList<MapCell> reservedCells;
        ArrayList<MapCell> targetCells;
        for (;;) {
            game.updateFrame();
            final Player me = game.me;
            final GameMap gameMap = game.gameMap;
            reservedCells = new ArrayList<>();
            targetCells = new ArrayList<>();
            HashMap<String, Integer> entityData2 = new HashMap<String, Integer>();
            HashMap<String, Position> targetData2 = new HashMap<String, Position>();

            final ArrayList<Command> commandQueue = new ArrayList<>();

            for (final Ship ship : me.ships.values()) {
//                if (gameMap.at(ship).halite < Constants.MAX_HALITE / 10 || ship.isFull()) {
                    Direction direction = null;
//                    commandQueue.add(ship.move(randomDirection));

                    Log.log("Проверка корабля");
                    Integer Data = entityData1.getOrDefault(getPosKey(ship.position), null);
                    if (Data == null)
                    {
                        entityData1.put(getPosKey(ship.position), goForHalite);
                        Data = goForHalite;
                    }
                    Log.log("состояние" + Data);

//                    targetPosition = targetData1.getKey(ship.position);
                    switch (Data)
                    {
                        case goHome:
                        {
                            Log.log("иду домой" + Data);
                            if(! me.shipyard.position.equals(ship.position))
                            {
                                direction = gameMap.naiveNavigate(ship, me.shipyard.position);
                                Position nPosition = ship.position.directionalOffset(direction);

                                if(isReserved(reservedCells, nPosition)){
                                    commandQueue.add(ship.stayStill());
                                    entityData2.put(getPosKey(ship.position), goHome);
                                    targetData2.put(getPosKey(ship.position), me.shipyard.position);
                                    Log.log("стоять на месте" + getPosKey(ship.position));
                                    continue;
                                }
                                if(! gameMap.at(nPosition).isOccupied()){
                                    final Direction randomDirection = Direction.ALL_CARDINALS.get(rng.nextInt(4));
                                    nPosition = ship.position.directionalOffset(randomDirection);
                                    if(!gameMap.at(nPosition).isOccupied() || isReserved(reservedCells, nPosition)){
                                        commandQueue.add(ship.stayStill());
                                        entityData2.put(getPosKey(ship.position), goHome);
                                        targetData2.put(getPosKey(nPosition), me.shipyard.position);

                                        Log.log("стоять на месте" + getPosKey(ship.position));
                                        continue;
                                    }
                                    commandQueue.add(ship.move(randomDirection));
                                    reservedCells.add(gameMap.at(nPosition));
                                    Log.log("случайная позиция" + getPosKey(nPosition));
                                    //случайная позиция
                                    entityData2.put(getPosKey(nPosition), goHome);
                                    targetData2.put(getPosKey(nPosition), me.shipyard.position);
                                    continue;
                                }

                                Log.log("идти домой" + getPosKey(ship.position));
                                reservedCells.add(gameMap.at(nPosition));
                                entityData2.put(getPosKey(nPosition), goHome);
                                targetData2.put(getPosKey(nPosition), me.shipyard.position);
                                commandQueue.add(ship.move(direction));

                                continue;
                            }

                        }
                        break;
                        case goForHalite:
                        {
                            Log.log("ищу Халиты" + Data);

                            MapCell targetCell;
                            Position buffPosition = targetData1.get(ship.position);
                            if (buffPosition == null)
                            {
                                targetCell = getTargetCell(getNearCells(distance, me.shipyard, gameMap), ship, game, me.shipyard,  Constants.MAX_HALITE * 0.3, targetCells);
                                targetCells.add(targetCell);
                            } else {
                                targetCell = gameMap.at(buffPosition);
                            }

                            Integer state = 1;
                            if(ship.halite > Constants.MAX_HALITE * 0.7){
                                state = 2;
                                Log.log("иди домой" + getPosKey(ship.position));
                            } else {
                                if(gameMap.at(ship.position).halite > minPrice){
                                    Log.log("стоять на месте" + getPosKey(ship.position));
                                    commandQueue.add(ship.stayStill());
                                    entityData2.put(getPosKey(ship.position), state);
                                    targetData2.put(getPosKey(ship.position), (state == 2) ? targetCell.position:me.shipyard.position);
                                    continue;
                                }
                            }

                            if(targetCell.position.equals(ship.position)){

                                Log.log("стоять на месте" + getPosKey(ship.position));
                                commandQueue.add(ship.stayStill());
                                entityData2.put(getPosKey(ship.position), state);
                                targetData2.put(getPosKey(ship.position), (state == 2) ? targetCell.position:me.shipyard.position);
                                continue;
                            } else {
                                direction = gameMap.naiveNavigate(ship, targetCell.position);
                                Position nPosition = ship.position.directionalOffset(direction);
                                if(isReserved(reservedCells, nPosition) )
                                {
                                    if(targetCell.halite < minPrice)
                                    {
                                        final Direction randomDirection = Direction.ALL_CARDINALS.get(rng.nextInt(4));
                                        commandQueue.add(ship.move(randomDirection));
                                        nPosition = ship.position.directionalOffset(randomDirection);
                                        reservedCells.add(gameMap.at(nPosition));
                                        Log.log("случайная позиция" + getPosKey(nPosition));
                                        //случайная позиция
                                        entityData2.put(getPosKey(nPosition), state);
                                        targetData2.put(getPosKey(nPosition), (state == 2) ? targetCell.position:me.shipyard.position);
                                    }
                                    else
                                    {
                                        Log.log("стоять на месте" + getPosKey(nPosition));
                                        //стоять на месте
                                        commandQueue.add(ship.stayStill());
                                        entityData2.put(getPosKey(ship.position), state);
                                        targetData2.put(getPosKey(ship.position), (state == 2) ? targetCell.position:me.shipyard.position);
                                    }
                                    continue;
                                }
                                //двигаться в сторону таргета
                                Log.log("двигаться в сторону таргета" + getPosKey(nPosition));
                                Log.log("двигаться в сторону таргета" + getPosKey(targetCell.position));
                                entityData2.put(getPosKey(nPosition), state);
                                targetData2.put(getPosKey(nPosition), (state == 2) ? targetCell.position:me.shipyard.position);
                                reservedCells.add(gameMap.at(nPosition));
                            }
                        }
                        break;
                    }
                    if(direction != null){
                        commandQueue.add(ship.move(direction));
                    }
//                } else {
//                    commandQueue.add(ship.stayStill());
//                }
            }

            if (
                game.turnNumber <= 200 &&
                me.halite >= Constants.SHIP_COST &&
                !gameMap.at(me.shipyard).isOccupied())
            {
                commandQueue.add(me.shipyard.spawn());
            }
//            entityData1 = new HashMap<String, Integer>();
            entityData1 = (HashMap<String, Integer>) entityData2.clone();
            targetData1 = (HashMap<String, Position>) targetData2.clone();
//            entityData2.clone();

            game.endTurn(commandQueue);
        }
    }

    public static boolean isReserved(ArrayList<MapCell> cells, Position position){
        for (final MapCell cell : cells) {
            if(position.equals(cell.position)){
                return true;
            }
        }
        return false;
    }

    public static String getPosKey(Position position){
        return position.x + "_" + position.y;
    }
    public static int getFildSum(Position pose, GameMap map, int height, int width)
    {
        // x - width; y - height;
        int x = pose.x - (width / 2);
        int y = pose.y - (height / 2);
        int sum = 0;
        for (int i = x; i < x + width; i++)
        {
            for (int j = y; j < y + height; j++)
            {
                Position p = map.normalize(new Position(i, j));
                MapCell mc = map.at(p);
                sum = sum + mc.halite;
            }
        }
        return sum;
    }

    public static TreeMap<Integer, Position> getCells(GameMap map, int height, int width)
    {
        TreeMap<Integer, Position> tm = new TreeMap<Integer, Position>();
        for (int i = 0; map.width > i; i++)
        {
            for (int j = 0; map.height > j; j++)
            {
                Position p = new Position(i, j);
                tm.put(getFildSum(p, map, height, width), p);
            }
        }
        return tm;

    }

    public static List<MapCell> getNearCells(int distance, Entity entity, GameMap map){
        LinkedList<MapCell> nearCells = new LinkedList<>();
        for (final MapCell[] cell1 : map.cells) {
            for (final MapCell cell : cell1) {
                if (map.calculateDistance(cell.position, entity.position) < distance) {
                    nearCells.add(cell);
                }
            }
        }
        return nearCells;
    }

    public static MapCell getTargetCell(List<MapCell> cells, Ship ship, Game game, Entity base, double price, ArrayList<MapCell> targetCells){
        MapCell maxCell = null;
        final GameMap gameMap = game.gameMap;
        for (final MapCell cell : cells) {
            if (!targetCells.contains(cell)) {
                if (maxCell == null) {
                    maxCell = cell;
                }
                if (cell.halite > price && cell.halite > maxCell.halite - gameMap.calculateDistance(base.position, ship.position) * Constants.MAX_HALITE / 10) {
                    maxCell = cell;
                }
            }
        }
        return maxCell;
    }

    //public static MapCell[][] getBfs(Position startP, GameMap map, int distance){
    //    LinkedList<MapCell> stack = new LinkedList<>();
//
//        Position[][] positions = new Position[distance][];
//        for (int y = 0; y < distance; ++y) {
//            positions[y] = new Position[distance];
//         }
//         // хранить тут
//         //position.halite - 2 * map.calculateDistance(startP, position) * Constants.MAX_HALITE / 10
//         int[][] price = new int[distance][];
//         for (int y = 0; y < distance; ++y) {
//                     price[y] = new price[distance];
//         }

//         if(){}
//     }

//    public LocalPlaner()
//    {
//
//    }
}