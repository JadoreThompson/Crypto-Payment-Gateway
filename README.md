# NeoPay

A cryptocurrency payment gateway for SaaS businesses and sole entrepreneurs, enabling seamless subscription management and invoicing with blockchain-based payments.

## Overview

NeoPay is a Spring Boot application that provides a complete payment infrastructure for accepting cryptocurrency payments. It bridges the gap between traditional e-commerce and Web3, offering:

- **Subscription Management**: Create and manage recurring billing cycles with flexible pricing models
- **Invoice Processing**: Self-contained invoice models for payment tracking and reconciliation
- **Blockchain Integration**: Real-time transaction detection and processing via Ethereum smart contracts
- **Centralized Ledger**: Internal balance management with full accounting and reconciliation capabilities
- **Webhook Notifications**: Event-driven architecture for real-time merchant notifications

### Target Users

- **SaaS Businesses**: Accept crypto payments for subscription-based services
- **Sole Entrepreneurs**: Simple invoicing and payment collection without complex infrastructure

### Core Features

| Feature | Description |
|---------|-------------|
| Customers | Manage customer profiles with metadata support |
| Products | Define goods/services with one-time or recurring pricing |
| Pricing | Flexible pricing models separate from product definitions |
| Invoices | Payment requests with status tracking and expiration |
| Transactions | Blockchain payment tracking with lifecycle management |
| Wallets | Merchant balance management per token |
| Withdrawals | Move funds from ledger balance to external wallets |
| Webhooks | Real-time event notifications to merchant endpoints |

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT REQUESTS                                  │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              API LAYER                                        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │   Auth   │ │Customer  │ │ Product  │ │  Price   │ │ Invoice  │           │
│  │Controller│ │Controller│ │Controller│ │Controller│ │Controller│           │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │Transaction│ │  Wallet  │ │Withdrawal│ │Merchant  │ │Subscription│         │
│  │Controller│ │Controller│ │Controller│ │Controller│ │Controller│           │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            SERVICE LAYER                                      │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐        │
│  │  AuthService │ │InvoiceService│ │ProductService│ │ PriceService │        │
│  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘        │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐        │
│  │WalletService │ │WithdrawalSvc │ │MerchantService│ │CustomerService│       │
│  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘        │
│  ┌──────────────────────┐ ┌──────────────────────┐                          │
│  │BalanceTransactionSvc │ │   WebhookService     │                          │
│  │  (Atomic Operations) │ │  (Async Queue)       │                          │
│  └──────────────────────┘ └──────────────────────┘                          │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                    ┌─────────────────┼─────────────────┐
                    ▼                 ▼                 ▼
┌─────────────────────────┐ ┌─────────────────┐ ┌─────────────────────────────┐
│   BLOCKCHAIN LAYER      │ │   DATA LAYER    │ │      EVENT SYSTEM           │
│                         │ │                 │ │                             │
│ ┌─────────────────────┐ │ │ ┌─────────────┐ │ │ ┌─────────────────────────┐ │
│ │EthereumWatcherService│ │ │ │ PostgreSQL  │ │ │ │  Transaction Events      │ │
│ │                     │ │ │ │             │ │ │ │  - ExecutedEvent         │ │
│ │ • Web3j Integration │ │ │ │ • Merchants │ │ │ │  - FailedEvent           │ │
│ │ • Block Monitoring  │ │ │ │ • Customers │ │ │ │                          │ │
│ │ • Event Detection   │ │ │ │ • Products  │ │ │ │  Balance Events          │ │
│ │ • Smart Contract    │ │ │ │ • Prices    │ │ │ │  - IncreaseBalance       │ │
│ │   Interaction       │ │ │ │ • Invoices  │ │ │ │  - DecreaseBalance       │ │
│ └─────────────────────┘ │ │ │ • Transactns│ │ │ └─────────────────────────┘ │
│                         │ │ │ • Wallets   │ │ │                             │
│ ┌─────────────────────┐ │ │ │ • Balances  │ │ │ ┌─────────────────────────┐ │
│ │  Smart Contract     │ │ │ │ • Withdrawls│ │ │ │    Webhook Queue        │ │
│ │  (Transaction.sol)  │ │ │ └─────────────┘ │ │ │                         │ │
│ └─────────────────────┘ │ │                 │ │ │ • BlockingQueue         │ │
│                         │ │ ┌─────────────┐ │ │ │ • Background Consumer   │ │
└─────────────────────────┘ │ │    Redis    │ │ │ │ • POST to Merchant URL  │ │
                            │ │ (Future)    │ │ │ └─────────────────────────┘ │
                            │ └─────────────┘ │ └─────────────────────────────┘
                            └─────────────────┘
