package dev.kmfg.flooring.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class Order {
    private static final NumberFormat NICE_NUMBER_FORMATTER = NumberFormat.getNumberInstance(Locale.US);
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100).setScale(0, RoundingMode.UNNECESSARY);

    static {
        NICE_NUMBER_FORMATTER.setMinimumFractionDigits(2);
    }

    private int orderNumber;
    private LocalDate orderDate;
    private String customerName;
    private BigDecimal area;
    private StateTax stateTax;
    private Product product;

    /**
     * Create an empty order.
     */
    public Order() {
        orderNumber = -1;
    }

    public Order setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
        return this;
    }

    public Order setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
        return this;
    }

    public Order setCustomerName(String customerName) {
        this.customerName = customerName;
        return this;
    }

    public Order setStateTax(StateTax stateTax) {
        this.stateTax = stateTax;
        return this;
    }

    public Order setProduct(Product product) {
        this.product = product;
        return this;
    }

    public Order setArea(BigDecimal area) {
        this.area = area;
        return this;
    }

    public String getCustomerName() {
        return customerName;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public BigDecimal getArea() {
        return area;
    }

    public StateTax getStateTax() {
        return stateTax;
    }

    public Product getProduct() {
        return product;
    }

    /**
     * Calculated from the area, and cost per sqft.
     * @return material cost, with a scale of 2, rounding half up.
     */
    public BigDecimal getMaterialCost() {
        return area.multiply(product.getCostPerSqft()).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculated from the area, and labor cost per sqft.
     * @return labor cost, with a scale of 2, rounding half up.
     */
    public BigDecimal getLaborCost() {
        return area.multiply(product.getLaborCostPerSqft()).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculated from the material cost, and labor cost.
     * @return tax, with a scale of 2, rounding half up.
     */
    public BigDecimal getTax() {
        final BigDecimal materialPlusLabor = getMaterialCost().add(getLaborCost());
        final BigDecimal taxRateAsFraction = stateTax.getTaxRate().divide(ONE_HUNDRED, RoundingMode.HALF_UP);
        return materialPlusLabor.multiply(taxRateAsFraction).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculated from the material cost, labor cost, and tax.
     * @return total including tax, with a scale of 2, rounding half up.
     */
    public BigDecimal getTotal() {
        return getMaterialCost().add(getLaborCost()).add(getTax()).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Clones an order, does NOT make deep copies of fields.
     * @return the cloned order
     */
    public Order cloneOrder() {
        return new Order()
                .setOrderNumber(orderNumber)
                .setCustomerName(customerName)
                .setArea(area)
                .setOrderDate(orderDate)
                .setProduct(product)
                .setStateTax(stateTax);
    }

    /**
     * Provides the order with its state tax, product, area, and totals.
     * @return String
     */
    @Override
    public String toString() {
        final boolean haveAreaAndProduct = area != null && product != null;
        final boolean haveAreaProductStateTax = haveAreaAndProduct && stateTax != null;

        final String formatStr = "Order%s for %s\n\tArea: %s sqft\n\tState Tax: %s\n\tProduct: %s\n\tMaterial: $%s | Labor: $%s | Tax: $%s | Total: $%s";
        final String orderNumberStr = orderNumber == -1 ? "" : " #" + orderNumber;

        return String.format(formatStr,
                orderNumberStr,
                customerName != null ? customerName : "N/A",
                area != null ? area.toString() : "N/A",
                stateTax != null ? stateTax.toString() : "N/A",
                product != null ? product.toString() : "N/A",
                haveAreaAndProduct ? NICE_NUMBER_FORMATTER.format(getMaterialCost()) : "N/A",
                haveAreaAndProduct ? NICE_NUMBER_FORMATTER.format(getLaborCost()) : "N/A",
                haveAreaProductStateTax ? NICE_NUMBER_FORMATTER.format(getTax()) : "N/A",
                haveAreaProductStateTax ? NICE_NUMBER_FORMATTER.format(getTotal()) : "N/A"
        );
    }

    @Override
    public int hashCode() {
        int result = 7;
        result *= 71 + orderNumber;
        result *= 71 + (orderDate == null ? 0 : orderDate.hashCode());
        result *= 71 + (customerName == null ? 0 : customerName.hashCode());
        result *= 71 + (area == null ? 0 : area.hashCode());
        result *= 71 + (stateTax == null ? 0 : stateTax.hashCode());
        result *= 71 + (product == null ? 0 : product.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if(other == null) return false;
        if(other == this) return true;
        if(other.getClass() != getClass()) return false;

        final Order otherOrder = (Order) other;
        if(otherOrder.orderNumber != orderNumber) return false;

        if(orderDate == null) {
            if(otherOrder.orderDate != null) {
                return false;
            }
        } else if(!otherOrder.orderDate.equals(orderDate)) {
            return false;
        }

        if(customerName == null) {
            if(otherOrder.customerName != null) {
                return false;
            }
        } else if(!customerName.equals(otherOrder.customerName)) {
            return false;
        }

        if(area == null) {
            if(otherOrder.area != null) {
                return false;
            }
        } else if(!area.equals(otherOrder.area)) {
            return false;
        }

        if(stateTax == null) {
            if(otherOrder.stateTax != null) {
                return false;
            }
        } else if(!stateTax.equals(otherOrder.stateTax)) {
            return false;
        }

        if(product == null) {
            return otherOrder.product == null;
        }
        return product.equals(otherOrder.product);
    }
}
