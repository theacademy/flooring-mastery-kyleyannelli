package dev.kmfg.flooring.controller;

import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dto.Order;
import dev.kmfg.flooring.service.FlooringServiceLayer;
import dev.kmfg.flooring.service.exception.OrderDataValidationException;
import dev.kmfg.flooring.view.FlooringView;
import dev.kmfg.flooring.view.MenuSelection;

public class FlooringController {
    private final FlooringView view;
    private final FlooringServiceLayer service;

    public FlooringController(FlooringView view, FlooringServiceLayer service) {
        this.view = view;
        this.service = service;
    }

    public void run() {
        MenuSelection userSelection = MenuSelection.NONE;
        while(userSelection != MenuSelection.EXIT) {
            userSelection = getUserChoiceAndDoAction();
        }
    }

    private MenuSelection getUserChoiceAndDoAction() {
        MenuSelection userSelection = view.displayAndGetMenuOption();

        try {
            switch(userSelection) {
                case ADD_ORDER:
                    addOrder();
                    break;
                case EXIT:
                    goodbye();
                    break;
                default:
                    view.displayUnimplementedMenuSelection(userSelection);
                    break;
            }

            if(userSelection != MenuSelection.EXIT) {
                view.displayPressEnterToContinue();
            }
        } catch (FlooringDataPersistenceException | OrderDataValidationException e) {
            view.displayError(e);
        }

        return userSelection;
    }

    private void goodbye() {
        view.displayGoodbye();
    }

    private void addOrder() throws FlooringDataPersistenceException, OrderDataValidationException {
        final Order order = view.displayAddOrder(service.getAllProducts(), service.getAlLStateTaxes());
        service.addOrder(order);
        view.displayAddedOrder(order);
    }
}