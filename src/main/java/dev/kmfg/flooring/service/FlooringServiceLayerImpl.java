package dev.kmfg.flooring.service;

import dev.kmfg.flooring.dao.OrderDao;
import dev.kmfg.flooring.dao.ProductDao;
import dev.kmfg.flooring.dao.StateTaxDao;
import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.OrderNotFoundException;
import dev.kmfg.flooring.dao.exception.ProductNotFoundException;
import dev.kmfg.flooring.dao.exception.StateTaxNotFoundException;
import dev.kmfg.flooring.dto.Order;
import dev.kmfg.flooring.dto.Product;
import dev.kmfg.flooring.dto.StateTax;
import dev.kmfg.flooring.service.exception.OrderDataValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlooringServiceLayerImpl implements FlooringServiceLayer {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100).setScale(0, RoundingMode.UNNECESSARY);

    private final OrderDao orderDao;
    private final ProductDao productDao;
    private final StateTaxDao stateTaxDao;

    public FlooringServiceLayerImpl(OrderDao orderDao, ProductDao productDao, StateTaxDao stateTaxDao) {
        this.orderDao = orderDao;
        this.productDao = productDao;
        this.stateTaxDao = stateTaxDao;
    }

    private List<Order> collectOrderStates(List<Order> orders) throws FlooringDataPersistenceException {
        final Map<String, StateTax> stateTaxMap = stateTaxDao.getAllStateTaxes().stream()
                .collect(Collectors.toMap(
                        StateTax::getStateAbbreviation,
                        stateTax -> stateTax
                ));

        StateTax foundStateTax;
        for(Order order: orders) {
            foundStateTax = stateTaxMap.get(order.getStateTax().getStateAbbreviation());

            if(foundStateTax == null) {
                throw new FlooringDataPersistenceException(
                        String.format(
                                "Order has a state tax \"%s\" which no longer exists!",
                                order.getStateTax().getStateAbbreviation()
                        )
                );
            }

            order.setStateTax(foundStateTax);
        }

        return orders;
    }

    private void validateProduct(Product product) throws OrderDataValidationException {
        if(product == null) {
            throw new OrderDataValidationException("Validation failed for order has a null product.");
        } else if(product.getProductType() == null || product.getProductType().isBlank()) {
            throw new OrderDataValidationException("Validation failed for order because its product does not have a type.");
        } else if(product.getCostPerSqft() == null) {
            throw new OrderDataValidationException(
                    "Validation failed for order because its product has a cost per sqft of 0 or less."
            );
        } else if(BigDecimal.ZERO.compareTo(product.getCostPerSqft()) > 0) {
            throw new OrderDataValidationException(
                    String.format(
                            "Validation failed for order because its product has a cost per sqft of %s.",
                            product.getCostPerSqft().toString()
                    )
            );
        } else if(product.getLaborCostPerSqft() == null || BigDecimal.ZERO.compareTo(product.getLaborCostPerSqft()) > 0) {
            throw new OrderDataValidationException("Validation failed for order because its product has a labor cost per sqft of 0 or less.");
        }

        try {
            if(!productDao.getProduct(product.getProductType())
                    .equals(product)) {
                throw new OrderDataValidationException("Validation failed because the product exists, but the data does not match the dao.");
            }
        } catch(FlooringDataPersistenceException e) {
            throw new OrderDataValidationException("Validation failed because the product could not be checked for existence.");
        } catch(ProductNotFoundException e) {
            throw new OrderDataValidationException("Validation failed for order because its product does not exist via the dao.");
        }
    }

    private void validateStateTax(StateTax stateTax) throws OrderDataValidationException {
        if(stateTax == null) {
            throw new OrderDataValidationException("Validation failed for order has a null state tax.");
        } else if(stateTax.getStateName() == null || stateTax.getStateName().isBlank()) {
            throw new OrderDataValidationException("Validation failed for order because its state tax does not have a state name.");
        } else if(stateTax.getStateAbbreviation() == null || stateTax.getStateAbbreviation().isBlank()) {
            throw new OrderDataValidationException("Validation failed for order because its state tax does not have a state abbreviation.");
        } else if(stateTax.getTaxRate() == null || BigDecimal.ZERO.compareTo(stateTax.getTaxRate()) > 0) {
            throw new OrderDataValidationException("Validation failed for order because its state tax has a tax rate of 0 or less.");
        }

        try {
            if(!stateTaxDao.getStateTax(stateTax.getStateAbbreviation())
                    .equals(stateTax)) {
                throw new OrderDataValidationException("Validation failed because the state tax exists, but the data does not match the dao.");
            }
        } catch(FlooringDataPersistenceException e) {
            throw new OrderDataValidationException("Validation failed because the state tax could not be checked for existence.");
        } catch(StateTaxNotFoundException e) {
            throw new OrderDataValidationException("Validation failed for order because its state tax does not exist via the dao.");
        }
    }

    private void validateOrderFields(Order order) throws OrderDataValidationException {
        if(order == null) {
            throw new OrderDataValidationException("Validation failed for order because it is null.");
        } else if(order.getCustomerName() == null){
            throw new OrderDataValidationException("Validation failed for order because customer name is null.");
        } else if(order.getCustomerName().isBlank()){
            throw new OrderDataValidationException("Validation failed for order because customer name has no characters.");
        } else if(order.getArea() == null) {
            throw new OrderDataValidationException("Validation failed for order because area is null.");
        } else if(ONE_HUNDRED.compareTo(order.getArea()) > 0) {
            throw new OrderDataValidationException(
                    String.format("Validation failed for order because area of %s is less than %s.",
                            order.getArea(),
                            ONE_HUNDRED
                    )
            );
        }
    }

    /**
     * Ensures the given order has all the required fields to allow for data persistence.
     * @param order to validate
     * @return the validated order
     * @throws OrderDataValidationException if the order or any of its fields have invalid data
     */
    public Order validateEntireOrder(Order order) throws OrderDataValidationException {
        validateOrderFields(order);
        validateProduct(order.getProduct());
        validateStateTax(order.getStateTax());
        return order;
    }

    @Override
    public Product getProduct(String productType) throws FlooringDataPersistenceException, ProductNotFoundException {
        return productDao.getProduct(productType);
    }

    @Override
    public List<Product> getAllProducts() throws FlooringDataPersistenceException {
        return productDao.getAllProducts();
    }

    @Override
    public StateTax getStateTax(String stateAbbreviation) throws FlooringDataPersistenceException, StateTaxNotFoundException {
        return stateTaxDao.getStateTax(stateAbbreviation);
    }

    @Override
    public List<StateTax> getAllStateTaxes() throws FlooringDataPersistenceException {
        return stateTaxDao.getAllStateTaxes();
    }

    @Override
    public List<Order> getAllOrders() throws FlooringDataPersistenceException {
        final List<Order> foundOrders = orderDao.getAllOrders();

        return collectOrderStates(foundOrders);
    }

    @Override
    public List<Order> getAllOrders(LocalDate orderDate) throws FlooringDataPersistenceException {
        final List<Order> foundOrders = orderDao.getAllOrders(orderDate);

        return collectOrderStates(foundOrders);
    }

    @Override
    public Order addOrder(Order order) throws FlooringDataPersistenceException, OrderDataValidationException {
        return orderDao.addOrder(validateEntireOrder(order));
    }

    @Override
    public Order getOrder(LocalDate orderDate, int orderNumber) throws FlooringDataPersistenceException, OrderNotFoundException {
        final Order foundOrder = orderDao.getOrder(orderDate, orderNumber);

        // The order file does not save the state name, only abbreviation, so we need to get the state to populate the field in order.
        StateTax foundStateTax;
        try {
            foundStateTax = stateTaxDao.getStateTax(foundOrder.getStateTax().getStateAbbreviation());
        } catch (StateTaxNotFoundException e) {
            throw new FlooringDataPersistenceException(
                    String.format(
                            "Order has a state tax \"%s\" which no longer exists!",
                            foundOrder.getStateTax().getStateAbbreviation()
                    ),
                    e
            );
        }

        foundOrder.setStateTax(foundStateTax);
        return foundOrder;
    }

    @Override
    public Order editOrder(Order order) throws FlooringDataPersistenceException, OrderNotFoundException, OrderDataValidationException {
        return orderDao.editOrder(validateEntireOrder(order));
    }

    @Override
    public Order removeOrder(LocalDate orderDate, int orderNumber) throws FlooringDataPersistenceException, OrderNotFoundException {
        return orderDao.removeOrder(orderDate, orderNumber);
    }

    @Override
    public int getTotalOrderCount() throws FlooringDataPersistenceException {
        return orderDao.getAllOrders().size();
    }
}
