import React, { useState } from 'react';
import axios from 'axios';
import { BrowserRouter as Router, Route, Routes, useLocation } from 'react-router-dom';

import styles from './App.module.css';
import VideoUpload from './VideoUpload';
import LoginPage from './auth/pages/LoginPage';
import MainPage from './common/pages/MainPage';
import SignupPage from './auth/pages/SignupPage';
import MyPage from './user/pages/MyPage';
import RankPage from './rank/RankPage';
import GroupCreatePage from './group/GroupCreatePage';
import GroupSearchPage from './group/GroupSearchPage';
import BoardPage from './board/BoardPage';
import LogoutPage from './auth/pages/LogoutPage'
import PrivateRoute from './auth/components/PrivateRoute';
import TokenPage from './auth/pages/TokenPage';
import BackgroundMusic from './common/components/BackgroundMusic';
import MatchSetupSubPage from './match/pages/MatchSetupSubPage';
import MatchPage from './match/pages/MatchPage';
import MatchQueueSubPage from './match/pages/MatchQueueSubPage';
import MatchLobbyPage from './match/pages/MatchLobbyPage';
import ProfileEditSubPage from './user/pages/ProfileEditSubPage';
import MatchResultsSubPage from './user/pages/MatchResultsSubPage';
import AccountDeletionSubPage from './user/pages/AccountDeletionSubPage';
import ChatRoom from './chat/ChatRoom';

function App() {

  const location = useLocation();
  const showBackground = !['/login', '/signup', '/token', '/logout'].includes(location.pathname);

  return (
    <div className={styles.layout}>
      {/* Conditional Background Video */}
      {showBackground && (
        <video autoPlay muted loop className={styles.backgroundVideo}>
          <source src="https://thank-you-berrymatch-bucket-0.s3.ap-northeast-2.amazonaws.com/design/main_background_video.mp4" type="video/mp4" />
        </video>
      )}
      
      {/* Conditional Background Music */}
      {showBackground && <BackgroundMusic />}

      <Routes>
        <Route path="/" element={<PrivateRoute><MainPage /></PrivateRoute>} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/logout" element={<LogoutPage />} />
        <Route path="/token" element={<TokenPage/>} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/rank" element={<PrivateRoute><RankPage /></PrivateRoute>} />
        <Route path="/mypage" element={<PrivateRoute><MyPage /></PrivateRoute>} >
          <Route path="profile-edit" element={<PrivateRoute><ProfileEditSubPage /></PrivateRoute>} />
          <Route path="match-results" element={<PrivateRoute><MatchResultsSubPage /></PrivateRoute>} />
          <Route path="account-deletion" element={<PrivateRoute><AccountDeletionSubPage /></PrivateRoute>} />
        </Route>
        <Route path="/match" element={<PrivateRoute><MatchPage /></PrivateRoute>} >
          <Route path="setup" element={<PrivateRoute><MatchSetupSubPage /></PrivateRoute>}/>
          <Route path="queue" element={<PrivateRoute><MatchQueueSubPage /></PrivateRoute>}/>
        </Route>
        <Route path="/match/lobby" element={<PrivateRoute><MatchLobbyPage /></PrivateRoute>} />
        <Route path="/board" element={<PrivateRoute><BoardPage /></PrivateRoute>} />
        <Route path="/group/create" element={<PrivateRoute><GroupCreatePage /></PrivateRoute>} />
        <Route path="/group/search" element={<PrivateRoute><GroupSearchPage /></PrivateRoute>} />
        <Route path="/alert" element={<PrivateRoute><MainPage /></PrivateRoute>} />
        <Route path="/chat" element={<PrivateRoute><ChatRoom/></PrivateRoute>}></Route>
      </Routes>
    </div>
  );
}

export default function AppWrapper() {
  return (
    <Router>
      <App />
    </Router>
  );
}