package Learning;

import Model.Elements.Element;
import Model.Simulation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

public class Fitness implements Serializable {

	/**
	 * Ivo's algorithm for measuring encirclement. It spans out in 4 directions from each burning
	 * cell and counts the burnable (and not on fire or burnt out) cells until it reaches a
	 * non-burnable. Then, depending on the depth, it might split up and go down two more extra
	 * paths as well.
	 *
	 * Hitting the edge of the map increases cost by 100
	 * Travelling over a burnable increases cost by 1
	 *
	 */
	public class SPE_Measure {
		private HashSet<String> directions = new HashSet<>(Arrays.asList("N", "S", "E", "W"));
		private Simulation model;
		private int maxDepth;

		SPE_Measure(Simulation model) {
			this.model = model;
		}

		public int getFitness(int depth) {
			this.maxDepth = depth;
			int count = 0;
			for (Element f : model.getActiveCells()) {
				for (String d : directions) {
					count += goDownPath(f.getX(), f.getY(), d, 0);
				}
			}
			return count;
		}

		private int goDownPath(int x, int y, String direction, int currentDepth) {
			// change/update (x, y) according to direction
			int newX = getNewX(x, direction);
			int newY = getNewY(y, direction);
			// if we are out of bounds (= reached the edge of the map, bad thing), return a high cost
			if (!model.isInBounds(newX, newY)) {
				if (currentDepth == maxDepth) {
					return 100;
				// if we have not reached maximum depth, go down the two new paths as well
				} else {
					if (direction.equals("N") || direction.equals("S")) {
						goDownPath(newX, newY, "E", currentDepth + 1);
						goDownPath(newX, newY, "W", currentDepth + 1);
						return 100;
					}
					if (direction.equals("E") || direction.equals("W")) {
						goDownPath(newX, newY, "N", currentDepth + 1);
						goDownPath(newX, newY, "S", currentDepth + 1);
						return 100;
					}
				}
			}
			// if it is not a burnable (= reached a road or a river, good thing), return 0 cost
			if (!isBurnable(newX, newY)) {
				if (currentDepth == maxDepth) {
					return 0;
				} else {
					if (direction.equals("N") || direction.equals("S")) {
						goDownPath(newX, newY, "E", currentDepth + 1);
						goDownPath(newX, newY, "W", currentDepth + 1);
						return 0;
					}
					if (direction.equals("E") || direction.equals("W")) {
						goDownPath(newX, newY, "N", currentDepth + 1);
						goDownPath(newX, newY, "S", currentDepth + 1);
						return 0;
					}
				}
			}
			// otherwise return a cost of one, and continue down the same path/direction
			return 1 + goDownPath(newX, newY, direction, currentDepth);
		}

		// updates x if the direction is East or West
		private int getNewX(int x, String direction) {
			if (direction.equals("E")) {
				return x + 1;
			}
			if (direction.equals("W")) {
				return x - 1;
			}
			return x;
		}

		// updates y if the direction is North or South
		private int getNewY(int y, String direction) {
			if (direction.equals("S")) {
				return y + 1;
			}
			if (direction.equals("N")) {
				return y - 1;
			}
			return y;
		}

		// returns true if the tile at (x, y) is burnable and not burning or burnt out
		private boolean isBurnable(int x, int y) {
			Element e = model.getElementAt(x, y);
			return (e.getFuel() > 0
					&& e.isBurnable()
					&& !e.isBurning());
		}
	}
}
