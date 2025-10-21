package dev.getelements.elements.webui.angular;

import java.util.Objects;

public class WebUIApplicationConfiguration {

    private WebUIApplicationApiConfiguration api;

    public WebUIApplicationApiConfiguration getApi() {
        return api;
    }

    public void setApi(WebUIApplicationApiConfiguration api) {
        this.api = api;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebUIApplicationConfiguration that = (WebUIApplicationConfiguration) o;
        return Objects.equals(getApi(), that.getApi());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getApi());
    }

}
