<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>섭취량 입력</title>
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

    /* ── 음식 히어로 ── */
    .food-hero {
      background: linear-gradient(160deg, #FFE4E4 0%, var(--bg) 55%);
      padding: 24px 32px;
      display: flex; align-items: center; gap: 18px;
    }
    .food-emoji-wrap {
      width: 68px; height: 68px; border-radius: 20px;
      background: var(--primary-light);
      display: flex; align-items: center; justify-content: center;
      font-size: 2rem; flex-shrink: 0;
      box-shadow: 0 4px 14px rgba(255,107,107,0.2);
    }
    .food-hero-info h2 { font-size: 1.2rem; font-weight: 900; line-height: 1.25; }
    .food-hero-info .sub { font-size: 0.8rem; color: var(--text-soft); margin-top: 5px; }

    /* ── 2열 콘텐츠 ── */
    .content-grid {
      display: grid; grid-template-columns: 1fr 1fr; gap: 20px;
      padding: 24px 28px 0;
    }
    .sec-title {
      font-size: 0.75rem; font-weight: 700; color: var(--muted);
      letter-spacing: 0.06em; text-transform: uppercase; margin-bottom: 10px;
    }

    /* 식사 시간 */
    .meal-grid { display: grid; grid-template-columns: repeat(4,1fr); gap: 8px; margin-bottom: 20px; }
    .meal-chip {
      padding: 13px 4px; border-radius: 14px;
      border: 1.5px solid var(--border); background: var(--surface);
      color: var(--muted); text-align: center;
      cursor: pointer; transition: all .15s; font-size: 0.82rem; font-weight: 500;
      box-shadow: 0 1px 5px rgba(0,0,0,0.04);
    }
    .meal-chip .ce { display: block; font-size: 1.5rem; margin-bottom: 4px; }
    .meal-chip.active { border-color: var(--primary); color: var(--primary); background: var(--primary-light); box-shadow: 0 2px 14px rgba(255,107,107,0.15); }

    /* 섭취량 카드 */
    .serving-card {
      background: var(--surface); border-radius: 18px;
      padding: 22px; box-shadow: 0 2px 14px rgba(0,0,0,0.06);
    }
    .serving-display { text-align: center; margin-bottom: 24px; }
    .serving-display .big { font-size: 4rem; font-weight: 900; color: var(--primary); line-height: 1; letter-spacing: -2px; }
    .serving-display .unit { font-size: 1rem; color: var(--text-soft); margin-top: 4px; }
    .serving-display .ref  { font-size: 0.74rem; color: var(--muted); margin-top: 6px; }

    input[type="range"] {
      width: 100%; -webkit-appearance: none;
      height: 5px; border-radius: 3px;
      background: linear-gradient(90deg, var(--primary) 0%, var(--border) 0%);
      outline: none; margin-bottom: 8px;
    }
    input[type="range"]::-webkit-slider-thumb {
      -webkit-appearance: none; width: 26px; height: 26px; border-radius: 50%;
      background: var(--primary); cursor: pointer;
      box-shadow: 0 2px 10px rgba(255,107,107,0.4); border: 3px solid #fff;
    }
    .slider-marks { display: flex; justify-content: space-between; font-size: 0.7rem; color: var(--muted); margin-bottom: 18px; }

    .manual-input-wrap { display: flex; align-items: center; gap: 10px; }
    .manual-input {
      flex: 1; padding: 12px 16px;
      background: var(--bg); border: 1.5px solid var(--border);
      border-radius: 12px; color: var(--text);
      font-family: 'Noto Sans KR', sans-serif; font-size: 1.05rem; font-weight: 700;
      outline: none; transition: border-color .15s;
    }
    .manual-input:focus { border-color: var(--primary); }
    .manual-unit { color: var(--muted); font-size: 0.9rem; }

    .quick-row { display: flex; gap: 8px; margin-top: 16px; flex-wrap: wrap; }
    .quick-btn {
      padding: 8px 16px; border-radius: 20px;
      border: 1.5px solid var(--border); background: var(--surface);
      color: var(--text-soft); font-size: 0.82rem; font-weight: 500;
      cursor: pointer; transition: all .15s;
    }
    .quick-btn:hover { border-color: var(--primary); color: var(--primary); background: var(--primary-light); }

    /* 영양소 프리뷰 */
    .nutrient-preview { background: var(--surface); border-radius: 16px; overflow: hidden; box-shadow: 0 2px 14px rgba(0,0,0,0.05); }
    .np-header { padding: 13px 18px; border-bottom: 1px solid var(--border); font-size: 0.78rem; font-weight: 600; color: var(--muted); }
    .np-grid { display: grid; grid-template-columns: 1fr 1fr; }
    .np-item {
      padding: 16px 18px; border-bottom: 1px solid var(--border); border-right: 1px solid var(--border);
    }
    .np-item:nth-child(2n) { border-right: none; }
    .np-item:nth-last-child(-n+2) { border-bottom: none; }
    .np-label { font-size: 0.75rem; color: var(--muted); margin-bottom: 5px; }
    .np-val { font-size: 1.15rem; font-weight: 900; }

    /* 상품 링크 */
    .product-link-mini {
      display: flex; align-items: center; gap: 12px;
      background: var(--surface); border-radius: 14px;
      padding: 14px 18px; text-decoration: none; color: var(--text);
      font-size: 0.88rem; box-shadow: 0 2px 12px rgba(0,0,0,0.05);
      transition: box-shadow .15s; margin-top: 14px;
    }
    .product-link-mini:hover { box-shadow: 0 4px 22px rgba(255,107,107,0.15); }

    /* 저장 버튼 */
    .save-wrap { grid-column: 1 / -1; padding-top: 4px; }
    .btn-save {
      width: 100%; padding: 18px;
      background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
      color: #fff; border: none; border-radius: 16px;
      font-family: 'Noto Sans KR', sans-serif;
      font-size: 1.05rem; font-weight: 700; cursor: pointer;
      transition: all .18s; letter-spacing: -.3px;
      box-shadow: 0 6px 26px rgba(255,107,107,0.35);
    }
    .btn-save:hover { transform: translateY(-2px); box-shadow: 0 8px 32px rgba(255,107,107,0.45); }
    .btn-save:active { transform: translateY(0); }
  </style>
</head>
<body>
<div class="app">

  <header>
    <a href="javascript:history.back()" class="back-btn">&#8249;</a>
    <div class="header-title">섭취량 입력</div>
  </header>

  <div class="food-hero">
    <div class="food-emoji-wrap">
      <c:choose>
        <c:when test="${food.foodType.name() == 'DISH'}">🍳</c:when>
        <c:otherwise>🛒</c:otherwise>
      </c:choose>
    </div>
    <div class="food-hero-info">
      <h2><c:out value="${food.foodName}"/></h2>
      <div class="sub">
        <c:if test="${not empty food.subCategory}"><c:out value="${food.subCategory}"/> &nbsp;·&nbsp;</c:if>
        <c:choose>
          <c:when test="${food.foodType.name() == 'DISH'}">요리</c:when>
          <c:otherwise>상품</c:otherwise>
        </c:choose>
        &nbsp;·&nbsp; 기준 <c:out value="${food.referenceAmount}"/>
      </div>
    </div>
  </div>

  <form action="/api/yamyam/add" method="post" id="logForm">
    <input type="hidden" name="foodId"      value="${food.foodId}"/>
    <input type="hidden" name="mealDate"    value="${date}"/>
    <input type="hidden" name="servingSize" id="hiddenServing"/>
    <input type="hidden" name="mealType"    id="hiddenMealType" value="${mealType}"/>

    <div class="content-grid">
      <!-- 좌: 식사시간 + 섭취량 슬라이더 -->
      <div>
        <div class="sec-title">식사 시간</div>
        <div class="meal-grid">
          <c:forEach var="mt" items="${['BREAKFAST','LUNCH','DINNER','SNACK']}">
            <c:set var="lbl" value="${mt == 'BREAKFAST' ? '아침' : mt == 'LUNCH' ? '점심' : mt == 'DINNER' ? '저녁' : '간식'}"/>
            <c:set var="em"  value="${mt == 'BREAKFAST' ? '🌅'  : mt == 'LUNCH' ? '☀️'  : mt == 'DINNER' ? '🌙'  : '🍪'}"/>
            <div class="meal-chip ${mealType == mt ? 'active' : ''}" id="chip-${mt}" onclick="selectMeal('${mt}')">
              <span class="ce">${em}</span>${lbl}
            </div>
          </c:forEach>
        </div>

        <div class="sec-title">섭취량</div>
        <div class="serving-card">
          <div class="serving-display">
            <div class="big" id="servingDisplay">100</div>
            <div class="unit">g</div>
            <div class="ref">기준 <c:out value="${food.referenceAmount}"/> &nbsp;/&nbsp; 1회 <c:out value="${food.foodWeight}"/></div>
          </div>
          <input type="range" id="servingSlider" min="10" max="500" step="5" value="100" oninput="onSlide(this.value)">
          <div class="slider-marks"><span>10g</span><span>500g</span></div>
          <div class="manual-input-wrap">
            <input type="number" id="servingManual" class="manual-input"
                   min="1" max="2000" value="100" oninput="onManual(this.value)" placeholder="직접 입력 (g)">
            <span class="manual-unit">g</span>
          </div>
          <div class="quick-row">
            <button type="button" class="quick-btn" onclick="setServing(50)">50g</button>
            <button type="button" class="quick-btn" onclick="setServing(100)">100g</button>
            <button type="button" class="quick-btn" onclick="setServing(150)">150g</button>
            <button type="button" class="quick-btn" onclick="setServing(200)">200g</button>
            <button type="button" class="quick-btn" onclick="setServing(300)">300g</button>
          </div>
        </div>
      </div>

      <!-- 우: 영양소 프리뷰 + 상품링크 -->
      <div>
        <div class="sec-title">섭취 영양소 미리보기</div>
        <div class="nutrient-preview">
          <div class="np-header">입력한 섭취량 기준 영양소</div>
          <div class="np-grid">
            <div class="np-item">
              <div class="np-label">에너지</div>
              <div class="np-val" style="color:var(--primary);" id="pv-energy">—</div>
            </div>
            <div class="np-item">
              <div class="np-label">탄수화물</div>
              <div class="np-val" style="color:#5B8CFF;" id="pv-carbs">—</div>
            </div>
            <div class="np-item">
              <div class="np-label">단백질</div>
              <div class="np-val" style="color:#2ECBBD;" id="pv-protein">—</div>
            </div>
            <div class="np-item">
              <div class="np-label">지방</div>
              <div class="np-val" style="color:#FFA940;" id="pv-fat">—</div>
            </div>
          </div>
        </div>

        <c:if test="${food.foodType.name() == 'PRODUCT' and not empty food.url}">
          <a href="${food.url}" target="_blank" class="product-link-mini">
            🛒 &nbsp; 마켓컬리에서 <strong><c:out value="${food.foodName}"/></strong> 주문하기 &nbsp;›
          </a>
        </c:if>
      </div>

      <!-- 저장 버튼 (전체 너비) -->
      <div class="save-wrap">
        <button type="button" class="btn-save" onclick="submitLog()">
          기록 저장하기 🎉
        </button>
      </div>
    </div>
  </form>

</div>

<script>
  const per100 = {
    energy: ${food.energy}, carbs: ${food.carbs}, protein: ${food.protein}, fat: ${food.fat}
  };
  let currentServing = 100;

  function calcAndDisplay(g) {
    currentServing = g;
    const r = g / 100;
    document.getElementById('servingDisplay').textContent = g;
    document.getElementById('pv-energy').textContent  = (per100.energy  * r).toFixed(0) + ' kcal';
    document.getElementById('pv-carbs').textContent   = (per100.carbs   * r).toFixed(1) + 'g';
    document.getElementById('pv-protein').textContent = (per100.protein * r).toFixed(1) + 'g';
    document.getElementById('pv-fat').textContent     = (per100.fat     * r).toFixed(1) + 'g';
    const pct = Math.min((g - 10) / (500 - 10) * 100, 100);
    document.getElementById('servingSlider').style.background =
      `linear-gradient(90deg, var(--primary) ${pct}%, var(--border) ${pct}%)`;
  }
  function onSlide(v)  { document.getElementById('servingManual').value = v; calcAndDisplay(parseInt(v)); }
  function onManual(v) { const g = Math.max(1, Math.min(2000, parseInt(v)||0)); if (g<=500) document.getElementById('servingSlider').value = g; calcAndDisplay(g); }
  function setServing(g) { document.getElementById('servingSlider').value = Math.min(g,500); document.getElementById('servingManual').value = g; calcAndDisplay(g); }
  function selectMeal(mt) {
    document.querySelectorAll('.meal-chip').forEach(c => c.classList.remove('active'));
    document.getElementById('chip-' + mt).classList.add('active');
    document.getElementById('hiddenMealType').value = mt;
  }
  function submitLog() {
    if (!document.getElementById('hiddenMealType').value) { alert('식사 시간을 선택해주세요!'); return; }
    document.getElementById('hiddenServing').value = currentServing;
    document.getElementById('logForm').submit();
  }
  calcAndDisplay(100);
</script>
</body>
</html>
