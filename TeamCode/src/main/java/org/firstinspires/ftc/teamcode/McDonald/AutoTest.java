package org.firstinspires.ftc.teamcode.McDonald;

import static org.firstinspires.ftc.teamcode.shared.VisionHardware.PropPosition.LEFT;
import static org.firstinspires.ftc.teamcode.shared.VisionHardware.PropPosition.MIDDLE;

import androidx.annotation.VisibleForTesting;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.shared.MotionHardware;
import org.firstinspires.ftc.teamcode.shared.MotionHardware.Direction;
import org.firstinspires.ftc.teamcode.shared.VisionHardware;
import org.firstinspires.ftc.teamcode.shared.VisionHardware.PropPosition;

@Config
@Autonomous(name = "Auto - RA Right", group = "Auto")
public class AutoTest extends LinearOpMode {

    MotionHardware robot = new MotionHardware(this);
    VisionHardware vision = new VisionHardware(this);
    @Override
    public void runOpMode() throws InterruptedException {
        robot.init();
        vision.init();

        waitForStart();

        while(opModeIsActive()) {
            PropPosition propPosition = vision.detectProp();

            // Center Strike Line
            // - Forward: 31.75
            // - Reverse: -44.75

            switch(propPosition) {
                case UNKNOWN:
                    robot.moveRobot(.5, -20, 10);
                    //Pretend to drop pixel
                    sleep(1000);
                    //Backup and clear pixel
                    robot.moveRobot(.5, -5, 5);
                    sleep(1000);
                    //Turn to parking location
                    robot.turnRobot(Direction.LEFT, 12, .5, 10);
                    sleep(1000);
                    //Park
                    robot.moveRobot(.5, 25, 10);
                case RIGHT:
                    robot.moveRobot(.5, -44.75, 10);
                    //Pretend to drop pixel
                    sleep(1000);
                    //Backup and clear pixel
                    robot.moveRobot(.5, -5, 5);
                    sleep(1000);
                    //Turn to parking location
                    robot.turnRobot(Direction.LEFT, 12, .5, 10);
                    sleep(1000);
                    //Park
                    robot.moveRobot(.5, 25, 10);
            case LEFT:
                robot.moveRobot(.5, -10, 10);
                //Pretend to drop pixel
                sleep(1000);
                //Backup and clear pixel
                robot.moveRobot(.5, -5, 5);
                sleep(1000);
                //Turn to parking location
                robot.turnRobot(Direction.LEFT, 12, .5, 10);
                sleep(1000);
                //Park
                robot.moveRobot(.5, 25, 10);
            case MIDDLE:
            //Drop off pixel
            robot.moveRobot(.5, -30, 10);
            //Pretend to drop pixel
            sleep(1000);
            //Backup and clear pixel
            robot.moveRobot(.5, -5, 5);
            sleep(1000);
            //Turn to parking location
            robot.turnRobot(Direction.LEFT, 12, .5, 10);
            sleep(1000);
            //Park
            robot.moveRobot(.5, 25, 10);

            sleep(20);
            break;
        }
    }
}
