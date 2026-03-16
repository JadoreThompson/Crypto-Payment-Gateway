// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

interface IERC20 {
    function transferFrom(address from, address to, uint256 amount) external returns (bool);
    function balanceOf(address account) external view returns (uint256);
}

contract Transaction {
    address public owner;

    event TransactionCreated(
        string invoiceId,
        address indexed sender,
        address indexed recipient,
        address token,
        uint256 amount,
        uint256 timestamp
    );

    event TransactionExecuted(
        string invoiceId,
        address indexed sender,
        address indexed recipient,
        address token,
        uint256 amount,
        uint256 timestamp
    );

    event TransactionFailed(
        string invoiceId,
        address indexed sender,
        address indexed recipient,
        address token,
        uint256 amount,
        string reason,
        uint256 timestamp
    );

    modifier onlyOwner() {
        require(msg.sender == owner, "Only owner can call this");
        _;
    }

    constructor() {
        owner = msg.sender;
    }

    function executeTransaction(
        string memory invoiceId,
        address recipient,
        address token,
        uint256 amount
    ) external onlyOwner {
        address sender = msg.sender;

        require(sender != address(0), "Invalid sender");
        require(token != address(0), "Invalid token");
        require(amount > 0, "Invalid amount");

        uint256 senderBalance = IERC20(token).balanceOf(sender);

        if (senderBalance < amount) {
            emit TransactionFailed(
                invoiceId,
                sender,
                recipient,
                token,
                amount,
                "Insufficient token balance",
                block.timestamp
            );
            return;
        }

        bool success = IERC20(token).transferFrom(
            sender,
            recipient,
            amount
        );

        if (!success) {
            emit TransactionFailed(
                invoiceId,
                sender,
                recipient,
                token,
                amount,
                "ERC20 transfer failed",
                block.timestamp
            );
            return;
        }

        emit TransactionExecuted(
            invoiceId,
            sender,
            recipient,
            token,
            amount,
            block.timestamp
        );
    }

    function transferOwnership(address newOwner) external onlyOwner {
        require(newOwner != address(0), "Invalid new owner");
        owner = newOwner;
    }
}