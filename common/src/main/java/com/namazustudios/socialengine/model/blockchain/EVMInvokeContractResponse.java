package com.namazustudios.socialengine.model.blockchain;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class EVMInvokeContractResponse {

    @ApiModelProperty("The removed status.")
    private String transactionHash;

    @ApiModelProperty("The removed status.")
    private long transactionIndex;

    @ApiModelProperty("The removed status.")
    private String blockHash;

    @ApiModelProperty("The removed status.")
    private long blockNumber;

    @ApiModelProperty("The removed status.")
    private long cumulativeGasUsed;

    @ApiModelProperty("The removed status.")
    private long gasUsed;

    @ApiModelProperty("The removed status.")
    private String contractAddress;

    @ApiModelProperty("The removed status.")
    private String root;

    // status is only present on Byzantium transactions onwards
    // see EIP 658 https://github.com/ethereum/EIPs/pull/658
    @ApiModelProperty("The removed status.")
    private long status;

    @ApiModelProperty("The removed status.")
    private String from;

    @ApiModelProperty("The removed status.")
    private String to;

    @ApiModelProperty("The removed status.")
    private List<EVMTransactionLog> logs;

    @ApiModelProperty("The removed status.")
    private String logsBloom;

    @ApiModelProperty("The removed status.")
    private String revertReason;

    @ApiModelProperty("The removed status.")
    private String type;

    @ApiModelProperty("The removed status.")
    private String effectiveGasPrice;

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public long getTransactionIndex() {
        return transactionIndex;
    }

    public long getTransactionIndexRaw() {
        return transactionIndex;
    }

    public void setTransactionIndex(long transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public long getCumulativeGasUsed() {
        return cumulativeGasUsed;
    }

    public void setCumulativeGasUsed(long cumulativeGasUsed) {
        this.cumulativeGasUsed = cumulativeGasUsed;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = gasUsed;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public boolean isStatusOK() {
//        if (null == getStatus()) {
//            return true;
//        }

        return getStatus() == 1;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<EVMTransactionLog> getLogs() {
        return logs;
    }

    public void setLogs(List<EVMTransactionLog> logs) {
        this.logs = logs;
    }

    public String getLogsBloom() {
        return logsBloom;
    }

    public void setLogsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
    }

    public String getRevertReason() {
        return revertReason;
    }

    public void setRevertReason(String revertReason) {
        this.revertReason = revertReason;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEffectiveGasPrice() {
        return effectiveGasPrice;
    }

    public void setEffectiveGasPrice(String effectiveGasPrice) {
        this.effectiveGasPrice = effectiveGasPrice;
    }
    
}
