package com.movix.push.pushtester;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.movix.push.controller.SenderController;

/**
 * 
 * @author dsepulveda
 *
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = SenderController.class)
public class PushTesterApplication {

    public static void main(String[] args) {
        SpringApplication.run(PushTesterApplication.class, args);
    }
}
