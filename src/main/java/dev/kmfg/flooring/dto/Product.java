package dev.kmfg.flooring.dto;

import java.math.BigDecimal;

public class Product {
    private final String productType;
    private final BigDecimal costPerSqft, laborCostPerSqft;

    public Product(String productType, BigDecimal costPerSqft, BigDecimal laborCostPerSqft) {
        this.productType = productType;
        this.costPerSqft = costPerSqft;
        this.laborCostPerSqft = laborCostPerSqft;
    }

    public BigDecimal getCostPerSqft() {
        return costPerSqft;
    }

    public BigDecimal getLaborCostPerSqft() {
        return laborCostPerSqft;
    }

    public String getProductType() {
        return productType;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash *= 71 + (costPerSqft == null ? 0 : costPerSqft.hashCode());
        hash *= 71 + (laborCostPerSqft == null ? 0 : laborCostPerSqft.hashCode());
        hash *= 71 + (productType == null ? 0 : productType.hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if(other == null) return false;
        if(other == this) return true;
        if(other.getClass() != getClass()) return false;

        final Product otherProduct = (Product) other;

        if(productType == null) {
            if(otherProduct.productType != null) {
                return false;
            }
        } else if(!productType.equals(otherProduct.productType)) {
            return false;
        }

        if(costPerSqft == null) {
            if(otherProduct.costPerSqft != null) {
                return false;
            }
        } else if(otherProduct.costPerSqft.compareTo(costPerSqft) != 0) {
            return false;
        }

        if(laborCostPerSqft == null) {
            return otherProduct.laborCostPerSqft == null;
        }
        return otherProduct.laborCostPerSqft.compareTo(laborCostPerSqft) == 0;
    }


    @Override
    public String toString() {
        return String.format("%s ($%s/sqft material, $%s/sqft labor)",
                productType,
                costPerSqft,
                laborCostPerSqft);
    }
}
