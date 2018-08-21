package com.namazustudios.socialengine.client.controlpanel.view.gameon;

import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.namazustudios.socialengine.client.rest.client.gameon.GameOnPrizesClient;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.admin.GameOnGetPrizeListResponse;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.lang.Math.floor;
import static java.lang.Math.min;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class PrizeDataProvider extends AsyncDataProvider<GameOnGetPrizeListResponse.Prize> {

    @Inject
    private GameOnPrizesClient prizesClient;

    private List<GameOnGetPrizeListResponse.Prize> prizeList = emptyList();

    private GameOnApplicationConfiguration gameOnApplicationConfiguration;

    private final List<BiConsumer<Method, Throwable>> prizesFailedListeners = new ArrayList<>();

    private final List<BiConsumer<Method, GameOnGetPrizeListResponse>> prizesLoadedListeners = new ArrayList<>();

    @Override
    protected void onRangeChanged(final HasData<GameOnGetPrizeListResponse.Prize> display) {

        final Range visibleRange = display.getVisibleRange();

        final int prizeCount = prizeList.size();
        final int visibleStart = visibleRange.getStart();
        final int visibleLength = visibleRange.getLength();

        final List<GameOnGetPrizeListResponse.Prize> data =
            prizeList.isEmpty()                         ? prizeList   :                      // No Prizes
            prizeList.size() <= visibleRange.getStart() ? emptyList() :                      // Scrolling past data set
            prizeList.subList(visibleStart, min(prizeCount, visibleStart + visibleLength));  // In-range of data

        display.setRowData(visibleStart, data);

    }

    public GameOnApplicationConfiguration getGameOnApplicationConfiguration() {
        return gameOnApplicationConfiguration;
    }

    public void clear() {
        prizeList = emptyList();
        updateRowCount(0, true);
    }

    public void reconfigure(final GameOnApplicationConfiguration gameOnApplicationConfiguration) {

        this.gameOnApplicationConfiguration = gameOnApplicationConfiguration;

        final String applicationId = gameOnApplicationConfiguration.getParent().getId();
        final String configurationId = gameOnApplicationConfiguration.getId();

        prizesClient.getPrizes(applicationId, configurationId, new MethodCallback<GameOnGetPrizeListResponse>() {

            @Override
            public void onFailure(final Method method, final Throwable exception) {
                prizesFailedListeners
                    .stream()
                    .collect(toList())
                    .forEach(c -> c.accept(method, exception));
            }

            @Override
            public void onSuccess(final Method method, final GameOnGetPrizeListResponse response) {
                prizeList = response.getPrizes();
                updateRowData(0, prizeList);
                prizesLoadedListeners
                    .stream()
                    .collect(toList())
                    .forEach(c -> c.accept(method, response));
            }

        });

    }

    public void addPrizesFailedListener(final BiConsumer<Method, Throwable> prizesFailedListener) {
        prizesFailedListeners.add(prizesFailedListener);
    }

    public void addPrizesLoadedListener(final BiConsumer<Method, GameOnGetPrizeListResponse> prizesLoadedListener) {
        prizesLoadedListeners.add(prizesLoadedListener);
    }

}
