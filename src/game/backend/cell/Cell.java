package game.backend.cell;

import game.backend.Grid;
import game.backend.element.Element;
import game.backend.element.Fruit;
import game.backend.element.Nothing;
import game.backend.move.Direction;

public class Cell {
	
	private final Grid grid;
	private final Cell[] around = new Cell[Direction.values().length];
	private Element content;
	private boolean isJailed = false;
	
	public Cell(Grid grid) {
		this.grid = grid;
		this.content = new Nothing();
	}

	public boolean isJailed() {
		return isJailed;
	}

	public void setJailed(boolean jailed) {
		isJailed = jailed;
	}

	/** Inicializa las celdas de al lado de la celda*/
	public void setAround(Cell up, Cell down, Cell left, Cell right) {
		this.around[Direction.UP.ordinal()] = up;
		this.around[Direction.DOWN.ordinal()] = down;
		this.around[Direction.LEFT.ordinal()] = left;
		this.around[Direction.RIGHT.ordinal()] = right;
	}

	/** Devuelve true si hay un candy o algo estable abajo */
	private boolean hasFloor() {
		return !around[Direction.DOWN.ordinal()].isEmpty();
	}

	/** Devuelve true si se puede mover */
	protected boolean isMovable(){
		return content.isMovable() && !isJailed;
	}

	/** Devuelve true si no hay nada solido en la celda */
	public boolean isEmpty() {
		return !content.isSolid();
	}

	public Element getContent() {
		return content;
	}

	/** Explota el contenido de la celda (si hay paquete o rayado hace esa explosion tambien), rellena esos lugares con Nothing */
	public void clearContent() {
		if (isMovable()) {
			Direction[] explosionCascade = content.explode();
			grid.cellExplosion(content);
			this.content = new Nothing();
			if (explosionCascade != null) {
				expandExplosion(explosionCascade); 
			}
			this.content = new Nothing();
		}
		if(isJailed){
			isJailed = false;
			getGrid().decrementJailsLeft();
		}
	}

	/** Por si hay paquete o rayado */
	private void expandExplosion(Direction[] explosion) {
		for(Direction d: explosion) {
			this.around[d.ordinal()].explode(d);
		}
	}

	/** explota lo que tenga que explotar */
	private void explode(Direction d) {
		if(!(content instanceof Fruit))
			clearContent();
		if (this.around[d.ordinal()] != null)
			this.around[d.ordinal()].explode(d);
	}

	/** Borra el contenido y devuelve lo que habia */
	protected Element getAndClearContent() {
		if (content.isMovable()) {
			Element ret = content;
			this.content = new Nothing();
			return ret;
		}
		return null;
	}

	/** Hace que caigan los candies */
	public boolean fallUpperContent() {
		Cell up = around[Direction.UP.ordinal()];
		if (this.isEmpty() && !up.isEmpty() && up.isMovable()) {
			this.content = up.getAndClearContent();
			grid.wasUpdated();
			if (this.hasFloor()) {
				grid.tryRemove(this);
				return true;
			} else {
				Cell down = around[Direction.DOWN.ordinal()];
				return down.fallUpperContent();
			}
		} 
		return false;
	}

	public void setContent(Element content) {
		this.content = content;
	}

	protected Grid getGrid(){
		return grid;
	}

}
