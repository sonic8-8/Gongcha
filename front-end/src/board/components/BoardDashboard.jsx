import styles from './BoardDashboard.module.css';
import useUserInfo from '../../user/useUserInfo';
import Cookies from 'js-cookie';
import axios from 'axios';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useState } from 'react';

function BoardDashboard() {

    const { userInfo, loading, error } = useUserInfo();

    console.log(loading);

    return (
        <div className={styles.dashboard_container}>
            <div className={styles.dashboard_top}>
                            
                <div className={styles.dashboard_top_logo_container}>
                    <img className={styles.dashboard_top_logo} src='https://thank-you-berrymatch-bucket-0.s3.ap-northeast-2.amazonaws.com/design/logo.png'/>
                    <div className={styles.dashboard_top_logo_title}>BerryMatch</div>
                </div>
                <div className={styles.dashboard_top_identifier}>환영합니다! {userInfo ? userInfo.nickname : '' }님</div>
                <Link to="/logout" className={styles.dashboard_top_logout}>
                        로그아웃
                </Link>

            </div>

            <div className={styles.dashboard_middle}>
                {/* <div className={styles.dashboard_middle_menu_container}>
                    <div className={styles.dashboard_middle_menu}>최신순</div>
                    <div className={styles.dashboard_middle_menu}>인기순</div>
                    <div className={styles.dashboard_middle_menu}>내 게시물 보기</div>
                </div> */}
                <div className={styles.dashboard_middle_content}>
                    <Outlet />
                </div>
            </div>

            <div className={styles.dashboard_bottom}>
                <Link to="/" className={styles.dashboard_bottom_menu}>Home</Link>
                <Link to="/alert" className={styles.dashboard_bottom_menu}>알림</Link>
                <Link to="/mypage" className={styles.dashboard_bottom_menu}>마이페이지</Link>
                <div className={styles.dashboard_bottom_menu}>그룹정보</div>
            </div>
        </div>
    );
}

export default BoardDashboard;

// private Long id;       // 유저 ID
// private String sport;  // 스포츠 종목
// private String time;   // 매칭 시간 (예: 14:00)
// private String groupCord;  // 그룹 코드
// private String date;   // 매칭 날짜 (예: 2024-08-23)