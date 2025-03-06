package dev.kmfg.flooring.dto;

import java.math.BigDecimal;

public class StateTax {
    private final String stateName, stateAbbreviation;
    private final BigDecimal taxRate;

    public StateTax(String stateAbbreviation, String stateName, BigDecimal taxRate) {
        this.stateAbbreviation = stateAbbreviation;
        this.stateName = stateName;
        this.taxRate = taxRate;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public String getStateAbbreviation() {
        return stateAbbreviation;
    }

    public String getStateName() {
        return stateName;
    }

    @Override
    public int hashCode() {
        int result = 7;
        result *= 71 + (stateName == null ? 0 : stateName.hashCode());
        result *= 71 + (stateAbbreviation == null ? 0 : stateAbbreviation.hashCode());
        result *= 71 + (taxRate == null ? 0 : taxRate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if(other == null) return false;
        if(other == this) return true;
        if(other.getClass() != getClass()) return false;

        final StateTax otherStateTax = (StateTax) other;

        if(stateName == null) {
            if(otherStateTax.stateName != null) {
                return false;
            }
        } else if(!stateName.equals(otherStateTax.stateName)) {
            return false;
        }

        if(stateAbbreviation == null) {
            if(otherStateTax.stateAbbreviation != null) {
                return false;
            }
        } else if(!stateAbbreviation.equals(otherStateTax.stateAbbreviation)) {
            return false;
        }

        if(taxRate == null) {
            return otherStateTax.taxRate == null;
        }
        return otherStateTax.taxRate.compareTo(taxRate) == 0;
    }


    @Override
    public String toString() {
        return String.format("%s (%s) - %.2f%%",
                stateName,
                stateAbbreviation,
                taxRate);
    }
}
