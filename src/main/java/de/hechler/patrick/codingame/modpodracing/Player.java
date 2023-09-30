package de.hechler.patrick.codingame.modpodracing;

import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		try (Scanner in = new Scanner(System.in)) {
			
			boolean boost = true;
			
			int lastx = -1;
			int lasty = -1;
			// game loop
			while (true) {
				int x                   = in.nextInt();
				int y                   = in.nextInt();
				int nextCheckpointX     = in.nextInt(); // x position of the next check point
				int nextCheckpointY     = in.nextInt(); // y position of the next check point
				int nextCheckpointDist  = in.nextInt(); // distance to the next checkpoint
				int nextCheckpointAngle = in.nextInt(); // angle between your pod orientation and the direction of the next checkpoint
				int opponentX           = in.nextInt();
				int opponentY           = in.nextInt();
				
				// Write an action using System.out.println()
				// To debug: System.err
				
				int speedx = x - lastx;
				int speedy = y - lasty;
				
				int diffx = nextCheckpointX - x;
				int diffy = nextCheckpointY - y;
				
				double mulx = diffx / (double) speedx;
				double muly = diffy / (double) speedy;
				
				double muldif  = Math.abs(mulx - muly);
				
				System.err.println("muldif: " + muldif);
				
				System.err.println("speed: " + Math.sqrt(speedx * speedx + speedy * speedy));
				
				System.err.println("mulx: " + mulx);
				System.err.println("muly: " + muly);
				
				System.err.println("diffx: " + speedx);
				System.err.println("diffy: " + speedy);
				
				System.err.println("nextCheckpointDist: " + nextCheckpointDist);
				System.err.println("nextCheckpointAngle: " + nextCheckpointAngle);
				System.err.println("nextCheckpointX: " + nextCheckpointX);
				System.err.println("nextCheckpointY: " + nextCheckpointY);
				
				// You have to output the target position
				// followed by the power (0 <= thrust <= 100)
				// i.e.: "x y thrust"
				
				if (boost) {
					System.out.println(nextCheckpointX + " " + nextCheckpointY + " BOOST");
					boost = false;
				} else if (x == lastx && y == lasty) {
					System.out.println(nextCheckpointX + " " + nextCheckpointY + " 100");
				} else if (nextCheckpointAngle < -90 || nextCheckpointAngle > 90
						|| muldif > 10d && Math.sqrt(speedx * speedx + speedy * speedy) > 300 && nextCheckpointDist < 2000) {
					System.out.println(nextCheckpointX + " " + nextCheckpointY + " 0");
				} else if (nextCheckpointDist < 500 || nextCheckpointAngle < -70 || nextCheckpointAngle > 70 
						|| muldif > 40d && Math.sqrt(speedx * speedx + speedy * speedy) > 700 && nextCheckpointDist > 5000) {
					System.out.println(nextCheckpointX + " " + nextCheckpointY + " 30");
				} else {
					System.out.println(nextCheckpointX + " " + nextCheckpointY + " 100");
				}
				lastx = x;
				lasty = y;
			}
		}
	}
	
}
