package org.firstinspires.ftc.teamcode.Components.RFModules.Devices;

import static org.firstinspires.ftc.teamcode.Old.FreightFrenzy.Robots.BlackoutRobot.logger;
import static org.firstinspires.ftc.teamcode.Robots.BasicRobot.op;
import static org.firstinspires.ftc.teamcode.Robots.BasicRobot.time;
import static org.firstinspires.ftc.teamcode.roadrunner.drive.PoseStorage.currentPos;
import static org.firstinspires.ftc.teamcode.roadrunner.drive.PoseStorage.currentVelocity;
import static java.lang.Double.max;
import static java.lang.Double.min;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import android.util.Log;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.Robots.BasicRobot;
import org.firstinspires.ftc.teamcode.roadrunner.drive.RFMotorPoseSim;

import java.util.ArrayList;
import java.util.Arrays;

@Config
public class RFMotor extends Motor {
    private DcMotorEx rfMotor = null;
    private ArrayList<Double> coefs = null;
    private ArrayList<Double> coefs2 = null;
    private ArrayList<String> inputlogs = new ArrayList<>();
    public static double D = 0.00000, D2 = 0, kP = 5E-4, kI, kV = 0.005, kA = 0.0001, kR = 0, kS = 0.15,
            MAX_VELOCITY = 1 / kP, MAX_ACCELERATION = 11000, RESISTANCE = 400;
    private double relativeDist, peakVelo, J, decelDist;
    private double[][] calculatedIntervals = new double[4][8];
    private double[][][] calculatedMotions = new double[3][7][5];;
    private double[] timeIntervals = new double[8];
    private double[] distances = new double[8];
    private double[] velocities = new double[8];
    private double[] positions = new double[8];
    private double maxtickcount = 0;
    private double mintickcount = 0;
    private double DEFAULTCOEF1 = 0.0001, DEFAULTCOEF2 = 0.01;
    private double lastError = 0, lastTime = 0;
    private double additionalTicks = 0;
    private double TICK_BOUNDARY_PADDING = 10, TICK_STOP_PADDING = 20;
    private double power = 0, position = 0, velocity = 0, targetPos = 0, resistance = 0, acceleration = 0, avgResistance;
    private String rfMotorName;
    private RFMotorPoseSim poseSim = new RFMotorPoseSim();
    private boolean isSim = false;

    /*Initializes the motor
        Inputs:
        motorName: the name of the device | Ex:'motorRightFront'
        motorDirection: the direction of the motor | 0 for Reverse, 1 for Forward | Ex: 0
     */

    //for motors used for complex functions

