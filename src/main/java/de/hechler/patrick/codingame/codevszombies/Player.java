package de.hechler.patrick.codingame.codevszombies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Save humans, destroy zombies!
 **/
class Player {
	
	public static final Ash              ash     = new Ash();
	public static final Entities<Human>  humans  = new Entities<>();
	public static final Entities<Zombie> zombies = new Entities<>();
	
	public static void main(String[] args) {
		try (Scanner in = new Scanner(System.in)) {
			// game loop
			while (true) {
				ash.pos.x = in.nextInt();
				ash.pos.y = in.nextInt();
				readHumans(in);
				readZombies(in);
				
				// Write an action using System.out.println()
				// To debug: System.err
				
				chooseTarget();
				
				System.out.println(ash.target.x + " " + ash.target.y); // Your destination coordinates
			}
		}
	}
	
	private static final List<Zombie> dangerous = new ArrayList<>();
	
	private static void chooseTarget() {
		dangerous.clear();
		int minAshTurns = Integer.MAX_VALUE;
		for (Human h : humans) {
			h.dangers.clear();
			for (Zombie z : zombies) {
				h.dangers.put(z, z);
			}
			Zombie     nearest = h.dangers.firstKey();
			int        dist    = h.nextDistance(nearest);
			int        turns   = (dist + (Zombie.SPEED * 2 - 1)) / Zombie.SPEED; // multiply 2 because distance is from [0] (z.next)
			List<Position> zway    = way(nearest, h.pos, turns);
			for (int i = 0, s = zway.size(); i < s;) {
				Position zpos = zway.get(i);
				if (canKill(zpos, ++i) && i < minAshTurns) {
					System.err.println("estimatet target " + zpos + " reach is " + i + " turns");
					System.err.println("zombie way: " + zway);
					System.err.println("zombie: " + nearest);
					System.err.println("human: " + h);
					minAshTurns = i;
					ash.target  = zpos;
				}
			}
		}
		if (minAshTurns == Integer.MAX_VALUE) {
			System.err.println("there is no one I can save");
			Zombie z = zombies.iterator().next();
			ash.target.x = z.next.x;
			ash.target.y = z.next.y;
		}
	}
	
	private static boolean canKill(Position t, int i) {
		Position pos = ash.pos.wayVec(t, Ash.SPEED);
		int      vx  = pos.x;
		int      vy  = pos.y;
		pos.x += ash.pos.x;
		pos.y += ash.pos.y;
		while (--i >= 0) {
			if (pos.distSq(t) <= (Ash.SPEED + Ash.RANGE) * (Ash.SPEED + Ash.RANGE)) return true;
			pos.x += vx;
			pos.y += vy;
		}
		return false;
	}
	
	private static List<Position> way(Zombie z, Position t, int expectedTurns) {
		List<Position> way = new ArrayList<>(expectedTurns);
		int            x   = z.next.x;
		int            y   = z.next.y;
		Position       vec = z.next.wayVec(t, Zombie.SPEED);
		while (true) {
			Position pos = new Position(x, y);
			way.add(pos);
			if (pos.distSq(t) <= Zombie.SPEED * Zombie.SPEED) break;
			x += vec.x;
			y += vec.y;
		}
		return way;
	}
	
	private static void readHumans(Scanner in) {
		int humanCount = in.nextInt();
		humans.markDead(humanCount);
		while (--humanCount >= 0) {
			int humanId = in.nextInt();
			int humanX  = in.nextInt();
			int humanY  = in.nextInt();
			humans.updateEntity(Human.class, humanId, humanX, humanY);
		}
		humans.removeDead();
	}
	
	private static void readZombies(Scanner in) {
		int zombieCount = in.nextInt();
		zombies.markDead(zombieCount);
		while (--zombieCount >= 0) {
			int    zombieId = in.nextInt();
			int    zombieX  = in.nextInt();
			int    zombieY  = in.nextInt();
			Zombie z        = zombies.updateEntity(Zombie.class, zombieId, zombieX, zombieY);
			z.next.x = in.nextInt();
			z.next.y = in.nextInt();
		}
		zombies.removeDead();
	}
	
}

class Entities<E extends Entity> implements Iterable<E> {
	
	// map size:
	// width: 16000
	// height: 9000
	
	private static final int FACTOR = 100;
	
	private Object[][] arr = new Object[160][90];
	private Entity[] es;
	private int      aliveCount;
	
	@SuppressWarnings("unchecked")
	public E get(int id) {
		return (E) this.es[id];
	}
	
	public int aliveCount() {
		return this.aliveCount;
	}
	
	@Override
	public Iterator<E> iterator() {
		return new Iterator<>() {
			
			private int i;
			
			@Override
			public boolean hasNext() {
				while (this.i < Entities.this.es.length) {
					if (Entities.this.es[this.i] != null) return true;
					this.i++;
				}
				return false;
			}
			
			@Override
			@SuppressWarnings("unchecked")
			public E next() {
				while (this.i < Entities.this.es.length) {
					Entity e = Entities.this.es[this.i++];
					if (e != null) return (E) e;
				}
				throw new NoSuchElementException("no more elements");
			}
			
		};
	}
	
