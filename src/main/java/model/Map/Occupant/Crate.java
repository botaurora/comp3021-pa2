package model.Map.Occupant;

/**
 * A class representing a crate that can be pushed around on the tiles
 */
public class Crate extends Occupant implements Cloneable {

    /**
     * @param r The row position of the crate
     * @param c The column position of the crate
     */
    public Crate(int r, int c) {
        super(r, c);
    }

    @Override
    public Crate clone() {
        return new Crate(getR(), getC());
    }
}
