package de.hechler.patrick.codingame.tictactoe.training.env;


@SuppressWarnings("javadoc")
public class TTTPos {
	
	public int outerX;
	public int outerY;
	
	public int innerX;
	public int innerY;
	
	@Override
	public int hashCode() {
		final int prime  = 31;
		int       result = 1;
		result = prime * result + this.innerX;
		result = prime * result + this.innerY;
		result = prime * result + this.outerX;
		result = prime * result + this.outerY;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof TTTPos)) return false;
		TTTPos other = (TTTPos) obj;
		if (this.innerX != other.innerX) return false;
		if (this.innerY != other.innerY) return false;
		if (this.outerX != other.outerX) return false;
		if (this.outerY != other.outerY) return false;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[outer: (");
		builder.append(this.outerX);
		builder.append('|');
		builder.append(this.outerY);
		builder.append(") -> inner: (");
		builder.append(this.innerX);
		builder.append('|');
		builder.append(this.innerY);
		builder.append(")]");
		return builder.toString();
	}
	
	
	
}
