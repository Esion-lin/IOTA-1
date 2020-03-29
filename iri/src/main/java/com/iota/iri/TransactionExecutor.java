/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package com.iota.iri;

import com.iota.iri.vm.TransactionExecutionSummary;
import com.iota.iri.vm.Repository;
import com.iota.iri.vm.EVMTransaction;
import com.iota.iri.vm.AccountState;
import org.apache.commons.lang3.tuple.Pair;
import com.iota.iri.vm.config.BlockchainConfig;
import com.iota.iri.vm.config.CommonConfig;
import com.iota.iri.vm.config.SystemProperties;
import com.iota.iri.vm.config.blockchain.PetersburgConfig;
// import com.iota.iri.vm.db.BlockStore;
import com.iota.iri.vm.ContractDetails;
// import org.ethereum.listener.EthereumListener;
// import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.util.ByteArraySet;
import com.iota.iri.vm.vm.*;
import com.iota.iri.vm.vm.hook.VMHook;
import com.iota.iri.vm.program.Program;
import com.iota.iri.vm.program.ProgramResult;
import com.iota.iri.vm.program.invoke.ProgramInvoke;
import com.iota.iri.vm.program.invoke.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ArrayUtils.getLength;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static com.iota.iri.vm.util.BIUtil.*;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.util.ByteUtil.toHexString;
import static com.iota.iri.vm.vm.VMUtils.saveProgramTraceFile;
import static com.iota.iri.vm.vm.VMUtils.zipAndEncode;

/**
 * @author Roman Mandeleil
 * @since 19.12.2014
 */
public class TransactionExecutor{

    private static Logger log = LoggerFactory.getLogger(TransactionExecutor.class);

    private static final Logger logger = LoggerFactory.getLogger("execute");
    private static final Logger stateLogger = LoggerFactory.getLogger("state");

    SystemProperties config;
    CommonConfig commonConfig;
    BlockchainConfig blockchainConfig;

    private EVMTransaction tx;
    private Repository track;
    private Repository cacheTrack;
    // private BlockStore blockStore;
    // private final long gasUsedInTheBlock;
    private boolean readyToExecute = false;
    private String execError;

    private ProgramInvokeFactory programInvokeFactory;
    // private byte[] coinbase;

    // private TransactionReceipt receipt;
    private ProgramResult result = new ProgramResult();
    // private Block currentBlock;

    // private final EthereumListener listener;

    private VM vm;
    private Program program;

    PrecompiledContracts.PrecompiledContract precompiledContract;

    // BigInteger m_endGas = BigInteger.ZERO;
    // long basicTxCost = 0;
    List<LogInfo> logs = null;

    private ByteArraySet touchedAccounts = new ByteArraySet();

    boolean localCall = false;
    private final VMHook vmHook;

    // public TransactionExecutor(Transaction tx, Repository track, BlockStore blockStore,
    //                            ProgramInvokeFactory programInvokeFactory, Block currentBlock) {

    //     this(tx, track, blockStore, programInvokeFactory, currentBlock, new EthereumListenerAdapter(), 0, VMHook.EMPTY);
    // }
    public TransactionExecutor(EVMTransaction tx, Repository track,
    ProgramInvokeFactory programInvokeFactory) {
        this(tx, track, programInvokeFactory, VMHook.EMPTY);
    }
    // public TransactionExecutor(Transaction tx,  Repository track, BlockStore blockStore,
    //                            ProgramInvokeFactory programInvokeFactory, Block currentBlock,
    //                            EthereumListener listener, long gasUsedInTheBlock) {
    //     this(tx, track, blockStore, programInvokeFactory, currentBlock, listener, gasUsedInTheBlock, VMHook.EMPTY);
    // }

    // public TransactionExecutor(Transaction tx, Repository track, BlockStore blockStore,
    //                            ProgramInvokeFactory programInvokeFactory, Block currentBlock,
    //                            EthereumListener listener, long gasUsedInTheBlock, VMHook vmHook) {

