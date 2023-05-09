package dev.getelements.elements.rt.remote;

import java.util.List;
import java.util.Objects;

public class Route  {

    List<Object> address;

    private String routingStrategyName;

    Class<? extends RoutingStrategy> routingStrategyType;

    public List<Object> getAddress() {
        return address;
    }

    public void setAddress(List<Object> address) {
        this.address = address;
    }

    public Class<? extends RoutingStrategy> getRoutingStrategyType() {
        return routingStrategyType;
    }

    public void setRoutingStrategyType(Class<? extends RoutingStrategy> routingStrategyType) {
        this.routingStrategyType = routingStrategyType;
    }

    public void setRoutingStrategyName(String routingStrategyName) {
        this.routingStrategyName = routingStrategyName;
    }

    public String getRoutingStrategyName() {
        return routingStrategyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route)) return false;
        Route route = (Route) o;
        return Objects.equals(getAddress(), route.getAddress()) &&
                Objects.equals(getRoutingStrategyName(), route.getRoutingStrategyName()) &&
                Objects.equals(getRoutingStrategyType(), route.getRoutingStrategyType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getRoutingStrategyName(), getRoutingStrategyType());
    }

    @Override
    public String toString() {
        return "Route{" +
                "address=" + address +
                ", routingStrategyName='" + routingStrategyName + '\'' +
                ", routingStrategyType=" + routingStrategyType +
                '}';
    }

}
