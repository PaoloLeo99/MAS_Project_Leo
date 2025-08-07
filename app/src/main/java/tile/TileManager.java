package tile;

import env.envModel;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;
import java.util.List;

public class TileManager {
    //Map a String id to a tile. The id is the name of the tile in the folder.
    Map<String, Tile> tileImages = new HashMap<>();
    //Tiles folder path
    String folderPath;
    //Describe which tiles go in which cell. Since multiple tiles can go in a cell, multiple layers are needed.
    List<int[][]> mapLayers;
    envModel model;
    //Used by the model to instantiate the right object in the right cell.
    int[][] bitmap = new int[envModel.gridSize][envModel.gridSize];
    //The objects in the bitmap have not the same mask used in the model. Their relation is specified in the
    //bitmap.txt itself and the map intToMask keeps track of it.
    Map<Integer, Integer> intToMask = new HashMap<>();

    public TileManager(final String tilesFolderPath, final envModel model, String mapName) {
        this.model=model;
        this.folderPath=this.getClass().getResource(tilesFolderPath).getPath();
        mapLayers = new ArrayList<>();

        loadMap(mapName);
        getTileImage();
        loadBitMap(mapName);
    }

    //Populates the tileImages map. Accesses the tiles folder and for each of them creates an entry in the map.
    public void getTileImage() {
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files!=null) {
            for (int i=0;i<files.length;i++) {
                String filename = files[i].getName();
                String idx = filename.substring(0, filename.lastIndexOf('.'));
                try {
                    Tile t = new Tile(ImageIO.read(files[i]), idx);
                    tileImages.put(idx, t);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    //Converts a layer represented as a list of String (each String is a map layer row) to a matrix representation
    private int[][] parseLayer(List<String> lines) {
        int rows = lines.size();
        int cols = lines.get(0).split(",").length;
        int[][] map = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            String[] tokens = lines.get(i).split(",");
            for (int j = 0; j < cols; j++) {
                map[i][j] = Integer.parseInt(tokens[j].trim());
            }
        }
        return map;
    }

    //Loads the map layer by layer. In the map.txt layers are separated with a ".". Each layer is first read from
    //the stream as a list of String, then it is converted in matrix form.
    public void loadMap(String mapName){
        String mapPath = this.getClass().getResource("/maps/"+mapName+".txt").getPath();
        try  {
            File mapFile = new File(mapPath);
            Scanner scanner = new Scanner(mapFile);
            List<String> currentLines = new ArrayList<>();
            while (scanner.hasNextLine()) {
                //read a line
                String line = scanner.nextLine().trim();
                //if it is "." the whole layer has been read, convert it. Otherwise, add the line to the list
                if (line.equals(".")) {
                    if (!currentLines.isEmpty()) {
                        int[][] layer = parseLayer(currentLines);
                        mapLayers.add(layer);
                        currentLines.clear();
                    }
                } else if (!line.isEmpty()) {
                    currentLines.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //Initialize both the bitmap and the intToMask map. Both of them are in the bitmap.txt file and are separated by a "."
    private void loadBitMap(String mapName) {
        String bitMapPath = this.getClass().getResource("/maps/"+mapName+"_bitmap.txt").getPath();
        try  {
            File mapFile = new File(bitMapPath);
            Scanner scanner = new Scanner(mapFile);
            //read the masks correspondences and add them to the map
            String line = scanner.nextLine().trim();
            while (!line.equals(".")) {
                String[] tokens = line.split(",");
                int x = Integer.parseInt(tokens[0]);
                int y = Integer.parseInt(tokens[1]);
                intToMask.put(x, y);
                line = scanner.nextLine().trim();
            }
            //read the bitmap, same logic of a single map layer
            List<String> currentLines = new ArrayList<>();
            while (scanner.hasNextLine()) {
                line = scanner.nextLine().trim();
                if (line.equals(".")) {
                    if (!currentLines.isEmpty()) {
                        bitmap = parseLayer(currentLines);
                    }
                } else if (!line.isEmpty()) {
                    currentLines.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<int[][]> getMap() {
        return mapLayers;
    }
    public Map<String, Tile> getTiles() {
        return tileImages;
    }
    public int[][] getBitMap() {
        return bitmap;
    }
    public Map<Integer, Integer> getIntToMask() {
        return intToMask;
    }
}

