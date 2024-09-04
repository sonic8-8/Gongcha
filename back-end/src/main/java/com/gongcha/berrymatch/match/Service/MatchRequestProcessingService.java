package com.gongcha.berrymatch.match.Service;

import com.gongcha.berrymatch.group.GroupRepository;
import com.gongcha.berrymatch.group.UserGroup;
import com.gongcha.berrymatch.match.DTO.MatchRequest;
import com.gongcha.berrymatch.match.Repository.MatchRepository;
import com.gongcha.berrymatch.match.Repository.MatchUserRepository;
import com.gongcha.berrymatch.match.Repository.MatchingQueueRepository;
import com.gongcha.berrymatch.match.domain.MatchQueueStatus;
import com.gongcha.berrymatch.match.domain.MatchType;
import com.gongcha.berrymatch.match.domain.MatchingQueue;
import com.gongcha.berrymatch.match.domain.Sport;
import com.gongcha.berrymatch.user.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

@Service
public class MatchRequestProcessingService {

    private final MatchingQueueRepository matchingQueueRepository;  // 매칭 대기열을 관리하는 리포지토리
    private final UserRepository userRepository;  // 유저 정보를 관리하는 리포지토리
    private final GroupRepository groupRepository;  // 그룹 정보를 관리하는 리포지토리
    private final MatchRepository matchRepository;  // 매칭 정보를 관리하는 리포지토리
    private final MatchUserRepository matchUserRepository;  // 매칭된 유저를 관리하는 리포지토리
    private final Lock matchLock;  // 매칭 요청 처리 시 동시성 문제를 방지하기 위한 잠금
    private static final int QUEUE_LIMIT = 1000; // 매칭 대기열의 최대 크기 설정

    @Autowired
    public MatchRequestProcessingService(
            MatchingQueueRepository matchingQueueRepository,
            UserRepository userRepository,
            GroupRepository groupRepository,
            MatchRepository matchRepository,
            MatchUserRepository matchUserRepository,
            Lock matchLock) {
        this.matchingQueueRepository = matchingQueueRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.matchRepository = matchRepository;
        this.matchUserRepository = matchUserRepository;
        this.matchLock = matchLock;
    }

    // 매칭 요청을 처리하는 메소드
    public void processMatchRequest(MatchRequest matchRequest) {
        matchLock.lock();  // 매칭 요청을 처리할 때 동시성 문제를 방지하기 위한 잠금 설정
        try {
            if (getQueueSize() < QUEUE_LIMIT) {  // 대기열 크기가 제한 이하일 때만 처리
                if (matchRequest.getGroupCode() != null && !matchRequest.getGroupCode().isEmpty()) {
                    handleGroupMatching(matchRequest);  // 그룹 매칭 처리
                } else {
                    handleIndividualMatching(matchRequest);  // 개인 매칭 처리
                }
            } else {
                System.out.println("Queue limit reached. Pausing request processing.");  // 대기열이 가득 찬 경우 처리 중단
                // 대기열이 꽉 찼을 때 추가적인 처리 로직을 구현할 수 있음
            }
        } finally {
            matchLock.unlock();  // 매칭 요청 처리가 끝난 후 잠금 해제
        }
    }

    // 현재 대기열의 크기를 반환하는 메소드
    private int getQueueSize() {
        return (int) matchingQueueRepository.count();  // 대기열의 크기를 조회하여 반환
    }

    // 그룹 매칭을 처리하는 메소드
    private void handleGroupMatching(MatchRequest matchRequest) {
        Optional<UserGroup> optionalGroup = groupRepository.findByGroupCode(matchRequest.getGroupCode());

        if (optionalGroup.isPresent()) {
            UserGroup userGroup = optionalGroup.get();
            List<User> groupMembers = userGroup.getMembers();

            List<Long> excludedUserIds = getExcludedUserIds();  // 이미 매칭된 유저나 대기열에 있는 유저 ID 목록 조회

            for (User user : groupMembers) {
                if (!excludedUserIds.contains(user.getId())) {
                    saveToMatchingQueue(user, matchRequest, MatchType.GROUP);  // 대기열에 그룹 유저 저장
                }
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 그룹 코드: " + matchRequest.getGroupCode());  // 유효하지 않은 그룹 코드에 대한 예외 처리
        }
    }

    // 개인 매칭을 처리하는 메소드
    private void handleIndividualMatching(MatchRequest matchRequest) {
        Long userId = matchRequest.getId();
        if (userId == null) {
            throw new IllegalArgumentException("유저 ID는 null일 수 없습니다.");  // 유저 ID가 null일 경우 예외 처리
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 유저 ID: " + userId));  // 유효하지 않은 유저 ID에 대한 예외 처리

        List<Long> excludedUserIds = getExcludedUserIds();  // 이미 매칭된 유저나 대기열에 있는 유저 ID 목록 조회

        if (!excludedUserIds.contains(userId)) {
            saveToMatchingQueue(user, matchRequest, MatchType.USER);  // 대기열에 개인 유저 저장
        }
    }

    // 이미 매칭된 유저와 대기열에 있는 유저의 ID를 반환하는 메소드
    private List<Long> getExcludedUserIds() {
        List<Long> queueUserIds = matchingQueueRepository.findAll().stream()
                .map(matchingQueue -> matchingQueue.getUser().getId())
                .collect(Collectors.toList());

        List<Long> matchedUserIds = matchUserRepository.findAll().stream()
                .map(matchUser -> matchUser.getUser().getId())
                .collect(Collectors.toList());

        queueUserIds.addAll(matchedUserIds);  // 대기열에 있는 유저와 이미 매칭된 유저 ID를 합침
        return queueUserIds;
    }

    // 매칭 대기열에 유저를 저장하는 메소드
    private void saveToMatchingQueue(User user, MatchRequest matchRequest, MatchType matchType) {
        if (user.getId() == null) {
            throw new IllegalStateException("User ID는 null이 될 수 없습니다.");  // 유저 ID가 null일 경우 예외 처리
        }

        // 요청된 스포츠를 유효한 Enum 값으로 변환
        Sport sport;
        try {
            sport = Sport.valueOf(matchRequest.getSport().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 스포츠: " + matchRequest.getSport(), e);  // 유효하지 않은 스포츠 이름에 대한 예외 처리
        }

        City city = user.getCity();
        District district = user.getDistrict();

        // 매칭 대기열 엔티티 생성
        MatchingQueue matchingQueue = MatchingQueue.builder()
                .user(user)
                .sport(sport)
                .city(city)
                .district(district)
                .groupCode(matchType == MatchType.GROUP ? matchRequest.getGroupCode() : null)
                .matchTime(LocalDateTime.parse(matchRequest.getDate() + "T" + matchRequest.getTime()))
                .status(MatchQueueStatus.PENDING)
                .enqueuedAt(LocalDateTime.now())
                .matchType(matchType)
                .build();

        // 대기열에 저장하고 유저 상태를 업데이트
        try {
            matchingQueueRepository.save(matchingQueue);

            user.updateMatchStatus(UserMatchStatus.MATCHED);
            userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("매칭 대기열 저장 중 오류 발생: " + e.getMessage(), e);  // 대기열 저장 중 예외 처리
        }
    }
}