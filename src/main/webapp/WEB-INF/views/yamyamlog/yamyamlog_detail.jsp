<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>기록 상세</title>
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
    .app { max-width: 1080px; margin: 0 auto; min-height: 100vh; background: var(--bg); padding-bottom: 40px; }

    /* 식사 타입 변수 */
    <c:set var="mealColor" value="${log.mealType.name() == 'BREAKFAST' ? '#FFA940' : log.mealType.name() == 'LUNCH' ? '#2ECBBD' : log.mealType.name() == 'DINNER' ? '#9B7FFA' : '#FF7EB3'}"/>
    <c:set var="mealLabel" value="${log.mealType.name() == 'BREAKFAST' ? '아침' : log.mealType.name() == 'LUNCH' ? '점심' : log.mealType.name() == 'DINNER' ? '저녁' : '간식'}"/>
    <c:set var="mealEmoji" value="${log.mealType.name() == 'BREAKFAST' ? '🌅' : log.mealType.name() == 'LUNCH' ? '☀️' : log.mealType.name() == 'DINNER' ? '🌙' : '🍪'}"/>

    /* ── 헤더 ── */
    header {
      position: sticky; top: 0; z-index: 100;
      background: var(--surface); border-bottom: 1px solid var(--border);
      display: flex; align-items: center; height: 60px; padding: 0 28px; gap: 14px;
    }
    .back-btn {
      width: 40px; height: 40px; border-radius: 12px;
      background: var(--bg); border: 1px solid var(--border);
      color: var(--text); font-size: 1.2rem;
      display: flex; align-items: center; justify-content: center;
      text-decoration: none; transition: all .15s;
    }
    .back-btn:hover { border-color: var(--primary); color: var(--primary); }
    .header-title { flex: 1; font-weight: 700; font-size: 1.05rem; }
    .meal-badge { padding: 5px 14px; border-radius: 20px; font-size: 0.78rem; font-weight: 700; }

    /* ── 히어로 ── */
    .hero {
      background: linear-gradient(160deg, ${mealColor}18 0%, var(--bg) 55%);
      padding: 32px 32px 24px;
    }
    .hero-inner { display: flex; align-items: center; gap: 20px; margin-bottom: 28px; }
    .food-emoji-big {
      width: 80px; height: 80px; border-radius: 22px;
      display: flex; align-items: center; justify-content: center;
      font-size: 2.6rem; flex-shrink: 0;
      box-shadow: 0 4px 18px ${mealColor}30;
    }
    .food-hero-info h1 { font-size: 1.5rem; font-weight: 900; line-height: 1.2; }
    .food-hero-info .sub { font-size: 0.84rem; color: var(--text-soft); margin-top: 5px; }
    .food-type-tag {
      display: inline-block; margin-top: 8px;
      padding: 3px 12px; border-radius: 20px;
      font-size: 0.73rem; font-weight: 600;
      background: var(--border); color: var(--text-soft);
    }

    /* 칼로리 + 매크로 */
    .kcal-banner {
      background: var(--surface); border-radius: 18px;
      padding: 22px 28px; box-shadow: 0 4px 22px rgba(0,0,0,0.07);
      display: flex; align-items: center; justify-content: space-between; gap: 32px;
    }
    .kcal-main .k-label { font-size: 0.75rem; color: var(--muted); }
    .kcal-main .k-num { font-size: 2.8rem; font-weight: 900; line-height: 1; letter-spacing: -2px; }
    .kcal-main .k-unit { font-size: 0.9rem; color: var(--text-soft); }
    .macro-stack { display: flex; gap: 24px; }
    .macro-row-item { display: flex; align-items: center; gap: 8px; font-size: 0.88rem; }
    .macro-dot { width: 9px; height: 9px; border-radius: 50%; flex-shrink: 0; }
    .macro-lbl { color: var(--muted); min-width: 48px; }
    .macro-v { font-weight: 700; }

    /* ── 2열 콘텐츠 ── */
    .content-grid {
      display: grid; grid-template-columns: 1fr 1fr; gap: 20px;
      padding: 24px 28px 0;
    }
    .sec-title {
      font-size: 0.75rem; font-weight: 700; color: var(--muted);
      letter-spacing: 0.06em; text-transform: uppercase; margin-bottom: 12px;
    }

    /* 섭취량 */
    .serving-card {
      background: var(--surface); border-radius: 18px;
      padding: 22px; box-shadow: 0 2px 14px rgba(0,0,0,0.05);
    }
    .serving-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
    .serving-lbl { font-size: 0.9rem; color: var(--text-soft); }
    .serving-input-wrap { display: flex; align-items: center; gap: 8px; }
    .serving-input {
      width: 90px; padding: 10px 14px; text-align: right;
      background: var(--bg); border: 1.5px solid var(--border);
      border-radius: 11px; color: var(--text);
      font-family: 'Noto Sans KR', sans-serif; font-size: 1rem; font-weight: 700;
      outline: none; transition: border-color 0.15s;
    }
    .serving-input:focus { border-color: var(--primary); }
    .serving-unit { font-size: 0.85rem; color: var(--muted); }
    .serving-divider { height: 1px; background: var(--border); margin: 0 0 16px; }
    .ref-note { font-size: 0.78rem; color: var(--muted); text-align: center; }

    /* 영양소 테이블 */
    .nutrient-card {
      background: var(--surface); border-radius: 18px;
      overflow: hidden; box-shadow: 0 2px 14px rgba(0,0,0,0.05);
    }
    .n-row {
      display: flex; align-items: center;
      padding: 13px 20px; border-bottom: 1px solid var(--border); font-size: 0.88rem;
    }
    .n-row:last-child { border-bottom: none; }
    .n-row.highlight { background: var(--primary-light); }
    .n-name { flex: 1; color: var(--text-soft); }
    .n-row.highlight .n-name { color: var(--text); font-weight: 600; }
    .n-per100 { font-size: 0.75rem; color: var(--muted); margin-right: 16px; }
    .n-actual { font-weight: 700; min-width: 80px; text-align: right; }

    /* 상품 링크 */
    .product-link {
      display: flex; align-items: center; gap: 14px;
      background: var(--surface); border-radius: 18px;
      padding: 16px 20px; text-decoration: none; color: var(--text);
      box-shadow: 0 2px 14px rgba(0,0,0,0.05); transition: box-shadow 0.15s;
    }
    .product-link:hover { box-shadow: 0 4px 22px rgba(255,107,107,0.15); }
    .pl-icon { font-size: 1.6rem; }
    .pl-info { flex: 1; }
    .pl-info strong { display: block; font-size: 0.92rem; }
    .pl-info span { font-size: 0.78rem; color: var(--muted); }

    /* 액션 버튼 */
    .actions { padding: 20px 28px 0; display: flex; gap: 12px; max-width: 500px; }
    .btn-base {
      flex: 1; padding: 15px; border-radius: 14px; border: none;
      font-family: 'Noto Sans KR', sans-serif;
      font-size: 0.95rem; font-weight: 700; cursor: pointer; transition: all 0.15s;
    }
    .btn-delete { background: #FFF0F0; color: #E85555; border: 1.5px solid #FFD5D5; }
    .btn-delete:hover { background: #FFE0E0; }
    .btn-save { background: var(--primary); color: #fff; box-shadow: 0 4px 18px rgba(255,107,107,0.3); }
    .btn-save:hover { background: var(--primary-dark); }
  </style>
</head>
<body>
<div class="app">

  <header>
    <a href="javascript:history.back()" class="back-btn">&#8249;</a>
    <div class="header-title">기록 상세</div>
    <div class="meal-badge" style="background:${mealColor}18;color:${mealColor};">
      ${mealEmoji} ${mealLabel}
    </div>
  </header>

  <!-- 히어로 -->
  <div class="hero">
    <div class="hero-inner">
      <div class="food-emoji-big" style="background:${mealColor}18;">${mealEmoji}</div>
      <div class="food-hero-info">
        <h1><c:out value="${food.foodName}"/></h1>
        <div class="sub"><c:out value="${log.mealDate}"/> &nbsp;·&nbsp; ${mealLabel}</div>
        <span class="food-type-tag">
          <c:choose>
            <c:when test="${food.foodType.name() == 'DISH'}">🍳 요리</c:when>
            <c:otherwise>🛒 상품</c:otherwise>
          </c:choose>
        </span>
      </div>
    </div>
    <div class="kcal-banner">
      <div class="kcal-main">
        <div class="k-label">섭취 칼로리</div>
        <div>
          <span class="k-num" style="color:${mealColor};" id="d-energy">
            <fmt:formatNumber value="${log.actualEnergy}" pattern="#,###"/>
          </span>
          <span class="k-unit">kcal</span>
        </div>
      </div>
      <div class="macro-stack">
        <div class="macro-row-item">
          <div class="macro-dot" style="background:#5B8CFF;"></div>
          <span class="macro-lbl">탄수화물</span>
          <span class="macro-v" style="color:#5B8CFF;" id="d-carbs">
            <fmt:formatNumber value="${log.actualCarbs}" pattern="#0.#"/>g
          </span>
        </div>
        <div class="macro-row-item">
          <div class="macro-dot" style="background:#2ECBBD;"></div>
          <span class="macro-lbl">단백질</span>
          <span class="macro-v" style="color:#2ECBBD;" id="d-protein">
            <fmt:formatNumber value="${log.actualProtein}" pattern="#0.#"/>g
          </span>
        </div>
        <div class="macro-row-item">
          <div class="macro-dot" style="background:#FFA940;"></div>
          <span class="macro-lbl">지방</span>
          <span class="macro-v" style="color:#FFA940;" id="d-fat">
            <fmt:formatNumber value="${log.actualFat}" pattern="#0.#"/>g
          </span>
        </div>
      </div>
    </div>
  </div>

  <!-- 2열 콘텐츠 -->
  <div class="content-grid">
    <!-- 좌: 섭취량 + 상품링크 -->
    <div>
      <div class="sec-title">섭취량</div>
      <div class="serving-card">
        <div class="serving-row">
          <span class="serving-lbl">섭취한 양</span>
          <div class="serving-input-wrap">
            <input type="number" class="serving-input" id="servingInput"
                   value="<fmt:formatNumber value='${log.servingSize}' pattern='#0.#'/>"
                   min="0" step="0.5" oninput="recalc()">
            <span class="serving-unit">g</span>
          </div>
        </div>
        <div class="serving-divider"></div>
        <div class="ref-note">
          기준량 <strong><c:out value="${food.referenceAmount}"/></strong> &nbsp;·&nbsp;
          1회 제공량 <strong><c:out value="${food.foodWeight}"/></strong>
        </div>
      </div>

      <c:if test="${food.foodType.name() == 'PRODUCT' and not empty food.url}">
        <div style="margin-top:16px;">
          <div class="sec-title">구매 링크</div>
          <a href="${food.url}" target="_blank" class="product-link">
            <div class="pl-icon">🛒</div>
            <div class="pl-info">
              <strong>마켓컬리에서 보기</strong>
              <span><c:out value="${food.foodName}"/> 검색 결과</span>
            </div>
            <span style="color:var(--muted);">›</span>
          </a>
        </div>
      </c:if>
    </div>

    <!-- 우: 영양성분 -->
    <div>
      <div class="sec-title">영양성분</div>
      <div class="nutrient-card">
        <div class="n-row highlight">
          <span class="n-name">에너지</span>
          <span class="n-per100"><fmt:formatNumber value="${food.energy}" pattern="#0.#"/> kcal/100g</span>
          <span class="n-actual" style="color:var(--primary);" id="t-energy">
            <fmt:formatNumber value="${log.actualEnergy}" pattern="#,##0.#"/> kcal
          </span>
        </div>
        <div class="n-row">
          <span class="n-name">탄수화물</span>
          <span class="n-per100"><fmt:formatNumber value="${food.carbs}" pattern="#0.#"/>g/100g</span>
          <span class="n-actual" style="color:#5B8CFF;" id="t-carbs">
            <fmt:formatNumber value="${log.actualCarbs}" pattern="#0.#"/>g
          </span>
        </div>
        <div class="n-row">
          <span class="n-name" style="padding-left:12px;">당류</span>
          <span class="n-per100"><fmt:formatNumber value="${food.sugar}" pattern="#0.#"/>g/100g</span>
          <span class="n-actual" id="t-sugar">
            <fmt:formatNumber value="${log.actualCarbs * (food.sugar / (food.carbs > 0 ? food.carbs : 1))}" pattern="#0.#"/>g
          </span>
        </div>
        <div class="n-row">
          <span class="n-name">단백질</span>
          <span class="n-per100"><fmt:formatNumber value="${food.protein}" pattern="#0.#"/>g/100g</span>
          <span class="n-actual" style="color:#2ECBBD;" id="t-protein">
            <fmt:formatNumber value="${log.actualProtein}" pattern="#0.#"/>g
          </span>
        </div>
        <div class="n-row">
          <span class="n-name">지방</span>
          <span class="n-per100"><fmt:formatNumber value="${food.fat}" pattern="#0.#"/>g/100g</span>
          <span class="n-actual" style="color:#FFA940;" id="t-fat">
            <fmt:formatNumber value="${log.actualFat}" pattern="#0.#"/>g
          </span>
        </div>
        <div class="n-row">
          <span class="n-name">나트륨</span>
          <span class="n-per100"><fmt:formatNumber value="${food.sodium}" pattern="#0.#"/>mg/100g</span>
          <span class="n-actual" id="t-sodium">
            <fmt:formatNumber value="${food.sodium * log.servingSize / 100}" pattern="#0.#"/>mg
          </span>
        </div>
      </div>
    </div>
  </div>

  <!-- 액션 버튼 -->
  <div class="actions">
    <form action="/api/yamyam/delete" method="post" style="flex:1;">
      <input type="hidden" name="logId" value="${log.logId}"/>
      <input type="hidden" name="date"  value="${log.mealDate}"/>
      <button type="submit" class="btn-base btn-delete"
              onclick="return confirm('이 기록을 삭제할까요?')">🗑 삭제</button>
    </form>
    <form action="/api/yamyam/update" method="post" style="flex:1;">
      <input type="hidden" name="logId"       value="${log.logId}"/>
      <input type="hidden" name="servingSize" id="hiddenServing" value="${log.servingSize}"/>
      <button type="submit" class="btn-base btn-save" onclick="syncServing()">✓ 저장</button>
    </form>
  </div>

</div>

<script>
  const per100 = {
    energy: ${food.energy}, carbs: ${food.carbs}, sugar: ${food.sugar},
    protein: ${food.protein}, fat: ${food.fat}, sodium: ${food.sodium}
  };

  function recalc() {
    const g = parseFloat(document.getElementById('servingInput').value) || 0;
    const r = g / 100;
    const f = (v, d=1) => v.toFixed(d).replace(/\B(?=(\d{3})+(?!\d))/g,',');
    document.getElementById('d-energy').textContent  = f(per100.energy  * r, 0);
    document.getElementById('d-carbs').textContent   = f(per100.carbs   * r) + 'g';
    document.getElementById('d-protein').textContent = f(per100.protein * r) + 'g';
    document.getElementById('d-fat').textContent     = f(per100.fat     * r) + 'g';
    document.getElementById('t-energy').textContent  = f(per100.energy  * r, 1) + ' kcal';
    document.getElementById('t-carbs').textContent   = f(per100.carbs   * r) + 'g';
    document.getElementById('t-sugar').textContent   = f(per100.sugar   * r) + 'g';
    document.getElementById('t-protein').textContent = f(per100.protein * r) + 'g';
    document.getElementById('t-fat').textContent     = f(per100.fat     * r) + 'g';
    document.getElementById('t-sodium').textContent  = f(per100.sodium  * r) + 'mg';
  }
  function syncServing() {
    document.getElementById('hiddenServing').value = document.getElementById('servingInput').value;
  }
</script>
</body>
</html>
