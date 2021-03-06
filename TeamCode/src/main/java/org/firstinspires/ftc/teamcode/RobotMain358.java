package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;

import java.lang.*;

public abstract class RobotMain358 extends LinearOpMode {
    protected DcMotor lf, lb, rf, rb;
    protected DcMotor slideMotor, crMotor, intakeMotor;
    protected Servo blackBox, stuck;
    protected DistanceSensor dsFrontLeft, dsFrontRight, dsLeft, dsRight;
    protected ColorSensor colorFreight;

    public double driveFactor = 0.9; //for TeleOp
    public final double slidePower = 0.1;
    public long lastTime = System.currentTimeMillis();
    public int timeElapsed = 500; // this is in milliseconds

    final double DRIVE_FACTOR = 64.6784;
    final double TURN_FACTOR = 13.07005139;
    final double STRAFE_FACTOR = 64.6784;

    ElapsedTime runtime = new ElapsedTime();    // Use to determine when end game is starting.
    final double HALF_TIME = 60.0;              // Wait this many seconds before rumble-alert for half-time.
    final double END_GAME = 90.0;               // Wait this many seconds before rumble-alert for end-time.

    public static final String TFOD_MODEL_ASSET = "FreightFrenzy_BCDM.tflite";
    public static final String[] LABELS = {"Ball", "Cube"};
    public static final String VUFORIA_KEY = "AbfVBDz/////AAABmYjPxLVfc06Kki/omu9b26Vk1TfvZO7giwjiWUu3cBC4GLD957469zF341ecaqFEoca1E35mbaSrBC/Hn5UZgPxpIjYNTOLRBJi72mUr9HO+mMAwuq9Qrs3MQ9E0OOTPolRHSiuorwRU/eTDNksoKVhNdtilPnWFktTaLS2dX6M8MiL3IXxUBxItTd+lbDuKLLVwPDO12DSWR1kOc11jKOnFBgfYUFrDLAq9X6yW74XQlOm26vE/mr/EJ3uO6y5QWysl9oQFGgoDioxqfRuCXQ2oy4BafcHVkwsMoJwAFeIP7zOVukmpIB7NzgZRQ8xy1+EfQTg75ojzmZplPf+wKWd4ypO4XJs3nGAk1kM+/thh";
    public VuforiaLocalizer vuforia;
    public TFObjectDetector tfod;
    public int stuckPosition = 0;

    public void CHASSIS_INITIALIZE() throws InterruptedException{
        lf = hardwareMap.dcMotor.get("lf");
        lb = hardwareMap.dcMotor.get("lb");
        rf = hardwareMap.dcMotor.get("rf");
        rb = hardwareMap.dcMotor.get("rb");

        lf.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lb.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rf.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rb.setZeroPowerBehavior(DcMotor. ZeroPowerBehavior.BRAKE);

        rf.setDirection(DcMotor.Direction.REVERSE);
        rb.setDirection(DcMotor.Direction.REVERSE);

        slideMotor = hardwareMap.dcMotor.get("slideMotor");
        slideMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        slideMotor.setDirection(DcMotor.Direction.REVERSE);
        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        crMotor = hardwareMap.dcMotor.get("crMotor");
        crMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        intakeMotor = hardwareMap.dcMotor.get("intakeMotor");
        intakeMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        blackBox = hardwareMap.servo.get("blackBox");
        blackBox.setPosition(0.439);
        stuck = hardwareMap.servo.get("stuck");
        stuck.setPosition(0.17);

        dsFrontLeft = hardwareMap.get(DistanceSensor.class, "dsFrontLeft");
        dsFrontRight = hardwareMap.get(DistanceSensor.class, "dsFrontRight");
        dsLeft = hardwareMap.get(DistanceSensor.class, "dsLeft");
        dsRight = hardwareMap.get(DistanceSensor.class, "dsRight");
        colorFreight = hardwareMap.get(ColorSensor.class, "colorFreight");
    }

    // TeleOp Switch Drive
    public double switchDriveUp(double df){
       if (df == 0.7){
            return 0.9;
       }
       return df;
    }

    public double switchDriveDown(double df){
        if (df == 0.9){
            return 0.7;
        }
        return df;
    }

    public void forward (double inch, double power){
        int ticks = (int) (inch * DRIVE_FACTOR);

        //Reset Encoders
        lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Set Target Position
        lf.setTargetPosition(lf.getCurrentPosition() - ticks);
        lb.setTargetPosition(lb.getCurrentPosition() - ticks);
        rf.setTargetPosition(rf.getCurrentPosition() - ticks);
        rb.setTargetPosition(rb.getCurrentPosition() - ticks);

        //Set Drive Power
        lf.setPower(power);
        lb.setPower(power);
        rf.setPower(power);
        rb.setPower(power);

        //Set to RUN_TO_POSITION mode
        lf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lb.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rb.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while (lf.isBusy() && lb.isBusy() && rf.isBusy() && rb.isBusy()){
            //Wait Until Target Position is Reached
        }
        sleep(200);
    }

