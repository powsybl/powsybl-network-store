package com.powsybl.network.store.model;

public interface LimitSelector {

    default LimitsAttributes getLimits(TemporaryLimitType type, int side) {
        throw new UnsupportedOperationException("Unsupported by default.");
    }

    default void setLimits(TemporaryLimitType type, int side, LimitsAttributes limits) {
        throw new UnsupportedOperationException("Unsupported by default.");
    }

}
