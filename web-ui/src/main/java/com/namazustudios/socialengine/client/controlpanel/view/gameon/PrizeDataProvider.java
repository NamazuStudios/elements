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
import java.util.function.Consumer;

import static java.lang.Math.min;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class PrizeDataProvider extends AsyncDataProvider<GameOnGetPrizeListResponse.Prize> {

    @Inject
    private GameOnPrizesClient prizesClient;

    private List<GameOnGetPrizeListResponse.Prize> prizeList = emptyList();

    private GameOnApplicationConfiguration gameOnApplicationConfiguration;

    private final List<Consumer<Throwable>> prizesFailedListeners = new ArrayList<>();

    private final List<Consumer<GameOnGetPrizeListResponse>> prizesLoadedListeners = new ArrayList<>();

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

        final String adminApiKey = getGameOnApplicationConfiguration().getAdminApiKey();

        prizesClient.getPrizes(adminApiKey, new MethodCallback<GameOnGetPrizeListResponse>() {

            @Override
            public void onFailure(Method method, Throwable exception) {
                prizesFailedListeners
                    .stream()
                    .collect(toList())
                    .forEach(c -> c.accept(exception));
            }

            @Override
            public void onSuccess(Method method, GameOnGetPrizeListResponse response) {
                prizeList = response.getPrizes();
                prizesLoadedListeners
                    .stream()
                    .collect(toList())
                    .forEach(c -> c.accept(response));
            }

        });

    }

    public ListenerRegistration addPrizesFailedListener(final Consumer<Throwable> prizesFailedListener) {
        prizesFailedListeners.add(prizesFailedListener);
        return () -> prizesFailedListeners.remove(prizesFailedListener);
    }

    public ListenerRegistration addPrizesLoadedListener(final Consumer<GameOnGetPrizeListResponse> prizesLoadedListener) {
        prizesLoadedListeners.add(prizesLoadedListener);
        return () -> prizesLoadedListeners.remove(prizesLoadedListener);
    }

    @FunctionalInterface
    public interface ListenerRegistration { void remove(); }

}
