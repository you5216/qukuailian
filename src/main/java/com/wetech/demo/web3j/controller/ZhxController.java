package com.wetech.demo.web3j.controller;

import com.wetech.demo.web3j.service.ZhxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/api/zhx")
@RequiredArgsConstructor
public class ZhxController {

	private final ZhxService zhxService;

	private static final Pattern ETH_ADDRESS = Pattern.compile("^0x[0-9a-fA-F]{40}$");
	private static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";

	@PostMapping("/deploy")
	public CompletableFuture<ResponseEntity<Map<String, String>>> deployContract() {
		return zhxService.deployContract()
				.thenApply(address -> {
					Map<String, String> response = new HashMap<>();
					response.put("contractAddress", address);
					return ResponseEntity.ok(response);
				});
	}

	@PostMapping("/load")
	public ResponseEntity<Map<String, String>> loadContract(@RequestParam String address) {
		Map<String, String> response = new HashMap<>();
		if (address == null || !ETH_ADDRESS.matcher(address).matches()) {
			response.put("error", "Invalid contract address. Expect 0x + 40 hex chars.");
			return ResponseEntity.badRequest().body(response);
		}
		zhxService.loadContract(address);
		response.put("message", "Contract loaded successfully");
		response.put("contractAddress", address);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/mint")
	public CompletableFuture<ResponseEntity<Map<String, String>>> mint(
			@RequestParam String to,
			@RequestParam String amount
	) {
		Map<String, String> err = new HashMap<>();
		if (zhxService.getContractAddress() == null) {
			err.put("error", "Contract not deployed or loaded. Call /api/zhx/deploy or /api/zhx/load first.");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		if (to == null || !ETH_ADDRESS.matcher(to).matches() || ZERO_ADDRESS.equalsIgnoreCase(to)) {
			err.put("error", "Invalid recipient address. Expect 0x + 40 hex chars.");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		BigInteger v;
		try {
			v = new BigInteger(amount);
		} catch (Exception e) {
			err.put("error", "Invalid amount");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		if (v.signum() <= 0) {
			err.put("error", "Amount must be positive");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		return zhxService.mint(to, v)
				.thenApply(receipt -> {
					Map<String, String> res = new HashMap<>();
					res.put("transactionHash", receipt.getTransactionHash());
					res.put("blockNumber", receipt.getBlockNumber().toString());
					res.put("gasUsed", receipt.getGasUsed().toString());
					res.put("status", receipt.getStatus());
					res.put("contractAddress", zhxService.getContractAddress());
					return ResponseEntity.ok(res);
				})
				.exceptionally(ex -> {
					Map<String, String> e = new HashMap<>();
					e.put("error", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
					return ResponseEntity.badRequest().body(e);
				});
	}

	@PostMapping("/transfer")
	public CompletableFuture<ResponseEntity<Map<String, String>>> transfer(
			@RequestParam String to,
			@RequestParam String amount
	) {
		Map<String, String> err = new HashMap<>();
		if (zhxService.getContractAddress() == null) {
			err.put("error", "Contract not deployed or loaded. Call /api/zhx/deploy or /api/zhx/load first.");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		if (to == null || !ETH_ADDRESS.matcher(to).matches() || ZERO_ADDRESS.equalsIgnoreCase(to)) {
			err.put("error", "Invalid recipient address. Expect 0x + 40 hex chars.");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		BigInteger v;
		try {
			v = new BigInteger(amount);
		} catch (Exception e) {
			err.put("error", "Invalid amount");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		if (v.signum() <= 0) {
			err.put("error", "Amount must be positive");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		return zhxService.transfer(to, v)
				.thenApply(receipt -> {
					Map<String, String> res = new HashMap<>();
					res.put("transactionHash", receipt.getTransactionHash());
					res.put("blockNumber", receipt.getBlockNumber().toString());
					res.put("gasUsed", receipt.getGasUsed().toString());
					res.put("status", receipt.getStatus());
					res.put("contractAddress", zhxService.getContractAddress());
					return ResponseEntity.ok(res);
				})
				.exceptionally(ex -> {
					Map<String, String> e = new HashMap<>();
					e.put("error", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
					return ResponseEntity.badRequest().body(e);
				});
	}

	@GetMapping("/balanceOf")
	public CompletableFuture<ResponseEntity<Map<String, String>>> balanceOf(@RequestParam String address) {
		Map<String, String> err = new HashMap<>();
		if (zhxService.getContractAddress() == null) {
			err.put("error", "Contract not deployed or loaded. Call /api/zhx/deploy or /api/zhx/load first.");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		if (address == null || !ETH_ADDRESS.matcher(address).matches()) {
			err.put("error", "Invalid address. Expect 0x + 40 hex chars.");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		return zhxService.balanceOf(address)
				.thenApply(balance -> {
					Map<String, String> res = new HashMap<>();
					res.put("balance", balance.toString());
					res.put("contractAddress", zhxService.getContractAddress());
					return ResponseEntity.ok(res);
				})
				.exceptionally(ex -> {
					Map<String, String> e = new HashMap<>();
					e.put("error", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
					return ResponseEntity.badRequest().body(e);
				});
	}

	@GetMapping("/allowance")
	public CompletableFuture<ResponseEntity<Map<String, String>>> allowance(
			@RequestParam String owner,
			@RequestParam String spender
	) {
		Map<String, String> err = new HashMap<>();
		if (zhxService.getContractAddress() == null) {
			err.put("error", "Contract not deployed or loaded. Call /api/zhx/deploy or /api/zhx/load first.");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		if (owner == null || !ETH_ADDRESS.matcher(owner).matches() || ZERO_ADDRESS.equalsIgnoreCase(owner)) {
			err.put("error", "Invalid owner address. Expect 0x + 40 hex chars.");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		if (spender == null || !ETH_ADDRESS.matcher(spender).matches() || ZERO_ADDRESS.equalsIgnoreCase(spender)) {
			err.put("error", "Invalid spender address. Expect 0x + 40 hex chars.");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		return zhxService.allowance(owner, spender)
				.thenApply(value -> {
					Map<String, String> res = new HashMap<>();
					res.put("allowance", value.toString());
					res.put("contractAddress", zhxService.getContractAddress());
					return ResponseEntity.ok(res);
				})
				.exceptionally(ex -> {
					Map<String, String> e = new HashMap<>();
					e.put("error", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
					return ResponseEntity.badRequest().body(e);
				});
	}

	@PostMapping("/approve")
	public CompletableFuture<ResponseEntity<Map<String, String>>> approve(
			@RequestParam String spender,
			@RequestParam String amount
	) {
		Map<String, String> err = new HashMap<>();
		if (zhxService.getContractAddress() == null) {
			err.put("error", "Contract not deployed or loaded. Call /api/zhx/deploy or /api/zhx/load first.");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		if (spender == null || !ETH_ADDRESS.matcher(spender).matches() || ZERO_ADDRESS.equalsIgnoreCase(spender)) {
			err.put("error", "Invalid spender address. Expect 0x + 40 hex chars.");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		BigInteger v;
		try {
			v = new BigInteger(amount);
		} catch (Exception e) {
			err.put("error", "Invalid amount");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		if (v.signum() <= 0) {
			err.put("error", "Amount must be positive");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		return zhxService.approve(spender, v)
				.thenApply(receipt -> {
					Map<String, String> res = new HashMap<>();
					res.put("transactionHash", receipt.getTransactionHash());
					res.put("blockNumber", receipt.getBlockNumber().toString());
					res.put("gasUsed", receipt.getGasUsed().toString());
					res.put("status", receipt.getStatus());
					res.put("contractAddress", zhxService.getContractAddress());
					return ResponseEntity.ok(res);
				})
				.exceptionally(ex -> {
					Map<String, String> e = new HashMap<>();
					e.put("error", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
					return ResponseEntity.badRequest().body(e);
				});
	}

	@PostMapping("/transferFrom")
	public CompletableFuture<ResponseEntity<Map<String, String>>> transferFrom(
			@RequestParam String from,
			@RequestParam String to,
			@RequestParam String amount
	) {
		Map<String, String> err = new HashMap<>();
		if (zhxService.getContractAddress() == null) {
			err.put("error", "Contract not deployed or loaded. Call /api/zhx/deploy or /api/zhx/load first.");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		if (from == null || !ETH_ADDRESS.matcher(from).matches() || ZERO_ADDRESS.equalsIgnoreCase(from)) {
			err.put("error", "Invalid from address. Expect 0x + 40 hex chars.");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		if (to == null || !ETH_ADDRESS.matcher(to).matches() || ZERO_ADDRESS.equalsIgnoreCase(to)) {
			err.put("error", "Invalid to address. Expect 0x + 40 hex chars.");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		BigInteger v;
		try {
			v = new BigInteger(amount);
		} catch (Exception e) {
			err.put("error", "Invalid amount");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		if (v.signum() <= 0) {
			err.put("error", "Amount must be positive");
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(err));
		}
		return zhxService.transferFrom(from, to, v)
				.thenApply(receipt -> {
					Map<String, String> res = new HashMap<>();
					res.put("transactionHash", receipt.getTransactionHash());
					res.put("blockNumber", receipt.getBlockNumber().toString());
					res.put("gasUsed", receipt.getGasUsed().toString());
					res.put("status", receipt.getStatus());
					res.put("contractAddress", zhxService.getContractAddress());
					return ResponseEntity.ok(res);
				})
				.exceptionally(ex -> {
					Map<String, String> e = new HashMap<>();
					e.put("error", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
					return ResponseEntity.badRequest().body(e);
				});
	}

	@GetMapping("/sender")
	public ResponseEntity<Map<String, String>> getSenderAddress() {
		Map<String, String> res = new HashMap<>();
		res.put("sender", zhxService.getSenderAddress());
		return ResponseEntity.ok(res);
	}

	@GetMapping("/address")
	public ResponseEntity<Map<String, String>> getContractAddress() {
		String address = zhxService.getContractAddress();
		Map<String, String> response = new HashMap<>();
		if (address != null) {
			response.put("contractAddress", address);
			return ResponseEntity.ok(response);
		} else {
			response.put("message", "No contract loaded");
			return ResponseEntity.ok(response);
		}
	}
}


