package com.namazustudios.socialengine.rt.remote;

import java.util.Objects;

public class Route  {

    Object address;

    Class<? extends RoutingStrategy> routingStrategyType;

    public Object getAddress() {
        return address;
    }

    public void setAddress(Object address) {
        this.address = address;
    }

    public Class<? extends RoutingStrategy> getRoutingStrategyType() {
        return routingStrategyType;
    }

    public void setRoutingStrategyType(Class<? extends RoutingStrategy> routingStrategyType) {
        this.routingStrategyType = routingStrategyType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route)) return false;
        Route route = (Route) o;
        return Objects.equals(getAddress(), route.getAddress()) &&
                Objects.equals(getRoutingStrategyType(), route.getRoutingStrategyType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getRoutingStrategyType());
    }

}
