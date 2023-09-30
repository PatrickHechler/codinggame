package de.hechler.patrick.codingame.censored;

import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {
	
	public static void main(String[] args) {
		try (Scanner in = new Scanner(System.in)) {
			int firstInitInput  = in.nextInt();
			int secondInitInput = in.nextInt();
			int thirdInitInput  = in.nextInt();
			if (in.hasNextLine()) {
				in.nextLine();
			}
			
			System.err.println("init-a: " + firstInitInput);
			System.err.println("init-b: " + secondInitInput);
			System.err.println("init-c: " + thirdInitInput);
			
			int[]  vals     = new int[thirdInitInput * 2];
			int[]  lastvals = new int[thirdInitInput * 2];
			String lastCmd  = null;
			
			// game loop
			while (true) {
				int  value;
				char c = in.nextLine().charAt(0);
				System.err.println("last command: " + lastCmd);
				if (c != '#' && c != '_') System.err.println("unknown c: " + c);
				value = c == '#' ? 1 : 0;
				c     = in.nextLine().charAt(0);
				if (c != '#' && c != '_') System.err.println("unknown c: " + c);
				value |= c == '#' ? 2 : 0;
				c      = in.nextLine().charAt(0);
				if (c != '#' && c != '_') System.err.println("unknown c: " + c);
				value |= c == '#' ? 4 : 0;
				c      = in.nextLine().charAt(0);
				if (c != '#' && c != '_') System.err.println("unknown c: " + c);
				value |= c == '#' ? 8 : 0;
				System.err.println("value: " + value);
				for (int i = 0; i < thirdInitInput; i++) {
					vals[i * 2]     = in.nextInt();
					vals[i * 2 + 1] = in.nextInt();
				}
				in.nextLine();
				
				int sum  = 0;
				int suma  = 0;
				int sumb  = 0;
				int diff = 0;
				for (int i = 0; i < vals.length; i += 2) {
					int sdiff = Math.abs(vals[i] - firstInitInput);
					sdiff += Math.abs(vals[i + 1] - secondInitInput);
					int ssum = vals[i] + vals[i + 1];
                    suma += vals[i];
                    sumb += vals[i+1];
					System.err.println("[" + (i / 2) + "]: {" + lastvals[i] + " -> " + vals[i] + ", " + lastvals[i+1] + " -> "+ vals[i + 1] + "} sum: " + ssum + "  diff: " + sdiff);
					sum  += ssum;
					diff += sdiff;
				}
				System.err.println("complete sum: " + sum);
				System.err.println("complete sum.a: " + suma);
				System.err.println("complete sum.b: " + sumb);
				System.err.println("complete diff: " + diff);
				
				// Write an action using System.out.println()
				// To debug: System.err
				
				switch (3) {
				case 0 -> System.out.println(lastCmd = "A");
				case 1 -> System.out.println(lastCmd = "B");
				case 2 -> System.out.println(lastCmd = "C");
				case 3 -> System.out.println(lastCmd = "D");
				case 4 -> System.out.println(lastCmd = "E");
				default -> throw new AssertionError();
				}
				System.arraycopy(vals, 0, lastvals, 0, vals.length);
			}
		}
	}
	
}