    public void exponentialForward(double inch, double power) {
        forward(inch/4, power/4);
        forward(inch/4, power/2);
        forward(inch/2, power);
    }

    public void forwardIntake (double inch, double power){
        int ticks = (int) (inch * DRIVE_FACTOR);

        //Reset Encoders
        lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Set Target Position
        lf.setTargetPosition(lf.getCurrentPosition() - ticks);
        lb.setTargetPosition(lb.getCurrentPosition() - ticks);
        rf.setTargetPosition(rf.getCurrentPosition() - ticks);
        rb.setTargetPosition(rb.getCurrentPosition() - ticks);

        //Set Drive Power
        lf.setPower(power);
        lb.setPower(power);
        rf.setPower(power);
        rb.setPower(power);

        //Set to RUN_TO_POSITION mode
        intakeMotor.setPower(1);
        lf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lb.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rb.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while (lf.isBusy() && lb.isBusy() && rf.isBusy() && rb.isBusy()){
            //Wait Until Target Position is Reached
        }
        intakeMotor.setPower(0);
        sleep(200);
    }

    public void turn (int degree, double power){
        int ticks = (int) (degree * TURN_FACTOR);

        //Reset Encoders
        lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Set Target Position
        lf.setTargetPosition(lf.getCurrentPosition() - ticks);
        lb.setTargetPosition(lb.getCurrentPosition() - ticks);
        rf.setTargetPosition(rf.getCurrentPosition() + ticks);
        rb.setTargetPosition(rb.getCurrentPosition() + ticks);

        //Set Drive Power
        lf.setPower(power);
        lb.setPower(power);
        rf.setPower(power);
        rb.setPower(power);

        //Set to RUN_TO_POSITION mode
        lf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lb.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rb.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while (lf.isBusy() && lb.isBusy() && rf.isBusy() && rb.isBusy()){
            //Wait Until Target Position is Reached
        }
        sleep(200);
    }

    public void strafe (double inch, double power){
        int ticks = (int) (inch * STRAFE_FACTOR);

        //Reset Encoders
        lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Set Target Position
        lf.setTargetPosition(lf.getCurrentPosition() + ticks);
        lb.setTargetPosition(lb.getCurrentPosition() - ticks);
        rf.setTargetPosition(rf.getCurrentPosition() - ticks);
        rb.setTargetPosition(rb.getCurrentPosition() + ticks);

        //Set Drive Power
        lf.setPower(power);
        lb.setPower(power);
        rf.setPower(power);
        rb.setPower(power);

        //Set to RUN_TO_POSITION mode
        lf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lb.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rb.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while (lf.isBusy() && lb.isBusy() && rf.isBusy() && rb.isBusy()){
            //Wait Until Target Position is Reached
        }
        sleep(200);
    }

    public void strafeLeftTeleOp(){
        //Reset Encoders
        lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Set Target Position
        lf.setTargetPosition(lf.getCurrentPosition() + 20000);
        lb.setTargetPosition(lb.getCurrentPosition() - 20000);
        rf.setTargetPosition(rf.getCurrentPosition() - 20000);
        rb.setTargetPosition(rb.getCurrentPosition() + 20000);

        //Set Drive Power
        lf.setPower(0.7);
        lb.setPower(0.7);
        rf.setPower(0.7);
        rb.setPower(0.7);

        //Set to RUN_TO_POSITION mode
        lf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lb.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rb.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while (lf.isBusy() && lb.isBusy() && rf.isBusy() && rb.isBusy()){
            if (dsLeft.getDistance(DistanceUnit.INCH) <= 5 || gamepad1.a) {
                lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

                lf.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                lb.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                rf.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                rb.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }
        }
    }

    public void strafeRightTeleOp(){
        //Reset Encoders
        lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Set Target Position
        lf.setTargetPosition(lf.getCurrentPosition() - 20000);
        lb.setTargetPosition(lb.getCurrentPosition() + 20000);
        rf.setTargetPosition(rf.getCurrentPosition() + 20000);
        rb.setTargetPosition(rb.getCurrentPosition() - 20000);

        //Set Drive Power
        lf.setPower(0.7);
        lb.setPower(0.7);
        rf.setPower(0.7);
        rb.setPower(0.7);

        //Set to RUN_TO_POSITION mode
        lf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lb.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rb.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while (lf.isBusy() && lb.isBusy() && rf.isBusy() && rb.isBusy()){
            if (dsRight.getDistance(DistanceUnit.INCH) <= 5 || gamepad1.a) {
                lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

                lf.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                lb.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                rf.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                rb.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }
        }
    }

