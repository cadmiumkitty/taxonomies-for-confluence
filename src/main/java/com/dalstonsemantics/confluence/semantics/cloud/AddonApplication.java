package com.dalstonsemantics.confluence.semantics.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling
public class AddonApplication {

    public static void main(String[] args) throws Exception {
        new SpringApplication(AddonApplication.class).run(args);
    }
}