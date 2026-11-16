package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.dao.DeploymentHealthStatusDao;
import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeRecord;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeStatus;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.ElementDeploymentState;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

import static dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeStatus.FAILED;
import static dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeStatus.UNSTABLE;
import static java.util.stream.Collectors.toSet;

/**
 * Reports on Element deployments which are flagged as mission critical. Deployments are considered unhealthy when
 * the runtime failed to load them, when loading completed with errors, or when a deployment which is enabled in the
 * database has no live runtime on this instance.
 * <p>
 * That last case closes a real blind spot (#209): a pod whose reconcile froze mid-reload (or whose load is stuck in
 * retry-backoff) has <b>no</b> runtime record for the deployment at all -- the old record is removed at unload and
 * the new one is only added when the load completes. The old logic only inspected <i>active</i> runtimes, so a
 * deployment that was enabled but not running reported a perfectly healthy instance while its element routes were
 * dead -- and the frozen pod stayed in the load balancer's rotation.
 * <p>
 * A deployment must be missing for longer than {@link #DEPLOYMENT_ABSENCE_THRESHOLD_SECONDS} before it is reported:
 * a <i>normal</i> hot reload also removes the record for the duration of the new version's load (minutes in
 * practice), and that must not flap the health endpoint or cause the orchestrator to restart a healthy pod
 * mid-reload.
 */
public class StandardDeploymentHealthStatusDao implements DeploymentHealthStatusDao {

    /**
     * The attribute key for the absence threshold in seconds. Default is 600 seconds (ten minutes): long
     * enough to cover a slow hot reload's record-free window, short enough to flag a genuinely stuck
     * reconcile in a useful time.
     */
    @ElementDefaultAttribute(value = "600", description = "Seconds an enabled deployment may be absent from the instance's active runtimes before it is reported unhealthy.")
    public static final String DEPLOYMENT_ABSENCE_THRESHOLD_SECONDS =
            "dev.getelements.elements.health.deployment.absence.threshold.seconds";

    private static final Logger logger = LoggerFactory.getLogger(StandardDeploymentHealthStatusDao.class);

    private ElementRuntimeService elementRuntimeService;

    private ElementDeploymentDao elementDeploymentDao;

    private LongSupplier ticker = System::nanoTime;

    private int absenceThresholdSeconds = 600;

    /**
     * Tracks when each mission-critical deployment was first observed missing from the active runtimes, so a
     * deployment is only reported absent once it has been missing longer than the threshold. Deployments
     * which reappear are removed. Keyed by deployment ID.
     */
    private final Map<String, Long> missingSince = new ConcurrentHashMap<>();

    @Override
    public List<String> getUnhealthyMissionCriticalDeployments() {

        final var runtimes = getElementRuntimeService().getActiveRuntimes();

        final var unhealthy = new ArrayList<String>(
                runtimes
                        .stream()
                        .filter(runtime -> runtime.deployment().missionCritical())
                        .filter(runtime -> isUnhealthy(runtime.status()))
                        .map(runtime -> describe(runtime.deployment()))
                        .collect(Collectors.toSet()));

        unhealthy.addAll(missingMissionCriticalDeployments(runtimes));

        return unhealthy.stream().sorted().toList();

    }

    /**
     * Reports enabled, mission-critical deployments which have no live runtime on this instance. A FAILED
     * record is not a live runtime -- but a deployment with a FAILED record is already reported through the
     * record status above, so only deployments with no record at all are considered missing here.
     * <p>
     * A failed read of the enabled deployments leaves the missing-check without a data source: report
     * nothing for this cycle rather than a false-positive (or worse, a false all-clear based on stale
     * data).
     */
    private List<String> missingMissionCriticalDeployments(final List<RuntimeRecord> runtimes) {

        final List<ElementDeployment> enabled;

        try {
            enabled = getElementDeploymentDao().getElementDeploymentsByState(ElementDeploymentState.ENABLED);
        } catch (Exception ex) {
            logger.warn("Unable to read enabled deployments; skipping the missing-deployment health check.", ex);
            return List.of();
        }

        final var liveDeploymentIds = runtimes
                .stream()
                .filter(runtime -> runtime.status() != FAILED)
                .map(runtime -> runtime.deployment().id())
                .collect(toSet());

        final var now = getTicker().getAsLong();
        final var thresholdNanos = (long) getAbsenceThresholdSeconds() * 1_000_000_000L;

        final var results = new ArrayList<String>();
        final var stillMissing = enabled
                .stream()
                .filter(ElementDeployment::missionCritical)
                .filter(deployment -> !liveDeploymentIds.contains(deployment.id()))
                .collect(Collectors.toMap(ElementDeployment::id, deployment -> deployment));

        for (final var entry : stillMissing.entrySet()) {

            final var deploymentId = entry.getKey();
            final var firstMissingNanos = missingSince.computeIfAbsent(deploymentId, missing -> now);

            if (now - firstMissingNanos < thresholdNanos) {
                continue; // within the grace window (e.g. a hot reload's record-free load window)
            }

            results.add(describe(entry.getValue()));

        }

        // Deployments which are live again (or no longer enabled) leave the absence tracker.
        missingSince.keySet().removeIf(deploymentId -> !stillMissing.containsKey(deploymentId));

        return results;

    }

    private static boolean isUnhealthy(final RuntimeStatus status) {
        return FAILED.equals(status) || UNSTABLE.equals(status);
    }

    private static String describe(final ElementDeployment deployment) {
        return deployment.name() == null
                ? deployment.id()
                : "%s (%s)".formatted(deployment.name(), deployment.id());
    }

    public ElementRuntimeService getElementRuntimeService() {
        return elementRuntimeService;
    }

    @Inject
    public void setElementRuntimeService(final ElementRuntimeService elementRuntimeService) {
        this.elementRuntimeService = elementRuntimeService;
    }

    public ElementDeploymentDao getElementDeploymentDao() {
        return elementDeploymentDao;
    }

    @Inject
    public void setElementDeploymentDao(final ElementDeploymentDao elementDeploymentDao) {
        this.elementDeploymentDao = elementDeploymentDao;
    }

    public int getAbsenceThresholdSeconds() {
        return absenceThresholdSeconds;
    }

    @Inject
    public void setAbsenceThresholdSeconds(@Named(DEPLOYMENT_ABSENCE_THRESHOLD_SECONDS) final int absenceThresholdSeconds) {
        this.absenceThresholdSeconds = absenceThresholdSeconds;
    }

    public LongSupplier getTicker() {
        return ticker;
    }

    public void setTicker(final LongSupplier ticker) {
        this.ticker = ticker;
    }

}
