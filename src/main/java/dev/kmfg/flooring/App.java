package dev.kmfg.flooring;

import dev.kmfg.flooring.controller.FlooringController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {
    public static void main(String[] args) {
        final ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        final FlooringController controller = ctx.getBean(FlooringController.class, "controller");
        controller.run();
    }
}