	public void markDead(int entityCount) {
		if (this.es != null) {
			if (entityCount > this.es.length) {
				for (Entity e : this.es) {
					if (e == null) continue;
					Position p = e.pos;
					int x = p.x / FACTOR;
					int y = p.y / FACTOR;
					Object val = this.arr[x][y];
					if (val == e) {
						this.arr[x][y] = null;
					} else {
						@SuppressWarnings("unchecked")
						List<Entity> l = (List<Entity>) val;
						if (!l.remove(e)) {
							throw new AssertionError();
						}
					}
				}
				System.err.println("recreate array (there are more than before now)");
				this.es = new Entity[entityCount];
			} else {
				for (Entity e : this.es) {
					if (e == null) continue;
					e.dead = true;
				}
			}
		} else {
			this.es = new Entity[entityCount];
		}
		this.aliveCount = entityCount;
	}
	
	public void removeDead() {
		for (int i = 0; i < this.es.length; i++) {
			Entity e = this.es[i];
			if (e == null) continue;
			if (e.pos.x == -1) this.es[i] = null;
		}
	}
	
	public E updateEntity(Class<E> cls, int id, int x, int y) {
		if (this.es[id] == null) {
			if (cls == Human.class) {
				this.es[id] = new Human(id, new Position(x, y));
			} else {
				this.es[id] = new Zombie(id, new Position(x, y), new Position(-1, -1));
			}
		} else {
			this.es[id].pos.x = x;
			this.es[id].pos.y = y;
		}
		return cls.cast(this.es[id]);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(Stream.of(this.es).filter(e -> e != null).toArray());
	}
	
}

class Position {
	
	int x;
	int y;
	
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		builder.append(this.x);
		builder.append('|');
		builder.append(this.y);
		builder.append(')');
		return builder.toString();
	}
	
	public int dist(Position other) {
		int diffx      = Math.abs(this.x - other.x);
		int diffy      = Math.abs(this.y - other.y);
		int diffSquare = diffy * diffy + diffx * diffx;
		return (int) Math.sqrt(diffSquare);
	}
	
	public int distSq(Position other) {
		int diffx = Math.abs(this.x - other.x);
		int diffy = Math.abs(this.y - other.y);
		return diffy * diffy + diffx * diffx;
	}
	
	public Position wayVec(Position target, int speed) {
		int    diffx     = target.x - this.x;
		int    diffy     = target.y - this.y;
		int    difflenSq = diffx * diffx + diffy * diffy;
		double difflen   = Math.sqrt(difflenSq);
		double dx        = (diffx / difflen) * speed;
		double dy        = (diffy / difflen) * speed;
		return new Position((int) Math.floor(dx), (int) Math.floor(dy));
	}
	
}

class Ash extends Human {
	
	static final int SPEED = 1000;
	static final int RANGE = 2000;
	
	Position target;
	
	public Ash() {
		super(-1, new Position(-1, -1));
		this.target = new Position(-1, -1);
	}
	
	@Override
	public Position nextPos() {
		if (this.target.x == -1) return this.pos;
		if (this.target.dist(this.pos) <= SPEED) return this.target;
		Position vec = this.pos.wayVec(this.target, SPEED);
		vec.x += this.pos.x;
		vec.y += this.pos.y;
		return vec;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Ash [target=");
		builder.append(this.target);
		builder.append(", pos=");
		builder.append(this.pos);
		builder.append("]");
		return builder.toString();
	}
	
}

class Zombie extends Entity {
	
	public static final int SPEED = 400;
	
	Position next;
	
	public Zombie(int id, Position pos, Position next) {
		super(id, pos);
		this.next = next;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Zombie [next=");
		builder.append(this.next);
		builder.append(", id=");
		builder.append(this.id);
		builder.append(", pos=");
		builder.append(this.pos);
		builder.append("]");
		return builder.toString();
	}
	
	@Override
	public Position nextPos() {
		return this.next;
	}
	
}

class Human extends Entity {
	
	public final NavigableMap<Zombie, Zombie> dangers = new TreeMap<>((a, b) -> {
		int at  = (this.nextDistance(a) + (Zombie.SPEED - 1)) / Zombie.SPEED;
		int bt  = (this.nextDistance(b) + (Zombie.SPEED - 1)) / Zombie.SPEED;
		int cmp = Integer.compare(at, bt);
		if (cmp != 0) return cmp;
		return Integer.compare(a.id, b.id);
	});
	
	public Human(int id, Position pos) {
		super(id, pos);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Human [id=");
		builder.append(this.id);
		builder.append(", pos=");
		builder.append(this.pos);
		builder.append("]");
		return builder.toString();
	}
	
	@Override
	public Position nextPos() {
		return this.pos;
	}
	
}

abstract class Entity {
	
	final int id;
	Position  pos;
	boolean dead;
	
	public Entity(int id, Position pos) {
		this.id  = id;
		this.pos = pos;
	}
	
	public int distance(Entity other) {
		return this.pos.dist(other.pos);
	}
	
	public int nextDistance(Entity other) {
		return this.nextPos().dist(other.nextPos());
	}
	
	public abstract Position nextPos();
	
	@Override
	public abstract String toString();
	
}