    //     this.tx = tx;
    //     this.track = track;
    //     this.cacheTrack = track.startTracking();
    //     this.blockStore = blockStore;
    //     this.programInvokeFactory = programInvokeFactory;
    //     this.currentBlock = currentBlock;
    //     this.listener = listener;
    //     this.gasUsedInTheBlock = gasUsedInTheBlock;
    //     this.m_endGas = toBI(tx.getGasLimit());
    //     this.vmHook = isNull(vmHook) ? VMHook.EMPTY : vmHook;

    //     withCommonConfig(CommonConfig.getDefault());
    // }
    public TransactionExecutor(EVMTransaction tx, Repository track, 
    ProgramInvokeFactory programInvokeFactory, VMHook vmHook) {

    this.tx = tx;
    this.track = track;
    this.cacheTrack = track.startTracking();
    this.programInvokeFactory = programInvokeFactory;
    this.vmHook = isNull(vmHook) ? VMHook.EMPTY : vmHook;

    withCommonConfig(CommonConfig.getDefault());
    }

    public void setLogger(Logger log){
        this.log = log;
    }
    public TransactionExecutor withCommonConfig(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
        this.config = commonConfig.systemProperties();
        this.blockchainConfig = config.getBlockchainConfig().getConfigForBlock(0);
        return this;
    }

    private void execError(String err) {
        logger.warn(err);
        execError = err;
    }

    /**
     * Do all the basic validation, if the executor
     * will be ready to run the transaction at the end
     * set readyToExecute = true
     */
    public void init() {
        // basicTxCost = tx.transactionCost(config.getBlockchainConfig(), currentBlock);

        if (localCall) {
            readyToExecute = true;
            return;
        }

        // BigInteger txGasLimit = new BigInteger(1, tx.getGasLimit());
        // BigInteger curBlockGasLimit = new BigInteger(1, currentBlock.getGasLimit());

        // boolean cumulativeGasReached = txGasLimit.add(BigInteger.valueOf(gasUsedInTheBlock)).compareTo(curBlockGasLimit) > 0;
        // if (cumulativeGasReached) {

        //     execError(String.format("Too much gas used in this block: Require: %s Got: %s", new BigInteger(1, currentBlock.getGasLimit()).longValue() - toBI(tx.getGasLimit()).longValue(), toBI(tx.getGasLimit()).longValue()));

        //     return;
        // }

        // if (txGasLimit.compareTo(BigInteger.valueOf(basicTxCost)) < 0) {

        //     execError(String.format("Not enough gas for transaction execution: Require: %s Got: %s", basicTxCost, txGasLimit));

        //     return;
        // }

        BigInteger reqNonce = track.getNonce(tx.getSender());
        BigInteger txNonce = toBI(tx.getNonce());
        if (isNotEqual(reqNonce, txNonce)) {
            execError(String.format("Invalid nonce: required: %s , tx.nonce: %s", reqNonce, txNonce));

            return;
        }

        // BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(txGasLimit);
        // BigInteger totalCost = toBI(tx.getValue()).add(txGasCost);
        BigInteger senderBalance = track.getBalance(tx.getSender());

        // if (!isCovers(senderBalance, totalCost)) {

        //     execError(String.format("Not enough cash: Require: %s, Sender cash: %s", totalCost, senderBalance));

        //     return;
        // }

        // if (!blockchainConfig.acceptTransactionSignature(tx)) {
        //     execError("Transaction signature not accepted: " + tx.getSignature());
        //     return;
        // }

        readyToExecute = true;
    }

