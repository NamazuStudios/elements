package dev.getelements.elements.rt.transact.unix;

/**
 * Thrown when the Garbage Collector has already collected the requested revision. This should be an extremely rare
 * occurrence, but it is possible. The correct action for handling this exception is to try with a new current
 * revision as the revision was collected between the time it was obtained and when it was collected.
 */
public class UnixFSRevisionCollectedException extends Exception {}
