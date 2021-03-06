package org.firstinspires.ftc.teamcode.archive;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;

@Disabled
@TeleOp
public class TEST_SERVO extends LinearOpMode {

    public Servo servo1;

//    public TouchSensor ts1 = hardwareMap.touchSensor.get("ts1");

    public void runOpMode() throws InterruptedException{

        servo1 = hardwareMap.servo.get("servo1");
        servo1.setPosition(0);

        waitForStart();
        while (opModeIsActive()) {

            if(gamepad1.left_bumper){
                servo1.setPosition(0.85);
            } else if (gamepad1.right_bumper){
                servo1.setPosition(0.068);
            }

            if (gamepad2.left_bumper){
                servo1.setPosition(servo1.getPosition() + 0.0005);
            }
            else if (gamepad2.right_bumper){
                servo1.setPosition(servo1.getPosition() - 0.0005);
            }

            telemetry.addData("position", servo1.getPosition());
            telemetry.update();

        }
    }
}