    public void execute() {

        if (!readyToExecute) return;
        if (localCall) {

        // if (!localCall) {
            // System.out.println("=====Sender is "+DatatypeConverter.printHexBinary(tx.getSender())+"=======");
            // System.out.println("=====Sender AccountState Exists? "+track.getAccountState(tx.getSender())+"=======");
            // System.out.println("=====Sender Nonce "+DatatypeConverter.printHexBinary(tx.getNonce())+"=======");

            BigInteger aaa = track.increaseNonce(tx.getSender());
            // System.out.println("=====Sender Nonce "+aaa.toString()+"=======");


            // BigInteger txGasLimit = toBI(tx.getGasLimit());
            // BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(txGasLimit);
            // track.addBalance(tx.getSender(), txGasCost.negate());

        //     if (logger.isInfoEnabled())
        //         logger.info("Paying: txGasCost: [{}], gasPrice: [{}], gasLimit: [{}]", txGasCost, toBI(tx.getGasPrice()), txGasLimit);
        // }
        }

        if (tx.isContractCreation()) {
            create();
        } else {
            call();
        }
    }

    private void call() {
        log.info("call");
        if (!readyToExecute) return;

        byte[] targetAddress = tx.getReceiveAddress();
        precompiledContract = PrecompiledContracts.getContractForAddress(DataWord.of(targetAddress), blockchainConfig);

        if (precompiledContract != null) {
            // long requiredGas = precompiledContract.getGasForData(tx.getData());

            // BigInteger spendingGas = BigInteger.valueOf(requiredGas).add(BigInteger.valueOf(basicTxCost));

            // if (!localCall && m_endGas.compareTo(spendingGas) < 0) {
                // no refund
                // no endowment
            //     execError("Out of Gas calling precompiled contract 0x" + toHexString(targetAddress) +
            //             ", required: " + spendingGas + ", left: " + m_endGas);
            //     m_endGas = BigInteger.ZERO;
            //     return;
            // } else {

                // m_endGas = m_endGas.subtract(spendingGas);

                // FIXME: save return for vm trace
                Pair<Boolean, byte[]> out = precompiledContract.execute(tx.getData());

                // if (!out.getLeft()) {
                //     execError("Error executing precompiled contract 0x" + toHexString(targetAddress));
                //     m_endGas = BigInteger.ZERO;
                //     return;
                // }
            // }

        } else {

            byte[] code = track.getCode(targetAddress);
            if (isEmpty(code)) {
                // m_endGas = m_endGas.subtract(BigInteger.valueOf(basicTxCost));
                // result.spendGas(basicTxCost);
            } else {
                ProgramInvoke programInvoke =
                        programInvokeFactory.createProgramInvoke(tx, cacheTrack, track);

                this.vm = new VM(config, vmHook);
                this.program = new Program(track.getCodeHash(targetAddress), code, programInvoke, tx, config, vmHook).withCommonConfig(commonConfig);
            }
        }

        BigInteger endowment = toBI(tx.getValue());
        transfer(cacheTrack, tx.getSender(), targetAddress, endowment);

        touchedAccounts.add(targetAddress);
    }

    private void create() {
        // log.info("create");
        byte[] newContractAddress = tx.getContractAddress();
        // log.info("[ContractAddress]"+ toHexString(newContractAddress));

        AccountState existingAddr = cacheTrack.getAccountState(newContractAddress);
        // log.info("Addr not Existing?"+(existingAddr==null));
        if (existingAddr != null && existingAddr.isContractExist(blockchainConfig)) {
            execError("Trying to create a contract with existing contract address: 0x" + toHexString(newContractAddress));
            // m_endGas = BigInteger.ZERO;
            return;
        }

        //In case of hashing collisions (for TCK tests only), check for any balance before createAccount()
        BigInteger oldBalance = track.getBalance(newContractAddress);
        cacheTrack.createAccount(tx.getContractAddress());
        cacheTrack.addBalance(newContractAddress, oldBalance);
        if (blockchainConfig.eip161()) {
            cacheTrack.increaseNonce(newContractAddress);
        }

        if (isEmpty(tx.getData())) {
            // m_endGas = m_endGas.subtract(BigInteger.valueOf(basicTxCost));
            // result.spendGas(basicTxCost);
        } else {
            Repository originalRepo = track;
            // Some TCK tests have storage only addresses (no code, zero nonce etc) - impossible situation in the real network
            // So, we should clean up it before reuse, but as tx not always goes successful, state should be correctly
            // reverted in that case too
            if (cacheTrack.hasContractDetails(newContractAddress)) {
                originalRepo = track.clone();
                originalRepo.delete(newContractAddress);
            }
            // log.info("tx:{},cacheTrack:{},originalRepo:{}",tx==null,cacheTrack==null,originalRepo==null);
            ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(tx, 
                    cacheTrack, originalRepo);

            this.vm = new VM(config, vmHook);
            this.program = new Program(tx.getData(), programInvoke, tx, config, vmHook).withCommonConfig(commonConfig);

            // reset storage if the contract with the same address already exists
            // TCK test case only - normally this is near-impossible situation in the real network
            ContractDetails contractDetails = program.getStorage().getContractDetails(newContractAddress);
            contractDetails.deleteStorage();
        }

        BigInteger endowment = toBI(tx.getValue());
        transfer(cacheTrack, tx.getSender(), newContractAddress, endowment);

        touchedAccounts.add(newContractAddress);
    }