    public void strafeLeftAuto(double inch, double power, double distance){
        int ticks = (int) (inch * STRAFE_FACTOR);

        //Reset Encoders
        lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Set Target Position
        lf.setTargetPosition(lf.getCurrentPosition() + ticks);
        lb.setTargetPosition(lb.getCurrentPosition() - ticks);
        rf.setTargetPosition(rf.getCurrentPosition() - ticks);
        rb.setTargetPosition(rb.getCurrentPosition() + ticks);

        //Set Drive Power
        lf.setPower(power);
        lb.setPower(power);
        rf.setPower(power);
        rb.setPower(power);

        //Set to RUN_TO_POSITION mode
        lf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lb.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rb.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while (lf.isBusy() && lb.isBusy() && rf.isBusy() && rb.isBusy()){
            if (dsLeft.getDistance(DistanceUnit.INCH) <= distance) {
                lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                break;
            }
        }
    }

    public void strafeRightAuto(double inch, double power, double distance){
        int ticks = (int) (inch * STRAFE_FACTOR);

        //Reset Encoders
        lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Set Target Position
        lf.setTargetPosition(lf.getCurrentPosition() - ticks);
        lb.setTargetPosition(lb.getCurrentPosition() + ticks);
        rf.setTargetPosition(rf.getCurrentPosition() + ticks);
        rb.setTargetPosition(rb.getCurrentPosition() - ticks);

        //Set Drive Power
        lf.setPower(power);
        lb.setPower(power);
        rf.setPower(power);
        rb.setPower(power);

        //Set to RUN_TO_POSITION mode
        lf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lb.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rb.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while (lf.isBusy() && lb.isBusy() && rf.isBusy() && rb.isBusy()){
            telemetry.addData("distance", dsRight.getDistance(DistanceUnit.INCH));
            telemetry.update();
            if (dsRight.getDistance(DistanceUnit.INCH) <= distance) {
                lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                break;
            }
        }
    }

    public void forwardAuto(double inch, double power, double distance){
        int ticks = (int) (inch * DRIVE_FACTOR);
        float averageFrontDistane;

        //Reset Encoders
        lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Set Target Position
        lf.setTargetPosition(lf.getCurrentPosition() - ticks);
        lb.setTargetPosition(lb.getCurrentPosition() - ticks);
        rf.setTargetPosition(rf.getCurrentPosition() - ticks);
        rb.setTargetPosition(rb.getCurrentPosition() - ticks);

        //Set Drive Power
        lf.setPower(power);
        lb.setPower(power);
        rf.setPower(power);
        rb.setPower(power);

        //Set to RUN_TO_POSITION mode
        lf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lb.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rb.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while (lf.isBusy() && lb.isBusy() && rf.isBusy() && rb.isBusy()){
            telemetry.addData("distance", dsRight.getDistance(DistanceUnit.INCH));
            telemetry.update();
            averageFrontDistane = (float) (dsFrontLeft.getDistance(DistanceUnit.INCH) + dsFrontRight.getDistance(DistanceUnit.INCH)) / 2;
            if (averageFrontDistane <= distance) {
                lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                break;
            }
        }
    }

    public double forwardUntilIntakeAuto(double inch, double power, double counter){
        int ticks = (int) (inch * DRIVE_FACTOR);

        //Reset Encoders
        lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Set Target Position
        lf.setTargetPosition(lf.getCurrentPosition() - ticks);
        lb.setTargetPosition(lb.getCurrentPosition() - ticks);
        rf.setTargetPosition(rf.getCurrentPosition() - ticks);
        rb.setTargetPosition(rb.getCurrentPosition() - ticks);

        //Set Drive Power
        lf.setPower(power);
        lb.setPower(power);
        rf.setPower(power);
        rb.setPower(power);

        intakeMotor.setPower(1);

        //Set to RUN_TO_POSITION mode
        lf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lb.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rf.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rb.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while (lf.isBusy() && lb.isBusy() && rf.isBusy() && rb.isBusy()){
            telemetry.addData("distance", dsRight.getDistance(DistanceUnit.INCH));
            telemetry.update();

            if (((DistanceSensor) colorFreight).getDistance(DistanceUnit.INCH) < 10) {
                lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                lb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                rb.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                return counter + inch;
            }
        }
        return counter + inch;
    }

    public void carousel(String state){

        //Reset Encoders
        crMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Set Target Position
        if (state == "red") {
            crMotor.setTargetPosition(crMotor.getCurrentPosition() - 2000);
        } else if (state == "blue") {
            crMotor.setTargetPosition(crMotor.getCurrentPosition() + 2000);
        }

        //Set Drive Power
        crMotor.setPower(0.45);

        //Set to RUN_TO_POSITION mode
        crMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while (crMotor.isBusy()){}
    }

    public int DETECT_POSITION_BLUE() {
        if (dsFrontRight.getDistance(DistanceUnit.INCH) < 25) {
            return 3;
        } else if (dsFrontLeft.getDistance(DistanceUnit.INCH) < 25) {
            return 2;
        } else {
            return 1;
        }
    }

    public int DETECT_POSITION_RED() {
        if (dsFrontRight.getDistance(DistanceUnit.INCH) < 25) {
            return 2;
        } else if (dsFrontLeft.getDistance(DistanceUnit.INCH) < 25) {
            return 1;
        } else {
            return 3;
        }
    }
}