package com.ssafy.yamyam.domain.user.mapper;


import com.ssafy.yamyam.domain.user.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScans;
import org.mybatis.spring.annotation.MapperScans;
import org.mybatis.spring.annotation.MapperScans;

@Mapper
public interface UserMapper {

    void insertUser(User user);

    User findByUserId(String userId);

    User findById(Long id);

    void updateUserProfile(User user);
}
