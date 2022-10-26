package com.namazustudios.socialengine.formidium;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.proxy.ProxyServlet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import static com.namazustudios.socialengine.formidium.FormidiumConstants.FORMIDIUM_API_KEY;
import static com.namazustudios.socialengine.formidium.FormidiumConstants.FORMIDIUM_API_KEY_HEADER;

public class  FormidiumProxyServlet extends ProxyServlet.Transparent {

    private String formidiumApiKey;

    @Override
    protected void addProxyHeaders(final HttpServletRequest clientRequest, final Request proxyRequest) {
        super.addProxyHeaders(clientRequest, proxyRequest);
        proxyRequest.headers(httpFields -> httpFields.add(FORMIDIUM_API_KEY_HEADER, getFormidiumApiKey()));
    }

    public String getFormidiumApiKey() {
        return formidiumApiKey;
    }

    @Inject
    public void setFormidiumApiKey(@Named(FORMIDIUM_API_KEY) String formidiumApiKey) {
        this.formidiumApiKey = formidiumApiKey;
    }

}
