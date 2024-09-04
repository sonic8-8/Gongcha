package com.gongcha.berrymatch.match.Service;

import com.gongcha.berrymatch.match.DTO.MatchReady;
import com.gongcha.berrymatch.match.Repository.MatchUserRepository;
import com.gongcha.berrymatch.match.Repository.MatchRepository;
import com.gongcha.berrymatch.match.domain.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchReadyService {

    private final MatchUserRepository matchUserRepository;
    private final MatchRepository matchRepository;

    public MatchReadyService(MatchUserRepository matchUserRepository, MatchRepository matchRepository) {
        this.matchUserRepository = matchUserRepository;
        this.matchRepository = matchRepository;
    }

    /**
     * 특정 유저의 상태를 Ready로 업데이트합니다.
     *
     * @param matchReady 유저 ID 및 매치 정보가 담긴 DTO
     * @return 업데이트된 MatchUser 객체
     */
    @Transactional
    public MatchUser UserReadyStatus(MatchReady matchReady) {
        // 유저 ID로 매치 유저 조회
        MatchUser matchUser = matchUserRepository.findByUserId(matchReady.getId())
                .orElseThrow(() -> new RuntimeException("MatchUser not found for userId: " + matchReady.getId()));

        // 유저 상태를 Ready로 업데이트
        matchUser.setStatus(MatchUserReady.Ready);
        matchUserRepository.save(matchUser);

        // 매치 ID를 사용하여 매치 조회
        Match match = matchUser.getMatch();

        // 매치에 속한 모든 유저가 Ready 상태인지 확인
        List<MatchUser> readyUsers = matchUserRepository.findByMatchIdAndStatus(match.getId(), MatchUserReady.Ready);

        if (readyUsers.size() == match.getMaxSize()) {
            // 모든 유저가 Ready 상태인 경우, 매치 상태를 IN_PROGRESS로 변경
            match.setMatchStatus(MatchStatus.IN_PROGRESS);
            matchRepository.save(match);
        }

        return matchUser;
    }

    /**
     * 특정 유저의 상태를 Waiting으로 업데이트합니다.
     *
     * @param matchReady 유저 ID 및 매치 정보가 담긴 DTO
     * @return 업데이트된 MatchUser 객체
     */
    @Transactional
    public MatchUser UserWitingStatus(MatchReady matchReady) {
        // 유저 ID로 매치 유저 조회
        MatchUser matchUser = matchUserRepository.findByUserId(matchReady.getId())
                .orElseThrow(() -> new RuntimeException("MatchUser not found for userId: " + matchReady.getId()));

        // 유저 상태를 Waiting으로 업데이트

        matchUser.setStatus(MatchUserReady.Waiting);
        return matchUserRepository.save(matchUser);
    }

    @Transactional
    public void UserMatchLeave(MatchReady matchReady) {
        // 유저 ID로 매치유저 조회
        MatchUser matchUser = matchUserRepository.findByUserId(matchReady.getId())
                .orElseThrow(() -> new RuntimeException("MatchUser not found for userId: " + matchReady.getId()));

        // 매치유저가 속한 매치 조회
        Match match = matchUser.getMatch();

        // 매치가 경기 중인지 확인
        if (match.getMatchStatus() == MatchStatus.IN_PROGRESS) {
            throw new IllegalStateException("경기 중에는 매치를 떠날 수 없습니다.");
        }

        // 매치유저 엔티티를 삭제
        matchUserRepository.delete(matchUser);

        // 매치에서 current_size를 감소
        match.setCurrentSize(match.getCurrentSize() - 1);

        // current_size가 max_size보다 작다면 상태를 NOT_FULL로 변경
        if (match.getCurrentSize() < match.getMaxSize()) {
            match.setStatus(MatchFullStatus.NOT_FULL);
        }

        // 매치에 남아있는 유저가 없는 경우 매치 상태를 EMPTY로 설정
        if (match.getCurrentSize() == 0) {
            match.setStatus(MatchFullStatus.EMPTY);
        }

        // 변경된 매치 정보를 저장
        matchRepository.save(match);
    }



}