package com.ssafy.yamyam.controller;

import jakarta.servlet.http.HttpSession;

public abstract class BaseController {

    protected Long getMemberId(HttpSession session) {
        Object id = session.getAttribute("memberId");
        return id != null ? (Long) id : 1L;
    }
}