    public void go() {
        if (!readyToExecute) return;

        try {

            if (vm != null) {

                if (config.playVM())
                    vm.play(program);

                result = program.getResult();


                if (tx.isContractCreation() && !result.isRevert()) {
                    if (getLength(result.getHReturn()) > blockchainConfig.getConstants().getMAX_CONTRACT_SZIE()) {
                        program.setRuntimeFailure(Program.Exception.notEnoughSpendingGas("Contract size too large: " + getLength(result.getHReturn()),
                                0, program));
                        result = program.getResult();
                        result.setHReturn(EMPTY_BYTE_ARRAY);
                    } else {
                        cacheTrack.saveCode(tx.getContractAddress(), result.getHReturn());
                    }
                }

                // String err = config.getBlockchainConfig().getConfigForBlock(currentBlock.getNumber()).
                //         validateTransactionChanges(blockStore, currentBlock, tx, null);
                // if (err != null) {
                //     program.setRuntimeFailure(new RuntimeException("Transaction changes validation failed: " + err));
                // }


                if (result.getException() != null || result.isRevert()) {
                    result.getDeleteAccounts().clear();
                    result.getLogInfoList().clear();
                    result.resetFutureRefund();
                    rollback();

                    if (result.getException() != null) {
                        throw result.getException();
                    } else {
                        execError("REVERT opcode executed");
                    }
                } else {
                    touchedAccounts.addAll(result.getTouchedAccounts());
                    cacheTrack.commit();
                }

            } else {
                cacheTrack.commit();
            }

        } catch (Throwable e) {

            // TODO: catch whatever they will throw on you !!!
//            https://github.com/ethereum/cpp-ethereum/blob/develop/libethereum/Executive.cpp#L241
            rollback();
            // m_endGas = BigInteger.ZERO;
            execError(e.getMessage());
        }
    }

    private void rollback() {

        cacheTrack.rollback();

        // remove touched account
        touchedAccounts.remove(
                tx.isContractCreation() ? tx.getContractAddress() : tx.getReceiveAddress());
    }

