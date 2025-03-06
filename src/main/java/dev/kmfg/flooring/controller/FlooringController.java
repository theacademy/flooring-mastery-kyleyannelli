package dev.kmfg.flooring.controller;

import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.OrderNotFoundException;
import dev.kmfg.flooring.dao.exception.StateTaxNotFoundException;
import dev.kmfg.flooring.dto.Order;
import dev.kmfg.flooring.service.FlooringServiceLayer;
import dev.kmfg.flooring.service.exception.OrderDataValidationException;
import dev.kmfg.flooring.view.FlooringView;
import dev.kmfg.flooring.view.MenuSelection;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
                case DISPLAY_ORDERS:
                    displayOrders();
                    break;
                case ADD_ORDER:
                    addOrder();
                    break;
                case EDIT_ORDER:
                    editOrder();
                    break;
                case EXIT:
                    goodbye();
                    break;
                default:
                    view.displayUnimplementedMenuSelection(userSelection);
                    break;
            }
        } catch(FlooringDataPersistenceException | OrderDataValidationException | StateTaxNotFoundException e) {
            view.displayError(e);
        } catch(OrderNotFoundException orderNotFoundException) {
            view.displayOrderNotFound(
                    orderNotFoundException.getOrderDate(),
                    orderNotFoundException.getOrderNumber()
            );
        }

        if(userSelection != MenuSelection.EXIT) {
            view.displayPressEnterToContinue();
        }

        return userSelection;
    }

    private void goodbye() {
        view.displayGoodbye();
    }

    private void addOrder() throws FlooringDataPersistenceException, OrderDataValidationException {
        final Optional<Order> orderOpt = view.displayAddOrder(service.getAllProducts(), service.getAllStateTaxes());

        if(orderOpt.isPresent()) {
            service.addOrder(orderOpt.get());
            view.displayAddedOrder(orderOpt.get());
        }
    }

    private void displayOrders() throws FlooringDataPersistenceException {
        final LocalDate dateToFindOrders = view.displayFindOrders();
        final List<Order> foundOrders = service.getAllOrders(dateToFindOrders);
        view.displayFoundOrders(foundOrders, dateToFindOrders);
    }

    private void editOrder() throws FlooringDataPersistenceException, OrderNotFoundException, StateTaxNotFoundException, OrderDataValidationException {
        Order orderToEdit = view.displayFindOrder();
        orderToEdit = service.getOrder(orderToEdit.getOrderDate(), orderToEdit.getOrderNumber());

        final Order editedOrder = view.displayEditOrder(orderToEdit, service.getAllStateTaxes(), service.getAllProducts());

        if(editedOrder.equals(orderToEdit)) {
            view.displayEditedOrderNotChanged(orderToEdit, editedOrder);
        } else if(view.displayConfirmOrderChange(orderToEdit, editedOrder)){
            service.editOrder(editedOrder);
        }
    }
}