```

### Payment Flow

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Invoice    │     │   Transaction │     │  Blockchain  │     │   Webhook    │
│   Created    │────▶│       │────▶│   Watcher    │────▶│   Sent       │
│              │     │   Generated   │     │   Detects    │     │   to Merchant│
└──────────────┘     └────────────── ┘     └──────────────┘     └──────────────┘
                                                 │
                                                 ▼
                                         ┌──────────────┐
                                         │   Ledger     │
                                         │   Updated    │
                                         │   (Balance + │
                                         │    Escrow)   │
                                         └──────────────┘
```

### Withdrawal Flow

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Withdraw   │     │   Balance    │     │   Funds      │     │   Webhook    │
│   Request    │────▶│   Verified   │────▶│   → Escrow   │────▶│   Sent       │
│              │     │              │     │              │     │              │
└──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘
                            │
                            ▼
                     ┌──────────────┐
                     │  Blockchain  │
                     │  Transaction │
                     │  Broadcast   │
                     └──────────────┘
```

### Key Components

#### Blockchain Watcher
Monitors Ethereum blockchain for transactions sent to gateway-managed deposit addresses:
- Smart contract event detection (TransactionExecuted, TransactionFailed)
- Automatic merchant balance crediting
- Webhook notification dispatch

#### Centralized Ledger
Internal balance management with full accounting:
- **Wallets**: Per-merchant, per-token balance tracking
- **Balances**: Global token balance aggregation
- **Escrow**: Funds reserved during withdrawal processing

#### Webhook Service
Asynchronous event delivery to merchant endpoints:
- Queue-based processing for reliability
- Background thread consumption
- JSON POST requests to configured URLs
- Graceful shutdown handling

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21+ |
| Framework | Spring Boot 4.0.3 |
| Database | PostgreSQL |
| Cache | Redis (planned) |
| Blockchain | Ethereum (Web3j) |
| Smart Contracts | Solidity |
| Authentication | JWT |
| Build Tool | Maven |

## Getting Started

### Prerequisites

- Java 21 or higher

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/JadoreThompson/neopay.git
   cd neopay
   ```

2. Configure the application:
   ```bash
   cp src/main/resources/example.application-dev.properties src/main/resources/application-dev.properties
   ```
   
   Edit `application-dev.properties` with your configuration:
   - Database connection settings
   - Ethereum node URL
   - Smart contract address
   - JWT secret

3. Run with Docker Compose (includes PostgreSQL):
   ```bash
   docker-compose -f dev-compose.yml up -d
   ```

4. Start the application:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

### Running Tests

```bash
mvn test -Dspring.profiles.active=test
```

## API Endpoints

| Endpoint | Description |
|----------|-------------|
| `POST /api/auth/register` | Register new user |
| `POST /api/auth/login` | Authenticate user |
| `GET /api/auth/me` | Get current user |
| `GET /api/customers` | List customers |
| `POST /api/customers` | Create customer |
| `GET /api/products` | List products |
| `POST /api/products` | Create product |
| `GET /api/prices` | List prices |
| `POST /api/prices` | Create price |
| `GET /api/invoices` | List invoices |
| `POST /api/invoices` | Create invoice |
| `GET /api/transactions` | List transactions |
| `GET /api/wallets` | List merchant wallets |
| `POST /api/withdrawals` | Create withdrawal request |
| `GET /api/merchants` | List merchants |
| `POST /api/merchants` | Create merchant |

## Smart Contract

The payment flow is managed by a Solidity smart contract (`Transaction.sol`) deployed on Ethereum testnet (Sepolia).

### Contract Address (Sepolia)
```
0x6ddBaF04B994ca7e7E305099dB2E94D6a20E6666
```

### Events
- `TransactionExecuted`: Emitted when a payment is successfully processed
- `TransactionFailed`: Emitted when a payment fails

## Webhook Events

| Event | Description |
|-------|-------------|
| `transaction.executed` | Payment successfully processed |
| `transaction.failed` | Payment processing failed |
| `invoice.paid` | Invoice fully paid |
| `withdrawal.processing` | Withdrawal in progress |
| `withdrawal.completed` | Withdrawal confirmed |
| `withdrawal.failed` | Withdrawal failed |

## Roadmap

- [x] CRUD Operations
- [x] Blockchain Watcher & Ledger
- [ ] Analytics Endpoints
- [ ] Dashboard
- [ ] Off-Ramp to Cash (fiat conversion)
- [ ] MCP Integration
- [ ] Smart Contract Proxy

## Resources

- [Web3j Documentation](https://docs.web3j.io/4.14.0/)
- [Solidity Documentation](https://docs.soliditylang.org/)
- [Remix IDE](https://remix.ethereum.org/)

## License

This project is proprietary software. All rights reserved.