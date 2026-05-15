<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>음식 검색</title>
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
    .app { max-width: 1080px; margin: 0 auto; min-height: 100vh; background: var(--bg); padding-bottom: 110px; }

    /* ── 검색 헤더 ── */
    .search-header {
      position: sticky; top: 0; z-index: 100;
      background: var(--surface); border-bottom: 1px solid var(--border);
      padding: 14px 28px 12px;
    }
    .search-row { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
    .back-btn {
      width: 40px; height: 40px; border-radius: 12px;
      background: var(--bg); border: 1px solid var(--border);
      color: var(--text); font-size: 1.2rem;
      display: flex; align-items: center; justify-content: center;
      text-decoration: none; flex-shrink: 0; transition: all .15s;
    }
    .back-btn:hover { border-color: var(--primary); color: var(--primary); }
    .search-box { flex: 1; position: relative; }
    .search-box input {
      width: 100%; padding: 13px 48px 13px 20px;
      background: var(--bg); border: 1.5px solid var(--border);
      border-radius: 14px; color: var(--text);
      font-family: 'Noto Sans KR', sans-serif; font-size: 0.95rem;
      outline: none; transition: border-color .15s, box-shadow .15s;
    }
    .search-box input:focus { border-color: var(--primary); box-shadow: 0 0 0 3px rgba(255,107,107,0.1); }
    .search-box input::placeholder { color: var(--muted); }
    .search-icon { position: absolute; right: 16px; top: 50%; transform: translateY(-50%); color: var(--muted); pointer-events: none; }

    /* 필터 칩 */
    .filter-row { display: flex; gap: 8px; }
    .filter-chip {
      padding: 7px 18px; border-radius: 20px; white-space: nowrap;
      font-size: 0.82rem; font-weight: 500; cursor: pointer;
      border: 1.5px solid var(--border); background: transparent;
      color: var(--muted); transition: all .15s;
    }
    .filter-chip.active { border-color: var(--primary); color: var(--primary); background: var(--primary-light); }
    .filter-chip:hover:not(.active) { border-color: #ccc; color: var(--text-soft); }

    /* 결과 메타 */
    .result-meta { padding: 12px 28px 4px; font-size: 0.82rem; color: var(--muted); }
    .result-meta strong { color: var(--primary); }

    /* ── 음식 카드 (2열 그리드) ── */
    .food-list {
      padding: 4px 28px;
      display: grid; grid-template-columns: 1fr 1fr; gap: 10px;
    }
    .food-card {
      background: var(--surface); border: 1.5px solid var(--border);
      border-radius: 16px; padding: 16px 18px;
      display: flex; align-items: center; gap: 14px;
      cursor: pointer; transition: all .15s;
    }
    .food-card:hover { border-color: var(--primary); box-shadow: 0 4px 18px rgba(255,107,107,0.1); }
    .food-card.selected { border-color: var(--primary); background: var(--primary-light); box-shadow: 0 4px 18px rgba(255,107,107,0.13); }
    .food-type-icon {
      width: 50px; height: 50px; border-radius: 14px;
      display: flex; align-items: center; justify-content: center;
      font-size: 1.3rem; flex-shrink: 0;
    }
    .icon-dish    { background: #E8FAF9; }
    .icon-product { background: #FFF4EC; }
    .food-info { flex: 1; min-width: 0; }
    .food-name { font-weight: 600; font-size: 0.93rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .food-cat { font-size: 0.74rem; color: var(--muted); margin-top: 3px; }
    .food-macros { display: flex; gap: 6px; margin-top: 8px; flex-wrap: wrap; }
    .food-macros span {
      font-size: 0.7rem; padding: 2px 8px; border-radius: 8px;
      background: var(--bg); color: var(--text-soft); border: 1px solid var(--border);
    }
    .food-kcal { text-align: right; flex-shrink: 0; }
    .food-kcal .k { font-size: 1.15rem; font-weight: 900; color: var(--primary); }
    .food-kcal .u { font-size: 0.68rem; color: var(--muted); }
    .food-kcal .ref { font-size: 0.64rem; color: var(--muted); display: block; }

    /* 빈 상태 */
    .empty-state { grid-column: 1 / -1; padding: 70px 24px; text-align: center; color: var(--muted); }
    .empty-state .em { font-size: 4rem; margin-bottom: 14px; }
    .empty-state p { font-size: 0.92rem; line-height: 1.7; }

    /* 하단 선택 바 */
    .select-bar {
      position: fixed; bottom: 0; left: 50%; transform: translateX(-50%);
      width: 100%; max-width: 1080px;
      padding: 14px 28px 26px;
      background: var(--surface); border-top: 1px solid var(--border);
      box-shadow: 0 -4px 22px rgba(0,0,0,0.08);
      display: none; z-index: 200;
    }
    .select-bar.visible { display: block; }
    .select-bar-inner { display: flex; gap: 12px; align-items: center; }
    .selected-food-name { flex: 1; font-weight: 600; font-size: 0.92rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .btn-select {
      padding: 13px 32px; border-radius: 14px;
      background: var(--primary); color: #fff; border: none;
      font-size: 0.92rem; font-weight: 700; cursor: pointer;
      transition: all .15s; white-space: nowrap;
      box-shadow: 0 4px 18px rgba(255,107,107,0.3);
    }
    .btn-select:hover { background: var(--primary-dark); }
  </style>
</head>
<body>
<div class="app">

  <div class="search-header">
    <div class="search-row">
      <a href="javascript:history.back()" class="back-btn">&#8249;</a>
      <form action="/food/search" method="get" class="search-box">
        <input type="hidden" name="mealType"   value="${mealType}"/>
        <input type="hidden" name="date"       value="${date}"/>
        <input type="hidden" name="typeFilter" id="typeFilterInput" value="${typeFilter}"/>
        <input type="text"   name="keyword" id="searchInput"
               value="<c:out value='${keyword}'/>"
               placeholder="음식 이름을 검색해보세요 🔍"
               autofocus autocomplete="off"/>
        <span class="search-icon">🔍</span>
      </form>
    </div>
    <div class="filter-row">
      <button class="filter-chip ${(typeFilter == 'ALL' or empty typeFilter) ? 'active' : ''}" onclick="setFilter('ALL')">🍽 전체</button>
      <button class="filter-chip ${typeFilter == 'DISH' ? 'active' : ''}"    onclick="setFilter('DISH')">🍳 요리</button>
      <button class="filter-chip ${typeFilter == 'PRODUCT' ? 'active' : ''}" onclick="setFilter('PRODUCT')">🛒 상품</button>
    </div>
  </div>

  <c:if test="${not empty keyword}">
    <div class="result-meta">
      "<strong><c:out value="${keyword}"/></strong>" 검색결과 &nbsp;<strong>${total}</strong>개
    </div>
  </c:if>

  <div class="food-list">
    <c:choose>
      <c:when test="${not empty foodList}">
        <c:forEach var="food" items="${foodList}">
          <div class="food-card" id="card-${food.foodId}"
               onclick="selectFood(${food.foodId}, '<c:out value="${food.foodName}"/>')">
            <div class="food-type-icon ${food.foodType.name() == 'DISH' ? 'icon-dish' : 'icon-product'}">
              <c:choose>
                <c:when test="${food.foodType.name() == 'DISH'}">🍳</c:when>
                <c:otherwise>🛒</c:otherwise>
              </c:choose>
            </div>
            <div class="food-info">
              <div class="food-name"><c:out value="${food.foodName}"/></div>
              <c:if test="${not empty food.subCategory}">
                <div class="food-cat"><c:out value="${food.subCategory}"/></div>
              </c:if>
              <div class="food-macros">
                <span>탄 <fmt:formatNumber value="${food.carbs}"   pattern="#0.#"/>g</span>
                <span>단 <fmt:formatNumber value="${food.protein}" pattern="#0.#"/>g</span>
                <span>지 <fmt:formatNumber value="${food.fat}"     pattern="#0.#"/>g</span>
              </div>
            </div>
            <div class="food-kcal">
              <div class="k"><fmt:formatNumber value="${food.energy}" pattern="#,###"/></div>
              <div class="u">kcal</div>
              <span class="ref"><c:out value="${food.referenceAmount}"/> 기준</span>
            </div>
          </div>
        </c:forEach>
      </c:when>
      <c:when test="${not empty keyword}">
        <div class="empty-state">
          <div class="em">🤔</div>
          <p>"<strong><c:out value="${keyword}"/></strong>"에 대한<br>검색 결과가 없어요.<br>다른 키워드로 시도해보세요!</p>
        </div>
      </c:when>
      <c:otherwise>
        <div class="empty-state">
          <div class="em">🍽️</div>
          <p>오늘 뭐 먹었나요?<br>음식 이름을 검색해서<br>식단을 기록해보세요!</p>
        </div>
      </c:otherwise>
    </c:choose>
  </div>

</div>

<div class="select-bar" id="selectBar">
  <div class="select-bar-inner">
    <span class="selected-food-name" id="selectedFoodName">—</span>
    <button class="btn-select" onclick="goToSelect()">이 음식 선택 →</button>
  </div>
</div>

<script>
  let selectedFoodId = null;

  function selectFood(foodId, foodName) {
    if (selectedFoodId) document.getElementById('card-' + selectedFoodId)?.classList.remove('selected');
    if (selectedFoodId === foodId) {
      selectedFoodId = null;
      document.getElementById('selectBar').classList.remove('visible');
      return;
    }
    selectedFoodId = foodId;
    document.getElementById('card-' + foodId).classList.add('selected');
    document.getElementById('selectedFoodName').textContent = foodName;
    document.getElementById('selectBar').classList.add('visible');
  }

  function goToSelect() {
    if (!selectedFoodId) return;
    const mealType = '<c:out value="${mealType}"/>';
    const date     = '<c:out value="${date}"/>';
    location.href = `/food/select?foodId=${selectedFoodId}&mealType=${mealType}&date=${date}`;
  }

  function setFilter(type) {
    document.getElementById('typeFilterInput').value = type;
    document.getElementById('searchInput').closest('form').submit();
  }
</script>
</body>
</html>
