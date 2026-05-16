package com.example.demo.service;

import com.example.demo.dto.UserPublicDto;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final ModerationReportRepository reportRepository;
    private final ConversationRepository conversationRepository;
    private final DirectMessageRepository messageRepository;

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("users", userRepository.count());
        stats.put("listings", productRepository.count());
        stats.put("reviews", reviewRepository.count());
        stats.put("reservations", reservationRepository.count());
        stats.put("openReports", reportRepository.countByStatus("OPEN"));
        stats.put("conversations", conversationRepository.count());
        stats.put("messages", messageRepository.count());
        return stats;
    }

    public Page<User> searchUsers(String q, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        if (q == null || q.isBlank()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.findByEmailContainingIgnoreCaseOrNumeContainingIgnoreCase(q, q, pageable);
    }

    @Transactional
    public User banUser(Long userId, String reason) {
        SecurityUtils.requireRole(UserRole.ADMIN);
        User user = userRepository.findById(userId).orElseThrow();
        if (UserRole.fromString(user.getRole()) == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Nu poți bloca un super admin.");
        }
        user.setBanned(true);
        user.setBanReason(reason);
        return userRepository.save(user);
    }

    @Transactional
    public User suspendUser(Long userId, int days, String reason) {
        SecurityUtils.requireRole(UserRole.ADMIN);
        User user = userRepository.findById(userId).orElseThrow();
        if (UserRole.fromString(user.getRole()) == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Nu poți suspenda un super admin.");
        }
        user.setSuspended(true);
        user.setSuspendedUntil(LocalDateTime.now().plusDays(days));
        user.setBanReason(reason);
        return userRepository.save(user);
    }

    @Transactional
    public User unbanUser(Long userId) {
        SecurityUtils.requireRole(UserRole.ADMIN);
        User user = userRepository.findById(userId).orElseThrow();
        user.setBanned(false);
        user.setSuspended(false);
        user.setSuspendedUntil(null);
        user.setBanReason(null);
        return userRepository.save(user);
    }

    @Transactional
    public User updateRole(Long userId, String role) {
        SecurityUtils.requireRole(UserRole.SUPER_ADMIN);
        User user = userRepository.findById(userId).orElseThrow();
        UserRole newRole = UserRole.fromString(role);
        user.setRole(newRole.name());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteListing(Long id) {
        SecurityUtils.requireRole(UserRole.MODERATOR);
        productRepository.deleteById(id);
    }

    @Transactional
    public void deleteReview(Long id) {
        SecurityUtils.requireRole(UserRole.MODERATOR);
        reviewRepository.deleteById(id);
    }

    public List<ModerationReport> getReports(String status) {
        SecurityUtils.requireRole(UserRole.MODERATOR);
        if (status == null || status.isBlank()) {
            return reportRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        return reportRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional
    public ModerationReport resolveReport(Long id) {
        SecurityUtils.requireRole(UserRole.MODERATOR);
        ModerationReport report = reportRepository.findById(id).orElseThrow();
        report.setStatus("RESOLVED");
        return reportRepository.save(report);
    }

    public List<Product> allListings() {
        SecurityUtils.requireRole(UserRole.MODERATOR);
        return productRepository.findAll();
    }

    public List<Review> allReviews() {
        SecurityUtils.requireRole(UserRole.MODERATOR);
        return reviewRepository.findAll();
    }

    public List<Reservation> allReservations() {
        SecurityUtils.requireRole(UserRole.MODERATOR);
        return reservationRepository.findAll();
    }

    public List<Conversation> allConversations() {
        SecurityUtils.requireRole(UserRole.MODERATOR);
        return conversationRepository.findAll();
    }

    @Transactional
    public ModerationReport createReport(String type, Long targetId, String reason, Long reporterId) {
        ModerationReport report = new ModerationReport();
        report.setType(type);
        report.setTargetId(targetId);
        report.setReason(reason);
        report.setReporterId(reporterId);
        report.setStatus("OPEN");
        return reportRepository.save(report);
    }
}
