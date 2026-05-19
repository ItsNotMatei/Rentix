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
    private final ListingReportRepository listingReportRepository;
    private final ConversationRepository conversationRepository;
    private final DirectMessageRepository messageRepository;
    private final DataCleanupService dataCleanupService;
    private final OfferRepository offerRepository;
    private final ImagineRepository imagineRepository;

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("users", userRepository.count());
        stats.put("listings", productRepository.count());
        stats.put("reviews", reviewRepository.count());
        stats.put("reservations", reservationRepository.count());
        stats.put("openReports", reportRepository.countByStatus("OPEN") + listingReportRepository.countByStatus("OPEN"));
        stats.put("openListingReports", listingReportRepository.countByStatus("OPEN"));
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
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Anunț inexistent.");
        }
        reviewRepository.deleteByAnunt_Id(id);
        reservationRepository.deleteByAnunt_Id(id);
        offerRepository.deleteByListingId(id);
        imagineRepository.deleteByAnunt_Id(id);
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

    public Page<Product> listingsPage(int page, int size, String q) {
        SecurityUtils.requireRole(UserRole.MODERATOR);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        if (q == null || q.isBlank()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.findByTitluContainingIgnoreCaseOrDescriereContainingIgnoreCase(q, q, pageable);
    }

    public Page<Review> reviewsPage(int page, int size) {
        SecurityUtils.requireRole(UserRole.MODERATOR);
        return reviewRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }

    public Map<String, Object> analytics() {
        SecurityUtils.requireRole(UserRole.MODERATOR);
        Map<String, Object> data = new HashMap<>(getStats());
        data.put("verifiedUsers", userRepository.countByIsVerifiedTrue());
        data.put("proUsers", userRepository.countByIsProTrue());
        return data;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> allReservations() {
        SecurityUtils.requireRole(UserRole.MODERATOR);
        return reservationRepository.findAll().stream().map(r -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", r.getId());
            row.put("anuntId", r.getAnunt() != null ? r.getAnunt().getId() : null);
            row.put("listingId", r.getAnunt() != null ? r.getAnunt().getId() : null);
            row.put("userId", r.getUser() != null ? r.getUser().getId() : null);
            row.put("userName", r.getUser() != null ? r.getUser().getNume() : "—");
            row.put("startDate", r.getStartDate() != null ? r.getStartDate().toString() : null);
            row.put("endDate", r.getEndDate() != null ? r.getEndDate().toString() : null);
            row.put("status", r.getStatus() != null ? r.getStatus().name() : "—");
            row.put("returnConfirmed", Boolean.TRUE.equals(r.getReturnConfirmed()));
            row.put("returnCondition", r.getReturnCondition());
            return row;
        }).toList();
    }

    public List<Map<String, Object>> allConversations() {
        SecurityUtils.requireRole(UserRole.MODERATOR);
        return conversationRepository.findAll().stream().map(c -> {
            User u1 = userRepository.findById(c.getParticipantOneId()).orElse(null);
            User u2 = userRepository.findById(c.getParticipantTwoId()).orElse(null);
            var msgs = messageRepository.findByConversation_IdOrderByCreatedAtAsc(c.getId());
            var last = msgs.isEmpty() ? null : msgs.get(msgs.size() - 1);
            Map<String, Object> row = new HashMap<>();
            row.put("id", c.getId());
            row.put("listingId", c.getListingId());
            row.put("participantOneName", u1 != null ? u1.getNume() : "Utilizator");
            row.put("participantTwoName", u2 != null ? u2.getNume() : "Utilizator");
            row.put("participantOneAvatar", u1 != null ? u1.getProfilePic() : null);
            row.put("participantTwoAvatar", u2 != null ? u2.getProfilePic() : null);
            row.put("lastMessage", last != null ? last.getContent() : "");
            row.put("lastMessageAt", last != null ? last.getCreatedAt() : c.getUpdatedAt());
            return row;
        }).toList();
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

    @Transactional
    public void cleanupDemoData() {
        SecurityUtils.requireRole(UserRole.SUPER_ADMIN);
        dataCleanupService.cleanupAllExceptStaff();
    }
}
