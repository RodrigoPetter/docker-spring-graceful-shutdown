package com.example.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  @GetMapping("hello")
  public String hello() {
    return "Hello";
  }

  @Scheduled(initialDelay = 5000L, fixedDelay = 500L)
  public void holdThread() throws InterruptedException {
    var mode = System.getenv("MODE");
    var holdMessage = "====---- HOLD ("+mode+") ----====";
    var threadMessage = Thread.currentThread().getId() + "["+Thread.currentThread().getName()+"]";

    System.out.println(holdMessage);
    System.out.println(threadMessage);
    System.out.println("15 seconds left");
    Thread.sleep(5000);

    System.out.println(holdMessage);
    System.out.println(threadMessage);
    System.out.println("10 seconds left");
    Thread.sleep(5000);

    System.out.println(holdMessage);
    System.out.println(threadMessage);
    System.out.println("5 seconds left");
    Thread.sleep(5000);

    System.out.println("====----HOLD FINISHED----====");
  }
}
