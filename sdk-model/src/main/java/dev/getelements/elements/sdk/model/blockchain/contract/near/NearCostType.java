package dev.getelements.elements.sdk.model.blockchain.contract.near;

/** Represents the cost types for NEAR protocol operations. */
public enum NearCostType {
    /** Base cost. */
    BASE,
    /** Base cost for loading a contract. */
    CONTRACT_LOADING_BASE,
    /** Per-byte cost for loading a contract. */
    CONTRACT_LOADING_BYTES,
    /** Base cost for compiling a contract. */
    CONTRACT_COMPILE_BASE,
    /** Per-byte cost for compiling a contract. */
    CONTRACT_COMPILE_BYTES,
    /** Cost for a function call. */
    FUNCTION_CALL,
    /** Base cost for logging. */
    LOG_BASE,
    /** Per-byte cost for logging. */
    LOG_BYTE,
    /** Cost for a new receipt. */
    NEW_RECEIPT,
    /** Cost for reading a cached trie node. */
    READ_CACHED_TRIE_NODE,
    /** Base cost for reading memory. */
    READ_MEMORY_BASE,
    /** Per-byte cost for reading memory. */
    READ_MEMORY_BYTE,
    /** Base cost for reading a register. */
    READ_REGISTER_BASE,
    /** Per-byte cost for reading a register. */
    READ_REGISTER_BYTE,
    /** Cost for staking. */
    STAKE,
    /** Base cost for a storage read. */
    STORAGE_READ_BASE,
    /** Per-byte cost for a storage read key. */
    STORAGE_READ_KEY_BYTE,
    /** Per-byte cost for a storage read value. */
    STORAGE_READ_VALUE_BYTE,
    /** Base cost for a storage remove. */
    STORAGE_REMOVE_BASE,
    /** Per-byte cost for a storage remove key. */
    STORAGE_REMOVE_KEY_BYTE,
    /** Per-byte cost for a storage remove return value. */
    STORAGE_REMOVE_RET_VALUE_BYTE,
    /** Base cost for checking if a storage key exists. */
    STORAGE_HAS_KEY_BASE,
    /** Per-byte cost for checking if a storage key exists. */
    STORAGE_HAS_KEY_BYTE,
    /** Base cost for a storage write. */
    STORAGE_WRITE_BASE,
    /** Per-byte cost for a storage write eviction. */
    STORAGE_WRITE_EVICTED_BYTE,
    /** Per-byte cost for a storage write key. */
    STORAGE_WRITE_KEY_BYTE,
    /** Per-byte cost for a storage write value. */
    STORAGE_WRITE_VALUE_BYTE,
    /** Cost for touching a trie node. */
    TOUCHING_TRIE_NODE,
    /** Base cost for UTF-8 decoding. */
    UTF8_DECODING_BASE,
    /** Per-byte cost for UTF-8 decoding. */
    UTF8_DECODING_BYTE,
    /** Cost per WASM instruction. */
    WASM_INSTRUCTION,
    /** Base cost for writing memory. */
    WRITE_MEMORY_BASE,
    /** Per-byte cost for writing memory. */
    WRITE_MEMORY_BYTE,
    /** Base cost for writing a register. */
    WRITE_REGISTER_BASE,
    /** Per-byte cost for writing a register. */
    WRITE_REGISTER_BYTE,
    /** Cost for a token transfer. */
    TRANSFER,
    /** Cost for returning a promise. */
    PROMISE_RETURN
}
