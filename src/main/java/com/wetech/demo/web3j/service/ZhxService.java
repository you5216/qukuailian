package com.wetech.demo.web3j.service;

import com.wetech.demo.web3j.contracts.zhx.ZHX;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZhxService {

	private final Web3j web3j;
	private final Credentials credentials;
	private final ContractGasProvider gasProvider;

	private ZHX contract;
	@Getter
	private String contractAddress;

	public String getSenderAddress() {
		return credentials.getAddress();
	}

	public CompletableFuture<String> deployContract() {
		log.info("Deploying ZHX contract...");
		return ZHX.deploy(web3j, credentials, gasProvider)
				.sendAsync()
				.thenApply(contract -> {
					this.contract = contract;
					this.contractAddress = contract.getContractAddress();
					log.info("ZHX contract deployed to: {}", contractAddress);
					return contractAddress;
				});
	}

	public void loadContract(String contractAddress) {
		log.info("Loading ZHX contract from address: {}", contractAddress);
		this.contract = ZHX.load(contractAddress, web3j, credentials, gasProvider);
		this.contractAddress = contractAddress;
	}

	public CompletableFuture<TransactionReceipt> mint(String to, BigInteger amount) {
		ensureLoaded();
		log.info("Mint {} tokens to {} on contract {}", amount, to, contractAddress);
		return contract.mint(to, amount).sendAsync();
	}

	public CompletableFuture<TransactionReceipt> transfer(String to, BigInteger amount) {
		ensureLoaded();
		log.info("Transfer {} tokens to {} on contract {}", amount, to, contractAddress);
		return contract.transfer(to, amount).sendAsync();
	}

	public CompletableFuture<BigInteger> balanceOf(String owner) {
		ensureLoaded();
		log.info("Query balance of {} on contract {}", owner, contractAddress);
		return contract.balanceOf(owner).sendAsync();
	}

	public CompletableFuture<BigInteger> allowance(String owner, String spender) {
		ensureLoaded();
		log.info("Query allowance owner {} -> spender {} on contract {}", owner, spender, contractAddress);
		return contract.allowance(owner, spender).sendAsync();
	}

	public CompletableFuture<TransactionReceipt> approve(String spender, BigInteger amount) {
		ensureLoaded();
		log.info("Approve {} tokens for spender {} on contract {}", amount, spender, contractAddress);
		return contract.approve(spender, amount).sendAsync();
	}

	public CompletableFuture<TransactionReceipt> transferFrom(String from, String to, BigInteger amount) {
		ensureLoaded();
		log.info("TransferFrom {} => {} amount {} on contract {}", from, to, amount, contractAddress);
		return contract.transferFrom(from, to, amount).sendAsync();
	}

	private void ensureLoaded() {
		if (contract == null) {
			throw new IllegalStateException("Contract not deployed or loaded");
		}
	}
}


