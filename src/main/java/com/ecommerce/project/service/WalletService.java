package com.ecommerce.project.service;

import com.ecommerce.project.entity.Wallet;
import com.ecommerce.project.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    public void save(Wallet wallet) {
        walletRepository.save(wallet);
    }

}
