package dev.kmfg.flooring;

import dev.kmfg.flooring.controller.FlooringController;
import dev.kmfg.flooring.dao.*;
import dev.kmfg.flooring.service.FlooringServiceLayer;
import dev.kmfg.flooring.service.FlooringServiceLayerImpl;
import dev.kmfg.flooring.view.FlooringView;
import dev.kmfg.flooring.view.UserIO;
import dev.kmfg.flooring.view.UserIOConsoleImpl;

public class App {
    public static void main(String[] args) {
        final UserIO io = new UserIOConsoleImpl();
        final FlooringView view = new FlooringView(io);

        final OrderDaoFileImpl orderDao = new OrderDaoFileImpl();
        final ProductDao productDao = new ProductDaoFileImpl();
        final StateTaxDao stateTaxDao = new StateTaxDaoFileImpl();
        final FlooringServiceLayer service = new FlooringServiceLayerImpl(orderDao, productDao, stateTaxDao);

        final FlooringController controller = new FlooringController(view, service);
        controller.run();
    }
}
