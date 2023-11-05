package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous (name = "name")
public class Autonomous extends LinearOpMode {

    protected DcMotor left_front;
    protected DcMotor right_front;
    protected DcMotor left_back;
    protected DcMotor right_back;

    @Override
    public void runOpMode() throws InterruptedException {
        left_front = hardwareMap.get(DcMotor.class, "left_front");
        right_front = hardwareMap.get(DcMotor.class, "right_front");
        left_back = hardwareMap.get(DcMotor.class, "left_back");
        right_back = hardwareMap.get(DcMotor.class, "right_back");

        left_front.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        left_back.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        right_front.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        right_back.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        waitForStart();

        right_front.setPower(1);
        left_front.setPower(1);
        right_back.setPower(1);
        left_back.setPower(1);
        sleep(10000);
    }

    public void CoolestDrive(double forward, double strafe, double yaw, double speed){
        left_front.setTargetPosition((int) (forward + strafe + yaw));
        left_front.setTargetPosition((int) (forward - strafe - yaw));
        left_front.setTargetPosition((int) (forward - strafe + yaw));
        left_front.setTargetPosition((int) (forward + strafe - yaw));

        left_front.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        left_back.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        right_front.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        right_back.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        double leftFrontPower = speed * (forward + strafe + yaw);
        double rightFrontPower = speed * (forward - strafe - yaw);
        double leftBackPower = speed * (forward - strafe + yaw);
        double rightBackPower = speed * (forward + strafe - yaw);

        // Normalize the values so no wheel power exceeds 100%
        // This ensures that the robot maintains the desired motion.
        double max = Math.max(Math.abs(leftFrontPower), Math.abs(rightFrontPower));
        max = Math.max(max, Math.abs(leftBackPower));
        max = Math.max(max, Math.abs(rightBackPower));

        if (max > 1.0) {
            leftFrontPower /= max;
            rightFrontPower /= max;
            leftBackPower /= max;
            rightBackPower /= max;
        }
    }

    public void moveForward(int power, int time) {
        right_front.setPower(power);
        left_front.setPower(power);
        right_back.setPower(power);
        left_back.setPower(power);
        sleep(time);
        right_front.setPower(0);
        left_front.setPower(0);
        right_back.setPower(0);
        left_back.setPower(0);

    }

    public void moveStrafing(int power, int time){
        right_front.setPower(power);
        left_front.setPower(power);
        right_back.setPower(-power);
        left_back.setPower(-power);
        sleep(time);
        right_front.setPower(0);
        left_front.setPower(0);
        right_back.setPower(0);
        left_back.setPower(0);

    }

    public void moveTurning(int power, int time){
        right_front.setPower(-power);
        left_front.setPower(power);
        right_back.setPower(-power);
        left_back.setPower(power);
        sleep( time);
        right_front.setPower(0);
        left_front.setPower(0);
        right_back.setPower(0);
        left_back.setPower(0);
    }



   /*
    Strafing:
    lf +
    rf +
    lb -
    rb -
    Turning
    lf +
    rf -
    lb +
    rb -
     */

    //Drive the robot forward
}