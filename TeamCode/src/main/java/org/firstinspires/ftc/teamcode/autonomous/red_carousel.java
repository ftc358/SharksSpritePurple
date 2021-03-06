package org.firstinspires.ftc.teamcode.autonomous;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import java.util.*;
import java.util.function.BinaryOperator;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.RobotMain358;

@Autonomous
public class red_carousel extends RobotMain358 {

    private boolean done = false;
    int FINAL_POSITION;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void runOpMode() throws InterruptedException {

        CHASSIS_INITIALIZE();

        while (!opModeIsActive()) {

            telemetry.addData("ds front left: ", dsFrontLeft.getDistance(DistanceUnit.INCH));
            telemetry.addData("ds front right: ", dsFrontRight.getDistance(DistanceUnit.INCH));
            telemetry.addData("ds left: ", dsLeft.getDistance(DistanceUnit.INCH));
            telemetry.addData("ds right: ", dsFrontRight.getDistance(DistanceUnit.INCH));

            List<Integer> detected = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                detected.add(DETECT_POSITION_RED());
                sleep(10);
            }

            FINAL_POSITION = detected.stream().
                    reduce(BinaryOperator.maxBy((o1, o2) -> Collections.frequency(detected, o1) -
                            Collections.frequency(detected, o2))).orElse(3);
            detected.clear();

            telemetry.addData("Position Detected", FINAL_POSITION);
            telemetry.update();
        }

        waitForStart();
        while (opModeIsActive() && !done) {

            strafe(13, 0.5);
            strafe(2.5, 0.3);

            carousel("red");

            strafe(-32, 0.5);
            forward(-10, 0.8);
            forward(38, 0.5);

            slideAuto();

            blackBox.setPosition(0.086);
            sleep(1300);
            blackBox.setPosition(0.439);
            sleep(500);
            blackBox.setPosition(0.086);
            sleep(1300);
            blackBox.setPosition(0.439);
            sleep(500);

            FINAL_POSITION = 0;
            slideAuto();

            strafe(10, 0.3);
            strafe(-5, 0.3);

            forward(13, 0.5);
            strafe(40, 0.5);
            strafe(5, 0.3);
            forward(-23, 0.5);

            done = true;
        }
    }

    public void slideAuto(){
        /**
         * LEVEL 1 = 600 - 50
         * LEVEL 2 = 1200 - 50
         * MAX / LEVEL 3 = 1800 - 50
         * */

        telemetry.addData("slide", "yay!");
        telemetry.update();

        // set target position based on sensed position
        if (FINAL_POSITION == 0){
            slideMotor.setTargetPosition(50);
        }else if (FINAL_POSITION == 1) {
            strafeRightAuto(10, 0.3, 9);
            slideMotor.setTargetPosition(550);
        } else if (FINAL_POSITION == 2) {
            strafeRightAuto(10, 0.3, 8);
            slideMotor.setTargetPosition(1150);
        } else if (FINAL_POSITION == 3) {
            strafeRightAuto(10, 0.3, 7);
            slideMotor.setTargetPosition(1750);
        }

        // set power and mode
        slideMotor.setPower(1);
        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while (slideMotor.isBusy()){}

    }
}