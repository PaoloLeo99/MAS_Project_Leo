package tile;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Tile {
    private final BufferedImage image;
    public static final int tileSize=32;
    private final String tileName;

    public Tile(BufferedImage image, String tileName) {
        this.image = image;
        this.tileName = tileName;
    }
    public Image getImage() {
        return image;
    }
    public String getTileName() {
        return tileName;
    }

}

