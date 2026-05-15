<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>얌얌로그</title>
  <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700;900&display=swap" rel="stylesheet">
  <style>
    :root {
      --primary: #FF6B6B;
      --primary-dark: #E85555;
      --primary-light: #FFF0F0;
      --bg: #FFFBF8;
      --surface: #FFFFFF;
      --text: #1E1E2D;
      --text-soft: #6B7280;
      --muted: #9CA3AF;
      --border: #F0EBE5;
    }
    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: 'Noto Sans KR', sans-serif; background: #F5F0EB; color: var(--text); -webkit-font-smoothing: antialiased; }
    .app { max-width: 1080px; margin: 0 auto; min-height: 100vh; background: var(--bg); padding-bottom: 84px; }

    /* ── 헤더 ── */
    .header {
      background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
      padding: 40px 32px 28px;
      position: relative; overflow: hidden;
    }
    .header::before {
      content: ''; position: absolute; top: -50px; right: -40px;
      width: 220px; height: 220px; border-radius: 50%;
      background: rgba(255,255,255,0.1);
    }
    .header::after {
      content: ''; position: absolute; bottom: -60px; left: -30px;
      width: 160px; height: 160px; border-radius: 50%;
      background: rgba(255,255,255,0.07);
    }
    .header-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
    .app-title { font-size: 1.5rem; font-weight: 900; color: #fff; letter-spacing: -0.5px; }
    .profile-btn {
      width: 40px; height: 40px; border-radius: 50%;
      background: rgba(255,255,255,0.25);
      display: flex; align-items: center; justify-content: center;
      text-decoration: none; font-size: 1.2rem;
    }
    .greeting { font-size: 0.92rem; color: rgba(255,255,255,0.88); margin-bottom: 18px; }

    /* 날짜 네비 */
    .date-nav { display: flex; align-items: center; gap: 14px; max-width: 320px; }
    .date-arrow {
      width: 34px; height: 34px; border-radius: 50%;
      background: rgba(255,255,255,0.22); border: none; color: #fff;
      font-size: 1.2rem; display: flex; align-items: center; justify-content: center;
      cursor: pointer; transition: background 0.15s;
    }
    .date-arrow:hover { background: rgba(255,255,255,0.38); }
    .date-display { flex: 1; }
    .date-main { font-size: 1.2rem; font-weight: 700; color: #fff; }
    .date-badge {
      display: inline-block; margin-left: 8px;
      padding: 2px 10px; border-radius: 20px;
      background: rgba(255,255,255,0.28); font-size: 0.72rem; font-weight: 700; color: #fff;
      vertical-align: middle;
    }

    /* ── 요약 카드 ── */
    .summary-wrap { padding: 0 24px; margin-top: -6px; }
    .summary-card {
      background: var(--surface); border-radius: 22px; padding: 24px 28px;
      box-shadow: 0 10px 36px rgba(255,107,107,0.13);
      display: grid; grid-template-columns: auto 1fr auto; gap: 28px; align-items: center;
    }
    .kcal-block {}
    .kcal-label { font-size: 0.78rem; color: var(--muted); margin-bottom: 4px; }
    .kcal-num { font-size: 3rem; font-weight: 900; color: var(--primary); line-height: 1; letter-spacing: -2px; }
    .kcal-unit { font-size: 0.95rem; font-weight: 500; color: var(--text-soft); margin-left: 4px; }
    .kcal-goal-line { font-size: 0.8rem; color: var(--muted); margin-top: 4px; }
    .kcal-goal-line strong { color: var(--text-soft); }

    .progress-block { min-width: 0; }
    .progress-track { height: 10px; background: #F5EFE9; border-radius: 5px; overflow: hidden; margin-bottom: 16px; }
    .progress-fill {
      height: 100%; border-radius: 5px;
      background: linear-gradient(90deg, #FF8E53, #FF6B6B);
      transition: width 0.6s cubic-bezier(.4,0,.2,1);
    }
    .macro-row { display: grid; grid-template-columns: repeat(3,1fr); gap: 10px; }
    .macro-pill {
      border-radius: 12px; padding: 10px 8px; text-align: center;
    }
    .macro-pill.carb  { background: #EEF4FF; }
    .macro-pill.prot  { background: #EDFFF7; }
    .macro-pill.fat   { background: #FFF8EC; }
    .macro-pill .mp-val { font-size: 1.05rem; font-weight: 700; display: block; margin-bottom: 2px; }
    .macro-pill.carb .mp-val { color: #5B8CFF; }
    .macro-pill.prot .mp-val { color: #2ECBBD; }
    .macro-pill.fat  .mp-val { color: #FFA940; }
    .macro-pill .mp-label { font-size: 0.72rem; color: var(--muted); }

    .add-btn-block { display: flex; flex-direction: column; align-items: center; gap: 6px; }
    .quick-add-btn {
      width: 54px; height: 54px; border-radius: 50%;
      background: var(--primary); color: #fff; border: none;
      font-size: 1.7rem; display: flex; align-items: center; justify-content: center;
      text-decoration: none; cursor: pointer;
      box-shadow: 0 4px 18px rgba(255,107,107,0.38);
      transition: transform 0.15s, box-shadow 0.15s;
    }
    .quick-add-btn:hover { transform: scale(1.08); box-shadow: 0 6px 24px rgba(255,107,107,0.5); }
    .quick-add-label { font-size: 0.7rem; color: var(--muted); }

    /* ── 식사 2열 그리드 ── */
    .meals-wrap {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
      padding: 20px 24px 0;
    }
    .meal-card { background: var(--surface); border-radius: 18px; overflow: hidden; box-shadow: 0 2px 14px rgba(0,0,0,0.05); }
    .meal-head { display: flex; align-items: center; padding: 16px 18px 12px; gap: 10px; }
    .meal-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
    .meal-name { font-weight: 700; font-size: 0.95rem; flex: 1; }
    .meal-kcal-badge { padding: 4px 11px; border-radius: 20px; font-size: 0.75rem; font-weight: 600; }

    .food-item {
      display: flex; align-items: center; gap: 14px;
      padding: 11px 18px; border-top: 1px solid var(--border);
      text-decoration: none; color: inherit; transition: background 0.12s;
    }
    .food-item:hover { background: var(--bg); }
    .food-icon-sm {
      width: 38px; height: 38px; border-radius: 11px;
      display: flex; align-items: center; justify-content: center;
      font-size: 1.05rem; flex-shrink: 0;
    }
    .fi-info { flex: 1; min-width: 0; }
    .fi-name { font-size: 0.88rem; font-weight: 500; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .fi-meta { font-size: 0.72rem; color: var(--muted); margin-top: 2px; }
    .fi-kcal { font-size: 0.92rem; font-weight: 700; color: var(--text-soft); flex-shrink: 0; }

    .add-row {
      display: flex; align-items: center; justify-content: center; gap: 6px;
      padding: 13px; border-top: 1px dashed var(--border);
      text-decoration: none; color: var(--muted); font-size: 0.82rem; font-weight: 500;
      transition: color 0.15s;
    }
    .add-row:hover { color: var(--primary); }

    /* ── 하단 네비 ── */
    .bottom-nav {
      position: fixed; bottom: 0; left: 50%; transform: translateX(-50%);
      width: 100%; max-width: 1080px;
      background: var(--surface); border-top: 1px solid var(--border);
      display: flex; align-items: center; height: 68px; z-index: 100;
    }
    .nav-item {
      flex: 1; display: flex; flex-direction: column; align-items: center; gap: 3px;
      padding: 8px 0; text-decoration: none; color: var(--muted);
      font-size: 0.65rem; font-weight: 500; transition: color 0.15s;
    }
    .nav-item.active { color: var(--primary); }
    .nav-item .ni-icon { font-size: 1.35rem; }
    .nav-fab { flex: 1; display: flex; justify-content: center; align-items: center; }
    .nav-fab a {
      width: 50px; height: 50px; border-radius: 50%; background: var(--primary);
      display: flex; align-items: center; justify-content: center;
      text-decoration: none; font-size: 1.5rem; color: #fff;
      box-shadow: 0 4px 18px rgba(255,107,107,0.4); margin-top: -22px;
      transition: transform 0.15s, box-shadow 0.15s;
    }
    .nav-fab a:hover { transform: scale(1.08); }
  </style>
</head>
<body>
<div class="app">

  <!-- 헤더 -->
  <div class="header">
    <div class="header-top">
      <div class="app-title">🍽️ 얌얌로그</div>
      <a href="/member/mypage" class="profile-btn">👤</a>
    </div>
    <div class="greeting">
      <c:choose>
        <c:when test="${logDate == today}">오늘 하루도 건강하게 먹어요 🌿</c:when>
        <c:otherwise>${logDate} 식단 기록이에요</c:otherwise>
      </c:choose>
    </div>
    <div class="date-nav">
      <button class="date-arrow" onclick="moveDate(-1)">&#8249;</button>
      <div class="date-display">
        <span class="date-main">${logDate}</span>
        <c:if test="${logDate == today}"><span class="date-badge">오늘</span></c:if>
      </div>
      <button class="date-arrow" onclick="moveDate(1)">&#8250;</button>
    </div>
  </div>

  <!-- 요약 카드 -->
  <div class="summary-wrap">
    <div class="summary-card">
      <div class="kcal-block">
        <div class="kcal-label">오늘 섭취 칼로리</div>
        <div>
          <span class="kcal-num"><fmt:formatNumber value="${totalEnergy}" pattern="#,###"/></span>
          <span class="kcal-unit">kcal</span>
        </div>
        <div class="kcal-goal-line">목표 <strong><fmt:formatNumber value="${goalEnergy}" pattern="#,###"/> kcal</strong></div>
      </div>
      <div class="progress-block">
        <div class="progress-track">
          <div class="progress-fill"
               style="width:${totalEnergy / goalEnergy * 100 > 100 ? 100 : totalEnergy / goalEnergy * 100}%"></div>
        </div>
        <div class="macro-row">
          <div class="macro-pill carb">
            <span class="mp-val"><fmt:formatNumber value="${totalCarbs}"   pattern="#,##0.#"/>g</span>
            <span class="mp-label">탄수화물</span>
          </div>
          <div class="macro-pill prot">
            <span class="mp-val"><fmt:formatNumber value="${totalProtein}" pattern="#,##0.#"/>g</span>
            <span class="mp-label">단백질</span>
          </div>
          <div class="macro-pill fat">
            <span class="mp-val"><fmt:formatNumber value="${totalFat}"     pattern="#,##0.#"/>g</span>
            <span class="mp-label">지방</span>
          </div>
        </div>
      </div>
      <div class="add-btn-block">
        <a href="/food/search?date=${logDate}" class="quick-add-btn">＋</a>
        <span class="quick-add-label">음식 추가</span>
      </div>
    </div>
  </div>

  <!-- 식사별 기록 (2열 그리드) -->
  <div class="meals-wrap">
    <c:set var="meals" value="${[
      ['BREAKFAST','아침','🌅','#FFA940','rgba(255,169,64,0.12)'],
      ['LUNCH',    '점심','☀️','#2ECBBD','rgba(46,203,189,0.12)'],
      ['DINNER',   '저녁','🌙','#9B7FFA','rgba(155,127,250,0.12)'],
      ['SNACK',    '간식','🍪','#FF7EB3','rgba(255,126,179,0.12)']
    ]}"/>

    <c:forEach var="m" items="${meals}">
      <c:set var="mKey"   value="${m[0]}"/>
      <c:set var="mLabel" value="${m[1]}"/>
      <c:set var="mEmoji" value="${m[2]}"/>
      <c:set var="mColor" value="${m[3]}"/>
      <c:set var="mBg"    value="${m[4]}"/>
      <c:set var="mLogs"  value="${mealMap[mKey]}"/>

      <%-- 해당 식사 총 칼로리 계산 --%>
      <c:set var="mKcal" value="0"/>
      <c:forEach var="lg" items="${mLogs}">
        <c:set var="mKcal" value="${mKcal + lg.actualEnergy}"/>
      </c:forEach>

      <div class="meal-card">
        <div class="meal-head">
          <div class="meal-dot" style="background:${mColor};"></div>
          <span class="meal-name">${mEmoji} ${mLabel}</span>
          <c:if test="${mKcal > 0}">
            <span class="meal-kcal-badge" style="background:${mBg};color:${mColor};">
              <fmt:formatNumber value="${mKcal}" pattern="#,###"/> kcal
            </span>
          </c:if>
        </div>

        <%-- 해당 식사의 모든 음식 항목 렌더링 --%>
        <c:forEach var="lg" items="${mLogs}">
          <a href="/yamyam/detail?logId=${lg.logId}" class="food-item">
            <div class="food-icon-sm" style="background:${mBg};">${mEmoji}</div>
            <div class="fi-info">
              <div class="fi-name"><c:out value="${lg.foodName}"/></div>
              <div class="fi-meta">
                <fmt:formatNumber value="${lg.servingSize}" pattern="#,##0.#"/>g &nbsp;·&nbsp;
                탄 <fmt:formatNumber value="${lg.actualCarbs}"   pattern="#0.#"/>g &nbsp;
                단 <fmt:formatNumber value="${lg.actualProtein}" pattern="#0.#"/>g &nbsp;
                지 <fmt:formatNumber value="${lg.actualFat}"     pattern="#0.#"/>g
              </div>
            </div>
            <span class="fi-kcal"><fmt:formatNumber value="${lg.actualEnergy}" pattern="#,###"/> kcal</span>
          </a>
        </c:forEach>

        <a href="/food/search?mealType=${mKey}&date=${logDate}" class="add-row">
          ＋ ${mLabel} 음식 추가
        </a>
      </div>
    </c:forEach>
  </div>

</div>

<nav class="bottom-nav">
  <a href="/yamyam/list" class="nav-item active">
    <span class="ni-icon">🏠</span>홈
  </a>
  <a href="/yamyam/list?date=${logDate}" class="nav-item">
    <span class="ni-icon">📋</span>기록
  </a>
  <div class="nav-fab">
    <a href="/food/search?date=${logDate}" title="음식 추가">＋</a>
  </div>
  <a href="/food/search" class="nav-item">
    <span class="ni-icon">🔍</span>검색
  </a>
  <a href="/member/mypage" class="nav-item">
    <span class="ni-icon">👤</span>마이
  </a>
</nav>

<script>
  function moveDate(delta) {
    const d = new Date('<c:out value="${logDate}"/>');
    d.setDate(d.getDate() + delta);
    location.href = '/yamyam/list?date=' + d.toISOString().slice(0,10);
  }
</script>
</body>
</html>