    public RFMotor(String motorName, DcMotorSimple.Direction motorDirection, DcMotor.RunMode runMode,
                   boolean resetPos, ArrayList<Double> coefficients,
                   double maxtick, double mintick) {
        rfMotor = (DcMotorEx) op.hardwareMap.dcMotor.get(motorName);
        rfMotor.setDirection(motorDirection);
        if (resetPos) {
            rfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
        rfMotor.setMode(runMode);
        coefs = coefficients;
        maxtickcount = maxtick;
        mintickcount = mintick;

        logger.createFile("/MotorLogs/RFMotor" + motorName, "Runtime    Component               " +
                "Function               Action");
        rfMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        additionalTicks = 0;
    }

    //same as above but assuming motor direction is foward
    public RFMotor(String motorName, DcMotor.RunMode runMode, boolean resetPos,
                   ArrayList<Double> coefficients, double maxtick, double mintick) {
        rfMotor = (DcMotorEx) op.hardwareMap.dcMotor.get(motorName);
        rfMotorName = motorName;
        if (resetPos) {
            rfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
        rfMotor.setMode(runMode);
        coefs = coefficients;
        maxtickcount = maxtick;
        mintickcount = mintick;

        logger.createFile("/MotorLogs/RFMotor" + motorName, "Runtime    Component               " +
                "Function               Action");
        rfMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        additionalTicks = 0;


    }


    //same as above but using default coefficients
    public RFMotor(String motorName, DcMotor.RunMode runMode, boolean resetPos,
                   double maxtick, double mintick) {
        rfMotor = (DcMotorEx) op.hardwareMap.dcMotor.get(motorName);
        rfMotorName = motorName;
        if (resetPos) {
            rfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
        rfMotor.setMode(runMode);
        coefs = new ArrayList<>();
        coefs.add(DEFAULTCOEF1);
        coefs.add(DEFAULTCOEF2);
        maxtickcount = maxtick;
        mintickcount = mintick;

        logger.createFile("/MotorLogs/RFMotor" + rfMotorName, "Runtime    Component               " +
                "Function               Action");
        rfMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

    }

    //for chassis wheels where you only need it to spin continuously
    public RFMotor(String motorName, DcMotor.RunMode runMode, boolean resetPos) {
        rfMotor = (DcMotorEx) op.hardwareMap.dcMotor.get(motorName);
        rfMotorName = motorName;
        if (resetPos) {
            rfMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
        rfMotor.setMode(runMode);

        logger.createFile("/MotorLogs/RFMotor" + motorName, "Runtime    Component               " +
                "Function               Action");
        rfMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    public void setDirection(DcMotorSimple.Direction direction) {
        rfMotor.setDirection(direction);
    }

    public void setVelToAnalog(double velToAnalog) {
        kP = velToAnalog;
    }

    public void setCurrentPosition(double position) {
        additionalTicks = position - rfMotor.getCurrentPosition();
    }

    public void update(double target){
        poseSim.updateSim();
        setPosition(target, 0.5);
        poseSim.getTargets(getTargetPosition(BasicRobot.time), setSimPosition(target, 0.5)[1]);
    }

    public void setPosition(double p_targetPos, double curve) {
        power = 0;
        double[] targetMotion = getTargetMotion(curve);
        double currentTargetPos = getTargetPosition(BasicRobot.time);
        double currentTargetVelo = getTargetVelocity(BasicRobot.time);
        if (p_targetPos > maxtickcount) {
            p_targetPos = maxtickcount;
        }
        if (p_targetPos < mintickcount) {
            p_targetPos = mintickcount;
        }
        position = getCurrentPosition();
        targetPos = p_targetPos;
        acceleration = getVelocity() - velocity;
        velocity += acceleration;
        acceleration /= (time - lastTime);
        getAvgResistance();
        double power = (kV * targetMotion[0] + kA * targetMotion[1] + kP * (currentTargetPos - getCurrentPosition())
                + kI * (currentTargetVelo - getVelocity()));
        if (abs(targetPos - position) > TICK_BOUNDARY_PADDING && abs(velocity) < 3) {
            if (power < 0) {
                power -= kS;
            } else {
                power += kS;
            }
        }
        setRawPower(power);
        lastTime = time;
    }

    public double[] setSimPosition(double p_targetPos, double curve) {
        if (p_targetPos > maxtickcount) {
            p_targetPos = maxtickcount;
        }
        if (p_targetPos < mintickcount) {
            p_targetPos = mintickcount;
        }
        position = currentPos;
        targetPos = p_targetPos;
        lastTime = time;
        isSim = true;

        return getTargetMotion(curve);
    }

    public double getTargetPosition(double p_time) {
        double currentTargetPos = 0;
        for (int i = 0; i < 7; i++) {
            if (p_time > timeIntervals[i] && p_time < timeIntervals[i + 1]) {
                for (int j = 0; j < 4; j++) {
                    if (calculatedMotions[2][i][4] != -1) {
                        if (calculatedMotions[2][i][j] != -1) {
                            currentTargetPos += pow(p_time - calculatedMotions[2][i][4], j) * calculatedMotions[2][i][j];
                        }
                    }
                    else {
                        if (calculatedMotions[2][i][j] != -1) {
                            currentTargetPos += pow(p_time, j) * calculatedMotions[2][i][j];
                        }
                    }
                }
                break;
            }
        }
        return currentTargetPos;
    }

    public double getTargetVelocity(double p_time) {
        double currentTargetVelo = 0;
        for (int i = 0; i < 7; i++) {
            if (p_time > timeIntervals[i] && p_time < timeIntervals[i + 1]) {
                for (int j = 0; j < 4; j++) {
                    if (calculatedMotions[1][i][4] != -1) {
                        if (calculatedMotions[1][i][j] != -1) {
                            currentTargetVelo += pow(p_time - calculatedMotions[1][i][4], j) * calculatedMotions[1][i][j];
                        }
                    }
                    else {
                        if (calculatedMotions[1][i][j] != -1) {
                            currentTargetVelo += pow(p_time, j) * calculatedMotions[1][i][j];
                        }
                    }
                }
                break;
            }
        }
        return currentTargetVelo;
    }

    public double getTargetAcceleration(double p_time) {
        double currentTargetAccel = 0;
        for (int i = 0; i < 7; i++) {
            if (p_time > timeIntervals[i] && p_time < timeIntervals[i + 1]) {
                for (int j = 0; j < 4; j++) {
                    if (calculatedMotions[0][i][4] != -1) {
                        if (calculatedMotions[0][i][j] != -1) {
                            currentTargetAccel += pow(p_time - calculatedMotions[0][i][4], j) * calculatedMotions[0][i][j];
                        }
                    }
                    else {
                        if (calculatedMotions[0][i][j] != -1) {
                            currentTargetAccel += pow(p_time, j) * calculatedMotions[0][i][j];
                        }
                    }
                }
                break;
            }
        }
        return currentTargetAccel;
    }

    public void setTargetPos(double p_targetPos) {
        targetPos = p_targetPos;
    }

    public double[] getTimeIntervals() {
        return timeIntervals;
    }

    public double getPeakVelo() {
        return peakVelo;
    }

    public double getJ() {
        return J;
    }

    public double getStartingVelo() {
        return velocities[0];
    }

    public void setPosition(double p_targetPos) {
        power = 0;
        if (p_targetPos > maxtickcount) {
            p_targetPos = maxtickcount;
        }
        if (p_targetPos < mintickcount) {
            p_targetPos = mintickcount;
        }
        position = getCurrentPosition();
        targetPos = p_targetPos;
        acceleration = getVelocity() - velocity;
        velocity += acceleration;
        acceleration /= (time - lastTime);
        getAvgResistance();
        double[] targetMotion = getTargetMotion();
        double power = (kP * (targetMotion[0] - resistance) - kA * targetMotion[1]);

        if (abs(targetPos - position) > TICK_BOUNDARY_PADDING && abs(velocity) < 3) {
            if (power < 0) {
                power -= kS;
            } else {
                power += kS;
            }
        }
        setRawPower(power);
//        TelemetryPacket data = new TelemetryPacket();
//        data.put("decelDist", getDecelDist());
//        data.put("dist", targetPos - position);
//        data.put("targetVelocity", targetMotion[0]);
//        data.put("velocity", velocity);
//        data.put("targetAcceleration", targetMotion[1]);
//        data.put("trueAcceleration", acceleration);
//        data.put("resistance", resistance);
//        data.put("power", power);
////        logger.log("/RobotLogs/GeneralRobot", "liftingTo" +targetPos);
//
//        BasicRobot.dashboard.sendTelemetryPacket(data);
        lastTime = time;
    }

    public double[] getTargetMotion(double curve) {
        double[] targets = {0, 0};
        relativeDist = targetPos - position;
        double direction = abs(relativeDist) / relativeDist;

        if (relativeDist < 0) {
            velocities[0] = -rfMotor.getVelocity();
        }
        if (isSim) {
            velocities[0] = currentVelocity;
            isSim = false;
        }

        if (velocities[0] == 0) {
            peakVelo = min((131 - 38 * curve)/131 * sqrt(MAX_ACCELERATION * relativeDist), MAX_VELOCITY);
        }
        else {
            peakVelo = min((131 - 38 * curve)/131 * sqrt(MAX_ACCELERATION * (relativeDist -
                    abs(velocities[0])/velocities[0] * pow(velocities[0], 2)/(2 * MAX_ACCELERATION))), MAX_VELOCITY);
        }

        velocities[0] = min(velocities[0], peakVelo);

        J = MAX_ACCELERATION / ((peakVelo / (MAX_ACCELERATION * (1 - curve / 2))) * curve / 2);

        calculateIntervals();
        calculateMotions();
        timeIntervals = calculatedIntervals[0];
        distances = calculatedIntervals[1];
        velocities = calculatedIntervals[2];
        positions = calculatedIntervals[3];
        decelDist = calculatedIntervals[3][7] - calculatedIntervals[3][4];

        targets[0] = getTargetVelocity(BasicRobot.time);
        targets[1] = getTargetAcceleration(BasicRobot.time);

        return targets;
    }

    public void calculateIntervals() {
        double[] timeIntervals = new double[8];
        double cruiseTime;
        double[] distances = new double[8];
        double semiTotal;
        double cruiseAccelTime;
        double[] velocities = new double[8];
        double[] positions = new double[8];

        timeIntervals[0] = 0;
        timeIntervals[2] = max(sqrt((peakVelo - velocities[0]) / J), (max(0, (peakVelo - velocities[0])
                / MAX_ACCELERATION)));
        timeIntervals[1] = min(timeIntervals[2], MAX_ACCELERATION / J);
        timeIntervals[3] = timeIntervals[1] + timeIntervals[2];

        cruiseAccelTime = timeIntervals[2] - timeIntervals[1];

        velocities[1] = velocities[0] + J * pow(timeIntervals[1], 2) / 2;
        velocities[2] = velocities[1] + MAX_ACCELERATION * cruiseAccelTime;
        velocities[3] = peakVelo;
        velocities[4] = peakVelo;
        velocities[5] = velocities[4] - J * pow(MAX_ACCELERATION / J, 2) / 2;
        velocities[6] = J * pow(MAX_ACCELERATION / J, 2) / 2;
        velocities[7] = 0;

        distances[0] = 0;
        distances[1] = J * pow(timeIntervals[1], 3) / 6 + velocities[0] * timeIntervals[1];
        distances[2] = velocities[1] * cruiseAccelTime + MAX_ACCELERATION * pow(cruiseAccelTime, 2) / 2;
        distances[3] = velocities[2] * timeIntervals[1] + J * pow(timeIntervals[1], 3) / 6;
        distances[5] = velocities[4] * MAX_ACCELERATION / J - J * pow(MAX_ACCELERATION / J, 3) / 6;
        distances[6] = velocities[5] * (peakVelo / MAX_ACCELERATION - MAX_ACCELERATION / J) -
                MAX_ACCELERATION * pow(peakVelo / MAX_ACCELERATION - MAX_ACCELERATION / J, 2) / 2;
        distances[7] = J * pow(MAX_ACCELERATION / J, 3) / 6;

        semiTotal = distances[1] + distances[2] + distances[3] + distances[5] + distances[6] + distances[7];

        if (relativeDist < 0) {
            cruiseTime = (-relativeDist - semiTotal) / peakVelo;
        }
        else {
            cruiseTime = (relativeDist - semiTotal) / peakVelo;
        }

        timeIntervals[4] = timeIntervals[3] + cruiseTime;
        timeIntervals[5] = timeIntervals[4] + MAX_ACCELERATION / J;
        timeIntervals[6] = timeIntervals[4] + peakVelo / MAX_ACCELERATION;
        timeIntervals[7] = timeIntervals[6] + MAX_ACCELERATION / J;

        distances[4] = velocities[3] * cruiseTime;

        positions[0] = 0;
        positions[1] = positions[0] + distances[1];
        positions[2] = positions[1] + distances[2];
        positions[3] = positions[2] + distances[3];
        positions[4] = positions[3] + distances[4];
        positions[5] = positions[4] + distances[5];
        positions[6] = positions[5] + distances[6];
        positions[7] = positions[6] + distances[7];

        calculatedIntervals[0] = timeIntervals;
        calculatedIntervals[1] = distances;
        calculatedIntervals[2] = velocities;
        calculatedIntervals[3] = positions;

    }

    public void calculateMotions() {
        double[][] acceleration = new double[7][5];
        double[][] velocity = new double[7][5];
        double[][] position = new double[7][5];

        for (double[] type : acceleration) {
            Arrays.fill(type, -1);
        }

        for (double[] type : velocity) {
            Arrays.fill(type, -1);
        }

        for (double[] type : position) {
            Arrays.fill(type, -1);
        }

        acceleration[0][1] = J;

        acceleration[1][0] = MAX_ACCELERATION * calculatedIntervals[0][1];

        acceleration[2][1] = -J;
        acceleration[2][4] = calculatedIntervals[0][3];

        acceleration[4][1] = -J;
        acceleration[4][4] = calculatedIntervals[0][4];

        acceleration[5][0] = -MAX_ACCELERATION;

        acceleration[6][1] = J;
        acceleration[6][4] = calculatedIntervals[0][7];

        velocity[0][0] = calculatedIntervals[2][0];
        velocity[0][2] = J / 2;

        velocity[1][0] = calculatedIntervals[2][1];
        velocity[1][1] = MAX_ACCELERATION;
        velocity[1][4] = calculatedIntervals[0][1];

        velocity[2][0] = calculatedIntervals[2][2] + J / 2 * pow(calculatedIntervals[0][1], 2);
        velocity[2][2] = -J / 2;
        velocity[2][4] = calculatedIntervals[0][3];

        velocity[3][0] = calculatedIntervals[2][3];

        velocity[4][0] = calculatedIntervals[2][4];
        velocity[4][2] = -J / 2;
        velocity[4][4] = calculatedIntervals[0][4];

        velocity[5][0] = calculatedIntervals[2][5];
        velocity[5][1] = -MAX_ACCELERATION;
        velocity[5][4] = calculatedIntervals[0][5];

        velocity[6][2] = J / 2;
        velocity[6][4] = calculatedIntervals[0][7];


        position[0][0] = calculatedIntervals[3][0];
        position[0][1] = calculatedIntervals[2][0];
        position[0][3] = J / 6;

        position[1][0] = calculatedIntervals[3][1];
        position[1][1] = calculatedIntervals[2][1];
        position[1][2] = MAX_ACCELERATION / 2;
        position[1][4] = calculatedIntervals[0][1];

        position[2][0] = calculatedIntervals[3][2];
        position[2][1] = calculatedIntervals[2][2];
        position[2][3] = J / 6;
        position[2][4] = calculatedIntervals[0][2];

        position[3][0] = calculatedIntervals[3][3];
        position[3][1] = calculatedIntervals[2][3];
        position[3][4] = calculatedIntervals[0][3];

        position[4][0] = calculatedIntervals[3][4];
        position[4][1] = calculatedIntervals[2][4];
        position[4][3] = -J / 6;
        position[4][4] = calculatedIntervals[0][4];

        position[5][0] = calculatedIntervals[3][5];
        position[5][1] = calculatedIntervals[2][5];
        position[5][2] = -MAX_ACCELERATION / 2;
        position[5][4] = calculatedIntervals[0][5];

        position[6][0] = calculatedIntervals[3][6];
        position[6][3] = J / 6;
        position[6][4] = calculatedIntervals[0][7];

        calculatedMotions[0] = acceleration;
        calculatedMotions[1] = velocity;
        calculatedMotions[2] = position;

    }

    public double[] getTargetMotion() {
        double[] targets = {0, 0};
        double DECEL_DIST = getDecelDist(), distance = targetPos - position;
        double direction = abs(distance) / distance;
        if (abs(distance) > DECEL_DIST && abs(velocity) < MAX_VELOCITY - RESISTANCE * direction - 0.1 * MAX_ACCELERATION) {
            if (distance > 0) {
                targets[0] = velocity + .1 * MAX_ACCELERATION * (1 - 1 / (abs(distance - DECEL_DIST) / 100 + 1));
                targets[1] = velocity - targets[0];
            } else {
                targets[0] = velocity - 0.1 * MAX_ACCELERATION * (1 - 1 / (abs(distance - DECEL_DIST) / 100 + 1));
                targets[1] = velocity - targets[0];
            }
        } else if (abs(distance) > DECEL_DIST && abs(distance) > 20) {
            if (distance > 0) {
                targets[0] = MAX_VELOCITY - RESISTANCE * direction;
                targets[1] = velocity - targets[0];
            } else {
                targets[0] = -MAX_VELOCITY - RESISTANCE * direction;
                targets[1] = velocity - targets[0];

            }
        } else {
            if (distance < 0) {
                targets[0] = min(-pow((abs(distance)) * (MAX_ACCELERATION - RESISTANCE * direction), 0.5), 0);
                targets[1] = velocity - targets[0];
            } else {
                targets[0] = max(pow((abs(distance)) * (MAX_ACCELERATION - RESISTANCE * direction), 0.5), 0);
                targets[1] = velocity - targets[0];
            }
        }
        return targets;
    }

    public double getTarget() {
        return targetPos;
    }

    public double getResistance() {
        double resistance = 0;
        resistance -= 200 + 0.4 * position - 0.00012 * position * position;
        resistance -= velocity * 0.3 * pow(abs(position) + 1, -.12);
        return -RESISTANCE;
    }

    public void getAvgResistance() {
        double resistances = 0;
        resistances -= RESISTANCE /* - 0.000135* position * position*/;
//        resistances -= velocity * 0.2 * pow(abs(position) + 1, -.13);
//        resistance = resistances* VOLTAGE_CONST;
        resistances -= RESISTANCE/* - 0.000135 * targetPos * targetPos*/;
        resistance = -RESISTANCE;
        avgResistance = -RESISTANCE;
    }

    public double getDecelDist() {
        double decelDist = 0;
        if (velocity > 0) {
            decelDist = 0.7 * pow(abs(velocity), 2) / (MAX_ACCELERATION - avgResistance);
        } else {
            decelDist = 0.7 * pow(abs(velocity), 2) / (MAX_ACCELERATION + avgResistance);
        }
        return decelDist;
    }

    public boolean atTargetPosition() {
        return abs(position - targetPos) < TICK_STOP_PADDING;
    }

    public void setPower(double power) {
        rfMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        getAvgResistance();
//        logger.log("/RobotLogs/GeneralRobot", rfMotorName + ",setPower():,Setting Power: " + power, false, false);
        rfMotor.setPower(power - kP * resistance);
//        logger.log("/MotorLogs/RFMotor" + rfMotorName, "Setting Power," + (power - kP * getResistance()), false, false);

    }

    public void setRawPower(double power) {
        rfMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        getAvgResistance();
//        logger.log("/RobotLogs/GeneralRobot", rfMotorName + ",setPower():,Setting Power: " + power, false, false);
        rfMotor.setPower(power);
//        logger.log("/MotorLogs/RFMotor" + rfMotorName, "Setting Power," + (power), false, false);

    }

    public double getPower() {
        return rfMotor.getPower();
    }

    public double getGRAVITY_CONSTANT() {
        return getResistance();
    }

    public void setVelocity(double velocity) {
        if (rfMotor.getVelocity() != velocity) {
//            inputlogs.add(rfMotorName);
//            inputlogs.add("setVelocity()");
//            inputlogs.add("Setting Velocity: " + velocity);
//            logger.log("/MotorLogs/RFMotor", rfMotorName + ",setVelocity()," +
//                    "Setting Velocity: " + df.format(velocity), true, true);
//            inputlogs.clear();
//            logger.log("/MotorLogs/RFMotor" + rfMotorName, "Setting Velocity," + velocity);
//            logger.log("/RobotLogs/GeneralRobot", rfMotorName + "\nsetVelocity():\nSetting Velocity:" + velocity);
        }
        rfMotor.setVelocity(velocity);
    }

    public int getCurrentPosition() {
//        inputlogs.add(rfMotorName);
//        inputlogs.add("getCurrentPosition()");
//        inputlogs.add("Getting Position: " + rfMotor.getCurrentPosition());
//        inputlogs.clear();

//        logger.log("/RobotLogs/GeneralRobot", inputlogs);
//        logger.log("/MotorLogs/RFMotor" + rfMotorName, "Current Tick Count," + rfMotor.getCurrentPosition());
        return rfMotor.getCurrentPosition() + (int) additionalTicks;
    }

    public void setMode(DcMotor.RunMode runMode) {
        rfMotor.setMode(runMode);
        if (rfMotor.getMode() != runMode) {
//            inputlogs.add(rfMotorName);
//            inputlogs.add("setMode()");
//            inputlogs.add("Setting RunMode: " + runMode);
            logger.log("/MotorLogs/RFMotor", rfMotorName + ",setMode(),Setting RunMode: " + runMode,
                    true, true);
//            inputlogs.clear();

//            logger.log("/MotorLogs/RFMotor" + rfMotorName, "Setting Mode," + runMode);
//            logger.log("/RobotLogs/GeneralRobot", rfMotorName + "setMode():\nSetting Mode," + runMode);
        }
    }

    public void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior) {
        rfMotor.setZeroPowerBehavior(behavior);
    }

    public double getVelocity() {
        return rfMotor.getVelocity();
    }

    public double getTICK_BOUNDARY_PADDING() {
        return TICK_BOUNDARY_PADDING;
    }

    public double getTICK_STOP_PADDING() {
        return TICK_STOP_PADDING;
    }

    public void setTICK_BOUNDARY_PADDING(double p_TICK_BOUNDARY_PADDING) {
        TICK_BOUNDARY_PADDING = p_TICK_BOUNDARY_PADDING;
    }

    public void setTICK_STOP_PADDING(double p_TICK_STOP_PADDING) {
        TICK_STOP_PADDING = p_TICK_STOP_PADDING;
    }
}