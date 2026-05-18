package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepository;

    @Transactional
    public void creditSeller(Long sellerId, long amountCents, String reason) {
        if (sellerId == null || amountCents <= 0) {
            return;
        }
        User seller = userRepository.findById(sellerId).orElseThrow();
        double addRon = amountCents / 100.0;
        seller.setBalance(seller.getBalance() + addRon);
        userRepository.save(seller);
    }

    public double getBalanceRon(Long userId) {
        return userRepository.findById(userId)
                .map(User::getBalance)
                .orElse(0.0);
    }
}
