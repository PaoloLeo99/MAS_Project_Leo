package env;

import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import tile.Tile;
import tile.TileManager;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class envView extends GridWorldView {

    // A list containing the various layers of the map. Each value of a layer represent the tile that goes in that cell.
    private final List<int[][]> mapLayers;
    // Maps to each possible value in mapLayers map the corresponding Tile.
    private final Map<String, Tile> tiles;

    public envView(final envModel model, TileManager tileManager) {
        super(model, "myEnv", 640);
        this.setVisible(true);
        this.repaint();
        this.setResizable(false);

        mapLayers = tileManager.getMap();
        tiles = tileManager.getTiles();
    }

    //Draws a specific tile of a specific layer and returns the tile drawn.
    private Tile drawTile(Graphics g, int x, int y, int layer, boolean isGreen) {
        int n = mapLayers.get(layer)[y][x];
        String idx;
        if (isGreen) {
            idx= n+"_green";
        }else{
            idx= Integer.toString(n);
        }
        Tile t = tiles.get(idx);
        g.drawImage(t.getImage(),x*cellSizeW,y*cellSizeH,null);
        return t;
    }
    // Draws the specified Object in the position x,y.
    @Override
    public void draw(Graphics g, int x, int y, int object) {
        if (object == envModel.GRASS||object == envModel.OBST_FIX||object == envModel.HIDING||
                object == envModel.STREET || object == envModel.EDGE){
            drawCell(g,x,y);
        }
        if (object == envModel.OBST_REM){
            String idx = "802";
            Tile t = tiles.get(idx);
            g.drawImage(t.getImage(),x*cellSizeW,y*cellSizeH,null);
        }
        if (object == envModel.CLEARED){
            Tile t = drawTile(g,x,y, 2,true);
            String idx = t.getTileName();
            switch(idx) {
                case "558_green":
                    model.add(envModel.CLEARED, new Location(x,y-1));
                    break;
                case "540_green":
                    model.add(envModel.CLEARED, new Location(x,y-1));
                    model.add(envModel.CLEARED, new Location(x+1,y-1));
                    model.add(envModel.CLEARED, new Location(x+1,y));
                    break;
                case "530_green":
                    model.add(envModel.CLEARED, new Location(x+1,y));
                    break;
                case "528_green":
                    model.add(envModel.CLEARED, new Location(x,y-1));
                    model.add(envModel.CLEARED, new Location(x-1,y-1));
                    model.add(envModel.CLEARED, new Location(x-1,y));
                    break;
            }
        }
        if (object == envModel.GROUND){
            String idx = "ground";
            Tile t = tiles.get(idx);
            g.drawImage(t.getImage(),x*cellSizeW,y*cellSizeH,null);
        }
        if (object == envModel.AIR){
            String idx = "drone";
            Tile t = tiles.get(idx);
            g.drawImage(t.getImage(),x*cellSizeW,y*cellSizeH,null);
        }
        if (object == envModel.TARGET){
            String idx = "circle";
            Tile t = tiles.get(idx);
            g.drawImage(t.getImage(),x*cellSizeW,y*cellSizeH,null);
        }
    }
    // Used to draw the map tiles in a position, it goes through the layers and draws, for each layer, the tile that goes in x,y.
    private void drawCell(Graphics g, int x, int y) {
        for (int[][] layer : mapLayers) {
            String idx = Integer.toString(layer[y][x]);
            if (!idx.equals("0")){
                Tile t = tiles.get(idx);
                g.drawImage(t.getImage(),x*cellSizeW,y*cellSizeH,null);
            }
        }
    }
}
