package de.hechler.patrick.codingame.modpodracing;

import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player3 {

	@SuppressWarnings({"resource", "unused"})
    public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

        boolean boost = true;

        // game loop
        while (true) {
            int x = in.nextInt();
            int y = in.nextInt();
            int nextCheckpointX = in.nextInt(); // x position of the next check point
            int nextCheckpointY = in.nextInt(); // y position of the next check point
            int nextCheckpointDist = in.nextInt(); // distance to the next checkpoint
            int nextCheckpointAngle = in.nextInt(); // angle between your pod orientation and the direction of the next checkpoint
            int opponentX = in.nextInt();
            int opponentY = in.nextInt();

            // Write an action using System.out.println()
            // To debug: System.err
            System.err.println("nextCheckpointDist: " + nextCheckpointDist);
            System.err.println("nextCheckpointAngle: " + nextCheckpointAngle);
            System.err.println("nextCheckpointX: " + nextCheckpointX);
            System.err.println("nextCheckpointY: " + nextCheckpointY);

            if (boost) {
                 System.out.println(nextCheckpointX + " " + nextCheckpointY + " BOOST");
                boost = false;
                continue;
            }

            // You have to output the target position
            // followed by the power (0 <= thrust <= 100)
            // i.e.: "x y thrust"
             if (nextCheckpointAngle < -120 || nextCheckpointAngle > 120){
                System.out.println(nextCheckpointX + " " + nextCheckpointY + " 0");
            } else if (nextCheckpointDist < 500 || nextCheckpointAngle < -90 || nextCheckpointAngle > 90){
                System.out.println(nextCheckpointX + " " + nextCheckpointY + " 30");
            //} else if (nextCheckpointDist < 100 || nextCheckpointAngle < -60 || nextCheckpointAngle > 60){ //NOSONAR
            //  System.out.println(nextCheckpointX + " " + nextCheckpointY + " 60"); //NOSONAR
            } else {
                System.out.println(nextCheckpointX + " " + nextCheckpointY + " 100");
            }
        }
    }
}