package dev.kmfg.flooring.controller;

import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.OrderNotFoundException;
import dev.kmfg.flooring.dao.exception.StateTaxNotFoundException;
import dev.kmfg.flooring.model.Order;
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
                case REMOVE_ORDER:
                    removeOrder();
                    break;
                case EXIT:
                    goodbye();
                    break;
                case EXPORT_ALL_ORDERS:
                    exportAllOrders();
                    break;
                default:
                    view.displayUnimplementedMenuSelection(userSelection);
                    break;
            }
        } catch(FlooringDataPersistenceException | OrderDataValidationException | StateTaxNotFoundException e) {
            view.displayError(e);
        } catch(OrderNotFoundException orderNotFoundException) {
            if(orderNotFoundException.areThereAnyOrdersInDate()) {
                view.displayOrderNotFound(
                        orderNotFoundException.getOrderDate(),
                        orderNotFoundException.getOrderNumber()
                );
            } else {
                view.displayNoOrdersForDate(
                        orderNotFoundException.getOrderDate()
                );
            }
        }

        if(userSelection != MenuSelection.EXIT) {
            view.displayPressEnterToContinue();
        }

        return userSelection;
    }

    private void goodbye() {
        view.displayGoodbye();
    }

    private void addOrder() throws FlooringDataPersistenceException, OrderDataValidationException, OrderNotFoundException {
        final Optional<Order> orderOpt = view.displayAddOrder(service.getAllProducts(), service.getAllStateTaxes());

        if(orderOpt.isPresent()) {
            service.addOrder(orderOpt.get());
            view.displayAddedOrder(orderOpt.get());
        }
    }

    private void displayOrders() throws FlooringDataPersistenceException, OrderNotFoundException {
        final LocalDate dateToFindOrders = view.displayFindOrders();
        final List<Order> foundOrders = service.getAllOrders(dateToFindOrders);
        view.displayFoundOrders(foundOrders, dateToFindOrders);
    }

    private void editOrder() throws FlooringDataPersistenceException, OrderNotFoundException, StateTaxNotFoundException, OrderDataValidationException {
        // get the order date and number
        Order orderToEdit = view.displayFindOrder();
        // reassign the order with a real one
        orderToEdit = service.getOrder(orderToEdit.getOrderDate(), orderToEdit.getOrderNumber());

        final Order editedOrder = view.displayEditOrder(orderToEdit, service.getAllStateTaxes(), service.getAllProducts());

        if(editedOrder.equals(orderToEdit)) {
            view.displayEditedOrderNotChanged(orderToEdit, editedOrder);
        } else if(view.displayConfirmOrderChange(orderToEdit, editedOrder)){
            view.displayEditedOrder(service.editOrder(editedOrder));
        }
    }

    private void exportAllOrders() throws FlooringDataPersistenceException, OrderNotFoundException {
        final int numOfOrders = service.getNumberOfOrders();
        final Optional<String> filePathOpt = view.displayExportAllOrders(numOfOrders);
        if(filePathOpt.isPresent()) {
            service.exportAll(filePathOpt.get());
            view.displayOrdersExported(numOfOrders, filePathOpt.get());
        } else {
            view.displayOrdersNotExported();
        }
    }

    private void removeOrder() throws FlooringDataPersistenceException, OrderNotFoundException, StateTaxNotFoundException {
        // get the order date and number
        Order orderToRemove = view.displayFindOrder();
        // reassign the order with a real one
        orderToRemove = service.getOrder(orderToRemove.getOrderDate(), orderToRemove.getOrderNumber());

        if(view.displayConfirmOrderRemove(orderToRemove)) {
            final Order removedOrder = service.removeOrder(orderToRemove.getOrderDate(), orderToRemove.getOrderNumber());
            view.displayRemovedOrder(removedOrder);
        }
    }
}