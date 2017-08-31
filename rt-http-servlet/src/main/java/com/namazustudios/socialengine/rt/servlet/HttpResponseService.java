package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.http.HttpResponse;

public interface HttpResponseService {

    HttpResponse getResponse(final Response response);

}