    public TransactionExecutionSummary finalization() {
        if (!readyToExecute) return null;

        TransactionExecutionSummary.Builder summaryBuilder = TransactionExecutionSummary.builderFor(tx)
                .gasLeftover(0)
                .logs(result.getLogInfoList())
                .result(result.getHReturn());
        // System.out.println("=============Finalization=========")    ;
        // System.out.println(result.getLogInfoList());
        // System.out.println(result.getHReturn());


        if (result != null) {
            // Accumulate refunds for suicides
            // result.addFutureRefund(result.getDeleteAccounts().size() * config.getBlockchainConfig().
            //         getConfigForBlock(currentBlock.getNumber()).getGasCost().getSUICIDE_REFUND());
            // long gasRefund = Math.min(Math.max(0, result.getFutureRefund()), getGasUsed() / 2);
            // System.out.println("=====111111======="+(tx.isContractCreation()? tx.getContractAddress() : tx.getReceiveAddress())+"=====111111=======");
            byte[] addr = tx.isContractCreation() ? tx.getContractAddress() : tx.getReceiveAddress();
            // m_endGas = m_endGas.add(BigInteger.valueOf(gasRefund));

            summaryBuilder
                    .gasUsed(toBI(0))
                    .gasRefund(toBI(0))
                    .deletedAccounts(result.getDeleteAccounts())
                    .internalTransactions(result.getInternalTransactions());
            // System.out.println(result.getDeleteAccounts());
            // System.out.println(result.getInternalTransactions());
            ContractDetails contractDetails = track.getContractDetails(addr);
            if (contractDetails != null) {
                // TODO
            //    summaryBuilder.storageDiff(track.getContractDetails(addr).getStorage());
//
//                if (program != null) {
//                    summaryBuilder.touchedStorage(contractDetails.getStorage(), program.getStorageDiff());
//                }
            }

            if (result.getException() != null) {
                summaryBuilder.markAsFailed();
            }
        }

        TransactionExecutionSummary summary = summaryBuilder.build();
        // System.out.println(summary.getEncoded().toString());

        // Refund for gas leftover
        track.addBalance(tx.getSender(), summary.getLeftover().add(summary.getRefund()));
        logger.info("Pay total refund to sender: [{}], refund val: [{}]", toHexString(tx.getSender()), summary.getRefund());

        // Transfer fees to miner
        // track.addBalance(coinbase, summary.getFee());
        // touchedAccounts.add(coinbase);
        // logger.info("Pay fees to miner: [{}], feesEarned: [{}]", toHexString(coinbase), summary.getFee());
        String storageChangeHash ="";
        for(String s : result.getStorageAKVHash()){
            // System.out.println(s);
            storageChangeHash += s;
        }

        summary.setStorageChangeHash(storageChangeHash);

        if (result != null) {
            logs = result.getLogInfoList();
            // Traverse list of suicides
            for (DataWord address : result.getDeleteAccounts()) {
                track.delete(address.getLast20Bytes());
            }
        }

        // if (blockchainConfig.eip161()) {
        //     for (byte[] acctAddr : touchedAccounts) {
        //         AccountState state = track.getAccountState(acctAddr);
        //         if (state != null && state.isEmpty()) {
        //             track.delete(acctAddr);
        //         }
        //     }
        // }


        // listener.onTransactionExecuted(summary);

        // if (config.vmTrace() && program != null && result != null) {
        //     String trace = program.getTrace()
        //             .result(result.getHReturn())
        //             .error(result.getException())
        //             .toString();


        //     if (config.vmTraceCompressed()) {
        //         trace = zipAndEncode(trace);
        //     }

        //     String txHash = toHexString(tx.getHash());
        //     saveProgramTraceFile(config, txHash, trace);
        //     // listener.onVMTraceCreated(txHash, trace);
        // }
        return summary;
    }

    public TransactionExecutor setLocalCall(boolean localCall) {
        this.localCall = localCall;
        return this;
    }


//     public TransactionReceipt getReceipt() {
// //         if (receipt == null) {
// //             receipt = new TransactionReceipt();
// //             // long totalGasUsed = gasUsedInTheBlock + getGasUsed();
// //             receipt.setCumulativeGas(0);
// //             receipt.setTransaction(tx);
// //             receipt.setLogInfoList(getVMLogs());
// //             receipt.setGasUsed(getGasUsed());
// //             receipt.setExecutionResult(getResult().getHReturn());
// //             receipt.setError(execError);
// // //            receipt.setPostTxState(track.getRoot()); // TODO later when RepositoryTrack.getRoot() is implemented
// //         }
//         return new TransactionReceipt;
//     }

    public List<LogInfo> getVMLogs() {
        return logs;
    }

    public ProgramResult getResult() {
        return result;
    }

    public long getGasUsed() {
        // return toBI(tx.getGasLimit()).subtract(BigInteger.valueOf(0)).longValue();
        return 0;
    }

}
